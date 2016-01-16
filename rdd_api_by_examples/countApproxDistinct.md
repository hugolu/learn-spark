## countApproxDistinct

定義
```
def countApproxDistinct(relativeSD: Double = 0.05): Long
```

範例
```
scala> val a = sc.parallelize(1 to 10000, 20)
scala> val b = a++a++a++a++a

scala> b.count
res8: Long = 50000

scala> b.countApproxDistinct(0.1)
res9: Long = 8224

scala> b.countApproxDistinct(0.01)
res10: Long = 9947

scala> b.countApproxDistinct(0.001)
res11: Long = 10000
```
- RDD 相加要用 ```++```
- ```countApproxDistinct``` 使用在資料分佈在大量節點，可以快速計算出一個大概的數量