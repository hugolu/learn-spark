# 聚類模型

考慮數據沒有標注的情形，具體模型稱作無監督學習，模擬訓練過程沒有被目標標籤監督。

實際應用
- 基於行為特徵或者 meta-data 將用戶或者客戶分成不同組
- 對網站內容或者零售商店中的商品進行分類
-  找到相似的基因的類
- 在生態學中進行群體分割
- 創建圖像分割用於圖像分析的應用，比如物體檢測

## 種類
- K-均值聚類
  - 最小化所有類簇的方差和
- 混合模型
- 層次聚類
  - agglomerative clustering
  - divisive clustering
  
### K-均值聚類
WCSS (Within cluster sum of squared errors): 計算每個類簇中樣本與中心的平方差，並在最後求和

- 將樣本分到 WCSS 最小的類簇中
- 根據前一步類分配情況，重新計算每個類簇的中心

K-均值迭代結束條件為達到最大迭代次數或者收斂。收斂意味者第一步類分類之後沒有改變，因此 WCSS 的值也沒有改變。

## Spark 建構聚類模型
source: [src/ex-7](src/ex-7)

### 從 MovieLens dataset 提取特徵

#### 提取電影數據
```scala
val movies = sc.textFile("../ml-100k/u.item")
println(movies.first)
```
```
1|Toy Story (1995)|01-Jan-1995||http://us.imdb.com/M/title-exact?Toy%20Story%20(1995)|0|0|0|1|1|1|0|0|0|0|0|0|0|0|0|0|0|0|0
```

u.item 欄位：
idx | field name 
----|------------
0   | movie id
1   | movie title 
2   | release date 
3   | video release date 
4   | IMDb URL 
5   | unknown
6   | Action
7   | Adventure
8   | Animation
9   | Children's
10  | Comedy
11  | Crime
12  | Documentary
13  | Drama
14  | Fantasy
15  | Film-Noir
16  | Horror
17  | Musical
18  | Mystery
19  | Romance
20  | Sci-Fi
21  | Thriller
22  | War
23  | Western

> 5~23 共19個 genres (1 表示電影屬於這個 genres, 則否為 0)

雖然有 movie genres 的特徵可以分類，但作者希望透過其他特徵或屬性對電影分類。這裏的想法是，透過用戶評分建立矩陣分解模型，在這個隱式特徵空間中用電影相關的因素幫電影分類。雖然隱式特徵無法直接解釋，但是他們表示一些可以影響用戶對電影評分的隱式結構，例如用戶對題材的偏好、演員、導演或電影的主題。
- 如果將電影的相關因素向量作為類聚模型的輸入，可以得到基於用戶實際評分行為的分類，而不是人工題材分類。
- 如果將打分數的隱式特徵空間中用用戶相關因素表示一個用戶，可以對用戶向量進行分類，得到基於用戶打分數行為的類聚結果。

#### 提取題材數據
```scala
val genres = sc.textFile("../ml-100k/u.genre")
println(genres.collect.mkString(", "))
```
```
unknown|0, Action|1, Adventure|2, Animation|3, Children's|4, Comedy|5, Crime|6, Documentary|7, Drama|8, Fantasy|9, Film-Noir|10, Horror|11, Musical|12, Mystery|13, Romance|14, Sci-Fi|15, Thriller|16, War|17, Western|18,
```

#### 取得題材映射關係
```scala
val genreMap = genres.filter(!_.isEmpty).map(line => line.split("\\|")).map(array => (array(1), array(0))).collectAsMap
println(genreMap)
```
```
Map(2 -> Adventure, 5 -> Comedy, 12 -> Musical, 15 -> Sci-Fi, 8 -> Drama, 18 -> Western, 7 -> Documentary, 17 -> War, 1 -> Action, 4 -> Children's, 11 -> Horror, 14 -> Romance, 6 -> Crime, 0 -> unknown, 9 -> Fantasy, 16 -> Thriller, 3 -> Animation, 10 -> Film-Noir, 13 -> Mystery)
```
- 將來用來解釋 movie 是否為某些 genres

#### 透過 genreMap 解釋 movie 題材
```scala
val titlesAndGenres = movies.map(_.split("\\|")).map{ array =>
  val genres = array.slice(5, array.size)
  val genresAssigned = genres.zipWithIndex.filter{ case (g, idx) => g == "1" }.map{ case (g, idx) => genreMap(idx.toString) }
  (array(0).toInt, (array(1), genresAssigned.mkString("(", ", ", ")")))
}
println(titlesAndGenres.first)
```
```
(1,(Toy Story (1995),(Animation, Children's, Comedy)))
```
- `genres.zipWithIndex.filter{ ... }.map{ ... }` 先找出欄位 5~23 為 1 的欄位，再對應 genreMap 找出題材名稱

### 訓練推薦模型
```scala
val rawData = sc.textFile("../ml-100k/u.data")
val rawRatings = rawData.map(_.split("\t").take(3))
val ratings = rawRatings.map{ case Array(user, movie, rating) => Rating(user.toInt, movie.toInt, rating.toDouble) }
val alsModel = ALS.train(ratings, 50, 10, 0.1)
```
- 得到 alsModel.productFeatures, alsModel.userFeatures，作為聚類模型的輸入

#### 將 productFeatures, userFeatures 轉換成 Vectors
```scala
scala> alsModel.productFeatures
res6: org.apache.spark.rdd.RDD[(Int, Array[Double])] = products MapPartitionsRDD[222] at mapValues at ALS.scala:272

scala> alsModel.userFeatures
res7: org.apache.spark.rdd.RDD[(Int, Array[Double])] = users MapPartitionsRDD[221] at mapValues at ALS.scala:268
```

要轉換成 Vectors 才能當作聚類模型輸入
```scala
val movieFactors = alsModel.productFeatures.map{ case (id, factor) => (id, Vectors.dense(factor)) }
val movieVectors = movieFactors.map(_._2)

val userFactors = alsModel.userFeatures.map{ case (id, factor) => (id, Vectors.dense(factor)) }
val userVectors = userFactors.map(_._2)
```

#### 是否需要歸一化 (normalization)
```scala
val movieMatrix = new RowMatrix(movieVectors)
val movieMatrixSummary = movieMatrix.computeColumnSummaryStatistics()

val userMatrix = new RowMatrix(userVectors)
val userMatrixSummary = userMatrix.computeColumnSummaryStatistics()
```
```scala
(movieMatrixSummary.mean.toArray.min, movieMatrixSummary.mean.toArray.max)          //= (-0.47623446825960364,0.5025996821508916)
(movieMatrixSummary.variance.toArray.min, movieMatrixSummary.variance.toArray.max)  //= (0.0227785374526902,0.05176045879190178)
(userMatrixSummary.mean.toArray.min, userMatrixSummary.mean.toArray.max)            //= (-0.7596118996726696,0.7629363966685025)
(userMatrixSummary.variance.toArray.min, userMatrixSummary.variance.toArray.max)    //= (0.024648487745135637,0.059129161048841965)
```
- 沒有特別的離群值會影響結果，不需要歸一化

### 訓練聚類模型
```scala
val numClusters = 5
val numIterations = 10
val numRuns = 3

val movieClusterModel = KMeans.train(movieVectors, numClusters, numIterations, numRuns, "k-means||", 42)
val userClusterModel = KMeans.train(userVectors, numClusters, numIterations, numRuns, "k-means||", 42)
```

```scala
scala> sc.setLogLevel("INFO")
scala> val movieClusterModelConverged = KMeans.train(movieVectors, numClusters, 100)
...
16/09/02 05:39:33 INFO KMeans: Run 0 finished in 33 iterations
16/09/02 05:39:33 INFO KMeans: Iterations took 3.658 seconds.
16/09/02 05:39:33 INFO KMeans: KMeans converged in 33 iterations.
16/09/02 05:39:33 INFO KMeans: The cost for the best run is 2249.2365442526802.
```
- 第 33 次迭代就已收斂

### 使用聚類模型預測
預測單個電影向量
```scala
val movieCluster = movieClusterModel.predict(movieVectors.first)
println(movieCluster)
```

針對多個輸入進行預測
```scala
val predictions = movieClusterModel.predict(movieVectors)
println(predictions.take(10).mkString(", "))
```

#### 用 MovieLens dataset 解釋類別預測
儘管無監督學習有不需要提供帶標注的訓練數據的優勢，但它的結果需要由人工來解釋

為了解釋電影聚類結果，嘗試觀察每個類簇具有可以解釋的含義
- 選擇距離類簇中心最近的電影，觀察這些電影共有的屬性

定義度量樣本距離的函數
```scala
def computeDistance(v1: DenseVector[Double], v2: DenseVector[Double]) = pow(v1 - v2, 2).sum
```

對算每個電影計算其特徵向量與所屬類簇中心向量的距離
```scala
val titlesWithFactors = titlesAndGenres.join(movieFactors)
val movieAssigned = titlesWithFactors.map{ case (id, ((title, genres), vector)) =>
  val pred = movieClusterModel.predict(vector)
  val clusterCenter = movieClusterModel.clusterCenters(pred)
  val dist = computeDistance(DenseVector(clusterCenter.toArray), DenseVector(vector.toArray))
  (id, title, genres, pred, dist)
}
val clusterAssignments = movieAssigned.groupBy{ case (id, title, genres, cluster, dist) => cluster }.collectAsMap
```
- ` (id, title, genres, pred, dist)`: 電影ID、標題、題材、類別索引、電影特徵向量與類簇中心的距離

列舉每個類簇距離中心最近的10部電影：
```scala
for ( (k, v) <- clusterAssignments.toSeq.sortBy(_._1)) {
  println(s"\nCluster $k: ")
  val m = v.toSeq.sortBy(_._5)
  println(m.take(10).map{ case (_, title, genres, _, d) => (title, genres, d) }.mkString("\n"))
}
```
```
Cluster 0:
(Last Time I Saw Paris, The (1954),(Drama),0.13441192088144988)
(Witness (1985),(Drama, Romance, Thriller),0.2181986360369887)
...

Cluster 1:
(Being Human (1993),(Drama),0.09184959799797433)
(Machine, The (1994),(Comedy, Horror),0.14128451526053637)
...

Cluster 2:
(Angela (1995),(Drama),0.21879105133157503)
(Johns (1996),(Drama),0.33281214321088615)
...

Cluster 3:
(King of the Hill (1993),(Drama),0.15851785324198917)
(Silence of the Palace, The (Saimt el Qusur) (1994),(Drama),0.2548107283459248)
...

Cluster 4:
(Killer: A Journal of Murder (1995),(Crime, Drama),0.35750181191045605)
(Sunchaser, The (1996),(Drama),0.3802884815672308)
...
```

- 我們不能明顯看出每個類簇所表示的內容
- 但證據表明類聚過程會提取電影間的屬性或者相似之處 (這不是基於電影名稱和題材容易看得出來的)

### 評估聚類模型性能

- 內部評價指標
  - 使類簇內部樣本距離盡可能靠近，不同類簇樣本相對較遠
  - WCSS, Davies-Bouldin index, the Dunn Index, silhouette coefficient
- 外部評價指標
  - 如果有一些帶標註的數據，便可以用這些標籤來評估類聚模型
  - Rand measure, F-measure, Jaccard index

#### 在 MovieLens 數據集計算性能
```scala
val movieCost = movieClusterModel.computeCost(movieVectors)   //= 2324.070441831106
val userCost = userClusterModel.computeCost(userVectors)      //= 1467.9665024564836
```

### 類聚性能調優
K-Means 均值模型只有一個可調參數，就是 K (類簇中心的數目)

使用 60/40 劃分訓練集與測試集，並使用 mllib 內建的 WCSS 累方法評估模行性能

#### 電影類聚
```scala
val Array(trainMovies, testMovies) = movieVectors.randomSplit(Array(0.6, 0.4), 123)
trainMovies.cache; testMovies.cache
val costsMovies = Seq(2, 3, 4, 5, 10, 20).map{ k =>
  (k, KMeans.train(trainMovies, numIterations, k, numRuns).computeCost(testMovies))
}
println("Movie clustering cross-validation: ")
costsMovies.foreach{ case (k, cost) => println(f"WCSS for K=$k id $cost%2.2f") }
```
```
Movie clustering cross-validation:
WCSS for K=2 id 913.79
WCSS for K=3 id 898.17
WCSS for K=4 id 906.13
WCSS for K=5 id 901.44
WCSS for K=10 id 893.00
WCSS for K=20 id 914.91
```
- WCSS 隨著 K 增大持續變小，但到某值後下降速度變緩，這時的K值通常為最優 (稱為拐點)
- 儘管較大的K直從數學的角度可以得到更優的解，但是類簇太多就會變得難以理解與解釋

#### 用戶類聚
```scala
val Array(trainUsers, testUsers) = userVectors.randomSplit(Array(0.6, 0.4), 123)
trainUsers.cache; testUsers.cache
val costsUsers = Seq(2, 3, 4, 5, 10, 20).map{ k =>
  (k, KMeans.train(trainUsers, numIterations, k, numRuns).computeCost(testUsers))
}
println("User clustering cross-validation: ")
costsUsers.foreach{ case (k, cost) => println(f"WCSS for K=$k id $cost%2.2f") }
```
```
User clustering cross-validation:
WCSS for K=2 id 566.21
WCSS for K=3 id 565.19
WCSS for K=4 id 564.52
WCSS for K=5 id 562.70
WCSS for K=10 id 558.10
WCSS for K=20 id 557.58
```
