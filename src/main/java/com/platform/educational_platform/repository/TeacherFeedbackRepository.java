package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.TeacherFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherFeedbackRepository extends JpaRepository<TeacherFeedback, Long> {
    List<TeacherFeedback> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<TeacherFeedback> findByStudentIdAndReadByStudentFalseOrderByCreatedAtDesc(Long studentId);

    List<TeacherFeedback> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    List<TeacherFeedback> findByQuizAttemptIdOrderByCreatedAtDesc(Long quizAttemptId);

    List<TeacherFeedback> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);
    long countByStudentIdAndReadByStudentFalse(Long studentId);
}