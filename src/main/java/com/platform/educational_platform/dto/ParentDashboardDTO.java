package com.platform.educational_platform.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class ParentDashboardDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Overview {
        private Long parentId;
        private String parentName;
        private int childrenCount;
        private List<ChildSummary> children;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChildSummary {
        private Long childId;
        private String childName;
        private String childEmail;

        private long totalAttempts;
        private long correctAttempts;
        private long distinctExercisesCompleted;
        private int totalPoints;
        private double successRate;
        private LocalDateTime lastActivityAt;
        private String lastExerciseTitle;
        private List<String> classroomNames;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChildDetail {
        private Long childId;
        private String childName;
        private String childEmail;
        private long totalAttempts;
        private long correctAttempts;
        private long distinctExercisesCompleted;
        private int totalPoints;
        private double successRate;

        private LocalDateTime lastActivityAt;
        private List<AttemptInfo> recentAttempts;
        private List<ClassroomProgress> classroomProgress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttemptInfo {
        private Long attemptId;
        private Long exerciseId;
        private String exerciseTitle;
        private boolean correct;
        private int attemptNumber;
        private int pointsEarned;
        private LocalDateTime submittedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClassroomProgress {
        private Long classroomId;
        private String classroomName;
        private int assignedQuizzes;
        private int completedQuizzes;
        private int totalPointsInClassroom;
    }
}