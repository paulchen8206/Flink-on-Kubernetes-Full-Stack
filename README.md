---
---
# Apache Flink on Kubernetes

Deploy and manage Apache Flink stream processing jobs on Kubernetes using the Flink Kubernetes Operator, Helm, and containerized local development tools.

---

## Table of Contents
- [Project Overview](#project-overview)
- [Deployment Procedures](#deployment-procedures)
- [Script Organization & Makefile Usage](#script-organization--makefile-usage)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Installation](#installation)
- [References](#references)

---

## Project Overview

This project provides a production-ready template for running Apache Flink jobs on Kubernetes, with:
- Flink Kubernetes Operator for job orchestration
- Helm charts for templated deployment
- Docker Compose for local development (with S3 emulation via LocalStack and MinIO, Kafka for event streaming, salesgen for event generation, and Kafka Control Center for topic management)
- CI/CD pipeline for automated build, image, and deployment

---
## Deployment Procedures

Two main workflows are supported:

### 1. Local Development (Docker Compose)
- Run Flink JobManager, TaskManager, WordCount job, LocalStack, MinIO, Kafka, salesgen, and Kafka Control Center as containers
- See [docker/docker-compose.yml](docker/docker-compose.yml) for service definitions
- Access Flink UI at http://localhost:8081
- S3 emulation via LocalStack (http://localhost:4566) and MinIO (http://localhost:9000, console at http://localhost:9001)
- Kafka broker at http://localhost:9092
- salesgen produces events to Kafka topic sales-events
- Kafka Control Center (Kafka UI) at http://localhost:8082 for topic/event management

### 2. Kubernetes Deployment (kind or cloud)
- Use kind for local Kubernetes cluster
- Automated scripts and Makefile targets for setup, build, image loading, and deployment
- Helm chart for templated Flink deployment

---
---

# Apache Flink on Kubernetes with the Flink Operator

> Deploy and manage Apache Flink stream processing jobs on Kubernetes using the Flink Kubernetes Operator for scalable, real-time data processing workloads.


12. [Run with Docker & kind](#run-with-docker--kind)
---


## Script Organization & Makefile Usage

All operational scripts are under `scripts/`. Use the Makefile for streamlined operations:


### Key Makefile Targets
- `make kind-up`: Start kind cluster
- `make kind-down`: Delete kind cluster
- `make prepare-dev`: Setup operator, RBAC, etc.
- `make build-jar`: Build Flink job JAR (via scripts/build-jar.sh)
- `make build-docker`: Build Docker image (via scripts/build-docker.sh)
- `make load-image IMAGE=...`: Load image into kind
- `make deploy`: Deploy Flink resources/job
- `make helm-deploy`: Deploy via Helm chart
- `make port-forward`: Port-forward Flink UI
- `make scale COUNT=...`: Scale TaskManagers
- `make upgrade`: Upgrade job
- `make savepoint`: Trigger savepoint

### Example Workflow
```sh
make kind-up
make prepare-dev
make build-jar
make build-docker
make load-image IMAGE=purchase-report-job:latest
make deploy
make helm-deploy
```
---

---
## Project Structure

- [k8s/README.md](k8s/README.md): Kubernetes manifests and resources
- [flink-jobs/word-count/README.md](flink-jobs/word-count/README.md): Job build/deploy instructions
- [docker/docker-compose.yml](docker/docker-compose.yml): Local container orchestration
  - Flink, LocalStack, MinIO, Kafka, salesgen, Kafka Control Center
- [helm/flink/README.md](helm/flink/README.md): Helm chart for Flink
- [ci-cd/pipeline.yaml](ci-cd/pipeline.yaml): CI/CD pipeline
- [docs/](docs/): Operations, best practices, architecture
- [scripts/](scripts/): Operational scripts
- [config/](config/): Flink and logging configs
---

### Prerequisites
- Docker
- kind (install via `brew install kind` or see [kind docs](https://kind.sigs.k8s.io/docs/user/quick-start/))
- kubectl
- Helm

### Steps
1. **Start a local kind cluster:**
  ```sh
  make kind-up
  ```
2. **Prepare the dev environment (install operator, etc):**
  ```sh
  make prepare-dev
  ```
3. **Build the job JAR and Docker image:**
  ```sh
  make build-job
  make build-image IMAGE=your-repo/flink-word-count:1.0
  ```
4. **Load the image into kind:**
  ```sh
  make load-image IMAGE=your-repo/flink-word-count:1.0
  ```
5. **Deploy Flink resources and job:**
  ```sh
  make deploy
  ```
6. **Deploy via Helm (optional):**
  ```sh
  make helm-deploy
  ```
7. **Monitor and access Flink UI:**
  ```sh
  make port-forward
  # Open http://localhost:8081
  ```

---

---

## Overview
Apache Flink provides stateful computations over data streams for real-time analytics, ETL pipelines, and event-driven applications. Running Flink on Kubernetes brings container orchestration benefits to stream processing. The Flink Kubernetes Operator simplifies deploying and managing Flink clusters and jobs.

---

## Architecture
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
See the sample job implementation in [flink-jobs/word-count/src/main/java/com/example/flink/WordCount.java](flink-jobs/word-count/src/main/java/com/example/flink/WordCount.java).


Build with Maven using the provided [pom.xml](flink-jobs/word-count/pom.xml).


Build and package using Maven as described in the pom.xml.


See the Docker build instructions in [docker/word-count/Dockerfile](docker/word-count/Dockerfile).

Build and push the Docker image using the Dockerfile in docker/word-count/.


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
kubectl get flinkdeployment word-count-app -n flink -o jsonpath='{.status.jobStatus.state}'
Creating a Custom Flink Job
Build a simple word count streaming application:
// WordCount.java
package com.example.flink;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class WordCount {
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

        // Parse and count words
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
        env.execute("Streaming Word Count");
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
COPY target/word-count-1.0.jar /opt/flink/usrlib/word-count.jar

# Copy dependencies if needed
COPY target/libs/*.jar /opt/flink/lib/
Build and push:
docker build -t your-registry/flink-word-count:1.0 .
docker push your-registry/flink-word-count:1.0
Deploying the Custom Job
Update the FlinkDeployment to use your custom image:
apiVersion: flink.apache.org/v1beta1
kind: FlinkDeployment
metadata:
  name: word-count-app
  namespace: flink
spec:
  image: your-registry/flink-word-count:1.0
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
    jarURI: local:///opt/flink/usrlib/word-count.jar
    entryClass: com.example.flink.WordCount
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
kubectl patch flinkdeployment word-count-app -n flink --type merge \
  -p '{"spec":{"taskManager":{"replicas":5}}}'

# Increase parallelism
kubectl patch flinkdeployment word-count-app -n flink --type merge \
  -p '{"spec":{"job":{"parallelism":16}}}'
The operator handles savepointing, stopping the job, updating configuration, and restarting from the savepoint automatically.
Upgrading Jobs with Savepoints
Update job code while preserving state:
# Build new version
mvn clean package
docker build -t your-registry/flink-word-count:1.1 .
docker push your-registry/flink-word-count:1.1

# Update deployment with new image
kubectl patch flinkdeployment word-count-app -n flink --type merge \
  -p '{"spec":{"image":"your-registry/flink-word-count:1.1"}}'
The operator automatically:
1.	Triggers a savepoint
2.	Cancels the running job
3.	Deploys new version
4.	Restarts from the savepoint
Monitor upgrade progress:
kubectl get flinkdeployment word-count-app -n flink -o yaml | grep -A5 jobStatus
Monitoring Flink Jobs
Access Flink Web UI:
# Port forward to JobManager
kubectl port-forward svc/word-count-app-rest -n flink 8081:8081
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
kubectl delete pod word-count-app-taskmanager-1-1 -n flink

# Watch recovery
kubectl get pods -n flink -w
The operator recreates failed pods, and Flink restores state from the last checkpoint. Processing continues with exactly-once guarantees.
Managing Savepoints
Trigger manual savepoints:
# Create savepoint
kubectl patch flinkdeployment word-count-app -n flink --type merge \
  -p '{"spec":{"job":{"savepointTriggerNonce":1}}}'

# Check savepoint location
kubectl get flinkdeployment word-count-app -n flink \
  -o jsonpath='{.status.jobStatus.savepointInfo.lastSavepoint.location}'
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

