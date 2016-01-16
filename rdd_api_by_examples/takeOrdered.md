## takeOrdered

定義
```
def takeOrdered(num: Int)(implicit ord: Ordering[T]): Array[T]
```

範例
```
scala> val b = sc.parallelize(List("dog", "cat", "ape", "salmon", "gnu"), 2)

scala> b.takeOrdered(2)
res2: Array[String] = Array(ape, cat)
```

範例
```
scala> val x = sc.parallelize(1 to 1000)
scala> val y = sc.parallelize(x.takeSample(false, 100))

scala> y.takeOrdered(10)
res10: Array[Int] = Array(4, 23, 28, 37, 42, 43, 55, 59, 78, 97)
```