package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.UserProgress;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    List<UserProgress> findByUserIdAndExerciseIdOrderBySubmittedAtAsc(
            Long userId, Long exerciseId);

    List<UserProgress> findByUserIdOrderBySubmittedAtDesc(Long userId);

    List<UserProgress> findByExerciseIdOrderBySubmittedAtDesc(Long exerciseId);

    int countByUserIdAndExerciseId(Long userId, Long exerciseId);

    int countByUserIdAndExerciseIdAndQuizAttemptId(Long userId, Long exerciseId, Long quizAttemptId);

    @Query("SELECT COUNT(DISTINCT up.exercise.id) FROM UserProgress up " +
            "WHERE up.user.id = :userId AND up.correct = true")
    Long countCompletedExercisesByUser(Long userId);

    @Query("SELECT COALESCE(SUM(up.pointsEarned), 0) FROM UserProgress up " +
            "WHERE up.user.id = :userId")
    Long sumPointsByUser(Long userId);

    @Query("SELECT COUNT(DISTINCT up.user.id) FROM UserProgress up " +
            "WHERE up.exercise.id = :exerciseId")
    Long countDistinctUsersByExercise(Long exerciseId);

    @Query("SELECT COUNT(up) FROM UserProgress up")
    Long countAllSubmissions();

    @Query("SELECT p FROM UserProgress p WHERE p.user.id = :userId ORDER BY p.submittedAt DESC")
    List<UserProgress> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COALESCE(AVG(up.pointsEarned), 0) FROM UserProgress up " +
            "WHERE up.exercise.id = :exerciseId AND up.correct = true")
    Double getAverageScoreForExercise(@Param("exerciseId") Long exerciseId);

    @Query("SELECT COALESCE(AVG(up.pointsEarned), 0) FROM UserProgress up " +
            "WHERE up.exercise.id = :exerciseId " +
            "AND up.user.id IN (" +
            "  SELECT s.id FROM Classroom c JOIN c.students s " +
            "  WHERE :userId IN (SELECT s2.id FROM c.students s2)" +
            ")")
    Double getClassAverageScoreForExercise(@Param("exerciseId") Long exerciseId,
                                           @Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(up.durationSeconds), 0) FROM UserProgress up WHERE up.user.id = :userId")
    Long sumDurationByUser(@Param("userId") Long userId);

    void deleteByExerciseId(Long exerciseId);

    @Query("""
        SELECT AVG(cnt) FROM (
            SELECT CAST(COUNT(up) AS double) AS cnt
            FROM UserProgress up
            WHERE up.user.id = :userId
            GROUP BY up.exercise.id
        ) counts
    """)
    Double getAvgAttemptsPerExercise(@Param("userId") Long userId);

    @Query("""
        SELECT AVG(cnt) FROM (
            SELECT CAST(COUNT(up) AS double) AS cnt
            FROM UserProgress up
            WHERE up.exercise.id = :exerciseId
            GROUP BY up.user.id
        ) counts
    """)
    Double getAvgAttemptsPerUserForExercise(@Param("exerciseId") Long exerciseId);

    @Query("""
        SELECT
            up.exercise.id,
            COUNT(up.id),
            MAX(CASE WHEN up.correct = true THEN true ELSE false END),
            MAX(up.pointsEarned)
        FROM UserProgress up
        WHERE up.user.id = :studentId
        GROUP BY up.exercise.id
        ORDER BY up.exercise.id
    """)
    List<Object[]> findAttemptsPerExerciseForStudent(@Param("studentId") Long studentId);

    @Query("""
        SELECT
            up.exercise.id,
            COUNT(up.id),
            SUM(CASE WHEN up.correct = true THEN 1 ELSE 0 END),
            AVG(up.attemptNumber)
        FROM UserProgress up
        GROUP BY up.exercise.id
        ORDER BY AVG(up.attemptNumber) DESC
    """)
    List<Object[]> findAttemptsStatsPerExercise();

    @Query("""
        SELECT
            up.exercise.id,
            AVG(up.pointsEarned),
            MAX(up.pointsEarned),
            MIN(up.pointsEarned),
            COUNT(up.id),
            COUNT(DISTINCT up.user.id),
            SUM(CASE WHEN up.correct = true THEN 1L ELSE 0L END)
        FROM UserProgress up
        GROUP BY up.exercise.id
        ORDER BY AVG(up.pointsEarned) ASC
    """)
    List<Object[]> findScoreStatsPerExercise();

    @Query("""
        SELECT
            up.user.id,
            up.user.name,
            COUNT(up.id),
            SUM(CASE WHEN up.correct = false THEN 1L ELSE 0L END),
            COUNT(DISTINCT up.exercise.id),
            COUNT(DISTINCT CASE WHEN up.correct = true THEN up.exercise.id END),
            AVG(up.attemptNumber)
        FROM UserProgress up
        GROUP BY up.user.id, up.user.name
        ORDER BY AVG(up.attemptNumber) DESC
    """)
    List<Object[]> findErrorPatternPerStudent();

    @Query("""
        SELECT up.user.id, up.user.name, up.user.email, MAX(up.submittedAt)
        FROM UserProgress up
        GROUP BY up.user.id, up.user.name, up.user.email
    """)
    List<Object[]> findLastActivityPerStudent();

    @Query("""
        SELECT up.user.id,
               COUNT(up.id),
               SUM(CASE WHEN up.correct = true THEN 1L ELSE 0L END)
        FROM UserProgress up
        GROUP BY up.user.id
    """)
    List<Object[]> findSuccessRatePerStudent();

    @Query("""
        SELECT up.user.id, up.exercise.id, COUNT(up.id)
        FROM UserProgress up
        WHERE up.user.id = :studentId
        GROUP BY up.user.id, up.exercise.id
        HAVING COUNT(up.id) >= 3
        ORDER BY COUNT(up.id) DESC
    """)
    List<Object[]> findHardExercisesForStudent(@Param("studentId") Long studentId);

    @Query("""
        SELECT up.quizAttempt.quiz.id,
               COUNT(DISTINCT up.exercise.id)
        FROM UserProgress up
        WHERE up.user.id = :studentId
          AND up.correct = true
        GROUP BY up.quizAttempt.quiz.id
    """)
    List<Object[]> findSolvedExercisesPerQuizForStudent(@Param("studentId") Long studentId);
}