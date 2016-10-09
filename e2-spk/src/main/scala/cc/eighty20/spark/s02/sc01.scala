package cc.eighty20.spark.s02

import org.apache.spark.sql.SparkSession
//import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
//import org.apache.spark.sql.Encoder

object sc01 {
  // define the table schema
  case class Word(text: String)

  def main(args: Array[String]) {
    val spark = SparkSession.builder().getOrCreate()
    import spark.implicits._

    // Step 1: data to RDD, preprocessing
    val wordsRDD = spark.sparkContext.textFile("data/README.md")
                      .map(_.toLowerCase)
                      .flatMap(_.split("\\s+"))
                      .map(Word(_))

    // Step 2: RDD to DataFrame, creating Table
    val wordsDF = wordsRDD.toDF()
    wordsDF.createOrReplaceTempView("words")

    // Step 3: Analyzing with Spark SQL
    val topWords = spark.sql("SELECT text, count(text) AS n FROM words GROUP BY text ORDER BY n DESC LIMIT 10")
    topWords.show()
  }
}
