package com.surveillance.robot.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detection_events")
@Getter
@Setter
@NoArgsConstructor
public class DetectionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "robot_id", nullable = false)
    private Robot robot;

    @Column(nullable = false, length = 100)
    private String objectClass;

    @Column(nullable = false)
    private Double confidence;

    private Double latitude;
    private Double longitude;

    @Column(length = 500)
    private String imageUrl;

    /** True when this detection produced an alert message. */
    @Column(nullable = false)
    private boolean alertSent;

    @Column(nullable = false, updatable = false)
    private Instant detectedAt;

    @PrePersist
    void onCreate() {
        detectedAt = Instant.now();
    }
}
