# reduceByKey vs. groupByKey

```scala
def groupByKey(): RDD[(K, Iterable[V])]
def reduceByKey(func: (V, V) => V): RDD[(K, V)]
```

```scala
val words = sc.parallelize(List("apple", "dog", "cat", "cat", "apple", "dog", "cat", "dog", "cat"), 3)
words.foreachPartition(iter => println(iter.mkString(",")))
//> apple,dog,cat
//> cat,apple,dog
//> cat,dog,cat

val wordPairs = words.map(word => (word, 1))

val wc1 = wordPairs.reduceByKey(_+_)
wc1.collect
//> res6: Array[(String, Int)] = Array((cat,4), (dog,3), (apple,2))

val wc2 = wordPairs.groupByKey().map(t => (t._1, t._2.sum))
wc2.collect
//> res7: Array[(String, Int)] = Array((cat,4), (dog,3), (apple,2))
```
