package com.surveillance.robot.service;

import com.surveillance.robot.config.RobotProperties;
import com.surveillance.robot.dto.PageResponse;
import com.surveillance.robot.dto.SensorReadingResponse;
import com.surveillance.robot.dto.SensorRequest;
import com.surveillance.robot.exception.ResourceNotFoundException;
import com.surveillance.robot.model.AlertType;
import com.surveillance.robot.model.Robot;
import com.surveillance.robot.model.SensorReading;
import com.surveillance.robot.repository.SensorReadingRepository;
import java.util.Locale;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorService {

    private final SensorReadingRepository readings;
    private final RobotService robotService;
    private final AlertService alertService;
    private final RobotProperties props;

    public SensorService(SensorReadingRepository readings, RobotService robotService,
                         AlertService alertService, RobotProperties props) {
        this.readings = readings;
        this.robotService = robotService;
        this.alertService = alertService;
        this.props = props;
    }

    @Transactional
    public SensorReadingResponse record(String robotCode, SensorRequest req) {
        Robot robot = robotService.getByCode(robotCode);
        robotService.touch(robot, req.latitude(), req.longitude());

        SensorReading reading = new SensorReading();
        reading.setRobot(robot);
        reading.setUltrasonicCm(req.ultrasonicCm());
        reading.setIrTriggered(Boolean.TRUE.equals(req.irTriggered()));
        reading.setSoundTriggered(Boolean.TRUE.equals(req.soundTriggered()));
        SensorReading saved = readings.save(reading);

        // Same rule as the Python driver: very close obstacle -> "Low Distance" alert
        if (req.ultrasonicCm() != null && req.ultrasonicCm() < props.distanceAlertCm()) {
            alertService.createAlert(robot, AlertType.LOW_DISTANCE,
                    String.format(Locale.ROOT, "Low Distance [%s]: obstacle at %.1f cm",
                            robot.getRobotCode(), req.ultrasonicCm()));
        }
        return SensorReadingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SensorReadingResponse latest(String robotCode) {
        Robot robot = robotService.getByCode(robotCode);
        return readings.findFirstByRobotOrderByRecordedAtDesc(robot)
                .map(SensorReadingResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No sensor readings yet for " + robotCode));
    }

    @Transactional(readOnly = true)
    public PageResponse<SensorReadingResponse> list(String robotCode, Pageable pageable) {
        Robot robot = robotService.getByCode(robotCode);
        return PageResponse.of(readings.findByRobot(robot, pageable).map(SensorReadingResponse::from));
    }
}
