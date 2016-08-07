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
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.rdd.RDD

object WordCount {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.OFF)
    System.setProperty("spark.ui.showConsoleProgress", "false")

    println("開始執行 WordCount")
    val sc = new SparkContext(new SparkConf().setAppName("WordCount").setMaster("local[4]"))

    println("開始讀取文字...")
    val textFile = sc.textFile("input/sample.txt")

    println("開始建立 RDD...")
    val countsRDD = textFile.flatMap(line => line.split(" "))
                      .map(word => (word, 1))
                      .reduceByKey(_ + _)

    println("開始儲存至文件...")
    try {
      countsRDD.saveAsTextFile("output")
      println("已經儲存成功")
    } catch {
      case e: Exception => println("輸出目錄已經存在，請先刪除原有目錄")
    }
  }
}
```

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
