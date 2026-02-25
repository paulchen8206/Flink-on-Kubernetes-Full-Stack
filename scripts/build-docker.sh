#!/bin/bash
set -e
cd ..
docker build -f docker/Dockerfile -t purchase-report-job:latest .
