## count

定義
```
def count(): Long
```
- 算出儲存在RDD裡面元素的數量，就這樣。
- 可省略```()```

範例
```
scala> val c = sc.parallelize(List("Gnu", "Cat", "Rat", "Dog"), 2)

scala> c.count
res1: Long = 4
```