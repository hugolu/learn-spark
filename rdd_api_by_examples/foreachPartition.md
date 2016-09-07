## foreachPartition

定義
```
def foreachPartition(f: Iterator[T] => Unit)
```

範例
```
scala> val b = sc.parallelize(List(1, 2, 3, 4, 5, 6, 7, 8, 9), 3)

scala> b.foreachPartition(x => println(x.reduce(_ + _)))
6
15
24
```
- partition 0: (1,2,3).reduce(_+_)=6
- partition 1: (4,5,6).reduce(_+_)=15
- partition 2: (7,8,9).reduce(_+_)=24

----
```scala
scala> val b = sc.parallelize(List(1,2,3,4,5,6,7,8,9),3)
scala> b.foreachPartition{ iter => println(iter.toList.mkString(",")) }
1,2,3
4,5,6
7,8,9
```
