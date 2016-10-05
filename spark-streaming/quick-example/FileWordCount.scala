import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

object FileWordCount {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("FileWordCount").setMaster("local[2]")
    val ssc = new StreamingContext(conf, Seconds(5))

    val lines = ssc.textFileStream("wordCount")
    val words = lines.flatMap(_.split(" "))
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_+_)
    wordCounts.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
