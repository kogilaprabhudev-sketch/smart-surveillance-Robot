package com.surveillance.robot.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** One YOLO detection reported by the robot. */
public record DetectionRequest(
        @NotBlank @Size(max = 100) String objectClass,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double confidence,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @Size(max = 500) String imageUrl) {
}
