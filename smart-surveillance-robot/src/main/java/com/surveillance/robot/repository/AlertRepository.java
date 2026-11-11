package com.surveillance.robot.repository;

import com.surveillance.robot.model.Alert;
import com.surveillance.robot.model.AlertType;
import com.surveillance.robot.model.Robot;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByAcknowledged(boolean acknowledged, Pageable pageable);

    boolean existsByRobotAndTypeAndCreatedAtAfter(Robot robot, AlertType type, Instant since);

    long countByRobotAndAcknowledgedFalse(Robot robot);

    long countByAcknowledgedFalse();

    @Modifying
    @Transactional
    @Query("update Alert a set a.delivered = true where a.id = :id")
    int markDelivered(@Param("id") Long id);
}
