
#!/bin/sh
# Wait until the Flink JobManager REST API is available
# Usage: ./wait-for-flink-jobmanager.sh [HOST] [PORT] [TIMEOUT]

set -e

# Host (default: jobmanager), port (default: 8081), and timeout in seconds (default: 120)
HOST="${1:-jobmanager}"
PORT="${2:-8081}"
TIMEOUT="${3:-120}"

# Poll the JobManager REST API until it responds or timeout is reached
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
