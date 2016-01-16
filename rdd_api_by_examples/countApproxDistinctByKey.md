## countApproxDistinctByKey [pair]

定義
```
def countApproxDistinctByKey(relativeSD: Double = 0.05): RDD[(K, Long)]
def countApproxDistinctByKey(relativeSD: Double, numPartitions: Int): RDD[(K, Long)]
def countApproxDistinctByKey(relativeSD: Double, partitioner: Partitioner): RDD[(K, Long)]
```

範例
```
scala> val a = sc.parallelize(List("Gnu", "Cat", "Rat", "Dog"), 2)
scala> val b = sc.parallelize(a.takeSample(true, 10000, 0), 20)
scala> val c = sc.parallelize(1 to b.count().toInt, 20)
scala> val d = b.zip(c)

scala> d.countByKey
res15: scala.collection.Map[String,Long] = Map(Rat -> 2514, Cat -> 2505, Dog -> 2538, Gnu -> 2443)

scala> d.countApproxDistinctByKey(0.1).collect
res16: Array[(String, Long)] = Array((Rat,2249), (Cat,2651), (Dog,2762), (Gnu,2030))

scala> d.countApproxDistinctByKey(0.01).collect
res17: Array[(String, Long)] = Array((Rat,2499), (Cat,2509), (Dog,2524), (Gnu,2435))

scala> d.countApproxDistinctByKey(0.001).collect
res18: Array[(String, Long)] = Array((Rat,2515), (Cat,2505), (Dog,2539), (Gnu,2442))
```
- a: "Gnu", "Cat", "Rat", "Dog"
- b: 從a重複取樣10000個
- c: 產生一串數字 (1, 2, 3, ... 10000)
- d: ```zip``` b 與 c，變成 (Cat,1), (Rat,2), (Gnu,3), (Rat,4), (Gnu,5), (Rat,6), (Cat,7), (Rat,8), (Cat,9), (Cat,10)....
- ```countByKey```計算每個```key```出現的次數
- ```countApproxDistinctByKey```在RDD分佈大量節點時使用，快速大概算出每個key出現次數
 
| method | Rat數量 | Cat數量 | Dog數量 | Gnu數量 | 合計 |
|--------|---------|---------|---------|---------|------|
| countByKey | 2514 | 2505 | 2538 | 2443 | 10000 |
| countApproxDistinctByKey(0.1) | 2249 | 2651 | 2762 | 2030 | 9692 |
| countApproxDistinctByKey(0.01) | 2499 | 2509 | 2524 | 2435 | 9967 |
| countApproxDistinctByKey(0.001) | 2515 | 2505 | 2539 | 2442 | 10001 |