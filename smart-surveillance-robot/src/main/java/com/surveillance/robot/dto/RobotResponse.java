package com.surveillance.robot.dto;

import com.surveillance.robot.model.Robot;
import com.surveillance.robot.model.RobotStatus;
import java.time.Instant;

public record RobotResponse(
        Long id, String robotCode, String name, RobotStatus status,
        Double latitude, Double longitude, Instant lastSeenAt, Instant createdAt) {

    public static RobotResponse from(Robot r) {
        return new RobotResponse(r.getId(), r.getRobotCode(), r.getName(), r.getStatus(),
                r.getLatitude(), r.getLongitude(), r.getLastSeenAt(), r.getCreatedAt());
    }
}
