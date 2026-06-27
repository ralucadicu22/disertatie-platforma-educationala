package com.platform.educational_platform.dto.metadata;

import com.platform.educational_platform.model.enums.GradeLevel;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateExerciseMetadataRequest {
    private String topic;
    private GradeLevel gradeLevel;
    private String difficulty;
    private Set<Long> tagIds;
}