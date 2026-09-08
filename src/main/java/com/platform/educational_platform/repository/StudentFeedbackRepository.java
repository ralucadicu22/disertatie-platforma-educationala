package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.StudentFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentFeedbackRepository extends JpaRepository<StudentFeedback, Long> {

    Optional<StudentFeedback> findByStudentIdAndExerciseId(Long studentId, Long exerciseId);

    List<StudentFeedback> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<StudentFeedback> findByExerciseIdOrderByCreatedAtDesc(Long exerciseId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT sf FROM StudentFeedback sf WHERE sf.exercise.id = :exerciseId " +
                    "AND sf.comment IS NOT NULL AND sf.comment <> '' ORDER BY sf.createdAt DESC")
    List<StudentFeedback> findWithCommentsByExerciseId(
            @org.springframework.data.repository.query.Param("exerciseId") Long exerciseId);
}