package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.StudentFeedbackDTO;
import com.platform.educational_platform.model.Exercise;
import com.platform.educational_platform.model.StudentFeedback;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.StudentFeedbackReaction;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.ExerciseRepository;
import com.platform.educational_platform.repository.StudentFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentFeedbackService {

    private final StudentFeedbackRepository feedbackRepository;
    private final ExerciseRepository exerciseRepository;
    @Transactional
    public StudentFeedbackDTO.FeedbackInfo submitFeedback(
            StudentFeedbackDTO.SubmitRequest req, User currentUser) {
        checkStudent(currentUser);

        Exercise exercise = exerciseRepository.findById(req.getExerciseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercițiul nu există"));
        if (req.getDifficultyRating() != null &&
                (req.getDifficultyRating() < 1 || req.getDifficultyRating() > 5)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rating-ul dificultății trebuie să fie între 1 și 5");
        }
        if (req.getEnjoymentRating() != null &&
                (req.getEnjoymentRating() < 1 || req.getEnjoymentRating() > 5)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rating-ul enjoyment trebuie să fie între 1 și 5");
        }

        boolean hasContent = req.getDifficultyRating() != null
                || req.getEnjoymentRating() != null
                || req.getReaction() != null
                || (req.getComment() != null && !req.getComment().trim().isEmpty());
        if (!hasContent) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Feedback-ul trebuie să conțină cel puțin un rating, o reacție sau un comentariu");
        }

        Optional<StudentFeedback> existing = feedbackRepository
                .findByStudentIdAndExerciseId(currentUser.getId(), exercise.getId());

        StudentFeedback feedback;
        if (existing.isPresent()) {
            feedback = existing.get();
            feedback.setDifficultyRating(req.getDifficultyRating());
            feedback.setEnjoymentRating(req.getEnjoymentRating());
            feedback.setReaction(req.getReaction());
            feedback.setComment(req.getComment() != null ? req.getComment().trim() : null);
        } else {
            feedback = StudentFeedback.builder()
                    .student(currentUser)
                    .exercise(exercise)
                    .difficultyRating(req.getDifficultyRating())
                    .enjoymentRating(req.getEnjoymentRating())
                    .reaction(req.getReaction())
                    .comment(req.getComment() != null ? req.getComment().trim() : null)
                    .build();
        }

        return toDto(feedbackRepository.save(feedback));
    }

    @Transactional
    public void deleteMyFeedback(Long exerciseId, User currentUser) {
        checkStudent(currentUser);
        StudentFeedback feedback = feedbackRepository
                .findByStudentIdAndExerciseId(currentUser.getId(), exerciseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Nu ai lăsat feedback pentru acest exercițiu"));
        feedbackRepository.delete(feedback);
    }

    @Transactional(readOnly = true)
    public Optional<StudentFeedbackDTO.FeedbackInfo> getMyFeedbackForExercise(
            Long exerciseId, User currentUser) {
        return feedbackRepository
                .findByStudentIdAndExerciseId(currentUser.getId(), exerciseId)
                .map(this::toDto);
    }
    @Transactional(readOnly = true)
    public List<StudentFeedbackDTO.FeedbackInfo> getMyFeedback(User currentUser) {
        checkStudent(currentUser);
        return feedbackRepository.findByStudentIdOrderByCreatedAtDesc(currentUser.getId())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentFeedbackDTO.FeedbackInfo> getFeedbackForExercise(
            Long exerciseId, User currentUser) {
        checkTeacherEditorOrAdmin(currentUser);
        return feedbackRepository.findByExerciseIdOrderByCreatedAtDesc(exerciseId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentFeedbackDTO.ExerciseFeedbackSummary getExerciseSummary(
            Long exerciseId, User currentUser) {
        checkTeacherEditorOrAdmin(currentUser);

        List<StudentFeedback> feedbacks = feedbackRepository.findByExerciseIdOrderByCreatedAtDesc(exerciseId);

        long total = feedbacks.size();
        Double avgDifficulty = feedbacks.stream()
                .filter(f -> f.getDifficultyRating() != null)
                .mapToInt(StudentFeedback::getDifficultyRating)
                .average().orElse(0.0);

        Double avgEnjoyment = feedbacks.stream()
                .filter(f -> f.getEnjoymentRating() != null)
                .mapToInt(StudentFeedback::getEnjoymentRating)
                .average().orElse(0.0);

        Map<StudentFeedbackReaction, Long> reactionCounts = feedbacks.stream()
                .filter(f -> f.getReaction() != null)
                .collect(Collectors.groupingBy(
                        StudentFeedback::getReaction,
                        Collectors.counting()));

        long commentCount = feedbacks.stream()
                .filter(f -> f.getComment() != null && !f.getComment().trim().isEmpty())
                .count();

        return StudentFeedbackDTO.ExerciseFeedbackSummary.builder()
                .exerciseId(exerciseId)
                .totalFeedback(total)
                .averageDifficulty(round(avgDifficulty))
                .averageEnjoyment(round(avgEnjoyment))
                .reactionCounts(reactionCounts)
                .commentCount(commentCount)
                .build();
    }

    private void checkStudent(User user) {
        if (user.getRole() != UserRole.ELEV) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Doar elevii pot lăsa feedback pe exerciții");
        }
    }

    private void checkTeacherEditorOrAdmin(User user) {
        if (user.getRole() != UserRole.PROFESOR
                && user.getRole() != UserRole.EDITOR
                && user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Acces interzis");
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private StudentFeedbackDTO.FeedbackInfo toDto(StudentFeedback f) {
        return StudentFeedbackDTO.FeedbackInfo.builder()
                .id(f.getId())
                .studentId(f.getStudent().getId())
                .studentName(f.getStudent().getName())
                .exerciseId(f.getExercise().getId())
                .difficultyRating(f.getDifficultyRating())
                .enjoymentRating(f.getEnjoymentRating())
                .reaction(f.getReaction())
                .comment(f.getComment())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}