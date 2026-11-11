package com.surveillance.robot.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "robots")
@Getter
@Setter
@NoArgsConstructor
public class Robot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String robotCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RobotStatus status = RobotStatus.OFFLINE;

    private Double latitude;
    private Double longitude;
    private Instant lastSeenAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Robot(String robotCode, String name) {
        this.robotCode = robotCode;
        this.name = name;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
