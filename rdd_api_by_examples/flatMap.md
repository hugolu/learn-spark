## flatMap

定義
```
def flatMap[U: ClassTag](f: T => TraversableOnce[U]): RDD[U]
```
- map之後flat每個TraversableOnce[U]裡面的元素

範例
```
scala> val a = sc.parallelize(1 to 10, 5)

scala> a.map(1 to _).collect
res34: Array[scala.collection.immutable.Range.Inclusive] = Array(Range(1), Range(1, 2), Range(1, 2, 3), Range(1, 2, 3, 4), Range(1, 2, 3, 4, 5), Range(1, 2, 3, 4, 5, 6), Range(1, 2, 3, 4, 5, 6, 7), Range(1, 2, 3, 4, 5, 6, 7, 8), Range(1, 2, 3, 4, 5, 6, 7, 8, 9), Range(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

scala> a.flatMap(1 to _).collect
res35: Array[Int] = Array(1, 1, 2, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
```
- for 1 to 10 in ```map(1 to _)```
    - 1: Range(1)
    - 2: Range(1, 2)
    - 3: Range(1, 2, 3)
    - ...
- for 1 to 10 in ```flatMap(1 to _)```
    - 1: Range(1) is flattened into 1
    - 2: Range(1, 2) is flattened into 1, 2
    - 3: Range(1, 2, 3) is flattened into 1, 2, 3

範例
```
scala> val a = sc.parallelize(List("apple", "banana", "carrot"))
scala> val b = a.flatMap(n=>n)

scala> b.collect
res7: Array[Char] = Array(a, p, p, l, e, b, a, n, a, n, a, c, a, r, r, o, t)
```
- ```flatMap(n=>n)```
    - ```"apple"```: "a", "p", "p", "l", "e"
    - ```"banana"```: "b", "a", "n", "a", "n", "a"
    - ```"carrot"```: "c", "a", "r", "r", "o", "t"