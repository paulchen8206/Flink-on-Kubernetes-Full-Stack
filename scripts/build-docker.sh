
#!/bin/bash
# Build the Docker image for the Flink job
# Usage: ./build-docker.sh
set -e

# Change to the project root directory (if needed)
cd ..

# Build the Docker image using the provided Dockerfile
docker build -f docker/Dockerfile -t purchase-report-job:latest .
