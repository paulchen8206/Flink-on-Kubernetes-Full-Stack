# Flink on Kubernetes: Operations Guide

This guide covers key operational tasks for running Apache Flink on Kubernetes using the Flink Kubernetes Operator.

## 1. Cluster Lifecycle
- **Start/Stop:** Use `kubectl apply` to start and `kubectl delete` to stop Flink clusters and jobs.
- **Scaling:**
	- Scale TaskManagers:
		```sh
		kubectl patch flinkdeployment purchase-report-app -n flink --type merge -p '{"spec":{"taskManager":{"replicas":5}}}'
		```
	- Increase job parallelism:
		```sh
		kubectl patch flinkdeployment purchase-report-app -n flink --type merge -p '{"spec":{"job":{"parallelism":16}}}'
		```

## 2. Monitoring
- **Flink Web UI:**
	- Port-forward:
		```sh
		kubectl port-forward svc/purchase-report-app-rest -n flink 8081:8081
		# Open http://localhost:8081
		```
- **Prometheus:**
	- Use ServiceMonitor and configure Flink for Prometheus metrics.

## 3. Savepoints & Upgrades
- **Trigger savepoint:**
	```sh
	kubectl patch flinkdeployment purchase-report-app -n flink --type merge -p '{"spec":{"job":{"savepointTriggerNonce":1}}}'
	```
- **Upgrade job with savepoint:**
	1. Build and push new image.
	2. Patch deployment with new image:
		 ```sh
		 kubectl patch flinkdeployment purchase-report-app -n flink --type merge -p '{"spec":{"image":"your-repo/flink-purchase-report:1.1"}}'
		 ```

## 4. Failure Recovery
- **Simulate TaskManager failure:**
	```sh
	kubectl delete pod <taskmanager-pod> -n flink
	```
- Flink Operator will restart pods and restore from last checkpoint.

## 5. Log Access
- **Get logs:**
	```sh
	kubectl logs <jobmanager-pod> -n flink
	kubectl logs <taskmanager-pod> -n flink
	```

## 6. Common kubectl Commands
- List deployments: `kubectl get flinkdeployment -n flink`
- List jobs: `kubectl get flinksessionjob -n flink`
- List pods: `kubectl get pods -n flink`

---
For more, see the official [Flink Kubernetes Operator documentation](https://nightlies.apache.org/flink/flink-kubernetes-operator-docs-main/).