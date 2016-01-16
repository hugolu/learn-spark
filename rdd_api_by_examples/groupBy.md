## groupBy

定義
```
def groupBy[K: ClassTag](f: T => K): RDD[(K, Iterable[T])]
def groupBy[K: ClassTag](f: T => K, numPartitions: Int): RDD[(K, Iterable[T])]
def groupBy[K: ClassTag](f: T => K, p: Partitioner): RDD[(K, Iterable[T])]
```

範例
```
scala> val a = sc.parallelize(1 to 9, 3)

scala> a.groupBy(x => { if (x % 2 == 0) "even" else "odd" }).collect
res48: Array[(String, Iterable[Int])] = Array((even,CompactBuffer(2, 4, 6, 8)), (odd,CompactBuffer(1, 3, 5, 7, 9)))
```
- 初始值
    - partition 0: 1,2,3
    - partition 1: 4,5,6
    - partition 2: 7,8,9
- ```groupBy```
    - key="even": 2,4,6,7
    - key="old": 1,3,5,7,9