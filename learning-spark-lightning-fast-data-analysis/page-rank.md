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

用 spark 編寫 page rank 很簡單：首先對當前的 ranksRDD 與靜態的 linksRDD 進行一次 `join` 操作，來獲取每個頁面 ID 對應的相鄰頁面列表和當前的排序值，然後用 flatMap 創建出 contributions 來記錄每個頁面對各相鄰頁面的貢獻。再把這些貢獻值按照頁面 id 分別累加起來。

為確保 RDD 以比較高效的方式進行分區，最小化通訊開銷，做了以下事情
1. 對 linksRDD 呼叫 `persist`，將它保留在記憶體以供每次迭代使用
2. linksRDD 是靜態數據集，在程式一開始就對它進行分區操作，所以與 ranksRDD 做 `join` 操作不需通過網路進行數據混洗 (shuffle)
3. 第一次創建 ranksRDD 使用 `mapValues` (不是 `map`) 來保留父 RDD 的分區方式，所以第一次 `join` 的開銷會很小
4. 迭代過程中，在 `reduceByKey` 而後使用 `mapValues`。因為 `reduceByKey` 結果已經是 hash partitioned，這樣一來，下次循環將映射操作的結果再次與 linksRDD 進行 `join` 操作時就會更加高效。
