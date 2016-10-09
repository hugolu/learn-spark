package cc.eighty20.spark.s02

import org.apache.spark.sql.SparkSession

object sfpd {
  def main(args: Array[String]) {
    val spark = SparkSession.builder().getOrCreate()
    import spark.implicits._

    val df = spark.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("data/sfpd.csv")
    df.createOrReplaceTempView("sfpd")

    df.printSchema
    df.select("Category").distinct().show //collect().foreach(println)
    spark.sql("SELECT distinct Category FROM sfpd").show //collect().foreach(println)
    spark.sql("SELECT Resolution , count(Resolution) as rescount FROM sfpd group by Resolution order by rescount desc limit 10").show
    spark.sql("SELECT Category , count(Category) as catcount FROM sfpd group by Category order by catcount desc limit 10").show
  }
}
