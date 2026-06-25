package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.ExerciseVersion;
import com.platform.educational_platform.model.enums.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExerciseVersionRepository extends JpaRepository<ExerciseVersion, Long> {

    List<ExerciseVersion> findByExerciseIdOrderByVersionNumberDesc(Long exerciseId);

    Optional<ExerciseVersion> findByExerciseIdAndStatus(Long exerciseId, VersionStatus status);

    @Query("SELECT COALESCE(MAX(v.versionNumber), 0) FROM ExerciseVersion v WHERE v.exercise.id = :exerciseId")
    Integer findMaxVersionNumber(@Param("exerciseId") Long exerciseId);

    List<ExerciseVersion> findByStatus(VersionStatus status);
    List<ExerciseVersion> findByStatusOrderByCreatedAtAsc(VersionStatus status);
    List<ExerciseVersion> findByIsTemplateTrueOrderByCreatedAtDesc();
}