# Spark SQL, DataFrame and Dataset

參考: [Spark SQL, DataFrames and Datasets Guide](http://spark.apache.org/docs/latest/sql-programming-guide.html)


<img src="https://ogirardot.files.wordpress.com/2015/05/future-of-spark.png" width="800">

圖片資料來自 [RDDS ARE THE NEW BYTECODE OF APACHE SPARK](https://ogirardot.wordpress.com/2015/05/29/rdds-are-the-new-bytecode-of-apache-spark/)

- Spark SQL execute **SQL** queries.
- A **Dataset** is a distributed collection of data. Dataset is a new interface added in Spark 1.6 that provides the benefits of RDDs with the benefits of Spark SQL’s optimized execution engine.
- A **DataFrame** is a Dataset organized into named columns. It is conceptually equivalent to a table in a relational database or a data frame in R/Python, but with richer optimizations under the hood.
- Spark SQL supports operating on a variety of **data sources** through the DataFrame interface.

## 小試身手

### 產生 SparkSession

```scala
import org.apache.spark.sql.SparkSession

val spark = SparkSession.builder().getOrCreate()
```
- `SparkSession`: 使用 Spark 操作 Dataset 與 DataFrame 的進入點

### 由 json 檔案產生 DataFrame
```scala
val df = spark.read.json("people.json")

df.show()
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
```
