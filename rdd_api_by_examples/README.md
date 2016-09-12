# RDD API by Examples

之前練習操作 RDD Transformation & Action，找到一篇[Spark RDD API Examples](http://homepage.cs.latrobe.edu.au/zhe/ZhenHeSparkRDDAPIExamples.html)，裡面的說明與範例非常簡潔易懂，值得細細品味。以下按照網頁列出API的順序，紀錄理解的筆記與練習的過程。

基本的RDD API把每筆資料視為單一值。然而，使用者有時想操作key-value，因此，Spark擴充RDD介面以提供額外功能，這些函式就能處理key-value。這些特殊的函式有:

| 標記 | 名稱 | 說明 |
|------|------|------|
| [Double] | DoubleRDDFunctions | 這些擴充的方法包含許多總計數值的方法。如果資料轉換成 double type，就能使用這些方法。 |
| [Pair] | PairRDDFunctions | 這些擴充的方法能處理 tuple 結構，第一個項目是key，第二個項目是value。 |
| [Ordered] | OrderedRDDFunctions | 這些擴充的方法能處理 key 可以排序的 tuple 結構。 |
| [SeqFile] | SequenceFileRDDFunctions | 這些擴充的方法讓使用者可以從RDD產生Hadoop sequence file。 (把記憶體上的資料結構寫到檔案中，之後讀出能還原成原先的模樣) |

## Basic 
### aggregate
```scala
def aggregate[U](zeroValue: U)(seqOp: (U, T) ⇒ U, combOp: (U, U) ⇒ U)(implicit arg0: ClassTag[U]): U
```
Aggregate the elements of each partition, and then the results for all the partitions, using given combine functions and a neutral "zero value".

```scala
val a = sc.parallelize(List(1,2,3,4,5,6,7,8,9), 3)
a.aggregate("")((str, num) => num.toString + str, (str1, str2) => str1 + str2)  //> res3: String = 321654987
```

### cartesian
```scala
def cartesian[U](other: RDD[U])(implicit arg0: ClassTag[U]): RDD[(T, U)]
```
Return the Cartesian product of this RDD and another one, that is, the RDD of all pairs of elements (a, b) where a is in this and b is in other.

```scala
val a = sc.parallelize(List(1,2,3))         //a: org.apache.spark.rdd.RDD[Int]
val b = sc.parallelize(List("a","b","c"))   //b: org.apache.spark.rdd.RDD[String]
val c = a.cartesian(b)                      //c: org.apache.spark.rdd.RDD[(Int, String)]
c.collect                                   //> res5: Array[(Int, String)] = Array((1,a), (1,b), (1,c), (2,a), (2,b), (2,c), (3,a), (3,b), (3,c))
```

### coalesce
```scala
def coalesce(numPartitions: Int, shuffle: Boolean = false, partitionCoalescer: Option[PartitionCoalescer] = Option.empty)(implicit ord: Ordering[T] = null): RDD[T]
```
Return a new RDD that is reduced into numPartitions partitions.

```scala
val a = sc.parallelize(1 to 10, 2)
a.foreachPartition( iter => println(iter.toList.mkString(",")) )
//> 1,2,3,4,5
//> 6,7,8,9,10

val b = a.coalesce(3, false)
b.foreachPartition( iter => println(iter.toList.mkString(",")) )
//> 1,2,3,4,5
//> 6,7,8,9,10

val c = a.coalesce(3, true)
c.foreachPartition( iter => println(iter.toList.mkString(",")) )
//> 3,8
//> 1,4,6,9
//> 2,5,7,10
```
- 如果 shuffle=false 且 source RDD partition 數目小於 numPartitions，無法執行 re-partition

### repartition
```scala
def repartition(numPartitions: Int)(implicit ord: Ordering[T] = null): RDD[T]
```
Return a new RDD that has exactly numPartitions partitions.

```scala
val a = sc.parallelize(1 to 10, 2)
val b = a.repartition(3)
b.foreachPartition( iter => println(iter.toList.mkString(",")) )
//> 3,8
//> 1,4,6,9
//> 2,5,7,10
```

### collect
```scala
def collect(): Array[T]
```
Return an array that contains all of the elements in this RDD.

```scala
val a = sc.parallelize(1 to 10, 2)
a.collect       //> res1: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
```

### context, sparkContext
```scala
def context: SparkContext
def sparkContext: SparkContext
```
The org.apache.spark.SparkContext that this RDD was created on.

```scala
val a = sc.parallelize(1 to 10, 2)
a.context       //> res3: org.apache.spark.SparkContext = org.apache.spark.SparkContext@5ece7044
a.SparkContext  //> res4: org.apache.spark.SparkContext = org.apache.spark.SparkContext@5ece7044
```

### count
```scala
def count(): Long
```
Return the number of elements in the RDD.

```scala
val a = sc.parallelize(1 to 10, 2)
a.count     //> res5: Long = 10
```

### countApproxDistinct
```scala
def countApproxDistinct(relativeSD: Double = 0.05): Long
```
Return approximate number of distinct elements in the RDD.

```scala
val a = sc.parallelize(1 to 1000000, 100)
val b = a ++ a ++ a ++ a ++ a
b.countApproxDistinct(0.05)   //> res6: Long = 1083002
b.countApproxDistinct(0.01)   //> res7: Long = 1013205
b.countApproxDistinct(0.001)  //> res8: Long = 1000902
```

### countByValue
```scala
def countByValue()(implicit ord: Ordering[T] = null): Map[T, Long]
```
Return the count of each unique value in this RDD as a local map of (value, count) pairs.

```scala
val b = sc.parallelize(List(1,2,3,4,5,6,7,8,2,4,2,1,1,1,1,1))
b.countByValue
//> res23: scala.collection.Map[Int,Long] = Map(5 -> 1, 1 -> 6, 6 -> 1, 2 -> 3, 7 -> 1, 3 -> 1, 8 -> 1, 4 -> 2)
```

### countByValueApprox
```scala
def countByValueApprox(timeout: Long, confidence: Double = 0.95)(implicit ord: Ordering[T] = null): PartialResult[Map[T, BoundedDouble]]
```
Approximate version of countByValue().

### dependencies
```scala
final def dependencies: Seq[Dependency[_]]
```
Get the list of dependencies of this RDD, taking into account whether the RDD is checkpointed or not.

```scala
val a = sc.parallelize(1 to 3)
val b = sc.parallelize("a" to "c")

val c = a.map(_+1)
c.dependencies.length     //> res38: Int = 1

val d = a.cartesian(b)
d.dependencies.length     //> res39: Int = 2
```

### distinct
```scala
def distinct(): RDD[T]
```
Return a new RDD containing the distinct elements in this RDD.

```scala
val a = sc.parallelize(List(1,2,2,3,3,3,4,4,4,4,5,5,5,5,5))
a.count             //> res40: Long = 15
a.distinct.count    //> res41: Long = 5
```

### first
```scala
def first(): T
```
Return the first element in this RDD.

```scala
val a = sc.parallelize(1 to 10, 4)
a.first             //> res44: Int = 1
```

### filter
```scala
def filter(f: (T) ⇒ Boolean): RDD[T]
```
Return a new RDD containing only the elements that satisfy a predicate.

```scala
val a = sc.parallelize(1 to 10, 4)
a.filter(_ % 2 == 0).collect    //> res45: Array[Int] = Array(2, 4, 6, 8, 10)
```

### flatMap
```scala
def flatMap[U](f: (T) ⇒ TraversableOnce[U])(implicit arg0: ClassTag[U]): RDD[U]
```
Return a new RDD by first applying a function to all elements of this RDD, and then flattening the results.

```scala
val a = sc.parallelize(1 to 9)
val b = a.flatMap{ n => (1 to 9).map(n*_) }
b.collect //> res47: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 2, 4, 6, 8, 10, 12, 14, 16, 18, 3, 6, 9, 12, 15, 18, 21, 24, 27, 4, 8, 12, 16, 20, 24, 28, 32, 36, 5, 10, 15, 20, 25, 30, 35, 40, 45, 6, 12, 18, 24, 30, 36, 42, 48, 54, 7, 14, 21, 28, 35, 42, 49, 56, 63, 8, 16, 24, 32, 40, 48, 56, 64, 72, 9, 18, 27, 36, 45, 54, 63, 72, 81)
```

### fold
```scala
def fold(zeroValue: T)(op: (T, T) ⇒ T): T
```
Aggregate the elements of each partition, and then the results for all the partitions, using a given associative function and a neutral "zero value".

```scala
val a = sc.parallelize(1 to 10)
a.fold(0)(_+_)   //> res52: Int = 55
```
### foreach
```scala
def foreach(f: (T) ⇒ Unit): Unit
```
Applies a function f to all elements of this RDD.

```scala
val a = sc.parallelize(1 to 3)
a.foreach(n => println(n*n))
//> 1
//> 4
//> 9
```

### foreachPartition
```scala
def foreachPartition(f: (Iterator[T]) ⇒ Unit): Unit
```
Applies a function f to each partition of this RDD.

```scala
val a = sc.parallelize(1 to 9, 3)
a.foreachPartition{ iter => println(iter.toArray.mkString(",")) }
//> 1,2,3
//> 4,5,6
//> 7,8,9
```

### getCheckpointFile
```scala
def getCheckpointFile: Option[String]
```
Gets the name of the directory to which this RDD was checkpointed.

```scala
sc.setCheckpointDir("my_directory_name")

val a = sc.parallelize(1 to 500, 5)
val b = a ++ a ++ a ++ a ++ a

b.getCheckpointFile //> res56: Option[String] = None
b.checkpoint
b.getCheckpointFile //> res58: Option[String] = None
b.collect           //> res59: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176,...
b.getCheckpointFile //> res60: Option[String] = Some(file:/home/hduser/my_directory_name/7e3ea3bb-dc5b-485b-b95f-6add102852a5/rdd-46)
```
> [Spark容错机制](http://www.jianshu.com/p/99ebcc7c92d3)：检查点（本质是通过将RDD写入Disk做检查点）是为了通过lineage做容错的辅助，lineage过长会造成容错成本过高，这样就不如在中间阶段做检查点容错，如果之后有节点出现问题而丢失分区，从做检查点的RDD开始重做Lineage，就会减少开销。

### preferredLocations
### getStorageLevel
### glom
### groupBy
### id
### intersection
### isCheckpointed
### iterator
### keyBy
### map
### mapPartitions
### mapPartitionsWithContext
### mapPartitionsWithIndex
### mapPartitionsWithSplit
### mapWith
### max
### min
### name
### setName
### partitioner
### partitions
### persist
### cache
### pipe
### randomSplit
### reduce
### repartition
### sample
### saveAsObjectFile
### saveAsTextFile
### sortBy
### subtract
### take
### takeOrdered
### takeSample
### treeAggregate
### treeReduce
### toDebugString
### toJavaRDD
### toLocalIterator
### top
### toString
### union
### ++
### unpersist
### zip
### zipPartitions
### zipWithIndex
### zipWithUniquId

## [PairRDDFunctions](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.PairRDDFunctions)
### aggregateByKey
### cogroup
### groupWith
### collectAsMap
### combineByKey
### countApproxDistinctByKey
### countByKey
### countByKeyApprox
### flatMapValues
### foldByKey
### fullOuterJoin
### groupByKey
### join
### keys
### leftOuterJoin
### lookup
### mapValues
### partitionBy
### reduceByKey
### reduceByKeyLocally
### reduceByKeyToDriver
### rightOuterJoin
### sampleByKey
### sampleByKeyExact
### saveAsHodoopFile
### saveAsHadoopDataset
### saveAsNewAPIHadoopFile
### subtractByKey
### values

## [DoubleRDDFunctions](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.DoubleRDDFunctions)
### mean
### meanApprox
### histogram
### stats
### stdev
### sampleStdev
### sum
### sumApprox[Double]
### variance
### sampleVariance

## [OrderedRDDFunctions](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.OrderedRDDFunctions)
### filterByRange
### repartitionAndSortWithPartitions
### sortByKey

## [SequenceFileRDDFunctions](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.SequenceFileRDDFunctions)
###saveAsSequenceFile
