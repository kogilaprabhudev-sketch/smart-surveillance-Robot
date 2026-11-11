package com.surveillance.robot.dto;

import java.util.List;

public record StatsResponse(
        long totalRobots,
        long activeRobots,
        long detectionsLast24h,
        long openAlerts,
        List<ClassCount> detectionsByClass) {

    public record ClassCount(String objectClass, long total) {
    }
}
