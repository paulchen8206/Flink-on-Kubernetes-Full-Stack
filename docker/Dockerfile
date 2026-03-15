
FROM flink:2.0.0-java17

# Copy the Flink job JAR
COPY purchase-report-1.0-SNAPSHOT.jar /opt/flink/usrlib/purchase-report.jar
# Copy the Kafka connector JAR
COPY flink-connector-kafka-4.0.0-2.0.jar /opt/flink/lib/

# Use the default Flink entrypoint for cluster setup
# For JobManager:
# ENTRYPOINT ["/docker-entrypoint.sh", "jobmanager"]
# For TaskManager:
# ENTRYPOINT ["/docker-entrypoint.sh", "taskmanager"]
# Or use start-cluster.sh for standalone mode:
# ENTRYPOINT ["/opt/flink/bin/start-cluster.sh"]
