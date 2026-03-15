
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

echo "Ensuring joined table exists in Postgres..."
docker-compose -f docker/docker-compose.yml exec -T postgres psql -U postgres -d purchase_db -c "CREATE TABLE IF NOT EXISTS purchase_inventory_merged (id SERIAL PRIMARY KEY, transaction_time TIMESTAMP, transaction_id VARCHAR(64), product_id VARCHAR(32), price DOUBLE PRECISION, quantity INT, state VARCHAR(16), is_member BOOLEAN, member_discount DOUBLE PRECISION, add_supplements BOOLEAN, supplement_price DOUBLE PRECISION, total_purchase DOUBLE PRECISION, event_time TIMESTAMP, existing_level INT, stock_quantity INT, new_level INT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"
echo "Deployment complete. Use 'kubectl get pods -n flink' to check status."