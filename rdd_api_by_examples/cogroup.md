## cogroup [Pair], groupWith [Pair]

定義
```
def cogroup[W](other: RDD[(K, W)]): RDD[(K, (Iterable[V], Iterable[W]))]
def cogroup[W](other: RDD[(K, W)], numPartitions: Int): RDD[(K, (Iterable[V], Iterable[W]))]
def cogroup[W](other: RDD[(K, W)], partitioner: Partitioner): RDD[(K, (Iterable[V], Iterable[W]))]
def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)]): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2]))]
def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)], numPartitions: Int): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2]))]
def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)], partitioner: Partitioner): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2]))]
def groupWith[W](other: RDD[(K, W)]): RDD[(K, (Iterable[V], Iterable[W]))]
def groupWith[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)]): RDD[(K, (Iterable[V], IterableW1], Iterable[W2]))]
```

範例
```
scala> val a = sc.parallelize(List(1, 2, 1, 3), 1)
scala> val b = a.map((_, "b"))
scala> val c = a.map((_, "c"))

scala> b.cogroup(c).collect
res25: Array[(Int, (Iterable[String], Iterable[String]))] = Array((1,(CompactBuffer(b, b),CompactBuffer(c, c))), (3,(CompactBuffer(b),CompactBuffer(c))), (2,(CompactBuffer(b),CompactBuffer(c))))
```
- 初始狀態
 - b: (1, "b"), (2, "b"), (1, "b"), (3, "b")
 - c: (1, "c"), (2, "c"), (1, "c"), (3, "c")
- ```b.cogroup(c)``` 作用後
 - key=1, value=(CompactBuffer(b, b),CompactBuffer(c, c))
 - key=2, value=(CompactBuffer(b),CompactBuffer(c))
 - key=3, value=(CompactBuffer(b),CompactBuffer(c))

範例
```
scala> val a = sc.parallelize(List(1, 2, 1, 3), 1)
scala> val b = a.map((_, "b"))
scala> val c = a.map((_, "c"))
scala> val d = a.map((_, "d"))

scala> b.cogroup(c, d).collect
res26: Array[(Int, (Iterable[String], Iterable[String], Iterable[String]))] = Array((1,(CompactBuffer(b, b),CompactBuffer(c, c),CompactBuffer(d, d))), (3,(CompactBuffer(b),CompactBuffer(c),CompactBuffer(d))), (2,(CompactBuffer(b),CompactBuffer(c),CompactBuffer(d))))
```
- 初始狀態
 - b: (1, "b"), (2, "b"), (1, "b"), (3, "b")
 - c: (1, "c"), (2, "c"), (1, "c"), (3, "c")
 - d: (1, "d"), (2, "d"), (1, "d"), (3, "d")
- ```b.cogroup(c,d)``` 作用後
 - key=1, value=(CompactBuffer("b", "b"),CompactBuffer("c", "c"),CompactBuffer("d", "d"))
 - key=2, value=(CompactBuffer("b"),CompactBuffer("c"),CompactBuffer("d")) 
 - key=3, value=(CompactBuffer("b"),CompactBuffer("c"),CompactBuffer("d")) 

範例
```
scala> val x = sc.parallelize(List((1, "apple"), (2, "banana"), (3, "orange"), (4, "kiwi")), 2)
scala> val y = sc.parallelize(List((5, "computer"), (1, "laptop"), (1, "desktop"), (4, "iPad")), 2)

scala> x.cogroup(y).collect
res27: Array[(Int, (Iterable[String], Iterable[String]))] = Array((4,(CompactBuffer(kiwi),CompactBuffer(iPad))), (2,(CompactBuffer(banana),CompactBuffer())), (1,(CompactBuffer(apple),CompactBuffer(laptop, desktop))), (3,(CompactBuffer(orange),CompactBuffer())), (5,(CompactBuffer(),CompactBuffer(computer))))
```
- 初始狀態
 - x: (1, "apple"), (2, "banana"), (3, "orange"), (4, "kiwi")
 - y: (5, "computer"), (1, "laptop"), (1, "desktop"), (4, "iPad")
- ```x.cogroup(y)```作用後
 - key=1, value=(CompactBuffer(apple),CompactBuffer(laptop, desktop))
 - key=2, value=(CompactBuffer(banana),CompactBuffer())
 - key=3, value=(CompactBuffer(orange),CompactBuffer())
 - key=4, value=(CompactBuffer(kiwi),CompactBuffer(iPad))
 - key=5, value=(CompactBuffer(),CompactBuffer(computer))

範例
```
scala> val x = sc.parallelize(List((1, "A"), (2, "B"), (3, "C"), (4, "D")))
scala> val y = sc.parallelize(List((2, "E"), (3, "D"), (4, "C"), (5, "B")))

scala> x.groupWith(y).collect
res28: Array[(Int, (Iterable[String], Iterable[String]))] = Array((4,(CompactBuffer(D),CompactBuffer(C))), (1,(CompactBuffer(A),CompactBuffer())), (3,(CompactBuffer(C),CompactBuffer(D))), (5,(CompactBuffer(),CompactBuffer(B))), (2,(CompactBuffer(B),CompactBuffer(E))))
```
- 初始狀態
 - x: (1, "A"), (2, "B"), (3, "C"), (4, "D")
 - y: (2, "E"), (3, "D"), (4, "C"), (5, "B")
- ```x.groupWith(y)```作用後
 - key=1, value=(CompactBuffer(A),CompactBuffer())
 - key=2, value=(CompactBuffer(B),CompactBuffer(E))
 - key=3, value=(CompactBuffer(C),CompactBuffer(D))
 - key=4, value=(CompactBuffer(D),CompactBuffer(C))
 - key=5, value=(CompactBuffer(),CompactBuffer(B))
 
範例
```
scala> val x = sc.parallelize(List((1, "A"), (2, "B"), (1, "C"), (4, "D")))
scala> val y = sc.parallelize(List((2, "E"), (3, "D"), (2, "C"), (5, "B")))
scala> val z= x.groupWith(y)

scala> x.groupWith(y).collect
res29: Array[(Int, (Iterable[String], Iterable[String]))] = Array((4,(CompactBuffer(D),CompactBuffer())), (1,(CompactBuffer(A, C),CompactBuffer())), (3,(CompactBuffer(),CompactBuffer(D))), (5,(CompactBuffer(),CompactBuffer(B))), (2,(CompactBuffer(B),CompactBuffer(E, C))))

```
