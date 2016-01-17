## mapPartitionsWithIndex

定義
```
def mapPartitionsWithIndex[U: ClassTag](f: (Int, Iterator[T]) => Iterator[U], preservesPartitioning: Boolean = false): RDD[U]
```
- the first parameter is the index of the partition
- the second is an iterator through all the items within this partition
- the output is an iterator containing the list of items after applying whatever transformation the function encodes.

範例
```
val x = sc.parallelize(List(1,2,3,4,5,6,7,8,9,10), 3)
def myfunc(index: Int, iter: Iterator[Int]) : Iterator[String] = {
  iter.toList.map(x => index + "," + x).iterator
}
x.mapPartitionsWithIndex(myfunc).collect()
res15: Array[String] = Array(0,1, 0,2, 0,3, 1,4, 1,5, 1,6, 2,7, 2,8, 2,9, 2,10)
```
- before ```mapPartitionsWithIndex(myfunc)```:
    - Partition 0: 1,2,3
    - Partition 1: 4,5,6
    - Partition 2: 7,8,9,10
- after ```mapPartitionsWithIndex(myfunc)```:
    - Partition 0: "0,1", "0,2", "0,3"
    - Partition 1: "1,4", "1,5", "1,6"
    - Partition 2: "2,7", "2,8", "2,9", "2,10"

