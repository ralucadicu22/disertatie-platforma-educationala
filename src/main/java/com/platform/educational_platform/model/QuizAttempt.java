package com.platform.educational_platform.model;

import com.platform.educational_platform.model.enums.QuizAttemptStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Quiz quiz;

    @Column(nullable = false)
    private Integer attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizAttemptStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;

    @Column
    private Integer totalPoints;

    @Column
    private Integer correctExercises;

    @Column
    private Integer totalExercises;

    @PrePersist
    public void prePersist() {
        if (this.startedAt == null) this.startedAt = LocalDateTime.now();
        if (this.status == null) this.status = QuizAttemptStatus.IN_PROGRESS;
    }
}