#!/bin/bash
# Delete the kind cluster
set -e

CLUSTER_NAME=${1:-flink-kind}
kind delete cluster --name "$CLUSTER_NAME"
