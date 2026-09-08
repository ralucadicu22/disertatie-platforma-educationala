package com.platform.educational_platform.model;

import com.platform.educational_platform.model.enums.GradeLevel;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "classrooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private String schoolYear;

    @Enumerated(EnumType.STRING)
    @Column
    private GradeLevel gradeLevel;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private Boolean active;

    @ManyToMany
    @JoinTable(
            name = "classroom_teachers",
            joinColumns = @JoinColumn(name = "classroom_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> teachers = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "classroom_students",
            joinColumns = @JoinColumn(name = "classroom_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> students = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "classroom_quizzes",
            joinColumns = @JoinColumn(name = "classroom_id"),
            inverseJoinColumns = @JoinColumn(name = "quiz_id")
    )
    @Builder.Default
    private Set<Quiz> assignedQuizzes = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
    }
}