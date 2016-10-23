import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf())

    val text = sc.textFile("README.md")
    val words = text.flatMap(_.split("\\s+")).map((_, 1))
    val wordCount = words.reduceByKey(_+_)
    wordCount.map(_.swap).top(5).foreach(println)
  }
}
