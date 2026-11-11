package com.surveillance.robot.service;

import com.surveillance.robot.dto.StatsResponse;
import com.surveillance.robot.model.RobotStatus;
import com.surveillance.robot.repository.AlertRepository;
import com.surveillance.robot.repository.DetectionEventRepository;
import com.surveillance.robot.repository.RobotRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {

    private final RobotRepository robots;
    private final DetectionEventRepository detections;
    private final AlertRepository alerts;

    public StatsService(RobotRepository robots, DetectionEventRepository detections, AlertRepository alerts) {
        this.robots = robots;
        this.detections = detections;
        this.alerts = alerts;
    }

    @Transactional(readOnly = true)
    public StatsResponse summary() {
        Instant since = Instant.now().minus(Duration.ofHours(24));
        List<StatsResponse.ClassCount> byClass = detections.countByClassSince(since).stream()
                .map(c -> new StatsResponse.ClassCount(c.getObjectClass(), c.getTotal()))
                .toList();
        return new StatsResponse(
                robots.count(),
                robots.countByStatusNot(RobotStatus.OFFLINE),
                detections.countByDetectedAtAfter(since),
                alerts.countByAcknowledgedFalse(),
                byClass);
    }
}
