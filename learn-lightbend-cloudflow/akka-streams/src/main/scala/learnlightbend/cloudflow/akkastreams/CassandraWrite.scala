package learnlightbend.cloudflow.akkastreams

import java.io.File
import java.util.UUID

import akka.NotUsed
import akka.kafka.ConsumerMessage.CommittableOffset
import akka.stream.alpakka.cassandra.scaladsl.CassandraFlow
import akka.stream.scaladsl.Sink
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl.{ FlowWithOffsetContext, RunnableGraphStreamletLogic }
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import com.datastax.driver.core.{ Cluster, PreparedStatement, Session }
import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory
import com.github.nosan.embedded.cassandra.api.connection.DefaultCassandraConnectionFactory
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet
import learnlightbend.data.TestData

class CassandraWrite extends AkkaStreamlet {

  val EmbeddedCassandraConf = BooleanConfigParameter(
    "embedded-cassandra",
    " ",
    Some(false)
  )

  val CassandraHostConf = StringConfigParameter(
    "cassandra-host",
    " ",
    Some("localhost")
  )

  val CassandraPasswordConf = StringConfigParameter(
    "cassandra-password",
    " ",
    Some(" ")
  )

  override def configParameters = Vector(EmbeddedCassandraConf, CassandraHostConf, CassandraPasswordConf)

  //\\//\\//\\ INLETS //\\//\\//\\
  val in = AvroInlet[TestData]("in")

  //\\//\\//\\ SHAPE //\\//\\//\\
  final override val shape = StreamletShape.withInlets(in)

  //\\//\\//\\ LOGIC //\\//\\//\\
  final override def createLogic = new RunnableGraphStreamletLogic {

    val embeddedCassandra = streamletConfig.getBoolean(EmbeddedCassandraConf.key)

    val cassandraPort = 9042
    val cassandraHost = streamletConfig.getString(CassandraHostConf.key)

    val cassandraUsername = "cassandra"
    val cassandraPassword = streamletConfig.getString(CassandraPasswordConf.key)

    log.info("Cassandra Config host: " + cassandraHost + " Port: " + cassandraPort + " password: " + cassandraPassword)

    implicit val session: Session = getCassandraConnection()

    case class TestDataEnvelope(data: TestData, offset: CommittableOffset)

    val preparedStatement = session.prepare(s"INSERT INTO learnlightbend.cloudflow_test(id, lastname) VALUES (uuid(), ?);")
    val statementBinder = (message: TestDataEnvelope, statement: PreparedStatement) ⇒ statement.bind(message.data.word)
    val cassFlow = CassandraFlow.createWithPassThrough(3, preparedStatement, statementBinder)

    def runnableGraph() = {
      sourceWithOffsetContext(in)
        .map(x ⇒ {
          log.info("Prepping to Write to Cassandra: " + x.word)

          x
        })
        .asSource
        .map { case (td, offset) ⇒ TestDataEnvelope(td, offset) }
        .via(cassFlow)
        .asSourceWithContext(td ⇒ td.offset)
        .map(x ⇒ {
          log.info("Written to Cassandra: " + x)
        })
        .to(sinkWithOffsetContext)
    }

    //    def runnableGraph() = {
    //
    //      sourceWithOffsetContext(in)
    //        .map(message ⇒ {
    //          log.info("Prepping to Write to Cassandra: " + message)
    //
    //          session.execute("INSERT INTO learnlightbend.cloudflow_test(id, lastname) VALUES (uuid(), '" + message.word + "');")
    //          log.info("Done Writing to Cassandra: " + message.word)
    //        })
    //        .to(Sink.ignore)
    //    }

    def setupCassandra(): Unit = {
      val cassandraFactory = new EmbeddedCassandraFactory()
      cassandraFactory.setPort(cassandraPort)

      val cassandra = cassandraFactory.create()
      cassandra.start()
      val cassandraConnectionFactory = new DefaultCassandraConnectionFactory()
      try {
        val connection = cassandraConnectionFactory.create(cassandra)
        connection.execute("CREATE KEYSPACE learnlightbend WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };")

        connection.execute("CREATE TABLE learnlightbend.cloudflow_test ( id UUID PRIMARY KEY, lastname text );")
      } finally {
        //        cassandra.stop()
      }

      log.info("Cassandra Started!")

    }

    def getCassandraConnection(): Session = {
      if (embeddedCassandra) {
        setupCassandra()

        Cluster.builder
          .addContactPoint(cassandraHost)
          .withPort(cassandraPort)
          .build
          .connect()
      } else {
        Cluster.builder
          .addContactPoint(cassandraHost)
          .withPort(cassandraPort)
          .withCredentials(cassandraUsername, cassandraPassword)
          .build
          .connect()
      }
    }
  }
}
