import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.rdd.RDD
import scala.collection.mutable.SynchronizedQueue

object StatefulWordCount {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("StatefulWordCount").setMaster("local[2]")
    val ssc = new StreamingContext(conf, Seconds(3))
    ssc.checkpoint("checkpoint")

    val lines = ssc.socketTextStream("localhost", 9999)
    val words = lines.flatMap(_.split(" "))
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.updateStateByKey[Int](updateFunction _)
    wordCounts.print()

    ssc.start()
    ssc.awaitTermination()
  }

  def updateFunction(newValues: Seq[Int], runningCount: Option[Int]): Option[Int] = {
    val currentCount = newValues.fold(0)(_+_)
    val previousCount = runningCount.getOrElse(0)
    Some(currentCount + previousCount)
  }
}
