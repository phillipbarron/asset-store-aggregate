#!/bin/bash

set -eu
SCRIPTS_PATH=$(dirname $0)
KAFKA_PORT=9999

trap "echo 'Deleting containers:' && $SCRIPTS_PATH/test-mongo.sh --stop-and-remove && $SCRIPTS_PATH/test-kafka.sh --stop-and-remove" EXIT

echo "Starting containers."
$SCRIPTS_PATH/test-mongo.sh --start-mongo
KAFKA_PORT=${KAFKA_PORT} $SCRIPTS_PATH/test-kafka.sh --start-kafka

echo "Waiting for kafka..."
KAFKA_PORT=${KAFKA_PORT} $SCRIPTS_PATH/test-kafka.sh --wait-kafka

echo "Waiting for mongo..."
$SCRIPTS_PATH/test-mongo.sh --wait-mongo

# Run Cucumber Tests
if [ $# -gt 0 ]; then
    echo "Starting cucumber tests with tag > $1"
    KAFKA_PORT=${KAFKA_PORT} sbt "cucumber --tags $1"
else
    # Run all Cucumber Tests
    KAFKA_PORT=${KAFKA_PORT} sbt 'cucumber'
fi

echo "All done!"
