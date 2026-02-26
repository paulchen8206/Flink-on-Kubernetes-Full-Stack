
#!/bin/bash
# Build the Flink job JAR using Maven
# Usage: ./build-jar.sh
set -e

# Change to the Flink job directory
cd flink-jobs/purchase-report

# Build the Maven project and create the job JAR
mvn clean package
