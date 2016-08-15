# 決策樹迴歸分析

## 應用情境
bike sharing 系統 (類似u-bike) 可在某處租借，另一處歸還。他們的管理階層希望運用大數據分析提供更好的服務。

### 分析問題
大數據分析師與營運人員討論後，發現不同天氣狀態下，租用數量差異很大。營運人員無法提供足夠數量、或不知道何時進行維修。因此希望能夠預測某種天氣情況下的租借數量。
- 車需要維修時 - 可以選在租用數量少的時間
- 租用數量大時 - 可以提供更多數量，增加營業額

### 設計模型
可能影響租用數量的因素：

欄位 | 說明
-----|-----
特徵 | 季節、月份、時間(0~23)、假日、星期、工作日、天氣、溫度、體感溫度、濕度、風速
標籤 | 每小時租用數量

### 收集資料
- 每小時租用數量可在 bike sharing 公司電腦系統查詢
- 季節、月份、時間(0~23)、假日、星期、工作日，可從提供天氣歷史資料的公司取得

### 分析資料
大數據分析師決定使用「決策樹回歸分析」(Decision Regression) 來建立模型、訓練、評估、預測資料。

> 與 DecisionTreeMulti 不同的是，DecisionTreeRegression 預測數值 (連續資料)，而 DecisionTreeMulti 預測特定範圍的 label 可能性 (離散資料)

### 其他應用
根據天氣預測的應用很廣泛。例如超商可以根據天氣預測煮關東煮、茶葉蛋、雨衣的銷售量。如果可以預測銷售量，就可以事先準備足夠的數量，增加營業額。也可減少準備太多數量導致浪費食材。

除了超商，餐廳也很適合使用天氣來預測銷售數量。

## Bike Sharing 資料集
資料來源 [Bike Sharing Dataset Data Set](http://archive.ics.uci.edu/ml/datasets/bike+sharing+dataset)

```shell
$ wget http://archive.ics.uci.edu/ml/machine-learning-databases/00275/Bike-Sharing-Dataset.zip
$ unzip Bike-Sharing-Dataset.zip -d data/
Archive:  Bike-Sharing-Dataset.zip
  inflating: data/Readme.txt
  inflating: data/day.csv
  inflating: data/hour.csv
```

hour.csv 欄位介紹

欄位 | 說明 | 處理方式
-----|------|----------
instant     | 序號 ID | 忽略
dteday      | 日期 | 忽略
season      | 氣節 (1:春, 2: 夏, 3: 秋, 4: 冬) | 特徵欄位
yr          | 年份 (0:2011, 1: 2012, ...) | 忽略
mnth        | 月份 (1~12) | 特徵欄位
hr          | 時間 (0~23) | 特徵欄位
holiday     | 假日 (0: 非假日, 1: 假日) | 特徵欄位
weekday     | 星期 | 特徵欄位
workingday  | 工作日 (非週末&非假日) | 特徵欄位
weathersit  | 天氣: <ol><li>Clear, Few clouds, Partly cloudy</li><li>Mist + Cloudy, Mist + Broken clouds, Mist + Few clouds, Mist</li><li>Ligit snow, Light Rain + Thunderstorm + Scattered clouds, Light Rain + Scattered clouds</li><li>Heavy Rain + Ice Pallets + Thunderstorm + Mist Snow + Fog</li></ol> | 特徵欄位
temp        | 攝氏溫度，將除以 41 進行標準化 | 特徵欄位
atemp       | 體感溫度，將溫度除以 50 進行標準化 | 特徵欄位
hum         | 濕度，將濕度除以 100 進行標準化 | 特徵欄位
windspeed   | 風速，將風速除以67進行標準化 | 特徵欄位
casual      | 臨時使用者：此時租借的數目 | 忽略
registered  | 已註冊會員：此時租借的數目 | 忽略
cnt         | 此時租借總數量 cnt = casual + registered | 標籤欄位 (預測目標)

## 建立 RunDecisionTreeRegression.scala
source: [Classification/src/main/scala/RunDecisionTreeRegression.scala](Classification/src/main/scala/RunDecisionTreeRegression.scala)

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

### evaluateMode
```scala
  def evaluateModel(model: DecisionTreeModel, validationData: RDD[LabeledPoint]): Double = {
    val scoreAndLabels = validationData.map { data =>
      var predict = model.predict(data.features)
      (predict, data.label)
    }
    val metrics = new RegressionMetrics(scoreAndLabels)
    val rmse = metrics.rootMeanSquaredError
    rmse
  }
```

#### 決策樹二元分類以 AUC 做為評估
```scala
val metrics = new BinaryClassificationMetrics(scoreAndLabels)
val auc = metrics.areaUnderROC
```
- auc 越高越好

#### 決策樹回歸分析以 RMSE (root mean square error) 計算誤差平均值
```scala
val metrics = new RegressionMetrics(scoreAndLabels)
val rmse = metrics.rootMeanSquaredError
```
- rmse 越低越好

### 調校最佳模型
```shell
$ sbt package
$ spark-submit --class RunDecisionTreeRegression --jars lib/joda-time-2.9.4.jar target/scala-2.11/classification_2.11-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 17379 筆
資料分成 trainData: 13791, validationData: 1810, testData = 1778
====== 訓練評估 ======
參數 impurity=variance, maxDepth= 3, maxBins=  3 ==> RMSE=134.55, time=4994ms
參數 impurity=variance, maxDepth= 3, maxBins=  5 ==> RMSE=138.88, time=1415ms
參數 impurity=variance, maxDepth= 3, maxBins= 10 ==> RMSE=126.97, time=758ms
參數 impurity=variance, maxDepth= 3, maxBins= 50 ==> RMSE=124.19, time=712ms
參數 impurity=variance, maxDepth= 3, maxBins=100 ==> RMSE=124.19, time=689ms
參數 impurity=variance, maxDepth= 5, maxBins=  3 ==> RMSE=128.78, time=690ms
參數 impurity=variance, maxDepth= 5, maxBins=  5 ==> RMSE=127.48, time=944ms
參數 impurity=variance, maxDepth= 5, maxBins= 10 ==> RMSE=114.80, time=634ms
參數 impurity=variance, maxDepth= 5, maxBins= 50 ==> RMSE=111.84, time=656ms
參數 impurity=variance, maxDepth= 5, maxBins=100 ==> RMSE=111.84, time=580ms
參數 impurity=variance, maxDepth=10, maxBins=  3 ==> RMSE=124.34, time=1260ms
參數 impurity=variance, maxDepth=10, maxBins=  5 ==> RMSE=116.35, time=1106ms
參數 impurity=variance, maxDepth=10, maxBins= 10 ==> RMSE=90.78, time=1053ms
參數 impurity=variance, maxDepth=10, maxBins= 50 ==> RMSE=81.58, time=1081ms
參數 impurity=variance, maxDepth=10, maxBins=100 ==> RMSE=81.06, time=1286ms
參數 impurity=variance, maxDepth=15, maxBins=  3 ==> RMSE=128.14, time=1866ms
參數 impurity=variance, maxDepth=15, maxBins=  5 ==> RMSE=133.74, time=2240ms
參數 impurity=variance, maxDepth=15, maxBins= 10 ==> RMSE=97.02, time=2575ms
參數 impurity=variance, maxDepth=15, maxBins= 50 ==> RMSE=86.73, time=3043ms
參數 impurity=variance, maxDepth=15, maxBins=100 ==> RMSE=86.88, time=2769ms
參數 impurity=variance, maxDepth=20, maxBins=  3 ==> RMSE=128.67, time=1984ms
參數 impurity=variance, maxDepth=20, maxBins=  5 ==> RMSE=143.14, time=2987ms
參數 impurity=variance, maxDepth=20, maxBins= 10 ==> RMSE=106.78, time=3562ms
參數 impurity=variance, maxDepth=20, maxBins= 50 ==> RMSE=92.97, time=4721ms
參數 impurity=variance, maxDepth=20, maxBins=100 ==> RMSE=91.31, time=5164ms
最佳參數 impurity=variance, maxDepth=10, maxBins=100, RMSE=81.0578983484941
====== 測試模型 ======
測試最佳模型，結果 RMSE=77.55708308061058
====== 預測資料 ======
共計 17379 筆
特徵 春天, 1月, 4時, 非假日, 六, 非工作日,  晴, 溫度:9度, 體感:14度, 濕度:75, 風速:0 => 預測:4, 實際:1, 誤差:3.7391304347826084
特徵 春天, 1月, 13時, 非假日, 日, 非工作日,  陰, 溫度:14度, 體感:17度, 濕度:66, 風速:8 => 預測:100, 實際:75, 誤差:25.34042553191489
特徵 春天, 1月, 22時, 非假日, 日, 非工作日,  晴, 溫度:9度, 體感:10度, 濕度:44, 風速:19 => 預測:38, 實際:9, 誤差:29.833333333333336
特徵 春天, 1月, 23時, 非假日, 日, 非工作日,  晴, 溫度:9度, 體感:11度, 濕度:47, 風速:11 => 預測:38, 實際:8, 誤差:30.833333333333336
特徵 春天, 1月, 1時, 非假日, 一, 工作日,  晴, 溫度:8度, 體感:8度, 濕度:44, 風速:27 => 預測:4, 實際:2, 誤差:2.333333333333333
特徵 春天, 1月, 14時, 非假日, 一, 工作日,  晴, 溫度:10度, 體感:12度, 濕度:30, 風速:19 => 預測:152, 實際:77, 誤差:75.51298701298703
特徵 春天, 1月, 5時, 非假日, 三, 工作日,  晴, 溫度:9度, 體感:11度, 濕度:47, 風速:11 => 預測:21, 實際:3, 誤差:18.75
特徵 春天, 1月, 17時, 非假日, 三, 工作日,  晴, 溫度:9度, 體感:11度, 濕度:38, 風速:12 => 預測:152, 實際:190, 誤差:37.487012987012974
特徵 春天, 1月, 12時, 非假日, 四, 工作日,  晴, 溫度:10度, 體感:14度, 濕度:35, 風速:0 => 預測:152, 實際:84, 誤差:68.51298701298703
特徵 春天, 1月, 0時, 非假日, 六, 非工作日,  陰, 溫度:7度, 體感:9度, 濕度:51, 風速:11 => 預測:25, 實際:25, 誤差:0.4499999999999993
===== 完成 ======
```
- 最佳模型參數 impurity=variance, maxDepth=10, maxBins=100, 訓練 RMSE=81.0578983484941 與評估 RMSE=77.55708308061058 相差不大，無 overfitting

### 測試最佳模型
```shell
$ spark-submit --class RunDecisionTreeRegression --jars lib/joda-time-2.9.4.jar target/scala-2.11/classification_2.11-1.0.0.jar variance 10 100
```
