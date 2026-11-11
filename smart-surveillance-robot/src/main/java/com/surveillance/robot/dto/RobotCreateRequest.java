package com.surveillance.robot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RobotCreateRequest(
        @NotBlank @Size(max = 40) @Pattern(regexp = "[A-Za-z0-9_-]+", message = "only letters, digits, - and _") String robotCode,
        @NotBlank @Size(max = 100) String name) {
}
