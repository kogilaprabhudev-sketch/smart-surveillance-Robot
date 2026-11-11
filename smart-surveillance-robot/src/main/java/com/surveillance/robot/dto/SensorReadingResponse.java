package com.surveillance.robot.dto;

import com.surveillance.robot.model.SensorReading;
import java.time.Instant;

public record SensorReadingResponse(
        Long id, String robotCode, Double ultrasonicCm,
        boolean irTriggered, boolean soundTriggered, Instant recordedAt) {

    public static SensorReadingResponse from(SensorReading s) {
        return new SensorReadingResponse(s.getId(), s.getRobot().getRobotCode(), s.getUltrasonicCm(),
                s.isIrTriggered(), s.isSoundTriggered(), s.getRecordedAt());
    }
}
