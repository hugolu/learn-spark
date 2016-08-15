# 決策樹二元分類

使用決策樹演算法訓練資料，以特徵 (features) 與欄位 (label) 建立決策樹。

- 濕度 < 60
  - 氣壓 < 1：晴
  - 氣壓 > 1：雨
- 濕度 > 60
  - 氣壓 < 1：雨
  - 氣壓 > 1：晴

使用歷史資料執行訓練建立決策樹，決策樹不可能無限成長，必須限制最大分支與深度，所以要設定以下參數：
- maxBins - 節點最大分支數目
- maxDepth - 決策樹最大深度
- Impurity - 分裂節點的方式
  - Gini - 對每種特徵欄位分隔點計算評估，選擇分裂最小的 Gini 指數方式
  - Entropy - 對每種特徵欄位分隔點計算評估，選擇分裂最小的 Entropy 方式

## 建立 RunDecisionTreeBinary
source: [Classification/src/main/scala/RunDecisionTreeBinary.scala](Classification/src/main/scala/RunDecisionTreeBinary.scala)

函數 | 說明
-----|-----
`main`          | 主程式，包含準備資料、訓練模型、測試模型、預測資料
`setLogger`     | 關閉 log & console 訊息
`prepareData`   | 匯入資料，建立 LabeledPoint、講資料分成 train, evaluation, test 三份
`trainEvaluateTunning` | 交差訓練各種參數組合的模型
`trainEvaluate` | 訓練評估流程，包含訓練模型、評估模型
`trainModel`    | 訓練模型
`evaluateModel` | 評估模型
`predictData`   | 使用模型預測資料

### prepareData
```scala
def prepareData(sc: SparkContext): (RDD[LabeledPoint], RDD[LabeledPoint], RDD[LabeledPoint], Map[String, Int]) = {
  ...
  
  //-- 2. 建立 RDD[LabeledPoint]
  val categoriesMap = lines.map{ fields => fields(3)}.distinct.collect().zipWithIndex.toMap
  val labelpointRDD = lines.map{ fields =>
    val trFields = fields.map(_.replaceAll("\"", ""))
    val categoryFeaturesArray = Array.ofDim[Double](categoriesMap.size)
    val categoryIdx = categoriesMap(fields(3))
    categoryFeaturesArray(categoryIdx) = 1
    val numericalFeatures = trFields.slice(4, fields.size - 1).map(d => if (d == "?") 0.0 else d.toDouble)
    val label = trFields(fields.size - 1).toInt
    LabeledPoint(label, Vectors.dense(categoryFeaturesArray ++ numericalFeatures))
  }
  
  ...
}
```
- 欄位 3 (alchemy_category) 是分類特徵欄位，要使用 1-of-k encoding 轉換成分類演算法可以使用的數值欄位 (如果網頁分類有 N 個分類，就要轉換成 N 個數值欄位)
  - `categoriesMap` 找出欄位 3 所有種類，對應成 Map
  - `categoryFeaturesArray` 產生 `categoriesMap.size` 大小的陣列
  - `categoriesMap(fields(3))` 找到某筆記錄，欄位 3 的 index
  - `categoryFeaturesArray(categoryIdx) = 1` 將特定欄位設定為 1

特徵欄位    | 數值欄位1 | 數值欄位2 | 數值欄位3 | 數值欄位4
------------|-----------|-----------|-----------|-----------
Business    | 1 | 0 | 0 | 0
Recreation  | 0 | 1 | 0 | 0
Health      | 0 | 0 | 1 | 0
Sports      | 0 | 0 | 0 | 1

### AUC (Area Under the Curve of ROC) 評估資料模型

二元分類運算法使用 AUC 評估模型好壞。

              | 實際 Positive       | 實際 Negative
--------------|---------------------|-------------------
預測 Positive | True Positive (TP)  | False Positive (FP)
預測 Negative | False Negative (FN) | True Negative (TN)

- 真陽性 TP: 預測為 1，實際為 1
- 偽陽性 FP: 預測為 1，實際為 0
- 偽陰性 FN: 預測為 0，實際為 1
- 真陰性 TN: 預測為 0，實際為 0

TPR (True Positive Rate): 所有實際為 1 的樣本中，被正確的判斷為 1 的比率
- TPR = TP / (TP + FN)

FPR (False Positive Rate): 所有實際為 0 的樣本中，被錯誤的判斷為 1 的比率
- FPR = FP / (FP + TN)

![](http://gim.unmc.edu/dxtests/roccomp.jpg)

有了 TPR 與 FPR 就可以畫出 ROC 曲線，AUC 就是 ROC (Receiver Operating Characteristic) 曲線下的面積，從 AUC 判斷二元分類的優缺。

> 延伸閱讀 [AUC(Area Under roc Curve )计算及其与ROC的关系](http://blog.csdn.net/chjjunking/article/details/5933105)

條件 | 說明
-----|-----
AUC = 1       | 最完美的情況，預測率 100%
0.5 < AUC < 1 | 優於隨機猜測，有預測價值
AUC = 0.5     | 與隨機猜測一樣，沒有預測價值
AUC < 0.5     | 比隨機猜測差，但反向預測就優於隨機猜測

MLlib 提供 BinaryClassificationMetrics 計算 AUC，過程如下
```scala
def evaluateModel(model: DecisionTreeModel, validationData: RDD[LabeledPoint]): Double = {
  val scoreAndLabels = validationData.map { data => (model.predict(data.features), data.label) }
  val metrics = new BinaryClassificationMetrics(scoreAndLabels)
  metrics.areaUnderROC
}
```
- `scoreAndLabels` 建立 (predict結果, 真實label)
- `BinaryClassificationMetrics(scoreAndLabels)` 得到評估 matrics
- 回傳 `metrics.areaUnderROC`

### 調校最佳模型
```shell
$ sbt package
$ spark-submit --class RunDecisionTreeBinary --jars lib/joda-time-2.9.4.jar target/scala-2.11/classification_2.11-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 7395 筆
資料分成 trainData: 5891, validationData: 754, testData = 750
====== 訓練評估 ======
impurity=   gini, maxDepth= 3, maxBins=  3 ==> AUC=0.60, time=8007ms
impurity=   gini, maxDepth= 3, maxBins=  5 ==> AUC=0.62, time=2227ms
impurity=   gini, maxDepth= 3, maxBins= 10 ==> AUC=0.60, time=2515ms
...
最佳參數 impurity=entropy, maxDepth=5, maxBins=5, AUC=0.68
====== 測試階段 ======
測試模型 AUC=0.6808752072054126
====== 預測資料 ======
共計 3171 筆
網址 http://www.lynnskitchenadventures.com/2009/04/homemade-enchilada-sauce.html ==> 預測: 長青網頁
網址 http://lolpics.se/18552-stun-grenade-ar ==> 預測: 暫時性網頁
網址 http://www.xcelerationfitness.com/treadmills.html ==> 預測: 暫時性網頁
網址 http://www.bloomberg.com/news/2012-02-06/syria-s-assad-deploys-tactics-of-father-to-crush-revolt-threatening-reign.html ==> 預測: 暫時性網頁
網址 http://www.wired.com/gadgetlab/2011/12/stem-turns-lemons-and-limes-into-juicy-atomizers/ ==> 預測: 暫時性網頁
網址 http://www.latimes.com/health/boostershots/la-heb-fat-tax-denmark-20111013,0,2603132.story ==> 預測: 暫時性網頁
網址 http://www.howlifeworks.com/a/a?AG_ID=1186&cid=7340ci ==> 預測: 長青網頁
網址 http://romancingthestoveblog.wordpress.com/2010/01/13/sweet-potato-ravioli-with-lemon-sage-brown-butter-sauce/ ==> 預測: 長青網頁
網址 http://www.funniez.net/Funny-Pictures/turn-men-down.html ==> 預測: 暫時性網頁
網址 http://youfellasleepwatchingadvd.com/ ==> 預測: 暫時性網頁
===== 完成 ======
```
- 最佳模型參數 impurity=entropy, maxDepth=5, maxBins=5, 訓練 AUC=0.68 與評估 AUC=0.6808752072054126 相差不大，無 overfitting

### 測試最佳模型
```shell
$ spark-submit --class RunDecisionTreeBinary --jars lib/joda-time-2.9.4.jar target/scala-2.11/classification_2.11-1.0.0.jar entropy 5 5
```
