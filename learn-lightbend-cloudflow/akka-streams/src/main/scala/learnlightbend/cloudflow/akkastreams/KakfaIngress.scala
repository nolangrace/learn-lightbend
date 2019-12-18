package learnlightbend.cloudflow.akkastreams

import akka.actor.Props
import akka.kafka.{ ConsumerSettings, ProducerSettings, Subscriptions }
import akka.kafka.scaladsl.{ Consumer, Producer }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import pipelines.akkastream._
import pipelines.akkastream.scaladsl.RunnableGraphStreamletLogic
import pipelines.streamlets._
import pipelines.streamlets.avro.AvroOutlet
import learnlightbend.data._
import net.manub.embeddedkafka.{ EmbeddedKafka, EmbeddedKafkaConfig }
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ ByteArrayDeserializer, StringDeserializer, StringSerializer }

class KakfaIngress extends AkkaStreamlet {

  val EmbeddedKafkaConf = BooleanConfigParameter(
    "embedded-kafka",
    " ",
    Some(false)
  )

  val kafkaHostnameConf = StringConfigParameter(
    "kafka-hostname",
    " ",
    Some("localhost")
  )

  val kafkaPortConf = IntegerConfigParameter(
    "kafka-port",
    " ",
    Some(9092)
  )

  override def configParameters = Vector(EmbeddedKafkaConf, kafkaHostnameConf, kafkaPortConf)

  //\\//\\//\\ INLETS //\\//\\//\\

  //\\//\\//\\ OUTLETS //\\//\\//\\
  val out = AvroOutlet[TestData]("out")

  //\\//\\//\\ SHAPE //\\//\\//\\
  final override val shape = StreamletShape(out)

  //\\//\\//\\ LOGIC //\\//\\//\\
  final override def createLogic = new RunnableGraphStreamletLogic {

    val embeddedKafka = streamletConfig.getBoolean(EmbeddedKafkaConf.key)

    log.info("Embedded kafka: " + embeddedKafka)

    val TopicName = "test-topic"

    val kafkaHost = streamletConfig.getString(kafkaHostnameConf.key)
    val kafkaPort = streamletConfig.getInt(kafkaPortConf.key)

    log.info("Configurations Kafka hostname: " + kafkaHost + " Kafka Port: " + kafkaPort)

    implicit val kafkaConfig = EmbeddedKafkaConfig(kafkaPort = kafkaPort)

    setupAndFeedKafka()

    val consumerSettings =
      ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers(kafkaHost + ":" + kafkaPort)
        .withGroupId("group1")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    //    def runnableGraph = Source(0 to 100).map(x ⇒ {
    //      log.info("Current NUm: " + x)
    //
    //      AggregatedCallStats(0L, 0L, 0.0, 0L)
    //    }).to(plainSink(out))

    def runnableGraph = Consumer
      .committableSource(consumerSettings, Subscriptions.topics(TopicName)).map(x ⇒ {
        log.info("Read Message from kafka: " + x.record.value())

        TestData(x.record.value(), 0L, 0L)
      }).to(plainSink(out))

    private def setupAndFeedKafka(): Unit = {
      if (embeddedKafka) {
        EmbeddedKafka.createCustomTopic(TopicName)
      }

      val props = Props[FeedKafkaActor]
      val myActor = system.actorOf(Props[FeedKafkaActor], "feed-kafka-actor")

      myActor ! StartFeed(kafkaHost, kafkaPort, TopicName)

    }
  }

}

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

case class StartFeed(host: String, port: Int, TopicName: String)

class FeedKafkaActor extends Actor {
  val log = Logging(context.system, this)

  implicit val materializer = ActorMaterializer()

  def receive = {
    case StartFeed(host, port, topicName) ⇒ {
      val producerSettings =
        ProducerSettings(context.system, new StringSerializer, new StringSerializer)
          .withBootstrapServers(host + ":" + port)

      Source(1 to 100)
        .map(_.toString)
        .map(value ⇒ new ProducerRecord[String, String](topicName, value))
        .runWith(Producer.plainSink(producerSettings))

    }
    case _ ⇒ log.info("received unknown message")
  }
}
