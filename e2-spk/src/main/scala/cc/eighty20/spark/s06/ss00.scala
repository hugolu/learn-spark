package cc.eighty20.spark.s06

import java.util.HashMap
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import scala.io.Source
import scala.util.Random

object ss00 {
  def main(args: Array[String]) {
    if (args.length < 4) {
      System.err.println("Usage: ss00 <metadataBrokerList> <topic> <messagesPerSec> <dataFile>")
      System.exit(1)
    }

    val Array(brokers, topic, messagesPerSec, dataFile) = args

    val listOfLines = Source.fromFile(dataFile).getLines().toArray
    val linesCount = listOfLines.length
    val random = new Random()

    val props = new HashMap[String, Object]()
    props.put("bootstrap.servers", brokers)
    props.put("acks", "all")
    props.put("retries", "0")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)

    sys.ShutdownHookThread {
      producer.close()
    }

    while(true) {
      val a = 0
      for( a <- 1 to messagesPerSec.toInt) {
        val line = listOfLines(random.nextInt(linesCount))
        val message = new ProducerRecord[String, String](topic, null, line)

        println("Sending: " + line)
        producer.send(message);
      }
      Thread.sleep(1000)
    }
  }
}
