package com.platform.educational_platform.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_timestamp", columnList = "timestamp DESC"),
        @Index(name = "idx_activity_user", columnList = "userId"),
        @Index(name = "idx_activity_action", columnList = "action")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Column(length = 255) private String userEmail;
    @Column(length = 255) private String userName;
    @Column(length = 50)  private String userRole;
    @Column(length = 100) private String action;
    @Column(length = 1000) private String description;
    @Column(length = 100)  private String entityType;
    @Column(length = 10)   private String httpMethod;
    @Column(length = 1000) private String endpoint;
    private Integer responseStatus;
    private Long durationMs;
    @Column(nullable = false) private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }
}