# Kubernetes Manifests for Flink

This directory contains Kubernetes manifests for deploying and managing Apache Flink and related resources.

## Structure
- `flink-deployments/`: FlinkApplication and FlinkDeployment CRDs for running Flink jobs via the Flink Kubernetes Operator.
- `secrets/`: Kubernetes Secret manifests (e.g., S3 credentials, sensitive configs).
- `monitoring/`: Monitoring resources such as ServiceMonitor for Prometheus integration.
- `flink-resources.yaml`: Namespace, RBAC, PersistentVolumeClaim, and other shared resources for Flink clusters.


## Usage
- Edit and apply the manifests as needed for your environment.
- For local S3 emulation and integration testing, use LocalStack and MinIO via Docker Compose (see project root README).
- See the main project README and docs/deployment-guide.md for deployment instructions.

## References
- [Flink Kubernetes Operator](https://nightlies.apache.org/flink/flink-kubernetes-operator-docs-main/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
