package com.platform.educational_platform.model;

import com.platform.educational_platform.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "impersonation_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpersonationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adminId;

    @Column(nullable = false)
    private String adminEmail;

    @Column(nullable = false)
    private String adminName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole impersonatedRole;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private LocalDateTime timestamp;
    @Column(length = 500)
    private String description;
}
