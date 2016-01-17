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
    - partition 1:
        - res = null 
        - res = (4, 5) :: res
        - res = (5, 6) :: res
        - then, res = List((5, 6), (4, 5))
    - partition 2:
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
- x:
    - Partition 0: 1, 2, 3
    - Partition 1: 4, 5, 6
    - Partition 2: 7, 8, 9, 10
- what ```myfunc()``` did
    - Partition 0:
        - res = null
        - res = res ::: List(1,1)
        - res = res ::: List(2,2)
        - res = res ::: List(3, 3, 3, 3, 3, 3, 3)
        - then, res = List(1, 1, 2, 2, 3, 3, 3, 3, 3, 3, 3)
    - Partition 1:
        - res = null
        - res = res ::: List(4, 4, 4, 4, 4, 4)
        - res = res ::: List(5, 5, 5, 5)
        - res = res ::: List(6, 6, 6, 6, 6, 6, 6, 6)
        - then, res = List(4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6)
    - Partition 2:
        - res = null
        - res = res ::: List(7, 7, 7, 7)
        - res = res ::: List(8, 8, 8, 8, 8, 8, 8, 8, 8)
        - res = res ::: List(9, 9, 9, 9, 9, 9, 9, 9)
        - res = res ::: List(10, 10)
        - then, res = List(7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10)

上面的範例也可用```flatMap```做到
```
val x  = sc.parallelize(1 to 10, 3)
x.flatMap(List.fill(scala.util.Random.nextInt(10))(_)).collect
```
- ```mapPartitions()``` - 針對個別partition iterator呼叫```List.fill()```
- ```flatMap()``` - 一口氣對所有partition元素呼叫```List.fill()```


### 補充
```
scala> val list = List[Int]()
list: List[Int] = List()

scala> val list2 = (1 :: (2 :: (3 :: list)))
list2: List[Int] = List(1, 2, 3)
```
- 合併元素與List使用```::```，元素放在前面，合併後產生一個新的List

```
scala> val list = List[Int]()
list: List[Int] = List()
scala> val list2 = (((list ::: List(1)) ::: List(2)) ::: List(3))
list2: List[Int] = List(1, 2, 3)
```
- 合併List與List使用```:::```，合併後產生一個新的List