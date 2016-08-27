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
```scala
val dataPoint = data.first
val prediction = lrModel.predict(dataPoint.features)
val trueLabel = dataPoint.label

val predictions = lrModel.predict(data.map(lp => lp.features))
val trueLabels = data.map(lp => lp.label)
```
```
(1.0,0.0)
(1.0,1.0)
(1.0,1.0)
(1.0,1.0)
(1.0,0.0)
(1.0,0.0)
(1.0,1.0)
(1.0,0.0)
(1.0,1.0)
(1.0,1.0)
```
- 預測結果很不準？！

### 評估分類模型的性能
怎麼知道用模型預測效果好不好？應該知道怎麼評估模型性能 (We need to be able to evaluate how well our model performs.) 二元分類使用評估方式包含：
- 預測正確率與錯誤率
- 準確率與召回率曲線下的面積
- ROC曲線下的面積
- F-Measure (書本沒提到？)

#### 預測正確率(Accuracy)、錯誤率 (prediction error)
- 正確率 - 訓練樣本中被正確分類的數目除以總樣本數
- 錯誤率 - 訓練樣本中被錯誤分類的數目除以總樣本數

```scala
val lrTotalCorrect = data.map{ lp => if (lrModel.predict(lp.features) == lp.label) 1 else 0 }.sum   //> 3806.0
val lrAccuracy = lrTotalCorrect / numData                                                           //> 0.5146720757268425

val svmTotalCorrect = data.map{ lp => if (svmModel.predict(lp.features) == lp.label) 1 else 0 }.sum //> 3806.0
val svmAccuracy = svmTotalCorrect / numData                                                         //> 0.5146720757268425

val nbTotalCorrect = data.map{ lp => if (nbModel.predict(lp.features) == lp.label) 1 else 0 }.sum   //> 4293.0
val nbAccuracy = nbTotalCorrect / numData                                                           //> 0.5805273833671399

val dtTotalCorrect = data.map{ lp =>
  val score = dtModel.predict(lp.features)
  val predicted = if (score > 0.5) 1 else 0
  if (predicted == lp.label) 1 else 0
}.sum                                                                                               //> 4794.0
val dtAccuracy = dtTotalCorrect / numData                                                           //> 0.6482758620689655
```

#### 延伸閱讀: [准确率(Accuracy), 精确率(Precision), 召回率(Recall)和F1-Measure](https://argcv.com/articles/1036.c)
```
假如某个班级有男生80人,女生20人,共计100人.目标是找出所有女生.
现在某人挑选出50个人,其中20人是女生,另外还错误的把30个男生也当作女生挑选出来了.
作为评估者的你需要来评估(evaluation)下他的工作
```

    |相关(Relevant),正类 | 无关(NonRelevant),负类
----|--------------------|------------------------
被检索到(Retrieved) |	true positives(TP 正类判定为正类,例子中就是正确的判定"这位是女生") |	false positives(FP 负类判定为正类,"存伪",例子中就是分明是男生却判断为女生,当下伪娘横行,这个错常有人犯)
未被检索到(Not Retrieved)	| false negatives(FN 正类判定为负类,"去真",例子中就是,分明是女生,这哥们却判断为男生--梁山伯同学犯的错就是这个)	| true negatives(TN 负类判定为负类,也就是一个男生被判断为男生,像我这样的纯爷们一准儿就会在此处)

![](https://upload.wikimedia.org/wikipedia/commons/thumb/2/26/Precisionrecall.svg/264px-Precisionrecall.svg.png)

#### 精確率 (Precision)、召回率 (Recall)
- 精確率 - 平價結果的品質 `= TP/ TP + FP`
- 召回率 - 平價結果完整性 `= TP/ TP + FN`

![](http://www.chioka.in/wp-content/uploads/2014/04/sample-PR-curve.png)

精確率-召回率 (PR) 曲線 - 表示給定模型隨著決策值得改變，精確率與召回率的對應關係
- PR 曲線線下的面積為平均準確率
- 直覺上，PR曲線下的面積為1等價于一個完美的模型，準確率與召回率達到100%
- Recall 越高，Precision 會降低 (全部都猜是女生，當然 Recall=100%，但是 Precision會降到20%)

#### ROC 曲線
- TPR (true positive rate) - 真陽性佔所有正樣本的比例 `= TP / TP + FN`
- FPR (false positive rate) - 偽陽性佔所有負樣本的比例  `= FP / FP + TN`

![](http://www.chioka.in/wp-content/uploads/2014/04/sample-ROC-curve.png)

ROC 曲線表示分類器性能在不同決策域值下 TRP 對 FPR 的折衷 (?)
- 曲線上每個點代表分類器決策函數中不同的域值
- ROC 下的面積表示平均值
- AUC 為1.0 表示一個完美個分類器，0.5 表示一個隨機性能跟用猜的一樣

> Area under PR 與 Area under ROC 的面積經過歸依化 (最小為0, 最大為1)，可以用這些度量方法比較不同參數配置下的模型，甚至可以比較不同的模型。

```scala
val metrics = Seq(lrModel, svmModel).map{ model =>
  val scoreAndLabels = data.map{ lp => (model.predict(lp.features), lp.label) }
  val metrics = new BinaryClassificationMetrics(scoreAndLabels)
  (model.getClass.getSimpleName, metrics.areaUnderPR, metrics.areaUnderROC)
}

val nbMetrics = Seq(nbModel).map{ model =>
  val scoreAndLabels = data.map{ lp =>
    val score = model.predict(lp.features)
    (if (score > 0.5) 1.0 else 0.0, lp.label)
  }
  val metrics = new BinaryClassificationMetrics(scoreAndLabels)
  (model.getClass.getSimpleName, metrics.areaUnderPR, metrics.areaUnderROC)
}

val dtMetrics = Seq(dtModel).map{ model =>
  val scoreAndLabels = data.map{ lp =>
    val score = model.predict(lp.features)
    (if (score > 0.5) 1.0 else 0.0, lp.label)
  }
  val metrics = new BinaryClassificationMetrics(scoreAndLabels)
  (model.getClass.getSimpleName, metrics.areaUnderPR, metrics.areaUnderROC)
}

val allMetrics = metrics ++ nbMetrics ++ dtMetrics
allMetrics.foreach{ case (m, pr, roc) =>
  println(f"$m, Area under PR: ${pr * 100.0}%2.4f%%, Area under ROC: ${roc * 100.0}%2.4f%%")
}
```
```
LogisticRegressionModel, Area under PR: 75.6759%, Area under ROC: 50.1418%
SVMModel, Area under PR: 75.6759%, Area under ROC: 50.1418%
NaiveBayesModel, Area under PR: 68.1003%, Area under ROC: 58.3683%
DecisionTreeModel, Area under PR: 74.3081%, Area under ROC: 64.8837%
```

### 改進模型性能與參數調校

#### 特徵標準化
模型對輸入數據的分佈和規模有一些固有的**假設**，最常見的形式是特徵滿足常態分佈。

先使用 `RowMatrix` 觀察特徵的統計特性：
```scala
val vectors = data.map(lp => lp.features)
val matrix = new RowMatrix(vectors)
val matrixSummary = matrix.computeColumnSummaryStatistics()

matrixSummary.mean        //> [0.41225805299526774,2.76182319198661,0.46823047328613876,0.21407992638350257,0.0920623607189991,0.04926216043908034,2.255103452212025,-0.10375042752143329,0.0,0.05642274498417848,0.02123056118999324,0.23377817665490225,0.2757090373659231,0.615551048005409,0.6603110209601082,30.077079107505178,0.03975659229208925,5716.598242055454,178.75456389452327,4.960649087221106,0.17286405047031753,0.10122079189276531]
matrixSummary.min         //> [0.0,0.0,0.0,0.0,0.0,0.0,0.0,-1.0,0.0,0.0,0.0,0.045564223,-1.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0]
matrixSummary.max         //> [0.0,0.0,0.0,0.0,0.0,0.0,0.0,-1.0,0.0,0.0,0.0,0.045564223,-1.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0]
matrixSummary.variance    //> [0.10974244167559023,74.30082476809655,0.04126316989120245,0.021533436332001124,0.009211817450882448,0.005274933469767929,32.53918714591818,0.09396988697611537,0.0,0.001717741034662896,0.020782634824610638,0.0027548394224293023,3.6837889196744116,0.2366799607085986,0.22433071201674218,415.87855895438463,0.03818116876739597,7.877330081138441E7,32208.11624742624,10.453009045764313,0.03359363403832387,0.0062775328842146995]
matrixSummary.numNonzeros //> [5053.0,7354.0,7172.0,6821.0,6160.0,5128.0,7350.0,1257.0,0.0,7362.0,157.0,7395.0,7355.0,4552.0,4883.0,7347.0,294.0,7378.0,7395.0,6782.0,6868.0,7235.0]
```
- 第二特徵的方差與均值比其他都高 (數據在原始形式下不符合高斯分佈)

為使數據更符合模型假設，可以對每個特徵進行標準化 (standardize) 使得每個特徵的0均值為單位標準差
- 具體作法 - 對每個特徵減去列的均值，然後除以列的標準差以進行縮放 `(x - 𝜇)/sqrt(variance)`

```scala
val scaler = new StandardScaler(withMean=true, withStd=true).fit(vectors)
val scaledData = data.map{ lp => LabeledPoint(lp.label, scaler.transform(lp.features)) }

scaledData.first.features(0)                                                            //> 1.1376473364976751
data.first.features(0) - matrixSummary.mean(0)) / math.sqrt(matrixSummary.variance(0))  //> 1.1376473364976751
```

使用標準化的數據重新訓練**羅輯回歸** (決策樹與樸素貝氏模型不受特徵化影響)
```scala
val lrModelScaled = LogisticRegressionWithSGD.train(scaledData, numIterations)
val lrTotalCorrectScaled = scaledData.map{ lp => if (lrModelScaled.predict(lp.features) == lp.label) 1 else 0 }.sum
val lrAccuracyScaled = lrTotalCorrectScaled / numData
val lrScoredAndLabels = scaledData.map{ lp => (lrModelScaled.predict(lp.features), lp.label) }
val lrMetricsScaled = new BinaryClassificationMetrics(lrScoredAndLabels)
val lrPr = lrMetricsScaled.areaUnderPR
val lrRoc = lrMetricsScaled.areaUnderROC
println(f"${lrModelScaled.getClass.getSimpleName}\n\tAccuracy: ${lrAccuracyScaled * 100}%2.4f%%\n\tarea under PR: ${lrPr * 100}%2.4f%%\n\tarea under ROC: ${lrRoc * 100}%2.4f%%")
//> LogisticRegressionModel
//>        Accuracy: 62.0419%
//>        area under PR: 72.7254%
//>        area under ROC: 61.9663%
```
- AUC: 50% -> 62%

#### 類別特徵

#### 使用正確的數據格式

#### 模型參數 - 線性模型
#### 模型參數 - 決策樹
#### 模型參數 - 樸素貝氏模型
#### 交叉驗證
