package cc.eighty20.spark.s02

import org.apache.spark.sql.SparkSession

object df00 {
  def main(args: Array[String]) {
    val spark = SparkSession.builder()
                  .appName("df00")
                  .master("local")
                  .getOrCreate()

    val df = spark.read.parquet("data/people.parquet")
    df.createOrReplaceTempView("people")

    println("----")
    df.show()
    spark.sql("SELECT * FROM people").show()

    println("----")
    df.printSchema()
    spark.sql("SELECT * FROM people").printSchema()

    println("----")
    df.select("name").show()
    spark.sql("SELECT name FROM people").show()

    println("----")
    df.select(df("name"), df("age") + 1).show()
    spark.sql("SELECT name, (age + 1) FROM people").show()

    println("----")
    df.filter(df("age") > 21).show()
    spark.sql("SELECT * FROM people WHERE age > 21").show()

    println("----")
    df.groupBy("age").count().show()
    spark.sql("SELECT age, count(*) as count FROM people GROUP BY age").show()
  }
}
