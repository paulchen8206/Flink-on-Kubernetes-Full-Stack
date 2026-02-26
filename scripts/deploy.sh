
#!/bin/bash
# Deploy Flink application and resources to Kubernetes (kind)
# Usage: ./deploy.sh
set -e

# Step 1: Build the job JAR and Docker image
echo "Building job JAR and Docker image..."
# Build the Flink job JAR using Maven
mvn clean package -f ../flink-jobs/purchase-report/pom.xml
# Build the Docker image for the job
docker build -t purchase-report-job:latest ../docker/purchase-report

# Step 2: Load the Docker image into the kind cluster
echo "Loading Docker image into kind cluster..."
./scripts/load-image-to-kind.sh purchase-report-job:latest

# Step 3: Apply Kubernetes manifests for Flink resources and deployment
echo "Applying Kubernetes resources..."
kubectl apply -f ../k8s/flink-resources.yaml
kubectl apply -f ../k8s/flink-deployments/flink-application-custom.yaml

echo "Deployment complete. Use 'kubectl get pods -n flink' to check status."