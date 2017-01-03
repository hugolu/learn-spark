import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.recommendation.{ Rating, ALS, MatrixFactorizationModel }
import org.apache.spark.mllib.evaluation.{ RegressionMetrics, RankingMetrics }
import org.jblas.DoubleMatrix

object RecommendationApp {
  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf().setAppName("RecommendationApp").setMaster("local[4]"))

    // Extracting features
    val rawData = sc.textFile("data/ml-100k/u.data")
    val rawRatings = rawData.map(_.split("\t").take(3))
    val ratings = rawRatings.map{ case Array(user, product, rating) => Rating(user.toInt, product.toInt, rating.toDouble) }

    // Training the recommendation model
    val model = ALS.train(ratings, 50, 10, 0.01)

    // User recommendations
    println("predictedRating=" + model.predict(798, 123))
    println("topKRecs=\n" + model.recommendProducts(789, 10).mkString("\n"))

    // Item recommendations
    val itemId = 567
    val itemFactor = model.productFeatures.lookup(itemId).head
    val itemVector = new DoubleMatrix(itemFactor)
    println("cosineSimilarity=" + cosineSimilarity(itemVector, itemVector))

    val sims = model.productFeatures.map{ case (id, factor) =>
      val factorVector = new DoubleMatrix(factor)
      val sim = cosineSimilarity(factorVector, itemVector)
      (id, sim)
    }
    val sortedSims = sims.top(10)(Ordering.by[(Int, Double), Double] { case (id, similarity) => similarity })
    println("cosineSimilarities=\n" + sortedSims.take(10).mkString("\n"))

    // SE (manual)
    val moviesForUser = ratings.keyBy(_.user).lookup(789)
    val actualRating = moviesForUser.take(1)(0)
    val predictedRating = model.predict(789, actualRating.product)
    val squaredError = math.pow(predictedRating - actualRating.rating, 2.0)
    println("squaredError=" + squaredError)

    // MSE/RMSE (manual)
    val usersProducts = ratings.map{ case Rating(user, product, rating) => (user, product) }
    val predictions = model.predict(usersProducts).map{ case Rating(user, product, rating) =>
      ((user, product), rating)
    }

    val ratingsAndPredictions = ratings.map{ case Rating(user, product, rating) =>
      ((user, product), rating) }.join(predictions)
    val MSE = ratingsAndPredictions.map{ case ((user, product), (actual, predicted)) => math.pow((actual - predicted), 2) }.reduce(_ + _) / ratingsAndPredictions.count
    val RMSE = math.sqrt(MSE)
    println(s"MSG=$MSE, RMSE=$RMSE")

    // MSE/RMSE (mllib)
    val predictedAndTrue = ratingsAndPredictions.map { case ((user, product), (actual, predicted)) => (actual, predicted) }
    val regressionMetrics = new RegressionMetrics(predictedAndTrue)
    println(s"MSE=${regressionMetrics.meanSquaredError}, RMSE=${regressionMetrics.rootMeanSquaredError}")

    // APK (manual)
    val topKRecs = model.recommendProducts(798, 10)
    val actualMovies = moviesForUser.map(_.product)
    val predictedMovies = topKRecs.map(_.product)
    val apk10 = avgPrecisionK(actualMovies, predictedMovies, 10)
    println(s"apk10=$apk10")

    // MAPK (manual)
    val itemFactors = model.productFeatures.map{ case (id, factor) => factor }.collect()
    val itemMatrix = new DoubleMatrix(itemFactors)
    println(s"(rows, cols)=(${itemMatrix.rows}, ${itemMatrix.columns})")

    val imBroadcast = sc.broadcast(itemMatrix)
    val allRecs = model.userFeatures.map{ case (userId, array) =>
      val userVector = new DoubleMatrix(array)
      val scores = imBroadcast.value.mmul(userVector)
      val sortedWithId = scores.data.zipWithIndex.sortBy(-_._1)
      val recommendedIds = sortedWithId.map(_._2 + 1).toSeq
      (userId, recommendedIds)
    }
    val userMovies = ratings.map{ case Rating(user, product, rating) => (user, product)}.groupBy(_._1).map{ case (k, v) => (k, v.map(_._2).toSeq) }

    val MAPK10 = allRecs.join(userMovies).map{ case (userId, (predicted, actual)) =>
      avgPrecisionK(actual, predicted, 10)
    }.reduce(_ + _) / allRecs.count
    println(s"MAPK10=${MAPK10}")

    val MAPK2000 = allRecs.join(userMovies).map{ case (userId, (predicted, actual)) =>
      avgPrecisionK(actual, predicted, 2000)
    }.reduce(_ + _) / allRecs.count
    println(s"MAPK2000=${MAPK2000}")

    // MAPK (mllib)
    val predictedAndTrueForRanking = allRecs.join(userMovies).map{ case (userId, (predicted, actual)) =>
      (predicted.toArray, actual.toArray)
    }
    val rankingMetrics = new RankingMetrics(predictedAndTrueForRanking)
    println(s"MAPK    =${rankingMetrics.meanAveragePrecision}")

  }

  def cosineSimilarity(vec1: DoubleMatrix, vec2: DoubleMatrix): Double = {
    vec1.dot(vec2) / (vec1.norm2() * vec2.norm2())
  }

  def avgPrecisionK(actual: Seq[Int], predicted: Seq[Int], k: Int): Double = {
    val predK = predicted.take(k)
    var score = 0.0
    var numHits = 0.0
    for ((p, i) <- predK.zipWithIndex) {
      if (actual.contains(p)) {
        numHits += 1.0
        score += numHits / (i.toDouble + 1.0)
      }
    }

    if (actual.isEmpty) {
      1.0
    } else {
      score / scala.math.min(actual.size, k).toDouble
    }
  }
}
