package com.platform.educational_platform.dto.exercise.submission;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequest {

    private Long exerciseId;
    private Object answer;
    private Integer durationSeconds;
}