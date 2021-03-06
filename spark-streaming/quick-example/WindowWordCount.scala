import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{StreamingContext, Seconds}

object WindowWordCount {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("WindowWordCount").setMaster("local[2]")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(1))
    ssc.checkpoint("checkpoint")

    val lines = ssc.socketTextStream("localhost", 9999)
    val words = lines.flatMap(_.split(" "))
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKeyAndWindow(_ + _, _ - _, Seconds(3), Seconds(2))
    wordCounts.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
