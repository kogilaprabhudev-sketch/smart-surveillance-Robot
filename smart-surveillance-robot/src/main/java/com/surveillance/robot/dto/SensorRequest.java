package com.surveillance.robot.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** One reading of the robot's ultrasonic, IR and sound sensors. */
public record SensorRequest(
        @PositiveOrZero Double ultrasonicCm,
        @NotNull Boolean irTriggered,
        @NotNull Boolean soundTriggered,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {
}
