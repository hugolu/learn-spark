# Spark on Docker

## 參考
- [Spark手把手-快速上手營](http://eighty20.cc/apps/e2-spk-v01/agenda.html)
- [sequenceiq/spark](https://hub.docker.com/r/sequenceiq/spark/)

## 準備環境

### 下載 Spark Docker Image
```shell
$ ￼￼￼docker pull sequenceiq/spark:1.6.0
```

### 執行 Image
```shell
$ docker run -it -p 8088:8088 -p 8042:8042 -h sandbox sequenceiq/spark:1.6.0 bash
bash-4.1#
```
```shell
$ docker run -d -h sandbox --name  sequenceiq/spark:1.6.0 -d
$ docker exec -it spark bash
bash-4.1#
```

### Hello World
```shell
bash-4.1# spark-shell --master local
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 1.6.0
      /_/

Using Scala version 2.10.5 (Java HotSpot(TM) 64-Bit Server VM, Java 1.7.0_51)
Type in expressions to have them evaluated.
Type :help for more information.
Spark context available as sc.
SQL context available as sqlContext.

scala> sc.parallelize(1 to 1000).count()
res0: Long = 1000
```
