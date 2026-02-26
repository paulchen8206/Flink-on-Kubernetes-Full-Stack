# Purchase Report Flink Job

This module contains the Flink application for consuming Kafka events and sinking them into PostgreSQL.

## Build Instructions
1. **Build the JAR:**
   ```sh
   make build-jar
   # Output: target/purchase-report-1.0-SNAPSHOT.jar
   ```
2. **Build Docker Image:**
   ```sh
   make build-docker
   ```
3. **Push Image (optional):**
   ```sh
   docker push purchase-report-job:latest
   ```

## Deploy on Docker Compose
- The job is included in the main docker-compose.yml as purchase-report-job.
- Ensure Kafka, Postgres, and other dependencies are running.

## Customization
- Edit `KafkaToPostgresDb.java` for custom logic.
- Update `pom.xml` for dependencies.

## References
- [Flink Documentation](https://nightlies.apache.org/flink/)
- [Project Root README](../../README.md)

## Custom Flink Job Example
See the sample job implementation in [src/main/java/com/example/flink/purchaseReport.java](src/main/java/com/example/flink/purchaseReport.java).

Build with Maven using the provided [pom.xml](pom.xml).

Build and package using Maven as described in the pom.xml.

See the Docker build instructions in the Dockerfile in this directory.

Build and push the Docker image using the Dockerfile.

Update your deployment YAML to use the built image as needed.
