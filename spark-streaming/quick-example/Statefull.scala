import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.rdd.RDD
import scala.collection.mutable.SynchronizedQueue

object Stateful {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Stateful").setMaster("local[2]")
    val ssc = new StreamingContext(conf, Seconds(5))
    ssc.checkpoint(".")

    val rddQueue = new SynchronizedQueue[RDD[String]]()
    val inputStream = ssc.queueStream(rddQueue)
    val mappedStream = inputStream.map((_, 1))
    val reduceStream = mappedStream.updateStateByKey[Int](updateFunction _)
    reduceStream.print()

    ssc.start()
    for (i <- 1 to 30) {
      val alpha = Array("A","B","C","D","E","F")
      def randNum = scala.util.Random.nextInt(alpha.length)
      val alphas = (1 to 100).toSeq.map(n => alpha(randNum))
      rddQueue += ssc.sparkContext.makeRDD(alphas)
      Thread.sleep(1000)
    }
    ssc.stop()
  }

  def updateFunction(newValues: Seq[Int], runningCount: Option[Int]): Option[Int] = {
    val currentCount = newValues.fold(0)(_+_)
    val previousCount = runningCount.getOrElse(0)
    Some(currentCount + previousCount)
  }
}
