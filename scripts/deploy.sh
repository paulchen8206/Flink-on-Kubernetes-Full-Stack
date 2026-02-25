#!/bin/bash
# Deploy Flink application and resources to Kubernetes (kind)
# Usage: ./deploy.sh
set -e

# 1. Build the job JAR and Docker image
echo "Building job JAR and Docker image..."
mvn clean package -f ../flink-jobs/word-count/pom.xml
docker build -t word-count-job:latest ../docker/word-count

# 2. Load the image into kind
echo "Loading Docker image into kind cluster..."
./scripts/load-image-to-kind.sh word-count-job:latest

# 3. Apply Kubernetes manifests
echo "Applying Kubernetes resources..."
kubectl apply -f ../k8s/flink-resources.yaml
kubectl apply -f ../k8s/flink-deployments/flink-application-custom.yaml

echo "Deployment complete. Use 'kubectl get pods -n flink' to check status."