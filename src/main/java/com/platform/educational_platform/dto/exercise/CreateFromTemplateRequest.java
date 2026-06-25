package com.platform.educational_platform.dto.exercise;

import lombok.Data;

@Data
public class CreateFromTemplateRequest {
    private Long quizId;
    private Long templateVersionId;
    private String changeNote;
}