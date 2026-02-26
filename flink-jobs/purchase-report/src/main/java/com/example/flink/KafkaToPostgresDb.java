package com.example.flink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import java.util.Properties;

public class KafkaToPostgresDb {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(10000);

        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", "kafka:9092");
        kafkaProps.setProperty("group.id", "flink-consumer-" + System.currentTimeMillis());

        DataStream<String> events = env.addSource(
            new FlinkKafkaConsumer<>(
                "store.purchases",
                new SimpleStringSchema(),
                kafkaProps
            )
        );

        events.addSink(
            JdbcSink.sink(
                "INSERT INTO purchases (event_data) VALUES (?)",
                (ps, event) -> ps.setString(1, event),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                    .withUrl("jdbc:postgresql://postgres:5432/purchase_db")
                    .withDriverName("org.postgresql.Driver")
                    .withUsername("postgres")
                    .withPassword("postgres")
                    .build()
            )
        );

        env.execute("Kafka to Postgres Sink");
    }
}
