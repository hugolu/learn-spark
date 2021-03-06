import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SocketWordCount {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("SocketWordCount").setMaster("local[2]")
    val ssc = new StreamingContext(conf, Seconds(3))

    val lines = ssc.socketTextStream("localhost", 9999)
    val words = lines.flatMap(_.split(" "))
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_+_)
    wordCounts.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
