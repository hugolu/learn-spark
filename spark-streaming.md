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

