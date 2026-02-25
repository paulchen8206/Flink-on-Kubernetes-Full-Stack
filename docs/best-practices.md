# Best Practices for Flink on Kubernetes

## 1. State Management
- Use RocksDB for large state, Memory backend for small state only.
- Enable incremental checkpoints to reduce checkpoint overhead.
- Store checkpoints/savepoints in durable storage (e.g., S3).

## 2. Resource Sizing
- Size TaskManager memory for RocksDB and network buffers.
- Match parallelism to available slots and workload.

## 3. Job Upgrades
- Always upgrade jobs using savepoints for stateful jobs.
- Test savepoint compatibility before production upgrades.

## 4. Monitoring & Alerting
- Integrate with Prometheus and Grafana for metrics.
- Monitor backpressure and checkpointing stats.

## 5. Reliability
- Use exactly-once semantics for critical workloads.
- Retain multiple savepoints for rollback.
- Regularly test failure and recovery scenarios.

## 6. Security
- Use Kubernetes secrets for sensitive data (e.g., S3 credentials).
- Restrict RBAC permissions to least privilege.

## 7. Automation
- Use CI/CD pipelines for build, test, and deployment.
- Automate scaling and recovery where possible.

---
For more, see the [Flink documentation](https://nightlies.apache.org/flink/flink-docs-release-1.18/).