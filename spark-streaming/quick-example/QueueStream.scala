import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.rdd.RDD
import scala.collection.mutable.SynchronizedQueue

object QueueStream {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("QueueStream").setMaster("local[2]")
    val ssc = new StreamingContext(conf, Seconds(5))

    val rddQueue = new SynchronizedQueue[RDD[Int]]()
    val inputStream = ssc.queueStream(rddQueue)
    val mappedStream = inputStream.map((_, 1))
    val reduceStream = mappedStream.reduceByKey(_+_)
    reduceStream.print()

    ssc.start()
    for (i <- 1 to 30) {
      rddQueue += ssc.sparkContext.parallelize(1 to 100).map(_ % 10)
      Thread.sleep(1000)
    }
    ssc.stop()
  }
}
