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

## Deploying a Flink Application
See the deployment manifest in [flink-deployments/flink-application.yaml](flink-deployments/flink-application.yaml) and supporting resources in [flink-resources.yaml](flink-resources.yaml).

To deploy and monitor, use the provided manifests and scripts. See this directory for YAML files and ../scripts/ for automation.

## Monitoring
- Access the Flink Web UI by port-forwarding the JobManager service.
- Prometheus monitoring example: see monitoring/servicemonitor.yaml.

## State Backend with S3
- For production, use S3 for checkpoints/savepoints. See secrets/s3-credentials.yaml for the S3 secret example. Reference the secret in your deployment and set S3 config in flinkConfiguration.

## Failure Recovery & Savepoints
- See the scripts and deployment notes for failure recovery, savepoints, and restore procedures.

## References
- [Flink Documentation](https://nightlies.apache.org/flink/)
- [Flink Kubernetes Operator](https://nightlies.apache.org/flink/flink-kubernetes-operator-docs-main/)
