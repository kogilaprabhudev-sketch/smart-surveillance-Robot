package com.surveillance.robot.dto;

import com.surveillance.robot.model.CommandType;
import jakarta.validation.constraints.NotNull;

public record CommandRequest(@NotNull CommandType command) {
}
