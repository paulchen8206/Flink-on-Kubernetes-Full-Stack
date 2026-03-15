
#!/bin/bash
# Delete the kind cluster
# Usage: ./delete-kind.sh [CLUSTER_NAME]
set -e

# Set cluster name to delete (default: flink-kind)
CLUSTER_NAME=${1:-flink-kind}

# Delete the kind cluster with the specified name
kind delete cluster --name "$CLUSTER_NAME"
