## coalesce, repartition

定義
```
def coalesce ( numPartitions : Int , shuffle : Boolean = false ): RDD [T]
def repartition ( numPartitions : Int ): RDD [T]
```
 - repartition(numPartitions) = coalesce(numPartitions, shuffle = true)

helper function
```
def myfunc[T](index: Int, iter: Iterator[T]) : Iterator[String] = {
  iter.toList.map(x => "[partID:" +  index + ", val: " + x + "]").iterator
}
```

範例
```
scala> val y = sc.parallelize(1 to 10, 10)
scala> val z = y.coalesce(2, false)
scala> z.partitions.length
res12: Int = 2
scala> z.mapPartitionsWithIndex(myfunc).foreach(println)
[partID:0, val: 1]
[partID:0, val: 2]
[partID:0, val: 3]
[partID:0, val: 4]
[partID:0, val: 5]
[partID:1, val: 6]
[partID:1, val: 7]
[partID:1, val: 8]
[partID:1, val: 9]
[partID:1, val: 10]
```
- Partition 0: 1, 2, 3, 4, 5
- Partition 1: 6, 7, 8, 9, 10
 
範例
```
scala> val y = sc.parallelize(1 to 10, 10)
scala> val z = y.repartition(2)
scala> z.partitions.length
res13: Int = 2
scala> z.mapPartitionsWithIndex(myfunc).foreach(println)
[partID:0, val: 1]
[partID:0, val: 2]
[partID:0, val: 3]
[partID:0, val: 4]
[partID:0, val: 5]
[partID:0, val: 6]
[partID:0, val: 7]
[partID:0, val: 8]
[partID:0, val: 9]
[partID:0, val: 10]
```
- Partition 0: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 (!?)
