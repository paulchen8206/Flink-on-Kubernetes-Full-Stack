
FROM flink:1.20.2-java17

# Copy the Flink job JAR
COPY purchase-report-1.0-SNAPSHOT.jar /opt/flink/usrlib/purchase-report.jar

# Use the default Flink entrypoint for cluster setup
# For JobManager:
# ENTRYPOINT ["/docker-entrypoint.sh", "jobmanager"]
# For TaskManager:
# ENTRYPOINT ["/docker-entrypoint.sh", "taskmanager"]
# Or use start-cluster.sh for standalone mode:
# ENTRYPOINT ["/opt/flink/bin/start-cluster.sh"]
