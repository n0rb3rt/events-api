package clients

import com.datastax.driver.core._
import com.datastax.driver.core.schemabuilder._
import javax.inject.Singleton
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

import scala.collection.JavaConverters._

/**
  * A Cassandra "client" object that automatically starts an embedded node
  */
@Singleton
class CassandraClient {
  EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra.yaml")

  val Keyspace = "events_api_v1"

  private lazy val cluster: Cluster = {
    Cluster
      .builder()
      .addContactPoint("localhost")
      .withPort(9142)
      .build()
  }

  private val createKeyspace: KeyspaceOptions = {
    SchemaBuilder
      .createKeyspace(Keyspace)
      .ifNotExists()
      .`with`()
      .replication(
        Map[String, AnyRef](
          "class" -> "SimpleStrategy",
          "replication_factor" -> "1"
        ).asJava
      )
  }

  lazy val session: Session = {
    val initSession = cluster.connect()
    initSession.execute(createKeyspace)
    initSession.close()
    cluster.connect(Keyspace)
  }

  def close(): Unit = {
    session.close()
    cluster.close()
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
  }
}