## toLocalIterator

定義
```
def toLocalIterator: Iterator[T]
```

範例
```
scala> val z = sc.parallelize(List(1,2,3,4,5,6), 2)
scala> val iter = z.toLocalIterator
iter: Iterator[Int] = non-empty iterator

scala> while (iter.hasNext) { println(iter.next) }
1
2
3
4
5
6
```