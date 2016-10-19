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

```shell
$ java -cp jars/e2-spk-s05-1.0.jar cc.eighty20.e2spks05.S05_01_Heartbeat_Publisher \
-b ${KAFKA_HOST_IP}:9092 \
-n 1 \
-t S05_01 \
-u hugo \
-r 5
```
```shell
$ java -cp jars/e2-spk-s05-1.0.jar cc.eighty20.e2spks05.S05_02_Heartbeat_Subscriber \
-b ${KAFKA_HOST_IP}:9092 \
-t S05_01 \
-g consumer_group
```
```shell
$ docker run --rm -i ches/kafka \
> kafka-topics.sh --zookeeper ${ZOOKEEPER_HOST_IP}:2181 \
> --describe --topic S05_01
Topic:S05_01   	PartitionCount:1       	ReplicationFactor:1    	Configs:
       	Topic: S05_01  	Partition: 0   	Leader: 0      	Replicas: 0    	Isr: 0
```
