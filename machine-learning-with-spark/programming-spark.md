# Spark Scala 入門

source: [ex-1.4](src/ex-1.4/)

編寫 scala 程式分析商品購買紀錄，檔案
- data/UserPurchaseHistory.csv - 購買紀錄
- build.sbt - sbt 配置文件
- scalaApp.scala - scala 應用程式

```shell
$ sbt package
$ spark-submit --class ScalaApp target/scala-2.11/scala-spark-app_2.11-1.0.jar
Total purchase: 5
Unique users: 4
Total revenue: 39.91
Most popular product: iPhone Cover with 2 purchases.
```

# Spark Python 入門

source: [ex-1.6](src/ex-1.6/)

編寫 Python 程式分析商品購買紀錄，檔案
- data/UserPurchaseHistory.csv - 購買紀錄
- pythonapp.py - python 應用程式

```shell
$ spark-submit pythonapp.py
Total pruchase: 5
Unique users: 4
Total revenue: 39.91
Most popular product: John with 2 purchases
```
