# RDD API by Examples

之前練習操作 RDD Transformation & Action，找到一篇[Spark RDD API Examples](http://homepage.cs.latrobe.edu.au/zhe/ZhenHeSparkRDDAPIExamples.html)，裡面的說明與範例非常簡潔易懂，值得細細品味。以下按照網頁列出API的順序，紀錄理解的筆記與練習的過程。

基本的RDD API把每筆資料視為單一值。然而，使用者有時想操作key-value，因此，Spark擴充RDD介面以提供額外功能，這些函式就能處理key-value。這些特殊的函式有:

| 標記 | 名稱 | 說明 |
|------|------|------|
| [Double] | DoubleRDDFunctions | 這些擴充的方法包含許多總計數值的方法。如果資料轉換成 double type，就能使用這些方法。 |
| [Pair] | PairRDDFunctions | 這些擴充的方法能處理 tuple 結構，第一個項目是key，第二個項目是value。 |
| [Ordered] | OrderedRDDFunctions | 這些擴充的方法能處理 key 可以排序的 tuple 結構。 |
| [SeqFile] | SequenceFileRDDFunctions | 這些擴充的方法讓使用者可以從RDD產生Hadoop sequence file。 (把記憶體上的資料結構寫到檔案中，之後讀出能還原成原先的模樣) |

依照字母順序練習解讀 RDD function call 的運算過程
- [aggregate](aggregate.md)
- [aggregateByKey](aggregateByKey.md) 
- [cartesian](cartesian.md)
- checkpoint
- coalesce, repartition
- cogroup [pair], groupWith [Pair]
- collect, toArray
- collectAsMap [pair]
- combineByKey [pair]
- compute
- context, sparkContext
- count
- countApprox
- countApproxDistinct
- countApproxDistinctByKey [pair]
- countByKey [pair]
- countByKeyApprox [pair]
- countByValue
- countByValueApprox
- dependencies
- distinct
- first
- filter
- filterByRange [Ordered]
- filterWith
- flatMap
- flatMapValues [Pair]
- flatMapWith
- fold
- foldByKey [Pair]
- foreach
- foreachPartition
- foreachWith
- fullOuterJoin [Pair]
- generator, setGenerator
- getCheckpointFile
- preferredLocations
- getStorageLevel
- glom
- groupBy
- groupByKey [Pair]
- histogram [Double]
- id
- intersection
- isCheckpointed
- iterator
- join [pair]
- keyBy
- keys [pair]
- leftOuterJoin [pair]
- lookup [pair]
- map
- mapPartitions
- mapPartitionsWithContext
- mapPartitionsWithIndex
- mapPartitionsWithSplit
- mapValues [pair]
- mapWith
- max
- mean [Double], meanApprox [Double]
- min
- name, setName
- partitionBy [Pair]
- partitioner
- partitions
- persist, cache
- pipe
- randomSplit
- reduce
- reduceByKey [Pair], reduceByKeyLocally[Pair], reduceByKeyToDriver[Pair]
- repartition
- repartitionAndSortWithPartitions [Ordered]
- rightOuterJoin [Pair]
- sample
- sampleByKey [Pair]
- sampleByKeyExact [Pair]
- saveAsHodoopFile [Pair], saveAsHadoopDataset [Pair], saveAsNewAPIHadoopFile [Pair]
- saveAsObjectFile
- saveAsSequenceFile [SeqFile]
- saveAsTextFile
- stats [Double]
- sortBy
- sortByKey [Ordered]
- stdev [Double], sampleStdev [Double]
- subtract
- subtractByKey [Pair]
- sum [Double], sumApprox[Double]
- take
- takeOrdered
- takeSample
- treeAggregate
- treeReduce
- toDebugString
- toJavaRDD
- toLocalIterator
- top
- toString
- union, ++
- unpersist
- values [Pair]
- variance [Double], sampleVariance [Double]
- zip
- zipPartitions
- zipWithIndex
- zipWithUniquId
