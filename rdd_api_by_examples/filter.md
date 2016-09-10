## filter

```scala
val a = sc.parallelize(1 to 10, 3)
val b = a.filter(_ % 2 == 0)
b.collect //> res0: Array[Int] = Array(2, 4, 6, 8, 10)
```
