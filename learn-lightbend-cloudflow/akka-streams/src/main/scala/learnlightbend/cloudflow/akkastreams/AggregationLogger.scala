package learnlightbend.cloudflow.akkastreams

import akka.stream.scaladsl.Sink
import cloudflow.akkastream.AkkaStreamlet
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import learnlightbend.data.{ AggregatedTestData }

class AggregationLogger extends AkkaStreamlet {

  //\\//\\//\\ INLETS //\\//\\//\\
  val in = AvroInlet[AggregatedTestData]("in")

  //\\//\\//\\ SHAPE //\\//\\//\\
  final override val shape = StreamletShape.withInlets(in)

  //\\//\\//\\ LOGIC //\\//\\//\\
  final override def createLogic = new RunnableGraphStreamletLogic {

    def runnableGraph() = {
      sourceWithOffsetContext(in)
        .map(message ⇒ {
          log.info("Aggregated Stats: " + message)
        }).to(Sink.ignore)

    }

  }

}
