# 安裝 Spark

## Spark Cluster 架構圖

![](http://spark.apache.org/docs/latest/img/cluster-overview.png)

- DriverProgram: 程式設計寫的 spark code，程式中定義 spark context 作為應用程式的入口
- SparkContext: 透過 Cluster Manager 管理 Worker Nodes，每個 Work Node 執行計算任務

Cluster Manager 可執行的模式
- Local machine: 程式只要 import Spark 程式庫就能只在本機執行
- Spark standalone cluster: 由 Spark 提供的 cluster 管理模式，執行平行運算，直接存取 Local Disk 或 HDFS
- Hadoop YARN: Hadoop 2.0 新架構中更高效的資源管理核心。Spark 在 YARN 上執行，讓 YARN 幫他進行多台機器的資源管理
- Cloud: 針對更大規模的計算

> YARN: Yet Another Resource Negotiator

## Scala 安裝

Scala 特性:
- 可編譯成 Java bytecode 在 JVM 上執行
- 可使用 Java library
- 函數式語言
- 物件導向語言

```shell
$ wget http://www.scala-lang.org/files/archive/scala-2.11.8.tgz
$ tar zxf scala-2.11.8.tgz
$ sudo mv scala-2.11.8 /usr/local/scala
$ vi ~/.bashrc
$ source ~/.bashrc
$ scala -version
```

.bashrc 加入以下內容
```
# scala variables
export SCALA_HOME=/usr/local/scala
export PATH=$PATH:$SCALA_HOME/bin
```

## Spark 安裝
```shell
$ wget http://archive.apache.org/dist/spark/spark-1.4.0/spark-1.4.0-bin-hadoop2.6.tgz
$ tar zxf spark-1.4.0-bin-hadoop2.6.tgz
$ sudo mv spark-1.4.0-bin-hadoop2.6 /usr/local/spark
$ vi ~/.bashrc
$ source ~/.bashrc
```

.bashrc 加入以下內容
```
# spark variables
export SPARK_HOME=/usr/local/spark
export PATH=$PATH:$SPARK_HOME/bin
```

## 啟動 spark-shell 互動介面

## 設定 spark-shell 顯示訊息

## 啟動 Hadoop

## spark-shell (Local machine)

## spark-shell (Hadoop YARN)

## 建置 Spark standalone cluster 執行環境

## spark-shell (Spark standalone)
