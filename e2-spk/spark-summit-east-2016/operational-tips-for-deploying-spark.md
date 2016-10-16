# [OPERATIONAL TIPS FOR DEPLOYING SPARK](https://spark-summit.org/east-2016/events/operational-tips-for-deploying-spark/)

## Configuration Systems

### Spark Core Configuration

```scala
// print Spark Config
sc.getConf.toDebugString

// print Hadoop Config
val hdConf = sc.hadoopConfiguration.iterator()
while(hdConf.hasNext) {
  println(hdConf.next().toString())
}
```

### Spark SQL Configuration

透過 SQL 介面設定:
```
sqlContext.sql("SET spark.sql.shuffle.partitions=10;")
```
- SET key=value

透過 SQL 查看設定:
```
val conf = sqlContext.getAllConfs
conf.foreach{case (key, value) => println(s"$key => $value")}
```

## Spark Pipeline Design

### File Formats
- Text: CSV, JSON
- Avro (for streaming)
- Parquet (Column store)

### Compression Codecs
- Snappy, LZO (壓縮率低，速度快)
- Gzip (壓縮率高，速度慢)，適合不常用的冷資料

設定參數
- `io.compression.codecs`
- `spark.sql.parquet.compression.codec`
- `spark.io.compression.codec`

### Spark APIs
兩類型的 partitioning - file level and spark

```sql
df.write.partitionBy("colName").saveAsTable("tableName")
```
```shell
ls -l /dbfs/user/hive/warehouse/tablename/
total 0
-rw-r--r--  1 root  root  0 Jan 1 1970  _SUCCESS
drwxr-xr-x  1 root  root  0 Jan 1 1970  year=2014
drwxr-xr-x  1 root  root  0 Jan 1 1970  year=2015
```

### Job Profiles
Monitoring & Metrics
- spark
- servers

Toolset
- Ganglia
- Grapite

> 這部分等有經驗再深入研究...
