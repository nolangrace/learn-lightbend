package learnlightbend.cloudflow.akkastreams

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.akkastream.util.scaladsl.HttpServerLogic
import cloudflow.streamlets._
import cloudflow.streamlets.avro.AvroOutlet

class KakfaIngress extends AkkaServerStreamlet {

  //\\//\\//\\ INLETS //\\//\\//\\

  //\\//\\//\\ OUTLETS //\\//\\//\\
  val out = AvroOutlet[]("")

  //\\//\\//\\ SHAPE //\\//\\//\\
  final override val shape = StreamletShape(out)

  //\\//\\//\\ LOGIC //\\//\\//\\
  final override def createLogic = new RunnableGraphStreamletLogic {


    def runnableGraph =


}
