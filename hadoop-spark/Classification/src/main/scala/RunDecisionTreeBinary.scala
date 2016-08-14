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
    val model = if (args.size == 0) {
      trainEvaluateTunning(trainData, validationData, Array("gini", "entropy"), Array(3,5,10,15,20), Array(3,5,10,50,100))
    } else {
      val impurity = args(0)
      val maxDepth = args(1).toInt
      val maxBins = args(2).toInt
      trainEvaluate(trainData, validationData, impurity, maxDepth, maxBins)
    }

    println("====== 測試階段 ======")
    val auc = evaluateModel(model, testData)
    println(s"測試模型 AUC=${auc}")

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
      println(f"impurity=$impurity%7s, maxDepth=$maxDepth%2d, maxBins=$maxBins%3d ==> AUC=$auc%.2f, time=$time%7.2fms")

      (impurity, maxDepth, maxBins, auc)
    }

    val bestEval = (evaluationsArray.sortBy(_._4).reverse)(0)
    val (model, time) = trainModel(trainData.union(validationData), bestEval._1, bestEval._2, bestEval._3)

    println(s"最佳參數 impurity=${bestEval._1}, maxDepth=${bestEval._2}, maxBins=${bestEval._3}, AUC=${bestEval._4}")
    model
  }

  def trainEvaluate(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint], impurity: String, maxDepth: Int, maxBins: Int): DecisionTreeModel = {
    println("開始訓練...")

    val (model, time) = trainModel(trainData, impurity, maxDepth, maxBins)
    println(s"訓練完成 所需時間:${time}ms")
    
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
