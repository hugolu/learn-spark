package cc.eighty20.spark.s02

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

object sc00 {
  def main(args: Array[String]) {
    val spark = SparkSession.builder().getOrCreate()

    val fileName = "data/README.md"
    val docs = spark.sparkContext.textFile(fileName)
    val lower = docs.map(line => line.toLowerCase())
    val words = lower.flatMap(line => line.split("\\s+"))
    val wordsRow = words.map(Row(_))

    val fields = Array(StructField("text", StringType, nullable=true))
    val schema = StructType(fields)

    val wordsDF = spark.createDataFrame(wordsRow, schema)
    wordsDF.createOrReplaceTempView("words")

    val topWords = spark.sql("SELECT text, count(text) AS n FROM words GROUP BY text ORDER BY n DESC LIMIT 10")
    topWords.show()
  }
}
