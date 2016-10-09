package cc.eighty20.spark.s02

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

object sc00 {
  def main(args: Array[String]) {
    val spark = SparkSession.builder().getOrCreate()

    // Step 1: data to RDD, preprocessing
    val wordsRow = spark.sparkContext.textFile("data/README.md")
                        .map(_.toLowerCase)
                        .flatMap(_.split("\\s+"))
                        .map(Row(_))

    // Step 2: RDD to DataFrame, creating Table
    val fields = Array(StructField("text", StringType, nullable=true))
    val schema = StructType(fields)
    val wordsDF = spark.createDataFrame(wordsRow, schema)
    wordsDF.createOrReplaceTempView("words")

    // Step 3: Analyzing with Spark SQL
    val topWords = spark.sql("SELECT text, count(text) AS n FROM words GROUP BY text ORDER BY n DESC LIMIT 10")
    topWords.show()
  }
}
