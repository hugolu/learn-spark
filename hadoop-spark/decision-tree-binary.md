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

## 建立 RunDecisionTreeBinary.scala
```shell
$ vi src/main/scala/RunDecisionTreeBinary.scala
```

RunDecisionTreeBinary.scala:
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

object RunDecisionTreeBinary {
  def main(args: Array[String]) {
    SetLogger

    val sc = new SparkContext(new SparkConf().setAppName("DecisionTreeBinary").setMaster("local[4]"))

    println("====== 準備階段 ======")
    val (trainData, validationData, testData, categoriesMap) = PrepareData(sc)
    trainData.persist()
    validationData.persist()
    testData.persist()

    println("====== 訓練評估 ======")
    val model = trainEvaluate(trainData, validationData)

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

    //-- 3. 以隨機方式將資料份成三份
    val Array(trainData, validationData, testData) = labelpointRDD.randomSplit(Array(0.8, 0.1, 0.1))

    println(s"資料分成 trainData: ${trainData.count}, validationData: ${validationData.count}, testData = ${testData.count}")

    (trainData, validationData, testData, categoriesMap)
  }

  def trainEvaluate(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint]): DecisionTreeModel = {
    println("開始訓練...")

    val (model, time) = trainModel(trainData, "entropy", 10, 10)
    println(s"訓練完成 所需時間:${time}ms")

    val auc = evaluateModel(model, validationData)
    println(s"評估結果 AUC=${auc}")
    
    model
  }

  def trainModel(trainData: RDD[LabeledPoint], impurity: String, maxDepth: Int, maxBins: Int): (DecisionTreeModel, Double) = {
    val startTime = new DateTime()
    val model = DecisionTree.trainClassifier(trainData, 2, Map[Int, Int](), impurity, maxDepth, maxBins)
    val endTime = new DateTime()
    val duration = new Duration(startTime, endTime)

    (model, duration.getMillis)
  }

  def evaluateModel(model: DecisionTreeModel, validationData: RDD[LabeledPoint]): Double = {
    val scoreAndLabels = validationData.map { data =>
      var predict = model.predict(data.features)
      (predict, data.label)
    }
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auc = metrics.areaUnderROC
    auc
  }

  def PredictData(sc: SparkContext, model: DecisionTreeModel, categoriesMap: Map[String, Int]) = {
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

函數 | 說明
-----|-----
`main`          | 主程式，包含準備資料、訓練模型、測試模型、預測資料
`SetLogger`     | 關閉 log & console 訊息
`PrepareData`   | 匯入資料，建立 LabeledPoint、講資料分成 train, evaluation, test 三份
`trainEvaluate` | 訓練評估流程，包含訓練模型、評估模型
`trainModel`    | 訓練模型
`evaluateModel` | 評估模型
`PredictData`   | 使用模型預測資料

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

### 執行 RunDecisionTreeBinary
```shell
$ sbt package
$ spark-submit --class RunDecisionTreeBinary --jars lib/joda-time-2.9.4.jar target/scala-2.10/classification_2.10-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 7395 筆
資料分成 trainData: 5944, validationData: 680, testData = 771
====== 訓練評估 ======
開始訓練...
訓練完成 所需時間:5516.0ms
評估結果 AUC=0.6508976854856153
====== 測試階段 ======
測試最佳模型，結果 AUC=0.6705470695108
====== 預測資料 ======
共計 3171 筆
網址 http://www.lynnskitchenadventures.com/2009/04/homemade-enchilada-sauce.html ==> 預測: 暫時性網頁
網址 http://lolpics.se/18552-stun-grenade-ar ==> 預測: 暫時性網頁
網址 http://www.xcelerationfitness.com/treadmills.html ==> 預測: 暫時性網頁
網址 http://www.bloomberg.com/news/2012-02-06/syria-s-assad-deploys-tactics-of-father-to-crush-revolt-threatening-reign.html ==> 預測: 暫時性網頁
網址 http://www.wired.com/gadgetlab/2011/12/stem-turns-lemons-and-limes-into-juicy-atomizers/ ==> 預測: 長青網頁
網址 http://www.latimes.com/health/boostershots/la-heb-fat-tax-denmark-20111013,0,2603132.story ==> 預測: 長青網頁
網址 http://www.howlifeworks.com/a/a?AG_ID=1186&cid=7340ci ==> 預測: 長青網頁
網址 http://romancingthestoveblog.wordpress.com/2010/01/13/sweet-potato-ravioli-with-lemon-sage-brown-butter-sauce/ ==> 預測: 長青網頁
網址 http://www.funniez.net/Funny-Pictures/turn-men-down.html ==> 預測: 暫時性網頁
網址 http://youfellasleepwatchingadvd.com/ ==> 預測: 長青網頁
===== 完成 ======
```

### 調校訓練參數
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

object RunDecisionTreeBinary {
  def main(args: Array[String]) {
    SetLogger

    val sc = new SparkContext(new SparkConf().setAppName("DecisionTreeBinary").setMaster("local[4]"))

    println("====== 準備階段 ======")
    val (trainData, validationData, testData, categoriesMap) = PrepareData(sc)
    trainData.persist()
    validationData.persist()
    testData.persist()

    println("====== 訓練評估 ======")
    val model = trainEvaluateTunning(trainData, validationData, Array("gini", "entropy"), Array(3,5,10,15,20), Array(3,5,10,50,100))

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

    //-- 3. 以隨機方式將資料份成三份
    val Array(trainData, validationData, testData) = labelpointRDD.randomSplit(Array(0.8, 0.1, 0.1))

    println(s"資料分成 trainData: ${trainData.count}, validationData: ${validationData.count}, testData = ${testData.count}")

    (trainData, validationData, testData, categoriesMap)
  }

  def trainEvaluateTunning(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint], impurityArray: Array[String], maxDepthArray: Array[Int], maxBinsArray: Array[Int]): DecisionTreeModel = {
    val evaluationsArray = for {
      impurity <- impurityArray
      maxDepth <- maxDepthArray
      maxBins <- maxBinsArray
    } yield {
      val (model, time) = trainModel(trainData, impurity, maxDepth, maxBins)
      val auc = evaluateModel(model, validationData)
      println(s"參數 impurity=$impurity, maxDepth=$maxDepth, maxBins=$maxBins, AUC=$auc, time=$time")

      (impurity, maxDepth, maxBins, auc)
    }

    val bestEval = (evaluationsArray.sortBy(_._4).reverse)(0)
    println(s"最佳參數 impurity=${bestEval._1}, maxDepth=${bestEval._2}, maxBins=${bestEval._3}, AUC=${bestEval._4}")
    
    val (model, time) = trainModel(trainData.union(validationData), bestEval._1, bestEval._2, bestEval._3)
    model
  }

  def trainModel(trainData: RDD[LabeledPoint], impurity: String, maxDepth: Int, maxBins: Int): (DecisionTreeModel, Double) = {
    val startTime = new DateTime()
    val model = DecisionTree.trainClassifier(trainData, 2, Map[Int, Int](), impurity, maxDepth, maxBins)
    val endTime = new DateTime()
    val duration = new Duration(startTime, endTime)

    (model, duration.getMillis)
  }

  def evaluateModel(model: DecisionTreeModel, validationData: RDD[LabeledPoint]): Double = {
    val scoreAndLabels = validationData.map { data =>
      var predict = model.predict(data.features)
      (predict, data.label)
    }
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auc = metrics.areaUnderROC
    auc
  }

  def PredictData(sc: SparkContext, model: DecisionTreeModel, categoriesMap: Map[String, Int]) = {
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
- 將 `trainEvaluate` 改成 `trainEvaluateTunning`，使用一組 impurity, 一組 maxDepth, 一組 maxBins，找出排列組合中有最高 AUC 的 model

```shell
$ spark-submit --class RunDecisionTreeBinary --jars lib/joda-time-2.9.4.jar target/scala-2.10/decisiontree_2.10-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 7395 筆
資料分成 trainData: 5898, validationData: 739, testData = 758
====== 訓練評估 ======
參數 impurity=gini, maxDepth=3, maxBins=3, AUC=0.5512290879259323, time=3306.0
參數 impurity=gini, maxDepth=3, maxBins=5, AUC=0.57700477571709, time=1358.0
參數 impurity=gini, maxDepth=3, maxBins=10, AUC=0.5744081626673698, time=714.0
...
最佳參數 impurity=gini, maxDepth=15, maxBins=10, AUC=0.6467763909642261
====== 測試模型 ======
測試最佳模型，結果 AUC=0.6133830419738646
====== 預測資料 ======
共計 3171 筆
網址 http://www.lynnskitchenadventures.com/2009/04/homemade-enchilada-sauce.html ==> 預測: 長青網頁
網址 http://lolpics.se/18552-stun-grenade-ar ==> 預測: 暫時性網頁
網址 http://www.xcelerationfitness.com/treadmills.html ==> 預測: 暫時性網頁
網址 http://www.bloomberg.com/news/2012-02-06/syria-s-assad-deploys-tactics-of-father-to-crush-revolt-threatening-reign.html ==> 預測: 暫時性網頁
網址 http://www.wired.com/gadgetlab/2011/12/stem-turns-lemons-and-limes-into-juicy-atomizers/ ==> 預測: 暫時性網頁
網址 http://www.latimes.com/health/boostershots/la-heb-fat-tax-denmark-20111013,0,2603132.story ==> 預測: 長青網頁
網址 http://www.howlifeworks.com/a/a?AG_ID=1186&cid=7340ci ==> 預測: 長青網頁
網址 http://romancingthestoveblog.wordpress.com/2010/01/13/sweet-potato-ravioli-with-lemon-sage-brown-butter-sauce/ ==> 預測: 長青網頁
網址 http://www.funniez.net/Funny-Pictures/turn-men-down.html ==> 預測: 暫時性網頁
網址 http://youfellasleepwatchingadvd.com/ ==> 預測: 暫時性網頁
===== 完成 ======
```
- 最佳模型參數 impurity="gini", maxDepth=15, maxBins=10, 訓練 AUC=0.646 與評估 AUC=0.613 相差不大，無 overfitting
