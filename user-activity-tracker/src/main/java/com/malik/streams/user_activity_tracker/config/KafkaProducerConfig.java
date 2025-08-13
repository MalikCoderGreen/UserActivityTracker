package com.malik.streams.user_activity_tracker.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

//    @Value("${kafka.username:leek}")
//    private String username;
//
//    @Value("${kafka.password:sesh4747---Nams}")
//    private String password;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Basic Kafka configuration
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Security configuration for RedPanda
//        configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
//        configProps.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
//        configProps.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
//                "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";",
//                username, password));

        // Performance optimizations
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 32 * 1024); // 32KB batches
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Wait up to 10ms for batching
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Enable compression
        configProps.put(ProducerConfig.ACKS_CONFIG, "1"); // Wait for leader acknowledgment
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // Retry failed sends

        // Disable client telemetry for RedPanda compatibility
        configProps.put("enable.metrics.push", "false");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory());

        // Set default topic if needed
        template.setDefaultTopic("user-activity");

        return template;
    }
}