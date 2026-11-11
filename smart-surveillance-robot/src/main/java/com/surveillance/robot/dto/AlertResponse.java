package com.surveillance.robot.dto;

import com.surveillance.robot.model.Alert;
import com.surveillance.robot.model.AlertType;
import java.time.Instant;

public record AlertResponse(
        Long id, String robotCode, AlertType type, String message,
        boolean delivered, boolean acknowledged,
        Instant createdAt, Instant acknowledgedAt, String acknowledgedBy) {

    public static AlertResponse from(Alert a) {
        return new AlertResponse(a.getId(), a.getRobot().getRobotCode(), a.getType(), a.getMessage(),
                a.isDelivered(), a.isAcknowledged(), a.getCreatedAt(), a.getAcknowledgedAt(), a.getAcknowledgedBy());
    }
}
