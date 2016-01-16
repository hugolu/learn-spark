## top

定義
```
ddef top(num: Int)(implicit ord: Ordering[T]): Array[T]
```

範例
```
scala> val c = sc.parallelize(Array(6, 9, 4, 7, 5, 8), 2)

scala> c.top(2)
res29: Array[Int] = Array(9, 8)
```

範例
```
scala> val a = sc.parallelize(List((2, "B"), (3, "C"), (1, "A"), (4, "D")))

scala> a.top(2)
res31: Array[(Int, String)] = Array((4,D), (3,C))
```