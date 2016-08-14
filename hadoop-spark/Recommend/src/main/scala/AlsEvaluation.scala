import java.io.File
import scala.io.Source
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd._
import org.apache.spark.mllib.recommendation.{ ALS, Rating, MatrixFactorizationModel }
import org.joda.time.format._
import org.joda.time._
import org.joda.time.ReadableInstant
import org.joda.time.Duration

object AlsEvaluation {
  def main(args: Array[String]) {
    SetLogger

    println("====== 資料準備階段 ======")
    val (trainData, validationData, testData) = PrepareData()
    trainData.persist()
    validationData.persist()
    testData.persist()

    println("====== 訓練驗證階段 ======")
    val bestModel = trainValidation(trainData, validationData)

    println("====== 測試階段 =====")
    val testRmse = computeRMSE(bestModel, testData)

    println("使用 testData 測試 bestModel, 結果 RMSE = " + testRmse)

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

  def PrepareData(): (RDD[Rating], RDD[Rating], RDD[Rating]) = {
    // 1. 建立用戶評價資料
    val sc = new SparkContext(new SparkConf().setAppName("Recommend").setMaster("local[4]"))
    println("開始讀取用戶評價資料...")
    val DataDir = "ml-100k"
    val rawUserData = sc.textFile(new File(DataDir, "u.data").toString)
    val rawRatings = rawUserData.map(_.split("\t").take(3))
    val ratingsRDD = rawRatings.map{ case Array(user, movie, rating) =>
                                      Rating(user.toInt, movie.toInt, rating.toDouble) }
    println("共計: " + ratingsRDD.count.toString + "筆 ratings")

    //2. 建立電影ID與名稱對照表
    println("開始讀取電影資料...")
    val itemRDD = sc.textFile(new File(DataDir, "u.item").toString)
    val movieTitle = itemRDD.map(line => line.split("\\|").take(2))
                            .map(array => (array(0).toInt, array(1)))
                            .collect()
                            .toMap

    //3. 顯示資料數
    val numRatings = ratingsRDD.count
    val numUsers = ratingsRDD.map(_.user).distinct.count
    val numMovies = ratingsRDD.map(_.product).distinct.count
    println("共計: ratings: " + numRatings + ", users: " + numUsers + ", movies: " + numMovies)

    //4. 以隨機方式將資料分成三份並回傳 train : validation : test = 8 : 1 : 1
    val Array(trainData, validationData, testData) = ratingsRDD.randomSplit(Array(0.8, 0.1, 0.1))
    println(s"train: ${trainData.count}, validation: ${validationData.count}, test: ${testData.count}")

    (trainData, validationData, testData)
  }

  def trainValidation(trainData: RDD[Rating], validationData: RDD[Rating]): MatrixFactorizationModel = {
    println("---- 評估 rank 參數 ----")
    evaluateParameter(trainData, validationData, "rank", Array(5, 10, 15, 20, 50, 100), Array(10), Array(0.1))

    println("---- 評估 numIterations 參數 ----")
    evaluateParameter(trainData, validationData, "numIterations", Array(10), Array(5, 10, 15, 20, 25), Array(0.1))

    println("---- 評估 lambda 參數 ----")
    evaluateParameter(trainData, validationData, "lambda", Array(10), Array(10), Array(0.05, 0.1, 1, 5, 10.0))

    println("---- 所有參數交叉評估找出最好的參數組合 ----")
    val bestModel = evaluateAllParameter(trainData, validationData,
                      Array(5, 10, 15, 20, 25),
                      Array(5, 10, 15, 20, 25),
                      Array(0.05, 0.1, 1, 5, 10.0))

    bestModel
  }

  def evaluateParameter(trainData: RDD[Rating], validationData: RDD[Rating], evaluationParameter: String,
                        rankArray: Array[Int], numIterationsArray: Array[Int], lambdaArray: Array[Double]) = {
    for (
      rank <- rankArray;
      numIterations <- numIterationsArray;
      lambda <- lambdaArray
    ) {
      trainModel(trainData, validationData, rank, numIterations, lambda)
    }
  }

  def evaluateAllParameter(trainData: RDD[Rating], validationData: RDD[Rating],
   rankArray: Array[Int], numIterationsArray: Array[Int], lambdaArray: Array[Double]): MatrixFactorizationModel = {
     val Evaluations = for (
        rank <- rankArray;
        numIterations <- numIterationsArray;
        lambda <- lambdaArray
      ) yield {
        val (rmse, time) = trainModel(trainData, validationData, rank, numIterations, lambda)
        (rank, numIterations, lambda, rmse)
      }
    val eval = Evaluations.sortBy(_._4)
    val bestEval = eval(0)

    println(f"最佳model: rank=${bestEval._1}, iterations=${bestEval._2}, lambda=${bestEval._3}%.2f, rmse=${bestEval._4}%.2f")
    val bestModel = ALS.train(trainData, bestEval._1, bestEval._2, bestEval._3)

    bestModel
  }

  def trainModel(trainData: RDD[Rating], validationData: RDD[Rating],
                 rank: Int, iterations: Int, lambda: Double): (Double, Double) = {
    val startTime = new DateTime()
    val model = ALS.train(trainData, rank, iterations, lambda)
    val endTime = new DateTime()
    val rmse = computeRMSE(model, validationData)
    val duration = new Duration(startTime, endTime)
    val time = duration.getStandardSeconds

    println(f"訓練參數: rank=${rank}, iterations=${iterations}, lambda=${lambda}%.2f, rmse=${rmse}%.2f, time=${time}%.2fms")
    (rmse, time)
  }

  def computeRMSE(model: MatrixFactorizationModel, ratingRDD: RDD[Rating]): Double = {
    val num = ratingRDD.count
    val predicatedRDD = model.predict(ratingRDD.map(r => (r.user, r.product)))
    val predictedAndRatings = predicatedRDD.map(p => ((p.user, p.product), p.rating)).join(
      ratingRDD.map(r => ((r.user, r.product), r.rating))).values

    math.sqrt(predictedAndRatings.map(x => (x._1 - x._2) * (x._1 - x._2)).reduce(_+_) / num)
  }

}
