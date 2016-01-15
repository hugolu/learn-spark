## context, sparkContext

定義
```
def compute(split: Partition, context: TaskContext): Iterator[T]
```
- Returns the SparkContext that was used to create the RDD.

範例
```
scala> val c = sc.parallelize(List("Gnu", "Cat", "Rat", "Dog"), 2)

scala> c.context
res0: org.apache.spark.SparkContext = org.apache.spark.SparkContext@67b5d446
```
- 搞不清楚用途...