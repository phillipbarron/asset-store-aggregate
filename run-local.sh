#!/bin/bash

set -eu
SCRIPTS_PATH="./ci"
KAFKA_PORT=9999

trap "echo 'Deleting containers:' && $SCRIPTS_PATH/test-postgres.sh --stop-and-remove && $SCRIPTS_PATH/test-kafka.sh --stop-and-remove" EXIT

echo "Starting containers."
$SCRIPTS_PATH/test-postgres.sh --start-postgres
KAFKA_PORT=${KAFKA_PORT} $SCRIPTS_PATH/test-kafka.sh --start-kafka

echo "Waiting for kafka..."
KAFKA_PORT=${KAFKA_PORT} $SCRIPTS_PATH/test-kafka.sh --wait-kafka

echo "Waiting for postgres..."
$SCRIPTS_PATH/test-postgres.sh --wait-postgres
sleep 5

echo "Creating events table in postgres."
psql -h localhost -p 5432 -U postgres -f ./infrastructure/database/001-create-events-table.sql

echo "Starting app on port 8080"
sbt run
