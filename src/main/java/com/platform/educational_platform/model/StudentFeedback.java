package com.platform.educational_platform.model;

import com.platform.educational_platform.model.enums.StudentFeedbackReaction;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_feedback",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_exercise_feedback",
                columnNames = {"student_id", "exercise_id"}
        ))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Exercise exercise;

    @Column
    private Integer difficultyRating;

    @Column
    private Integer enjoymentRating;

    @Enumerated(EnumType.STRING)
    @Column
    private StudentFeedbackReaction reaction;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}