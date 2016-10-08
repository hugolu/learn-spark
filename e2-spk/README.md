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
```

### 資料處理的步驟
1. ￼收集資料並透過 Spark RDD 做基本前處理
2. 將 RDD 轉換成 DataFrame 物件，並註冊成 Table
3. 透過 Spark SQL 來分析
