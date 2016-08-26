# 分類模型

分類通常只將事物分成不同的類別。在分類模型中，期望根據一組特徵來判斷類別，這些特徵代表了物體、事件或是上下文相關的屬性 
(變量)。

最簡單的分類形式是兩個類別，即二分類 (binary classification)。如果不只兩類，則稱為多類別分類 (multiclass classification)。

分類是監督學習的一種形式，用帶有類標記或者類輸出的訓練樣本訓練模型 (通過輸出結果監督被訓練的模型)。

適用場景：
- 預測廣告點擊率 (二分類)
- 預測詐欺 (二分類)
- 預測拖欠貸款 (二分類)
- 對圖片、視頻、聲音分類 (多分類)
- 對新聞、網頁、其他內容標記類別或打標籤 (多分類)
- 發現垃圾郵件、垃圾頁面、網路入侵、惡意行為 (二分類或多分類)
- 檢測故障 (機器或網路)
- 根據顧客或用戶購買商品，或者使用服務的機率進行排序
- 預測顧客或用戶有誰可能停止使用某個產品或服務

## 分類模型
- 線性模型 (Linear model) - 簡單且容易擴展到非常大的數據集
- 決策樹 (Decision tree) - 強大的非線性技術，訓練過程計算量很大且較難擴展，但在很多情況下性能很好
- 樸素貝氏模型 (Naive Bayes model) - 模型簡單、容易訓練，並且具有高效與並行的優點
  - 訓練模型只需遍歷一次所有數據一次
  - 可以當作一個很好的模型測試基準，用於比較其他模型的性能

### 線性模型
- 對輸入（特徵、自變數）應用簡單的線性預測函數
- 對預測結果進行建模

y = f(w<sup>T</sup>x)

- y: 目標變量
- w: 參數向量 (權重向量)
- x: 輸入特徵向量
- (w<sup>T</sup>x): 關於權重向量w與特徵向量x的預測器
- f: 連接函數

給定輸入數據的特徵向量與相關的目標值，存在一個權重向量能夠最好對數據進行擬合，擬合的過程即最小化模型輸出與實際值的誤差。這個過程稱為模型的擬合(fitting)、訓練(trining)、優化(optimization)。

需要找到一個權重向量能夠最小化由損失函數計算出來的損失(誤差)的和。損失函數的輸入是給定訓練樣本的輸入向量、權重向量、實際輸出，函數的輸出為損失。

#### 邏輯回歸 (Logistic Regression)
羅輯回歸是一個概率模型，預測結果的值域是[0, 1]。對於二分法來說，羅輯回歸的輸出等價于模型預測某個數據屬於正類的概率估計。

- 連接函數: 1 / (1 + exp(-w<sup>T</sup>x))
- 損失函數: log(1 + exp(-yw<sup>t</sup>x))

#### 線性支持向量機 (Linear Support Vector Machine)
SVM 與羅輯回歸不同，不是一個概率模型，但可以基於模型對正負的估計預測類別。

- 連接函數: y = w<sup>T</sup>x
- 損失函數: max(0, 1- yw<sup>T</sup>x)

SVM 是一個最大區間的分類氣，它試圖訓練一個使得類別盡可能分開的權重向量。很多分類任務中，SVM 不僅表現性能突出，對大數據集的擴展是線性變化的。

### 決策樹
- 決策樹是一個強大的非概率模型，可以表達複雜的非線性模式與特徵互相關係
- 決策樹容易理解與解釋，可以處理類屬或數值特徵，同時不要求數據輸入歸一化或標準化
- 決策樹很適合應用集成方法 (ensemble method)，比如多個決策樹的集成，稱為決策森林。
- 決策樹是一種自上而下始於跟節點(特徵)的分法，在每一個步驟中通過評估特徵分裂的信息增益，最後選出分割數據集最優個特徵
  - 信息增益通過計算結點不純度，減去分割後兩個子結點不純度的加權和(?)

### 樸素貝氏模型
- 樸素貝氏模型是個概率模型，通過計算給定數據據點在某個類別的概率來進行預測
- 樸素貝氏模型假定每個特徵分配到某個類別的概率是獨立分布的 (各特徵間條件獨立)
  - 屬於某個類別的概率表示為若干概率乘積的函數，這些概率包括某個特徵在給定某個類別條件下出現的概率(條件概率)，以及該類別的概率(先驗概率)。這使得模型訓練非常直接與易於處理
  - 類別的先驗概率和特徵的條件概率可以通過數據的**頻率**估計得到

上述假設非常適合二元特徵 (例如 1-of-k, k 維特徵向量中只有1維為1，其他為0)

## Spark 建構分類模型
source: [src/ex-5](src/ex-5)

### 從數據抽取合適的特徵
下載資料: http://www.kaggle.com/c/stumbleupon/data

#### 刪除第一行的標題
```shell
$ sed 1d train.tsv > train_noheader.tsv
```
- 有時候透過其他工具執行 ETL 比使用 spark 的指令拼湊來得方便有效率

#### 載入資料、分隔欄位
```scala
val rawData = sc.textFile("../data/train_noheader.tsv")
val records = rawData.map(line => line.split("\t"))
```

#### 清理數據、處理缺失數據
```scala
  val data = records.map{ r =>
    val trimmed = r.map(_.replaceAll("\"", ""))
    val label = trimmed(r.size - 1).toInt
    val features = trimmed.slice(4, r.size - 1).map(d => if (d == "?") 0.0 else d.toDouble)
    LabeledPoint(label, Vectors.dense(features))
  }
  data.cache
  val numData = data.count
```
- 拿掉資料裡 `"` 符號
- 將資料中 `?` 取代為 `0`

#### 針對樸素貝氏模型特別處理
```scala
val nbData = records.map{ r =>
  val trimmed = r.map(_.replaceAll("\"", ""))
  val label = trimmed(r.size - 1).toInt
  val features = trimmed.slice(4, r.size - 1).map(d => if (d == "?") 0.0 else d.toDouble).map(d => if (d < 0) 0.0 else d)
  LabeledPoint(label, Vectors.dense(features))
}
```
- 要求特徵值非負值

### 訓練分類模型
```scala
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.tree.DecisionTree

import org.apache.spark.mllib.tree.configuration.Algo
import org.apache.spark.mllib.tree.impurity.{ Impurity, Entropy, Gini }

val numIterations = 10
val maxTreeDepth = 5
```

```scala
val lrModel = LogisticRegressionWithSGD.train(data, numIterations)
val svmModel = SVMWithSGD.train(data, numIterations)
val nbModel = NaiveBayes.train(nbData)
val dtModel = DecisionTree.train(data, Algo.Classification, Entropy, maxTreeDepth)
```
- `lrModel`: 訓練 Logistic Regression Model
- `svmModel`: 訓練 Support Vector Machine
- `nbModel`: 訓練 Naive Bayes Model，使用沒有負數特徵值的數據
- `dtModel`:  訓練 Decision Tree Model

### 使用分類模型

### 評估分類模型的性能

### 改進模型性能與參數調校
