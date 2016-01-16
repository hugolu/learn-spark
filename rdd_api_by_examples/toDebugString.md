## toDebugString

定義
```
def toDebugString: String
```
- Returns a string that contains debug information about the RDD and its dependencies.
- 如何解讀這些 debug string?

範例
```
scala> val a = sc.parallelize(1 to 9, 3)
scala> val b = sc.parallelize(1 to 3, 3)
scala> val c = a.subtract(b)

scala> a.toDebugString
res19: String = (3) ParallelCollectionRDD[22] at parallelize at <console>:21 []

scala> b.toDebugString
res20: String = (3) ParallelCollectionRDD[23] at parallelize at <console>:21 []

scala> c.toDebugString
res21: String =
(3) MapPartitionsRDD[27] at subtract at <console>:25 []
 |  SubtractedRDD[26] at subtract at <console>:25 []
 +-(3) MapPartitionsRDD[24] at subtract at <console>:25 []
 |  |  ParallelCollectionRDD[22] at parallelize at <console>:21 []
 +-(3) MapPartitionsRDD[25] at subtract at <console>:25 []
    |  ParallelCollectionRDD[23] at parallelize at <console>:21 []
```