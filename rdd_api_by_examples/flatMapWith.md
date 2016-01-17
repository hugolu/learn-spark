## flatMapWith

定義
```
def flatMapWith[A: ClassTag, U: ClassTag](constructA: Int => A, preservesPartitioning: Boolean = false)(f: (T, A) => Seq[U]): RDD[U]
```
- 看起來像是 currying function
- 第一個輸入Index，透過匿名函式做一些轉換。```preservesPartitioning```作用不明
- 第二個輸入(value, 處理過的Index)，再做一次轉換

範例
```
scala> val a = sc.parallelize(List(1,2,3,4,5,6,7,8,9), 3)

scala> a.flatMapWith(x => x, true)((x, y) => List(y, x)).collect
res60: Array[Int] = Array(0, 1, 0, 2, 0, 3, 1, 4, 1, 5, 1, 6, 2, 7, 2, 8, 2, 9)
```
- 原始值
    - Partition 0: 1, 2, 3
    - Partition 1: 4, 5, 6
    - Partition 2: 7, 8, 9
- 第一個匿名函式
    - Partition 0: index 0 => 0
    - Partition 1: index 1 => 1
    - Partition 1: index 2 => 2
- 第二個匿名函式
    - Partition 0: (1, 0) => (0, 1), (2, 0) => (0, 2), (3, 0) => (0, 3)
    - Partition 1: (4, 1) => (1, 4), (5, 1) => (1, 5), (6, 1) => (1, 6)
    - Partition 2: (7, 2) => (2, 7), (8, 2) => (2, 8), (9, 2) => (2, 9)
- 最後執行 flap


## 附註：Currying function

參考資料
- [Currying Functions in Java & Scala
](http://baddotrobot.com/blog/2013/07/21/curried-functions/)
- [鞣製（Curry）](http://openhome.cc/Gossip/Scala/Curry.html)

正常的函式看起來像
```
def add(x: Int, y: Int): Int = {
  x + y
}
```

currying function 看起來像
```
// shorthand
def add(x: Int)(y: Int): Int = {
  x + y
}

val x = add(1)(2)
x: Int = 3
```

比較正式的currying function這麼寫
```
// longhand
def add(x: Int): (Int => Int) = {
  (y: Int) => {
    x + y
  }
}

val x = add(1)(2)
x: Int = 3
```
- 第一個函式回傳一個匿名函式，型態為```(Int=>Int)```

currying function 可以這麼用
```
def add(x: Int): (Int => Int) = {
  (y: Int) => {
    x + y
  }
}

scala> val onePlus = add(1)
onePlus: Int => Int = <function1>

scala> onePlus(3)
res73: Int = 4

scala> val twoPlus = add(2)
twoPlus: Int => Int = <function1>

scala> twoPlus(3)
res74: Int = 5
```

上面範例如果要用 shorthand currying function，要寫成
```
def add(x: Int)(y: Int): Int = {
  x + y
}

scala> val onePlus = add(1)_
onePlus: Int => Int = <function1>

scala> onePlus(3)
res75: Int = 4

scala> val twoPlus = add(2)_
twoPlus: Int => Int = <function1>

scala> twoPlus(3)
res76: Int = 5
```