## aggregateByKey

### 定義
```
def aggregateByKey[U](zeroValue: U)(seqOp: (U, V) ⇒ U, combOp: (U, U) ⇒ U)(implicit arg0: ClassTag[U]): RDD[(K, U)]
def aggregateByKey[U](zeroValue: U, numPartitions: Int)(seqOp: (U, V) ⇒ U, combOp: (U, U) ⇒ U)(implicit arg0: ClassTag[U]): RDD[(K, U)]
def aggregateByKey[U](zeroValue: U, partitioner: Partitioner)(seqOp: (U, V) ⇒ U, combOp: (U, U) ⇒ U)(implicit arg0: ClassTag[U]): RDD[(K, U)]
```
- unlike the aggregate function the initial value is not applied to the second reduce.

### 範例
```
scala> val pairRDD = sc.parallelize(List( ("cat",2), ("cat", 5), ("mouse", 4),("cat", 12), ("dog", 12), ("mouse", 2)), 2)
scala> pairRDD.aggregateByKey(0)(math.max(_, _), _ + _).collect
res9: Array[(String, Int)] = Array((dog,12), (cat,17), (mouse,6))
```
- 初始值
 - Partition 0: ("cat",2), ("cat", 5), ("mouse", 4)
 - Partition 1: ("cat", 12), ("dog", 12), ("mouse", 2)
 - zeroValue: 0
- 第一次 reduce，max(x, y)
 - Partition 0:
  - "cat": max(max(0, 2), 5) = 5
  - "mouse": max(0, 4) = 4
 - Partition 1:
  - "cat": max(0, 12) = 12
  - "dog": max(0, 12) = 12
  - "mouse": max(0, 2) = 2
- 第二次 reduce，x + y
 - "dog": 0 + 12 = 12
 - "cat": 5 + 12 = 17
 - "mouse": 4 + 2 = 6

### 範例
```
scala> val pairRDD = sc.parallelize(List( ("cat",2), ("cat", 5), ("mouse", 4),("cat", 12), ("dog", 12), ("mouse", 2)), 2)
scala> pairRDD.aggregateByKey(100)(math.max(_, _), _ + _).collect
res10: Array[(String, Int)] = Array((dog,100), (cat,200), (mouse,200))
```
- 初始值
 - Partition 0: ("cat",2), ("cat", 5), ("mouse", 4)
 - Partition 1: ("cat", 12), ("dog", 12), ("mouse", 2)
  - zeroValue: 100
- 第一次 reduce，max(x, y)
 - Partition 0:
  - "cat": max(100, 5) = 100
  - "mouse": max(100, 4) = 100
 - Partition 1:
  - "cat": max(100, 12) = 100
  - "dog": max(100, 12) = 100
  - "mouse": max(100, 2) = 100
- 第二次 reduce，x + y
 - "dog": 0 + 100 = 100
 - "cat": 100 + 100 = 200
 - "mouse": 100 + 100 = 200
