## collectAsMap [pair]

定義
```
def collectAsMap(): Map[K, V]
```

範例
```
scala> val a = sc.parallelize(List(1, 2, 1, 3), 1)
scala> val b = a.zip(a)

scala> b.collectAsMap
res4: scala.collection.Map[Int,Int] = Map(2 -> 2, 1 -> 1, 3 -> 3)
```
- a: (1, 2, 1, 3)
- b: (1,1), (2,2), (1,1), (3,3)
 
範例
```
scala> val x = sc.parallelize(List((1, "A"), (2, "B"), (1, "C"), (3, "D")))

scala> x.collectAsMap
res6: scala.collection.Map[Int,String] = Map(2 -> B, 1 -> C, 3 -> D)
```
- key=1出現兩次，第一次的value被第二次的value覆蓋
