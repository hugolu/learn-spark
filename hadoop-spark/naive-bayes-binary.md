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

## 建立 RunNaiveBayesBinary.scala
```scala
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.storage.StorageLevel
import org.apache.spark.rdd.RDD
import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.evaluation._
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.joda.time._

object RunNaiveBayesBinary {
  def main(args: Array[String]) {
    SetLogger

    val sc = new SparkContext(new SparkConf().setAppName("NavieBayes").setMaster("local[4]"))

    println("====== 準備階段 ======")
    val (trainData, validationData, testData, categoriesMap) = PrepareData(sc)
    trainData.persist()
    validationData.persist()
    testData.persist()

    println("====== 訓練評估 ======")
    val model = trainEvaluateTunning(trainData, validationData, Array(1,3,5,15,25))

    println("====== 測試模型 ======")
    val auc = evaluateModel(model, testData)
    println(s"測試最佳模型，結果 AUC=${auc}")

    println("====== 預測資料 ======")
    PredictData(sc, model, categoriesMap)

    println("===== 完成 ======")
  }

  def SetLogger = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("com").setLevel(Level.OFF)
    System.setProperty("spark.ui.showConsoleProgress", "false")
    Logger.getRootLogger().setLevel(Level.OFF)
  }

  def PrepareData(sc: SparkContext): (RDD[LabeledPoint], RDD[LabeledPoint], RDD[LabeledPoint], Map[String, Int]) = {
    //-- 1. 匯入、轉換資料
    println("開始匯入資料")

    val rawDataWithHeader = sc.textFile("data/train.tsv")
    val rawData = rawDataWithHeader.mapPartitionsWithIndex{ (idx, iter) => if (idx == 0) iter.drop(1) else iter }
    val lines = rawData.map(_.split("\t"))
    println(s"共計 ${lines.count} 筆")

    //-- 2. 建立 RDD[LabeledPoint]
    val categoriesMap = lines.map{ fields => fields(3)}.distinct.collect().zipWithIndex.toMap
    val labelpointRDD = lines.map{ fields =>
      val trFields = fields.map(_.replaceAll("\"", ""))
      val categoryFeaturesArray = Array.ofDim[Double](categoriesMap.size)
      val categoryIdx = categoriesMap(fields(3))
      categoryFeaturesArray(categoryIdx) = 1
      val numericalFeatures = trFields.slice(4, fields.size - 1).map(d => if (d == "?") 0.0 else d.toDouble).map(d => if (d < 0) 0.0 else d)
      val label = trFields(fields.size - 1).toInt
      LabeledPoint(label, Vectors.dense(categoryFeaturesArray ++ numericalFeatures))
    }

    //-- 進行資料標準化
    val featuresData = labelpointRDD.map(labelpoint => labelpoint.features)
    val stdScaler = new StandardScaler(withMean = false, withStd = true).fit(featuresData)
    val scaledRDD = labelpointRDD.map(labelpoint => LabeledPoint(labelpoint.label, stdScaler.transform(labelpoint.features)))


    //-- 3. 以隨機方式將資料份成三份
    val Array(trainData, validationData, testData) = scaledRDD.randomSplit(Array(0.8, 0.1, 0.1))

    println(s"資料分成 trainData: ${trainData.count}, validationData: ${validationData.count}, testData = ${testData.count}")

    (trainData, validationData, testData, categoriesMap)
  }

  def trainEvaluateTunning(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint], lambdaArray: Array[Int]): NaiveBayesModel = {
    val evaluationsArray = for {
      lambda <- lambdaArray
    } yield {
      val (model, time) = trainModel(trainData, lambda)
      val auc = evaluateModel(model, validationData)
      println(s"參數 lambda=$lambda, AUC=$auc, time=$time")

      (lambda, auc)
    }

    val bestEval = (evaluationsArray.sortBy(_._2).reverse)(0)
    println(s"最佳參數 lambea=${bestEval._1}, AUC=${bestEval._2}")

    val (model, time) = trainModel(trainData.union(validationData), bestEval._1)

    model
  }

  def trainModel(trainData: RDD[LabeledPoint], lambda: Int): (NaiveBayesModel, Double) = {
    val startTime = new DateTime()
    val model = NaiveBayes.train(trainData, lambda)
    val endTime = new DateTime()
    val duration = new Duration(startTime, endTime)

    (model, duration.getMillis)
  }

  def evaluateModel(model: NaiveBayesModel, validationData: RDD[LabeledPoint]): Double = {
    val scoreAndLabels = validationData.map { data =>
      var predict = model.predict(data.features)
      (predict, data.label)
    }
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auc = metrics.areaUnderROC
    auc
  }

  def PredictData(sc: SparkContext, model: NaiveBayesModel, categoriesMap: Map[String, Int]) = {
    //-- 1. 匯入並轉換資料
    val rawDataWithHeader = sc.textFile("data/test.tsv")
    val rawData = rawDataWithHeader.mapPartitionsWithIndex{ (idx, iter) => if (idx == 0) iter.drop(1) else iter }
    val lines = rawData.map(_.split("\t"))
    println(s"共計 ${lines.count} 筆")

    //-- 2. 建立預測所需資料
    val dataRDD = lines.take(10).map{ fields =>
      val trFields = fields.map(_.replaceAll("\"", ""))
      val categoryFeaturesArray = Array.ofDim[Double](categoriesMap.size)
      val categoryIdx = categoriesMap(fields(3))
      categoryFeaturesArray(categoryIdx) = 1
      val numericalFeature = trFields.slice(4, fields.size).map(d => if (d == "?") 0.0 else d.toDouble)
      val label = 0

      //-- 3. 進行預測
      val url = trFields(0)
      val Features = Vectors.dense(categoryFeaturesArray ++ numericalFeature)
      val predict = model.predict(Features).toInt
      val predictDesc = predict match {
        case 0 => "暫時性網頁"
        case 1 => "長青網頁"
      }
      
      println(s"網址 ${url} ==> 預測: ${predictDesc}")
    }
  }

}
```

### 執行 RunNaiveBayesBinary
```shell
$ sbt package
$ spark-submit --class RunNaiveBayesBinary --jars lib/joda-time-2.9.4.jar target/scala-2.10/classification_2.10-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 7395 筆
資料分成 trainData: 5895, validationData: 754, testData = 746
====== 訓練評估 ======
參數 lambda=1, AUC=0.645320197044335, time=1379.0
參數 lambda=3, AUC=0.645320197044335, time=181.0
參數 lambda=5, AUC=0.645320197044335, time=127.0
參數 lambda=15, AUC=0.645320197044335, time=106.0
參數 lambda=25, AUC=0.6451149425287356, time=148.0
最佳參數 lambea=15, AUC=0.645320197044335
====== 測試模型 ======
測試最佳模型，結果 AUC=0.6492883841288097
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
