import org.apache.log4j.Logger
import org.apache.log4j.Level
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
    setLogger

    val sc = new SparkContext(new SparkConf().setAppName("NavieBayes").setMaster("local[4]"))

    println("====== 準備階段 ======")
    val (trainData, validationData, testData, categoriesMap) = prepareData(sc)
    trainData.persist()
    validationData.persist()
    testData.persist()

    println("====== 訓練評估 ======")
    val model = if (args.size == 0) {
      trainEvaluateTunning(trainData, validationData,
        Array(1, 3, 5, 15, 25))
    } else {
      val lambda = args(0).toInt
      trainEvaluate(trainData, validationData, lambda)
    }

    println("====== 測試模型 ======")
    val auc = evaluateModel(model, testData)
    println(s"測試最佳模型，結果 AUC=${auc}")

    println("====== 預測資料 ======")
    predictData(sc, model, categoriesMap)

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

  def prepareData(sc: SparkContext): (RDD[LabeledPoint], RDD[LabeledPoint], RDD[LabeledPoint], Map[String, Int]) = {
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
      println(f"lambda=${lambda}%2d ==> AUC=${auc}%.2f, time=${time}ms")

      (lambda, auc)
    }

    val bestEval = (evaluationsArray.sortBy(_._2).reverse)(0)
    println(s"最佳參數 lambda=${bestEval._1}, AUC=${bestEval._2}")

    val (model, time) = trainModel(trainData.union(validationData), bestEval._1)

    model
  }

  def trainEvaluate(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint], lambda: Int): NaiveBayesModel = {
    println("開始訓練...")

    val (model, time) = trainModel(trainData.union(validationData), lambda)
    println(s"訓練完成 所需時間:${time}ms")
    
    model
  }
  def trainModel(trainData: RDD[LabeledPoint], lambda: Int): (NaiveBayesModel, Long) = {
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

  def predictData(sc: SparkContext, model: NaiveBayesModel, categoriesMap: Map[String, Int]) = {
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
