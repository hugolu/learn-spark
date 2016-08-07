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
### 下載
## 匯入資料
## 查看資料

## ALS.train
### 訓練模型
### 進行推薦
### 顯示推薦

## 建立專案
### 建立 Recommend.scala
### 執行 Recommend.scala
### 建立 AlsEvaluation.scala - 調校訓練參數
### 執行 AlsEvaluation.scala
### 設定最佳參數組合

