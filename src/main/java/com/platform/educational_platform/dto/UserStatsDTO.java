package com.platform.educational_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {
    private Long userId;
    private String userName;
    private Long completedExercises;
    private Long totalSubmissions;
    private Long totalPoints;
}