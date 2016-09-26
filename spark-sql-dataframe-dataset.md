# Spark SQL, DataFrame and Dataset

參考: [Spark SQL, DataFrames and Datasets Guide](http://spark.apache.org/docs/latest/sql-programming-guide.html)


<img src="https://ogirardot.files.wordpress.com/2015/05/future-of-spark.png" width="800">

圖片資料來自 [RDDS ARE THE NEW BYTECODE OF APACHE SPARK](https://ogirardot.wordpress.com/2015/05/29/rdds-are-the-new-bytecode-of-apache-spark/)

- Spark SQL execute SQL queries.
- A Dataset is a distributed collection of data. Dataset is a new interface added in Spark 1.6 that provides the benefits of RDDs with the benefits of Spark SQL’s optimized execution engine.
- A DataFrame is a Dataset organized into named columns. It is conceptually equivalent to a table in a relational database or a data frame in R/Python, but with richer optimizations under the hood.
- Spark SQL supports operating on a variety of data sources through the DataFrame interface.
