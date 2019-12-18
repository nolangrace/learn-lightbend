package learnlightbend.cloudflow

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import cloudflow.spark.{ SparkStreamlet, SparkStreamletLogic }
import org.apache.spark.sql.streaming.OutputMode
import cloudflow.spark.sql.SQLImplicits._
import org.apache.log4j.{ Level, Logger }

import learnlightbend.data._

class SparkAggregator extends SparkStreamlet {

  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  val in = AvroInlet[TestData]("in")
  val out = AvroOutlet[TestData]("out", _.word)
  val shape = StreamletShape(in, out)

  override def createLogic = new SparkStreamletLogic {

    override def buildStreamingQueries = {
      val dataset = readStream(in)
      val outStream = process(dataset)

      outStream.writeStream
        .outputMode("update")
        .format("console")

      writeStream(outStream, out, OutputMode.Update).toQueryExecution
    }

    private def process(inDataset: Dataset[TestData]): Dataset[TestData] = {
      inDataset
    }
  }
}
