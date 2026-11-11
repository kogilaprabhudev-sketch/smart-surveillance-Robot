package com.surveillance.robot.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sensor_readings")
@Getter
@Setter
@NoArgsConstructor
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "robot_id", nullable = false)
    private Robot robot;

    /** Distance from the ultrasonic sensor in cm; null means out of range. */
    private Double ultrasonicCm;

    @Column(nullable = false)
    private boolean irTriggered;

    @Column(nullable = false)
    private boolean soundTriggered;

    @Column(nullable = false, updatable = false)
    private Instant recordedAt;

    @PrePersist
    void onCreate() {
        recordedAt = Instant.now();
    }
}
