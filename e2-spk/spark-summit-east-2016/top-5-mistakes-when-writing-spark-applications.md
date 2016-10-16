# [TOP 5 MISTAKES WHEN WRITING SPARK APPLICATIONS](https://spark-summit.org/east-2016/events/top-5-mistakes-when-writing-spark-applications/)

## 錯誤一
環境：6 nodes, 16 cores each, 64GB of RAM each

分配多少 executors, cores, memory？
- `--num-executors`
- `--executor-cores`
- `--executor-memory`

### Answer#1：一個 core 一個 executor
- 96 executors
- 4 GB/executor
- 1 core/executor

錯誤：沒用到同一個 JVM 執行多任務的好處

### Answer#2：一個 node 一個 executor
- 6 executors
- 64 GB/executor
- 16 core/executor

錯誤：需要留一些 memory overhead 給 OS/Hadoop daemons

### Answer#3：考慮額外 overhead
- 6 executors
- 63 GB/executor
- 15 core/executor

錯誤：YARN 需要記憶體、core、一個 executor 使用15個 core 會降低 HDFS I/O 效能

### 結論
- 5 core/executor (max HDFS throughput)
- 可用 core 數：6x15 = 90 (每個節點 Hadoop/Yarn daemon 佔用一個 core)
- 可用 executor 數：90/5 = 18
- 一個 executor 給 AM：剩下 17 個
- 每個 node 分配 3 個 executor
- 每個 executor 分配 63/3 x (1-0.07) ~ 19GB (減掉 head overhead)

### Correct Answer
- 17 executors
- 19 GB/executor
- 5 core/executor

## 錯誤二
```
java.lang.IllegalArgumentException: Size exceeds Integer.MAX_VALUE
```
- Spark shuffle block 不能超過 2GB

### Spark SQL
- Spark SQL 常見問題
- 執行 shuffle 時，預設 partition 數目是 200

### 怎麼做
- 增加 partition 數目 （減少 partition 大小)
- 排除資料 skew 問題

### 實際設定
- Spark SQL: 增加 `spark.sql.shuffle.partitions`
- Spark App: 設定 `rdd.repartition()` 或 `rdd.coalesce()`

### 總結
- 不要使用太大的 partition size
- 不要使用太少的 partition number 
- 經驗法則: 128MB / partition
- 如果 partition 數目接近 2000，但少於 2000，使用大於 2000 的數目

## 錯誤三

## 錯誤四

## 錯誤五
