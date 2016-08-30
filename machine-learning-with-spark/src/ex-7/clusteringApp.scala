import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.recommendation.{ ALS, Rating }
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.RowMatrix

object clusteringApp {
  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf().setAppName("clusteringApp").setMaster("local[4]"))

    // Extracting features from the MovieLens dataset
    val movies = sc.textFile("../ml-100k/u.item")
    println(movies.first)

    val genres = sc.textFile("../ml-100k/u.genre")
    println(genres.collect.mkString(", "))

    val genreMap = genres.filter(!_.isEmpty).map(line => line.split("\\|")).map(array => (array(1), array(0))).collectAsMap
    println(genreMap)

    val titlesAndGenres = movies.map(_.split("\\|")).map{ array =>
      val genres = array.slice(5, array.size)
      val genresAssigned = genres.zipWithIndex.filter{ case (g, idx) =>
        g == "1"
      }.map{ case (g, idx) =>
        genreMap(idx.toString)
      }
      (array(0).toInt, (array(1), genresAssigned.mkString("(", ", ", ")")))
    }
    println(titlesAndGenres.first)

    // Training the recommendation model
    val rawData = sc.textFile("../ml-100k/u.data")
    val rawRatings = rawData.map(_.split("\t").take(3))
    val ratings = rawRatings.map{ case Array(user, movie, rating) => Rating(user.toInt, movie.toInt, rating.toDouble) }
    ratings.cache
    val alsModel = ALS.train(ratings, 50, 10, 0.1)

    val movieFactors = alsModel.productFeatures.map{ case (id, factor) => (id, Vectors.dense(factor)) }
    val movieVectors = movieFactors.map(_._2)

    val userFactors = alsModel.userFeatures.map{ case (id, factor) => (id, Vectors.dense(factor)) }
    val userVectors = userFactors.map(_._2)

    // Normalization
    val movieMatrix = new RowMatrix(movieVectors)
    val movieMatrixSummary = movieMatrix.computeColumnSummaryStatistics()
    
    val userMatrix = new RowMatrix(userVectors)
    val userMatrixSummary = userMatrix.computeColumnSummaryStatistics()

    println("Movie factors mean: " + movieMatrixSummary.mean)
    println("Movie factors variance: " + movieMatrixSummary.variance)
    println("User factors mean: " + userMatrixSummary.mean)
    println("User factors variance: " + userMatrixSummary.variance)
  }
}
