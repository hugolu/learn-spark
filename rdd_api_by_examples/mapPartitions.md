## mapPartitions

定義
```
def mapPartitions[U: ClassTag](f: Iterator[T] => Iterator[U], preservesPartitioning: Boolean = false): RDD[U]
```

範例
```
val a = sc.parallelize(1 to 9, 3)

def myfunc[T](iter: Iterator[T]) : Iterator[(T, T)] = {
  var res = List[(T, T)]()
  var pre = iter.next
  while (iter.hasNext)
  {
    val cur = iter.next;
    res .::= (pre, cur)
    pre = cur;
  }
  res.iterator
}

a.mapPartitions(myfunc).collect
res0: Array[(Int, Int)] = Array((2,3), (1,2), (5,6), (4,5), (8,9), (7,8))
```
- before
    - partition 0: 1, 2, 3
    - partition 1: 4, 5, 6
    - partition 2: 7, 8, 9
- what ```myfunc()``` did
    - partition 0:
        - res = null 
        - res = (1, 2) :: res
        - res = (2, 3) :: res
        - then, res = List((2, 3), (1, 2))
    - partition 0:
        - res = null 
        - res = (4, 5) :: res
        - res = (5, 6) :: res
        - then, res = List((5, 6), (4, 5))
    - partition 0:
        - res = null 
        - res = (7, 8) :: res
        - res = (8, 9) :: res
        - then, res = List((8, 9), (7, 8))

範例
```
val x = sc.parallelize(List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 3)

def myfunc(iter: Iterator[Int]) : Iterator[Int] = {
  var res = List[Int]()
  while (iter.hasNext) {
    val cur = iter.next;
    res = res ::: List.fill(scala.util.Random.nextInt(10))(cur)
  }
  res.iterator
}

x.mapPartitions(myfunc).collect
res7: Array[Int] = Array(1, 1, 2, 2, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10)
```
