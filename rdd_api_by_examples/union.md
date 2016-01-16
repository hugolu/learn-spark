## union, ++

定義
```
def ++(other: RDD[T]): RDD[T]
def union(other: RDD[T]): RDD[T]
```
- 聯集，value 可重複

範例
```
scala> val a = sc.parallelize(1 to 3, 1)
scala> val b = sc.parallelize(5 to 7, 1)

scala> (a ++ b).collect
res35: Array[Int] = Array(1, 2, 3, 5, 6, 7)

scala> a.union(b).collect
res36: Array[Int] = Array(1, 2, 3, 5, 6, 7)
```
- a: 1, 2, 3
- b: 5, 6, 7
- a ++ b: 1, 2, 3, 5, 6, 7

範例
```
scala> val c = sc.parallelize(1 to 5)
scala> val d = sc.parallelize(3 to 7)

scala> c.union(d).collect
res39: Array[Int] = Array(1, 2, 3, 4, 5, 3, 4, 5, 6, 7)
```
- c: 1, 2, 3, 4, 5
- d: 3, 4, 5, 6, 7
- c ++ d: 1, 2, 3, 4, 5, 3, 4, 5, 6, 7