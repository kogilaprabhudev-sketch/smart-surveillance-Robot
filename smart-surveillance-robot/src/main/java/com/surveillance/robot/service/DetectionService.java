package com.surveillance.robot.service;

import com.surveillance.robot.config.RobotProperties;
import com.surveillance.robot.dto.DetectionRequest;
import com.surveillance.robot.dto.DetectionResponse;
import com.surveillance.robot.dto.PageResponse;
import com.surveillance.robot.model.Alert;
import com.surveillance.robot.model.AlertType;
import com.surveillance.robot.model.CommandType;
import com.surveillance.robot.model.DetectionEvent;
import com.surveillance.robot.model.Robot;
import com.surveillance.robot.model.RobotStatus;
import com.surveillance.robot.repository.DetectionEventRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DetectionService {

    private final DetectionEventRepository detections;
    private final RobotService robotService;
    private final AlertService alertService;
    private final CommandService commandService;
    private final RobotProperties props;

    public DetectionService(DetectionEventRepository detections, RobotService robotService,
                            AlertService alertService, CommandService commandService, RobotProperties props) {
        this.detections = detections;
        this.robotService = robotService;
        this.alertService = alertService;
        this.commandService = commandService;
        this.props = props;
    }

    /**
     * Stores a YOLO detection. A detection is "valid" when its confidence reaches the
     * threshold AND its class is one of the predefined target classes (report section 4.7).
     * A valid detection raises an alert (sent to Telegram) and queues a STOP command.
     */
    @Transactional
    public DetectionResponse record(String robotCode, DetectionRequest req) {
        Robot robot = robotService.getByCode(robotCode);
        robotService.touch(robot, req.latitude(), req.longitude());

        DetectionEvent event = new DetectionEvent();
        event.setRobot(robot);
        event.setObjectClass(req.objectClass().trim().toLowerCase(Locale.ROOT));
        event.setConfidence(req.confidence());
        event.setLatitude(req.latitude());
        event.setLongitude(req.longitude());
        event.setImageUrl(req.imageUrl());

        if (isValidThreat(event)) {
            String message = String.format(Locale.ROOT,
                    "ALERT [%s] %s detected (confidence %.0f%%) at location %s",
                    robot.getRobotCode(), event.getObjectClass(), event.getConfidence() * 100,
                    location(req.latitude(), req.longitude()));
            Optional<Alert> alert = alertService.createAlert(robot, AlertType.DETECTION, message);
            event.setAlertSent(alert.isPresent());
            robot.setStatus(RobotStatus.ALERT);
            if (alert.isPresent()) {
                commandService.queue(robot, CommandType.STOP, "SYSTEM");
            }
        }
        return DetectionResponse.from(detections.save(event));
    }

    @Transactional(readOnly = true)
    public PageResponse<DetectionResponse> list(String robotCode, Pageable pageable) {
        var page = (robotCode == null || robotCode.isBlank())
                ? detections.findAll(pageable)
                : detections.findByRobot(robotService.getByCode(robotCode), pageable);
        return PageResponse.of(page.map(DetectionResponse::from));
    }

    @Transactional(readOnly = true)
    public Optional<DetectionResponse> latest() {
        return detections.findFirstByOrderByDetectedAtDesc().map(DetectionResponse::from);
    }

    private boolean isValidThreat(DetectionEvent e) {
        return e.getConfidence() >= props.confidenceThreshold()
                && props.targetClasses().stream().anyMatch(c -> c.trim().equalsIgnoreCase(e.getObjectClass()));
    }

    private static String location(Double lat, Double lon) {
        return (lat == null || lon == null) ? "unknown"
                : String.format(Locale.ROOT, "%.5f, %.5f", lat, lon);
    }
}
