import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.recommendation.{ ALS, Rating }
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.clustering.KMeans
import breeze.linalg._
import breeze.numerics.pow

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
      val genresAssigned = genres.zipWithIndex.filter{ case (g, idx) => g == "1" }.map{ case (g, idx) => genreMap(idx.toString) }
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
    movieVectors.cache

    val userFactors = alsModel.userFeatures.map{ case (id, factor) => (id, Vectors.dense(factor)) }
    val userVectors = userFactors.map(_._2)
    userVectors.cache

    // Normalization
    val movieMatrix = new RowMatrix(movieVectors)
    val movieMatrixSummary = movieMatrix.computeColumnSummaryStatistics()
    
    val userMatrix = new RowMatrix(userVectors)
    val userMatrixSummary = userMatrix.computeColumnSummaryStatistics()

    println("Movie factors mean: " + movieMatrixSummary.mean)
    println("Movie factors variance: " + movieMatrixSummary.variance)
    println("User factors mean: " + userMatrixSummary.mean)
    println("User factors variance: " + userMatrixSummary.variance)

    // Training a clustering model
    val numClusters = 5
    val numIterations = 10
    val numRuns = 3

    val movieClusterModel = KMeans.train(movieVectors, numClusters, numIterations, numRuns, "k-means||", 42)
    val movieClusterModelConverged = KMeans.train(movieVectors, numClusters, 100)

    val userClusterModel = KMeans.train(userVectors, numClusters, numIterations, numRuns, "k-means||", 42)
    val userClusterModelConverged = KMeans.train(userVectors, numClusters, 100)

    // Making predictions using a clustering model
    val movieCluster = movieClusterModel.predict(movieVectors.first)
    println(movieCluster)

    val predictions = movieClusterModel.predict(movieVectors)
    println(predictions.take(10).mkString(", "))

    // Interpreting the movie clusters
    def computeDistance(v1: DenseVector[Double], v2: DenseVector[Double]) = pow(v1 - v2, 2).sum

    val titlesWithFactors = titlesAndGenres.join(movieFactors)
    val movieAssigned = titlesWithFactors.map{ case (id, ((title, genres), vector)) =>
      val pred = movieClusterModel.predict(vector)
      val clusterCenter = movieClusterModel.clusterCenters(pred)
      val dist = computeDistance(DenseVector(clusterCenter.toArray), DenseVector(vector.toArray))
      (id, title, genres, pred, dist)
    }
    val clusterAssignments = movieAssigned.groupBy{ case (id, title, genres, cluster, dist) => cluster }.collectAsMap

    for ( (k, v) <- clusterAssignments.toSeq.sortBy(_._1)) {
      println(s"\nCluster $k: ")
      val m = v.toSeq.sortBy(_._5)
      println(m.take(10).map{ case (_, title, genres, _, d) => (title, genres, d) }.mkString("\n"))
    }

    // Evaluating the performance of clustering models
    val movieCost = movieClusterModel.computeCost(movieVectors)
    val userCost = userClusterModel.computeCost(userVectors)
    println("WCSS for movies: " + movieCost)
    println("WCSS for users: " + userCost)

    // Selecting K through cross-validation
    val Array(trainMovies, testMovies) = movieVectors.randomSplit(Array(0.6, 0.4), 123)
    trainMovies.cache; testMovies.cache
    val costsMovies = Seq(2, 3, 4, 5, 10, 20).map{ k =>
      (k, KMeans.train(trainMovies, numIterations, k, numRuns).computeCost(testMovies))
    }
    println("Movie clustering cross-validation: ")
    costsMovies.foreach{ case (k, cost) => println(f"WCSS for K=$k id $cost%2.2f") }

    val Array(trainUsers, testUsers) = userVectors.randomSplit(Array(0.6, 0.4), 123)
    trainUsers.cache; testUsers.cache
    val costsUsers = Seq(2, 3, 4, 5, 10, 20).map{ k =>
      (k, KMeans.train(trainUsers, numIterations, k, numRuns).computeCost(testUsers))
    }
    println("User clustering cross-validation: ")
    costsUsers.foreach{ case (k, cost) => println(f"WCSS for K=$k id $cost%2.2f") }
  }
}
