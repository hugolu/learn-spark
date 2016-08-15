# 單純貝式二元分類

單純貝氏分析 (Naive-Bayes) 是簡單且實用的分析分法。以貝氏定理為基礎，透過機率統計的分析，用以判斷未知類別的資料應該屬於哪一個類別。

## 實例

項目|濕度|氣壓|風向|是否會下雨？
----|----|----|----|------------
1   |<50  |高 |西  |否
2   |51~60|低 |東  |否
3   |51~60|高 |東  |否
4   |>70  |低 |西  |是
5   |61~70|高 |西  |否
6   |61~70|中 |西  |是
7   |51~60|高 |南  |是
8   |51~60|中 |南  |是
9   |61~70|低 |南  |是
10  |<50  |高 |北  |是

### 計算「高氣壓、濕度51~60、西風」，會下雨的機率

機率條件 | 計算方式 | 計算結果
---------|----------|---------
`P(會)` | (會下雨的比數) / (全部筆數) | (6/10)
`P(高|會)` | (高氣壓 & 會下雨的筆數) / (會下雨的筆數) | (2/6)
`P(51~60|會)` | (濕度 51~60 & 會下雨的筆數) / (會下雨的筆數) | (2/6)
`P(西|會)` | (西風 & 會下雨的筆數) / (會下雨的筆數) | (2/6)
`P(會)*P(高|會)*P(51~60|會)*P(西|會)` | (6/10)*(2/6)*(2/6)*(2/6) | 0.02222

### 計算「高氣壓、濕度51~60、西風」，不會下雨的機率

機率條件 | 計算方式 | 計算結果
---------|----------|---------
`P(不會)` | (不會下雨的比數) / (全部筆數) | (4/10)
`P(高|不會)` | (高氣壓 & 不會下雨的筆數) / (不會下雨的筆數) | (3/4)
`P(51~60|不會)` | (濕度 51~60 & 不會下雨的筆數) / (不會下雨的筆數) | (2/4)
`P(西|不會)` | (西風 & 會下雨的筆數) / (會下雨的筆數) | (2/4)
`P(不會)*P(高|不會)*P(51~60|不會)*P(西|不會)` | (4/10)*(3/4)*(2/4)*(2/4) | 0.075

### 預測結果
「會下雨」機率(=0.02222)，小於「不會下雨」機率(=0.075)，所以單純貝式分類預測新進樣本不會下雨。

## 建立 RunNaiveBayesBinary
source: [Classification/src/main/scala/RunNaiveBayesBinary.scala](Classification/src/main/scala/RunNaiveBayesBinary.scala)

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

### trainMode
```scala
def trainModel(trainData: RDD[LabeledPoint], lambda: Int): (NaiveBayesModel, Long) = {
  ...
  val model = NaiveBayes.train(trainData, lambda)
  ...
}
```

參數 | 型態 | 說明
-----|------|------
`trainData` | `RDD[LabeledPoint]` | 輸入的訓練資料
`lambda`    | `Int` | 設定 lambda 參數

### 調校最佳模型
```shell
$ sbt package
$ spark-submit --class RunNaiveBayesBinary --jars lib/joda-time-2.9.4.jar target/scala-2.11/classification_2.11-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 7395 筆
資料分成 trainData: 5928, validationData: 740, testData = 727
====== 訓練評估 ======
lambda= 1 ==> AUC=0.67, time=1492ms
lambda= 3 ==> AUC=0.67, time=191ms
lambda= 5 ==> AUC=0.67, time=198ms
lambda=15 ==> AUC=0.67, time=139ms
lambda=25 ==> AUC=0.67, time=114ms
最佳參數 lambda=15, AUC=0.6686097475455821
====== 測試模型 ======
測試最佳模型，結果 AUC=0.6434325691268663
====== 預測資料 ======
共計 3171 筆
網址 http://www.lynnskitchenadventures.com/2009/04/homemade-enchilada-sauce.html ==> 預測: 長青網頁
網址 http://lolpics.se/18552-stun-grenade-ar ==> 預測: 長青網頁
網址 http://www.xcelerationfitness.com/treadmills.html ==> 預測: 長青網頁
網址 http://www.bloomberg.com/news/2012-02-06/syria-s-assad-deploys-tactics-of-father-to-crush-revolt-threatening-reign.html ==> 預測: 長青網頁
網址 http://www.wired.com/gadgetlab/2011/12/stem-turns-lemons-and-limes-into-juicy-atomizers/ ==> 預測: 長青網頁
網址 http://www.latimes.com/health/boostershots/la-heb-fat-tax-denmark-20111013,0,2603132.story ==> 預測: 長青網頁
網址 http://www.howlifeworks.com/a/a?AG_ID=1186&cid=7340ci ==> 預測: 長青網頁
網址 http://romancingthestoveblog.wordpress.com/2010/01/13/sweet-potato-ravioli-with-lemon-sage-brown-butter-sauce/ ==> 預測: 長青網頁
網址 http://www.funniez.net/Funny-Pictures/turn-men-down.html ==> 預測: 長青網頁
網址 http://youfellasleepwatchingadvd.com/ ==> 預測: 長青網頁
===== 完成 ======
```
- 最佳模型參數 lambda=15, 訓練 AUC=0.6686097475455821 與評估 AUC=0.6434325691268663 相差不大，無 overfitting

### 測試最佳模型
```shell
$ spark-submit --class RunNaiveBayesBinary --jars lib/joda-time-2.9.4.jar target/scala-2.11/classification_2.11-1.0.0.jar 15
```
