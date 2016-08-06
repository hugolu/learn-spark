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

### src/main/scala/SimpleApp.scala
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

### simple.sbt
```shell
$ vi simple.sbt
```
```sbt
name := "Simple Project"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.0.0"
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
