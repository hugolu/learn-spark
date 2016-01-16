## treeReduce

定義
```
def  treeReduce(f: (T, T) ⇒ T, depth: Int = 2): T
```
- 跟```reduce()```作用一樣，差別使用 multi-level tree 的方式


範例
```
scala> val z = sc.parallelize(List(1,2,3,4,5,6), 2)

scala> z.treeReduce(_+_)
res16: Int = 21
```
- ```z```
    - partition 0: 1, 2, 3
    - partition 1: 4, 5, 6
- ```treeReduce```
    - 1+2+3+4+5+6 = 21 