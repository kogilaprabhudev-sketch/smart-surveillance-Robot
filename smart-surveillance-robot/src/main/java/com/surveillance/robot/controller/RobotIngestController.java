package com.surveillance.robot.controller;

import com.surveillance.robot.dto.CommandResponse;
import com.surveillance.robot.dto.DetectionRequest;
import com.surveillance.robot.dto.DetectionResponse;
import com.surveillance.robot.dto.HeartbeatRequest;
import com.surveillance.robot.dto.RobotResponse;
import com.surveillance.robot.dto.SensorReadingResponse;
import com.surveillance.robot.dto.SensorRequest;
import com.surveillance.robot.service.CommandService;
import com.surveillance.robot.service.DetectionService;
import com.surveillance.robot.service.RobotService;
import com.surveillance.robot.service.SensorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints called by the Raspberry Pi. Protected by the X-API-KEY header. */
@RestController
@RequestMapping("/api/robot/{robotCode}")
public class RobotIngestController {

    private final RobotService robotService;
    private final SensorService sensorService;
    private final DetectionService detectionService;
    private final CommandService commandService;

    public RobotIngestController(RobotService robotService, SensorService sensorService,
                                 DetectionService detectionService, CommandService commandService) {
        this.robotService = robotService;
        this.sensorService = sensorService;
        this.detectionService = detectionService;
        this.commandService = commandService;
    }

    @PostMapping("/heartbeat")
    public RobotResponse heartbeat(@PathVariable String robotCode,
                                   @Valid @RequestBody(required = false) HeartbeatRequest body) {
        return robotService.heartbeat(robotCode, body);
    }

    @PostMapping("/sensors")
    public ResponseEntity<SensorReadingResponse> sensors(@PathVariable String robotCode,
                                                         @Valid @RequestBody SensorRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sensorService.record(robotCode, body));
    }

    @PostMapping("/detections")
    public ResponseEntity<DetectionResponse> detections(@PathVariable String robotCode,
                                                        @Valid @RequestBody DetectionRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(detectionService.record(robotCode, body));
    }

    /** 200 with the next motor command, or 204 when nothing is waiting. */
    @GetMapping("/commands/next")
    public ResponseEntity<CommandResponse> nextCommand(@PathVariable String robotCode) {
        return commandService.nextFor(robotCode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
