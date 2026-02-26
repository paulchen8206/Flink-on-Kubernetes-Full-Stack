FROM flink:1.20.0

# Copy the JAR from the local build context (docker directory)
COPY purchase-report-1.0-SNAPSHOT.jar /opt/flink/usrlib/purchase-report.jar

CMD ["flink", "run", "-c", "com.example.flink.KafkaToPostgresDb", "/opt/flink/usrlib/purchase-report.jar"]
