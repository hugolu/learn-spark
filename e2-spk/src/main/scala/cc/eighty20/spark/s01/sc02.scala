package cc.eighty20.spark.s01

import org.apache.spark.{SparkConf, SparkContext}

object sc02 {
  def main(args: Array[String]) {
    var conf = new SparkConf().setAppName("sc02").setMaster("local[*]")
    var sc = new SparkContext(conf)

    val fileName = util.Try(args(0)).getOrElse("data/log.txt")
    val linesRDD = sc.textFile(fileName)
    val errorsRDD = linesRDD.filter(_.startsWith("ERROR"))
    val messagesRDD = errorsRDD.map(_.split("\t")).map(_(1)).cache()
    val mysqlErrorCount = messagesRDD.filter(_.contains("mysql")).count()
    val phpErrorCount = messagesRDD.filter(_.contains("php")).count()
    println(s"mysql errors: ${mysqlErrorCount}\nphp errors: ${phpErrorCount}")
  }
}
