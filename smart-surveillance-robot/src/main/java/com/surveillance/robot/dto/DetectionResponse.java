package com.surveillance.robot.dto;

import com.surveillance.robot.model.DetectionEvent;
import java.time.Instant;

public record DetectionResponse(
        Long id, String robotCode, String objectClass, Double confidence,
        Double latitude, Double longitude, String imageUrl,
        boolean alertSent, Instant detectedAt) {

    public static DetectionResponse from(DetectionEvent d) {
        return new DetectionResponse(d.getId(), d.getRobot().getRobotCode(), d.getObjectClass(),
                d.getConfidence(), d.getLatitude(), d.getLongitude(), d.getImageUrl(),
                d.isAlertSent(), d.getDetectedAt());
    }
}
