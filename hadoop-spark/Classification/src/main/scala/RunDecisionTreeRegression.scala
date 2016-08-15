import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.rdd._
import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.evaluation._
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.joda.time._

object RunDecisionTreeRegression {
  def main(args: Array[String]) {
    setLogger

    val sc = new SparkContext(new SparkConf().setAppName("DecisionTreeRegression").setMaster("local[4]"))

    println("====== 準備階段 ======")
    val (trainData, validationData, testData) = prepareData(sc)
    trainData.persist()
    validationData.persist()
    testData.persist()

    println("====== 訓練評估 ======")
    val model = if (args.size == 0) {
      trainEvaluateTunning(trainData, validationData,
        Array("variance"),
        Array(3, 5, 10, 15, 20),
        Array(3, 5, 10, 50, 100))
    } else {
      val impurity = args(0)
      val maxDepth = args(1).toInt
      val maxBins = args(2).toInt
      trainEvaluate(trainData, validationData, impurity, maxDepth, maxBins)
    }

    println("====== 測試模型 ======")
    val rmse = evaluateModel(model, testData)
    println(s"測試最佳模型，結果 RMSE=${rmse}")

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
      println(f"impurity=${impurity}, maxDepth=${maxDepth}%2d, maxBins=${maxBins}%3d ==> RMSE=${rmse}%6.2f, time=${time}ms")

      (impurity, maxDepth, maxBins, rmse)
    }

    val evaluationsArraySortedAsc = (evaluationsArray.sortBy(_._4))
    val bestEval = evaluationsArraySortedAsc(0)
    val (model, time) = trainModel(trainData.union(validationData), bestEval._1, bestEval._2, bestEval._3)
    println(f"最佳參數 impurity=${bestEval._1}, maxDepth=${bestEval._2}, maxBins=${bestEval._3}, RMSE=${bestEval._4}")

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

  def predictData(sc: SparkContext, model: DecisionTreeModel) = {
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
          case 1 => " 晴"
          case 2 => " 陰"
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
