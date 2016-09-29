package cc.eighty20.spark.s01

import org.apache.spark.{SparkConf, SparkContext}

object sc01 {
  def main(args: Array[String]) {
    val conf = new SparkConf()
                .setAppName("sc01")
                .setMaster("local[*]")
    val sc = new SparkContext(conf)

    val fileName = util.Try(args(0)).getOrElse("data/pom.xml")
    val linesRDD = sc.textFile(fileName).cache()
    val count = linesRDD.count()

    println(s"line: $count")
  }
}
