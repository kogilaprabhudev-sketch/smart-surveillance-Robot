package com.surveillance.robot.service;

import com.surveillance.robot.config.RobotProperties;
import com.surveillance.robot.dto.HeartbeatRequest;
import com.surveillance.robot.dto.RobotCreateRequest;
import com.surveillance.robot.dto.RobotResponse;
import com.surveillance.robot.exception.DuplicateResourceException;
import com.surveillance.robot.exception.ResourceNotFoundException;
import com.surveillance.robot.model.AlertType;
import com.surveillance.robot.model.Robot;
import com.surveillance.robot.model.RobotStatus;
import com.surveillance.robot.repository.RobotRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RobotService {

    private static final Logger log = LoggerFactory.getLogger(RobotService.class);

    private final RobotRepository robots;
    private final AlertService alertService;
    private final RobotProperties props;

    public RobotService(RobotRepository robots, AlertService alertService, RobotProperties props) {
        this.robots = robots;
        this.alertService = alertService;
        this.props = props;
    }

    /** Entity lookup for other services (call inside a transaction). */
    public Robot getByCode(String robotCode) {
        return robots.findByRobotCode(robotCode)
                .orElseThrow(() -> new ResourceNotFoundException("Robot '" + robotCode + "' not found"));
    }

    @Transactional
    public RobotResponse create(RobotCreateRequest req) {
        if (robots.existsByRobotCode(req.robotCode())) {
            throw new DuplicateResourceException("Robot '" + req.robotCode() + "' already exists");
        }
        return RobotResponse.from(robots.save(new Robot(req.robotCode(), req.name())));
    }

    @Transactional(readOnly = true)
    public List<RobotResponse> list() {
        return robots.findAll().stream().map(RobotResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RobotResponse get(String robotCode) {
        return RobotResponse.from(getByCode(robotCode));
    }

    @Transactional
    public RobotResponse heartbeat(String robotCode, HeartbeatRequest req) {
        Robot robot = getByCode(robotCode);
        touch(robot, req == null ? null : req.latitude(), req == null ? null : req.longitude());
        return RobotResponse.from(robot);
    }

    /** Records that the robot is alive and (optionally) where it is. */
    @Transactional
    public void touch(Robot robot, Double latitude, Double longitude) {
        robot.setLastSeenAt(Instant.now());
        if (robot.getStatus() == RobotStatus.OFFLINE) {
            robot.setStatus(RobotStatus.ONLINE);
        }
        if (latitude != null && longitude != null) {
            robot.setLatitude(latitude);
            robot.setLongitude(longitude);
        }
    }

    /** Marks robots OFFLINE when they have not reported for too long. */
    @Scheduled(fixedDelayString = "${robot.offline-check-interval-ms:15000}")
    @Transactional
    public void markStaleRobotsOffline() {
        Instant cutoff = Instant.now().minusSeconds(props.heartbeatTimeoutSeconds());
        for (Robot robot : robots.findByStatusNotAndLastSeenAtBefore(RobotStatus.OFFLINE, cutoff)) {
            robot.setStatus(RobotStatus.OFFLINE);
            alertService.createAlert(robot, AlertType.ROBOT_OFFLINE,
                    "Robot " + robot.getRobotCode() + " is offline (no data for "
                            + props.heartbeatTimeoutSeconds() + "s)");
            log.warn("Robot {} marked OFFLINE", robot.getRobotCode());
        }
    }
}
