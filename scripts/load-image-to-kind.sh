#!/bin/bash
# Load a local Docker image into the kind cluster
set -e

IMAGE_NAME=${1:?Specify image name, e.g. your-repo/flink-word-count:1.0}
CLUSTER_NAME=${2:-flink-kind}

kind load docker-image "$IMAGE_NAME" --name "$CLUSTER_NAME"
