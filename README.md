---
---



# Flink on Kubernetes: End-to-End Streaming Stack

Deploy and manage Apache Flink jobs on Kubernetes with a modern, production-ready stack.

## Features
- **Flink streaming jobs** (Java, Maven)
- **Kafka** for event ingestion
- **PostgreSQL** for sinks
- **Helm** and **Kubernetes** manifests for deployment
- **Docker Compose** for local development
- **CI/CD** pipeline for build, test, and deploy

---

## Table of Contents
- [Overview & Architecture](#overview--architecture)
- [Repository Structure](#repository-structure)
- [Quick Start](#quick-start)
- [Development & Packaging](#development--packaging)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Scaling, Upgrades, Savepoints](#scaling-upgrades-savepoints)
- [Monitoring & Observability](#monitoring--observability)
- [CI/CD Pipeline](#cicd-pipeline)
- [Best Practices](#best-practices)
- [References](#references)

---

## Overview & Architecture

This project demonstrates a full streaming data pipeline:
- **Flink job**: Reads purchase events from Kafka, writes to PostgreSQL
- **Kafka**: Event broker for streaming data
- **Postgres**: Sink for processed events
- **salesgen**: Synthetic event generator for Kafka
- **LocalStack/MinIO**: S3-compatible storage for state backend (optional)
- **Helm/K8s**: Production deployment
- **Docker Compose**: Local dev stack

```mermaid
graph TD;
  salesgen-->kafka;
  kafka-->flink;
  flink-->postgres;
  flink-->|checkpoints/savepoints|s3[(S3/MinIO/LocalStack)];
  flink-->prometheus[(Prometheus)];
  kafka-ui-.->kafka;
```

---


## Repository Structure

| Path | Description |
|------|-------------|
| `flink-jobs/purchase-report/` | Main Flink job (Java, Maven) |
| `docker/` | Docker Compose, Dockerfiles |
| `salesgen/` | Synthetic event generator (Python) |
| `pgsql/` | Postgres init scripts |
| `helm/flink/` | Helm chart for Flink deployment |
| `k8s/` | K8s manifests: CRDs, RBAC, PVC, monitoring |
| `ci-cd/` | CI/CD pipeline (GitHub Actions) |
| `scripts/` | Automation scripts (build, deploy, scale, upgrade, etc.) |
| `docs/` | Architecture, deployment, ops docs |

---


## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 11+
- Maven
- Python 3 (for salesgen)
- kind (for local K8s)
- kubectl, helm

### Local Dev Stack

```sh
make -f Makefile.docker all
# or manually:
cd flink-jobs/purchase-report && mvn clean package
docker build -f docker/Dockerfile -t purchase-report-job:latest .
docker-compose -f docker/docker-compose.yml up
```

Access:
- Flink UI: http://localhost:8081
- Kafka UI: http://localhost:8082
- Postgres: localhost:5432 (user/pass: postgres)

---


## Development & Packaging

- Edit Flink job: `flink-jobs/purchase-report/src/main/java/com/example/flink/KafkaToPostgresDb.java`
- Build JAR: `make build-jar` or `mvn clean package`
- Build Docker image: `make build-docker` or `docker build ...`
- Run stack: `make -f Makefile.docker up`
- Generate events: salesgen auto-starts, or run manually

---


**Flink Job:**
- Java 11+, Flink 1.20, Kafka connector, JDBC connector
- See `pom.xml` for dependencies

**Build steps:**
```sh
cd flink-jobs/purchase-report
mvn clean package
cd ../..
docker build -f docker/Dockerfile -t purchase-report-job:latest .
```

---


## Kubernetes Deployment

### 1. Prepare Cluster
- Start kind: `./scripts/start-kind.sh`
- Install Flink K8s Operator: `./scripts/prepare-dev-env.sh`

### 2. Build & Load Image
```sh
./scripts/build-jar.sh
./scripts/build-docker.sh
./scripts/load-image-to-kind.sh purchase-report-job:latest
```

### 3. Deploy Resources
```sh
kubectl apply -f k8s/flink-resources.yaml
kubectl apply -f k8s/flink-deployments/flink-application.yaml
```

### 4. Monitor
```sh
kubectl get pods -n flink
kubectl port-forward svc/purchase-report-app-rest -n flink 8081:8081
# Access Flink UI at http://localhost:8081
```

---


## Scaling, Upgrades, Savepoints

- Scale TaskManagers: `./scripts/scale-taskmanagers.sh <replicas>`
- Trigger savepoint: `./scripts/trigger-savepoint.sh <job_id>`
- Upgrade job: `./scripts/upgrade-job.sh`
- Parallelism: edit K8s manifest or patch CRD

---


## Monitoring & Observability

- Prometheus ServiceMonitor: `k8s/monitoring/servicemonitor.yaml`
- Flink metrics exported on port 9249
- View logs: `make -f Makefile.docker logs` or `kubectl logs`

---


## CI/CD Pipeline

- GitHub Actions: `ci-cd/pipeline.yaml`
  - Build JAR with Maven
  - Build & push Docker image
  - Deploy to K8s with Helm

---


## Best Practices

- Use RocksDB for large state
- Enable incremental checkpoints
- Use exactly-once semantics
- Monitor backpressure and metrics
- Retain savepoints for rollback
- Test recovery and upgrade flows

---


---

## References

- [Flink Documentation](https://nightlies.apache.org/flink/)
- [Flink Kubernetes Operator](https://nightlies.apache.org/flink/flink-kubernetes-operator-docs-main/)
- [Helm](https://helm.sh/docs/)
- [Kubernetes](https://kubernetes.io/docs/)

---

## License

Apache License 2.0

