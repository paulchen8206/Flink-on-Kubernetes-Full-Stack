# Dockerfile for oltp Python application
FROM python:3.10-slim

WORKDIR /app

# Copy application code
COPY . /app


# Install dependencies if requirements.txt exists
RUN if [ -f requirements.txt ]; then pip install --no-cache-dir -r requirements.txt; fi

# Always install kafka-python for KafkaProducer
RUN pip install --no-cache-dir kafka-python

# Set environment variables if needed
# ENV ...

# Wait for Kafka to be available before starting the app
CMD python wait-for-kafka.py && python purchases.py
