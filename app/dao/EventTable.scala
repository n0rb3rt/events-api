package dao

import java.util.Date

import clients.CassandraClient
import com.datastax.driver.core.schemabuilder.SchemaStatement
import com.datastax.driver.core.{ResultSet, Row}
import javax.inject._
import models._
import util._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@Singleton
class EventTable @Inject()(client: CassandraClient) extends FutureAdapter {
  val ColumnFamily = "events"

  Await.result(this.createSchema(), 5.seconds)

  /** Save an Event model to Cassandra table
    *
    * @param e Event class
    * @return A Cassandra ResultSet
    */
  def save(e: Event): Future[ResultSet] = ???

  /** Get latest Event for a given srcId
    *
    * @param srcId Event srcId
    * @return Some Event, or None if srcId not found
    */
  def getLatest(srcId: String): Future[Option[Event]] = ???

  /** Find Events for a srcId within a time range
    *
    * @param srcId Event srcId
    * @param from A starting timestamp (optional)
    * @param to An ending timestamp (optional)
    * @return Some sequence of Events, or empty iterator if no Events found
    */
  def getRange(srcId: String, from: Option[Date] = None, to: Option[Date] = None): Future[Iterator[Event]] = ???

  /** Summarize a sequence of Events
    *
    * @param srcId Event srcId
    * @param from A starting timestamp (optional)
    * @param to An ending timestamp (opttional)
    * @return Some summary of Events, or None if not found
    */
  def getSummary(srcId: String, from: Option[Date] = None, to: Option[Date] = None): Future[Option[EventSummary]] = ???

  /** Convert a Cassandra Row into an Event model
    *
    * @param row Cassandra Row (from query result)
    * @return Event model
    */
  private def fromRow(row: Row): Event = ???

  private val create: SchemaStatement = ??? //SchemaBuilder.createTable...

  /** Create the Cassandra column family
    *
    * @return ResultSet
    */
  private def createSchema(): Future[ResultSet] = client.session.executeAsync(create)
}