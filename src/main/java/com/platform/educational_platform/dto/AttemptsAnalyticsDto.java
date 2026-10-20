package com.platform.educational_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AttemptsAnalyticsDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseAttempts {
        private Long exerciseId;
        private String exerciseTitle;
        private int attemptsUsed;
        private Integer maxAttempts;
        private boolean solved;
        private Double bestScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassAverageAttempts {
        private Long exerciseId;
        private String exerciseTitle;
        private Double avgAttemptsPerStudent;
        private int totalStudents;
        private Integer maxAttempts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentDashboard {
        private Long studentId;
        private String studentName;
        private List<ExerciseAttemptsWithClassAvg> exercises;
        private Double overallAvgAttempts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseAttemptsWithClassAvg {
        private Long exerciseId;
        private String exerciseTitle;
        private int attemptsUsed;
        private Integer maxAttempts;
        private boolean solved;
        private Double bestScore;
        private Double classAvgAttempts;
        private boolean showGroupAverage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseScoreRow {
        private Long   exerciseId;
        private String exerciseLabel;
        private Double avgScore;
        private Double maxScore;
        private Double minScore;
        private Long   totalAttempts;
        private Long   uniqueStudents;
        private Long   correctCount;
        private Double successRate;
        private Double errorRate;

        public ExerciseScoreRow(Long exerciseId, Double avgScore, Double maxScore,
                                Double minScore, Long totalAttempts,
                                Long uniqueStudents, Long correctCount) {
            this.exerciseId     = exerciseId;
            this.exerciseLabel  = "Exercițiul #" + exerciseId;
            this.avgScore       = avgScore;
            this.maxScore       = maxScore;
            this.minScore       = minScore;
            this.totalAttempts  = totalAttempts;
            this.uniqueStudents = uniqueStudents;
            this.correctCount   = correctCount;
            this.successRate    = totalAttempts > 0
                    ? Math.round((double) correctCount / totalAttempts * 1000.0) / 10.0
                    : 0.0;
            this.errorRate      = Math.round((100.0 - this.successRate) * 10.0) / 10.0;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentErrorPattern {
        private Long   userId;
        private String userName;
        private Long   totalAttempts;
        private Long   failedAttempts;
        private Long   exercisesTried;
        private Long   exercisesSolved;
        private Double avgAttemptsPerExercise;
        private Double overallSuccessRate;

        public StudentErrorPattern(Long userId, String userName, Long totalAttempts,
                                   Long failedAttempts, Long exercisesTried,
                                   Long exercisesSolved, Double avgAttemptsPerExercise) {
            this.userId                = userId;
            this.userName              = userName;
            this.totalAttempts         = totalAttempts;
            this.failedAttempts        = failedAttempts;
            this.exercisesTried        = exercisesTried;
            this.exercisesSolved       = exercisesSolved;
            this.avgAttemptsPerExercise = avgAttemptsPerExercise != null
                    ? Math.round(avgAttemptsPerExercise * 10.0) / 10.0 : 0.0;
            this.overallSuccessRate    = totalAttempts > 0
                    ? Math.round((double)(totalAttempts - failedAttempts) / totalAttempts * 1000.0) / 10.0
                    : 0.0;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdvancedAnalyticsResponse {
        private List<ExerciseScoreRow>    exerciseScores;
        private List<StudentErrorPattern> studentPatterns;
    }
}