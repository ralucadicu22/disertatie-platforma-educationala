package com.platform.educational_platform.dto;

import com.platform.educational_platform.model.enums.StudentFeedbackReaction;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

public class StudentFeedbackDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmitRequest {
        private Long exerciseId;
        private Integer difficultyRating;
        private Integer enjoymentRating;
        private StudentFeedbackReaction reaction;
        private String comment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeedbackInfo {
        private Long id;
        private Long studentId;
        private String studentName;
        private Long exerciseId;
        private Integer difficultyRating;
        private Integer enjoymentRating;
        private StudentFeedbackReaction reaction;
        private String comment;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExerciseFeedbackSummary {
        private Long exerciseId;
        private long totalFeedback;

        private Double averageDifficulty;
        private Double averageEnjoyment;

        private Map<StudentFeedbackReaction, Long> reactionCounts;

        private long commentCount;
    }
}