package com.malik.streams.user_activity_tracker.listener;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malik.streams.user_activity_tracker.metrics.EventMetrics;
import com.malik.streams.user_activity_tracker.model.UserActivityEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UserActivityListener {
    private final Logger log = LoggerFactory.getLogger(UserActivityListener.class.getName());
    private final EventMetrics metrics;

    @Autowired
    private ElasticsearchClient esClient;

    @Autowired
    private ObjectMapper objectMapper;

    public UserActivityListener(EventMetrics metrics) {
        this.metrics = metrics;
    }

    @KafkaListener(topics = "user-activity", groupId = "primary-group")
    public void listenUserActivity(String message) throws IOException {
        log.info("Consumer thread: " + Thread.currentThread().getName() + " Received message: " + message);
        // push to user-activity index in ElasticSearch
        UserActivityEvent event = objectMapper.readValue(message, UserActivityEvent.class);
        IndexRequest<UserActivityEvent> req = new IndexRequest.Builder<UserActivityEvent>()
                .index("user-activity")
                .id(event.userId() + "#" + event.timestamp() + "#" + event.page())
                .document(event)
                .build();

        try {
            IndexResponse resp = esClient.index(req);
            metrics.incrementIndexed();
            metrics.incrementConsumed();
        } catch (Exception e) {
            metrics.incrementFailed();
        }
    }
}
