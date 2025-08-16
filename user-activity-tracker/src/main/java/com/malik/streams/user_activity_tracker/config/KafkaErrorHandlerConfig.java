package com.malik.streams.user_activity_tracker.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaErrorHandlerConfig {
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        // Send failed records to <original-topic>.dlt
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

        // Retry w/ exponential backoff
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxInterval(10000L);
        backOff.setMaxElapsedTime(30000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        return errorHandler;
    }
}
