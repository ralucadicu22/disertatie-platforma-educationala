package com.platform.educational_platform.dto;

import com.platform.educational_platform.model.enums.QuizAttemptStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class QuizAttemptDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttemptInfo {
        private Long id;
        private Long quizId;
        private String quizTitle;
        private Integer attemptNumber;
        private QuizAttemptStatus status;
        private LocalDateTime startedAt;
        private LocalDateTime finishedAt;
        private Integer totalPoints;
        private Integer correctExercises;
        private Integer totalExercises;
        private Integer maxReplays;
        private Integer replaysRemaining;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StartResponse {
        private Long attemptId;
        private Long quizId;
        private String quizTitle;
        private Integer attemptNumber;
        private Integer maxReplays;
        private Integer replaysRemaining;
        private LocalDateTime startedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FinishResponse {
        private Long attemptId;
        private Integer totalPoints;
        private Integer correctExercises;
        private Integer totalExercises;
        private double successRate;
        private LocalDateTime finishedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizHistory {
        private Long quizId;
        private String quizTitle;
        private Integer maxReplays;
        private Integer totalAttempts;
        private Integer replaysRemaining;
        private List<AttemptInfo> attempts;
    }
}