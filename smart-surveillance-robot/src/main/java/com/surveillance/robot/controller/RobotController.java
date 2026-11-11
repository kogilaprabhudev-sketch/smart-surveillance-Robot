package com.surveillance.robot.controller;

import com.surveillance.robot.dto.CommandRequest;
import com.surveillance.robot.dto.CommandResponse;
import com.surveillance.robot.dto.DetectionResponse;
import com.surveillance.robot.dto.PageResponse;
import com.surveillance.robot.dto.RobotCreateRequest;
import com.surveillance.robot.dto.RobotResponse;
import com.surveillance.robot.dto.SensorReadingResponse;
import com.surveillance.robot.service.CommandService;
import com.surveillance.robot.service.DetectionService;
import com.surveillance.robot.service.RobotService;
import com.surveillance.robot.service.SensorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Operator-facing endpoints (HTTP Basic, ADMIN or OPERATOR). */
@RestController
@RequestMapping("/api/robots")
public class RobotController {

    private final RobotService robotService;
    private final SensorService sensorService;
    private final DetectionService detectionService;
    private final CommandService commandService;

    public RobotController(RobotService robotService, SensorService sensorService,
                           DetectionService detectionService, CommandService commandService) {
        this.robotService = robotService;
        this.sensorService = sensorService;
        this.detectionService = detectionService;
        this.commandService = commandService;
    }

    @GetMapping
    public List<RobotResponse> list() {
        return robotService.list();
    }

    @GetMapping("/{robotCode}")
    public RobotResponse get(@PathVariable String robotCode) {
        return robotService.get(robotCode);
    }

    /** ADMIN only (see SecurityConfig). */
    @PostMapping
    public ResponseEntity<RobotResponse> create(@Valid @RequestBody RobotCreateRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(robotService.create(body));
    }

    @GetMapping("/{robotCode}/sensors/latest")
    public SensorReadingResponse latestSensors(@PathVariable String robotCode) {
        return sensorService.latest(robotCode);
    }

    @GetMapping("/{robotCode}/sensors")
    public PageResponse<SensorReadingResponse> sensors(
            @PathVariable String robotCode,
            @PageableDefault(size = 20, sort = "recordedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return sensorService.list(robotCode, pageable);
    }

    @GetMapping("/{robotCode}/detections")
    public PageResponse<DetectionResponse> detections(
            @PathVariable String robotCode,
            @PageableDefault(size = 20, sort = "detectedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return detectionService.list(robotCode, pageable);
    }

    @PostMapping("/{robotCode}/commands")
    public ResponseEntity<CommandResponse> sendCommand(@PathVariable String robotCode,
                                                       @Valid @RequestBody CommandRequest body,
                                                       Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.queueForRobot(robotCode, body.command(), auth.getName()));
    }

    @GetMapping("/{robotCode}/commands")
    public PageResponse<CommandResponse> commands(
            @PathVariable String robotCode,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return commandService.list(robotCode, pageable);
    }
}
