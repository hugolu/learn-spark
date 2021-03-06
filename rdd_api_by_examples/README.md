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

### getStorageLevel
```scala
def getStorageLevel: StorageLevel
```
Get the RDD's current storage level, or StorageLevel.NONE if none is set.

```scala
val a = sc.parallelize(1 to 10)
a.getStorageLevel.description   //> res77: String = Serialized 1x Replicated
a.cache
a.getStorageLevel.description   //> res79: String = Memory Deserialized 1x Replicated
a.unpersist(true)
a.getStorageLevel.description   //> res86: String = Serialized 1x Replicated
a.persist(org.apache.spark.storage.StorageLevel.DISK_ONLY)
a.getStorageLevel.description   //> res88: String = Disk Serialized 1x Replicated
```

### glom
```scala
def glom(): RDD[Array[T]]
```
Return an RDD created by coalescing all elements within each partition into an array.

```scala
val a = sc.parallelize(1 to 10, 3)
a.glom.collect
//> res90: Array[Array[Int]] = Array(Array(1, 2, 3), Array(4, 5, 6), Array(7, 8, 9, 10))
```

### groupBy
```scala
def groupBy[K](f: (T) ⇒ K, p: Partitioner)(implicit kt: ClassTag[K], ord: Ordering[K] = null): RDD[(K, Iterable[T])]
def groupBy[K](f: (T) ⇒ K, numPartitions: Int)(implicit kt: ClassTag[K]): RDD[(K, Iterable[T])]
def groupBy[K](f: (T) ⇒ K)(implicit kt: ClassTag[K]): RDD[(K, Iterable[T])]
```
Return an RDD of grouped items.

```scala
val a = sc.parallelize(1 to 10)
a.groupBy(_%2).collect    //> res92: Array[(Int, Iterable[Int])] = Array((0,CompactBuffer(2, 4, 6, 8, 10)), (1,CompactBuffer(1, 3, 5, 7, 9)))
a.groupBy(_%5).collect    //> res94: Array[(Int, Iterable[Int])] = Array((0,CompactBuffer(5, 10)), (3,CompactBuffer(3, 8)), (4,CompactBuffer(4, 9)), (1,CompactBuffer(1, 6)), (2,CompactBuffer(2, 7)))
```

### id
```scala
val id: Int
```
A unique ID for this RDD (within its SparkContext).

```scala
val a = sc.parallelize(1 to 3)  //> a: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[66] at parallelize at <console>:24
a.id                            //> res99: Int = 66
```

### intersection
```scala
def intersection(other: RDD[T]): RDD[T]
```
Return the intersection of this RDD and another one.

```scala
val a = sc.parallelize(1 to 7)
val b = sc.parallelize(3 to 10)
a.intersection(b).collect   //> res101: Array[Int] = Array(4, 6, 3, 7, 5)
```

### isCheckpointed
```scala
def isCheckpointed: Boolean
```
Return whether this RDD is checkpointed and materialized, either reliably or locally.

```scala
val a = sc.parallelize(1 to 3)
a.isCheckpointed    //> res134: Boolean = false
a.checkpoint
a.isCheckpointed    //> res136: Boolean = false
a.collect           //> res137: Array[Int] = Array(1, 2, 3)
a.isCheckpointed    //> res138: Boolean = true
```

### keyBy
```scala
def keyBy[K](f: (T) ⇒ K): RDD[(K, T)]
```
Creates tuples of the elements in this RDD by applying f.

```scala
val a = sc.parallelize(1 to 10)

a.keyBy(_%3).collect            //> res140: Array[(Int, Int)] = Array((1,1), (2,2), (0,3), (1,4), (2,5), (0,6), (1,7), (2,8), (0,9), (1,10))
a.keyBy(_%3).groupByKey.collect //> res142: Array[(Int, Iterable[Int])] = Array((0,CompactBuffer(3, 6, 9)), (1,CompactBuffer(1, 4, 7, 10)), (2,CompactBuffer(2, 5, 8)))
```

### map
```scala
def map[U](f: (T) ⇒ U)(implicit arg0: ClassTag[U]): RDD[U]
```
Return a new RDD by applying a function to all elements of this RDD.

```scala
val a = sc.parallelize(1 to 5)
a.map(n=>n*n).collect //> res144: Array[Int] = Array(1, 4, 9, 16, 25)
```

### mapPartitions
```scala
def mapPartitions[U](f: (Iterator[T]) ⇒ Iterator[U], preservesPartitioning: Boolean = false)(implicit arg0: ClassTag[U]): RDD[U]
```
Return a new RDD by applying a function to each partition of this RDD.

```scala
val a = sc.parallelize(1 to 10, 5)
a.mapPartitions(iter => iter.map(n=>n*n))
```

### mapPartitionsWithIndex
```scala
def mapPartitionsWithIndex[U](f: (Int, Iterator[T]) ⇒ Iterator[U], preservesPartitioning: Boolean = false)(implicit arg0: ClassTag[U]): RDD[U]
```
Return a new RDD by applying a function to each partition of this RDD, while tracking the index of the original partition.

```scala
val a = sc.parallelize(1 to 10, 5)
val b = a.mapPartitionsWithIndex((idx, iter) => iter.map((idx, _)))
b.collect
//> res155: Array[(Int, Int)] = Array((0,1), (0,2), (1,3), (1,4), (2,5), (2,6), (3,7), (3,8), (4,9), (4,10))
```

### max, min
```scala
def max()(implicit ord: Ordering[T]): T
def min()(implicit ord: Ordering[T]): T
```
Returns the max/min of this RDD as defined by the implicit Ordering[T].

```scala
val a = sc.parallelize(1 to 10, 5)
a.max   //> res156: Int = 10
a.min   //> res157: Int = 1
```

### name
```scala
var name: String
```
A friendly name for this RDD

```scala
def setName(_name: String): RDD.this.type
```
Assign a name to this RDD

```scala
val a = sc.parallelize(1 to 10, 5)
a.name                    //> res158: String = null
a.setName("one to ten")   //> res159: a.type = one to ten ParallelCollectionRDD[98] at parallelize at <console>:24
a.name                    //> res160: String = one to ten
```

### partitioner
```scala
val partitioner: Option[Partitioner]
```
Optionally overridden by subclasses to specify how they are partitioned.

### partitions
```scala
final def partitions: Array[Partition]
```
Get the array of partitions of this RDD, taking into account whether the RDD is checkpointed or not.

```scala
val a = sc.parallelize(1 to 10, 5)
a.partitions
//> res163: Array[org.apache.spark.Partition] = Array(org.apache.spark.rdd.ParallelCollectionPartition@1643, org.apache.spark.rdd.ParallelCollectionPartition@1644, org.apache.spark.rdd.ParallelCollectionPartition@1645, org.apache.spark.rdd.ParallelCollectionPartition@1646, org.apache.spark.rdd.ParallelCollectionPartition@1647)
```

### persist, cache
```scala
def persist(): RDD.this.type
def cache(): RDD.this.type
```
Persist this RDD with the default storage level (MEMORY_ONLY).

```scala
def persist(newLevel: StorageLevel): RDD.this.type
```
Set this RDD's storage level to persist its values across operations after the first time it is computed.

```scala
val c = sc.parallelize(List("Gnu", "Cat", "Rat", "Dog", "Gnu", "Rat"), 2)
c.getStorageLevel     //> res164: org.apache.spark.storage.StorageLevel = StorageLevel(1 replicas)
c.cache
c.getStorageLevel     //> res166: org.apache.spark.storage.StorageLevel = StorageLevel(memory, deserialized, 1 replicas)
```

### pipe
```scala
def pipe(command: Seq[String], env: Map[String, String] = Map(), printPipeContext: ((String) ⇒ Unit) ⇒ Unit = null, printRDDElement: (T, (String) ⇒ Unit) ⇒ Unit = null, separateWorkingDir: Boolean = false, bufferSize: Int = 8192, encoding: String = Codec.defaultCharsetCodec.name): RDD[String]
def pipe(command: String, env: Map[String, String]): RDD[String]
def pipe(command: String): RDD[String]
```
Return an RDD created by piping elements to a forked external process.

```scala
val a = sc.parallelize(1 to 9, 3)
a.pipe("head -n 1").collect   //> res167: Array[String] = Array(1, 4, 7)
a.pipe("wc -l").collect       //> res168: Array[String] = Array(3, 3, 3)
```

### randomSplit
```scala
def randomSplit(weights: Array[Double], seed: Long = Utils.random.nextLong): Array[RDD[T]]
```
Randomly splits this RDD with the provided weights.
>  Note the actual size of each smaller RDD is only approximately equal to the percentages specified by the weights Array. 

```scala
val a = sc.parallelize(1 to 10)
val b = a.randomSplit(Array(0.7, 0.3), 42)
b.foreach(rdd => println(rdd.collect.mkString(",")))
//> 1,5,6,7,8,9,10
//> 2,3,4
```

### reduce
```scala
def reduce(f: (T, T) ⇒ T): T
```
Reduces the elements of this RDD using the specified commutative and associative binary operator.

```scala
val a = sc.parallelize(1 to 10)
a.reduce(_+_)   //> res181: Int = 55
```

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

### sample
```scala
def sample(withReplacement: Boolean, fraction: Double, seed: Long = Utils.random.nextLong): RDD[T]
```
Return a sampled subset of this RDD.

> withReplacement: can elements be sampled multiple times (replaced when sampled out)

```scala
val a = sc.parallelize(1 to 10)

a.sample(true, 1, 1).collect      //> res202: Array[Int] = Array(1, 1, 2, 3, 3, 4, 6, 8, 8, 10)
a.sample(false, 1, 1).collect     //> res203: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

a.sample(true, 0.6, 11).collect   //> res216: Array[Int] = Array(4, 4, 9, 9)
a.sample(false, 0.6, 11).collect  //> res217: Array[Int] = Array(1, 5, 6, 7, 8, 9, 10)
```

### saveAsObjectFile
```scala
def
saveAsObjectFile(path: String): Unit
```
Save this RDD as a SequenceFile of serialized objects.

```scala
val a = sc.parallelize(1 to 10)       //> a: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[160] at parallelize at <console>:24
a.saveAsObjectFile("objFile")
val b = sc.objectFile[Int]("objFile") //> b: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[164] at objectFile at <console>:24
b.collect                             //> res226: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
```

### saveAsTextFile
```scala
def saveAsTextFile(path: String, codec: Class[_ <: CompressionCodec]): Unit
```
Save this RDD as a compressed text file, using string representations of elements.

```scala
saveAsTextFile(path: String): Unit
```
Save this RDD as a text file, using string representations of elements.

```scala
val a = sc.parallelize(1 to 10)     //> a: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[160] at parallelize at <console>:24
a.saveAsTextFile("textFile")
val val b = sc.textFile("textFile") //> b: org.apache.spark.rdd.RDD[String] = textFile MapPartitionsRDD[167] at textFile at <console>:24
b.collect                           //> res228: Array[String] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
```

### sortBy
```scala
def sortBy[K](f: (T) ⇒ K, ascending: Boolean = true, numPartitions: Int = this.partitions.length)(implicit ord: Ordering[K], ctag: ClassTag[K]): RDD[T]
```
Return this RDD sorted by the given key function.

```scala
val a = sc.parallelize(List(1,3,5,7,9,8,6,4,2,0))
a.sortBy(n => n, true).collect  //> res233: Array[Int] = Array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
a.sortBy(n => n, false).collect //> res234: Array[Int] = Array(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
```

```scala
val a = sc.parallelize(List("apple","banana","cherry","date","elderberry"))
a.sortBy(n => n, true).collect        //> res238: Array[String] = Array(apple, banana, cherry, date, elderberry)
a.sortBy(n => n.length, true).collect //> res239: Array[String] = Array(date, apple, banana, cherry, elderberry)
```

```scala
case class Box(n: Int)
val a = sc.parallelize(List(Box(2),Box(5),Box(1),Box(4)))

implicit val sortingBox = new Ordering[Box] {
 override def compare(a: Box, b: Box) = a.n.compare(b.n)
}

a.sortBy(n=>n).collect
//> res133: Array[Box] = Array(Box(1), Box(2), Box(4), Box(5))
```

### subtract
```scala
def subtract(other: RDD[T], p: Partitioner)(implicit ord: Ordering[T] = null): RDD[T]
def subtract(other: RDD[T], numPartitions: Int): RDD[T]
def subtract(other: RDD[T]): RDD[T]
```
Return an RDD with the elements from `this` that are not in `other`.

```scala
val a = sc.parallelize(1 to 7)
val b = sc.parallelize(5 to 10)
a.subtract(b).collect   //> res240: Array[Int] = Array(1, 2, 3, 4)
```

### take
```scala
def take(num: Int): Array[T]
```
Take the first num elements of the RDD.

```scala
val a = sc.parallelize(List(2,4,6,8,7,5,3,1))
a.take(3)         //> res242: Array[Int] = Array(2, 4, 6)
```

### takeOrdered
```scala
def takeOrdered(num: Int)(implicit ord: Ordering[T]): Array[T]
```
Returns the first k (smallest) elements from this RDD as defined by the specified implicit Ordering[T] and maintains the ordering.

```scala
val a = sc.parallelize(List(2,4,6,8,7,5,3,1))
a.takeOrdered(3)  //> res243: Array[Int] = Array(1, 2, 3)
```

### takeSample
```scala
def takeSample(withReplacement: Boolean, num: Int, seed: Long = Utils.random.nextLong): Array[T]
```
Return a fixed-size sampled subset of this RDD in an array

```scala
val a = sc.parallelize(List(2,4,6,8,7,5,3,1))
a.takeSample(false, 3)  //> res245: Array[Int] = Array(7, 8, 1)
```

### treeAggregate
```scala
def treeAggregate[U](zeroValue: U)(seqOp: (U, T) ⇒ U, combOp: (U, U) ⇒ U, depth: Int = 2)(implicit arg0: ClassTag[U]): U
```
Aggregates the elements of this RDD in a multi-level tree pattern.

### treeReduce
```scala
def treeReduce(f: (T, T) ⇒ T, depth: Int = 2): T
```
Reduces the elements of this RDD in a multi-level tree pattern.

### toDebugString
```scala
def toDebugString: String
```
A description of this RDD and its recursive dependencies for debugging.

```scala
val a = sc.parallelize(1 to 9, 3)
val b = sc.parallelize(1 to 3, 3)
val c = a.subtract(b)
c.toDebugString
res246: String =
(3) MapPartitionsRDD[210] at subtract at <console>:28 []
 |  SubtractedRDD[209] at subtract at <console>:28 []
 +-(3) MapPartitionsRDD[207] at subtract at <console>:28 []
 |  |  ParallelCollectionRDD[205] at parallelize at <console>:24 []
 +-(3) MapPartitionsRDD[208] at subtract at <console>:28 []
    |  ParallelCollectionRDD[206] at parallelize at <console>:24 []
```

### toJavaRDD
```scala
def toJavaRDD(): JavaRDD[T]
```

```scala
val a = sc.parallelize(1 to 10)
a.toJavaRDD //> res247: org.apache.spark.api.java.JavaRDD[Int] = ParallelCollectionRDD[211] at parallelize at <console>:24
```

### toLocalIterator
```scala
def toLocalIterator: Iterator[T]
```
Return an iterator that contains all of the elements in this RDD.
 
```scala
val a = sc.parallelize(1 to 10, 2)
val iter = a.toLocalIterator
iter.next //> res248: Int = 1
iter.next //> res249: Int = 2
```

### top
```scala
def top(num: Int)(implicit ord: Ordering[T]): Array[T]
```
Returns the top k (largest) elements from this RDD as defined by the specified implicit Ordering[T] and maintains the ordering.

```scala
val a = sc.parallelize(List(1,3,5,7,9,8,6,4,2,0))
a.top(3)  //> res252: Array[Int] = Array(9, 8, 7)
```

### toString
```scala
def toString(): String
```

```scala
val a = sc.parallelize(List(1,3,5,7,9,8,6,4,2,0)) //> a: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[215] at parallelize at <console>:24
a.toString                                        //> res253: String = ParallelCollectionRDD[215] at parallelize at <console>:24
```

### union
```scala
def union(other: RDD[T]): RDD[T]
```
Return the union of this RDD and another one.

```scala
val a = sc.parallelize(1 to 4)
val b = sc.parallelize(3 to 6)
a.union(b).collect    //> res255: Array[Int] = Array(1, 2, 3, 4, 3, 4, 5, 6)
```

### ++
```scala
def ++(other: RDD[T]): RDD[T]
```
Return the union of this RDD and another one.

```scala
val a = sc.parallelize(1 to 4)
val b = sc.parallelize(3 to 6)
(a ++ b).collect      //> res256: Array[Int] = Array(1, 2, 3, 4, 3, 4, 5, 6)
```

### unpersist
```scala
def unpersist(blocking: Boolean = true): RDD.this.type
```
Mark the RDD as non-persistent, and remove all blocks for it from memory and disk.

### zip
```scala
def zip[U](other: RDD[U])(implicit arg0: ClassTag[U]): RDD[(T, U)]
```
Zips this RDD with another one, returning key-value pairs with the first element in each RDD, second element in each RDD, etc.

```scala
val a = sc.parallelize(1 to 4)
val b = sc.parallelize(3 to 6)
a.zip(b).collect  //> res258: Array[(Int, Int)] = Array((1,3), (2,4), (3,5), (4,6))
```

### zipPartitions
```scala
def zipPartitions[B, C, D, V](rdd2: RDD[B], rdd3: RDD[C], rdd4: RDD[D])(f: (Iterator[T], Iterator[B], Iterator[C], Iterator[D]) ⇒ Iterator[V])(implicit arg0: ClassTag[B], arg1: ClassTag[C], arg2: ClassTag[D], arg3: ClassTag[V]): RDD[V]
def zipPartitions[B, C, D, V](rdd2: RDD[B], rdd3: RDD[C], rdd4: RDD[D], preservesPartitioning: Boolean)(f: (Iterator[T], Iterator[B], Iterator[C], Iterator[D]) ⇒ Iterator[V])(implicit arg0: ClassTag[B], arg1: ClassTag[C], arg2: ClassTag[D], arg3: ClassTag[V]): RDD[V]
def zipPartitions[B, C, V](rdd2: RDD[B], rdd3: RDD[C])(f: (Iterator[T], Iterator[B], Iterator[C]) ⇒ Iterator[V])(implicit arg0: ClassTag[B], arg1: ClassTag[C], arg2: ClassTag[V]): RDD[V]
def zipPartitions[B, C, V](rdd2: RDD[B], rdd3: RDD[C], preservesPartitioning: Boolean)(f: (Iterator[T], Iterator[B], Iterator[C]) ⇒ Iterator[V])(implicit arg0: ClassTag[B], arg1: ClassTag[C], arg2: ClassTag[V]): RDD[V]
def zipPartitions[B, V](rdd2: RDD[B])(f: (Iterator[T], Iterator[B]) ⇒ Iterator[V])(implicit arg0: ClassTag[B], arg1: ClassTag[V]): RDD[V]
def zipPartitions[B, V](rdd2: RDD[B], preservesPartitioning: Boolean)(f: (Iterator[T], Iterator[B]) ⇒ Iterator[V])(implicit arg0: ClassTag[B], arg1: ClassTag[V]): RDD[V]
```
Zip this RDD's partitions with one (or more) RDD(s) and return a new RDD by applying a function to the zipped partitions.

> 什麼時候會用到？

### zipWithIndex
```scala
def zipWithIndex(): RDD[(T, Long)]
```
Zips this RDD with its element indices.

```scala
val a = sc.parallelize(List("apple","banana","cherry","data","elderberry"))
a.zipWithIndex.collect  //> res259: Array[(String, Long)] = Array((apple,0), (banana,1), (cherry,2), (data,3), (elderberry,4))
```

### zipWithUniquId
```scala
def zipWithUniqueId(): RDD[(T, Long)]
```
Zips this RDD with generated unique Long ids.

```scala
val a = sc.parallelize(List("apple","banana","cherry","data","elderberry"))
a.zipWithUniqueId.collect //> res261: Array[(String, Long)] = Array((apple,0), (banana,1), (cherry,2), (data,3), (elderberry,4))
```

## [PairRDDFunctions](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.PairRDDFunctions)

### aggregateByKey
```scala
def aggregateByKey[U](zeroValue: U)(seqOp: (U, V) ⇒ U, combOp: (U, U) ⇒ U)(implicit arg0: ClassTag[U]): RDD[(K, U)]
def aggregateByKey[U](zeroValue: U, numPartitions: Int)(seqOp: (U, V) ⇒ U, combOp: (U, U) ⇒ U)(implicit arg0: ClassTag[U]): RDD[(K, U)]
def aggregateByKey[U](zeroValue: U, partitioner: Partitioner)(seqOp: (U, V) ⇒ U, combOp: (U, U) ⇒ U)(implicit arg0: ClassTag[U]): RDD[(K, U)]
```
Aggregate the values of each key, using given combine functions and a neutral "zero value".

```scala
val pairRDD = sc.parallelize(List( ("cat",2), ("cat", 5), ("mouse", 4),("cat", 12), ("dog", 12), ("mouse", 2)), 2)

pairRDD.aggregateByKey(0)(_+_, _+_).collect
//> res3: Array[(String, Int)] = Array((dog,12), (cat,19), (mouse,6))

pairRDD.aggregateByKey(0)(math.max(_,_), math.max(_,_)).collect
//> res5: Array[(String, Int)] = Array((dog,12), (cat,12), (mouse,4))
```

```scala
val a = sc.parallelize(List(("A", 100), ("B", 150), ("A", 200), ("C", 50), ("B", 50)))

val result = a.aggregateByKey((0,0))((acc: (Int, Int), v) => (acc._1 + v, acc._2 + 1), (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2)).map{case (key: String, value: (Int, Int)) => (key, value._1 / value._2.toDouble) }
//> result: org.apache.spark.rdd.RDD[(String, Double)] = MapPartitionsRDD[61] at map at <console>:28

result.collect
//> res45: Array[(String, Double)] = Array((B,100.0), (A,150.0), (C,50.0))
```

### cogroup
```scala
def cogroup[W1, W2, W3](other1: RDD[(K, W1)], other2: RDD[(K, W2)], other3: RDD[(K, W3)], numPartitions: Int): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2], Iterable[W3]))]
def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)], numPartitions: Int): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2]))]
def cogroup[W](other: RDD[(K, W)], numPartitions: Int): RDD[(K, (Iterable[V], Iterable[W]))]
def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)]): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2]))]
def cogroup[W](other: RDD[(K, W)]): RDD[(K, (Iterable[V], Iterable[W]))]
def cogroup[W1, W2, W3](other1: RDD[(K, W1)], other2: RDD[(K, W2)], other3: RDD[(K, W3)]): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2], Iterable[W3]))]
def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)], partitioner: Partitioner): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2]))]
def cogroup[W](other: RDD[(K, W)], partitioner: Partitioner): RDD[(K, (Iterable[V], Iterable[W]))]
def cogroup[W1, W2, W3](other1: RDD[(K, W1)], other2: RDD[(K, W2)], other3: RDD[(K, W3)], partitioner: Partitioner): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2], Iterable[W3]))]
```
For each key k in this or other1 or other2 or other3, return a resulting RDD that contains a tuple with the list of values for that key in this, other1, other2 and other3.

```scala
val a = sc.parallelize(List(1, 2, 1, 3), 1)
val b = a.map((_,"b"))
val c = a.map((_,"c"))
val d = a.map((_,"d"))

b.cogroup(c).collect
//> res11: Array[(Int, (Iterable[String], Iterable[String]))] = Array((1,(CompactBuffer(b, b),CompactBuffer(c, c))), (3,(CompactBuffer(b),CompactBuffer(c))), (2,(CompactBuffer(b),CompactBuffer(c))))

b.cogroup(c,d).collect
//> res12: Array[(Int, (Iterable[String], Iterable[String], Iterable[String]))] = Array((1,(CompactBuffer(b, b),CompactBuffer(c, c),CompactBuffer(d, d))), (3,(CompactBuffer(b),CompactBuffer(c),CompactBuffer(d))), (2,(CompactBuffer(b),CompactBuffer(c),CompactBuffer(d))))
```

### groupWith
```scala
def groupWith[W1, W2, W3](other1: RDD[(K, W1)], other2: RDD[(K, W2)], other3: RDD[(K, W3)]): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2], Iterable[W3]))]
def groupWith[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)]): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2]))]
def groupWith[W](other: RDD[(K, W)]): RDD[(K, (Iterable[V], Iterable[W]))]
```
Alias for cogroup.

```scala
val a = sc.parallelize(List(1, 2, 1, 3), 1)
val b = a.map((_,"b"))
val c = a.map((_,"c"))
val d = a.map((_,"d"))

b.groupWith(c).collect
//> res13: Array[(Int, (Iterable[String], Iterable[String]))] = Array((1,(CompactBuffer(b, b),CompactBuffer(c, c))), (3,(CompactBuffer(b),CompactBuffer(c))), (2,(CompactBuffer(b),CompactBuffer(c))))

b.groupWith(c,d).collect
//> res14: Array[(Int, (Iterable[String], Iterable[String], Iterable[String]))] = Array((1,(CompactBuffer(b, b),CompactBuffer(c, c),CompactBuffer(d, d))), (3,(CompactBuffer(b),CompactBuffer(c),CompactBuffer(d))), (2,(CompactBuffer(b),CompactBuffer(c),CompactBuffer(d))))
```

### collectAsMap
```scala
def
collectAsMap(): Map[K, V]
```
Return the key-value pairs in this RDD to the master as a Map.

```scala
val a = sc.parallelize(List("apple","banana","cherry","date","elderberry"))

a.zipWithIndex.collect
//> res18: Array[(String, Long)] = Array((apple,0), (banana,1), (cherry,2), (date,3), (elderberry,4))

a.zipWithIndex.collectAsMap
//> res20: scala.collection.Map[String,Long] = Map(date -> 3, banana -> 1, elderberry -> 4, cherry -> 2, apple -> 0)
```

### combineByKey
```scala
def combineByKey[C](createCombiner: (V) ⇒ C, mergeValue: (C, V) ⇒ C, mergeCombiners: (C, C) ⇒ C): RDD[(K, C)]
def combineByKey[C](createCombiner: (V) ⇒ C, mergeValue: (C, V) ⇒ C, mergeCombiners: (C, C) ⇒ C, numPartitions: Int): RDD[(K, C)]
def combineByKey[C](createCombiner: (V) ⇒ C, mergeValue: (C, V) ⇒ C, mergeCombiners: (C, C) ⇒ C, partitioner: Partitioner, mapSideCombine: Boolean = true, serializer: Serializer = null): RDD[(K, C)]
```
使用不同返回類型，合併具有相同鍵的值
- 對於新元素，`createCombiner` 創建鍵值對應的累加器的初值。針對每個分區第一次出現各個鍵值時發生，不是整個 RDD 中第一次出現一個鍵時發生
- 如果這是一個在處理當前分區已經遇過的鍵值，使用 `mergeValue` 將該鍵的累加器對應當前的值與這個新的值進行合併
- 由於每個分區都是獨立處理，所以同一個鍵可以有多個累加器。有兩個或更多分區對應同一個鍵的累加器，需要使用用戶提供的 `mergeCombiners` 將個分區的結果進行合併

```scala
val a = sc.parallelize(List(("A", 100), ("B", 150), ("A", 200), ("C", 50), ("B", 50)))

val result = a.combineByKey((v) => (v, 1), (acc: (Int, Int), v) => (acc._1 + v, acc._2 + 1), (acc1: (Int, Int), acc2: (Int, Int)) => (acc1._1 + acc2._1, acc1._2 + acc2._2)).map{ case (key, value) => (key, value._1 / value._2.toDouble) }

result.collect //> res34: Array[(String, Double)] = Array((B,100.0), (A,150.0), (C,50.0))
```

### countApproxDistinctByKey
```scala
def countApproxDistinctByKey(relativeSD: Double = 0.05): RDD[(K, Long)]
def countApproxDistinctByKey(relativeSD: Double, numPartitions: Int): RDD[(K, Long)]
def countApproxDistinctByKey(relativeSD: Double, partitioner: Partitioner): RDD[(K, Long)]
def countApproxDistinctByKey(p: Int, sp: Int, partitioner: Partitioner): RDD[(K, Long)]
```
Return approximate number of distinct values for each key in this RDD.

> The parameter `relativeSD` controls the accuracy of the computation.

```scala
val a = sc.parallelize(List("apple","banana","cherry","date"))
val b = sc.parallelize(a.takeSample(true, 10000), 20)
val c = b.zipWithIndex

c.countApproxDistinctByKey(0.1).collect  //> res1: Array[(String, Long)] = Array((apple,2285), (banana,2854), (date,2375), (cherry,2459))
c.countApproxDistinctByKey(0.01).collect //> res2: Array[(String, Long)] = Array((apple,2420), (banana,2549), (date,2546), (cherry,2485))
```

### countByKey, countByKeyApprox
```scala
def countByKey(): Map[K, Long]
```
Count the number of elements for each key, collecting the results to a local Map.

```scala
def countByKeyApprox(timeout: Long, confidence: Double = 0.95): PartialResult[Map[K, BoundedDouble]]
```
Approximate version of countByKey that can return a partial result if it does not finish within a timeout.

```scala
val a = sc.parallelize(List("apple","banana","cherry","date","elderberry"))
val b = a.map(n => (n.length, n))
b.countByKey
//> res4: scala.collection.Map[Int,Long] = Map(4 -> 1, 6 -> 2, 10 -> 1, 5 -> 1)
```

### flatMapValues
```scala
def flatMapValues[U](f: (V) ⇒ TraversableOnce[U]): RDD[(K, U)]
```
Pass each value in the key-value pair RDD through a flatMap function without changing the keys; this also retains the original RDD's partitioning.

```scala
val a = sc.parallelize(List("apple","banana","cherry"))
val b = a.map(n => (n, n.length))

b.collect
//> res29: Array[(String, Int)] = Array((apple,5), (banana,6), (cherry,6))

b.flatMapValues(n => 1 to n).collect
//> res30: Array[(String, Int)] = Array((apple,1), (apple,2), (apple,3), (apple,4), (apple,5), (banana,1), (banana,2), (banana,3), (banana,4), (banana,5), (banana,6), (cherry,1), (cherry,2), (cherry,3), (cherry,4), (cherry,5), (cherry,6))
```

### foldByKey
```scala
def foldByKey(zeroValue: V)(func: (V, V) ⇒ V): RDD[(K, V)]
def foldByKey(zeroValue: V, numPartitions: Int)(func: (V, V) ⇒ V): RDD[(K, V)]
def foldByKey(zeroValue: V, partitioner: Partitioner)(func: (V, V) ⇒ V): RDD[(K, V)]
```
Merge the values for each key using an associative function and a neutral "zero value" which may be added to the result an arbitrary number of times, and must not change the result (e.g., Nil for list concatenation, 0 for addition, or 1 for multiplication.).

```scala
val a = sc.parallelize(List(("A", 100), ("B", 150), ("A", 200), ("C", 50), ("B", 50)))
a.foldByKey(0)(_+_).collect      //> res34: Array[(String, Int)] = Array((B,200), (A,300), (C,50))
a.foldByKey(0)(math.max).collect //> res36: Array[(String, Int)] = Array((B,150), (A,200), (C,50))
```

### groupByKey
```scala
def groupByKey(): RDD[(K, Iterable[V])]
def groupByKey(numPartitions: Int): RDD[(K, Iterable[V])]
def groupByKey(partitioner: Partitioner): RDD[(K, Iterable[V])]
```
Group the values for each key in the RDD into a single sequence.

```scala
val a = sc.parallelize(List("apple","banana","cherry","date","elderberry"))
val b = a.keyBy(_.length)

b.groupByKey.collect
//> res39: Array[(Int, Iterable[String])] = Array((4,CompactBuffer(date)), (6,CompactBuffer(banana, cherry)), (10,CompactBuffer(elderberry)), (5,CompactBuffer(apple)))
```

### keys
```scala
def keys: RDD[K]
```
Return an RDD with the keys of each tuple.

```scala
val a = sc.parallelize(List("apple","banana","cherry","date","elderberry"))
val b = a.keyBy(_.length)
b.keys.collect
//> res50: Array[Int] = Array(5, 6, 6, 4, 10)
```

### join, leftOuterJoin, rightOuterJoin, fullOuterJoin

```scala
def join[W](other: RDD[(K, W)], numPartitions: Int): RDD[(K, (V, W))]
def join[W](other: RDD[(K, W)]): RDD[(K, (V, W))]
def join[W](other: RDD[(K, W)], partitioner: Partitioner): RDD[(K, (V, W))]
```
Return an RDD containing all pairs of elements with matching keys in this and other.

```scala
def leftOuterJoin[W](other: RDD[(K, W)], numPartitions: Int): RDD[(K, (V, Option[W]))]
def leftOuterJoin[W](other: RDD[(K, W)]): RDD[(K, (V, Option[W]))]
def leftOuterJoin[W](other: RDD[(K, W)], partitioner: Partitioner): RDD[(K, (V, Option[W]))]
```
Perform a left outer join of this and other.

```scala
def rightOuterJoin[W](other: RDD[(K, W)], numPartitions: Int): RDD[(K, (Option[V], W))]
def rightOuterJoin[W](other: RDD[(K, W)]): RDD[(K, (Option[V], W))]
def rightOuterJoin[W](other: RDD[(K, W)], partitioner: Partitioner): RDD[(K, (Option[V], W))]
```
Perform a right outer join of this and other.

```scala
def fullOuterJoin[W](other: RDD[(K, W)], numPartitions: Int): RDD[(K, (Option[V], Option[W]))]
def fullOuterJoin[W](other: RDD[(K, W)]): RDD[(K, (Option[V], Option[W]))]
def fullOuterJoin[W](other: RDD[(K, W)], partitioner: Partitioner): RDD[(K, (Option[V], Option[W]))]
```
Perform a full outer join of this and other.

```scala
val a = sc.parallelize(List(("A",1),("B",2),("B",3),("C",4)))
val b = sc.parallelize(List(("B",5),("C",6),("C",7),("D",8)))

a.join(b).collect
//> res122: Array[(String, (Int, Int))] = Array((B,(2,5)), (B,(3,5)), (C,(4,6)), (C,(4,7)))

a.leftOuterJoin(b).collect
//> res123: Array[(String, (Int, Option[Int]))] = Array((B,(2,Some(5))), (B,(3,Some(5))), (A,(1,None)), (C,(4,Some(6))), (C,(4,Some(7))))

a.rightOuterJoin(b).collect
//> res124: Array[(String, (Option[Int], Int))] = Array((B,(Some(2),5)), (B,(Some(3),5)), (C,(Some(4),6)), (C,(Some(4),7)), (D,(None,8)))

a.fullOuterJoin(b).collect
//> res125: Array[(String, (Option[Int], Option[Int]))] = Array((B,(Some(2),Some(5))), (B,(Some(3),Some(5))), (A,(Some(1),None)), (C,(Some(4),Some(6))), (C,(Some(4),Some(7))), (D,(None,Some(8))))
```

### lookup
```scala
def lookup(key: K): Seq[V]
```
Return the list of values in the RDD for key key.

```scala
val a = sc.parallelize(List("dog", "tiger", "lion", "cat", "panther", "eagle"), 2)
val b = a.map(x => (x.length, x))
b.lookup(5)
//> res66: Seq[String] = WrappedArray(tiger, eagle)
```

### mapValues
```scala
def mapValues[U](f: (V) ⇒ U): RDD[(K, U)]
```
Pass each value in the key-value pair RDD through a map function without changing the keys; this also retains the original RDD's partitioning.

```scala
val a = sc.parallelize(List("dog", "tiger", "lion", "cat", "panther", "eagle"), 2)
val b = a.map(x => (x.length, x))
b.mapValues("x" + _ + "x").collect
//> res68: Array[(Int, String)] = Array((3,xdogx), (5,xtigerx), (4,xlionx), (3,xcatx), (7,xpantherx), (5,xeaglex))
```

### partitionBy
```scala
def partitionBy(partitioner: Partitioner): RDD[(K, V)]
```
Return a copy of the RDD partitioned using the specified partitioner.

### reduceByKey, reduceByKeyLocally, reduceByKeyToDriver
```scala
def reduceByKey(func: (V, V) ⇒ V): RDD[(K, V)]
def reduceByKey(func: (V, V) ⇒ V, numPartitions: Int): RDD[(K, V)]
def reduceByKey(partitioner: Partitioner, func: (V, V) ⇒ V): RDD[(K, V)]
```
Merge the values for each key using an associative and commutative reduce function.

```scala
def reduceByKeyLocally(func: (V, V) ⇒ V): Map[K, V]
```
Merge the values for each key using an associative and commutative reduce function, but return the results immediately to the master as a Map.

```scala
val a = sc.parallelize(List(("A", 100), ("B", 150), ("A", 200), ("C", 50), ("B", 50)))
a.reduceByKey(_+_).collect
//> res74: Array[(String, Int)] = Array((B,200), (A,300), (C,50))
```

### sampleByKey, sampleByKeyExact
```scala
def sampleByKey(withReplacement: Boolean, fractions: Map[K, Double], seed: Long = Utils.random.nextLong): RDD[(K, V)]
```
Return a subset of this RDD sampled by key (via stratified sampling).

```scala
def sampleByKeyExact(withReplacement: Boolean, fractions: Map[K, Double], seed: Long = Utils.random.nextLong): RDD[(K, V)]
```
Return a subset of this RDD sampled by key (via stratified sampling) containing exactly math.ceil(numItems * samplingRate) for each stratum (group of pairs with the same key).

```scala
val a =  sc.parallelize(List((0,0), (1,1), (2,2), (3,3), (0,4), (1,5), (2,6), (3,7), (0,8), (1,9), (2, 10), (3,11)))
a.lookup(0) //> res117: Seq[Int] = WrappedArray(0, 4, 8)
a.lookup(1) //> res118: Seq[Int] = WrappedArray(1, 5, 9)
a.lookup(2) //> res119: Seq[Int] = WrappedArray(2, 6, 10)
a.lookup(3) //> res120: Seq[Int] = WrappedArray(3, 7, 11)

val sampleMap = List((0, 0.4), (2, 0.6)).toMap
a.sampleByKey(false, sampleMap).collect

16/09/13 10:35:11 ERROR Executor: Exception in task 0.0 in stage 114.0 (TID 242)
java.util.NoSuchElementException: key not found: 1
```
> 不懂?!

### saveAsHodoopFile
### saveAsHadoopDataset
### saveAsNewAPIHadoopFile

### subtractByKey
```scala
def subtractByKey[W](other: RDD[(K, W)], p: Partitioner)(implicit arg0: ClassTag[W]): RDD[(K, V)]
def subtractByKey[W](other: RDD[(K, W)], numPartitions: Int)(implicit arg0: ClassTag[W]): RDD[(K, V)]
def subtractByKey[W](other: RDD[(K, W)])(implicit arg0: ClassTag[W]): RDD[(K, V)]
```
Return an RDD with the pairs from this whose keys are not in other.

```scala
val a = sc.parallelize(List(("A",1), ("B",2), ("C",3)))
val b = sc.parallelize(List(("B",4), ("C",5), ("D",6)))

a.subtractByKey(b).collect //> res93: Array[(String, Int)] = Array((A,1))
b.subtractByKey(a).collect //> res95: Array[(String, Int)] = Array((D,6))
```

### values
```scala
def values: RDD[V]
```
Return an RDD with the values of each tuple.

```scala
val a = sc.parallelize(List("apple","banana","cherry","date","elderberry"))
val b = a.keyBy(_.length)
b.values.collect
//> res70: Array[String] = Array(apple, banana, cherry, date, elderberry)
```

### aggregateByKey vs combineByKey
```scala
def aggregateByKey[U](zeroValue: U)(seqOp: (U, V) ⇒ U, combOp: (U, U) ⇒ U)(implicit arg0: ClassTag[U]): RDD[(K, U)]
def combineByKey[C](createCombiner: (V) ⇒ C, mergeValue: (C, V) ⇒ C, mergeCombiners: (C, C) ⇒ C): RDD[(K, C)]
```

```scala
val a = sc.parallelize(List(("A", 100), ("B", 150), ("A", 200), ("C", 50), ("B", 50)))

a.aggregateByKey((0, 0))((acc, v) => (acc._1 + v, acc._2 + 1), (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2)).map{ case (key, value) => (key, value._1 / value._2.toDouble) }.collect
//> res50: Array[(String, Double)] = Array((B,100.0), (A,150.0), (C,50.0))

a.combineByKey((v) => (v, 1), (acc: (Int, Int), v) => (acc._1 + v, acc._2 + 1), (acc1: (Int, Int), acc2: (Int, Int)) => (acc1._1 + acc2._1, acc1._2 + acc2._2)).map{ case (key, value) => (key, value._1 / value._2.toDouble) }.collect
//> res55: Array[(String, Double)] = Array((B,100.0), (A,150.0), (C,50.0))
```
> `combineByKey` 使用上比 `aggregateByKey` 更繁瑣，什麼情況非用不可呢？

## [DoubleRDDFunctions](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.DoubleRDDFunctions)

### mean, meanApprox
```scala
def mean(): Double
```
Compute the mean of this RDD's elements.

```scala
def meanApprox(timeout: Long, confidence: Double = 0.95): PartialResult[BoundedDouble]
```
Approximate operation to return the mean within a timeout.

```scala
val a = sc.parallelize(List(9.1, 1.0, 1.2, 2.1, 1.3, 5.0, 2.0, 2.1, 7.4, 7.5, 7.6, 8.8, 10.0, 8.9, 5.5), 3)
a.mean  //> res63: Double = 5.3
a.meanApprox(1) //> res64: org.apache.spark.partial.PartialResult[org.apache.spark.partial.BoundedDouble] = (final: [5.300, 5.300])
```

### histogram
```scala
def histogram(bucketCount: Int): (Array[Double], Array[Long])
```
Compute a histogram of the data using bucketCount number of buckets evenly spaced between the minimum and maximum of the RDD.

```scala
val a = sc.parallelize(1 to 10) ++ sc.parallelize(3 to 8)

a.histogram(5)
//> res69: (Array[Double], Array[Long]) = (Array(1.0, 2.8, 4.6, 6.4, 8.2, 10.0),Array(2, 4, 4, 4, 2))

a.histogram(3)
//> res70: (Array[Double], Array[Long]) = (Array(1.0, 4.0, 7.0, 10.0),Array(4, 6, 6))
```

### stats
```scala
def stats(): StatCounter
```
Return a org.apache.spark.util.StatCounter object that captures the mean, variance and count of the RDD's elements in one operation.

```scala
val x = sc.parallelize(List(1.0, 2.0, 3.0, 5.0, 20.0, 19.02, 19.29, 11.09, 21.0), 2)
x.stats
//> res75: org.apache.spark.util.StatCounter = (count: 9, mean: 11.266667, stdev: 8.126859, max: 21.000000, min: 1.000000)
```
### stdev, sampleStdev
```scala
def stdev(): Double
```
Compute the standard deviation of this RDD's elements.

```scala
def sampleStdev(): Double
```
Compute the sample standard deviation of this RDD's elements (which corrects for bias in estimating the standard deviation by dividing by N-1 instead of N).

```scala
val x = sc.parallelize(List(1.0, 2.0, 3.0, 5.0, 20.0, 19.02, 19.29, 11.09, 21.0), 2)
x.stdev       //> res76: Double = 8.126859445348149
x.sampleStdev //> res77: Double = 8.619836135333431
```

### sum, sumApprox
```scala
def sum(): Double
```
Add up the elements in this RDD.

```scala
def sumApprox(timeout: Long, confidence: Double = 0.95): PartialResult[BoundedDouble]
```
Approximate operation to return the sum within a timeout.

```scala
val x = sc.parallelize(List(1.0, 2.0, 3.0, 5.0, 20.0, 19.02, 19.29, 11.09, 21.0), 2)
x.sum           //> res95: Double = 101.39999999999999
x.sumApprox(10) //> res99: org.apache.spark.partial.PartialResult[org.apache.spark.partial.BoundedDouble] = (final: [101.400, 101.400])
```

### variance, sampleVariance
```scala
def variance(): Double
```
Compute the variance of this RDD's elements.

```scala
def sampleVariance(): Double
```
Compute the sample variance of this RDD's elements (which corrects for bias in estimating the variance by dividing by N-1 instead of N).

```scala
val x = sc.parallelize(List(1.0, 2.0, 3.0, 5.0, 20.0, 19.02, 19.29, 11.09, 21.0), 2)
x.variance       //> res100: Double = 66.04584444444443
x.sampleVariance //> res103: Double = 74.30157499999999
```

## [OrderedRDDFunctions](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.OrderedRDDFunctions)

### filterByRange
```scala
def filterByRange(lower: K, upper: K): RDD[P]
```
Returns an RDD containing only the elements in the inclusive range lower to upper.

```scala
val randRDD = sc.parallelize(List( (2,"cat"), (6, "mouse"),(7, "cup"), (3, "book"), (4, "tv"), (1, "screen"), (5, "heater")), 3)

randRDD.filterByRange(1,3).collect  //> res23: Array[(Int, String)] = Array((2,cat), (3,book), (1,screen))
randRDD.filterByRange(3,5).collect  //> res25: Array[(Int, String)] = Array((3,book), (4,tv), (5,heater))
```

### repartitionAndSortWithPartitions

### sortByKey
```scala
def sortByKey(ascending: Boolean = true, numPartitions: Int = self.partitions.length): RDD[(K, V)]
```
Sort the RDD by key, so that each partition contains a sorted range of the elements.

```scala
val a = sc.parallelize(List("dog", "cat", "owl", "gnu", "ant"), 2)
val b = sc.parallelize(1 to a.count.toInt, 2)
val c = a.zip(b)

c.collect                  //> res57: Array[(String, Int)] = Array((dog,1), (cat,2), (owl,3), (gnu,4), (ant,5))
c.sortByKey(true).collect  //> res58: Array[(String, Int)] = Array((ant,5), (cat,2), (dog,1), (gnu,4), (owl,3))
```

其他作法
```scala
c.sortBy(kv => kv._1, true).collect  //> res60: Array[(String, Int)] = Array((ant,5), (cat,2), (dog,1), (gnu,4), (owl,3))
c.sortBy(kv => kv._2, false).collect //> res61: Array[(String, Int)] = Array((ant,5), (gnu,4), (owl,3), (cat,2), (dog,1))
```

## [SequenceFileRDDFunctions](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.SequenceFileRDDFunctions)

### saveAsSequenceFile
```scala
def saveAsSequenceFile(path: String, codec: Option[Class[_ <: CompressionCodec]] = None): Unit
```
Output the RDD as a Hadoop SequenceFile using the Writable types we infer from the RDD's key and value types.

```scala
val a = sc.parallelize(Array(("owl",3), ("gnu",4), ("dog",1), ("cat",2), ("ant",5)), 2)
a.saveAsSequenceFile("hd_seq_file")

val b = sc.sequenceFile[String, Int]("hd_seq_file")
b.collect
//> res22: Array[(String, Int)] = Array((dog,1), (cat,2), (ant,5), (owl,3), (gnu,4))
```
