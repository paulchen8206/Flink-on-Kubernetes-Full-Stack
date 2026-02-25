# Flink on Kubernetes: Architecture Overview

## Components
- **JobManager:** Orchestrates job execution, checkpointing, and recovery.
- **TaskManager:** Executes parallel tasks and manages state.
- **Flink Kubernetes Operator:** Manages Flink clusters and jobs via CRDs (FlinkDeployment, FlinkSessionJob).
- **Persistent Storage:** Used for checkpoints/savepoints (e.g., S3, PVC).

## Deployment Model
- Each Flink job runs as a Kubernetes custom resource (CRD).
- Operator watches CRDs and manages Flink clusters as pods/services.
- Resources (CPU/memory) are defined in the CRD spec.

## Networking
- Flink JobManager and TaskManagers communicate via Kubernetes services.
- Flink Web UI is exposed as a Kubernetes service (NodePort/LoadBalancer/port-forward).

## High Availability
- Use persistent storage for HA metadata and state.
- Operator restarts failed pods and restores from checkpoints.

---
See the [Flink Kubernetes Operator architecture docs](https://nightlies.apache.org/flink/flink-kubernetes-operator-docs-main/docs/concepts/architecture/) for more details.