FROM flink:1.20.0

COPY ../flink-jobs/purchase-report/target/purchase-report-1.0-SNAPSHOT.jar /opt/flink/usrlib/purchase-report.jar

CMD ["flink", "run", "-c", "com.example.flink.KafkaToPostgresDb", "/opt/flink/usrlib/purchase-report.jar"]
