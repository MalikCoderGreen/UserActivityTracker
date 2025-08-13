package com.malik.streams.user_activity_tracker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class UserActivityEvent {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Event type is required")
    private String eventType; // "click", "view", "scroll", "purchase", etc.

    @NotNull(message = "Timestamp is required")
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    private String page;        // Current page/route
    private String elementId;   // Button ID, link ID, etc.
    private String sessionId;   // User session identifier
    private String userAgent;   // Browser info
    private String ipAddress;   // User IP (for analytics)

    // Constructors
    public UserActivityEvent() {
        this.timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    }

    public UserActivityEvent(String userId, String eventType, String page) {
        this.userId = userId;
        this.eventType = eventType;
        this.page = page;
        this.timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getPage() { return page; }
    public void setPage(String page) { this.page = page; }

    public String getElementId() { return elementId; }
    public void setElementId(String elementId) { this.elementId = elementId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    @Override
    public String toString() {
        return "UserActivityEvent{" +
                "userId='" + userId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", page='" + page + '\'' +
                ", elementId='" + elementId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}