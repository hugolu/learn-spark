# SVM 二元分類

## SVM 演算法基本概念
SVM (Support Vector Machine) 是用來做分類的工具，在分類過程中尋找具有最大邊緣區 (Maximum Marginal Hyperplane) 的分隔線，因為其具有較高的分類準確性，且有較高的誤差容忍度。

![](http://www.improvedoutcomes.com/docs/WebSiteDocs/image/diagram_svm_maximal_margin.gif)

圖 D 擁有對大邊緣區，對 SVM 而言，是較好的分類。

## 建立 RunSVMWithSGDBinary.scala
```shell
$ vi src/main/scala/RunSVMWithSGDBinary.scala
```

RunSVMWithSGDBinary.scala:
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
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.classification.SVMModel
import org.joda.time._

object RunSVMWithSGDBinary {
  def main(args: Array[String]) {
    SetLogger

    val sc = new SparkContext(new SparkConf().setAppName("DecisionTreeBinary").setMaster("local[4]"))

    println("====== 準備階段 ======")
    val (trainData, validationData, testData, categoriesMap) = PrepareData(sc)
    trainData.persist()
    validationData.persist()
    testData.persist()

    println("====== 訓練評估 ======")
    val model = trainEvaluateTunning(trainData, validationData,
      Array(1, 3, 5, 15, 25),
      Array(10, 50, 100, 200),
      Array(0.01, 0.1, 1))

    println("====== 測試模型 ======")
    val auc = evaluateModel(model, testData)
    println(s"測試最佳模型，結果 AUC=${auc}")

    println("====== 預測資料 ======")
    PredictData(sc, model, categoriesMap)

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
      val numericalFeatures = trFields.slice(4, fields.size - 1).map(d => if (d == "?") 0.0 else d.toDouble)
      val label = trFields(fields.size - 1).toInt
      LabeledPoint(label, Vectors.dense(categoryFeaturesArray ++ numericalFeatures))
    }

    //-- 進行資料標準化
    val featuresData = labelpointRDD.map(labelpoint => labelpoint.features)
    val stdScaler = new StandardScaler(withMean = true, withStd = true).fit(featuresData)
    val scaledRDD = labelpointRDD.map(labelpoint => LabeledPoint(labelpoint.label, stdScaler.transform(labelpoint.features)))

    //-- 3. 以隨機方式將資料份成三份
    val Array(trainData, validationData, testData) = labelpointRDD.randomSplit(Array(0.8, 0.1, 0.1))

    println(s"資料分成 trainData: ${trainData.count}, validationData: ${validationData.count}, testData = ${testData.count}")

    (trainData, validationData, testData, categoriesMap)
  }

  def trainEvaluateTunning(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint], numIterationsArray: Array[Int], stepSizeArray: Array[Int], regParamArray: Array[Double]): SVMModel= {
    val evaluationsArray = for {
      numIterations <- numIterationsArray
      stepSize <- stepSizeArray
      regParam <- regParamArray
    } yield {
      val (model, time) = trainModel(trainData, numIterations, stepSize, regParam)
      val auc = evaluateModel(model, validationData)
      println(s"參數 numIterations=$numIterations, stepSize=$stepSize, regParam=$regParam, AUC=$auc, time=$time")

      (numIterations, stepSize, regParam, auc)
    }

    val bestEval = (evaluationsArray.sortBy(_._4).reverse)(0)
    println(s"最佳參數 numIterations=${bestEval._1}, stepSize=${bestEval._2}, regParam=${bestEval._3}, AUC=${bestEval._4}")

    val (model, time) = trainModel(trainData.union(validationData), bestEval._1, bestEval._2, bestEval._3)

    model
  }

  def trainModel(trainData: RDD[LabeledPoint], numIterations: Int, stepSize: Double, regParam: Double): (SVMModel, Double) = {
    val startTime = new DateTime()
    val model = SVMWithSGD.train(trainData, numIterations, stepSize, regParam)
    val endTime = new DateTime()
    val duration = new Duration(startTime, endTime)

    (model, duration.getMillis)
  }

  def evaluateModel(model: SVMModel, validationData: RDD[LabeledPoint]): Double = {
    val scoreAndLabels = validationData.map { data =>
      var predict = model.predict(data.features)
      (predict, data.label)
    }
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auc = metrics.areaUnderROC
    auc
  }

  def PredictData(sc: SparkContext, model: SVMModel, categoriesMap: Map[String, Int]) = {
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

### 執行 RunSVMWithSGDBinary
```shell
$ sbt package 
$ spark-submit --class RunSVMWithSGDBinary --jars lib/joda-time-2.9.4.jar target/scala-2.10/classification_2.10-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 7395 筆
資料分成 trainData: 5935, validationData: 785, testData = 675
====== 訓練評估 ======
參數 numIterations=1, stepSize=10, regParam=0.01, AUC=0.5013089005235603, time=1682.0
參數 numIterations=1, stepSize=10, regParam=0.1, AUC=0.5013089005235603, time=261.0
參數 numIterations=1, stepSize=10, regParam=1.0, AUC=0.5013089005235603, time=181.0
...
最佳參數 numIterations=15, stepSize=10, regParam=0.01, AUC=0.5026178010471204
====== 測試模型 ======
測試最佳模型，結果 AUC=0.5030581039755352
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
- 糟糕！最佳模型 AUC 只有 0.50，差不多是隨機預測 :(
