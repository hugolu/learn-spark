## countByKey [pair]

定義
```
def countByKey(): Map[K, Long]
```

範例
```
scala> val c = sc.parallelize(List((3, "Gnu"), (3, "Yak"), (5, "Mouse"), (3, "Dog")), 2)

scala> c.countByKey
res34: scala.collection.Map[Int,Long] = Map(3 -> 3, 5 -> 1)
```
- 計算個別key出現的次數