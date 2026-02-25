#!/bin/bash
# Prepare local development environment for Flink on Kubernetes in Docker
set -e

# 1. Start kind cluster
./scripts/start-kind.sh

# 2. Install Helm if not present
if ! command -v helm &> /dev/null; then
  echo "Helm not found. Please install Helm manually."
  exit 1
fi

# 3. Install Flink Kubernetes Operator
kubectl create namespace flink-system || true
helm repo add flink-operator-repo https://downloads.apache.org/flink/flink-kubernetes-operator-1.7.0/
helm repo update
helm install flink-kubernetes-operator flink-operator-repo/flink-kubernetes-operator --namespace flink-system || true
