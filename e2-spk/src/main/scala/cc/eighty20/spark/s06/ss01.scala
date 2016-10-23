package cc.eighty20.spark.s06

import java.util.HashMap
import java.util.Arrays
import org.apache.kafka.clients.consumer.{KafkaConsumer, ConsumerRecord}
import scala.io.Source
import scala.util.Random
import collection.JavaConversions._

object ss01 {
  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println("Usage: ss01 <metadataBrokerList> <topic> <subGroup>")
      System.exit(1)
    }
    val Array(brokers, topic, subGroup) = args;

    val props = new HashMap[String, Object]()
    props.put("bootstrap.servers", brokers)
    props.put("group.id",subGroup) 
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("enable.auto.commit", "true")
    props.put("auto.commit.interval.ms", "1000")
    props.put("session.timeout.ms", "30000")

    val consumer = new KafkaConsumer[String, String](props)
    val topics: java.util.Collection[String] = topic.split(",").toSeq
    consumer.subscribe(topics)
    sys.ShutdownHookThread {
      consumer.close()
    }

    try {
      while(true) {
        val records = consumer.poll(1000)
        val itr = records.iterator()
        while(itr.hasNext()){
          val record = itr.next()
          println(s"${record.topic}: ${record.value}")
        }
      }
    } finally {
      consumer.close()
    }
  }
}
