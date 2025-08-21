package com.malik.streams.user_activity_tracker.service;

import com.malik.streams.user_activity_tracker.repository.StatsRepository;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class StatsService {

    private final StatsRepository statsRepository;

    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public Map<String, Long> getTopPages(String window) {
        return statsRepository.queryTopPages(window);
    }

    public Long getActiveUsers(String window) {
        return statsRepository.queryActiveUsers(window);
    }

    public Map<String, Long> getEventsHourly(String window) {
        return statsRepository.queryEventsHourly(window);
    }
}
