package learnlightbend.cloudflow.akkastreams

import akka.kafka.{ ConsumerSettings, Subscriptions }
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Source
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.streamlets._
import cloudflow.streamlets.avro.AvroOutlet
import learnlightbend.data._
import net.manub.embeddedkafka.{ EmbeddedKafka, EmbeddedKafkaConfig }
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ ByteArrayDeserializer, StringDeserializer }

class KakfaIngress extends AkkaStreamlet {

  //\\//\\//\\ INLETS //\\//\\//\\

  //\\//\\//\\ OUTLETS //\\//\\//\\
  val out = AvroOutlet[TestData]("out")

  //\\//\\//\\ SHAPE //\\//\\//\\
  final override val shape = StreamletShape(out)

  //\\//\\//\\ LOGIC //\\//\\//\\
  final override def createLogic = new RunnableGraphStreamletLogic {

    val TopicName = "test-topic"
    setupLocalKafkaTopic()

    val consumerSettings =
      ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers("localhost:9092")
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

    private def setupLocalKafkaTopic(): Unit = {
      if (EmbeddedKafka.isRunning) {
        implicit val kafkaConfig = EmbeddedKafkaConfig(kafkaPort = 9092)
        EmbeddedKafka.createCustomTopic(TopicName)

        EmbeddedKafka.publishStringMessageToKafka(TopicName, "test")
        EmbeddedKafka.publishStringMessageToKafka(TopicName, "test1")
        EmbeddedKafka.publishStringMessageToKafka(TopicName, "test2")
      }
    }
  }

}
