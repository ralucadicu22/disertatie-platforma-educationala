package com.platform.educational_platform.dto.exercise.version;

import com.platform.educational_platform.model.enums.VersionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseVersionDTO {
    private Long id;
    private Long exerciseId;
    private Integer versionNumber;
    private String config;
    private VersionStatus status;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime archivedAt;
    private String changeNote;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private String reviewNote;
    private Boolean isTemplate;
}