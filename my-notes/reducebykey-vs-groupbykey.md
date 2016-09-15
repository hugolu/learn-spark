# reduceByKey vs. groupByKey

参考：[在Spark中尽量少使用GroupByKey函数](https://www.iteblog.com/archives/1357)

```scala
def groupByKey(): RDD[(K, Iterable[V])]
def reduceByKey(func: (V, V) => V): RDD[(K, V)]
```

> 虽然两个函数都能得出正确的结果， 但reduceByKey函数更适合使用在大数据集上。 这是因为Spark知道它可以在每个分区移动数据之前将输出数据与一个共用的 key 结合。

```scala
val words = sc.parallelize(List("apple", "dog", "cat", "cat", "apple", "dog", "cat", "dog", "cat"), 3)
words.foreachPartition(iter => println(iter.mkString(",")))
//> apple,dog,cat
//> cat,apple,dog
//> cat,dog,cat

val wordPairs = words.map(word => (word, 1))
wordPairs.foreachPartition(iter => println(iter.mkString(",")))
//> (apple,1),(dog,1),(cat,1)
//> (cat,1),(apple,1),(dog,1)
//> (cat,1),(dog,1),(cat,1)
```

```scala
wordPairs.reduceByKey(_+_).collect                //> res1: Array[(String, Int)] = Array((cat,4), (dog,3), (apple,2))
```
key-value 被搬移前，同一台机器上先用 lambda 函数合并相同 key 的值，shuffle 后，lambda 函数再将每个分区的 key-value 合并成最终结果。

![](https://www.iteblog.com/pic/reduce_by.png)

```scala
wordPairs.groupByKey().map(t => (t._1, t._2.sum)) //> res2: Array[(String, Int)] = Array((cat,4), (dog,3), (apple,2))
```
当调用 groupByKey时，所有的键值对(key-value pair) 都会被移动。在网络上传输这些数据非常没有必要。避免使用 GroupByKey。

![](https://www.iteblog.com/pic/group_by.png)
