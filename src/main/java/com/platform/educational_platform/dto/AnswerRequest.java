package com.platform.educational_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {
    private Long exerciseId;
    private String type;
    private Integer attemptNumber;

    private List<TargetAnswer> answers;

    private String simpleAnswer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetAnswer {
        private Long targetId;
        private Integer value;
    }
}