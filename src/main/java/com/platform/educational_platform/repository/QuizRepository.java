package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByModuleIdOrderByOrderIndexAsc(Long moduleId);
}