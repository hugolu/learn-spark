import java.util.HashMap
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object Producer1 {
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
 
    val event= Event(user, System.currentTimeMillis(), "heartbeat")
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    while(true) {
      for (i <- 1 to messagePerSecond.toInt) {
        event.evt_dt = System.currentTimeMillis()
        val message = new ProducerRecord[String, String](topic, user, mapper.writeValueAsString(event))
        println(s"${event.name}: ${event.evt_dt}")
        producer.send(message)

        Thread.sleep(1000/messagePerSecond.toInt)
      }
    }
  }
}
