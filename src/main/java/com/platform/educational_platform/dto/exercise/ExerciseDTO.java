package com.platform.educational_platform.dto.exercise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseDTO {
    private Long id;
    private String type;
    private String config;
    private Integer points;
    private String difficulty;
    private Integer orderIndex;
    private Integer maxAttempts;
}