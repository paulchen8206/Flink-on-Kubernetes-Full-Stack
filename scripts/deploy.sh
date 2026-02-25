#!/bin/bash
# Deploy Flink application and resources to Kubernetes (kind)
# Usage: ./deploy.sh
set -e

# 1. Build the job JAR and Docker image
echo "Building job JAR and Docker image..."
mvn clean package -f ../flink-jobs/purchase-report/pom.xml
docker build -t purchase-report-job:latest ../docker/purchase-report

# 2. Load the image into kind
echo "Loading Docker image into kind cluster..."
./scripts/load-image-to-kind.sh purchase-report-job:latest

# 3. Apply Kubernetes manifests
echo "Applying Kubernetes resources..."
kubectl apply -f ../k8s/flink-resources.yaml
kubectl apply -f ../k8s/flink-deployments/flink-application-custom.yaml

echo "Deployment complete. Use 'kubectl get pods -n flink' to check status."