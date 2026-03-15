
#!/bin/bash
# Start a local Kubernetes cluster using kind (Kubernetes in Docker)
# Usage: ./start-kind.sh [CLUSTER_NAME] [K8S_VERSION]
set -e

# Set cluster name (default: flink-kind) and Kubernetes version (default: v1.28.0)
CLUSTER_NAME=${1:-flink-kind}
K8S_VERSION=${2:-v1.28.0}

# Create the kind cluster with the specified name and version
kind create cluster --name "$CLUSTER_NAME" --image "kindest/node:$K8S_VERSION" --wait 120s

# Show cluster info for the created kind cluster
kubectl cluster-info --context kind-$CLUSTER_NAME
