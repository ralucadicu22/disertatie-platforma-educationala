package com.platform.educational_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long exerciseId;
    private Boolean correct;
    private Integer attemptNumber;
    private Integer pointsEarned;
    private LocalDateTime submittedAt;
}