package com.surveillance.robot.service;

import com.surveillance.robot.dto.CommandResponse;
import com.surveillance.robot.dto.PageResponse;
import com.surveillance.robot.model.CommandStatus;
import com.surveillance.robot.model.CommandType;
import com.surveillance.robot.model.MovementCommand;
import com.surveillance.robot.model.Robot;
import com.surveillance.robot.repository.MovementCommandRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Operators queue motor commands; the robot polls and executes them. */
@Service
public class CommandService {

    private final MovementCommandRepository commands;
    private final RobotService robotService;

    public CommandService(MovementCommandRepository commands, RobotService robotService) {
        this.commands = commands;
        this.robotService = robotService;
    }

    /** Internal use (call inside a transaction). */
    @Transactional
    public MovementCommand queue(Robot robot, CommandType type, String issuedBy) {
        MovementCommand c = new MovementCommand();
        c.setRobot(robot);
        c.setCommand(type);
        c.setIssuedBy(issuedBy);
        return commands.save(c);
    }

    @Transactional
    public CommandResponse queueForRobot(String robotCode, CommandType type, String issuedBy) {
        Robot robot = robotService.getByCode(robotCode);
        return CommandResponse.from(queue(robot, type, issuedBy));
    }

    /** Called by the robot: returns the oldest pending command and marks it delivered. */
    @Transactional
    public Optional<CommandResponse> nextFor(String robotCode) {
        Robot robot = robotService.getByCode(robotCode);
        robotService.touch(robot, null, null);   // polling also proves the robot is alive
        return commands.findFirstByRobotAndStatusOrderByCreatedAtAsc(robot, CommandStatus.PENDING)
                .map(c -> {
                    c.setStatus(CommandStatus.DELIVERED);
                    c.setDeliveredAt(Instant.now());
                    return CommandResponse.from(c);
                });
    }

    @Transactional(readOnly = true)
    public PageResponse<CommandResponse> list(String robotCode, Pageable pageable) {
        Robot robot = robotService.getByCode(robotCode);
        return PageResponse.of(commands.findByRobot(robot, pageable).map(CommandResponse::from));
    }
}
