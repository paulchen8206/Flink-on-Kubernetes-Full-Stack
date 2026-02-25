---
---

# Flink-Kafka-Postgres-AWS-Helm-Kubernetes

Deploy and manage Apache Flink stream processing jobs on Kubernetes using the Flink Kubernetes Operator, Helm, and containerized local development tools.

---


## Table of Contents

- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [Deployment Options](#deployment-options)
- [Script Organization & Makefile Usage](#script-organization--makefile-usage)
- [Developing & Packaging Flink Jobs](#developing--packaging-flink-jobs)
- [Operations: Scaling, Monitoring, Recovery](#operations-scaling-monitoring-recovery)
- [Best Practices](#best-practices)
- [References](#references)
- [Conclusion](#conclusion)

---


## Repository Layout

| Directory/File | Purpose |
| ------------- | ------- |
| ci-cd/ | CI/CD pipeline configuration ([ci-cd/pipeline.yaml](ci-cd/pipeline.yaml)) |
| config/ | Flink and logging configuration files |
| &nbsp;&nbsp;&nbsp;&nbsp;flink-conf/ | Flink application configs |
| &nbsp;&nbsp;&nbsp;&nbsp;logging/ | Log4j and logging configs |
| docker/ | Docker Compose and Dockerfiles for local development and job images |
| &nbsp;&nbsp;&nbsp;&nbsp;docker-compose.yml | Local stack with Flink, Kafka, MinIO, LocalStack, salesgen, Kafka UI |
| &nbsp;&nbsp;&nbsp;&nbsp;Dockerfile | Base Docker image for Flink jobs |
| docs/ | Documentation |
| &nbsp;&nbsp;&nbsp;&nbsp;architecture.md | System architecture |
| &nbsp;&nbsp;&nbsp;&nbsp;best-practices.md | Flink and Kubernetes best practices |
| &nbsp;&nbsp;&nbsp;&nbsp;deployment-guide.md | Step-by-step deployment instructions |
| &nbsp;&nbsp;&nbsp;&nbsp;operations.md | Operations and troubleshooting |
| flink-jobs/ | Flink job source code and build files |
| &nbsp;&nbsp;&nbsp;&nbsp;purchase-report/ | Example Flink job (Java, Maven) |
| helm/ | Helm chart for templated Flink deployment |
| &nbsp;&nbsp;&nbsp;&nbsp;flink/ | Helm chart, values, and templates |
| k8s/ | Kubernetes manifests and resources |
| &nbsp;&nbsp;&nbsp;&nbsp;flink-resources.yaml | Namespace, RBAC, PVC, etc. |
| &nbsp;&nbsp;&nbsp;&nbsp;flink-deployments/ | FlinkDeployment CRDs for jobs |
| &nbsp;&nbsp;&nbsp;&nbsp;monitoring/ | Prometheus ServiceMonitor, metrics config |
| &nbsp;&nbsp;&nbsp;&nbsp;namespaces/, rbac/, secrets/, storage/ | Supporting manifests |


## Final Notes

---

# 📖 Flink on Kubernetes: Reference Guide

## 1. Overview

Apache Flink on Kubernetes with the Flink Operator provides a scalable, resilient, and production-ready platform for real-time stream processing. This chapter summarizes the most important operational and development practices for this project.

---

## 2. Deployment Guide

- Use the [deployment guide](docs/deployment-guide.md) for step-by-step instructions.
- Example manifests:
  - [Flink Deployments](k8s/flink-deployments/)
  - [Cluster Resources](k8s/flink-resources.yaml)
- Makefile and scripts automate cluster setup, job deployment, scaling, and monitoring.
- Supports both local (Docker Compose) and Kubernetes-based workflows.

---

## 3. Job Development & Packaging

- Sample job: [purchase-report](flink-jobs/purchase-report/)
- Build with Maven: see [pom.xml](flink-jobs/purchase-report/pom.xml)
- Package and build Docker images: see [Dockerfile](docker/purchase-report/Dockerfile)
- Update deployment YAMLs to use your custom image and entry class.

---

## 4. State Management & S3

- Use S3 for checkpoints/savepoints in production.
- Reference [k8s/secrets/s3-credentials.yaml](k8s/secrets/s3-credentials.yaml) for secret example.
- Set S3 config in your FlinkDeployment YAML.
- Enable incremental checkpoints for efficiency.

---

## 5. Scaling, Upgrades & Savepoints

- Scale TaskManagers and adjust parallelism using Makefile targets or Helm values.
- Use savepoints for safe upgrades and stateful restarts.
- See scripts and README for automated scaling and upgrade flows.
- Example: `make scale-taskmanagers COUNT=5`, `make trigger-savepoint`, `make upgrade-job IMAGE=...`

---

## 6. Monitoring & Observability

- Access the Flink Web UI via port-forwarding: `make port-forward`
- Prometheus integration: see [k8s/monitoring/servicemonitor.yaml](k8s/monitoring/servicemonitor.yaml)
- Configure Flink to export metrics for observability.

---

## 7. Failure Recovery & Best Practices

- Automated savepoints and recovery ensure high availability.
- Test failure scenarios and recovery procedures regularly.
- Recommended practices:
  - Use RocksDB for large state
  - Configure incremental checkpoints
  - Match parallelism to available slots
  - Monitor backpressure
  - Test savepoint compatibility
  - Size TaskManager memory for RocksDB and buffers
  - Use exactly-once semantics for critical workloads
  - Retain savepoints for rollback

---

## 8. Further Resources

- [Flink Documentation](https://nightlies.apache.org/flink/)
- [Flink Kubernetes Operator](https://nightlies.apache.org/flink/flink-kubernetes-operator-docs-main/)
- [Helm Documentation](https://helm.sh/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)

Flink applications consist of a **JobManager** (coordinates execution, scheduling, checkpointing, recovery) and **TaskManagers** (run parallel computations). State is managed via checkpoints and stored in backends like RocksDB or memory, enabling exactly-once semantics. S3-compatible storage (LocalStack, MinIO) is used for state backend in local development.
---

---

## Installation
See [docs/deployment-guide.md](docs/deployment-guide.md) for detailed deployment instructions.
---
## References
- [Flink Documentation](https://nightlies.apache.org/flink/)
- [Flink Kubernetes Operator](https://nightlies.apache.org/flink/flink-kubernetes-operator-docs-main/)
- [Helm Documentation](https://helm.sh/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
Install the Flink Kubernetes Operator using Helm:


See the official Flink Kubernetes Operator documentation for installation steps. The operator manages FlinkDeployment and FlinkSessionJob resources.

---


## Deploying a Flink Application
See the deployment manifest in [k8s/flink-deployments/flink-application.yaml](k8s/flink-deployments/flink-application.yaml) and supporting resources in [k8s/flink-resources.yaml](k8s/flink-resources.yaml).
These files define the FlinkDeployment and required Kubernetes resources for running Flink on Kubernetes.


To deploy and monitor, use the provided manifests and scripts. See k8s/ for YAML files and scripts/ for automation.

---


## Custom Flink Job Example
See the sample job implementation in [flink-jobs/purchase-report/src/main/java/com/example/flink/purchaseReport.java](flink-jobs/purchase-report/src/main/java/com/example/flink/purchaseReport.java).


Build with Maven using the provided [pom.xml](flink-jobs/purchase-report/pom.xml).


Build and package using Maven as described in the pom.xml.


See the Docker build instructions in [docker/purchase-report/Dockerfile](docker/purchase-report/Dockerfile).

Build and push the Docker image using the Dockerfile in docker/purchase-report/.


Update your deployment YAML to use the built image as needed.

---

## State Backend with S3
For production, use S3 for checkpoints/savepoints. Create a secret:

See k8s/secrets/s3-credentials.yaml for the S3 secret example. Reference the secret in your deployment and set S3 config in flinkConfiguration.

---

## Scaling & Upgrading

See the README and scripts for scaling, upgrading, and patching deployments. Use kubectl patch and the provided scripts for automation.

---

## Monitoring

Access the Flink Web UI by port-forwarding the JobManager service. See the scripts and deployment notes for details.

Prometheus monitoring example: see [k8s/monitoring/servicemonitor.yaml](k8s/monitoring/servicemonitor.yaml).

See k8s/monitoring/servicemonitor.yaml and your flinkConfiguration for Prometheus integration.

---

## Failure Recovery & Savepoints

See the scripts and deployment notes for failure recovery, savepoints, and restore procedures.

---

## Best Practices
1. Use RocksDB for large state
2. Configure incremental checkpoints
3. Match parallelism to available slots
4. Monitor backpressure
5. Test savepoint compatibility
6. Size TaskManager memory for RocksDB and buffers
7. Use exactly-once semantics for critical workloads
8. Retain savepoints for rollback

---

## Conclusion
Apache Flink on Kubernetes with the Flink Operator provides a robust platform for stream processing. The operator manages job lifecycle, upgrades, recovery, and scaling. Monitor job health, configure state backends, and test recovery procedures for production reliability.
        - name: flink-main-container
          volumeMounts:
          - name: flink-data
            mountPath: /flink-data
      volumes:
      - name: flink-data
        persistentVolumeClaim:
          claimName: flink-data

  job:
    jarURI: local:///opt/flink/examples/streaming/StateMachineExample.jar
    parallelism: 4
    upgradeMode: savepoint
    state: running
Create the namespace and persistent volume:
# flink-resources.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: flink
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: flink
  namespace: flink
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: flink-role
  namespace: flink
rules:
- apiGroups: [""]
  resources: ["pods", "configmaps"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: flink-role-binding
  namespace: flink
subjects:
- kind: ServiceAccount
  name: flink
  namespace: flink
roleRef:
  kind: Role
  name: flink-role
  apiGroup: rbac.authorization.k8s.io
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: flink-data
  namespace: flink
spec:
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 50Gi
  storageClassName: nfs-storage
Deploy the application:
kubectl apply -f flink-resources.yaml
kubectl apply -f flink-application.yaml

# Watch deployment
kubectl get flinkdeployment -n flink -w

# Check job status
kubectl get flinkdeployment purchase-report-app -n flink -o jsonpath='{.status.jobStatus.state}'
Creating a Custom Flink Job
Build a simple purchase Report streaming application:
// purchaseReport.java
package com.example.flink;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class purchaseReport {
    public static void main(String[] args) throws Exception {
        // Set up the streaming execution environment
        final StreamExecutionEnvironment env =
            StreamExecutionEnvironment.getExecutionEnvironment();

        // Enable checkpointing every 10 seconds
        env.enableCheckpointing(10000);

        // Read from Kafka
        DataStream<String> text = env
            .addSource(new FlinkKafkaConsumer<>(
                "input-topic",
                new SimpleStringSchema(),
                properties
            ));

        // Parse and count purchase Reports
        DataStream<Tuple2<String, Integer>> counts = text
            .flatMap(new Tokenizer())
            .keyBy(value -> value.f0)
            .window(Time.seconds(5))
            .sum(1);

        // Write to Kafka
        counts.addSink(new FlinkKafkaProducer<>(
            "output-topic",
            new KeyedSerializationSchemaWrapper<>(new SimpleStringSchema()),
            properties,
            FlinkKafkaProducer.Semantic.EXACTLY_ONCE
        ));

        // Execute program
        env.execute("Streaming purchase Report");
    }

    public static final class Tokenizer
            implements FlatMapFunction<String, Tuple2<String, Integer>> {

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            // Normalize and split the line
            String[] tokens = value.toLowerCase().split("\\W+");

            // Emit the pairs
            for (String token : tokens) {
                if (token.length() > 0) {
                    out.collect(new Tuple2<>(token, 1));
                }
            }
        }
    }
}
Build and package:
<!-- pom.xml -->
<project>
    <properties>
        <flink.version>1.18.0</flink.version>
        <scala.binary.version>2.12</scala.binary.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-streaming-java</artifactId>
            <version>${flink.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-connector-kafka</artifactId>
            <version>${flink.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.4.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
Build the JAR:
mvn clean package
Create Docker image with the JAR:
FROM flink:1.18.0

# Copy job JAR
COPY target/purchase-report-1.0.jar /opt/flink/usrlib/purchase-report.jar

# Copy dependencies if needed
COPY target/libs/*.jar /opt/flink/lib/
Build and push:
docker build -t your-registry/flink-purchase-report:1.0 .
docker push your-registry/flink-purchase-report:1.0
Deploying the Custom Job
Update the FlinkDeployment to use your custom image:
apiVersion: flink.apache.org/v1beta1
kind: FlinkDeployment
metadata:
  name: purchase-report-app
  namespace: flink
spec:
  image: your-registry/flink-purchase-report:1.0
  flinkVersion: v1_18

  flinkConfiguration:
    taskmanager.numberOfTaskSlots: "4"
    state.backend: rocksdb
    state.backend.incremental: "true"
    state.checkpoints.dir: s3://my-bucket/flink/checkpoints
    state.savepoints.dir: s3://my-bucket/flink/savepoints
    execution.checkpointing.interval: 60s
    execution.checkpointing.mode: EXACTLY_ONCE
    execution.checkpointing.timeout: 10min
    state.backend.rocksdb.localdir: /flink-data/rocksdb

  jobManager:
    resource:
      memory: "2048m"
      cpu: 1

  taskManager:
    resource:
      memory: "4096m"
      cpu: 2
    replicas: 3

  job:
    jarURI: local:///opt/flink/usrlib/purchase-report.jar
    entryClass: com.example.flink.purchaseReport
    args: []
    parallelism: 8
    upgradeMode: savepoint
    state: running
Configuring State Backend with S3
For production, use S3 for checkpoints and savepoints:
# Create secret for S3 credentials
apiVersion: v1
kind: Secret
metadata:
  name: s3-credentials
  namespace: flink
type: Opaque
stringData:
  access-key: YOUR_ACCESS_KEY
  secret-key: YOUR_SECRET_KEY
---
# Update FlinkDeployment
spec:
  flinkConfiguration:
    s3.endpoint: https://s3.amazonaws.com
    s3.access-key: ${S3_ACCESS_KEY}
    s3.secret-key: ${S3_SECRET_KEY}
    state.checkpoints.dir: s3://my-bucket/flink/checkpoints
    state.savepoints.dir: s3://my-bucket/flink/savepoints

  podTemplate:
    spec:
      containers:
      - name: flink-main-container
        env:
        - name: S3_ACCESS_KEY
          valueFrom:
            secretKeyRef:
              name: s3-credentials
              key: access-key
        - name: S3_SECRET_KEY
          valueFrom:
            secretKeyRef:
              name: s3-credentials
              key: secret-key
Scaling Flink Jobs
Scale TaskManagers for more processing capacity:
# Scale up TaskManagers
kubectl patch flinkdeployment purchase-report-app -n flink --type merge \
  -p '{"spec":{"taskManager":{"replicas":5}}}'

# Increase parallelism
kubectl patch flinkdeployment purchase-report-app -n flink --type merge \
  -p '{"spec":{"job":{"parallelism":16}}}'
The operator handles savepointing, stopping the job, updating configuration, and restarting from the savepoint automatically.
Upgrading Jobs with Savepoints
Update job code while preserving state:
# Build new version
mvn clean package
docker build -t your-registry/flink-purchase-report:1.1 .
docker push your-registry/flink-purchase-report:1.1

# Update deployment with new image
kubectl patch flinkdeployment purchase-report-app -n flink --type merge \
  -p '{"spec":{"image":"your-registry/flink-purchase-report:1.1"}}'
The operator automatically:
1.	Triggers a savepoint
2.	Cancels the running job
3.	Deploys new version
4.	Restarts from the savepoint
Monitor upgrade progress:
kubectl get flinkdeployment purchase-report-app -n flink -o yaml | grep -A5 jobStatus
Monitoring Flink Jobs
Access Flink Web UI:
# Port forward to JobManager
kubectl port-forward svc/purchase-report-app-rest -n flink 8081:8081
Open browser to http://localhost:8081 to view:
•	Job topology and metrics
•	Task parallelism and status
•	Checkpoint statistics
•	Backpressure monitoring
Deploy Prometheus monitoring:
# servicemonitor.yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: flink-metrics
  namespace: flink
spec:
  selector:
    matchLabels:
      app: flink
  endpoints:
  - port: metrics
    interval: 30s
Configure Flink to export metrics:
flinkConfiguration:
  metrics.reporter.prom.class: org.apache.flink.metrics.prometheus.PrometheusReporter
  metrics.reporter.prom.port: 9249
Handling Failures and Recovery
Flink automatically recovers from failures using checkpoints:
# Simulate TaskManager failure
make delete-taskmanager NAME=purchase-report-app-taskmanager-1-1

# Watch recovery
make watch-pods
The operator recreates failed pods, and Flink restores state from the last checkpoint. Processing continues with exactly-once guarantees.
Managing Savepoints
Trigger manual savepoints:
make trigger-savepoint

# Check savepoint location
make get-savepoint-location
Restore from specific savepoint:
spec:
  job:
    initialSavepointPath: s3://my-bucket/flink/savepoints/savepoint-abc123
    allowNonRestoredState: false
Best Practices
Follow these guidelines for production:
1.	Use RocksDB for large state - Memory backend works for small state only
2.	Configure incremental checkpoints - Reduces checkpoint overhead
3.	Set appropriate parallelism - Match task slots across TaskManagers
4.	Monitor backpressure - Indicates bottlenecks in processing
5.	Test savepoint compatibility - Verify state schema changes work
6.	Size TaskManager memory carefully - Account for RocksDB and network buffers
7.	Use exactly-once semantics - Critical for financial and transactional workloads
8.	Retain savepoints for rollback - Keep several versions for safety
Conclusion
Apache Flink on Kubernetes with the Flink Operator provides a powerful platform for stream processing. The operator handles the complexity of job lifecycle management, including upgrades with savepoints, automatic recovery from failures, and dynamic scaling. By properly configuring state backends, checkpointing intervals, and resource allocation, you can build robust real-time data processing pipelines that maintain exactly-once semantics even during failures. Monitor job health through metrics and the Flink UI, and test failure scenarios regularly to ensure your recovery procedures work correctly.

