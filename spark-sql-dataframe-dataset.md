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
資料來源: [people.json](https://github.com/apache/spark/blob/master/examples/src/main/resources/people.json)

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

### 操作 DataFrame (Untyped Dataset)
參考: [org.apache.spark.sql.Dataset](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset)

```scala
df.printSchema()                      // Print the schema in a tree format
df.select("name").show()              // Select only the "name" column
df.select($"name", $"age" + 1).show() // Select everybody, but increment the age by 1
df.filter($"age" > 21).show()         // Select people older than 21
df.groupBy("age").count().show()      // Count people by age
```

### RDD, DataFrame, DataSet
參考: [RDD、DataFrame和DataSet的区别](http://www.jianshu.com/p/c0181667daa0)

#### RDD vs DataFrame
![RDD vs DataFrame](https://raw.githubusercontent.com/jacksu/utils4s/master/spark-knowledge/images/rdd-dataframe-dataset/rdd-dataframe.png)

RDD | DataFrame
----|----------
以Person为类型参数，但Spark框架本身不了解Person类的内部结构 | 提供了详细的结构信息，使得Spark SQL可以清楚地知道该数据集中包含哪些列，每列的名称和类型各是什么
分布式的Java对象的集合 | 分布式的Row对象的集合

DataFrame除了提供了比RDD更丰富的算子以外，更重要的特点是提升执行效率、减少数据读取以及执行计划的优化，比如filter下推、裁剪等。

#### RDD vs DataSet
RDD | DataSet
----|--------
依赖于运行时反射机制 | 以Catalyst逻辑执行计划表示，并且数据以编码的二进制形式被存储，不需要反序列化就可以执行sorting、shuffle等操作。

DataSet的性能比RDD的要好很多

#### DataFrame vs DataSet
Dataset可以认为是DataFrame的一个特例，主要区别是Dataset每一个record存储的是一个强类型值而不是一个Row。

DataSet | DataFrame
--------|-----------
在编译时检查类型 | DataFrame会继承DataSet
面向对象的编程接口 | 面向Spark SQL的接口

DataFrame和DataSet可以相互转化，`df.as[ElementType]`这样可以把DataFrame转化为DataSet，`ds.toDF()`这样可以把DataSet转化为DataFrame。

### 定義 Schema
當處理的資料尚未定義欄位 (例如資料來源為 string 或 text), DataFrame 要透過三個步驟以程式化方式定義

1. 從原始 RDD 產生 RDD[Row]
2. 產生由 `StructType` 表示的 schema 
3. 將 schema 應用到 RDD[Row]

資料來源: [people.txt](https://github.com/apache/spark/blob/master/examples/src/main/resources/people.txt)

```scala
import org.apache.spark.sql.types._
import org.apache.spark.sql.Row

val peopleRDD = sc.textFile("people.txt")
val rowRDD = peopleRDD.map(_.split(",")).map{case Array(name, age) => Row(name, age.trim.toInt)}

val fields = Array(StructField("name", StringType, nullable=true), StructField("age", IntegerType, nullable=true))
val schema = StructType(fields)

val peopleDF = spark.createDataFrame(rowRDD, schema)
val people = peopleDF.createOrReplaceTempView("people")

spark.sql("SELECT name, age FROM people WHERE age BETWEEN 13 AND 19").show()
// +------+---+
// |  name|age|
// +------+---+
// |Justin| 19|
// +------+---+
```

## Data Sources

### 儲存與載入

#### 預設格式 Parquet
參考: [深入分析Parquet列式存储格式](http://www.infoq.com/cn/articles/in-depth-analysis-of-parquet-column-storage-format)

column-store 優點:
- 可以跳过不符合条件的数据，只读取需要的数据，降低IO数据量。
- 压缩编码可以降低磁盘存储空间。由于同一列的数据类型是一样的，可以使用更高效的压缩编码（例如Run Length Encoding和Delta Encoding）进一步节约存储空间。
- 只读取需要的列，支持向量运算，能够获取更好的扫描性能。

```scala
val userDF = spark.read.load("users.parquet")
userDF.select("name", "favorite_color").write.save("user2.parquet")
```

#### 自定義格式
透過 `format()` 定義 data source 格式 (json, parquet, jdbc)

```scala
val peopleDF = spark.read.format("json").load("people.json")
peopleDF.select("name").write.format("json").save("people2.json")
```

#### 直接查詢檔案

```scala
spark.sql("SELECT * FROM parquet.`users.parquet`")
```

#### 定義儲存模式
- `SaveMode.ErrorIfExists` (default) - "error"
- `SaveMode.Append` - "append"
- `SaveMode.Overwrite` - "overwrite"
- `SaveMode.Ignore` - "ignore"

```scala
peopleDF.write.format("json").mode("overwrite").save("people2.json")
peopleDF.select("name").write.format("json").mode("error").save("people2.json")
```

#### 儲存為持久化表格
DataFrames can also be saved as persistent tables into Hive metastore using the saveAsTable command.

