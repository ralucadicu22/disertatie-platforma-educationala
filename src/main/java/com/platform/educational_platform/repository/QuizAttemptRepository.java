package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.QuizAttempt;
import com.platform.educational_platform.model.enums.QuizAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUserIdAndQuizIdOrderByAttemptNumberDesc(Long userId, Long quizId);

    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(Long userId);

    Optional<QuizAttempt> findFirstByUserIdAndQuizIdAndStatusOrderByAttemptNumberDesc(
            Long userId, Long quizId, QuizAttemptStatus status);

    int countByUserIdAndQuizId(Long userId, Long quizId);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.user.id = :userId " +
            "AND qa.quiz.id = :quizId AND qa.status = 'COMPLETED'")
    int countCompletedByUserAndQuiz(@Param("userId") Long userId, @Param("quizId") Long quizId);
}