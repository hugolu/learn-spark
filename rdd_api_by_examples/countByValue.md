## countByValue

定義
```
def countByValue(): Map[T, Long]
```

範例
```
scala> val b = sc.parallelize(List(1,2,3,4,5,6,7,8,2,4,2,1,1,1,1,1))

scala> b.countByValue
res38: scala.collection.Map[Int,Long] = Map(5 -> 1, 1 -> 6, 6 -> 1, 2 -> 3, 7 -> 1, 3 -> 1, 8 -> 1, 4 -> 2)
```
- 計算個別value出現的次數