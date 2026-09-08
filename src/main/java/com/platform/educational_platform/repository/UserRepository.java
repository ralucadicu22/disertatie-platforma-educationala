package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.UserProgress;
import com.platform.educational_platform.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);

    @Query("SELECT u FROM User u JOIN u.children c WHERE c.id = :childId")
    List<User> findParentsByChildId(@Param("childId") Long childId);

    @Query("SELECT c FROM User u JOIN u.children c WHERE u.id = :parentId")
    List<User> findChildrenByParentId(@Param("parentId") Long parentId);

    @Query("SELECT MAX(p.submittedAt) FROM UserProgress p WHERE p.user.id = :userId")
    LocalDateTime findLastActivityByUserId(@Param("userId") Long userId);


    @Query("SELECT p FROM UserProgress p WHERE p.user.id = :userId ORDER BY p.submittedAt DESC LIMIT 1")
    Optional<UserProgress> findLastAttemptByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM UserProgress p WHERE p.user.id = :userId AND p.exercise.quiz.id = :quizId")
    List<UserProgress> findByUserIdAndQuizId(@Param("userId") Long userId, @Param("quizId") Long quizId);

    @Query("SELECT p FROM UserProgress p WHERE p.user.id = :userId ORDER BY p.submittedAt DESC")
    List<UserProgress> findRecentByUserId(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);
}
