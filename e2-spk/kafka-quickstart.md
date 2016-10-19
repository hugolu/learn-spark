# Kafka Quick Start

## 參考連結
- [Apache Kafka Documentation](http://kafka.apache.org/documentation.html#quickstart)
- [Apache Kafka on Docker](https://hub.docker.com/r/ches/kafka/)

## 下載 docker images
```shell
$ docker pull jplock/zookeeper
$ docker pull ches/kafka
```

## 啟動服務
```shell
$ docker run -d --name zookeeper jplock/zookeeper
$ docker run -d --name kafka --link zookeeper:zookeeper ches/kafka
```

## 取得 server ip
```shell
export ZOOKEEPER_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' zookeeper)
export KAFKA_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' kafka)
```

## 創建 topic
```shell
$ docker run --rm ches/kafka kafka-topics.sh --create \
--zookeeper ${ZOOKEEPER_IP}:2181 \
--replication-factor 1 \
--partitions 1 \
--topic test
```
```shell
$ docker run --rm ches/kafka kafka-topics.sh --list \
--zookeeper ${ZOOKEEPER_IP}:2181
```

## 單一 Pruducer, 單一 Consumer
```shell
$ docker run --rm -i ches/kafka kafka-console-producer.sh \
--broker-list ${KAFKA_IP}:9092 \
--topic test
This is a message
This is another message
```
```shell
$ docker run --rm -i ches/kafka kafka-console-consumer.sh \
--zookeeper ${ZOOKEEPER_IP}:2181 \
--topic test --from-beginning
This is a message
This is another message
```
