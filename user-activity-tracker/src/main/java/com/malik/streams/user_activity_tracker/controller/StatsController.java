package com.malik.streams.user_activity_tracker.controller;

import com.malik.streams.user_activity_tracker.service.StatsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/top-pages")
    public ResponseEntity<?> getTopPages(
            @RequestParam(defaultValue = "24h") String window) {
        return ResponseEntity.ok(statsService.getTopPages(window));
    }

    @GetMapping("/active-users")
    public ResponseEntity<?> getActiveUsers(
            @RequestParam(defaultValue = "24h") String window) {
        return ResponseEntity.ok(statsService.getActiveUsers(window));
    }

    @GetMapping("/events/hourly")
    public ResponseEntity<?> getEventsHourly(
            @RequestParam(defaultValue = "24h") String window) {
        return ResponseEntity.ok(statsService.getEventsHourly(window));
    }
}