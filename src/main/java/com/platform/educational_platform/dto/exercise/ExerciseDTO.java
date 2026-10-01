package com.platform.educational_platform.dto.exercise;

import com.platform.educational_platform.dto.metadata.TagDTO;
import com.platform.educational_platform.model.enums.GradeLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

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
    private String topic;
    private GradeLevel gradeLevel;
    private Set<TagDTO> tags;
    private String status;
    private String name;
}