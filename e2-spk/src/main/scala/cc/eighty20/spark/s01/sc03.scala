package cc.eighty20.spark.s01

import org.apache.spark.{SparkConf, SparkContext}

object sc03 {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("sc03").setMaster("local[2]")
    val sc = new SparkContext(conf)

    val textRDD = sc.textFile("data/README.md")
    val linesRDD = textRDD.map(_.toLowerCase)
    val wordsRDD = linesRDD.flatMap(_.split("\\s+")).filter(!_.isEmpty)
    val pairsRDD = wordsRDD.map((_, 1))
    val freqRDD = pairsRDD.reduceByKey(_+_)
    val top10 = freqRDD.top(10)(Ordering.by[(String, Int), Int]{case (key, value) => value})

    top10.foreach(println)
  }
}
