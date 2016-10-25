# Spark Streaming + Kafka 整合筆記

參考: 
- [Spark Streaming + Kafka Integration Guide (Kafka broker version 0.10.0 or higher)](http://spark.apache.org/docs/latest/streaming-kafka-0-10-integration.html)
- [Easy JSON (un)marshalling in Scala with Jackson](https://coderwall.com/p/o--apg/easy-json-un-marshalling-in-scala-with-jackson)
- [Spark Streaming + Kakfa 编程指北](http://www.jianshu.com/p/674a51feba6c)

## 環境
```shell
$ docker run --name spark -v /Users/hugo/learn-spark/spark-streaming-kafka-integration:/volume -d gettyimages/spark:2.0.1-hadoop-2.7
```

## LocationStrategies

新版 kafka consumer API 可以預取訊息。Spark 把保存快取的 consumer 放在 executor (而不是每個 batch 重新產生)，傾向在 consumer 存在的 host 安排 partition。

- 大部份情況，使用 `LocationStrategies.PreferConsistent`，把 partition 均勻分散到可用的 executor
- 如果 executor 跟 kafka broker 在同一機器，使用 `LocationStrategies.PreferBrokers`
- 如果 partition 有很嚴重的 skew，使用 `LocationStrategies.PreferFixed`，讓你明確地義 map partition to host

## ConsumerStrategies

新版 kafka consumer API 提供一些定義 topic 的方式，部分需要考慮 post-object-instantiation 設定。

- `ConsumerStrategies.Subscribe` 允許訂閱一些固定的 topic
- `ConsumerStrategies.SubscribePattern` 允許使用正規表示法描述訂閱的 topic
- `ConsumerStrategies.Assign` 允許定義一些固定的 partition

<< TBC >>

## Creating an RDD
## Obtaining Offsets
## SSL / TLS
## Deploying
