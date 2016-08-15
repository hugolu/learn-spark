import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.rdd._
import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.evaluation._
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.joda.time._

object RunDecisionTreeMulti {
  def main(args: Array[String]) {
    setLogger

    val sc = new SparkContext(new SparkConf().setAppName("DecisionTreeMulti").setMaster("local[4]"))

    println("====== 準備階段 ======")
    val (trainData, validationData, testData) = prepareData(sc)
    trainData.persist()
    validationData.persist()
    testData.persist()

    println("====== 訓練評估 ======")
    val model = if (args.size == 0) {
      trainEvaluateTunning(trainData, validationData,
        Array("gini", "entropy"),
        Array(3, 5, 10, 15, 20), 
        Array(3, 5, 10, 50, 100))
    } else {
      val impurity = args(0)
      val maxDepth = args(1).toInt
      val maxBins = args(2).toInt
      trainEvaluate(trainData, validationData, impurity, maxDepth, maxBins)
    }

    println("====== 測試模型 ======")
    val auc = evaluateModel(model, testData)
    println(s"測試最佳模型，結果 AUC=${auc}")

    println("====== 預測資料 ======")
    predictData(sc, model)

    println("===== 完成 ======")
    trainData.unpersist()
    validationData.unpersist()
    testData.unpersist()
  }

  def setLogger = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("com").setLevel(Level.OFF)
    System.setProperty("spark.ui.showConsoleProgress", "false")
    Logger.getRootLogger().setLevel(Level.OFF)
  }

  def prepareData(sc: SparkContext): (RDD[LabeledPoint], RDD[LabeledPoint], RDD[LabeledPoint]) = {
    //-- 1. 匯入、轉換資料
    println("開始匯入資料")

    val rawData = sc.textFile("data/covtype.data")
    println(s"共計 ${rawData.count} 筆")

    //-- 2. 建立 RDD[LabeledPoint]
    val labelpointRDD = rawData.map{ record =>
      val fields = record.split(',').map(_.toDouble)
      val label = fields.last - 1
      LabeledPoint(label, Vectors.dense(fields.init))
    }

    //-- 3. 以隨機方式將資料份成三份
    val Array(trainData, validationData, testData) = labelpointRDD.randomSplit(Array(0.8, 0.1, 0.1))
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
      val auc = evaluateModel(model, validationData)
      println(f"impurity=${impurity}%7s, maxDepth=${maxDepth}%2d, maxBins=${maxBins}%3d ==> AUC=${auc}%.2f, time=${time}ms")

      (impurity, maxDepth, maxBins, auc)
    }

    val bestEval = (evaluationsArray.sortBy(_._4).reverse)(0)
    val (model, time) = trainModel(trainData.union(validationData), bestEval._1, bestEval._2, bestEval._3)
    println(f"最佳參數 impurity=${bestEval._1}, maxDepth=${bestEval._2}, maxBins=${bestEval._3}, AUC=${bestEval._4}%.2f")

    model
  }

  def trainEvaluate(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint], impurity: String, maxDepth: Int, maxBins: Int): DecisionTreeModel = {
    println("開始訓練...")

    val (model, time) = trainModel(trainData.union(validationData), impurity, maxDepth, maxBins)
    println(s"訓練完成 所需時間:${time}ms")
    
    model
  }

  def trainModel(trainData: RDD[LabeledPoint], impurity: String, maxDepth: Int, maxBins: Int): (DecisionTreeModel, Long) = {
    val startTime = new DateTime()
    val model = DecisionTree.trainClassifier(trainData, 7, Map[Int, Int](), impurity, maxDepth, maxBins)
    val endTime = new DateTime()
    val duration = new Duration(startTime, endTime)

    (model, duration.getMillis)
  }

  def evaluateModel(model: DecisionTreeModel, validationData: RDD[LabeledPoint]): Double = {
    val scoreAndLabels = validationData.map { data =>
      var predict = model.predict(data.features)
      (predict, data.label)
    }
    val metrics = new MulticlassMetrics(scoreAndLabels)
    val precision = metrics.precision
    precision
  }

  def predictData(sc: SparkContext, model: DecisionTreeModel) = {
    //-- 1. 匯入並轉換資料
    val rawData = sc.textFile("data/covtype.data")
    println(s"共計 ${rawData.count} 筆")

    //-- 2. 建立預測所需資料
    val Array(pData, oData) = rawData.randomSplit(Array(0.1, 0.9))
    val data = pData.take(20).map { record => 
      val fields = record.split(',').map(_.toDouble)
      val features = Vectors.dense(fields.init)
      val label = fields.last - 1
      val predict = model.predict(features)
      val result = if (label == predict) "正確" else "錯誤"

      println(s"土地條件 海拔:${features(0)}, 方位:${features(1)}, 斜率:＄${features(2)}, 水源垂直距離:${features(3)}, 水源水平距離:${features(4)}, 9點時陰影:${features(5)} => 預測:${predict}, 實際:${label}, 結果:${result}")
    }
  }

}
