package com.platform.educational_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerRequest {

    private Long exerciseId;
    private Integer attemptNumber;

    private String simpleAnswer;

    private List<TargetAnswer> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TargetAnswer {
        private String targetId;
        private Integer value;
    }
}