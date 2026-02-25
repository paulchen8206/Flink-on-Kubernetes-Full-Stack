# Flink Helm Chart

This Helm chart deploys Apache Flink JobManager and related resources on Kubernetes.

## Structure
- `Chart.yaml`: Chart metadata
- `values.yaml`: Default configuration values
- `templates/`: Kubernetes manifest templates (Deployment, Service, etc.)

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

## References
- [Helm Documentation](https://helm.sh/docs/)
- [Flink Kubernetes Operator](https://nightlies.apache.org/flink/flink-kubernetes-operator-docs-main/)
