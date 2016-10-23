# Spark Streaming

參考：[Spark Streaming Programming Guide](http://spark.apache.org/docs/latest/streaming-programming-guide.html)

## 總觀

![streaming arch](http://spark.apache.org/docs/latest/img/streaming-arch.png)
- 擴充自 core Spark API，對於即時資料擁有高擴充、高流量、失敗高容忍的特性
- 支援多種資料來源: Kafka, Flume, Kinesis, TCP sockets
- 提供複雜的高階函式: map, reduce, join, window
- 支援多種資料輸出: filesystem, database, dashboard

![streaming flow](http://spark.apache.org/docs/latest/img/streaming-flow.png)
- 接收即時輸入
- 將資料分割為批次處理
- 批次輸出結果

### DStream
- Spark Streaming 提供高階抽象的資料集 `DStream` (discretized stream) - 表示一個連續的資料流
- DStream 可以產生字輸入資料流如 Kafka, Flume, and Kinesis，或是來自高階操作或其他 DStream
- DStream 在 Spark Streaming 內部表示為一系列的 RDD

## 簡單範例
source: [SocketWordCount.scala](quick-example/SocketWordCount.scala)

### 產生 StreamingContext
```scala
val conf = new SparkConf().setAppName("NetworkWordCount").setMaster("local[2]")
val ssc = new StreamingContext(conf, Seconds(1))
```

### 從 socketTextStream 讀取資料並列印
```scala
val lines = ssc.socketTextStream(args(0), args(1).toInt)
val words = lines.flatMap(_.split(" "))
val pairs = words.map(word => (word, 1))
val wordCounts = pairs.reduceByKey(_+_)
wordCounts.print()
```

### 開始流計算
```scala
ssc.start()
ssc.awaitTermination()
```

### 使用 netcat 傳輸資料
```shell
$ nc -lp 9999
apple banana apple cherry apple banana orange apple
```

### 處理流數據
```shell
$ spark-submit --class org.apache.spark.examples.streaming.NetworkWordCount target/scala-2.11/spark-streaming-app_2.11-1.0.jar localhost 9999
```

```shell
$ run-example org.apache.spark.examples.streaming.NetworkWordCount localhost 9999
```

```
-------------------------------------------
Time: 1475052703000 ms
-------------------------------------------
(orange,1)
(apple,4)
(banana,2)
(cherry,1)
```

## 基本概念

### 使用 SBT 編譯
```scala
libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "2.0.0"
libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-0-8_2.11" % "2.0.0"
libraryDependencies += "org.apache.spark" % "spark-streaming-flume_2.11" % "2.0.0"
libraryDependencies += "org.apache.spark" % "spark-streaming-kinesis-asl_2.11" % "2.0.0"
```

### 初始化 StreamingContext
```scala
import org.apache.spark._
import org.apache.spark.streaming._
```
- 需要的 package

```scala
val conf = new SparkConf().setAppName(appName).setMaster(master)
```
- appName: 顯示在 cluster UI 的 app 名稱
- master: 連接方式 (Spark, Mesos, YARN, local)

```scala
val ssc = new StreamingContext(conf, Seconds(1))
```
- context 建立後，執行以下動作
  - 定義輸入來源 (產生DStream)
  - 定義流處理過程 (對DStream執行轉換或輸出)
  - 接受輸入 - 使用 `streamingContext.start()` 
  - 等待停止 - 使用 `streamingContext.awaitTermination()` 
  - 停止計算 - 使用 `streamingContext.stop()`
- 記住幾個重點
  - 一旦 context 開始，不可以再設定或新增流計算
  - 一旦 context 停止，不可再被啟動
  - JVM 一次只能啟動一個 StreamingContext
  - 停止 StreamingContext 也會同時停止 SparkContext。如果想單獨停止 StreamingContext，使用 `stop(stopSparkContext=false)`
  - SparkContext 可以重複使用產生 StreamingContext，只要前一個 StreamingContext 在下一個 StreamingContext 產生之前停止。

### DStreams 離散化流
![](http://spark.apache.org/docs/latest/img/streaming-dstream.png)
- DStream 表示連續的資料流，來自輸入資料或是由輸入流轉換而來。
- DStream 內部是一連串的 RDD，每個 RDD 包含某時間區間收集的數據

![](http://spark.apache.org/docs/latest/img/streaming-dstream-ops.png)
- 對 DStream 操作會被轉譯成對 RDD 的操作，底層 RDD 轉換由 spark engin 負責計算
- DStream 操作隱藏大部分細節，開發者使用高階API

### DStream 輸入源與接收器 (Input DStreams and Receivers)

#### 輸入源
- File Streams
  - `StreamingContext.fileStream[K, V, F <: InputFormat[K, V]](directory: String)(implicit arg0: ClassTag[K], arg1: ClassTag[V], arg2: ClassTag[F]): InputDStream[(K, V)]`
- Queue of RDDs as a Stream
  - `StreamingContext.queueStream[T](queue: Queue[RDD[T]], oneAtATime: Boolean, defaultRDD: RDD[T])(implicit arg0: ClassTag[T]): InputDStream[T]`
- Streams based on Custom Receivers
  - `StreamingContext.receiverStream[T](receiver: Receiver[T])(implicit arg0: ClassTag[T]): ReceiverInputDStream[T]`
- Streams of TCP socket connection
  - `StreamingContext.socketStream[T](hostname: String, port: Int, converter: (InputStream) ⇒ Iterator[T], storageLevel: StorageLevel)(implicit arg0: ClassTag[T]): ReceiverInputDStream[T]`
- Kafka
  - [Receiver-based Approach](http://spark.apache.org/docs/latest/streaming-kafka-integration.html#approach-1-receiver-based-approach) `KafkaUtils.createStream(streamingContext, 
     [ZK quorum], [consumer group id], [per-topic number of Kafka partitions to consume])`
  - [Direct Approach (No Receivers)](http://spark.apache.org/docs/latest/streaming-kafka-integration.html#approach-2-direct-approach-no-receivers): `KafkaUtils.createDirectStream[
     [key class], [value class], [key decoder class], [value decoder class] ](
     streamingContext, [map of Kafka parameters], [set of topics to consume])`
- Flume
  - [Flume-style Push-based Approach](http://spark.apache.org/docs/latest/streaming-flume-integration.html#approach-1-flume-style-push-based-approach): `FlumeUtils.createStream(streamingContext, [chosen machine's hostname], [chosen port])`
  - [Pull-based Approach using a Custom Sink](http://spark.apache.org/docs/latest/streaming-flume-integration.html#approach-2-pull-based-approach-using-a-custom-sink): `FlumeUtils.createPollingStream(streamingContext, [sink machine hostname], [sink port])`
- Kinesis
  - [Spark Streaming + Kinesis Integration](http://spark.apache.org/docs/latest/streaming-kinesis-integration.html#spark-streaming-kinesis-integration): `KinesisUtils.createStream(
     streamingContext, [Kinesis app name], [Kinesis stream name], [endpoint URL],
     [region name], [initial position], [checkpoint interval], StorageLevel.MEMORY_AND_DISK_2)`
     
#### 接收器
Kafka 與 Flume 的來源支援資料傳輸的確認。如果使用 Ack 從可靠來源接收資料，可以確保資料不為因為任何故障而遺失。

- Reliable Receiver - 當資料被成功接收並存在 Spark，接收器透過 ack 通知資料源
- Unreliable Receiver - 接收器不會傳送 ack 給資料源。可用在資料源不支援 ack 的情況，或是不需要處理複雜 ack 機制的情況

### DStream 轉換動作 (Transformations)

- 支援與一般 RDD 相同的轉換: `map(func)`, `flatMap(func)`, `filter(func)`, `repartition(numPartitions)`, `union(otherStream)`, `count()`, `reduce(func)`, `countByValue()`, `reduceByKey(func, [numTasks])`, `join(otherStream, [numTasks]) `, `cogroup(otherStream, [numTasks])`, `transform(func)`, `updateStateByKey(func)`
- 針對 window 的轉換: `window(windowLength, slideInterval)`, `countByWindow(windowLength, slideInterval)`, `reduceByWindow(func, windowLength, slideInterval)`, `reduceByKeyAndWindow(func, windowLength, slideInterval, [numTasks])`, `reduceByKeyAndWindow(func, invFunc, windowLength, slideInterval, [numTasks])`, `countByValueAndWindow(windowLength, slideInterval, [numTasks])`

### DStream 輸出操作 (Output Operations)

DStream 的資料可以輸出到外部資料庫或檔案系統。因為允許資料累積在外部系統，所以會觸發真正的轉換動作。

目前支援幾類輸出動作: `print()`, `saveAsTextFiles(prefix, [suffix])`, `saveAsObjectFiles(prefix, [suffix])`, `saveAsHadoopFiles(prefix, [suffix])`, `foreachRDD(func)`

> 重點在 `foreachRDD(func)` 的用法，這是最通用的輸出方式，應用函數 `func` 把 RDD 中的數據保存到外部系統，如文件、或通過網路連接保存到資料庫。但我對資料庫的理解程度還不夠銜接，先 bookmark [Spark踩坑记——数据库（Hbase+Mysql）](http://www.cnblogs.com/xlturing/p/spark.html) 這篇文章，有機會再回過頭看。

### Accumulators and Broadcast Variables
### DataFrame and SQL Operations
### MLlib Operations
### Caching / Persistence
### Checkpointing
### Deploying Applications
### Monitoring Applications
