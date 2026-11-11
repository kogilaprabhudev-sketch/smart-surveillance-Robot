package com.surveillance.robot.controller;

import com.surveillance.robot.dto.StatsResponse;
import com.surveillance.robot.service.StatsService;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return statsService.summary();
    }

    /** Who am I? Handy for the dashboard and for testing credentials. */
    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        return Map.of("username", auth.getName(),
                "roles", auth.getAuthorities().stream().map(a -> a.getAuthority()).toList());
    }
}
