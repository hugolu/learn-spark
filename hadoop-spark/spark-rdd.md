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
Spark RDD 支援 Key-Value 運算，鍵值運算是 MapReduce 的基礎。

```scala
scala> val kvRDD1 = sc.parallelize(List((3,4), (3,6), (5,6), (1,2)))
kvRDD1: org.apache.spark.rdd.RDD[(Int, Int)] = ParallelCollectionRDD[0] at parallelize at <console>:21

scala> kvRDD1.keys.collect
res0: Array[Int] = Array(3, 3, 5, 1)

scala> kvRDD1.values.collect
res1: Array[Int] = Array(4, 6, 6, 2)

scala> kvRDD1.filter({ case (k, v) => k < 5 }).collect
res2: Array[(Int, Int)] = Array((3,4), (3,6), (1,2))

scala> kvRDD1.filter{ case (k, v) => v < 5 }.collect
res3: Array[(Int, Int)] = Array((3,4), (1,2))

scala> kvRDD1.mapValues(x=>x*x).collect
res4: Array[(Int, Int)] = Array((3,16), (3,36), (5,36), (1,4))

scala> kvRDD1.sortByKey().collect
res5: Array[(Int, Int)] = Array((1,2), (3,4), (3,6), (5,6))

scala> kvRDD1.sortByKey(false).collect
res6: Array[(Int, Int)] = Array((5,6), (3,4), (3,6), (1,2))

scala> kvRDD1.reduceByKey(_+_).collect
res7: Array[(Int, Int)] = Array((1,2), (3,10), (5,6))
```

## Key-Value 多個轉換運算

```scala
scala> val kvRDD1 = sc.parallelize(List((3,4), (3,6), (5,6), (1,2)))
kvRDD1: org.apache.spark.rdd.RDD[(Int, Int)] = ParallelCollectionRDD[0] at parallelize at <console>:21

scala> val kvRDD2 = sc.parallelize(List(3,8))
kvRDD2: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[1] at parallelize at <console>:21
```

### join
```scala
scala> kvRDD1.join(kvRDD2).foreach(println)
(3,(4,8))
(3,(6,8))
```
- 將兩個 RDD 依照相同的鍵值 join 起來 (key = 3)
- kvRDD1: (3,4), (3,6)
- kvRDD2: (3,8)

### leftOuterJoin
```scala
scala> kvRDD1.leftOuterJoin(kvRDD2).foreach(println)
(1,(2,None))
(3,(4,Some(8)))
(3,(6,Some(8)))
(5,(6,None))
```
- 由左邊集合 kvRDD1 對應到右邊集合 kvRDD2，顯示所有左邊集合中所有元素
- kvRDD2: (3,8)

kvRDD1 | result
-------|--------
(3,4)  | (3, (4, Some(8))
(3,6)  | (3, (6, Some(8))
(5,6)  | (5, (6, None))
(1,2)  | (1, (2, None))

### rigthOuterJoin
```scala
scala> kvRDD1.rightOuterJoin(kvRDD2).foreach(println)
(3,(Some(4),8))
(3,(Some(6),8))
```
- 由右邊集合 kvRDD2 對應到左邊集合 kvRDD1，顯示右邊所有集合中的元素
- kvRDD1: (3,4), (3,6), (5,6), (1,2)

kvRDD2 | result
-------|-------
(3,8)  | (3, (Some(4), 8))
       | (3, (Some(6), 8))

### substractByKey
```scala
scala> kvRDD1.subtractByKey(kvRDD2).collect
res7: Array[(Int, Int)] = Array((1,2), (5,6))
```
- 移除相同鍵值的資料

## Key-Value 動作運算
```scala
scala> kvRDD1.first
res0: (Int, Int) = (3,4)

scala> kvRDD1.first._1
res1: Int = 3

scala> kvRDD1.first._2
res2: Int = 4

scala> kvRDD1.take(2)
res3: Array[(Int, Int)] = Array((3,4), (3,6))

scala> kvRDD1.countByKey
res5: scala.collection.Map[Int,Long] = Map(1 -> 1, 3 -> 2, 5 -> 1)
```

```scala
scala> var kv = kvRDD1.collectAsMap()
kv: scala.collection.Map[Int,Int] = Map(5 -> 6, 1 -> 2, 3 -> 6)

scala> kv(3)
res6: Int = 6

scala> kv(1)
res7: Int = 2
```

```scala
scala> kvRDD1.lookup(3)
res9: Seq[Int] = WrappedArray(4, 6)

scala> kvRDD1.lookup(5)
res10: Seq[Int] = WrappedArray(6)

scala> kvRDD1.lookup(0)
res11: Seq[Int] = WrappedArray()
```

## Broadcast 廣播變數

### 不使用 broadcast
```scala
scala> val kvFruit = sc.parallelize(List((1, "apple"), (2, "orange"), (3, "banana"), (4, "grape")))
kvFruit: org.apache.spark.rdd.RDD[(Int, String)] = ParallelCollectionRDD[0] at parallelize at <console>:21

scala> val fruitMap = kvFruit.collectAsMap
fruitMap: scala.collection.Map[Int,String] = Map(2 -> orange, 4 -> grape, 1 -> apple, 3 -> banana)

scala> val fruitIds = sc.parallelize(List(2, 4, 1, 3))
fruitIds: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[1] at parallelize at <console>:21

scala> val fruitNames = fruitIds.map(x => fruitMap(x)).collect
fruitNames: Array[String] = Array(orange, grape, apple, banana)
```

平行處理中，每次轉換都須將 fruitIds 與 fruitMap 傳送到 worker node 才能執行運算。如果 fruitMap 很大，需要轉換的 fruitIds 也很大，會耗費很多記憶體與時間。

### 使用 broadcast
規則：
- 使用 SparkContext.broadcast([initial values]) 建立廣播變數
- 使用 .value 方法讀取廣播的值
- 廣播變數建立後不可修改

```scala
scala> val bcFruitMap = sc.broadcast(fruitMap)
bcFruitMap: org.apache.spark.broadcast.Broadcast[scala.collection.Map[Int,String]] = Broadcast(2)

scala> val fruitNames = fruitIds.map(x => bcFruitMap.value(x)).collect
fruitNames: Array[String] = Array(orange, grape, apple, banana)
```

平行處理中，bcFruitMap 廣播變數會傳送到 worker node，儲存在記憶體。後續 worker node 使用此 bcFruitMap 廣播變數執行轉換，可以節省很多記憶體(?)與傳送時間。

## accumulator 累加器

MapReduce 常用加總，為了方便平行運算，Spark 提供 accumulator 共享變數，規則如下：
- 使用 SparkContext.accumulator([initial values]) 建立累加器
- 使用 += 累加
- foreach 中不能讀取累加器的值
- 迴圈外才能使用 .value 讀取累加器的值

```scala
scala> val intRDD = sc.parallelize(List(3,1,2,5,5))
intRDD: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[0] at parallelize at <console>:21

scala> val total = sc.accumulator(0.0)
total: org.apache.spark.Accumulator[Double] = 0.0

scala> val num = sc.accumulator(0)
num: org.apache.spark.Accumulator[Int] = 0

scala> intRDD.foreach{ i =>
     |   total += i
     |   num += 1
     | }

scala> println("total=" + total.value + ", num=" + num.value)
total=16.0, num=5

scala> val avg = total.value / num.value
avg: Double = 3.2
```

## RDD 持久化
RDD 持久化機制，可以把想要重複運算的 RDD 儲存在記憶體中，提升運算效率。

### 持久化操作
```scala
scala> val intRddMemory = sc.parallelize(List(3,1,2,5,5))
intRddMemory: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[0] at parallelize at <console>:21

scala> intRddMemory.persist
res1: intRddMemory.type = ParallelCollectionRDD[0] at parallelize at <console>:21

scala> intRddMemory.unpersist()
res2: intRddMemory.type = ParallelCollectionRDD[0] at parallelize at <console>:21
```

### 儲存等級
- MEMORY_ONLY
- MEMORY_AND_DISK
- MEMORY_ONLY_SER
- MEMORY_AND_DISK_SER (序列化)
- DISK_ONLY
- MEMORY_ONLY_2, MEMORY_ANDK_DISK_2 (複製到兩個節點)

```scala
scala> import org.apache.spark.storage.StorageLevel
import org.apache.spark.storage.StorageLevel

scala> val intRddMemoryAndDisk = sc.parallelize(List(3,1,2,5,5))
intRddMemoryAndDisk: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[1] at parallelize at <console>:22

scala> intRddMemoryAndDisk.persist(StorageLevel.MEMORY_AND_DISK)
res4: intRddMemoryAndDisk.type = ParallelCollectionRDD[1] at parallelize at <console>:22

scala> intRddMemoryAndDisk.unpersist()
res5: intRddMemoryAndDisk.type = ParallelCollectionRDD[1] at parallelize at <console>:22
```

## Spark WordCount
```scala
val textFile = sc.textFile("file:/home/hduser/wordcount/input/sample.txt")
val stringRDD = textFile.flatMap(line => line.split(" "))
val countsRDD = stringRDD.map(word => (word, 1)).reduceByKey(_+_)
countsRDD.saveAsTextFile("file:/home/hduser/wordcount/output")
```
- `.textFile()` 讀取本地文字檔
- `.flatMap(line => line.split(" "))` 取出每個文字
- `.map(word => (word, 1))` 將每個文字轉成 key-value
- `.reduceByKey.(_+_)` 合併相同 key 的 value，使用 `_+_` 相加
- `.saveAsTextFile()` 結果寫回本地文件

```shell
$ cat /home/hduser/wordcount/output/part-00000
(dog,3)
(apple,2)
(cat,4)
```
