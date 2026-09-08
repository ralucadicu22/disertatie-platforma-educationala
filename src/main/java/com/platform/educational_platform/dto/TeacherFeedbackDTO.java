package com.platform.educational_platform.dto;

import com.platform.educational_platform.model.enums.FeedbackTag;
import lombok.*;
import java.time.LocalDateTime;

public class TeacherFeedbackDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Long studentId;
        private Long quizAttemptId;
        private Long classroomId;
        private String message;
        private Integer rating;
        private FeedbackTag tag;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String message;
        private Integer rating;
        private FeedbackTag tag;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeedbackInfo {
        private Long id;
        private Long teacherId;
        private String teacherName;
        private Long studentId;
        private String studentName;
        private Long quizAttemptId;
        private String quizTitle;
        private Long classroomId;
        private String classroomName;
        private String message;
        private Integer rating;
        private FeedbackTag tag;
        private Boolean readByStudent;
        private LocalDateTime createdAt;
        private LocalDateTime readAt;
    }
}