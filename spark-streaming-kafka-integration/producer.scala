import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.HashMap
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Producer {
  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println("Usage: Consumer <brokers> <topic> <messagePerSecond>")
      System.exit(1)
    }
    val Array(brokers, topic, messagePerSecond) = args;

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
      for (i <- 1 to messagePerSecond.toInt) {
        val message = new ProducerRecord[String, String](topic, null, "hello")
        println("sending: hello")
        producer.send(message)
      }
      Thread.sleep(1000)
    }
  }
}
