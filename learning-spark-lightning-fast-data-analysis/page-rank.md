#　Page Rank

Page Rank 是執行多次連接(join)的迭代演算法，是RDD分區操作很好的範例。

演算法維護兩個數據集
- (pageId, linkList): 包含每個頁面的相鄰頁面列表
- (pageId, rank): 包含每個頁面當前的排序值

計算步驟如下

1. 每個頁面排序值初始化為1.0
2. 每次迭代，對於頁面 p，向其每個相鄰頁面發送一個值為 rank(p)/numNeighbors(p) 的貢獻值
3. 將每個頁面的排序值設為 0.15 + 0.85 * contributionsReceived

最後兩個步驟重複數次

```scala
import org.apache.spark

val pages = sc.parallelize(List(("A", Seq("B", "D", "E")), ("B", Seq("A", "C", "E")), ("C", Seq("A", "D")), ("D", Seq("E")), ("E", Seq())))
val links = pages.partitionBy(new spark.HashPartitioner(2)).persist

var ranks = links.mapValues(v => 1.0)

for (i <- 1 to 10) {
  val contributions = links.join(ranks).flatMap {
    case (pageId, (links, rank)) =>
      links.map(dest => (dest, rank / links.size))
  }
  ranks = contributions.reduceByKey((x, y) => x + y).mapValues(v => 0.15 + 0.85*v)
}

ranks.foreach(println)
```

```
(B,0.23785017429055144)
(D,0.3302947700117511)
(A,0.3098696041893569)
(C,0.21742500846815718)
(E,0.586211539592435)
```
