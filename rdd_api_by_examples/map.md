## map

定義
```
def map[U: ClassTag](f: T => U): RDD[U]
```

範例
```
scala> val a = sc.parallelize(List("dog", "salmon", "salmon", "rat", "elephant"), 3)
scala> val b = a.map(_.length)
scala> val c = a.zip(b)

scala> c.collect
res50: Array[(String, Int)] = Array((dog,3), (salmon,6), (salmon,6), (rat,3), (elephant,8))
```
| RDD | #0 | #1 | #2 | #3 | #4 |
|-----|----|----|----|----|----|
| a | dog | salmon | salmon | rat | elephant |
| b | 3 | 6 | 6 | 3 | 8 |
| c | (dog,3) | (salmon,6) | (salmon,6) | (rat,3) | (elephant,8) |