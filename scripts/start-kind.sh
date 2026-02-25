#!/bin/bash
# Start a local Kubernetes cluster using kind (Kubernetes in Docker)
set -e

CLUSTER_NAME=${1:-flink-kind}
K8S_VERSION=${2:-v1.28.0}

kind create cluster --name "$CLUSTER_NAME" --image "kindest/node:$K8S_VERSION" --wait 120s
kubectl cluster-info --context kind-$CLUSTER_NAME
