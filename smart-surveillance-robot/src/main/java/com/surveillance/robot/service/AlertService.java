package com.surveillance.robot.service;

import com.surveillance.robot.config.RobotProperties;
import com.surveillance.robot.dto.AlertResponse;
import com.surveillance.robot.dto.PageResponse;
import com.surveillance.robot.exception.ResourceNotFoundException;
import com.surveillance.robot.model.Alert;
import com.surveillance.robot.model.AlertType;
import com.surveillance.robot.model.Robot;
import com.surveillance.robot.model.RobotStatus;
import com.surveillance.robot.repository.AlertRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

    private final AlertRepository alerts;
    private final ApplicationEventPublisher events;
    private final RobotProperties props;

    public AlertService(AlertRepository alerts, ApplicationEventPublisher events, RobotProperties props) {
        this.alerts = alerts;
        this.events = events;
        this.props = props;
    }

    /**
     * Stores an alert and queues it for Telegram. Returns empty when the same alert
     * type was already raised for this robot within the cooldown window (anti-spam).
     */
    @Transactional
    public Optional<Alert> createAlert(Robot robot, AlertType type, String message) {
        Instant since = Instant.now().minusSeconds(props.alertCooldownSeconds());
        if (alerts.existsByRobotAndTypeAndCreatedAtAfter(robot, type, since)) {
            return Optional.empty();
        }
        Alert alert = new Alert();
        alert.setRobot(robot);
        alert.setType(type);
        alert.setMessage(message.length() > 500 ? message.substring(0, 500) : message);
        Alert saved = alerts.save(alert);
        events.publishEvent(new AlertCreatedEvent(saved.getId(), saved.getMessage()));
        return Optional.of(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<AlertResponse> list(Boolean acknowledged, Pageable pageable) {
        var page = (acknowledged == null)
                ? alerts.findAll(pageable)
                : alerts.findByAcknowledged(acknowledged, pageable);
        return PageResponse.of(page.map(AlertResponse::from));
    }

    @Transactional
    public AlertResponse acknowledge(Long id, String username) {
        Alert alert = alerts.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert " + id + " not found"));
        if (!alert.isAcknowledged()) {
            alert.setAcknowledged(true);
            alert.setAcknowledgedAt(Instant.now());
            alert.setAcknowledgedBy(username);
            alerts.saveAndFlush(alert);

            Robot robot = alert.getRobot();
            if (robot.getStatus() == RobotStatus.ALERT && alerts.countByRobotAndAcknowledgedFalse(robot) == 0) {
                robot.setStatus(RobotStatus.ONLINE);
            }
        }
        return AlertResponse.from(alert);
    }
}
