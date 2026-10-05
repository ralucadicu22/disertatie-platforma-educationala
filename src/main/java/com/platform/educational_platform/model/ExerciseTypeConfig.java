package com.platform.educational_platform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exercise_type_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseTypeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercise_type", nullable = false, unique = true, length = 50)
    private String exerciseType;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
