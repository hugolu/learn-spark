## aggregate

### 定義
```
def aggregate[U: ClassTag](zeroValue: U)(seqOp: (U, T) => U, combOp: (U, U) => U): U
```

### 範例
```
scala> val z = sc.parallelize(List(1,2,3,4,5,6), 2)
scala> z.aggregate(0)(math.max(_, _), _ + _)
res0: Int = 9
```
- 初始值
  - Partition 0: 1, 2, 3
  - Partition 1: 4, 5, 6
  - zeroValue: 0
- 第一次 reduce，max(x , y)
  - Partition 0: max(max(max(0, 1), 2), 3) = 3
  - Partition 1: max(max(max(0, 4), 5), 6) = 6
- 第二次 reduce，x + y
  - ((0 + 3) + 6) = 9
  
### 範例
```
scala>val z = sc.parallelize(List(1,2,3,4,5,6), 2)
scala>z.aggregate(5)(math.max(_, _), _ + _)
res1: Int = 16
```
- 初始值
  - Partition 0: 1, 2, 3
  - Partition 1: 4, 5, 6
  - zeroValue: 5
- 第一次 reduce，max(x, y)
  - Partition 0: max(max(max(5, 1), 2), 3) = 5
  - Partition 1: max(max(max(5, 4), 5), 6) = 6
- 第二次 reduce，x + y
  - ((5 + 6) + 6) = 16

### 範例
```
scala> val z = sc.parallelize(List("a","b","c","d","e","f"),2)
scala> z.aggregate("")(_ + _, _+_)
res2: String = abcdef
```
- 初始值
  - Partition 0: "a", "b", "c"
  - Partition 1: "d", "e", "f"
  - zeroValue: ""
- 第一次 reduce，x + y
  - Partition 0: ((("" + "a") + "b") + "c") = "abc"
  - Partition 1: ((("" + "d") + "e") + "f") = "def"
- 第二次 reduce，x + y
  - (("" + "abc") + "def") = "abcdef"

### 範例
```
scala> val z = sc.parallelize(List("a","b","c","d","e","f"),2)
scala> z.aggregate("x")(_ + _, _+_)
res3: String = xxabcxdef
```
- 初始值
  - Partition 0: "a", "b", "c"
  - Partition 1: "d", "e", "f"
  - zeroValue: "x"
- 第一次 reduce，x + y
  - Partition 0: ((("x" + "a") + "b") + "c") = "xabc"
  - Partition 1: ((("x" + "d") + "e") + "f") = "xdef"
- 第二次 reduce，x + y
  - (("x" + "xabc") + "xdef") = "xxabcxdef"

### 範例
```
scala> val z = sc.parallelize(List("12","23","345","4567"),2)
scala> z.aggregate("")((x,y) => math.max(x.length, y.length).toString, (x,y) => x + y)
res4: String = 24
```
- 初始值
  - Partition 0: "12", "23"
  - Partition 1: "345", "4567"
  - zeroValue: ""
- 第一次 reduce，max(x.length, y.length).toString
  - Partition 0: max(max("".length, "12".length).toString.lenght, "34".length).toString = "2"
  - Partition 1: max(max("".length, "345".length).toString.length, "4567".length).toString = "4"
- 第二次 reduce，x+y
  - ((""+"2")+"4") = "24"

### 範例
```
scala> val z = sc.parallelize(List("12","23","345","4567"),2)
scala> z.aggregate("")((x,y) => math.min(x.length, y.length).toString, (x,y) => x + y)
res5: String = 11
```
- 初始值
  - Partition 0: "12", "23"
  - Partition 1: "345", "4567"
  - zeroValue: ""
- 第一次 reduce，min(x.length, y.length).toString
  - Partition 0: min(min("".length, "12".length).toString.length, "34".length).toString = "1"
  - Partition 1: min(min("".length, "345".length).toString.length, "4567".length).toString = "1"
- 第二次 reduce，x + y
  - (("" + "1") + "1") = "11"

### 範例
```
scala> val z = sc.parallelize(List("12","23","345",""),2)
scala> z.aggregate("")((x,y) => math.min(x.length, y.length).toString, (x,y) => x + y)
res6: String = 10
```
- 初始值
  - Partition 0: "12", "23"
  - Partition 1: "345", ""
  - zeroValue: ""
- 第一次 reduce，min(x.length, y.length).toString
  - Partition 0: min(min("".length, "12".length).toString.length, "34".length).toString = "1"
  - Partition 1: min(min("".length, "345".length).toString.length, "".length).toString = "0"
- 第二次 reduce，x + y
  - (("" + "1") + "0") = "0"

----
### 範例 - 計算平均值
```scala
val nums = sc.parallelize(List(1,2,3,4,5,6))
```

```scala
val (total, count) = nums.map(num => (num, 1)).fold((0,0))((tuple1, tuple2) => (tuple1._1 + tuple2._1, tuple1._2 + tuple2._2))
```
- 先將 nums 映射為 (num, 1) 的 tuple，然後透過 fold 個別加總 (total, count)

```scala
val (total, count) = nums.aggregate((0, 0))((acc, num) => (acc._1 + num, acc._2 + 1), (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2))
```
- 可以用 `aggregate` 代替 `map()` 後面接 `fold()` 的方式 (可讀性？)

```scala
val mean = total / count.toDouble   //= 3.5
```

### `aggregate` 與 `reduce`, `fold` 的差異
By definition,
```scala
def aggregate[U: ClassTag](zeroValue: U)(seqOp: (U, T) => U, combOp: (U, U) => U): U

def reduce(f: (T, T) => T): T
def fold(zeroValue: T)(op: (T, T) => T): T
```
- aggregate 的 `zeroValue: U` 可以不同於 RDD 元素的型別，使用 `seqOp: (U, T) => U` 轉換 T 得到 U
- reduct 與 fold 的匿名函式只能輸入、輸出與相同 RDD 元素的型別
