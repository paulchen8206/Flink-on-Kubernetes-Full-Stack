# Flink Helm Chart

This Helm chart deploys Apache Flink JobManager and related resources on Kubernetes.

## Structure
- `Chart.yaml`: Chart metadata
- `values.yaml`: Default configuration values
- `templates/`: Kubernetes manifest templates (Deployment, Service, ConfigMap, etc.)

## Usage
1. Customize `values.yaml` as needed (image, job JAR, main class, etc).
2. Install the chart:
   ```sh
   helm install my-flink ./helm/flink
   ```
3. Upgrade or uninstall as needed:
   ```sh
   helm upgrade my-flink ./helm/flink
   helm uninstall my-flink
   ```

## Configuration

The chart supports full customization via `values.yaml`:
- Custom job parameters (Kafka, Postgres, Flink)
- Resource requests/limits for JobManager and TaskManager
- Automatic mounting of a YAML config file (`flink-job-config.yaml`) via ConfigMap

Example `values.yaml`:
```yaml
job:
  kafkaBootstrap: kafka:9092
  purchasesTopic: store.purchases
  inventoriesTopic: store.inventories
  groupIdPrefix: flink-job
  pgHost: postgres
  pgPort: 5432
  pgDb: purchase_db
  pgUser: postgres
  pgPassword: postgres
  mergedTable: purchase_inventory_merged
  checkpointInterval: 10000
  parallelism: 1
  configFile: /opt/flink/flink-job-config.yaml
resources:
  jobmanager:
    requests:
      cpu: "500m"
      memory: "1Gi"
    limits:
      cpu: "1"
      memory: "2Gi"
```

The chart automatically mounts `flink-job-config.yaml` into the container (generated from `values.yaml`). Your Flink job can read this file for runtime configuration.

## Scaling & Upgrading

Use `helm upgrade` for configuration changes or new images. For advanced scaling and patching, see the project [scripts/](../../scripts/) directory and the main [README](../../README.md).

## References

- [Helm Documentation](https://helm.sh/docs/)
- [Flink Kubernetes Operator](https://nightlies.apache.org/flink/flink-kubernetes-operator-docs-main/)
