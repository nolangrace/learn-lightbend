//package learnlightbend.cloudflow.akkastreams
//
//import cloudflow.akkastream._
//import cloudflow.akkastream.util.scaladsl.MergeLogic
//import cloudflow.streamlets._
//import cloudflow.streamlets.avro._
//
//class ExampleMerge extends AkkaStreamlet {
//
//  //\\//\\//\\ INLETS //\\//\\//\\
//
//  //\\//\\//\\ OUTLETS //\\//\\//\\
//  val out = AvroOutlet[]("out", )
//
//  //\\//\\//\\ SHAPE //\\//\\//\\
//  final override val shape = StreamletShape.withInlets().withOutlets(out)
//
//  //\\//\\//\\ LOGIC //\\//\\//\\
//  final override def createLogic = new MergeLogic(Vector(), out)
//}
