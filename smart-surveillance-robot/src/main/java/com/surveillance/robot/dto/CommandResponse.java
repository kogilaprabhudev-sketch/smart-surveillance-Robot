package com.surveillance.robot.dto;

import com.surveillance.robot.model.CommandStatus;
import com.surveillance.robot.model.CommandType;
import com.surveillance.robot.model.MovementCommand;
import java.time.Instant;

public record CommandResponse(
        Long id, String robotCode, CommandType command, CommandStatus status,
        String issuedBy, Instant createdAt, Instant deliveredAt) {

    public static CommandResponse from(MovementCommand c) {
        return new CommandResponse(c.getId(), c.getRobot().getRobotCode(), c.getCommand(), c.getStatus(),
                c.getIssuedBy(), c.getCreatedAt(), c.getDeliveredAt());
    }
}
