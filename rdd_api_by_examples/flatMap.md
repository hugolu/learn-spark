## flatMap

定義
```
def flatMap[U: ClassTag](f: T => TraversableOnce[U]): RDD[U]
```
- map之後flat

範例
```
scala> val a = sc.parallelize(1 to 10, 5)

scala> a.map(1 to _).collect
res34: Array[scala.collection.immutable.Range.Inclusive] = Array(Range(1), Range(1, 2), Range(1, 2, 3), Range(1, 2, 3, 4), Range(1, 2, 3, 4, 5), Range(1, 2, 3, 4, 5, 6), Range(1, 2, 3, 4, 5, 6, 7), Range(1, 2, 3, 4, 5, 6, 7, 8), Range(1, 2, 3, 4, 5, 6, 7, 8, 9), Range(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

scala> a.flatMap(1 to _).collect
res35: Array[Int] = Array(1, 1, 2, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
```

範例
```
scala> val a = sc.parallelize(List(1, 2, 3), 2)

scala> a.map(x => List(x,x,x)).collect
res36: Array[List[Int]] = Array(List(1, 1, 1), List(2, 2, 2), List(3, 3, 3))

scala>

scala> a.flatMap(x => List(x,x,x)).collect
res37: Array[Int] = Array(1, 1, 1, 2, 2, 2, 3, 3, 3)
```