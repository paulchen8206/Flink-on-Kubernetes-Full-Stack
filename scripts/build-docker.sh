

#!/bin/bash
# Build the Docker image for the Flink job
# Usage: ./build-docker.sh

set -e

if ! command -v docker &> /dev/null; then
	echo "Docker not found. Please install Docker and try again."
	exit 1
fi

cd .. || { echo "Project root directory not found."; exit 1; }

docker build -f docker/Dockerfile -t purchase-report-job:latest . || { echo "Docker build failed."; exit 1; }
echo "Docker image build complete: purchase-report-job:latest"
