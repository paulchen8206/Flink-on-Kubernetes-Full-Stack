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

        // Sink merged JSON to purchase_inventory_merged with column mapping
        joined.addSink(
            JdbcSink.sink(
                "INSERT INTO purchase_inventory_merged (transaction_time, transaction_id, product_id, price, quantity, state, is_member, member_discount, add_supplements, supplement_price, total_purchase, event_time, existing_level, stock_quantity, new_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (ps, event) -> {
                    try {
                        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(event);
                        ps.setTimestamp(1, java.sql.Timestamp.valueOf(node.path("transaction_time").asText()));
                        ps.setString(2, node.path("transaction_id").asText());
                        ps.setString(3, node.path("product_id").asText());
                        ps.setDouble(4, node.path("price").asDouble());
                        ps.setInt(5, node.path("quantity").asInt());
                        ps.setString(6, node.path("state").asText());
                        ps.setBoolean(7, node.path("is_member").asBoolean());
                        ps.setDouble(8, node.path("member_discount").asDouble());
                        ps.setBoolean(9, node.path("add_supplements").asBoolean());
                        ps.setDouble(10, node.path("supplement_price").asDouble());
                        ps.setDouble(11, node.path("total_purchase").asDouble());
                        // event_time may have microseconds, so parse safely
                        String eventTimeStr = node.path("event_time").asText();
                        java.sql.Timestamp eventTime = null;
                        try {
                            eventTime = java.sql.Timestamp.valueOf(eventTimeStr.split(".")[0]);
                        } catch (Exception e) {
                            eventTime = null;
                        }
                        ps.setTimestamp(12, eventTime);
                        ps.setInt(13, node.path("existing_level").asInt());
                        ps.setInt(14, node.path("stock_quantity").asInt());
                        ps.setInt(15, node.path("new_level").asInt());
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
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
        private final java.util.Map<String, Inventory> inventoryByProduct = new java.util.HashMap<>();

        public PurchaseInventoryEnrichment(ObjectMapper mapper) {
            this.mapper = mapper;
        }


        @Override
        public void flatMap1(Purchase purchase, org.apache.flink.util.Collector<String> out) throws Exception {
            Inventory inventory = inventoryByProduct.get(purchase.product_id);
            if (inventory != null) {
                ObjectNode merged = mapper.createObjectNode();
                ObjectNode purchaseNode = mapper.valueToTree(purchase);
                purchaseNode.fieldNames().forEachRemaining(field -> merged.set(field, purchaseNode.get(field)));
                ObjectNode inventoryNode = mapper.valueToTree(inventory);
                inventoryNode.fieldNames().forEachRemaining(field -> merged.set(field, inventoryNode.get(field)));
                out.collect(mapper.writeValueAsString(merged));
            }
        }

        @Override
        public void flatMap2(Inventory inventory, org.apache.flink.util.Collector<String> out) {
            inventoryByProduct.put(inventory.product_id, inventory);
        }
    }
}
