## flatMapValues [Pair]

定義
```
def flatMapValues[U](f: V => TraversableOnce[U]): RDD[(K, U)]
```
- Very similar to mapValues, but collapses the inherent structure of the values during mapping.
- 把value的元素拆開做mapping，不知有什麼應用？

範例
```
scala> val a = sc.parallelize(List("dog", "tiger", "lion", "cat", "panther", "eagle"), 2)
scala> val b = a.map(x => (x.length, x))
scala> val c = b.flatMapValues(_.toString)

scala> c.collect
res43: Array[(Int, Char)] = Array((3,d), (3,o), (3,g), (5,t), (5,i), (5,g), (5,e), (5,r), (4,l), (4,i), (4,o), (4,n), (3,c), (3,a), (3,t), (7,p), (7,a), (7,n), (7,t), (7,h), (7,e), (7,r), (5,e), (5,a), (5,g), (5,l), (5,e))
```
- a: dog, tiger, lion, cat, panther, eagle
- b: (3,dog), (5,tiger), (4,lion), (3,cat), (7,panther), (5,eagle)
- c:
    - (3,d), (3,o), (3,g), 
    - (5,t), (5,i), (5,g), (5,e), (5,r), 
    - (4,l), (4,i), (4,o), (4,n), 
    - (3,c), (3,a), (3,t), 
    - (7,p), (7,a), (7,n), (7,t), (7,h), (7,e), (7,r),
    - (5,e), (5,a), (5,g), (5,l), (5,e)