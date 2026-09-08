package com.platform.educational_platform.model;

import com.platform.educational_platform.model.enums.FeedbackTag;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private QuizAttempt quizAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Classroom classroom;

    @Column(nullable = false, length = 2000)
    private String message;
    @Column
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column
    private FeedbackTag tag;

    @Column(nullable = false)
    @Builder.Default
    private Boolean readByStudent = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.readByStudent == null) this.readByStudent = false;
    }
}