package com.surveillance.robot.repository;

import com.surveillance.robot.model.CommandStatus;
import com.surveillance.robot.model.MovementCommand;
import com.surveillance.robot.model.Robot;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementCommandRepository extends JpaRepository<MovementCommand, Long> {

    Optional<MovementCommand> findFirstByRobotAndStatusOrderByCreatedAtAsc(Robot robot, CommandStatus status);

    Page<MovementCommand> findByRobot(Robot robot, Pageable pageable);
}
