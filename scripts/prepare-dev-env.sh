
#!/bin/bash
# Prepare local development environment for Flink on Kubernetes in Docker
# Usage: ./prepare-dev-env.sh
set -e

# Step 1: Start kind cluster (Kubernetes in Docker)
./scripts/start-kind.sh

# Step 2: Check if Helm is installed, prompt if missing
if ! command -v helm &> /dev/null; then
  echo "Helm not found. Please install Helm manually."
  exit 1
fi

# Step 3: Install Flink Kubernetes Operator using Helm
# Create namespace if it doesn't exist
kubectl create namespace flink-system || true
# Add the Flink Operator Helm repo
helm repo add flink-operator-repo https://downloads.apache.org/flink/flink-kubernetes-operator-1.7.0/
helm repo update
# Install the Flink Kubernetes Operator chart
helm install flink-kubernetes-operator flink-operator-repo/flink-kubernetes-operator --namespace flink-system || true
