package learnlightbend.cloudflow.akkastreams

import cloudflow.akkastream._
import cloudflow.akkastream.util.scaladsl.MergeLogic
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import learnlightbend.data.AggregatedCallStats

class ExampleMerge extends AkkaStreamlet {

  //\\//\\//\\ INLETS //\\//\\//\\
  val in1 = AvroInlet[AggregatedCallStats]("in1")

  //\\//\\//\\ OUTLETS //\\//\\//\\
  val out = AvroOutlet[AggregatedCallStats]("out")

  //\\//\\//\\ SHAPE //\\//\\//\\
  final override val shape = StreamletShape.withInlets(in1).withOutlets(out)

  //\\//\\//\\ LOGIC //\\//\\//\\
  final override def createLogic = new MergeLogic(Vector(in1), out)
}
