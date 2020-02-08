#!/bin/bash

set -eu
SCRIPTS_PATH="./ci"
KAFKA_PORT=9999

trap "echo 'Deleting containers:' && ${SCRIPTS_PATH}/test-mongo.sh --stop-and-remove && ${SCRIPTS_PATH}/test-kafka.sh --stop-and-remove" EXIT

echo "Starting containers."
${SCRIPTS_PATH}/test-mongo.sh --start-mongo
KAFKA_PORT=${KAFKA_PORT} ${SCRIPTS_PATH}/test-kafka.sh --start-kafka

echo "Waiting for kafka..."
KAFKA_PORT=${KAFKA_PORT} ${SCRIPTS_PATH}/test-kafka.sh --wait-kafka

echo "Waiting for mongo..."
${SCRIPTS_PATH}/test-mongo.sh --wait-mongo
sleep 5

echo "Creating assets collection in mongo."

#mongo --eval "db use test-assets-db"
#mongo --eval "db.createCollection('_assets')"

echo "Starting app on port 8080"
sbt run
