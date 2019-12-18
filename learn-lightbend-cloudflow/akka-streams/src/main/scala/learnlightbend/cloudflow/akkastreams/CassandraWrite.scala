package learnlightbend.cloudflow.akkastreams

import java.io.File
import java.util.UUID

import akka.NotUsed
import akka.stream.alpakka.cassandra.scaladsl.CassandraFlow
import akka.stream.scaladsl.Sink
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import com.datastax.driver.core.{ Cluster, PreparedStatement }
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

  override def configParameters = Vector(EmbeddedCassandraConf)

  //\\//\\//\\ INLETS //\\//\\//\\
  val in = AvroInlet[TestData]("in")

  //\\//\\//\\ SHAPE //\\//\\//\\
  final override val shape = StreamletShape.withInlets(in)

  //\\//\\//\\ LOGIC //\\//\\//\\
  final override def createLogic = new RunnableGraphStreamletLogic {

    val embeddedCassandra = streamletConfig.getBoolean(EmbeddedCassandraConf.key)

    val cassandraPort = 9042
    val cassandraHost = "localhost"

    setupCassandra()

    implicit val session = Cluster.builder
      .addContactPoint(cassandraHost)
      .withPort(cassandraPort)
      .build
      .connect()

    val preparedStatement = session.prepare(s"INSERT INTO learnlightbend.cloudflow_test(id, lastname) VALUES (?, ?)")
    val statementBinder = (data: TestData, statement: PreparedStatement) ⇒ statement.bind(UUID.randomUUID().toString, data.word)
    val cassFlow = CassandraFlow.createWithPassThrough(3, preparedStatement, statementBinder)

    def runnableGraph() = {
      plainSource(in)
        .map(x ⇒ {
          println("Prepping to Write to Cassandra: " + x.word)
          log.info("Prepping to Write to Cassandra: " + x.word)

          x
        })
        .via(cassFlow)
        .map(x ⇒ {
          log.info("Written to Cassandra: " + x.word)
        })
        .to(Sink.ignore)
    }

    //    def runnableGraph() = {
    //      plainSource(in)
    //        .map(x ⇒ {
    //          log.info("TEST TEST TEST: " + x.word)
    //        })
    //        .to(Sink.ignore)
    //    }

    def setupCassandra(): Unit = {

      if (embeddedCassandra) {
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
    }
  }
}
