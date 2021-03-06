import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Consumer {
  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println("Usage: Consumer <brokers> <topic> <groupId>")
      System.exit(1)
    }
    val Array(brokers, topic, groupId) = args;

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> brokers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> groupId,
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val conf = new SparkConf().setAppName("Consumer").setMaster("local[2]")
    val streamingContext = new StreamingContext(conf, Seconds(1))

    val topics = topic.split(",")
    val stream = KafkaUtils.createDirectStream[String, String] (
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    stream.foreachRDD{ (rdd, time) =>
      rdd.foreach(record => println(s"${record.key}: ${record.value}"))
    }

    streamingContext.start()
    streamingContext.awaitTermination()
  }
}
