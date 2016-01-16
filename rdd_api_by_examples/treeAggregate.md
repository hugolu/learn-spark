## treeAggregate

定義
```
def treeAggregate[U](zeroValue: U)(seqOp: (U, T) ⇒ U, combOp: (U, U) ⇒ U, depth: Int = 2)(implicit arg0: ClassTag[U]): U
```

範例
```
scala> val z = sc.parallelize(List(1,2,3,4,5,6), 2)

scala> z.treeAggregate(0)(math.max(_, _), _ + _)
res14: Int = 9
```
- 初始值 ```z```:
    - partition 0: 1, 2, 3
    - partition 1: 4, 5, 6
- ```seqOp```
    - zeroValue=0
    - partition 0: max(max(max(0,1),2),3)=3
    - partition 1: max(max(max(0,4),5),6)=6
- ```combOp```
    - 3 + 6 = 9

範例
```
scala> val z = sc.parallelize(List(1,2,3,4,5,6), 2)

scala> z.treeAggregate(5)(math.max(_, _), _ + _)
res15: Int = 11
```
- 初始值 ```z```:
    - partition 0: 1, 2, 3
    - partition 1: 4, 5, 6
- ```seqOp```
    - zeroValue=5
    - partition 0: max(max(max(5,1),2),3)=5
    - partition 1: max(max(max(5,4),5),6)=6
- ```combOp```
    - 5 + 6 = 11