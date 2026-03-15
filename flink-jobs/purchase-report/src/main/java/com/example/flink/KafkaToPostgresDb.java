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

public class KafkaToPostgresDb {
    public static void main(String[] args) throws Exception {
        // Load YAML config for Kafka, Postgres, and Flink job parameters
        com.fasterxml.jackson.databind.ObjectMapper yamlMapper = new com.fasterxml.jackson.databind.ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
        java.util.Map<String, Object> config;
        try (java.io.InputStream is = KafkaToPostgresDb.class.getClassLoader().getResourceAsStream("flink-job-config.yaml")) {
            if (is == null)
                throw new RuntimeException("flink-job-config.yaml not found in resources");
            config = yamlMapper.readValue(is, java.util.Map.class);
        }

        java.util.Map<String, Object> kafkaConfig = (java.util.Map<String, Object>) config.get("kafka");
        java.util.Map<String, Object> postgresConfig = (java.util.Map<String, Object>) config.get("postgres");
        java.util.Map<String, Object> flinkConfig = (java.util.Map<String, Object>) config.get("flink");

        String kafkaBootstrap = (String) kafkaConfig.get("bootstrapServers");
        String purchasesTopic = (String) kafkaConfig.get("purchasesTopic");
        String inventoriesTopic = (String) kafkaConfig.get("inventoriesTopic");
        String groupIdPrefix = (String) kafkaConfig.get("groupIdPrefix");
        String pgHost = (String) postgresConfig.get("host");
        int pgPort = (Integer) (postgresConfig.get("port") instanceof Integer ? postgresConfig.get("port") : ((Number)postgresConfig.get("port")).intValue());
        String pgDb = (String) postgresConfig.get("database");
        String reportDb = (String) postgresConfig.getOrDefault("reportDatabase", "sales_report");
        String pgUser = (String) postgresConfig.get("user");
        String pgPassword = (String) postgresConfig.get("password");
        String mergedTable = (String) postgresConfig.get("mergedTable");
        int checkpointInterval = (Integer) (flinkConfig.get("checkpointIntervalMs") instanceof Integer ? flinkConfig.get("checkpointIntervalMs") : ((Number)flinkConfig.get("checkpointIntervalMs")).intValue());
        int parallelism = (Integer) (flinkConfig.get("parallelism") instanceof Integer ? flinkConfig.get("parallelism") : ((Number)flinkConfig.get("parallelism")).intValue());

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(parallelism);
        env.enableCheckpointing(checkpointInterval);

        // Read purchases using new KafkaSource API
        KafkaSource<String> purchasesSource = KafkaSource.<String>builder()
            .setBootstrapServers(kafkaBootstrap)
            .setTopics(purchasesTopic)
            .setGroupId(groupIdPrefix + "-purchases-" + System.currentTimeMillis())
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
            .setBootstrapServers(kafkaBootstrap)
            .setTopics(inventoriesTopic)
            .setGroupId(groupIdPrefix + "-inventories-" + System.currentTimeMillis())
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
        String insertSql = "INSERT INTO " + mergedTable + " (transaction_time, transaction_id, product_id, price, quantity, state, is_member, member_discount, add_supplements, supplement_price, total_purchase, event_time, existing_level, stock_quantity, new_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String jdbcUrl = "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDb;
        String reportJdbcUrl = "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + reportDb;

        // Sink raw purchase events for debugging/troubleshooting visibility.
        purchasesRaw.addSink(
            JdbcSink.sink(
                "INSERT INTO purchases (event_data) VALUES (?)",
                (ps, event) -> ps.setString(1, event),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                    .withUrl(jdbcUrl)
                    .withDriverName("org.postgresql.Driver")
                    .withUsername(pgUser)
                    .withPassword(pgPassword)
                    .build()
            )
        );

        joined.addSink(
            JdbcSink.sink(
                insertSql,
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
                            eventTime = java.sql.Timestamp.valueOf(eventTimeStr.split("\\.")[0]);
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
                    .withUrl(jdbcUrl)
                    .withDriverName("org.postgresql.Driver")
                    .withUsername(pgUser)
                    .withPassword(pgPassword)
                    .build()
            )
        );

        // Sink lightweight report metrics into sales_report.purchase_report.
        String reportInsertSql = "INSERT INTO purchase_report (dim_item, dim_category, dim_state, purchase_time, "
            + "fact_count_transactions, fact_sum_quantity, fact_sum_price, fact_sum_member_discount, "
            + "fact_sum_supplement_price, fact_sum_total_purchase, fact_avg_total_purchase) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT (dim_item, dim_category, dim_state, purchase_time) DO UPDATE SET "
            + "fact_count_transactions = purchase_report.fact_count_transactions + EXCLUDED.fact_count_transactions, "
            + "fact_sum_quantity = purchase_report.fact_sum_quantity + EXCLUDED.fact_sum_quantity, "
            + "fact_sum_price = purchase_report.fact_sum_price + EXCLUDED.fact_sum_price, "
            + "fact_sum_member_discount = purchase_report.fact_sum_member_discount + EXCLUDED.fact_sum_member_discount, "
            + "fact_sum_supplement_price = purchase_report.fact_sum_supplement_price + EXCLUDED.fact_sum_supplement_price, "
            + "fact_sum_total_purchase = purchase_report.fact_sum_total_purchase + EXCLUDED.fact_sum_total_purchase, "
            + "fact_avg_total_purchase = ((purchase_report.fact_avg_total_purchase * purchase_report.fact_count_transactions) "
            + "+ (EXCLUDED.fact_avg_total_purchase * EXCLUDED.fact_count_transactions)) "
            + "/ (purchase_report.fact_count_transactions + EXCLUDED.fact_count_transactions)";

        joined.addSink(
            JdbcSink.sink(
                reportInsertSql,
                (ps, event) -> {
                    try {
                        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(event);
                        java.sql.Timestamp purchaseTime = java.sql.Timestamp.valueOf(node.path("transaction_time").asText());
                        double price = node.path("price").asDouble();
                        int quantity = node.path("quantity").asInt();
                        double totalPurchase = node.path("total_purchase").asDouble();

                        ps.setString(1, node.path("product_id").asText());
                        ps.setString(2, "unknown");
                        ps.setString(3, node.path("state").asText());
                        ps.setTimestamp(4, purchaseTime);
                        ps.setFloat(5, 1.0f);
                        ps.setFloat(6, (float) quantity);
                        ps.setFloat(7, (float) (price * quantity));
                        ps.setFloat(8, (float) node.path("member_discount").asDouble());
                        ps.setFloat(9, (float) node.path("supplement_price").asDouble());
                        ps.setFloat(10, (float) totalPurchase);
                        ps.setFloat(11, (float) totalPurchase);
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                    .withUrl(reportJdbcUrl)
                    .withDriverName("org.postgresql.Driver")
                    .withUsername(pgUser)
                    .withPassword(pgPassword)
                    .build()
            )
        );

        // Mirror report metrics into purchase_db for local unified monitoring view.
        joined.addSink(
            JdbcSink.sink(
                reportInsertSql,
                (ps, event) -> {
                    try {
                        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(event);
                        java.sql.Timestamp purchaseTime = java.sql.Timestamp.valueOf(node.path("transaction_time").asText());
                        double price = node.path("price").asDouble();
                        int quantity = node.path("quantity").asInt();
                        double totalPurchase = node.path("total_purchase").asDouble();

                        ps.setString(1, node.path("product_id").asText());
                        ps.setString(2, "unknown");
                        ps.setString(3, node.path("state").asText());
                        ps.setTimestamp(4, purchaseTime);
                        ps.setFloat(5, 1.0f);
                        ps.setFloat(6, (float) quantity);
                        ps.setFloat(7, (float) (price * quantity));
                        ps.setFloat(8, (float) node.path("member_discount").asDouble());
                        ps.setFloat(9, (float) node.path("supplement_price").asDouble());
                        ps.setFloat(10, (float) totalPurchase);
                        ps.setFloat(11, (float) totalPurchase);
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                    .withUrl(jdbcUrl)
                    .withDriverName("org.postgresql.Driver")
                    .withUsername(pgUser)
                    .withPassword(pgPassword)
                    .build()
            )
        );

        // Sink merged JSON to Kafka topic purchase_inventory_merged
        org.apache.flink.connector.kafka.sink.KafkaSink<String> mergedKafkaSink = org.apache.flink.connector.kafka.sink.KafkaSink.<String>builder()
            .setBootstrapServers(kafkaBootstrap)
            .setRecordSerializer(org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema.builder()
                .setTopic("purchase_inventory_merged")
                .setValueSerializationSchema(new SimpleStringSchema())
                .build())
            .build();
        joined.sinkTo(mergedKafkaSink);

        env.execute("Kafka Join to Postgres and Kafka Sink");
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
