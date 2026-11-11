package com.surveillance.robot.repository;

import com.surveillance.robot.model.Robot;
import com.surveillance.robot.model.RobotStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RobotRepository extends JpaRepository<Robot, Long> {

    Optional<Robot> findByRobotCode(String robotCode);

    boolean existsByRobotCode(String robotCode);

    long countByStatusNot(RobotStatus status);

    List<Robot> findByStatusNotAndLastSeenAtBefore(RobotStatus status, Instant cutoff);
}
