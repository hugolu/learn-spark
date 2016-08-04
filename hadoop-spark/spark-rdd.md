# Spark RDD

RDD (Resilient Destributed Dataset) 彈性分散式資料集，是一種分散式的記憶體。Spark 的優勢來自 RDD 的特性，RDD 能與其他系統相容，可匯入外部儲存系統的資料，如 HDFS, HBase, 其他 Hadoop 資料。

## RDD 特性
運算類型 | 說明
---------|------
轉換 (Transformation) | 運算結果產生另一個 RDD，但 RDD 有 lazy 特性，在動作發生前，轉換運算不會被執行
動作 (Action)         | 運算結果不會產生另一個 RDD，會產生數值、陣列、或寫檔，動作發生時會立即運算，連同之前的轉換一併執行
持久化 (Presistence)  | 將重複使用的 RDD 持久化放在記憶體，以便後續使用加速效能

### Lineage 機制具備容錯能力
RDD 有 immutable 特性，記錄每個 RDD 與其父代 RDD 的關聯，會紀錄透過什麼操作才由父代 RDD 得到該 RDD 的特性。

如果某節點故障，儲存上面的 RDD 毀損，能重新執行一連串的轉換產生新資料，避免特定節點故障造成整個系統無法運作。

## 基本轉換運算
```scala
scala> val intRDD = sc.parallelize(List(3, 1, 2, 5, 5))
intRDD: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[1] at parallelize at <console>:21

scala> val intRDD2 = intRDD.map(_+1)
intRDD2: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[2] at map at <console>:23

scala> intRDD2.collect()
res1: Array[Int] = Array(4, 2, 3, 6, 6)
```

### filter
```scala
scala> intRDD.filter(_ < 3).collect
res4: Array[Int] = Array(1, 2)
```

### distinct
```scala
scala> intRDD.distinct.collect
res5: Array[Int] = Array(1, 3, 5, 2)
```

### groupBy
```scala
scala> val gRDD = intRDD.groupBy(x => if (x % 2 == 0) "even" else "odd").collect
gRDD: Array[(String, Iterable[Int])] = Array((even,CompactBuffer(2)), (odd,CompactBuffer(3, 1, 5, 5)))
```

## 多個轉換運算
```scala
scala> val intRDD1 = sc.parallelize(List(3, 1, 2, 5, 5))
intRDD1: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[0] at parallelize at <console>:21

scala> val intRDD2 = sc.parallelize(List(5, 6))
intRDD2: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[1] at parallelize at <console>:21

scala> val intRDD3 = sc.parallelize(List(2, 7))
intRDD3: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[2] at parallelize at <console>:21
```

### union 聯集
```scala
scala> intRDD1.union(intRDD2).union(intRDD3).collect
res0: Array[Int] = Array(3, 1, 2, 5, 5, 5, 6, 2, 7)

scala> (intRDD1 ++ intRDD2 ++ intRDD3).collect
res2: Array[Int] = Array(3, 1, 2, 5, 5, 5, 6, 2, 7)
```

### intersection 交集
```scala
scala> intRDD1.intersection(intRDD2).collect
res3: Array[Int] = Array(5)
```

### subtract 差集
```scala
scala> intRDD1.subtract(intRDD2).collect
res4: Array[Int] = Array(1, 2, 3)
```
- intRDD1 扣除 intRDD2 重複的部分

### cartesian
```scala
scala> intRDD1.cartesian(intRDD2).collect
res5: Array[(Int, Int)] = Array((3,5), (3,6), (1,5), (1,6), (2,5), (2,6), (5,5), (5,6), (5,5), (5,6))
```
- 5*2=10 種組合

## 基本動作運算

### 讀取元素
```scala
scala> intRDD.first
res0: Int = 3

scala> intRDD.take(2)
res1: Array[Int] = Array(3, 1)

scala> intRDD.takeOrdered(3)
res2: Array[Int] = Array(1, 2, 3)

scala> intRDD.takeOrdered(3)(Ordering[Int].reverse)
res3: Array[Int] = Array(5, 5, 3)
```

### 統計功能
```scala
scala> intRDD.stats
res4: org.apache.spark.util.StatCounter = (count: 5, mean: 3.200000, stdev: 1.600000, max: 5.000000, min: 1.000000)

scala> intRDD.count
res5: Long = 5

scala> intRDD.mean
res6: Double = 3.2

scala> intRDD.stdev
res7: Double = 1.6

scala> intRDD.max
res8: Int = 5

scala> intRDD.min
res9: Int = 1

scala> intRDD.sum
res10: Double = 16.0
```

## Key-Value 基本轉換運算

## Key-Value 多個轉換運算

## Key-Value 動作運算

## Broadcast 廣播變數

## accumulator 累加器

## RDD 持久化

## Spark WordCount
