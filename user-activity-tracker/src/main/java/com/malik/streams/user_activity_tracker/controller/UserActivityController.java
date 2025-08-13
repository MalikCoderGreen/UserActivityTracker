package com.malik.streams.user_activity_tracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.malik.streams.user_activity_tracker.model.UserActivityEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/track")
@CrossOrigin(origins = "*") // Configure appropriately for production
public class UserActivityController {

    private static final Logger log = LoggerFactory.getLogger(UserActivityController.class);
    private static final String USER_ACTIVITY_TOPIC = "user-activity";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    private final ObjectMapper objectMapper;

    public UserActivityController() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Track user clicks
     * POST /api/track/click
     */
    @PostMapping("/click")
    public ResponseEntity<Map<String, Object>> trackClick(
            @Valid @RequestBody UserActivityEvent event,
            HttpServletRequest request) {

        event.setEventType("click");
        return processEvent(event, request);
    }

    @GetMapping("/test")
    public String test() {
        return "Controller is working!";
    }

    /**
     * Track page views
     * POST /api/track/view
     */
    @PostMapping("/view")
    public ResponseEntity<Map<String, Object>> trackView(
            @Valid @RequestBody UserActivityEvent event,
            HttpServletRequest request) {

        event.setEventType("view");
        return processEvent(event, request);
    }

    /**
     * Track scroll events
     * POST /api/track/scroll
     */
    @PostMapping("/scroll")
    public ResponseEntity<Map<String, Object>> trackScroll(
            @Valid @RequestBody UserActivityEvent event,
            HttpServletRequest request) {

        event.setEventType("scroll");
        return processEvent(event, request);
    }

    /**
     * Generic event tracking endpoint
     * POST /api/track/event
     */
    @PostMapping("/event")
    public ResponseEntity<Map<String, Object>> trackEvent(
            @Valid @RequestBody UserActivityEvent event,
            HttpServletRequest request) {

        return processEvent(event, request);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Process and send event to Kafka
     */
    private ResponseEntity<Map<String, Object>> processEvent(UserActivityEvent event, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Enrich event with request metadata
            enrichEvent(event, request);

            // Convert to JSON
            String eventJson = objectMapper.writeValueAsString(event);

            // Send to Kafka using userId as partition key for ordering
            kafkaTemplate.send(USER_ACTIVITY_TOPIC, event.getUserId(), eventJson)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Event sent successfully: user={}, type={}, page={}",
                                    event.getUserId(), event.getEventType(), event.getPage());
                        } else {
                            log.error("Failed to send event to Kafka", ex);
                        }
                    });

            response.put("status", "success");
            response.put("message", "Event tracked successfully");
            response.put("eventId", event.getUserId() + "-" + event.getTimestamp());

            return ResponseEntity.ok(response);

        } catch (JsonProcessingException e) {
            log.error("Error serializing event to JSON", e);
            response.put("status", "error");
            response.put("message", "Failed to process event");
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Unexpected error processing event", e);
            response.put("status", "error");
            response.put("message", "Internal server error");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Enrich event with request metadata
     */
    private void enrichEvent(UserActivityEvent event, HttpServletRequest request) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }

        if (event.getUserAgent() == null) {
            event.setUserAgent(request.getHeader("User-Agent"));
        }

        if (event.getIpAddress() == null) {
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getHeader("X-Real-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }
            event.setIpAddress(ipAddress);
        }
    }
}