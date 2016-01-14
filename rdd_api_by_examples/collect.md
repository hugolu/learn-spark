## collect, toArray

定義
```
def collect(): Array[T]
def collect[U: ClassTag](f: PartialFunction[T, U]): RDD[U]
def toArray(): Array[T]
```

範例
```
scala> val c = sc.parallelize(List("Gnu", "Cat", "Rat", "Dog", "Gnu", "Rat"), 2)

scala> c.collect
res0: Array[String] = Array(Gnu, Cat, Rat, Dog, Gnu, Rat)

scala> c.toArray
res1: Array[String] = Array(Gnu, Cat, Rat, Dog, Gnu, Rat)
```
- warning: method toArray in class RDD is deprecated: use collect
