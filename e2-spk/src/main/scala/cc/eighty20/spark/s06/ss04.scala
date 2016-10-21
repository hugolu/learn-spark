package cc.eighty20.spark.s06

import kafka.serializer.StringDecoder
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import java.util.Date
import org.apache.log4j.Logger
import org.apache.log4j.Level

object ss04 {
  case class Word(text: String)

  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println("""
        |Usage: ss04 <brokers> <topics> <numOfTop>
        |  <brokers> is a list of one or more Kafka brokers
        |  <topics> is a list of one or more kafka topics to consume from
        |  <numOfTop> is the number of Top N words
        """.stripMargin)
      System.exit(1)
    }
    val Array(brokers, topics, numOfTop) = args;

    val masterURL = if (System.getProperty("spark.master") == null || System.getProperty("spark.master").isEmpty) "local" else System.getProperty("spark.master")
    val sparkConf = new SparkConf().setAppName("ss04").setMaster(masterURL)
    val ssc = new StreamingContext(sparkConf, Seconds(1))
    Logger.getRootLogger.setLevel(Level.ERROR)

    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val topN = Integer.parseInt(numOfTop)
    val docs_mini = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)

    val lines = docs_mini.map(_._2)
    val lines_lower = lines.map(_.toLowerCase())
    val words = lines_lower.flatMap(_.split("\\s+"))

    words.foreachRDD{(rdd, time) =>
      val spark = SparkSession.builder().getOrCreate()
      import spark.implicits._

      val words = rdd.map(w => Word(w)).toDF()
      words.createOrReplaceTempView("words")

      val top = spark.sql("select text, count(text) as n from words group by text order by n desc limit " + topN).collect

      println()
      println("=== " + new Date(time.milliseconds) + " ===")
      println("Microbatch Dataset [Top " + topN + "]\n")
      top.foreach(arr => println(s"(${arr(1)},${arr(0)})") )
      println()
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
