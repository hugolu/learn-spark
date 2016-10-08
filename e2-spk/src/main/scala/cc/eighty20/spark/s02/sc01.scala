package cc.eighty20.spark.s02

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.Encoder

object sc01 {
  case class Word(text: String)

  def main(args: Array[String]) {
    val spark = SparkSession.builder().getOrCreate()
    import spark.implicits._

    val fileName = "data/README.md"
    val docs = spark.sparkContext.textFile(fileName)
    val lower = docs.map(line => line.toLowerCase())
    val words = lower.flatMap(line => line.split("\\s+"))
    val wordsDF = words.map(w => Word(w)).toDF()
    wordsDF.createOrReplaceTempView("words")

    val topWords = spark.sql("SELECT text, count(text) AS n FROM words GROUP BY text ORDER BY n DESC LIMIT 10")
    topWords.show()
  }
}
