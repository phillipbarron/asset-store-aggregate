#!/bin/bash

set -e

UNIQUE_SUFFIX="test-optimo-history-api"
KAFKA_NAME="kafka-${UNIQUE_SUFFIX}"
ZOOKEEPER_NAME="zookeeper-${UNIQUE_SUFFIX}"

# These must be tags that exist in the docker library
CONFLUENT_PLATFORM_VERSION=5.1.3

function retryPort() {
    local port=$1
    local n=0

    until [ $n -ge 30 ]; do
        set +e
        nc -v -w 1 localhost ${port} </dev/null
        returnCode=$?

        set -e
        if [ ${returnCode} -eq  '0' ]; then
            echo "Connection to ${port} is available"
            exit 0;
        else
            n=$(($n + 1))
            echo "${n} - retrying connection to port ${port}";
            sleep 1
        fi
    done
}

function startZookeeper() {
    docker run --name ${ZOOKEEPER_NAME} -d -p 2181:2181 \
-e ZOOKEEPER_CLIENT_PORT=2181 \
confluentinc/cp-zookeeper:${CONFLUENT_PLATFORM_VERSION}
}

function startKafka() {
    docker run --name ${KAFKA_NAME} --link ${ZOOKEEPER_NAME} -d -p ${KAFKA_PORT}:${KAFKA_PORT} \
-e KAFKA_LISTENERS="PLAINTEXT://0.0.0.0:${KAFKA_PORT}" \
-e KAFKA_ADVERTISED_LISTENERS="PLAINTEXT://localhost:${KAFKA_PORT}" \
-e KAFKA_ZOOKEEPER_CONNECT=${ZOOKEEPER_NAME}:2181 \
-e KAFKA_BROKER_ID=0 \
-e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
confluentinc/cp-kafka:${CONFLUENT_PLATFORM_VERSION}
}

function stopAndRemoveAll() {
    docker container rm -f ${ZOOKEEPER_NAME} || true
    docker container rm -f ${KAFKA_NAME} || true
}

while [ "$1" != "" ]; do
    case $1 in
        --start-kafka )
            startZookeeper
            startKafka
            ;;
        --wait-kafka )
            retryPort ${KAFKA_PORT}
            ;;
        --logs-kafka )
            docker logs -f ${KAFKA_NAME}
            ;;
        --stop-and-remove )
            stopAndRemoveAll
            ;;
        * )
        echo "Unexpected $1"
        exit 1
    esac
    shift
done
