package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByModuleIdOrderByOrderIndexAsc(Long moduleId);
    @Query("SELECT DISTINCT q FROM Classroom c JOIN c.assignedQuizzes q JOIN c.students s " +
            "WHERE s.id = :studentId AND c.active = true")
    List<Quiz> findAssignedToStudent(@Param("studentId") Long studentId);
}