## values [Pair]

定義
```
def values: RDD[V]
```
- 取出所有tuple的值

範例
```
scala> val a = sc.parallelize(List("dog", "tiger", "lion", "cat", "panther", "eagle"), 2)
scala> val b = a.map(x => (x.length, x))

scala> b.values.collect
res40: Array[String] = Array(dog, tiger, lion, cat, panther, eagle)
```
- b: (3,dog), (5,tiger), (4,lion), (3,cat), (7,panther), (5,eagle)
- b.values: dog, tiger, lion, cat, panther, eagle