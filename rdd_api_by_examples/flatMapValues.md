## flatMapValues [Pair]

定義
```
def flatMapValues[U](f: V => TraversableOnce[U]): RDD[(K, U)]
```
- Very similar to mapValues, but collapses the inherent structure of the values during mapping.
- 把value的元素拆開做mapping，不知有什麼應用？
- ```f```回傳值型態要符合 TraversableOnce

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
 
範例
```
scala> val a = sc.parallelize(1 to 9, 3)
scala> val b = a.map(Math.pow(_,2).toInt)
scala> val c = a.zip(b)

scala> c.flatMapValues(_.toString).collect
res54: Array[(Int, Char)] = Array((1,1), (2,4), (3,9), (4,1), (4,6), (5,2), (5,5), (6,3), (6,6), (7,4), (7,9), (8,6), (8,4), (9,8), (9,1))

scala> c.flatMapValues(List(_)).collect
res55: Array[(Int, Int)] = Array((1,1), (2,4), (3,9), (4,16), (5,25), (6,36), (7,49), (8,64), (9,81))
```
- c: (1,1), (2,4), (3,9), (4,16), (5,25), (6,36), (7,49), (8,64), (9,81)
- ```flatMapValues(_.toInt)```會出現錯誤，因為```Int```不是```TraversableOnce[?]```
- ```flatMapValues(_.toString)```會對```_.toString```做展開的動作(collapses the inherent structure of the values )，因此字串```"81"```會被展開成```"8"```與```"1"```。執行```flatMapValues()```產生```Array[(Int, Char)]```。
- ```flatMapValues(List(_))```會對```List(_)```做展開的動作，但是list裡面只有一個元素(```Int```)，所以不會被拆開。執行```flatMapValues()```產生```Array[(Int, Int)]```。