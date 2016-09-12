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
### countByValueApprox
### dependencies
### distinct
### first
### filter
### filterWith
### flatMap
### flatMapWith
### fold
### foreach
### foreachPartition
### foreachWith
### generator
### setGenerator
### getCheckpointFile
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
