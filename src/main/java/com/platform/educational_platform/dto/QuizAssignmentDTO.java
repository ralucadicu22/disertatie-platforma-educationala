package com.platform.educational_platform.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class QuizAssignmentDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignRequest {
        private List<Long> quizIds;
        private List<Long> classroomIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignedQuizInfo {
        private Long quizId;
        private String quizTitle;
        private String quizDescription;
        private Long classroomId;
        private String classroomName;
        private int exerciseCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClassroomQuizzes {
        private Long classroomId;
        private String classroomName;
        private List<QuizInfo> quizzes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizInfo {
        private Long id;
        private String title;
        private String description;
        private int exerciseCount;
    }
}