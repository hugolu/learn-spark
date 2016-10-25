import java.util.HashMap
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

object Producer {
  def main(args: Array[String]) {
    if (args.length < 4) {
      System.err.println("Usage: Producer <broker> <topic> <user> <messagePerSecond>")
      System.exit(1)
    }
    val Array(broker, topic, user, messagePerSecond) = args;

    val props = new HashMap[String, Object]()
    props.put("bootstrap.servers", broker)
    props.put("acks", "all")
    props.put("retries", "0")
    props.put("key.serializer", classOf[StringSerializer])
    props.put("value.serializer", classOf[StringSerializer])

    val producer = new KafkaProducer[String, String](props)
    sys.ShutdownHookThread {
      producer.close()
    }
 
    while(true) {
      for (i <- 1 to messagePerSecond.toInt) {
        val value = System.currentTimeMillis().toString
        val message = new ProducerRecord[String, String](topic, user, value)
        println(s"$user: $value")
        producer.send(message)

        Thread.sleep(1000/messagePerSecond.toInt)
      }
    }
  }
}
