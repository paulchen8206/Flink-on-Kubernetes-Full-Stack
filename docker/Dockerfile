
FROM flink:1.20.0

# Copy the Flink job JAR
COPY purchase-report-1.0-SNAPSHOT.jar /opt/flink/usrlib/purchase-report.jar
# Copy the Kafka connector JAR
COPY flink-connector-kafka-3.0.2-1.20.jar /opt/flink/lib/

CMD ["flink", "run", "-c", "com.example.flink.KafkaToPostgresDb", "/opt/flink/usrlib/purchase-report.jar"]
