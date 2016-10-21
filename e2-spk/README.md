# e2-spark 自我練習

活動網址：[Spark 手把手 - 快速上手營](http://eighty20.cc/apps/e2-spk-v01/index.html)

## Session 1
source: [s01](src/main/scala/cc/eighty20/spark/s01)

```shell
$ spark-submit --class cc.eighty20.spark.s01.sc00 target/scala-2.11/e2-spk-app_2.11-1.0.0.jar
$ spark-submit --class cc.eighty20.spark.s01.sc01 target/scala-2.11/e2-spk-app_2.11-1.0.0.jar
$ spark-submit --class cc.eighty20.spark.s01.sc02 target/scala-2.11/e2-spk-app_2.11-1.0.0.jar 
$ spark-submit --class cc.eighty20.spark.s01.sc03 target/scala-2.11/e2-spk-app_2.11-1.0.0.jar 
```

## Session 2
source: [s02](src/main/scala/cc/eighty20/spark/s02)

```shell
$ spark-submit --class cc.eighty20.spark.s02.sc00 target/scala-2.11/e2-spk-app_2.11-1.0.0.jar
$ spark-submit --class cc.eighty20.spark.s02.sc01 target/scala-2.11/e2-spk-app_2.11-1.0.0.jar
$ spark-submit --class cc.eighty20.spark.s02.df00 target/scala-2.11/e2-spk-app_2.11-1.0.0.jar
$ spark-submit --class cc.eighty20.spark.s02.df01 target/scala-2.11/e2-spk-app_2.11-1.0.0.jar
```

### 資料處理的步驟
1. ￼收集資料並透過 Spark RDD 做基本前處理
2. 將 RDD 轉換成 DataFrame 物件，並註冊成 Table
3. 透過 Spark SQL 來分析

```shell
$ spark-submit --class cc.eighty20.spark.s02.ebay target/scala-2.11/e2-spk-app_2.11-1.0.0.jar
$ spark-submit --class cc.eighty20.spark.s02.sfpd --jars jars/spark-csv_2.10-1.4.0.jar,jars/commons-csv-1.1.jar target/scala-2.11/e2-spk-app_2.11-1.0.0.jar
```
- `spark.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("data/sfpd.csv")` 透過 spark-csv 處理 csv 讀取格式細節

## Session 3
source: [s03](src/main/scala/cc/eighty20/spark/s03)

環境設定:
```shell
$ docker run -v $HOME/learn-spark/e2-spk:/common:ro --name db -e MYSQL_ROOT_PASSWORD=123 -d mariadb
$ docker exec db mysql -uroot -p123 -e "source /common/data/northwind.sql"
$ docker exec db mysql -uroot -p123 -e "show databases"
$ docker exec db mysql -uroot -p123 -e "show tables" northwind
```
```shell
$ docker run -v $HOME/learn-spark/e2-spk:/common:rw -p 8080:8080 --name zeppelin --link db:db -d dylanmei/zeppelin
$ docker exec -it zeppelin bash # 進入 zeppelin 操作 spark
```

@zeppelin:
```
# spark-submit --class cc.eighty20.spark.s03.zp00 /common/target/scala-2.11/e2-spk-app_2.11-1.0.0.jar
# spark-submit --class cc.eighty20.spark.s03.zp01 --jars /common/jars/commons-csv-1.1.jar,/common/jars/spark-csv_2.10-1.4.0.jar  /common/target/scala-2.11/e2-spk-app_2.11-1.0.0.jar
# spark-submit --class cc.eighty20.spark.s03.zp02 --jars /common/jars/commons-csv-1.1.jar,/common/jars/spark-csv_2.10-1.4.0.jar,/common/jars/mysql-connector-java-5.1.39-bin.jar /common/target/scala-2.11/e2-spk-app_2.11-1.0.0.jar
```

## Session 4
Type | Data source | Computing 
-----|-------------|-----------
![](http://eighty20.cc/apps/e2-spk-v01/present/e2-spk-s04/assets/imgs/team-1-layout.png)| centralized | centralized
![](http://eighty20.cc/apps/e2-spk-v01/present/e2-spk-s04/assets/imgs/team-2-layout.png)| centralized | distributed
![](http://eighty20.cc/apps/e2-spk-v01/present/e2-spk-s04/assets/imgs/team-3-layout.png)| distributed | distributed

為什麼 Sorting 對分散式計算這麼重要?
- At the core of sorting is the **shuffle** operation, which moves data across all machines.
- **Shuffle** underpins almost all distributed data processing workloads.
- **Sorting**, however, is one of the most challenging because there is no reduction of data along the pipeline.
- 了解Sorting的概念對於設計Spark程式與運維Spark是很重要的

### Spark DevOps 大雜燴
- [Spark+HDFS vs 傳統資料庫](spark-summit-east-2016/not-your-fathers-database-how-to-use-apache-spark-properly-in-your-big-data-architecture.md)
- [開發 SPARK APP 最常犯的前五項錯誤](spark-summit-east-2016/top-5-mistakes-when-writing-spark-applications.md)
- [部署 SPARK 的 OPERATION TIPS](spark-summit-east-2016/operational-tips-for-deploying-spark.md)

## Session 5

環境設定:
```shell
# source env.sh 192.168.0.101
export ZOOKEEPER_HOST_IP=$1
export KAFKA_HOST_IP=$1
```
```shell
$ docker run --name zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 -d jplock/zookeeper
$ docker run --name kafka -p 9092:9092 -p 7203:7203 --env KAFKA_ADVERTISED_HOST_NAME=${KAFKA_HOST_IP} --env ZOOKEEPER_IP=${ZOOKEEPER_HOST_IP} -d ches/kafka
```
```shell
$ docker run --rm -i ches/kafka \
kafka-console-consumer.sh \
--topic test \
--from-beginning \
--zookeeper ${ZOOKEEPER_HOST_IP}:2181

$ docker run --rm -i ches/kafka \
kafka-console-producer.sh \
--topic test \
--broker-list ${KAFKA_HOST_IP}:9092
```

[Apache Kafka](http://kafka.apache.org/) 與傳統訊息佇列不同處：
- ￼￼￼￼Scalability: 能水平擴展，增加處理訊息的能力
- Distributed: 分散應付許多訊息 publisher 與 subscriber
- Reliability: 能穩定傳送訊息，發生錯誤時能自動平衡附載
- High-Throughput: 提供高訊息吞吐量

### Kafka 實體元件
元件 | 說明
----|----
Producer  | 將訊息傳入 ￼￼Borker 的訊息產生者
Consumer  | 從 Broker 收取訊息的訊息消費者
Broker    | Kafka cluster 的節點
Zookeeper | Kafka cluster 不同 Broker 的協調者 (選舉 master、資料 deplication)

### Kafka 邏輯元件
元件 | 說明
----|----
Topic     | 訊息傳進 Borker 的目的地名稱
Partition | 一個 Topic 可以有多個 partition (Kafka 平行處理的基本單位)
Message   | 鍵值對 - Key 決定訊息落在哪個 partition，value 存放訊息內容。

### 訊息佇列 (one partition, one consumer)
#### Message Publisher
```shell
$ java -cp jars/e2-spk-s05-1.0.jar cc.eighty20.e2spks05.S05_01_Heartbeat_Publisher \
-b ${KAFKA_HOST_IP}:9092 \
-n 1 \
-t S05_01 \
-u hugo \
-r 5
```
#### Message Subscriber 
```shell
$ java -cp jars/e2-spk-s05-1.0.jar cc.eighty20.e2spks05.S05_02_Heartbeat_Subscriber \
-b ${KAFKA_HOST_IP}:9092 \
-t S05_01 \
-g consumer_group
```
#### 檢查 topic
```shell
$ docker run --rm -i ches/kafka \
kafka-topics.sh --zookeeper ${ZOOKEEPER_HOST_IP}:2181 \
--describe --topic S05_01

Topic:S05_01   	PartitionCount:1       	ReplicationFactor:1    	Configs:
       	Topic: S05_01  	Partition: 0   	Leader: 0      	Replicas: 0    	Isr: 0
```

### 訊息發佈/訂閱 (one partition, multiple consumer)
#### 訊息發佈
```shell
$ java -cp jars/e2-spk-s05-1.0.jar cc.eighty20.e2spks05.S05_01_Heartbeat_Publisher \
-b ${KAFKA_HOST_IP}:9092 \
-n 1 \
-t S05_02 \
-u hugo
```
#### 訊息訂閱#1
```shell
$ java -cp jars/e2-spk-s05-1.0.jar cc.eighty20.e2spks05.S05_02_Heartbeat_Subscriber \
-b ${KAFKA_HOST_IP}:9092 \
-t S05_02 \
-g consumer_group_01 \
-v true \
-r 10
```
#### 訊息訂閱#2
```shell
$ java -cp jars/e2-spk-s05-1.0.jar cc.eighty20.e2spks05.S05_02_Heartbeat_Subscriber \
-b ${KAFKA_HOST_IP}:9092 \
-t S05_02 \
-g consumer_group_02 \
-v true \
-r 10
```

### 多個分割區
#### 產生一個Multiple Partition的Topic
```shell
$ docker run --rm -i ches/kafka \
kafka-topics.sh \
--zookeeper ${ZOOKEEPER_HOST_IP}:2181 \
--create --topic S05_03 \
--partitions 3 \
--replication-factor 1
```

#### 檢視Kafka Topic
```shell
$ docker run --rm -i ches/kafka \
kafka-topics.sh --zookeeper ${ZOOKEEPER_HOST_IP}:2181 \
--describe --topic S05_03

Topic:S05_03   	PartitionCount:3       	ReplicationFactor:1      	Configs:
       	Topic: S05_03  	Partition: 0   	Leader: 0      	Replicas: 0      	Isr: 0
       	Topic: S05_03  	Partition: 1   	Leader: 0      	Replicas: 0      	Isr: 0
       	Topic: S05_03  	Partition: 2   	Leader: 0      	Replicas: 0      	Isr: 0
```

### Message With Key + Multi-parition Topic
#### 訊息發佈
```shell
$ java -cp jars/e2-spk-s05-1.0.jar cc.eighty20.e2spks05.S05_01_Heartbeat_Publisher \
-b ${KAFKA_HOST_IP}:9092 \
-n 1 \
-t S05_03 \
-u hugo
```
```shell
$ java -cp jars/e2-spk-s05-1.0.jar cc.eighty20.e2spks05.S05_01_Heartbeat_Publisher \
-b ${KAFKA_HOST_IP}:9092 \
-n 1 \
-t S05_03 \
-u eddy
```

#### 訊息訂閱
```shell
$ java -cp jars/e2-spk-s05-1.0.jar cc.eighty20.e2spks05.S05_02_Heartbeat_Subscriber \
-b ${KAFKA_HOST_IP}:9092 \
-t S05_03 \
-g consumer_group \
-v true \
-r 10
```

### Message Without Key + Multi-parition Topic
#### 訊息發佈
```shell
$ java -cp jars/e2-spk-s05-1.0.jar cc.eighty20.e2spks05.S05_05_Heartbeat_Publisher_WithoutKey \
-b ${KAFKA_HOST_IP}:9092 \
-n 1 \
-t S05_03 \
-u hugo
```

#### 訊息訂閱
```shell
$ java -cp jars/e2-spk-s05-1.0.jar cc.eighty20.e2spks05.S05_02_Heartbeat_Subscriber \
-b ${KAFKA_HOST_IP}:9092 \
-t S05_03 \
-g consumer_group \
-v true \
-r 10
```

### KAFKA 消費群組 (consumer group)
#### 設定 Topic
```shell
$ docker run --rm -i ches/kafka \
kafka-topics.sh \
--zookeeper ${ZOOKEEPER_HOST_IP}:2181 \
--create --topic S05_04 \
--partitions 1 \
--replication-factor 1 \
--config cleanup.policy=compact
```

#### 檢視 Topic
```shell
$ docker run --rm -i ches/kafka \
kafka-topics.sh \
--zookeeper ${ZOOKEEPER_HOST_IP}:2181 \
--describe \
--topic S05_04

Topic:S05_04   	PartitionCount:1       	ReplicationFactor:1    	Configs:cleanup.policy=compact
       	Topic: S05_04  	Partition: 0   	Leader: 0      	Replicas: 0    	Isr: 0
```

## Session 6
參考資料: [Spark Streaming + Kafka Integration Guide](https://spark.apache.org/docs/2.0.0-preview/streaming-kafka-integration.html)

```shell
$ sbt "run-main cc.eighty20.spark.s06.ss00 ${KAFKA_HOST_IP}:9092 CHAT_STREAM 5 data/README.md" # string stream publisher
$ sbt "run-main cc.eighty20.spark.s06.ss01 ${KAFKA_HOST_IP}:9092 CHAT_STREAM 5"                # string stream processor
$ sbt "run-main cc.eighty20.spark.s06.ss02 ${KAFKA_HOST_IP}:9092 CHAT_STREAM 5"                # string stream processor
$ sbt "run-main cc.eighty20.spark.s06.ss03 ${KAFKA_HOST_IP}:9092 CHAT_STREAM 5"                # string stream processor
$ sbt "run-main cc.eighty20.spark.s06.ss04 ${KAFKA_HOST_IP}:9092 CHAT_STREAM 5"                # string stream processor
```
- ss01: 直接顯示收到的訊息
- ss02: 找出 tumbling window 中出現文字的 topN，對每個 RDD 處理，計算出現最多的文字
- ss03: 找出 tumbling window 中出現文字的 topN，將把 TopN 的運算拉出 RDD 外面
- ss04: 找出 tumbling window 中出現文字的 topN，將每個 RDD 轉成 Dataframe，使用 Spark SQL 找出 TopN

```shell
$ sbt "run-main cc.eighty20.spark.s06.he00 ${KAFKA_HOST_IP}:9092 HEARTBEAT_STREAM hugo 65"    # heartbeat event producer
$ sbt "run-main cc.eighty20.spark.s06.he00 ${KAFKA_HOST_IP}:9092 HEARTBEAT_STREAM eddy 65"    # heartbeat event producer
$ sbt "run-main cc.eighty20.spark.s06.he01 ${KAFKA_HOST_IP}:9092 HEARTBEAT_STREAM group0"     # heartbeat event consumer
$ sbt "run-main cc.eighty20.spark.s06.he02 ${KAFKA_HOST_IP}:9092 HEARTBEAT_STREAM"            # heartbeat event consumer
$ sbt "run-main cc.eighty20.spark.s06.he03 ${KAFKA_HOST_IP}:9092 HEARTBEAT_STREAM /tmp/cp"    # heartbeat event consumer
$ sbt "run-main cc.eighty20.spark.s06.he04 ${KAFKA_HOST_IP}:9092 HEARTBEAT_STREAM /tmp/cp"    # heartbeat event consumer
```
- he01: 直接顯示收到的 heartbeat events
- he02: 使用 SparkSession 讀取 Json 訊息，然後透過 Spark SQL 計算一秒內的 heartbeat events
- he03: 使用 countByWindow 計算一分鐘內總共出現幾次 heartbeat events
- he04: 使用 reduceByKeyAndWindow 計算一分鐘內每個人出現幾次 heartbeat events
