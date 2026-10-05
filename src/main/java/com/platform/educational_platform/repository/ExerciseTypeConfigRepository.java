package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.ExerciseTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseTypeConfigRepository extends JpaRepository<ExerciseTypeConfig, Long> {
    Optional<ExerciseTypeConfig> findByExerciseType(String exerciseType);
    List<ExerciseTypeConfig> findAllByOrderByExerciseTypeAsc();
}
