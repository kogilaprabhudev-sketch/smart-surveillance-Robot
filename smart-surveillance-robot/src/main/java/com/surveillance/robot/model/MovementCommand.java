package com.surveillance.robot.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movement_commands")
@Getter
@Setter
@NoArgsConstructor
public class MovementCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "robot_id", nullable = false)
    private Robot robot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommandType command;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommandStatus status = CommandStatus.PENDING;

    @Column(nullable = false, length = 50)
    private String issuedBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant deliveredAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
