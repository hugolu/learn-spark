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
```
