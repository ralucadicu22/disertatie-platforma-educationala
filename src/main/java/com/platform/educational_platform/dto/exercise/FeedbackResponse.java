package com.platform.educational_platform.dto.exercise;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private boolean correct;
    private String message;
    private String hint;
    private String explanation;
    private Integer pointsEarned;
    private boolean showHint;
    private boolean showExplanation;
    private boolean attemptsExhausted;
    private List<TargetResult> targetResults;
    private Integer correctTargets;
    private Integer totalTargets;
}