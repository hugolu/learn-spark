package cc.eighty20.spark.s01

import org.apache.spark.{SparkConf, SparkContext}

object sc00 {
  def main(args: Array[String]) {
    val appName = "sc00"
    val master = "local[2]"
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    val sc = new SparkContext(conf)

    val words = sc.parallelize(Seq("eighty20", "spark", "traing", "hello", "world"))
    val count = words.count()

    println(s"count: $count")
  }
}
