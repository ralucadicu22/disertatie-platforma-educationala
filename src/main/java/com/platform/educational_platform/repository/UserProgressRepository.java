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
}