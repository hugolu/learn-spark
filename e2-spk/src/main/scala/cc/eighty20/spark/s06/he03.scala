package cc.eighty20.spark.s06

import kafka.serializer.StringDecoder
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import com.fasterxml.jackson.databind._
import java.util.Date

object he03 {
  case class Heartbeat(evt_type: String, evt_dt: Long, name: String)

  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println("Usage: he03 <brokers> <topics> <checkpointPach>")
      System.exit(1)
    }
    val Array(brokers, topics, checkpointPath) = args

    val masterURL = if (System.getProperty("spark.master") == null || System.getProperty("spark.master").isEmpty) "local[5]" else System.getProperty("spark.master")

    val sparkConf = new SparkConf().setAppName("he03").setMaster(masterURL)
    val ssc = new StreamingContext(sparkConf, Seconds(1))
    ssc.checkpoint(checkpointPath)

    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)

    val messages_rdd = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)
    val messages_values = messages_rdd.map(_._2)

    val windowed_heartbeats_count = messages_values.countByWindow(Seconds(60), Seconds(1))
    windowed_heartbeats_count.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
