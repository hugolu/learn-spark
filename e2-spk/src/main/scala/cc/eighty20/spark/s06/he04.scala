package cc.eighty20.spark.s06

import kafka.serializer.StringDecoder
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import com.fasterxml.jackson.databind._
import java.util.Date

object he04 {
  case class Heartbeat(evt_type: String, evt_dt: Long, name: String)

  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println("Usage: he04 <brokers> <topics> <checkpointPath>")
      System.exit(1)
    }
    val Array(brokers, topics, checkpointPath) = args

    val masterURL = if (System.getProperty("spark.master") == null || System.getProperty("spark.master").isEmpty) "local[5]" else System.getProperty("spark.master")

    val sparkConf = new SparkConf().setAppName("he04").setMaster(masterURL)
    val ssc = new StreamingContext(sparkConf, Seconds(1))
    ssc.checkpoint(checkpointPath)

    val om = new ObjectMapper()

    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)

    val messages_rdd = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)

    val events = messages_rdd.map{ tuple => 
      val jsonNode = om.readTree(tuple._2)
      val evt_type = jsonNode.get("evt_type").asText()
      val evt_dt = jsonNode.get("evt_dt").asLong()
      val name = jsonNode.get("name").asText()
      (name, 1)
    }

    val windowed_heartbeats_reduceByKey = events.reduceByKeyAndWindow((a:Int, b:Int) => a+b, Seconds(60), Seconds(1))
    windowed_heartbeats_reduceByKey.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
