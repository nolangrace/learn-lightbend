package learnlightbend.cloudflow.akkastreams

import akka.stream.alpakka.elasticsearch.{ ElasticsearchWriteSettings, WriteMessage }
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchFlow
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import learnlightbend.data.TestData
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import spray.json._
import DefaultJsonProtocol._
import akka.kafka.ConsumerMessage.CommittableOffset
import akka.stream.scaladsl.Sink

class ElasticsearchWrite extends AkkaStreamlet {

  //\\//\\//\\ INLETS //\\//\\//\\
  val in = AvroInlet[TestData]("in")

  //\\//\\//\\ SHAPE //\\//\\//\\
  final override val shape = StreamletShape.withInlets(in)

  val ElasticHostConf = StringConfigParameter(
    "elastic-host",
    " ",
    Some("localhost")
  )

  //\\//\\//\\ LOGIC //\\//\\//\\
  final override def createLogic = new RunnableGraphStreamletLogic {

    val elasticHost = system.settings.config.getString("elasticsearch.host")
    //    val elasticHost = streamletConfig.getString(ElasticHostConf.key)
    val elasticPort = 9200

    log.info("Elasticsearch Config host: " + elasticHost + " Port: " + elasticPort)

    implicit val client = RestClient.builder(new HttpHost(elasticHost, 9200)).build()

    val sourceSettings = ElasticsearchWriteSettings()

    implicit val format: JsonFormat[TestData] = jsonFormat3(TestData.apply)

    case class KafkaOffset(offset: Int)

    def runnableGraph() =
      sourceWithOffsetContext(in)
        .asSource
        .map {
          case (td, offset) ⇒ {
            log.info("Prepping to Write to Elastic: " + td.word)
            WriteMessage.createIndexMessage(source = TestData(td.ts, td.word, td.num))
              .withPassThrough(offset)
          }
        }
        .via(
          ElasticsearchFlow.createWithPassThrough[TestData, CommittableOffset](
            indexName = "test",
            typeName = "_doc"
          )
        //          .map(r => r)
        )
        .asSourceWithContext(td ⇒ td.message.passThrough)
        .map(x ⇒ {
          log.info("Written to Elastic: " + x)
        })
        .to(committableSink)

  }
}
