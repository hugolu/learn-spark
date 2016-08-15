# 邏輯迴歸二元分類

## 羅輯回歸分析介紹

### 簡單迴歸分析 (Simple Regression Analysis)
假設變數 y 是自變數 x 所構成的某種現性函數，再加上一個誤差值所得到的值: y = b<sub>0</sub> + b<sub>1</sub>x

如果線性迴歸中「應變數」不是連續變項，而是二分變項，例如[是][否]得到疾病，就要使用 logistic regression。

將 y = b<sub>0</sub> + b<sub>1</sub>x 轉換成 Sigmoid 函數，用來界定某個資料的類別: 

![](https://wikimedia.org/api/rest_v1/media/math/render/svg/a26a3fa3cbb41a3abfe4c7ff88d47f0181489d13)
其中 t = b<sub>0</sub> + b<sub>1</sub>x

![](https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Logistic-curve.svg/600px-Logistic-curve.svg.png)

透過 sigmoid 算出來的值 p (probability) 如果大於 0.5，則歸類為「是」，反之則為「否」

### 複迴歸 (Multiple Regression Analysis)
複迴歸使用超過一個自變量，公式: y = b<sub>0</sub> + b<sub>1</sub>x<sub>1</sub> + b<sub>2</sub>x<sub>2</sub> + ... + b<sub>n</sub>x<sub>n</sub>

轉換成 Sigmoid 函數，t = b<sub>0</sub> + b<sub>1</sub>x<sub>1</sub> + b<sub>2</sub>x<sub>2</sub> + ... + b<sub>n</sub>x<sub>n</sub>

## RunLogisticRegressionWithSGDBinary.scala
source: [Classification/src/main/scala/RunLogisticRegressionWithSGDBinary.scala](Classification/src/main/scala/RunLogisticRegressionWithSGDBinary.scala)

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
  
  //-- 進行資料標準化
  val featuresData = labelpointRDD.map(labelpoint => labelpoint.features)
  val stdScaler = new StandardScaler(withMean = true, withStd = true).fit(featuresData)
  val scaledRDD = labelpointRDD.map(labelpoint => LabeledPoint(labelpoint.label, stdScaler.transform(labelpoint.features)))

...
}
```
- 因為數值特徵值欄位單位不同，數字差異大，無法相比較。所以使用標準化，讓特徵值有共同的標準
  - `stdScaler = new StandardScaler(withMean = true, withStd = true).fit(featuresData)` 用 features 得到適合的 scaler
  - `stdScaler.transform(labelpoint.features)` 再用這個 scaler 把所有 features 標準化

### `LogisticRegressionWithSGD.train(input, numIterations, stepSize, miniBatchFraction)`
`LogisticRegressionWithSGD` 使用 Stochastic gradient decent (SGD) 梯度下降方法求得最佳解

參數 | 型態 | 說明
-----|------|------
`input`             | `RDD[LabeledPoint]` | 輸入的訓練資料
`numIterations`     | `Int`               | 使用 SGD 迭代次數，預設為 100
`stepSize`          | `Double`            | 每次迭代步伐大小，預設為 1
`miniBatchFraction` | `Double`            | 每次迭代參與計算的樣本比例，數值為 0~1，預設為 1

### 調校最佳模型
```shell
$ sbt package
$ spark-submit --class RunLogisticRegressionWithSGDBinary --jars lib/joda-time-2.9.4ar target/scala-2.11/classification_2.11-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 7395 筆
資料分成 trainData: 5901, validationData: 766, testData = 728
====== 訓練評估 ======
numIterations=  5, stepSize= 10, miniBatchFraction=0.5 ==> AUC=0.67, time=2496ms
numIterations=  5, stepSize= 10, miniBatchFraction=0.8 ==> AUC=0.69, time=456ms
numIterations=  5, stepSize= 10, miniBatchFraction=1.0 ==> AUC=0.69, time=401ms
...
最佳參數 numIterations=100, stepSize=10, miniBatchFraction=1.0, AUC=0.70
====== 測試模型 ======
測試結果 AUC=0.663947119924457
====== 預測資料 ======
共計 3171 筆
網址 http://www.lynnskitchenadventures.com/2009/04/homemade-enchilada-sauce.html ==> 預測: 暫時性網頁
網址 http://lolpics.se/18552-stun-grenade-ar ==> 預測: 暫時性網頁
網址 http://www.xcelerationfitness.com/treadmills.html ==> 預測: 暫時性網頁
網址 http://www.bloomberg.com/news/2012-02-06/syria-s-assad-deploys-tactics-of-father-to-crush-revolt-threatening-reign.html ==> 預測: 暫時性網頁
網址 http://www.wired.com/gadgetlab/2011/12/stem-turns-lemons-and-limes-into-juicy-atomizers/ ==> 預測: 暫時性網頁
網址 http://www.latimes.com/health/boostershots/la-heb-fat-tax-denmark-20111013,0,2603132.story ==> 預測: 暫時性網頁
網址 http://www.howlifeworks.com/a/a?AG_ID=1186&cid=7340ci ==> 預測: 暫時性網頁
網址 http://romancingthestoveblog.wordpress.com/2010/01/13/sweet-potato-ravioli-with-lemon-sage-brown-butter-sauce/ ==> 預測: 暫時性網頁
網址 http://www.funniez.net/Funny-Pictures/turn-men-down.html ==> 預測: 暫時性網頁
網址 http://youfellasleepwatchingadvd.com/ ==> 預測: 暫時性網頁
===== 完成 ======
```
- 最佳模型參數 numIterations=100, stepSize=10, miniBatchFraction=1.0, 訓練 AUC=0.70 與評估 AUC=0.663947119924457 相差不大，無 overfitting

### 測試最佳模型
```shell
$ spark-submit --class RunLogisticRegressionWithSGDBinary --jars lib/joda-time-2.9.4.jar target/scala-2.11/classification_2.11-1.0.0.jar 100 10 1.0
```
