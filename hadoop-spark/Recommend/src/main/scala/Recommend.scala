import java.io.File
import scala.io.Source
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd._
import org.apache.spark.mllib.recommendation.{ ALS, Rating, MatrixFactorizationModel }
import scala.collection.immutable.Map

object Recommend {
  def main(args: Array[String]) {
    SetLogger

    println("====== 準備階段 ======")
    val (ratings, movieTitle) = PrepareData()

    println("====== 訓練階段 ======")
    val model = ALS.train(ratings, 5, 20, 0.1)

    println("====== 推薦階段 ======")
    recommand(model, movieTitle)

    println("完成")
  }

  def SetLogger = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("com").setLevel(Level.OFF)
    System.setProperty("spark.ui.showConsoleProgress", "false")
    Logger.getRootLogger().setLevel(Level.OFF)
  }

  def PrepareData(): (RDD[Rating], Map[Int, String]) = {
    //1. 建議用戶評價資料
    val sc = new SparkContext(new SparkConf().setAppName("Recommend").setMaster("local[4]"))
    println("開始讀取用戶評價資料...")
    val DataDir = "ml-100k"
    val rawUserData = sc.textFile(new File(DataDir, "u.data").toString)
    val rawRatings = rawUserData.map(_.split("\t").take(3))
    val ratingsRDD = rawRatings.map{ case Array(user, movie, rating) =>
                                      Rating(user.toInt, movie.toInt, rating.toDouble) }
    println("共計: " + ratingsRDD.count.toString + "筆 ratings")

    //2. 建立電影ID名稱對照表
    println("開始讀取電影資料...")
    val itemRDD = sc.textFile(new File(DataDir, "u.item").toString)
    val movieTitle = itemRDD.map(line => line.split("\\|").take(2))
                            .map(array => (array(0).toInt, array(1)))
                            .collect()
                            .toMap

    //3. 顯示資料筆數
    val numRatings = ratingsRDD.count
    val numUsers = ratingsRDD.map(_.user).distinct.count
    val numMovies = ratingsRDD.map(_.product).distinct.count
    println("共計: ratings: " + numRatings + ", users: " + numUsers + ", movies: " + numMovies)

    return (ratingsRDD, movieTitle)
  }

  def recommand(model: MatrixFactorizationModel, movieTitle: Map[Int, String]) = {
    var choose = ""
    while (choose != "3") {
      println("請選擇要推薦的類型: 1: 針對用戶推薦電影, 2: 針對電影推薦有興趣的用戶, 3: 離開")
      choose = readLine()

      if (choose == "1") {
        print("請輸入用戶ID? ")
        val inputUserID = readLine()
        RecommendMovies(model, movieTitle, inputUserID.toInt)
      } else if (choose == "2") {
        print("請輸入電影ID? ")
        val inputMovieID = readLine()
        RecommendUsers(model, movieTitle, inputMovieID.toInt)
      }
    }
  }

  def RecommendMovies(model: MatrixFactorizationModel, movieTitle: Map[Int, String], inputUserID: Int) = {
    val RecommendMovie = model.recommendProducts(inputUserID, 10)
    println("針對用戶: " + inputUserID + " 推薦以下電影:")
    RecommendMovie.foreach{ r => println("電影: " + movieTitle(r.product) + ", 評價: " + r.rating.toString) }
  }

  def RecommendUsers(model: MatrixFactorizationModel, movieTitle: Map[Int, String], inputMovieID: Int) = {
    val RecommendUser = model.recommendUsers(inputMovieID, 10)
    println("針對電影: " + movieTitle(inputMovieID.toInt) + ", 推薦以下用戶:")
    RecommendUser.foreach{ r => println("用戶: " + r.user + ", 評價: " + r.rating) }
  }

} 
