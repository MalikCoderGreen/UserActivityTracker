package com.malik.streams.user_activity_tracker.listener;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Autowired
    private ElasticsearchClient esClient;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "user-activity", groupId = "primary-group")
    public void listenUserActivity(String message) throws IOException {
        log.info("Consumer thread: " + Thread.currentThread().getName() + " Received message: " + message);
        // push to user-activity index in ElasticSearch
        UserActivityEvent event = objectMapper.readValue(message, UserActivityEvent.class);

        IndexRequest<UserActivityEvent> req = new IndexRequest.Builder<UserActivityEvent>()
                .index("user-activity")
                .id(event.getUserId() + "#" + event.getTimestamp() + "#" + event.getPage())
                .document(event)
                .build();
        IndexResponse resp = esClient.index(req);
        log.info("Response from ElasticSearch after attempting to write to index: " + resp);
    }
}
