package com.surveillance.robot.repository;

import com.surveillance.robot.model.DetectionEvent;
import com.surveillance.robot.model.Robot;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetectionEventRepository extends JpaRepository<DetectionEvent, Long> {

    Page<DetectionEvent> findByRobot(Robot robot, Pageable pageable);

    Optional<DetectionEvent> findFirstByOrderByDetectedAtDesc();

    long countByDetectedAtAfter(Instant since);

    @Query("select d.objectClass as objectClass, count(d) as total "
            + "from DetectionEvent d where d.detectedAt >= :since "
            + "group by d.objectClass order by count(d) desc")
    List<ClassCount> countByClassSince(@Param("since") Instant since);

    interface ClassCount {
        String getObjectClass();

        long getTotal();
    }
}
