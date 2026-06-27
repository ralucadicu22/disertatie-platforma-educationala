package com.platform.educational_platform.dto.exercise.version;

import lombok.Data;

@Data
public class CreateFromTemplateRequest {
    private Long quizId;
    private Long templateVersionId;
    private String changeNote;
}