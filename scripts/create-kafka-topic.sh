#!/bin/bash
# Create Kafka topic purchase_inventory_merged if it does not exist
set -e
KAFKA_CONTAINER=${KAFKA_CONTAINER:-kafka}
TOPIC=${TOPIC:-purchase_inventory_merged}
PARTITIONS=${PARTITIONS:-1}
REPLICATION=${REPLICATION:-1}

# Wait for Kafka to be ready
sleep 10

docker exec "$KAFKA_CONTAINER" kafka-topics --create --if-not-exists \
  --topic "$TOPIC" \
  --bootstrap-server kafka:9092 \
  --partitions "$PARTITIONS" \
  --replication-factor "$REPLICATION"
