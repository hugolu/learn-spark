package cc.eighty20.spark.s03

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object zp01 {
  def main(args: Array[String]) {
    val spark = SparkSession.builder().appName("zp01").master("local").getOrCreate()
    import spark.implicits._
    
    val df1 = spark.read
                .format("com.databricks.spark.csv")
                .option("header", "true")
                .option("inferSchema", "true")
                .option("delimiter",",")
                .load("file:///common/data/pivot_sample.csv")

    // group by A and B
    // privot C
    // sum of D
    df1.groupBy("A", "B").pivot("C").sum("D").show()

    // group by A and B
    // pivot on C (with distinct values "small" and "large")
    // sum of D
    df1.groupBy("A", "B").pivot("C", Seq("small", "large")).sum("D").show()

    // group by A and B
    // pivot on C
    // sum of D and avg of D
    df1.groupBy("A", "B").pivot("C").agg(sum("D"), avg("D")).show()


    val df2 = spark.read
                .format("com.databricks.spark.csv")
                .option("header", "true")
                .option("inferSchema", "true")
                .option("delimiter","\t")
                .load("file:///common/data/pivot_sample2.csv")

    // Product Field to the Row Labels area
    // Amount Field to the Value area
    // Country Field to the Report Filter area
    df2.groupBy("Product").sum("Amount").orderBy("Product").show()
    df2.filter("Country=\"Canada\"").groupBy("Product").sum("Amount").orderBy("Product").show()
    df2.filter("Country=\"France\"").groupBy("Product").sum("Amount").orderBy("Product").show()

    // Country Field to the Row Labels area
    // Product Field to the Column Labels area
    // Amount Field to the Values area
    // Category Field to the Report Filter area
    df2.groupBy("Country").pivot("Product").sum("Amount").orderBy("Country").show()
    df2.filter("Category=\"Fruit\"").groupBy("Country").pivot("Product").sum("Amount").orderBy("Country").show()
    df2.filter("Category=\"Vegetables\"").groupBy("Country").pivot("Product").sum("Amount").orderBy("Country").show()


  }
}
