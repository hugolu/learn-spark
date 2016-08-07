# Spark IDE

身為 vim 死硬派，我決定跳過 IDE，直接用 sbt 編譯 Scala code for Spark。

## Self-Contained Applications
參考連結: [Spark Quick Start](http://spark.apache.org/docs/latest/quick-start.html)

### 產生專案目錄
```shell
$ mkdir test
$ cd test
$ mkdir -p src/main/scala
```

### 建立 SimpleApp.scala
```shell
$ vi src/main/scala/SimpleApp.scala
```
```scala
/* SimpleApp.scala */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object SimpleApp {
  def main(args: Array[String]) {
    val logFile = "/usr/local/spark/README.md"
    val conf = new SparkConf().setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val logData = sc.textFile(logFile, 2).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))
  }
}
```

### 建立 simple.sbt
```shell
$ vi simple.sbt
```
```sbt
name := "Simple Project"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.4.0"
```

### 編譯、打包
```shell
$ sbt compile
$ sbt package
```

### 執行
```shell
$ spark-submit \
> --class "SimpleApp" \
> --master local[4] \
> target/scala-2.11/simple-project_2.11-1.0.jar
16/08/06 14:26:22 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
16/08/06 14:26:22 WARN Utils: Your hostname, debian resolves to a loopback address: 127.0.1.1; using 10.0.2.15 instead (on interface eth0)
16/08/06 14:26:22 WARN Utils: Set SPARK_LOCAL_IP if you need to bind to another address
Lines with a: 60, Lines with b: 29
```

## 書本範例: WordCount

### 產生專案目錄
```shell
$ mkdir wordcount
$ cd wordcount
$ mkdir -p src/main/scala
```

## 建立 WordCount.scala
```shell
$ vi src/main/scala/WordCount.scala
```
```scala
import org.apache.log4j.Logger                                                                  //#1
import org.apache.log4j.Level
import org.apache.spark.{SparkContext, SparkConf}                                               //#2
import org.apache.spark.rdd.RDD

object WordCount {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.OFF)                                                 //#3
    System.setProperty("spark.ui.showConsoleProgress", "false")

    println("開始執行 WordCount")
    val sc = new SparkContext(new SparkConf().setAppName("WordCount").setMaster("local[4]"))    //#4

    println("開始讀取文字...")
    val textFile = sc.textFile("input/sample.txt")                                              //#5

    println("開始建立 RDD...")
    val countsRDD = textFile.flatMap(line => line.split(" "))                                   //#6
                      .map(word => (word, 1))
                      .reduceByKey(_ + _)

    println("開始儲存至文件...")
    try {
      countsRDD.saveAsTextFile("output")                                                        //#7
      println("已經儲存成功")
    } catch {
      case e: Exception => println("輸出目錄已經存在，請先刪除原有目錄")
    }
  }
}
```
- #1, #3: 設定不要顯示太多資訊
- #2: 匯入相關程式庫
- #4: 建立 spark context
- #5: 讀取文字檔
- #6: 執行 MapReduce
  - `.flatMap(line => line.split(" "))` 針對每一行，取出每個字，然後合併成一個集合
  - `.map(word => (word, 1))` 將集合中每個字轉成 key-value `tuple2[String, Int]`
  - `.reduceByKey(_ + _)` 合併同樣鍵值，使用 `_ + _` 密名函數
- #7: 儲存至檔案

### 建立 build.sbt
```shell
$ vi build.sbt
```
```sbt
name := "Word Count"

version := "1.0.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.4.0"
```

### 編譯、打包
```shell
$ sbt compile
$ sbt package
```

### 準備測試檔案
```shell
$ mkdir input
$ vi input/simple.txt
```

input/sample.txt:
```
apple dog cat
cat apple dog
cat dog cat
```

### 執行
```shell
$ spark-submit --class WordCount --master local[4] target/scala-2.11/word-count_2.11-1.0.0.jar
開始執行 WordCount
開始讀取文字...
開始建立 RDD...
開始儲存至文件...
已經儲存成功
```

### 查看結果
```
$ ls output/
part-00000  part-00001  _SUCCESS
$ cat output/part-00000
(dog,3)
(apple,2)
(cat,4)
```

## spark-submit 
參考連結: [Submitting Applications](http://spark.apache.org/docs/latest/submitting-applications.html)

### 使用 spark-submit 啟動應用程式
```shell
spark-submit \
  --class <main-class> \
  --master <master-url> \
  --deploy-mode <deploy-mode> \
  --conf <key>=<value> \
  ... # other options
  <application-jar> \
  [application-arguments]
```

選項 | 說明
-----|------
`--class <main-class>`        | 要執行的 application 主要類別名稱
`--master <master-url>`       | 設定執行環境
`--deploy-mode <deploy-mode>` | 要部署 driver 到 work node (cluster) 或在本機的一個外部 client 
`--conf <key>=<value>`        | spark 設定屬性，使用 `"key=value"` 格式
`--driver-memory MEM`         | driver 程式所使用的記憶體
`--executor-memory MEM`       | executor 程式所使用的記憶體
`--jars JARS`                 | 要執行的 application 會引用到的外部程式庫
`--name NAME`                 | 要執行的 application 名稱
`<application-jar>`           | 要執行的 application 的 jar 路徑
`[application-arguments]`     | 要傳遞給主要類別的 main 方法的參數

### Master URLs
`--master <master-url>` 選項：

Master URL | 說明
-----------|-----
`local`             | 本機執行，使用一個執行緒
`local [K]`         | 本機執行，使用K個執行緒 (使用本機多核心 CPU)
`local [*]`         | 本機執行，自動盡量利用本機上的多核心 CPU
`spark://HOST:PORT` | 在 Spark standalone cluster 執行預設 port=7077
`mesos://HOST:PORT` | 在 Mesos cluster 執行，預設 port=5050
`yarn-client`       | 在 Yarn cluster 執行


### 執行範例

```shell
# Run application locally on 8 cores
./bin/spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --master local[8] \
  /path/to/examples.jar \
  100
```
```shell
# Run on a Spark standalone cluster in client deploy mode
./bin/spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --master spark://207.184.161.138:7077 \
  --executor-memory 20G \
  --total-executor-cores 100 \
  /path/to/examples.jar \
  1000
```
```shell
# Run on a Spark standalone cluster in cluster deploy mode with supervise
./bin/spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --master spark://207.184.161.138:7077 \
  --deploy-mode cluster \
  --supervise \
  --executor-memory 20G \
  --total-executor-cores 100 \
  /path/to/examples.jar \
  1000
```
```shell
# Run on a YARN cluster
export HADOOP_CONF_DIR=XXX
./bin/spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --master yarn \
  --deploy-mode cluster \  # can be client for client mode
  --executor-memory 20G \
  --num-executors 50 \
  /path/to/examples.jar \
  1000
```
```shell
# Run a Python application on a Spark standalone cluster
./bin/spark-submit \
  --master spark://207.184.161.138:7077 \
  examples/src/main/python/pi.py \
  1000
```
```shell
# Run on a Mesos cluster in cluster deploy mode with supervise
./bin/spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --master mesos://207.184.161.138:7077 \
  --deploy-mode cluster \
  --supervise \
  --executor-memory 20G \
  --total-executor-cores 100 \
  http://path/to/examples.jar \
  1000
```
