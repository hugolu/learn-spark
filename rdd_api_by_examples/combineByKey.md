## combineByKey [pair]

定義
```
def combineByKey[C](createCombiner: V => C, mergeValue: (C, V) => C, mergeCombiners: (C, C) => C): RDD[(K, C)]
def combineByKey[C](createCombiner: V => C, mergeValue: (C, V) => C, mergeCombiners: (C, C) => C, numPartitions: Int): RDD[(K, C)]
def combineByKey[C](createCombiner: V => C, mergeValue: (C, V) => C, mergeCombiners: (C, C) => C, partitioner: Partitioner, mapSideCombine: Boolean = true, serializerClass: String = null): RDD[(K, C)]
```

範例
```
scala> val a = sc.parallelize(List("dog","cat","gnu","salmon","rabbit","turkey","wolf","bear","bee"), 3)
scala> val b = sc.parallelize(List(1,1,2,2,2,1,2,2,2), 3)
scala> val c = b.zip(a)

scala> val d = c.combineByKey(List(_), (x:List[String], y:String) => y :: x, (x:List[String], y:List[String]) => x ::: y)
scala> d.collect
res17: Array[(Int, List[String])] = Array((1,List(cat, dog, turkey)), (2,List(gnu, rabbit, salmon, bee, bear, wolf)))
```
- c is a List of tuple(Int, String)
  - (1,dog), (1,cat), (2,gnu), (2,salmon), (2,rabbit), (1,turkey), (2,wolf), (2,bear), (2,bee)
- ```combineByKey()```
  - createCombiner: x => List(x)
  - mergeValue: (x:List[String], y:String) => y :: x
  - mergeCombiners: (x:List[String], y:List[String]) => x ::: y
- d is a List of tuple(Int, List[String])
  - key=1, value=List(cat, dog, turkey)
  - key=2, value=List(gnu, rabbit, salmon, bee, bear, wolf)
