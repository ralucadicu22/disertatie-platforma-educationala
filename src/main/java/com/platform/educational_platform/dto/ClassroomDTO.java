package com.platform.educational_platform.dto;

import com.platform.educational_platform.model.enums.GradeLevel;
import com.platform.educational_platform.model.enums.UserRole;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class ClassroomDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String name;
        private String description;
        private String schoolYear;
        private GradeLevel gradeLevel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String name;
        private String description;
        private String schoolYear;
        private GradeLevel gradeLevel;
        private Boolean active;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberRequest {
        private Long userId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private Long id;
        private String name;
        private String description;
        private String schoolYear;
        private GradeLevel gradeLevel;
        private Boolean active;
        private LocalDateTime createdAt;
        private int teacherCount;
        private int studentCount;
        private int quizCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Detail {
        private Long id;
        private String name;
        private String description;
        private String schoolYear;
        private GradeLevel gradeLevel;
        private Boolean active;
        private LocalDateTime createdAt;
        private List<MemberInfo> teachers;
        private List<MemberInfo> students;
        private List<QuizInfo> assignedQuizzes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberInfo {
        private Long id;
        private String name;
        private String email;
        private UserRole role;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizInfo {
        private Long id;
        private String title;
        private String description;
    }
}