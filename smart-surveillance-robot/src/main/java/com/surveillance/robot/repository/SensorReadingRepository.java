package com.surveillance.robot.repository;

import com.surveillance.robot.model.Robot;
import com.surveillance.robot.model.SensorReading;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    Page<SensorReading> findByRobot(Robot robot, Pageable pageable);

    Optional<SensorReading> findFirstByRobotOrderByRecordedAtDesc(Robot robot);
}
