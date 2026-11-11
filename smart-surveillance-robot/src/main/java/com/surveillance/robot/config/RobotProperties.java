package com.surveillance.robot.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "robot")
public record RobotProperties(
        String apiKey,
        String defaultRobotCode,
        int heartbeatTimeoutSeconds,
        double distanceAlertCm,
        double confidenceThreshold,
        List<String> targetClasses,
        int alertCooldownSeconds) {

    public RobotProperties {
        if (targetClasses == null) {
            targetClasses = List.of("person");
        }
    }
}
