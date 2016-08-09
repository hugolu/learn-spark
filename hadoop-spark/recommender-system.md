# 建立推薦引擎

## 推薦演算法

演算法 | 說明
-------|-----
Association Rule | <ul><li>基於關聯式規則的推薦</li><li>消費者購買產品Ａ，有多大機會夠買產品Ｂ</li><li>購物籃分析 (啤酒與尿布)</li></ul> 
Content-based | <ul><li>基於內容的推薦</li><li>分析網頁內容自動分群，再將使用者自動分群</li><li>將新進已分群的網頁推薦給對該群感興趣的使用者</li></ul>
Demographic | <ul><li>人口統計式的推薦</li><li>將使用者依個人屬性 (年齡、性別、教育、居住地、語言) 作為分類基礎</li><li>以此基礎最為推薦的基礎</li></ul>
Collaborative Filtering | <ul><li>協同過濾式的推薦</li><li>透過觀察到所有使用者對產品的評價，來推斷使用者的喜好</li><li>找出與你對產品的評價相近的使用者，他喜歡的產品你多半也會喜歡</li></ul>

這章節討論 Collaborative Filtering 推薦引擎，優缺如下:
- 優點
  - 可以達到個人化推薦
  - 不需內容分析
  - 可以發現使用者新的興趣
  - 自動化程度高
- 缺點
  - 冷啟動問題：如果沒有所以使用者的歷史資料就沒辦法分析
  - 新使用者問題，新使用者沒有評價，就不知道他的喜好

### 「推薦引擎」分析使用情境

有個線上電影院，會員可以付費線上看電影，希望運用推薦引擎增加會員看影片的次數以增加營收...

- 找出問題：現有推薦方式為 Demegraphic，需具備個人屬性資料。但由於隱私權關係，無法收集正確的屬性資料，且屬性相同的人未必有相同的喜好...
- 設計解決模型：使用 Colleborative Filtering，透過觀察所有會員給影片的評價推斷每個會員的喜好，並向會員推薦適合的影片...
- 收集資料：Colleborative Filtering 最大缺點是冷啟動，沒有歷史資料就無法分析、推薦。因為網站已經運作一段時間，累積許多會員對電影的評價與觀看紀錄，所以利用這些資料建構協同過濾式的推薦引擎
- 建立模型：使用 Spark MLlib 的 ALS (Alternating Least Squares) 推薦演算法，解決稀疏矩陣 (sparse matrix) 問題。即使大量使用者與產品，也能在合理時間內完成運算
- 訓練模型：使用歷史資料訓練後，就可以建立模型
- 進行推薦：有了模型，就可以使用模型進行推薦，設計如下推薦功能
  - 針對使用者推薦有興趣的電影 (user-based)：針對每一個會員，定期發送簡訊或e-mail，或登入時推薦他有興趣的電影
  - 恩對電影推薦有興趣的使用者 (item-based)：當要促銷某些電影，找出可能對這些電影有興趣的會員，發送簡訊或e-mail

![](http://www.salemmarafi.com/wp-content/uploads/2014/04/collaborativeFiltering-960x540.jpg)

### ALS 推薦演算法

#### 明確評價 (Explicit Rating)
分級平價 (1~5顆星)

      | item1 | item2 | item3 | item4 | item5
------|-------|-------|-------|-------|-------
user1 | 2     | 1     | 5     |       |
user2 | 1     | 3     | 1     | 1     |
user3 | 3     |       |       | 4     |
user4 | 2     |       | 2     | 1     | 2
user5 | 1     | 1     | 1     | 4     | 1

#### 隱含式評價 (Inplicit Rating)
記錄使用者是否點選某產品，因為使用者可能對產品有興趣，但不知道評價為幾顆星，使用隱含式評價。1 代表對該產品有興趣

      | item1 | item2 | item3 | item4 | item5
------|-------|-------|-------|-------|-------
user1 | 1     | 1     | 1     |       |
user2 | 1     | 1     | 1     | 1     |
user3 | 1     |       |       | 1     |
user4 | 1     |       | 1     | 1     | 1
user5 | 1     | 1     | 1     | 1     | 1

推薦演算法就是找出使用者喜好的相似性。例如 user1 有興趣的項目 (item1,2,3)，與 user2 有興趣的項目 (item1,2,3,4) 類似，當要推薦項目給 user1 時會推薦 item4。

#### 稀疏矩陣 (Sparse Matrix) 的問題
![](http://data-artisans.com/img/blog/factorization.svg) 圖片出處：[Computing Recommendations at Extreme Scale with Apache Flink](http://data-artisans.com/computing-recommendations-at-extreme-scale-with-apache-flink/)

當使用者與項目評價越來越多，發現大部分都是空白，這就是稀疏矩陣 (sparse matrix)。矩陣越大且為空白，計算就要浪費許多記憶體，且花費很多時間。

為了解決稀疏矩陣問題，採用矩陣分解 (Matrix Factorization)。將原本 RatingMatrix (mxn) 分解成 UserMatrix (m x rank) 與 ItemMatrix (rank x n)，且 RatingMatrix ≈ UserMatrix x ItemMatrix。

## ml-100k 推薦資料
[MovieLens](http://grouplens.org/datasets/movielens/) 是一個推薦系統與虛擬社群網站，使用 Colleborative Filtering 向會員推薦電影。

提供不同大小的資料集: [100k](http://files.grouplens.org/datasets/movielens/ml-100k.zip), [1M](http://files.grouplens.org/datasets/movielens/ml-1m.zip), [10M](http://files.grouplens.org/datasets/movielens/ml-10m.zip), [20M](http://files.grouplens.org/datasets/movielens/ml-20m.zip), [Latest-small](http://files.grouplens.org/datasets/movielens/ml-latest-small.zip), [Latest](http://files.grouplens.org/datasets/movielens/ml-latest.zip), [Tag Genome](http://files.grouplens.org/datasets/tag-genome/tag-genome.zip)

### 下載
```shell
$ mkdir -p Recommend/data
$ cd Recommend/data
$ wget http://files.grouplens.org/datasets/movielens/ml-100k.zip
$ unzip -j ml-100k.zip
Archive:  ml-100k.zip
  inflating: allbut.pl
  inflating: mku.sh
  inflating: README
  inflating: u.data
  inflating: u.genre
  inflating: u.info
  inflating: u.item
  inflating: u.occupation
  inflating: u.user
  inflating: u1.base
  inflating: u1.test
  inflating: u2.base
  inflating: u2.test
  inflating: u3.base
  inflating: u3.test
  inflating: u4.base
  inflating: u4.test
  inflating: u5.base
  inflating: u5.test
  inflating: ua.base
  inflating: ua.test
  inflating: ub.base
  inflating: ub.test
```
- `unzip -j` 解壓到當前目錄

u.data: 使用者評價資料
- 欄位：user id, item id, rating, timestamp

u.item: 電影資料
- 欄位：movie id, movie title, release date, video release date, IMDb URL, unknown, Action, Adventure, Animation, Children's, Comedy, Crime, Documentary, Drama, Fantasy, Film-Noir, Horror, Musical, Mystery, Romance, Sci-Fi, Thriller, War, Western
- 只要使用前2項，ps. 最後19項是 genres

### 匯入資料
```shell
$ spark-shell
scala> val rawUserData = sc.textFile("u.data")
rawUserData: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[1] at textFile at <console>:21
```
### 查看資料
```scala
scala> rawUserData.first
res0: String = 196	242	3	881250949   # 欄位: user id | item id | rating | timestamp
```

#### 查看前五筆
```scala
scala> rawUserData.take(5).foreach(println)
196	242	3	881250949
186	302	3	891717742
22	377	1	878887116
244	51	2	880606923
166	346	1	886397596
```

#### 評價統計資料
```scala
scala> rawUserData.map(_.split("\t")(2).toDouble).stats
res2: org.apache.spark.util.StatCounter = (count: 100000, mean: 3.529860, stdev: 1.125668, max: 5.000000, min: 1.000000)
```

## ALS.train
### 訓練模型
訓練步驟：
- 將 rawUserData 以 map 轉換成 rawRating
- 將 rawRating 以 map 轉換成 RDD[Rating]
- 使用 ALS.train 進行訓練，建立推薦模型 MatrixFactorizationModel

#### 匯入程式庫
```scala
scala> import org.apache.spark.mllib.recommendation.ALS
scala> import org.apache.spark.mllib.recommendation.Rating
```

#### 讀取 rawUserData
```scala
scala> val rawUserData = sc.textFile("u.data")
scala> val rawRatings = rawUserData.map(_.split("\t").take(3))
```

#### 準備訓練資料
```scala
scala> val ratingsRDD = rawRatings.map{ case Array(user, movie, rating) => Rating(user.toInt, movie.toInt, rating.toDouble) }
```
- 把 rawRatings 的元素 `Array[String, String, String]` 轉換成 `Array[Int, Int, Double]`，以便後續 ALS.train 使用

#### 進行模型訓練
```scala
scala> val model = ALS.train(ratingsRDD, 10, 10, 0.01)
model: org.apache.spark.mllib.recommendation.MatrixFactorizationModel = org.apache.spark.mllib.recommendation.MatrixFactorizationModel@72db43a7
```

##### 評價方式
明確評估訓練：
- `ALS.train(rating: RDD[Rating], rank: Int, iterations: Int, lambda: Double): MatrixFactorizationModel`

隱式評估訓練：
- `ALS.trainImplicit(rating: RDD[Rating], rank: Int, iterations: Int, lambda: Double): MatrixFactorizationModel`

參數 | 型別 | 說明
-----|------|------
`Ratings`     | Int     | 訓練格式為 `Rating(userID, productID, rating)` 的 RDD
`rank`        | Int     | 執行矩陣分解，將原本 A(m x n)分解成 X(m x rank) 與 Y(rank x n)
`iterations`  | Int     | ALS 演算法重複計算次數 (建議值 10~20)
`lambda`      | Double  | 建議值 0.01

#####  訓練結果 MatrixFactorizationModel

成員| 型別 | 說明
----|------|------
`rank`            | Int                       | 分解的參數
`userFeatures`    | RDD[(Int, Array[Double])  | 分解後用戶矩陣 X(m x rank)
`productFeatures` | RDD[(Int, Array[Double])  | 分解後產品矩陣 Y(rank x n)

### 進行推薦
#### 針對使用者推薦電影
針對使用者 196 推薦前 5 部電影
```scala
scala> model.recommendProducts(195, 5).mkString("\n")
res1: String =
Rating(195,1643,6.3138021884694435)
Rating(195,1449,5.725196779932537)
Rating(195,318,5.114167815408342)
Rating(195,1463,5.097048078430008)
Rating(195,1169,5.04607411159459)
```

`MatrixFactorizationModel.recommendProduct(user: Int, num: Int): Array[Rating]`: 輸入參數 user，針對此 user 推薦有可能興趣的產品

參數 | 型別 | 說明
-----|------|-------
`user`  | Int  | 被推薦的 user id
`num`   | Int  | 推薦筆數

回傳 `Array(user: Int, product: Int, rating: Double)`
- 每筆記錄都是系統推薦產品，rating 是系統給的評分
- rating越高，表示系統越推薦此產品
- 回傳陣列依 rating 排序 (大到小)

#### 查看對使用者推薦產品的評分
```scala
scala> model.predict(196, 464)
res2: Double = 4.2454766877731345
```

#### 針對電影推薦給使用者
```scala
scala> model.recommendUsers(464, 5).mkString("\n")
res3: String =
Rating(219,464,9.911530653128569)
Rating(316,464,8.198440132803974)
Rating(88,464,8.025521138674367)
Rating(471,464,7.887180460483838)
Rating(153,464,7.844976676398668)
```

`MatrixFactorizationModel.recommendUser(product: Int, num: Int): Array[Rating]`: 輸入參數 product，針對此電影找出可能感興趣的會員

參數 | 型別 | 說明
-----|------|-------
`product` | Int  | 被推薦的 product id
`num`     | Int  | 推薦筆數

### 顯示推薦
#### 建立電影 ID 與名稱的對照表
```scala
scala> val itemRDD = sc.textFile("u.item")
scala> val movieTitle = itemRDD.map(line => line.split("\\|").take(2)).map(array => (array(0).toInt, array(1))).collectAsMap()
movieTitle: scala.collection.Map[Int,String] = Map(137 -> Big Night (1996), 891 -> Bent (1997), 550 -> Die Hard: With a Vengeance (1995), 1205 -> Secret Agent, The (1996), 146 -> Unhook the Stars (1996), 864 -> My Fellow Americans (1996), 559 -> Interview with the Vampire (1994), 218 -> Cape Fear (1991), 568 -> Speed (1994), 227 -> Star Trek VI: The Undiscovered Country (1991), 765 -> Boomerang (1992), 1115 -> Twelfth Night (1996), 774 -> Prophecy, The (1995), 433 -> Heathers (1989), 92 -> True Romance (1993), 1528 -> Nowhere (1997), 846 -> To Gillian on Her 37th Birthday (1996), 1187 -> Switchblade Sisters (1975), 1501 -> Prisoner of the Mountains (Kavkazsky Plennik) (1996), 442 -> Amityville Curse, The (1990), 1160 -> Love! Valour! Compassion! (1997), 101 -> Heavy Metal (1981), 1196 -...
```
> 如果用“|”作为分隔的话,必须是如下写法,String.split("\\|"),这样才能正确的分隔开,不能用String.split("|")

#### 顯示對照表前 5 筆
```scala
scala> movieTitle.take(5).foreach(println)
(146,Unhook the Stars (1996))
(1205,Secret Agent, The (1996))
(550,Die Hard: With a Vengeance (1995))
(891,Bent (1997))
(137,Big Night (1996))
```

#### 查詢電影名稱
```scala
scala> movieTitle(146)
res6: String = Unhook the Stars (1996)
```
- 查詢 movie id = 146 的電影名稱

#### 顯示前五筆推薦電影名稱
```scala
scala> model.recommendProducts(196, 5).map(rating => (rating.product, movieTitle(rating.product), rating.rating)).foreach(println)
(1643,Angel Baby (1995),12.124441061944287)
(998,Cabin Boy (1994),9.17632698426916)
(1242,Old Lady Who Walked in the Sea, The (Vieille qui marchait dans la mer, La) (1991),8.623231721127286)
(1160,Love! Valour! Compassion! (1997),8.553355395361205)
(634,Microcosmos: Le peuple de l'herbe (1996),8.23342220368146)
```
- 用 `rating.product` 做 key，查詢 `movieTitle` 的 value

## Recommend 專案

#### 建立目錄
```shel
$ mkdir Recommend
$ cd Recommend
```

#### 準備資料
```shell
$ wget http://files.grouplens.org/datasets/movielens/ml-100k.zip
$ unzip ml-100k.zip
```

#### 建立 build.sbt
```shell
$ vi build.sbt
```

build.sbt:
```
name := "Recommend"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.4.0"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.4.0"
```

> 在這裏吃了一點苦頭，因為我的 scala 版本是 2.11.7，就天真的把 build.sbt 裡面的 scalaVersion 設定為 2.11.7，結果在運行 Recommand 時發生 NoSuchMethodError 的錯誤。將 scalaVersion 設定為編譯 spark 的 scalaVersion，就能避免問題發生。細節請參考 [scala代码能够在spark-shell运行，但是不能通过spark-submit提交运行，why？](http://www.zhihu.com/question/34099679)

### 建立 Recommend.scala
```shell
$ mkdir -p src/main/scala
$ vi src/main/scala/Recommand.scala
```

Recommend.scala
```scala
import java.io.File
import scala.io.Source
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd._
import org.apache.spark.mllib.recommendation.{ ALS, Rating, MatrixFactorizationModel }
import scala.collection.immutable.Map

object Recommend {
  def main(args: Array[String]) {
    SetLogger

    println("====== 準備階段 ======")
    val (ratings, movieTitle) = PrepareData()

    println("====== 訓練階段 ======")
    val model = ALS.train(ratings, 5, 20, 0.1)

    println("====== 推薦階段 ======")
    recommand(model, movieTitle)

    println("完成")
  }

  def SetLogger = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("com").setLevel(Level.OFF)
    System.setProperty("spark.ui.showConsoleProgress", "false")
    Logger.getRootLogger().setLevel(Level.OFF)
  }

  def PrepareData(): (RDD[Rating], Map[Int, String]) = {
    //1. 建議用戶評價資料
    val sc = new SparkContext(new SparkConf().setAppName("Recommend").setMaster("local[4]"))
    println("開始讀取用戶評價資料...")
    val DataDir = "ml-100k"
    val rawUserData = sc.textFile(new File(DataDir, "u.data").toString)
    val rawRatings = rawUserData.map(_.split("\t").take(3))
    val ratingsRDD = rawRatings.map{ case Array(user, movie, rating) =>
                                      Rating(user.toInt, movie.toInt, rating.toDouble) }
    println("共計: " + ratingsRDD.count.toString + "筆 ratings")

    //2. 建立電影ID名稱對照表
    println("開始讀取電影資料...")
    val itemRDD = sc.textFile(new File(DataDir, "u.item").toString)
    val movieTitle = itemRDD.map(line => line.split("\\|").take(2))
                            .map(array => (array(0).toInt, array(1)))
                            .collect()
                            .toMap

    //3. 顯示資料筆數
    val numRatings = ratingsRDD.count
    val numUsers = ratingsRDD.map(_.user).distinct.count
    val numMovies = ratingsRDD.map(_.product).distinct.count
    println("共計: ratings: " + numRatings + ", users: " + numUsers + ", movies: " + numMovies)

    return (ratingsRDD, movieTitle)
  }

  def recommand(model: MatrixFactorizationModel, movieTitle: Map[Int, String]) = {
    var choose = ""
    while (choose != "3") {
      println("請選擇要推薦的類型: 1: 針對用戶推薦電影, 2: 針對電影推薦有興趣的用戶, 3: 離開")
      choose = readLine()

      if (choose == "1") {
        print("請輸入用戶ID? ")
        val inputUserID = readLine()
        RecommendMovies(model, movieTitle, inputUserID.toInt)
      } else if (choose == "2") {
        print("請輸入電影ID? ")
        val inputMovieID = readLine()
        RecommendUsers(model, movieTitle, inputMovieID.toInt)
      }
    }
  }

  def RecommendMovies(model: MatrixFactorizationModel, movieTitle: Map[Int, String], inputUserID: Int) = {
    val RecommendMovie = model.recommendProducts(inputUserID, 10)
    println("針對用戶: " + inputUserID + " 推薦以下電影:")
    RecommendMovie.foreach{ r => println("電影: " + movieTitle(r.product) + ", 評價: " + r.rating.toString) }
  }

  def RecommendUsers(model: MatrixFactorizationModel, movieTitle: Map[Int, String], inputMovieID: Int) = {
    val RecommendUser = model.recommendUsers(inputMovieID, 10)
    println("針對電影: " + movieTitle(inputMovieID.toInt) + ", 推薦以下用戶:")
    RecommendUser.foreach{ r => println("用戶: " + r.user + ", 評價: " + r.rating) }
  }

} 
```

函式 | 說明
-----|------
`main`            | 主流程，包含準備資料、訓練模型、進行推薦
`SetLogger`       | 關閉 log & console 資訊
`PrepareData`     | 讀取、分析 ml-100k 資料，建立電影ID與名稱對照表，顯示資料筆數
`recommand`       | 推薦主流程: 推薦用戶電影、推薦電影給有興趣的用戶
`RecommendMovies` | 推薦用戶電影
`RecommendUsers`  | 推薦電影給有興趣的用戶

### 執行 Recommend.scala
```shell
$ sbt compile && sbt package
$ spark-submit --class Recommend target/scala-2.10/recommend_2.10-1.0.jar
====== 準備階段 ======
開始讀取用戶評價資料...
共計: 100000筆 ratings
開始讀取電影資料...
共計: ratings: 100000, users: 943, movies: 1682
====== 訓練階段 ======
====== 推薦階段 ======
請選擇要推薦的類型: 1: 針對用戶推薦電影, 2: 針對電影推薦有興趣的用戶, 3: 離開
1
請輸入用戶ID? 123
針對用戶: 123 推薦以下電影:
電影: World of Apu, The (Apur Sansar) (1959), 評價: 5.377772108106955
電影: Boy's Life 2 (1997), 評價: 5.060697010930745
電影: Aparajito (1956), 評價: 5.035422410155741
電影: Angel Baby (1995), 評價: 5.034419833281095
電影: Boys, Les (1997), 評價: 4.91623207943767
電影: Pather Panchali (1955), 評價: 4.858722578508513
電影: Aiqing wansui (1994), 評價: 4.786641598268988
電影: Anna (1996), 評價: 4.7346614949817765
電影: The Deadly Cure (1996), 評價: 4.679141639367238
電影: Some Mother's Son (1996), 評價: 4.669613077521147
請選擇要推薦的類型: 1: 針對用戶推薦電影, 2: 針對電影推薦有興趣的用戶, 3: 離開
2
請輸入電影ID? 321
針對電影: Mother (1996), 推薦以下用戶:
用戶: 252, 評價: 3.95371364523988
用戶: 118, 評價: 3.927088466299569
用戶: 592, 評價: 3.899087235034477
用戶: 810, 評價: 3.867388861372387
用戶: 173, 評價: 3.8608321335302023
用戶: 366, 評價: 3.845314771980446
用戶: 923, 評價: 3.8342164099830036
用戶: 16, 評價: 3.827473253784422
用戶: 808, 評價: 3.825115441218599
用戶: 688, 評價: 3.8209388865595297
請選擇要推薦的類型: 1: 針對用戶推薦電影, 2: 針對電影推薦有興趣的用戶, 3: 離開
3
完成
```

### 建立 AlsEvaluation.scala - 調校訓練參數
```scala
import java.io.File
import scala.io.Source
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd._
import org.apache.spark.mllib.recommendation.{ ALS, Rating, MatrixFactorizationModel }
import org.joda.time.format._
import org.joda.time._
import org.joda.time.ReadableInstant
import org.joda.time.Duration

object AlsEvaluation {
  def main(args: Array[String]) {
    SetLogger

    println("====== 資料準備階段 ======")
    val (trainData, validationData, testData) = PrepareData()
    trainData.persist()
    validationData.persist()
    testData.persist()

    println("====== 訓練驗證階段 ======")
    val bestModel = trainValidation(trainData, validationData)

    println("====== 測試階段 =====")
    val testRmse = computeRMSE(bestModel, testData)

    println("使用 testData 測試 bestModel, 結果 RMSE = " + testRmse)

    trainData.unpersist()
    validationData.unpersist()
    testData.unpersist()
  }

  def SetLogger = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("com").setLevel(Level.OFF)
    System.setProperty("spark.ui.showConsoleProgress", "false")
    Logger.getRootLogger().setLevel(Level.OFF)
  }

  def PrepareData(): (RDD[Rating], RDD[Rating], RDD[Rating]) = {
    // 1. 建立用戶評價資料
    val sc = new SparkContext(new SparkConf().setAppName("Recommend").setMaster("local[4]"))
    println("開始讀取用戶評價資料...")
    val DataDir = "ml-100k"
    val rawUserData = sc.textFile(new File(DataDir, "u.data").toString)
    val rawRatings = rawUserData.map(_.split("\t").take(3))
    val ratingsRDD = rawRatings.map{ case Array(user, movie, rating) =>
                                      Rating(user.toInt, movie.toInt, rating.toDouble) }
    println("共計: " + ratingsRDD.count.toString + "筆 ratings")

    //2. 建立電影ID與名稱對照表
    println("開始讀取電影資料...")
    val itemRDD = sc.textFile(new File(DataDir, "u.item").toString)
    val movieTitle = itemRDD.map(line => line.split("\\|").take(2))
                            .map(array => (array(0).toInt, array(1)))
                            .collect()
                            .toMap

    //3. 顯示資料數
    val numRatings = ratingsRDD.count
    val numUsers = ratingsRDD.map(_.user).distinct.count
    val numMovies = ratingsRDD.map(_.product).distinct.count
    println("共計: ratings: " + numRatings + ", users: " + numUsers + ", movies: " + numMovies)

    //4. 以隨機方式將資料分成三份並回傳 train : validation : test = 8 : 1 : 1
    val Array(trainData, validationData, testData) = ratingsRDD.randomSplit(Array(0.8, 0.1, 0.1))
    println(s"train: ${trainData.count}, validation: ${validationData.count}, test: ${testData.count}")

    (trainData, validationData, testData)
  }

  def trainValidation(trainData: RDD[Rating], validationData: RDD[Rating]): MatrixFactorizationModel = {
    println("---- 評估 rank 參數 ----")
    evaluateParameter(trainData, validationData, "rank", Array(5, 10, 15, 20, 50, 100), Array(10), Array(0.1))

    println("---- 評估 numIterations 參數 ----")
    evaluateParameter(trainData, validationData, "numIterations", Array(10), Array(5, 10, 15, 20, 25), Array(0.1))

    println("---- 評估 lambda 參數 ----")
    evaluateParameter(trainData, validationData, "lambda", Array(10), Array(10), Array(0.05, 0.1, 1, 5, 10.0))

    println("---- 所有參數交叉評估找出最好的參數組合 ----")
    val bestModel = evaluateAllParameter(trainData, validationData,
                      Array(5, 10, 15, 20, 25),
                      Array(5, 10, 15, 20, 25),
                      Array(0.05, 0.1, 1, 5, 10.0))

    bestModel
  }

  def evaluateParameter(trainData: RDD[Rating], validationData: RDD[Rating], evaluationParameter: String,
                        rankArray: Array[Int], numIterationsArray: Array[Int], lambdaArray: Array[Double]) = {
    for (
      rank <- rankArray;
      numIterations <- numIterationsArray;
      lambda <- lambdaArray
    ) {
      trainModel(trainData, validationData, rank, numIterations, lambda)
    }
  }

  def evaluateAllParameter(trainData: RDD[Rating], validationData: RDD[Rating],
   rankArray: Array[Int], numIterationsArray: Array[Int], lambdaArray: Array[Double]): MatrixFactorizationModel = {
     val Evaluations = for (
        rank <- rankArray;
        numIterations <- numIterationsArray;
        lambda <- lambdaArray
      ) yield {
        val (rmse, time) = trainModel(trainData, validationData, rank, numIterations, lambda)
        (rank, numIterations, lambda, rmse)
      }
    val eval = Evaluations.sortBy(_._4)
    val bestEval = eval(0)

    println(f"最佳model: rank=${bestEval._1}, iterations=${bestEval._2}, lambda=${bestEval._3}%.2f, rmse=${bestEval._4}%.2f")
    val bestModel = ALS.train(trainData, bestEval._1, bestEval._2, bestEval._3)

    bestModel
  }

  def trainModel(trainData: RDD[Rating], validationData: RDD[Rating],
                 rank: Int, iterations: Int, lambda: Double): (Double, Double) = {
    val startTime = new DateTime()
    val model = ALS.train(trainData, rank, iterations, lambda)
    val endTime = new DateTime()
    val rmse = computeRMSE(model, validationData)
    val duration = new Duration(startTime, endTime)
    val time = duration.getStandardSeconds

    println(f"訓練參數: rank=${rank}, iterations=${iterations}, lambda=${lambda}%.2f, rmse=${rmse}%.2f, time=${time}%.2fms")
    (rmse, time)
  }

  def computeRMSE(model: MatrixFactorizationModel, ratingRDD: RDD[Rating]): Double = {
    val num = ratingRDD.count
    val predicatedRDD = model.predict(ratingRDD.map(r => (r.user, r.product)))
    val predictedAndRatings = predicatedRDD.map(p => ((p.user, p.product), p.rating)).join(
      ratingRDD.map(r => ((r.user, r.product), r.rating))).values

    math.sqrt(predictedAndRatings.map(x => (x._1 - x._2) * (x._1 - x._2)).reduce(_+_) / num)
  }

}
```

### 執行 AlsEvaluation.scala
```shell
$ sbt package
$ spark-submit --class AlsEvaluation target/scala-2.10/recommend_2.10-1.0.jar
Exception in thread "main" java.lang.NoClassDefFoundError: org/joda/time/ReadableInstant
	at AlsEvaluation.main(AlsEvaluation.scala)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:606)
	at org.apache.spark.deploy.SparkSubmit$.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:664)
	at org.apache.spark.deploy.SparkSubmit$.doRunMain$1(SparkSubmit.scala:169)
	at org.apache.spark.deploy.SparkSubmit$.submit(SparkSubmit.scala:192)
	at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:111)
	at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
Caused by: java.lang.ClassNotFoundException: org.joda.time.ReadableInstant
	at java.net.URLClassLoader$1.run(URLClassLoader.java:366)
	at java.net.URLClassLoader$1.run(URLClassLoader.java:355)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.net.URLClassLoader.findClass(URLClassLoader.java:354)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:425)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:358)
	... 10 more
```

下載 joda jar
```shell
$ wget https://github.com/JodaOrg/joda-time/releases/download/v2.9.4/joda-time-2.9.4-dist.tar.gz
$ tar zxf joda-time-2.9.4-dist.tar.gz
```

額外指定 joda-time jar
```shell
$ spark-submit --jars joda-time-2.9.4/joda-time-2.9.4.jar --class AlsEvaluation target/scala-2.10/recommend_2.10-1.0.jar
====== 資料準備階段 ======
開始讀取用戶評價資料...
共計: 100000筆 ratings
開始讀取電影資料...
共計: ratings: 100000, users: 943, movies: 1682
train: 80116, validation: 9975, test: 9909
====== 訓練驗證階段 ======
---- 評估 rank 參數 ----
訓練參數: rank=5, iterations=10, lambda=0.10, rmse=0.93, time=9.00ms
訓練參數: rank=10, iterations=10, lambda=0.10, rmse=0.93, time=3.00ms
訓練參數: rank=15, iterations=10, lambda=0.10, rmse=0.93, time=3.00ms
訓練參數: rank=20, iterations=10, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=50, iterations=10, lambda=0.10, rmse=0.93, time=5.00ms
訓練參數: rank=100, iterations=10, lambda=0.10, rmse=0.93, time=16.00ms
---- 評估 numIterations 參數 ----
訓練參數: rank=10, iterations=5, lambda=0.10, rmse=0.94, time=1.00ms
訓練參數: rank=10, iterations=10, lambda=0.10, rmse=0.93, time=1.00ms
訓練參數: rank=10, iterations=15, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=10, iterations=20, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=10, iterations=25, lambda=0.10, rmse=0.92, time=3.00ms
---- 評估 lambda 參數 ----
訓練參數: rank=10, iterations=10, lambda=0.05, rmse=0.97, time=1.00ms
訓練參數: rank=10, iterations=10, lambda=0.10, rmse=0.93, time=1.00ms
訓練參數: rank=10, iterations=10, lambda=1.00, rmse=1.38, time=1.00ms
訓練參數: rank=10, iterations=10, lambda=5.00, rmse=3.70, time=1.00ms
訓練參數: rank=10, iterations=10, lambda=10.00, rmse=3.70, time=1.00ms
---- 所有參數交叉評估找出最好的參數組合 ----
訓練參數: rank=5, iterations=5, lambda=0.05, rmse=0.95, time=0.00ms
訓練參數: rank=5, iterations=5, lambda=0.10, rmse=0.94, time=0.00ms
訓練參數: rank=5, iterations=5, lambda=1.00, rmse=1.38, time=0.00ms
訓練參數: rank=5, iterations=5, lambda=5.00, rmse=3.70, time=0.00ms
訓練參數: rank=5, iterations=5, lambda=10.00, rmse=3.70, time=0.00ms
訓練參數: rank=5, iterations=10, lambda=0.05, rmse=0.95, time=1.00ms
訓練參數: rank=5, iterations=10, lambda=0.10, rmse=0.93, time=1.00ms
訓練參數: rank=5, iterations=10, lambda=1.00, rmse=1.38, time=1.00ms
訓練參數: rank=5, iterations=10, lambda=5.00, rmse=3.70, time=1.00ms
訓練參數: rank=5, iterations=10, lambda=10.00, rmse=3.70, time=1.00ms
訓練參數: rank=5, iterations=15, lambda=0.05, rmse=0.95, time=1.00ms
訓練參數: rank=5, iterations=15, lambda=0.10, rmse=0.93, time=1.00ms
訓練參數: rank=5, iterations=15, lambda=1.00, rmse=1.38, time=1.00ms
訓練參數: rank=5, iterations=15, lambda=5.00, rmse=3.70, time=1.00ms
訓練參數: rank=5, iterations=15, lambda=10.00, rmse=3.70, time=1.00ms
訓練參數: rank=5, iterations=20, lambda=0.05, rmse=0.95, time=2.00ms
訓練參數: rank=5, iterations=20, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=5, iterations=20, lambda=1.00, rmse=1.38, time=2.00ms
訓練參數: rank=5, iterations=20, lambda=5.00, rmse=3.70, time=2.00ms
訓練參數: rank=5, iterations=20, lambda=10.00, rmse=3.70, time=2.00ms
訓練參數: rank=5, iterations=25, lambda=0.05, rmse=0.94, time=2.00ms
訓練參數: rank=5, iterations=25, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=5, iterations=25, lambda=1.00, rmse=1.38, time=2.00ms
訓練參數: rank=5, iterations=25, lambda=5.00, rmse=3.70, time=2.00ms
訓練參數: rank=5, iterations=25, lambda=10.00, rmse=3.70, time=2.00ms
訓練參數: rank=10, iterations=5, lambda=0.05, rmse=0.97, time=0.00ms
訓練參數: rank=10, iterations=5, lambda=0.10, rmse=0.94, time=1.00ms
訓練參數: rank=10, iterations=5, lambda=1.00, rmse=1.38, time=0.00ms
訓練參數: rank=10, iterations=5, lambda=5.00, rmse=3.70, time=1.00ms
訓練參數: rank=10, iterations=5, lambda=10.00, rmse=3.70, time=1.00ms
訓練參數: rank=10, iterations=10, lambda=0.05, rmse=0.97, time=1.00ms
訓練參數: rank=10, iterations=10, lambda=0.10, rmse=0.93, time=1.00ms
訓練參數: rank=10, iterations=10, lambda=1.00, rmse=1.38, time=1.00ms
訓練參數: rank=10, iterations=10, lambda=5.00, rmse=3.70, time=1.00ms
訓練參數: rank=10, iterations=10, lambda=10.00, rmse=3.70, time=1.00ms
訓練參數: rank=10, iterations=15, lambda=0.05, rmse=0.97, time=1.00ms
訓練參數: rank=10, iterations=15, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=10, iterations=15, lambda=1.00, rmse=1.38, time=1.00ms
訓練參數: rank=10, iterations=15, lambda=5.00, rmse=3.70, time=2.00ms
訓練參數: rank=10, iterations=15, lambda=10.00, rmse=3.70, time=1.00ms
訓練參數: rank=10, iterations=20, lambda=0.05, rmse=0.97, time=2.00ms
訓練參數: rank=10, iterations=20, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=10, iterations=20, lambda=1.00, rmse=1.38, time=2.00ms
訓練參數: rank=10, iterations=20, lambda=5.00, rmse=3.70, time=3.00ms
訓練參數: rank=10, iterations=20, lambda=10.00, rmse=3.70, time=2.00ms
訓練參數: rank=10, iterations=25, lambda=0.05, rmse=0.97, time=2.00ms
訓練參數: rank=10, iterations=25, lambda=0.10, rmse=0.93, time=3.00ms
訓練參數: rank=10, iterations=25, lambda=1.00, rmse=1.38, time=3.00ms
訓練參數: rank=10, iterations=25, lambda=5.00, rmse=3.70, time=4.00ms
訓練參數: rank=10, iterations=25, lambda=10.00, rmse=3.70, time=4.00ms
訓練參數: rank=15, iterations=5, lambda=0.05, rmse=0.97, time=1.00ms
訓練參數: rank=15, iterations=5, lambda=0.10, rmse=0.93, time=1.00ms
訓練參數: rank=15, iterations=5, lambda=1.00, rmse=1.38, time=1.00ms
訓練參數: rank=15, iterations=5, lambda=5.00, rmse=3.70, time=1.00ms
訓練參數: rank=15, iterations=5, lambda=10.00, rmse=3.70, time=1.00ms
訓練參數: rank=15, iterations=10, lambda=0.05, rmse=0.98, time=2.00ms
訓練參數: rank=15, iterations=10, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=15, iterations=10, lambda=1.00, rmse=1.38, time=2.00ms
訓練參數: rank=15, iterations=10, lambda=5.00, rmse=3.70, time=1.00ms
訓練參數: rank=15, iterations=10, lambda=10.00, rmse=3.70, time=1.00ms
訓練參數: rank=15, iterations=15, lambda=0.05, rmse=0.98, time=2.00ms
訓練參數: rank=15, iterations=15, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=15, iterations=15, lambda=1.00, rmse=1.38, time=2.00ms
訓練參數: rank=15, iterations=15, lambda=5.00, rmse=3.70, time=2.00ms
訓練參數: rank=15, iterations=15, lambda=10.00, rmse=3.70, time=2.00ms
訓練參數: rank=15, iterations=20, lambda=0.05, rmse=0.98, time=2.00ms
訓練參數: rank=15, iterations=20, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=15, iterations=20, lambda=1.00, rmse=1.38, time=2.00ms
訓練參數: rank=15, iterations=20, lambda=5.00, rmse=3.70, time=2.00ms
訓練參數: rank=15, iterations=20, lambda=10.00, rmse=3.70, time=2.00ms
訓練參數: rank=15, iterations=25, lambda=0.05, rmse=0.99, time=3.00ms
訓練參數: rank=15, iterations=25, lambda=0.10, rmse=0.93, time=3.00ms
訓練參數: rank=15, iterations=25, lambda=1.00, rmse=1.38, time=3.00ms
訓練參數: rank=15, iterations=25, lambda=5.00, rmse=3.70, time=3.00ms
訓練參數: rank=15, iterations=25, lambda=10.00, rmse=3.70, time=3.00ms
訓練參數: rank=20, iterations=5, lambda=0.05, rmse=0.98, time=1.00ms
訓練參數: rank=20, iterations=5, lambda=0.10, rmse=0.93, time=1.00ms
訓練參數: rank=20, iterations=5, lambda=1.00, rmse=1.38, time=1.00ms
訓練參數: rank=20, iterations=5, lambda=5.00, rmse=3.70, time=1.00ms
訓練參數: rank=20, iterations=5, lambda=10.00, rmse=3.70, time=1.00ms
訓練參數: rank=20, iterations=10, lambda=0.05, rmse=0.98, time=1.00ms
訓練參數: rank=20, iterations=10, lambda=0.10, rmse=0.93, time=1.00ms
訓練參數: rank=20, iterations=10, lambda=1.00, rmse=1.38, time=1.00ms
訓練參數: rank=20, iterations=10, lambda=5.00, rmse=3.70, time=1.00ms
訓練參數: rank=20, iterations=10, lambda=10.00, rmse=3.70, time=1.00ms
訓練參數: rank=20, iterations=15, lambda=0.05, rmse=0.99, time=2.00ms
訓練參數: rank=20, iterations=15, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=20, iterations=15, lambda=1.00, rmse=1.38, time=2.00ms
訓練參數: rank=20, iterations=15, lambda=5.00, rmse=3.70, time=2.00ms
訓練參數: rank=20, iterations=15, lambda=10.00, rmse=3.70, time=2.00ms
訓練參數: rank=20, iterations=20, lambda=0.05, rmse=0.99, time=3.00ms
訓練參數: rank=20, iterations=20, lambda=0.10, rmse=0.93, time=3.00ms
訓練參數: rank=20, iterations=20, lambda=1.00, rmse=1.38, time=3.00ms
訓練參數: rank=20, iterations=20, lambda=5.00, rmse=3.70, time=3.00ms
訓練參數: rank=20, iterations=20, lambda=10.00, rmse=3.70, time=3.00ms
訓練參數: rank=20, iterations=25, lambda=0.05, rmse=0.98, time=4.00ms
訓練參數: rank=20, iterations=25, lambda=0.10, rmse=0.93, time=4.00ms
訓練參數: rank=20, iterations=25, lambda=1.00, rmse=1.38, time=4.00ms
訓練參數: rank=20, iterations=25, lambda=5.00, rmse=3.70, time=4.00ms
訓練參數: rank=20, iterations=25, lambda=10.00, rmse=3.70, time=3.00ms
訓練參數: rank=25, iterations=5, lambda=0.05, rmse=0.98, time=1.00ms
訓練參數: rank=25, iterations=5, lambda=0.10, rmse=0.94, time=1.00ms
訓練參數: rank=25, iterations=5, lambda=1.00, rmse=1.38, time=1.00ms
訓練參數: rank=25, iterations=5, lambda=5.00, rmse=3.70, time=1.00ms
訓練參數: rank=25, iterations=5, lambda=10.00, rmse=3.70, time=1.00ms
訓練參數: rank=25, iterations=10, lambda=0.05, rmse=0.98, time=2.00ms
訓練參數: rank=25, iterations=10, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=25, iterations=10, lambda=1.00, rmse=1.38, time=2.00ms
訓練參數: rank=25, iterations=10, lambda=5.00, rmse=3.70, time=2.00ms
訓練參數: rank=25, iterations=10, lambda=10.00, rmse=3.70, time=2.00ms
訓練參數: rank=25, iterations=15, lambda=0.05, rmse=0.99, time=2.00ms
訓練參數: rank=25, iterations=15, lambda=0.10, rmse=0.93, time=2.00ms
訓練參數: rank=25, iterations=15, lambda=1.00, rmse=1.38, time=3.00ms
訓練參數: rank=25, iterations=15, lambda=5.00, rmse=3.70, time=2.00ms
訓練參數: rank=25, iterations=15, lambda=10.00, rmse=3.70, time=2.00ms
訓練參數: rank=25, iterations=20, lambda=0.05, rmse=0.99, time=3.00ms
訓練參數: rank=25, iterations=20, lambda=0.10, rmse=0.93, time=3.00ms
訓練參數: rank=25, iterations=20, lambda=1.00, rmse=1.38, time=3.00ms
訓練參數: rank=25, iterations=20, lambda=5.00, rmse=3.70, time=3.00ms
訓練參數: rank=25, iterations=20, lambda=10.00, rmse=3.70, time=3.00ms
訓練參數: rank=25, iterations=25, lambda=0.05, rmse=0.98, time=5.00ms
訓練參數: rank=25, iterations=25, lambda=0.10, rmse=0.93, time=4.00ms
訓練參數: rank=25, iterations=25, lambda=1.00, rmse=1.38, time=5.00ms
訓練參數: rank=25, iterations=25, lambda=5.00, rmse=3.70, time=4.00ms
訓練參數: rank=25, iterations=25, lambda=10.00, rmse=3.70, time=4.00ms
最佳model: rank=10, iterations=25, lambda=0.10, rmse=0.93
====== 測試階段 =====
使用 testData 測試 bestModel, 結果 RMSE = 0.9148917187331619
```
- 最佳模型參數組合，rank=10, iterations=25, lambda=0.1, 結果 RMSE=0.93
- 使用測試資料檢驗最佳模型，MRSE=0.9148917187331619，與訓練評估階段RMSE差異不大，沒有overfitting問題

### 設定最佳參數組合
使用最佳模型參數組合訓練模型，rank=10, iterations=25, lambda=0.1
```scala
  def main(args: Array[String]) {
    SetLogger

    println("====== 準備階段 ======")
    val (ratings, movieTitle) = PrepareData()

    println("====== 訓練階段 ======")
    val model = ALS.train(ratings, 10, 25, 0.1)

    println("====== 推薦階段 ======")
    recommand(model, movieTitle)

    println("完成")
  }
```
