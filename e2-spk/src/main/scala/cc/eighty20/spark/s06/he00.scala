package cc.eighty20.spark.s06

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import com.fasterxml.jackson.databind.ObjectMapper
import scala.io.Source
import scala.util.Random
import cc.eighty20.spark.s06.dto.Heartbeat

object he00 {
  def main(args: Array[String]) {
    if (args.length < 4) {
      System.err.println("Usage: he00 <brokers> <topic> <userName> <beatsPerMinute>")
      System.exit(1)
    }
    val Array(brokers, topic, userName, beatsPerMinute) = args;

    val pauseMillis = Math.round(60000/Integer.parseInt(beatsPerMinute))
    val om = new ObjectMapper()

    val props = new Properties()
    props.put("bootstrap.servers", brokers)
    props.put("acks", "all")
    props.put("retries", "0")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    sys.ShutdownHookThread {
      producer.close()
    }

    val evt = new Heartbeat()
    evt.setName(userName)
    evt.setEvt_type("heartbeat")

    while(true) {
      evt.setEvt_dt(System.currentTimeMillis())

      val message = new ProducerRecord[String, String](topic, userName, om.writeValueAsString(evt))
      producer.send(message)
      println("Heartbeating: " + evt.toString)
      Thread.sleep(pauseMillis)
    }
  }
}
