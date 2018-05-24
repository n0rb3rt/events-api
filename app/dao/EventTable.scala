package dao

import java.sql.Date

import clients.CassandraClient
import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.{QueryBuilder, Select}
import com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction
import com.datastax.driver.core.schemabuilder.{SchemaBuilder, SchemaStatement}
import javax.inject._
import models._
import util._

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Singleton
class EventTable @Inject()(client: CassandraClient) extends FutureAdapter {
  val ColumnFamily = "events"
//  this.createSchema()
//    Await.result(this.createSchema(), 5.seconds)

  /** Save an Event model to Cassandra table
    *
    * @param e Event class
    * @return A Cassandra ResultSet
    */
  def save(e: Event): Future[ResultSet] = {
    val saveQuery = QueryBuilder.insertInto(client.Keyspace, ColumnFamily)
      .value("src_id", e.srcId)
      .value("ts", e.ts)
      .value("count", e.count)
      //.ifNotExists()
      .setConsistencyLevel(ConsistencyLevel.ONE) // Replication is 1
    client.session.executeAsync(saveQuery)
  }

  /** Get latest Event for a given srcId
    *
    * @param srcId Event srcId
    * @return Some Event, or None if srcId not found
    */
  def getLatest(srcId: String): Future[Option[Event]] = {
    val findLatest: Statement = QueryBuilder
      .select()
      .all()
      .from(client.Keyspace, ColumnFamily)
      .where(QueryBuilder.eq("src_id", srcId))
      .limit(1)
      .setConsistencyLevel(ConsistencyLevel.ONE)

    client.session.executeAsync(findLatest).map {
      case fr if fr.isExhausted => None
      case fr => Some(fromRow(fr.one()))
    }
  }


  /** Find Events for a srcId within a time range
    *
    * @param srcId Event srcId
    * @param from  A starting timestamp (optional)
    * @param to    An ending timestamp (optional)
    * @return Some sequence of Events, or empty iterator if no Events found
    */
  def getRange(srcId: String, from: Option[Date] = None, to: Option[Date] = None): Future[Iterator[Event]] = {
    var queryOnTs: Select.Where = QueryBuilder
      .select().all()
      .from(client.Keyspace, ColumnFamily)
      .where(QueryBuilder.eq("src_id", srcId))
    if (from.isDefined) {
      queryOnTs = queryOnTs.and(QueryBuilder.gt("ts", from.get))
    }
    if (to.isDefined) {
      queryOnTs = queryOnTs.and(QueryBuilder.gt("ts", to.get))
    }
    client.session.executeAsync(queryOnTs).map(itr => itr.iterator().map(x => fromRow(x)))
  }

  /** Summarize a sequence of Events
    *
    * @param srcId Event srcId
    * @param from  A starting timestamp (optional)
    * @param to    An ending timestamp (opttional)
    * @return Some summary of Events, or None if not found
    */
  //  def getSummary(srcId: String, from: Option[Date] = None, to: Option[Date] = None): Future[Option[EventSummary]] = ???

  /** Convert a Cassandra Row into an Event model
    *
    * @param row Cassandra Row (from query result)
    * @return Event model
    */
  private def fromRow(row: Row): Event = {
    Event(row.getString("src_id"), row.getDate("ts"), row.getInt("count"))
  }

  private val create: SchemaStatement = {
    val cassSchema =
      s"""
         |CREATE TABLE IF NOT EXISTS ${client.Keyspace}.$ColumnFamily
         |src_id TEXT,
         |ts timestamp,
         |count int
         |PRIMARY KEY (src_id, ts) WITH CLUSTERING ORDER BY ts DESC;
      """.stripMargin
    SchemaBuilder
      .createTable(client.Keyspace, ColumnFamily)
      .ifNotExists()
      .addPartitionKey("src_id", DataType.text())
      .addClusteringColumn("ts", DataType.timestamp())
      .addColumn("count", DataType.cint())
      .withOptions().clusteringOrder("ts", Direction.DESC)
  } //SchemaBuilder.createTable...

  /** Create the Cassandra column family
    *
    * @return ResultSet
    */
  def createSchema(): ResultSet = client.session.execute(create)
}