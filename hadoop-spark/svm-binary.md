# SVM 二元分類

## SVM 演算法基本概念
SVM (Support Vector Machine) 是用來做分類的工具，在分類過程中尋找具有最大邊緣區 (Maximum Marginal Hyperplane) 的分隔線，因為其具有較高的分類準確性，且有較高的誤差容忍度。

![](http://www.improvedoutcomes.com/docs/WebSiteDocs/image/diagram_svm_maximal_margin.gif)

圖 D 擁有對大邊緣區，對 SVM 而言，是較好的分類。

## 建立 RunSVMWithSGDBinary
source: [Classification/src/main/scala/RunSVMWithSGDBinary.scala](Classification/src/main/scala/RunSVMWithSGDBinary.scala)

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

### `SVMWithSGD.train()`
在 SVMWithSGD 使用 stochastic gradient decent (SGD) 梯度下降法求最佳解

```scala
def trainModel(trainData: RDD[LabeledPoint], numIterations: Int, stepSize: Double, regParam: Double): (SVMModel, Long) = {
  ...
  val model = SVMWithSGD.train(trainData, numIterations, stepSize, regParam)
  ...
}
```

參數 | 型態 | 說明
-----|------|------
`trainData`     | `RDD[LabeledPoint]` | 輸入的訓練資料
`numIterations` | `Int` | 使用 SGD 迭代次數
`stepSize`      | `Double` | 每次執行 SGD 迭代步伐大小
`regParam`      | `Double` | 正規化參數，數值 0~1 之間

### 調校最佳模型
```shell
$ sbt package
$ spark-submit --class RunSVMWithSGDBinary --jars lib/joda-time-2.9.4.jar target/scala-2.11/classification_2.11-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 7395 筆
資料分成 trainData: 5914, validationData: 737, testData = 744
====== 訓練評估 ======
numIterations= 1, stepSize= 10, regParam=0.01 ==> AUC=0.623, time=2256ms
numIterations= 1, stepSize= 10, regParam=0.10 ==> AUC=0.623, time=313ms
numIterations= 1, stepSize= 10, regParam=1.00 ==> AUC=0.623, time=312ms
...
最佳參數 numIterations=25, stepSize=10.0, regParam=0.01, AUC=0.634090909090909
====== 測試模型 ======
測試結果 AUC=0.6429687500000001
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

### 測試最佳模型
```shell
$ spark-submit --class RunSVMWithSGDBinary --jars lib/joda-time-2.9.4.jar target/scala-2.11/classification_2.11-1.0.0.jar 25 10.0 0.01
```
