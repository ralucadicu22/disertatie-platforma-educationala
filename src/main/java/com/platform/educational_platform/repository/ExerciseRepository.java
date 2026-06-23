package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByQuizIdOrderByOrderIndexAsc(Long quizId);
}
