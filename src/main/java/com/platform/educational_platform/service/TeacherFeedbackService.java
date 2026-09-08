package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.TeacherFeedbackDTO;
import com.platform.educational_platform.model.Classroom;
import com.platform.educational_platform.model.QuizAttempt;
import com.platform.educational_platform.model.TeacherFeedback;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.ClassroomRepository;
import com.platform.educational_platform.repository.QuizAttemptRepository;
import com.platform.educational_platform.repository.TeacherFeedbackRepository;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherFeedbackService {

    private final TeacherFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ClassroomRepository classroomRepository;

    @Transactional
    public TeacherFeedbackDTO.FeedbackInfo createFeedback(
            TeacherFeedbackDTO.CreateRequest req, User currentUser) {
        checkTeacherOrAdmin(currentUser);

        User student = userRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Elevul nu există"));

        if (student.getRole() != UserRole.ELEV) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Utilizatorul nu este elev");
        }

        if (currentUser.getRole() == UserRole.PROFESOR) {
            checkStudentInTeacherClassrooms(student.getId(), currentUser.getId());
        }

        QuizAttempt quizAttempt = null;
        if (req.getQuizAttemptId() != null) {
            quizAttempt = quizAttemptRepository.findById(req.getQuizAttemptId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Tentativa nu există"));

            if (!quizAttempt.getUser().getId().equals(student.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Tentativa nu aparține acestui elev");
            }
        }

        Classroom classroom = null;
        if (req.getClassroomId() != null) {
            classroom = classroomRepository.findById(req.getClassroomId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Clasa nu există"));
        }

        if (req.getRating() != null && (req.getRating() < 1 || req.getRating() > 5)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rating-ul trebuie să fie între 1 și 5");
        }

        if (req.getMessage() == null || req.getMessage().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Mesajul feedback-ului este obligatoriu");
        }

        TeacherFeedback feedback = TeacherFeedback.builder()
                .teacher(currentUser)
                .student(student)
                .quizAttempt(quizAttempt)
                .classroom(classroom)
                .message(req.getMessage().trim())
                .rating(req.getRating())
                .tag(req.getTag())
                .readByStudent(false)
                .build();

        return toDto(feedbackRepository.save(feedback));
    }

    @Transactional
    public TeacherFeedbackDTO.FeedbackInfo updateFeedback(
            Long feedbackId, TeacherFeedbackDTO.UpdateRequest req, User currentUser) {
        TeacherFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback inexistent"));

        if (currentUser.getRole() != UserRole.ADMIN &&
                !feedback.getTeacher().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Nu poți edita feedback-ul altui profesor");
        }

        if (req.getMessage() != null) {
            if (req.getMessage().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Mesajul nu poate fi gol");
            }
            feedback.setMessage(req.getMessage().trim());
        }
        if (req.getRating() != null) {
            if (req.getRating() < 1 || req.getRating() > 5) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Rating-ul trebuie să fie între 1 și 5");
            }
            feedback.setRating(req.getRating());
        }
        if (req.getTag() != null) {
            feedback.setTag(req.getTag());
        }

        return toDto(feedbackRepository.save(feedback));
    }

    @Transactional
    public void deleteFeedback(Long feedbackId, User currentUser) {
        TeacherFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback inexistent"));

        if (currentUser.getRole() != UserRole.ADMIN &&
                !feedback.getTeacher().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Nu poți șterge feedback-ul altui profesor");
        }

        feedbackRepository.delete(feedback);
    }

    @Transactional
    public void markAsRead(Long feedbackId, User currentUser) {
        TeacherFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback inexistent"));


        if (!feedback.getStudent().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Nu poți marca feedback-ul altui elev");
        }

        if (!feedback.getReadByStudent()) {
            feedback.setReadByStudent(true);
            feedback.setReadAt(LocalDateTime.now());
            feedbackRepository.save(feedback);
        }
    }


    @Transactional(readOnly = true)
    public List<TeacherFeedbackDTO.FeedbackInfo> getMyReceivedFeedback(User currentUser) {
        if (currentUser.getRole() != UserRole.ELEV) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Doar elevii pot vedea feedback-urile primite");
        }
        return feedbackRepository.findByStudentIdOrderByCreatedAtDesc(currentUser.getId())
                .stream().map(this::toDto).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public long getUnreadCount(User currentUser) {
        if (currentUser.getRole() != UserRole.ELEV) return 0;
        return feedbackRepository.countByStudentIdAndReadByStudentFalse(currentUser.getId());
    }


    @Transactional(readOnly = true)
    public List<TeacherFeedbackDTO.FeedbackInfo> getMyGivenFeedback(User currentUser) {
        checkTeacherOrAdmin(currentUser);
        return feedbackRepository.findByTeacherIdOrderByCreatedAtDesc(currentUser.getId())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeacherFeedbackDTO.FeedbackInfo> getFeedbackForStudent(Long studentId, User currentUser) {
        checkTeacherOrAdmin(currentUser);

        if (currentUser.getRole() == UserRole.PROFESOR) {
            checkStudentInTeacherClassrooms(studentId, currentUser.getId());
        }

        return feedbackRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeacherFeedbackDTO.FeedbackInfo> getFeedbackForAttempt(Long quizAttemptId, User currentUser) {
        QuizAttempt attempt = quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa nu există"));

        boolean isOwnerStudent = attempt.getUser().getId().equals(currentUser.getId());
        boolean isTeacherOrAdmin = currentUser.getRole() == UserRole.PROFESOR
                || currentUser.getRole() == UserRole.ADMIN;

        if (!isOwnerStudent && !isTeacherOrAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces interzis");
        }

        return feedbackRepository.findByQuizAttemptIdOrderByCreatedAtDesc(quizAttemptId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    private void checkTeacherOrAdmin(User user) {
        if (user.getRole() != UserRole.PROFESOR && user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Doar profesorii pot da feedback");
        }
    }

    private void checkStudentInTeacherClassrooms(Long studentId, Long teacherId) {
        List<Classroom> teacherClassrooms = classroomRepository.findByTeacherId(teacherId);
        boolean isInClassroom = teacherClassrooms.stream()
                .flatMap(c -> c.getStudents().stream())
                .anyMatch(s -> s.getId().equals(studentId));
        if (!isInClassroom) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Elevul nu face parte din niciuna dintre clasele tale");
        }
    }

    private TeacherFeedbackDTO.FeedbackInfo toDto(TeacherFeedback f) {
        return TeacherFeedbackDTO.FeedbackInfo.builder()
                .id(f.getId())
                .teacherId(f.getTeacher().getId())
                .teacherName(f.getTeacher().getName())
                .studentId(f.getStudent().getId())
                .studentName(f.getStudent().getName())
                .quizAttemptId(f.getQuizAttempt() != null ? f.getQuizAttempt().getId() : null)
                .quizTitle(f.getQuizAttempt() != null && f.getQuizAttempt().getQuiz() != null
                        ? f.getQuizAttempt().getQuiz().getTitle() : null)
                .classroomId(f.getClassroom() != null ? f.getClassroom().getId() : null)
                .classroomName(f.getClassroom() != null ? f.getClassroom().getName() : null)
                .message(f.getMessage())
                .rating(f.getRating())
                .tag(f.getTag())
                .readByStudent(f.getReadByStudent())
                .createdAt(f.getCreatedAt())
                .readAt(f.getReadAt())
                .build();
    }
}