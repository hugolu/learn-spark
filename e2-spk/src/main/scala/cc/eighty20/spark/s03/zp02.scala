package cc.eighty20.spark.s03

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SaveMode
import java.util.Properties

object zp02 {
  def main(args: Array[String]) {
    val spark = SparkSession.builder().appName("zp02").master("local").getOrCreate()
    import spark.implicits._

    val url = "jdbc:mysql://db:3306/northwind?user=root&password=123"
    val url2 = "jdbc:mysql://db:3306/northwind2?user=root&password=123"
    val dir = "file:///common/data/northwind/"

    val tables = List("Categories","CustomerCustomerDemo","CustomerDemographics","Customers", "Employees","EmployeeTerritories", "OrderDetails", "Orders","Region","Products","Shippers","Suppliers","Territories")

    // MySQL => Dataframe => Parquet
    tables.foreach{ table =>
      val df = spark.read
                .format("jdbc")
                .option("url", url)
                .option("dbtable", table)
                .option("driver", "com.mysql.jdbc.Driver")
                .option("fetchSize", "1000")
                .load()
      df.printSchema()

      df.write.mode(SaveMode.Overwrite).save(dir + table)
    }

    // Parquet => Dataframe => MySQL
    tables.foreach{ table =>
      val df = spark.read
              .format("parquet")
              .load(dir + table)
      df.printSchema()

      //df.write.mode(SaveMode.Overwrite).jdbc(url2, table, new Properties) //bug: Data truncation: Incorrect datetime value
    }
  }
}
