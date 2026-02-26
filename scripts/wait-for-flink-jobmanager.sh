#!/bin/sh
# wait-for-flink-jobmanager.sh
# Wait until the Flink JobManager REST API is available

set -e

HOST="${1:-jobmanager}"
PORT="${2:-8081}"
TIMEOUT="${3:-120}"

for i in $(seq 1 "$TIMEOUT"); do
  if curl -sf "http://$HOST:$PORT/overview" >/dev/null; then
    echo "Flink JobManager REST API is up at $HOST:$PORT/overview"
    exit 0
  fi
  echo "Waiting for Flink JobManager REST API at $HOST:$PORT/overview... ($i/$TIMEOUT)"
  sleep 1
done

echo "Timeout waiting for Flink JobManager REST API at $HOST:$PORT/overview"
exit 1
