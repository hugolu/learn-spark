# Spark on Docker

## 參考
- [Spark手把手-快速上手營](http://eighty20.cc/apps/e2-spk-v01/agenda.html)
- [sequenceiq/spark](https://hub.docker.com/r/sequenceiq/spark/)
- [gettyimages/spark](https://hub.docker.com/r/gettyimages/spark/)

## Spark 1.6

### 下載 Spark Docker Image
```shell
$ ￼￼￼docker pull sequenceiq/spark:1.6.0
```

### 執行 Image
```shell
$ docker run -it -p 8088:8088 -p 8042:8042 -h sandbox sequenceiq/spark:1.6.0 bash
```
```shell
$ docker run -d -h sandbox --name  sequenceiq/spark:1.6.0 -d
$ docker exec -it spark bash
```

### Hello World
```shell
bash-4.1# spark-shell --master local
...
scala> sc.parallelize(1 to 1000).count()
res0: Long = 1000
```

## Spark 2.0.1

### 下載 Spark Docker Image
```shell
$ docker pull gettyimages/spark:2.0.1-hadoop-2.7
```

### 執行 Image
```shell
$ docker run --name spark -v /Users/hugo/learn-spark/spark-on-docker:/volume -d gettyimages/spark
```

### Word Count
source: [wordCount.scala](wordCount.scala)
```shell
$ docker exec -it spark bash
root@ada019d9cc5e:/usr/spark-2.0.1# cd /volume
root@ada019d9cc5e:/volume# spark-submit --class WordCount target/scala-2.11/spark-on-docker_2.11-1.0.0.jar
```
