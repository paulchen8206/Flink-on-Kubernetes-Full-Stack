

#!/bin/bash
# Build the Flink job JAR using Maven
# Usage: ./build-jar.sh

set -e

if ! command -v mvn &> /dev/null; then
	echo "Maven (mvn) not found. Please install Maven and try again."
	exit 1
fi

cd flink-jobs/purchase-report || { echo "Directory flink-jobs/purchase-report not found."; exit 1; }

mvn clean package || { echo "Maven build failed."; exit 1; }
echo "JAR build complete: target/purchase-report-1.0-SNAPSHOT.jar"
