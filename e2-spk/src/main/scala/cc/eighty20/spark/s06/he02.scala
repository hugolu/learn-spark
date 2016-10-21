package cc.eighty20.spark.s06

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import java.util.Date

object he02 {
  case class Heartbeat(evt_type: String, evt_dt: Long, name: String)

  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println("Usage: he02 <brokers> <topics>")
      System.exit(1)
    }
    val Array(brokers, topics) = args

    val masterURL = if (System.getProperty("spark.master") == null || System.getProperty("spark.master").isEmpty) "local[5]" else System.getProperty("spark.master")
    val sparkConf = new SparkConf().setAppName("he02").setMaster(masterURL)
    val ssc = new StreamingContext(sparkConf, Seconds(1))

    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val messages_rdd = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)
    val messages_values = messages_rdd.map(_._2)

    messages_values.foreachRDD{ (rdd, time) =>
      println("=== " + new Date(time.milliseconds) + " ===")

      if (rdd.count() > 0) {
        val spark = SparkSession.builder().getOrCreate()
        import spark.implicits._

        val heartbeats_df = spark.read.json(rdd)
        heartbeats_df.createOrReplaceTempView("heartbeats")

        val result = spark.sql("select name, count(*) as heartbeats from heartbeats group by name order by name")
        result.show()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
