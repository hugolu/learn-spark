# 基於分區進行操作

基於分區對數據進行操作可以讓我們避免為每個數據元素進行重複的配置工作。
- 通過使用基於分區的操作，可以在每個分區共享一個數據庫連接，來避免建立太多連接，還可以重用 JSON 解析器
- 除了避免重複的配置工作，也可以是用 `mapPartitions` 避免創建物件的開銷

```scala
val a = sc.parallelize(1 to 10000, 100)

val b = a.map(n => (n, 1)).reduce((x, y) => (x._1 + y._1, x._2 + y._2))
//> b: (Int, Int) = (50005000,10000)

val c = a.mapPartitions{ iter =>
  var sum = 0
  var cnt = 0
  while (iter.hasNext) {
    sum += iter.next
    cnt += 1
  }
  List((sum, cnt)).iterator
}.reduce((x, y) => (x._1 + y._1, x._2 + y._2))
//> c: (Int, Int) = (50005000,10000)
```
- b: 為每個元素創建一個 tuple
- c: 為每個分區創建一個 tuple，不用為每個元素都執行這個操作

## `aggregate` 更好的用法
```scala
val d = a.aggregate((0,0))((acc, value) => (acc._1 + value, acc._2 + 1), (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2))
//> d: (Int, Int) = (50005000,10000)
```
The aggregate function allows the user to apply two different reduce functions to the RDD.
- The first reduce function is applied within each **partition** to reduce the data within each partition into a single result.
- The second reduce function is used to combine the different reduced results of all partitions together to arrive at one final result.

The ability to have two separate reduce functions for intra partition versus across partition reducing adds a lot of flexibility.
