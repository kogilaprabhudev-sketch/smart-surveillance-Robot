package com.surveillance.robot.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "robot_id", nullable = false)
    private Robot robot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertType type;

    @Column(nullable = false, length = 500)
    private String message;

    /** True once the message was delivered to Telegram. */
    @Column(nullable = false)
    private boolean delivered;

    @Column(nullable = false)
    private boolean acknowledged;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant acknowledgedAt;

    @Column(length = 50)
    private String acknowledgedBy;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
