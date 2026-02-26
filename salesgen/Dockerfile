# Dockerfile for salesgen Python application
FROM python:3.10-slim

WORKDIR /app

# Copy application code
COPY . /app

# Install dependencies if requirements.txt exists
RUN if [ -f requirements.txt ]; then pip install --no-cache-dir -r requirements.txt; fi

# Set environment variables if needed
# ENV ...

# Default command to run the salesgen app (update as needed)
CMD ["python", "purchases.py"]
