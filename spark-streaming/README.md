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
source: [NetworkWordCount.scala](quick-example/NetworkWordCount.scala)

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

### DStream 輸入來源與接收器
### DStream 轉換動作 (Transformations)
### DStream 輸出操作 (Output Operations)

### Accumulators and Broadcast Variables
### DataFrame and SQL Operations
### MLlib Operations
### Caching / Persistence
### Checkpointing
### Deploying Applications
### Monitoring Applications
