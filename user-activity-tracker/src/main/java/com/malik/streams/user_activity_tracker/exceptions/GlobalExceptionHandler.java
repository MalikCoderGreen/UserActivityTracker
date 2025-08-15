package com.malik.streams.user_activity_tracker.exceptions;

import com.malik.streams.user_activity_tracker.metrics.EventMetrics;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;
import java.nio.charset.StandardCharsets;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private EventMetrics metrics;

    public GlobalExceptionHandler(EventMetrics metrics) {
        this.metrics = metrics;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleJsonParseError(HttpMessageNotReadableException ex,
                                                       HttpServletRequest request) {
        String rawJson = extractRequestBody(request);
        // Here you could log, publish to DLQ, etc.
        kafkaTemplate.send("user-activity-dlq", rawJson);
        metrics.incrementDlq();
        return ResponseEntity.badRequest().body("Invalid JSON: " + ex.getMostSpecificCause().getMessage());
    }

    private String extractRequestBody(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                return new String(buf, StandardCharsets.UTF_8);
            }
        }
        return "";
    }
}