//package learnlightbend.cloudflow
//
//import learnlightbend.data._
//import cloudflow.spark.sql.SQLImplicits._
//import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic}
//import org.apache.log4j.{Level, Logger}
//import org.apache.spark.sql.Dataset
//import org.apache.spark.sql.streaming.OutputMode
//
//class SparkAggregator extends SparkStreamlet {
//
//  val rootLogger = Logger.getRootLogger()
//  rootLogger.setLevel(Level.ERROR)
//
//
//  //\\//\\//\\ INLETS //\\//\\//\\
//
//  //\\//\\//\\ OUTLETS //\\//\\//\\
//
//
//  //\\//\\//\\ LOGIC //\\//\\//\\
//  override def createLogic = new SparkStreamletLogic {
//
//    //tag::docs-aggregationQuery-example[]
//    override def buildStreamingQueries = {
//      val dataset = readStream(in)
//      val outStream = process(dataset)
//      writeStream(outStream, out, OutputMode.Update).toQueryExecution
//    }
//
//    private def process(inDataset: Dataset[]): Dataset[] = {
//      // TO Be Implemented
//    }
//  }
//}
