import org.apache.spark.{ SparkConf, SparkContext }

object ScalaApp {
  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf().setAppName("First Scala App").setMaster("local[4]"))
    val data = sc.textFile("data/UserPurchaseHistory.csv")
      .map(line => line.split(","))
      .map(record => (record(0), record(1), record(2)))

    val numPurchases = data.count()
    val uniqueUsers = data.map{ case (user, product, price) => user }.distinct().count()
    val totalRevenue = data.map{ case (user, product, price) => price.toDouble }.sum()
    val productByPopularity = data.map{ case (user, product, price) => (product, 1) }.reduceByKey(_ + _).collect().sortBy(-_._2)
    val mostPopular = productByPopularity(0)

    println("Total purchase: " + numPurchases)
    println("Unique users: " + uniqueUsers)
    println("Total revenue: " + totalRevenue)
    println("Most popular product: %s with %d purchases.".format(mostPopular._1, mostPopular._2))
  }
}
