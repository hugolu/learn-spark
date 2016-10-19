# Kafka Quick Start

## 參考連結
- [Apache Kafka Documentation](http://kafka.apache.org/documentation.html#quickstart)
- [Apache Kafka on Docker](https://hub.docker.com/r/ches/kafka/)

## 下載 docker images
```shell
$ docker pull jplock/zookeeper
$ docker pull ches/kafka
```

## 設定環境
```shell
# source env.sh 192.168.0.105
export ZOOKEEPER_HOST_IP=$1
export KAFKA_HOST_IP=$1
```

## 啟動服務
```shell
$ docker run --name zookeeper \
-p 2181:2181 -p 2888:2888 -p 3888:3888 \
-d jplock/zookeeper
```
```shell
$ docker run --name kafka \
-p 9092:9092 -p 7203:7203 \
--env KAFKA_ADVERTISED_HOST_NAME=${KAFKA_HOST_IP} \
--env ZOOKEEPER_IP=${ZOOKEEPER_HOST_IP} \
-d ches/kafka
```
- `KAFKA_ADVERTISED_HOST_NAME=<container's IP within docker0's subnet>`
  - Maps to Kafka's `advertised.host.name` setting.
  - This setting should reflect the address at which producers can reach the broker on the network.
- `ZOOKEEPER_IP=<taken from linked "zookeeper" container, if available>`
  - Used in constructing Kafka's `zookeeper.connect` setting.
  - Required if no container is linked with the alias "zookeeper" and publishing port 2181, or not using ZOOKEEPER_CONNECTION_STRING instead. 
  
## 創建 topic
```shell
$ docker run --rm ches/kafka kafka-topics.sh --create \
--zookeeper ${ZOOKEEPER_HOST_IP}:2181 \
--replication-factor 1 \
--partitions 1 \
--topic test
```
```shell
$ docker run --rm ches/kafka kafka-topics.sh --list \
--zookeeper ${ZOOKEEPER_HOST_IP}:2181
```

