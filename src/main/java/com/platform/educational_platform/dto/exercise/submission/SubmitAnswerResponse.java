package com.platform.educational_platform.dto.exercise.submission;

import com.platform.educational_platform.dto.BadgeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerResponse {

    private boolean correct;
    private double score;
    private Map<String, Object> details;
    private Integer maxAttempts;
    private Integer attemptNumber;

    private Integer attemptsRemaining;
    private List<BadgeDTO> newBadges;
}