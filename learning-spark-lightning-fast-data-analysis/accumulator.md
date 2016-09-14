# Accumulator

累加器提供了將工作節點的值聚合到驅動器程序中的簡單語法。累加器的一個常見的用途是在調試時對作業執行過程中的事件進行計數。

```scala
val a = sc.parallelize(1 to 10000, 100)
val evens = sc.accumulator(0)
val b = a.map{ n =>
  if (n % 2 == 0) {
    evens += 1
  }
  n + 1
}

b.first
println(evens.value)  //> 0

b.take(10)
println(evens.value)  //> 5

evens.value = 0
b.collect
println(evens.value)  //> 5000
```
- 只有在 `b.collect` 動作後才能看見正確的計數，因為操作前的轉化動作是惰性的

當然也可以使用 `reduce()` 的操作將整個 RDD 的值都聚合到驅動器中，只是有時候我們希望使用一種更簡單的方法對那些與 RDD 本身範圍和粒度不一樣的值進行聚合。
