package cc.eighty20.spark.s06

import kafka.serializer.StringDecoder
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.SparkConf
import java.util.Date
import org.apache.log4j.Logger
import org.apache.log4j.Level

object ss02 {
  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println("""
        |Usage: ss02 <brokers> <topics> <numOfTop>
        |  <brokers> is a list of one or more Kafka brokers
        |  <topics> is a list of one or more kafka topics to consume from
        |  <numOfTop> is the number of Top N words
        """.stripMargin)
      System.exit(1)
    }
    val Array(brokers, topics, numOfTop) = args;

    val masterURL = if (System.getProperty("spark.master") == null || System.getProperty("spark.master").isEmpty) "local" else System.getProperty("spark.master")
    val sparkConf = new SparkConf().setAppName("ss02").setMaster(masterURL)
    val ssc = new StreamingContext(sparkConf, Seconds(1))
    Logger.getRootLogger.setLevel(Level.ERROR)

    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val topN = Integer.parseInt(numOfTop)
    val docs_mini = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)

    docs_mini.foreachRDD{(rdd, time) =>
      val lines = rdd.map(_._2)
      val lines_lower = lines.map(_.toLowerCase())
      val words = lines_lower.flatMap(_.split("\\s+"))
      val counts = words.map(word => (word, 1))
      val freq = counts.reduceByKey(_ + _)
      val top = freq.map(_.swap).top(topN)

      println()
      println("=== " + new Date(time.milliseconds) + " ===")
      println("Microbatch Dataset [Top " + topN + "]\n")
      top.foreach(println)
      println()
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
