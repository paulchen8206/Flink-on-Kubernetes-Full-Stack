
#!/bin/bash
# Load a local Docker image into the kind cluster
# Usage: ./load-image-to-kind.sh <IMAGE_NAME> [CLUSTER_NAME]
set -e

# Image name to load (required)
IMAGE_NAME=${1:?Specify image name, e.g. your-repo/flink-purchase-report:1.0}
# Cluster name (default: flink-kind)
CLUSTER_NAME=${2:-flink-kind}

# Load the Docker image into the specified kind cluster
kind load docker-image "$IMAGE_NAME" --name "$CLUSTER_NAME"
