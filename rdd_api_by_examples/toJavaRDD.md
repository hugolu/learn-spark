## toJavaRDD

定義
```
def toJavaRDD() : JavaRDD[T]
```

範例
```
scala> val c = sc.parallelize(List("Gnu", "Cat", "Rat", "Dog"), 2)

scala> c.toJavaRDD
res22: org.apache.spark.api.java.JavaRDD[String] = ParallelCollectionRDD[28] at parallelize at <console>:21
```