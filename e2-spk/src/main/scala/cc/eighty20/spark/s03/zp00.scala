package cc.eighty20.spark.s03

import org.apache.spark.sql.SparkSession

object zp00 {
  case class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)

  def main(args: Array[String]) {
    val spark = SparkSession.builder().appName("zp00").master("local").getOrCreate()
    import spark.implicits._

    val bank = spark.sparkContext.textFile("/common/data/bank.csv")
              .filter(line => !line.startsWith("\"age\""))
              .map(_.split(";")
              .map(_.replaceAll("\"", ""))).map(t => Bank(t(0).toInt, t(1), t(2), t(3), t(5).toInt))
              .toDF()
    bank.createOrReplaceTempView("bank")

    spark.sql("select age, count(1) value from bank where age < 30 group by age order by age").show
    spark.sql("select age, count(1) value from bank where marital=\"single\" group by age order by age").show
  }
}
