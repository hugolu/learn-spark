# Summary

* [RDD API by Examples](rdd_api_by_examples/README.md)
    * [aggregate](rdd_api_by_examples/aggregate.md)
    * [aggregateByKey](rdd_api_by_examples/aggregateByKey.md)
    * [cartesian](rdd_api_by_examples/cartesian.md)
    * checkpoint
    * coalesce, repartition
    * cogroup [pair], groupWith [Pair]
    * collect, toArray
    * collectAsMap [pair]
    * combineByKey [pair]
    * compute
    * context, sparkContext
    * count
    * countApprox
    * countApproxDistinct
    * countApproxDistinctByKey [pair]
    * countByKey [pair]
    * countByKeyApprox [pair]
    * countByValue
    * countByValueApprox
    * dependencies
    * distinct
    * first
    * filter
    * filterByRange [Ordered]
    * filterWith
    * flatMap
    * flatMapValues [Pair]
    * flatMapWith
    * fold
    * foldByKey [Pair]
    * foreach
    * foreachPartition
    * foreachWith
    * fullOuterJoin [Pair]
    * generator, setGenerator
    * getCheckpointFile
    * preferredLocations
    * getStorageLevel
    * glom
    * groupBy
    * groupByKey [Pair]
    * histogram [Double]
    * id
    * intersection
    * isCheckpointed
    * iterator
    * join [pair]
    * keyBy
    * keys [pair]
    * leftOuterJoin [pair]
    * lookup [pair]
    * map
    * mapPartitions
    * mapPartitionsWithContext
    * mapPartitionsWithIndex
    * mapPartitionsWithSplit
    * mapValues [pair]
    * mapWith
    * max
    * mean [Double], meanApprox [Double]
    * min
    * name, setName
    * partitionBy [Pair]
    * partitioner
    * partitions
    * persist, cache
    * pipe
    * randomSplit
    * reduce
    * reduceByKey [Pair], reduceByKeyLocally[Pair], reduceByKeyToDriver[Pair]
    * repartition
    * repartitionAndSortWithPartitions [Ordered]
    * rightOuterJoin [Pair]
    * sample
    * sampleByKey [Pair]
    * sampleByKeyExact [Pair]
    * saveAsHodoopFile [Pair], saveAsHadoopDataset [Pair], saveAsNewAPIHadoopFile [Pair]
    * saveAsObjectFile
    * saveAsSequenceFile [SeqFile]
    * saveAsTextFile
    * stats [Double]
    * sortBy
    * sortByKey [Ordered]
    * stdev [Double], sampleStdev [Double]
    * subtract
    * subtractByKey [Pair]
    * sum [Double], sumApprox[Double]
    * take
    * takeOrdered
    * takeSample
    * treeAggregate
    * treeReduce
    * toDebugString
    * toJavaRDD
    * toLocalIterator
    * top
    * toString
    * union, ++
    * unpersist
    * values [Pair]
    * variance [Double], sampleVariance [Double]
    * zip
    * zipPartitions
    * zipWithIndex
    * zipWithUniquId