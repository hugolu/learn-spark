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

### [HOW TO USE APACHE SPARK PROPERLY IN YOUR BIG DATA ARCHITECTURE](https://spark-summit.org/east-2016/events/not-your-fathers-database-how-to-use-apache-spark-properly-in-your-big-data-architecture/)

#### 優點：一般化處理
- 資料形式：檔案、非結構化資料、社群媒體或網頁資料集、資料備份
- 工作形式：批次、臨時性分析、多步驟 pipeline、交互查詢
- 好處：便宜儲存、擴充彈性、速度與規模
> Pipeline是一系列的Stage按照声明的顺序排列成的工作流

#### 缺點：隨機存取
```
sqlContext.sql("select * from my_large_table where id=2134823")
```
- 很沒效率 - spark 可能要搜尋所有檔案才能找到需要的 row

解方: 如果要隨機存取資料，使用資料庫。
- RMDB 使用 index
- Key-Value NoSQL 使用 key 找 value

#### 缺點：頻繁插入
```
sqlContext.sql("insert TABLE myTable select fields from my2ndTable")
```
- 每次插入都會創建新檔案 - 創建很快、但未來查詢會很慢

解方：
- 使用資料庫支援插入
- 定期合併(壓縮)小檔案

#### 優點：資料轉換 (ETL)
檔案儲存成本很便宜，可以將資料轉換成任意格式 (LOG, Parquet, JSON, CSV, ZIP)

#### 缺點：頻繁/定增的更新
不支援更新 (Update) 語法 = 隨機存取 + 刪除 + 插入
- 檔案格式無法對更新動作最佳化

解方：
- 使用資料庫做有效率的更新操作

使用案例：
- Database Snapshot + Incremental SQL Query → Spark
- 秘訣: 使用 ClusterBy 快速合併

#### 優點：連接 BI 工具
HDFD ← Spark ← Tableau
- 秘訣: 表格快取優化效能

#### 缺點：外部報表
無法同時處理太多請求

解方：
- 將分析結果儲存到資料庫，處理外部查詢

#### 優點：機器學習 & 資料科學
使用 MLlib, GraphX, Spark packages，好處
- 內建分散式演算法
- In-memory 適合迭代性工作
- 資料清潔、特徵化、訓練、測試...

#### 缺點：搜尋內容
```
sqlContext.sql("select * from mytable where name like '%xyz%'")
```
- spark 會找遍所有資料

解方：
- 使用 elastic, solr

### [TOP 5 MISTAKES WHEN WRITING SPARK APPLICATIONS](https://spark-summit.org/east-2016/events/top-5-mistakes-when-writing-spark-applications/)

### [OPERATIONAL TIPS FOR DEPLOYING SPARK](https://spark-summit.org/east-2016/events/operational-tips-for-deploying-spark/)
## Session 5

環境設定:
```shell
$ KAFKA_HOST_IP=127.0.0.1
$ ZOOKEEPER_HOST_IP=127.0.0.1
```
```shell
$ docker run --name zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 -d jplock/zookeeper
$ docker run --name kafka -p 9092:9092 -p 7203:7203 --env KAFKA_ADVERTISED_HOST_NAME=${KAFKA_HOST_IP} --env ZOOKEEPER_IP=${ZOOKEEPER_HOST_IP} -d ches/kafka
```

```shell
$ docker run --rm -i ches/kafka kafka-console-consumer.sh --topic test --from-beginning --zookeeper ${ZOOKEEPER_HOST_IP}:2181
$ docker run --rm -i ches/kafka kafka-console-producer.sh --topic test --broker-list ${KAFKA_HOST_IP}:9092
```
