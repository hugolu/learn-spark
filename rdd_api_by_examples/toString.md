## toString

定義
```
override def toString: String
```
- Assembles a human-readable textual description of the RDD
- 還是看不懂，這有什麼用？

範例
```
scala> val z = sc.parallelize(List(1,2,3,4,5,6), 2)
z: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[36] at parallelize at <console>:21

scala> z.toString
res33: String = ParallelCollectionRDD[36] at parallelize at <console>:21
```

範例
```
scala> val randRDD = sc.parallelize(List( (7,"cat"), (6, "mouse"),(7, "cup"), (6, "book"), (7, "tv"), (6, "screen"), (7, "heater")))

scala> val sortedRDD = randRDD.sortByKey()
sortedRDD: org.apache.spark.rdd.RDD[(Int, String)] = ShuffledRDD[38] at sortByKey at <console>:23

scala> sortedRDD.toString
res34: String = ShuffledRDD[38] at sortByKey at <console>:23
```