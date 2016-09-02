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
