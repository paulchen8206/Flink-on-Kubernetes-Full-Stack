# Flink on Kubernetes: Deployment Guide

## Prerequisites
- Kubernetes cluster (local with kind, or cloud)
- kubectl, Helm, Docker
- Flink job JAR and Docker image

## 1. Install Flink Kubernetes Operator
```sh
helm repo add flink-operator-repo https://downloads.apache.org/flink/flink-kubernetes-operator-1.7.0/
helm repo update
kubectl create namespace flink-system
helm install flink-kubernetes-operator flink-operator-repo/flink-kubernetes-operator --namespace flink-system
```

## 2. Prepare Flink Resources
- Edit and apply k8s/flink-resources.yaml for namespace, RBAC, PVC, secrets.

## 3. Build and Push Job Image
```sh
mvn clean package
docker build -t <your-repo>/flink-purchase-report:1.0 ./docker/purchase-report
docker push <your-repo>/flink-purchase-report:1.0
```

## 4. Deploy Flink Job
- Edit k8s/flink-deployments/flink-application-custom.yaml to use your image.
- Apply the deployment:
	```sh
	kubectl apply -f k8s/flink-deployments/flink-application-custom.yaml
	```

## 5. Monitor and Manage
- Port-forward Flink UI:
	```sh
	kubectl port-forward svc/purchase-report-app-rest -n flink 8081:8081
	# Open http://localhost:8081
	```
- Use kubectl and the Flink Operator for scaling, upgrades, and savepoints.

---
See the main README and Flink Operator docs for more advanced deployment scenarios.