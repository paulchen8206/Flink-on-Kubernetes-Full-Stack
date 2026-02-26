package com.example.flink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
// import org.apache.flink.api.java.tuple.Tuple2; // (unused)
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Properties;

public class KafkaToPostgresDb {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(10000);

        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", "kafka:9092");
        kafkaProps.setProperty("group.id", "flink-consumer-" + System.currentTimeMillis());


        // Read purchases using new KafkaSource API
        KafkaSource<String> purchasesSource = KafkaSource.<String>builder()
            .setBootstrapServers("kafka:9092")
            .setTopics("store.purchases")
            .setGroupId("flink-consumer-purchases-" + System.currentTimeMillis())
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new SimpleStringSchema())
            .build();
        DataStream<String> purchasesRaw = env.fromSource(
            purchasesSource,
            WatermarkStrategy.noWatermarks(),
            "KafkaSource-purchases"
        );

        // Read inventories using new KafkaSource API
        KafkaSource<String> inventoriesSource = KafkaSource.<String>builder()
            .setBootstrapServers("kafka:9092")
            .setTopics("store.inventories")
            .setGroupId("flink-consumer-inventories-" + System.currentTimeMillis())
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new SimpleStringSchema())
            .build();
        DataStream<String> inventoriesRaw = env.fromSource(
            inventoriesSource,
            WatermarkStrategy.noWatermarks(),
            "KafkaSource-inventories"
        );

        ObjectMapper mapper = new ObjectMapper();

        // Parse JSON to POJO
        DataStream<Purchase> purchases = purchasesRaw.map(json -> mapper.readValue(json, Purchase.class), TypeInformation.of(Purchase.class));
        DataStream<Inventory> inventories = inventoriesRaw.map(json -> mapper.readValue(json, Inventory.class), TypeInformation.of(Inventory.class));

        // Key by product_id
        KeyedStream<Purchase, String> keyedPurchases = purchases.keyBy(p -> p.product_id);
        KeyedStream<Inventory, String> keyedInventories = inventories.keyBy(i -> i.product_id);

        // Simple key-based join: enrich purchase with latest inventory (using broadcast state for demo simplicity)
        DataStream<String> joined = keyedPurchases.connect(keyedInventories)
            .flatMap(new PurchaseInventoryEnrichment(mapper))
            .returns(String.class);

        // Sink merged JSON to Postgres using classic JdbcSink (deprecated)
        joined.addSink(
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

        env.execute("Kafka Join to Postgres Sink");
    }
    // FlatMapFunction for joining Purchase and Inventory by product_id and merging as JSON
    public static class PurchaseInventoryEnrichment extends org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction<Purchase, Inventory, String> {
        private final ObjectMapper mapper;
        private Inventory latestInventory;

        public PurchaseInventoryEnrichment(ObjectMapper mapper) {
            this.mapper = mapper;
        }


        @Override
        public void flatMap1(Purchase purchase, org.apache.flink.util.Collector<String> out) throws Exception {
            if (latestInventory != null && purchase.product_id.equals(latestInventory.product_id)) {
                // Merge all fields from both sources at the top level
                ObjectNode merged = mapper.createObjectNode();
                // Add all purchase fields
                ObjectNode purchaseNode = mapper.valueToTree(purchase);
                purchaseNode.fieldNames().forEachRemaining(field -> merged.set(field, purchaseNode.get(field)));
                // Add all inventory fields (may overwrite if same name)
                ObjectNode inventoryNode = mapper.valueToTree(latestInventory);
                inventoryNode.fieldNames().forEachRemaining(field -> merged.set(field, inventoryNode.get(field)));
                out.collect(mapper.writeValueAsString(merged));
            }
        }

        @Override
        public void flatMap2(Inventory inventory, org.apache.flink.util.Collector<String> out) {
            this.latestInventory = inventory;
        }
    }
}
