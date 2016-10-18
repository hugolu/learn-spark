#!/bin/bash

if [ $# == 0 ]; then
  echo "env.sh <ip address>"
fi
export ZOOKEEPER_HOST_IP=$1
export KAFKA_HOST_IP=$1
