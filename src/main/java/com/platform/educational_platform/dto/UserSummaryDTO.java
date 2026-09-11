package com.platform.educational_platform.dto;

import com.platform.educational_platform.model.enums.UserRole;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
}