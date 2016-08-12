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
```shell
$ vi src/main/scala/RunDecisionTreeRegression.scala
```

RunDecisionTreeRegression.scala:
```scala
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.storage.StorageLevel
import org.apache.spark.rdd._
import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.evaluation._
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.joda.time.format._
import org.joda.time._
import org.joda.time.Duration

object RunDecisionTreeRegression {
  def main(args: Array[String]) {
    SetLogger

    val sc = new SparkContext(new SparkConf().setAppName("DecisionTreeRegression").setMaster("local[4]"))

    println("====== 準備階段 ======")
    val (trainData, validationData, testData) = PrepareData(sc)
    trainData.persist()
    validationData.persist()
    testData.persist()

    println("====== 訓練評估 ======")
    val model = trainEvaluateTunning(trainData, validationData,
      Array("variance"),
      Array(3, 5, 10, 15, 20),
      Array(3, 5, 10, 50, 100))

    println("====== 測試模型 ======")
    val auc = evaluateModel(model, testData)
    println(s"測試最佳模型，結果 AUC=${auc}")

    println("====== 預測資料 ======")
    PredictData(sc, model)

    println("===== 完成 ======")
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

  def PrepareData(sc: SparkContext): (RDD[LabeledPoint], RDD[LabeledPoint], RDD[LabeledPoint]) = {
    //-- 1. 匯入、轉換資料
    println("開始匯入資料")
    val rawDataWithHeader = sc.textFile("data/hour.csv")
    val rawData = rawDataWithHeader.mapPartitionsWithIndex{ (idx, iter) => if (idx == 0) iter.drop(1) else iter }
    println(s"共計 ${rawData.count} 筆")

    //-- 2. 建立 RDD[LabeledPoint]
    val records = rawData.map(line => line.split(","))
    val data = records.map{ fields =>
      val label = fields(fields.size - 1).toInt
      val featureSeason = fields.slice(2,3).map(d => d.toDouble)
      val features = fields.slice(4,fields.size-3).map(d => d.toDouble)
      LabeledPoint(label, Vectors.dense(featureSeason ++ features))
    }

    //-- 3. 以隨機方式將資料份成三份
    val Array(trainData, validationData, testData) = data.randomSplit(Array(0.8, 0.1, 0.1))

    println(s"資料分成 trainData: ${trainData.count}, validationData: ${validationData.count}, testData = ${testData.count}")

    (trainData, validationData, testData)
  }

  def trainEvaluateTunning(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint], impurityArray: Array[String], maxDepthArray: Array[Int], maxBinsArray: Array[Int]): DecisionTreeModel = {
    val evaluationsArray = for {
      impurity <- impurityArray
      maxDepth <- maxDepthArray
      maxBins <- maxBinsArray
    } yield {
      val (model, time) = trainModel(trainData, impurity, maxDepth, maxBins)
      val rmse = evaluateModel(model, validationData)
      println(s"參數 impurity=$impurity, maxDepth=$maxDepth, maxBins=$maxBins, RMSE=$rmse, time=$time")

      (impurity, maxDepth, maxBins, rmse)
    }

    val evaluationsArraySortedAsc = (evaluationsArray.sortBy(_._4))
    val bestEval = evaluationsArraySortedAsc(0)
    println(s"最佳參數 impurity=${bestEval._1}, maxDepth=${bestEval._2}, maxBins=${bestEval._3}, AUC=${bestEval._4}")

    val (model, time) = trainModel(trainData.union(validationData), bestEval._1, bestEval._2, bestEval._3)

    model
  }

  def trainModel(trainData: RDD[LabeledPoint], impurity: String, maxDepth: Int, maxBins: Int): (DecisionTreeModel, Double) = {
    val startTime = new DateTime()
    val model = DecisionTree.trainRegressor(trainData, Map[Int, Int](), impurity, maxDepth, maxBins)
    val endTime = new DateTime()
    val duration = new Duration(startTime, endTime)

    (model, duration.getMillis)
  }

  def evaluateModel(model: DecisionTreeModel, validationData: RDD[LabeledPoint]): Double = {
    val scoreAndLabels = validationData.map { data =>
      var predict = model.predict(data.features)
      (predict, data.label)
    }
    val metrics = new RegressionMetrics(scoreAndLabels)
    val rmse = metrics.rootMeanSquaredError
    rmse
  }

  def PredictData(sc: SparkContext, model: DecisionTreeModel) = {
    //-- 1. 匯入並轉換資料
    val rawDataWithHeader = sc.textFile("data/hour.csv")
    val rawData = rawDataWithHeader.mapPartitionsWithIndex{ (idx, iter) => if (idx == 0) iter.drop(1) else iter }
    println(s"共計 ${rawData.count} 筆")

    //-- 2. 建立預測所需資料
    val Array(pData, oData) = rawData.randomSplit(Array(0.1, 0.9))
    val records = pData.map(line => line.split(","))
    val data = records.take(10).map { fields =>
      val label = fields(fields.size-1).toInt
      val featureSeason = fields.slice(2,3).map(d => d.toDouble)
      val features = fields.slice(4,fields.size-3).map(d => d.toDouble)
      val featuresVectors = Vectors.dense(featureSeason ++ features)
      val dataDesc = (
        {featuresVectors(0) match {
          case 1 => "春"
          case 2 => "夏"
          case 3 => "秋"
          case 4 => "冬" }} + "天, " +
        featuresVectors(1).toInt + "月, " +
        featuresVectors(2).toInt + "時, " +
        {featuresVectors(3) match {
          case 0 => "非假日"
          case 1 => "假日" }} + ", " +
        {featuresVectors(4) match {
          case 0 => "日"
          case 1 => "一"
          case 2 => "二"
          case 3 => "三"
          case 4 => "四"
          case 5 => "五"
          case 6 => "六" }} + ", " +
        {featuresVectors(5) match {
          case 1 => "工作日"
          case 0 => "非工作日" }} + ", " + 
        {featuresVectors(6) match {
          case 1 => "晴"
          case 2 => "陰"
          case 3 => "小雨"
          case 4 => "大雨" }} + ", " +
        "溫度:" + (featuresVectors(7) * 41).toInt + "度, " +
        "體感:" + (featuresVectors(8) * 50).toInt + "度, " +
        "濕度:" + (featuresVectors(9) * 100).toInt + ", " +
        "風速:" + (featuresVectors(10) * 67).toInt + "")
      val predict = model.predict(featuresVectors)
      val result = if (label == predict) "正確" else "錯誤"
      val error = math.abs(label - predict).toString
      println(s"特徵 ${dataDesc} => 預測:${predict.toInt}, 實際:${label.toInt}, 誤差:${error}")
      }
  }

}
```

### 執行 RunDecisionRegression
```shell
$ sbt package
$ spark-submit --class RunDecisionTreeRegression --jars lib/joda-time-2.9.4.jar --driver-memory 1024M target/scala-2.10/classification_2.10-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 17379 筆
資料分成 trainData: 13827, validationData: 1795, testData = 1757
====== 訓練評估 ======
參數 impurity=variance, maxDepth=3, maxBins=3, RMSE=135.3054133904627, time=3686.0
參數 impurity=variance, maxDepth=3, maxBins=5, RMSE=139.610859313469, time=890.0
參數 impurity=variance, maxDepth=3, maxBins=10, RMSE=127.33837245350315, time=517.0
...
最佳參數 impurity=variance, maxDepth=10, maxBins=50, AUC=80.72422988170608
====== 測試模型 ======
測試最佳模型，結果 AUC=78.66417037243221
====== 預測資料 ======
共計 17379 筆
特徵 春天, 1月, 12時, 非假日, 六, 非工作日, 晴, 溫度:17度, 體感:21度, 濕度:77, 風速:19 => 預測:230, 實際:84, 誤差:146.52272727272728
特徵 春天, 1月, 23時, 非假日, 六, 非工作日, 陰, 溫度:18度, 體感:22度, 濕度:88, 風速:19 => 預測:67, 實際:39, 誤差:28.200000000000003
特徵 春天, 1月, 7時, 非假日, 日, 非工作日, 陰, 溫度:16度, 體感:20度, 濕度:76, 風速:12 => 預測:26, 實際:1, 誤差:25.153846153846153
特徵 春天, 1月, 15時, 非假日, 日, 非工作日, 小雨, 溫度:13度, 體感:16度, 濕度:81, 風速:11 => 預測:72, 實際:74, 誤差:1.2727272727272663
特徵 春天, 1月, 11時, 非假日, 一, 工作日, 晴, 溫度:8度, 體感:9度, 濕度:40, 風速:22 => 預測:67, 實際:51, 誤差:16.087248322147644
特徵 春天, 1月, 22時, 非假日, 一, 工作日, 晴, 溫度:5度, 體感:7度, 濕度:69, 風速:8 => 預測:42, 實際:20, 誤差:22.886792452830186
特徵 春天, 1月, 7時, 非假日, 二, 工作日, 晴, 溫度:4度, 體感:7度, 濕度:74, 風速:8 => 預測:127, 實際:94, 誤差:33.875
特徵 春天, 1月, 20時, 非假日, 三, 工作日, 晴, 溫度:9度, 體感:11度, 濕度:47, 風速:11 => 預測:81, 實際:89, 誤差:7.769230769230774
特徵 春天, 1月, 23時, 非假日, 三, 工作日, 晴, 溫度:8度, 體感:12度, 濕度:47, 風速:0 => 預測:21, 實際:19, 誤差:2.7142857142857153
特徵 春天, 1月, 8時, 非假日, 五, 工作日, 晴, 溫度:8度, 體感:9度, 濕度:51, 風速:16 => 預測:261, 實際:210, 誤差:51.1521739130435
===== 完成 ======
```
