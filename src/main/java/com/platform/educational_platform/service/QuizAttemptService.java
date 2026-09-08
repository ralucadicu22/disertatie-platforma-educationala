package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.QuizAttemptDTO;
import com.platform.educational_platform.model.Quiz;
import com.platform.educational_platform.model.QuizAttempt;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.UserProgress;
import com.platform.educational_platform.model.enums.QuizAttemptStatus;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.QuizAttemptRepository;
import com.platform.educational_platform.repository.QuizRepository;
import com.platform.educational_platform.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final UserProgressRepository userProgressRepository;
    @Transactional
    public QuizAttemptDTO.StartResponse startQuiz(Long quizId, User currentUser) {
        checkStudent(currentUser);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quizul nu există"));

        Optional<QuizAttempt> active = quizAttemptRepository
                .findFirstByUserIdAndQuizIdAndStatusOrderByAttemptNumberDesc(
                        currentUser.getId(), quizId, QuizAttemptStatus.IN_PROGRESS);

        if (active.isPresent()) {
            return toStartResponse(active.get(), quiz);
        }

        int completedCount = quizAttemptRepository.countCompletedByUserAndQuiz(
                currentUser.getId(), quizId);
        Integer maxReplays = quiz.getMaxReplays() != null ? quiz.getMaxReplays() : 0;

        if (maxReplays > 0 && completedCount >= maxReplays) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ai atins limita de " + maxReplays + " tentative pentru acest quiz");
        }

        int totalAttempts = quizAttemptRepository.countByUserIdAndQuizId(
                currentUser.getId(), quizId);
        QuizAttempt attempt = QuizAttempt.builder()
                .user(currentUser)
                .quiz(quiz)
                .attemptNumber(totalAttempts + 1)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .build();

        attempt = quizAttemptRepository.save(attempt);
        return toStartResponse(attempt, quiz);
    }
    @Transactional
    public QuizAttemptDTO.FinishResponse finishQuiz(Long attemptId, User currentUser) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa nu există"));

        if (!attempt.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Nu poți închide tentativa altui elev");
        }

        if (attempt.getStatus() != QuizAttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tentativa este deja închisă (status: " + attempt.getStatus() + ")");
        }

        List<UserProgress> submissions = userProgressRepository
                .findByUserIdOrderBySubmittedAtDesc(currentUser.getId()).stream()
                .filter(p -> p.getQuizAttempt() != null
                        && p.getQuizAttempt().getId().equals(attemptId))
                .collect(Collectors.toList());

        Quiz quiz = attempt.getQuiz();
        int totalExercises = quiz.getExercises() != null ? quiz.getExercises().size() : 0;

        int correctExercises = 0;
        int totalPoints = 0;
        if (quiz.getExercises() != null) {
            for (var ex : quiz.getExercises()) {
                Optional<UserProgress> best = submissions.stream()
                        .filter(s -> s.getExercise().getId().equals(ex.getId()))
                        .max((a, b) -> Integer.compare(a.getPointsEarned(), b.getPointsEarned()));
                if (best.isPresent()) {
                    if (best.get().getCorrect()) correctExercises++;
                    totalPoints += best.get().getPointsEarned();
                }
            }
        }

        attempt.setStatus(QuizAttemptStatus.COMPLETED);
        attempt.setFinishedAt(LocalDateTime.now());
        attempt.setTotalPoints(totalPoints);
        attempt.setCorrectExercises(correctExercises);
        attempt.setTotalExercises(totalExercises);

        quizAttemptRepository.save(attempt);

        double successRate = totalExercises > 0
                ? (correctExercises * 100.0 / totalExercises) : 0.0;

        return QuizAttemptDTO.FinishResponse.builder()
                .attemptId(attempt.getId())
                .totalPoints(totalPoints)
                .correctExercises(correctExercises)
                .totalExercises(totalExercises)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .finishedAt(attempt.getFinishedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public QuizAttemptDTO.QuizHistory getMyQuizHistory(Long quizId, User currentUser) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quizul nu există"));

        List<QuizAttempt> attempts = quizAttemptRepository
                .findByUserIdAndQuizIdOrderByAttemptNumberDesc(currentUser.getId(), quizId);

        Integer maxReplays = quiz.getMaxReplays() != null ? quiz.getMaxReplays() : 0;
        int completedCount = (int) attempts.stream()
                .filter(a -> a.getStatus() == QuizAttemptStatus.COMPLETED).count();
        Integer replaysRemaining = maxReplays > 0
                ? Math.max(0, maxReplays - completedCount) : null;

        return QuizAttemptDTO.QuizHistory.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .maxReplays(maxReplays)
                .totalAttempts(attempts.size())
                .replaysRemaining(replaysRemaining)
                .attempts(attempts.stream()
                        .map(a -> toAttemptInfo(a, quiz))
                        .collect(Collectors.toList()))
                .build();
    }

    private void checkStudent(User user) {
        if (user.getRole() != UserRole.ELEV) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Doar elevii pot începe quizuri");
        }
    }

    private QuizAttemptDTO.StartResponse toStartResponse(QuizAttempt attempt, Quiz quiz) {
        Integer maxReplays = quiz.getMaxReplays() != null ? quiz.getMaxReplays() : 0;
        int completedCount = quizAttemptRepository.countCompletedByUserAndQuiz(
                attempt.getUser().getId(), quiz.getId());
        Integer replaysRemaining = maxReplays > 0
                ? Math.max(0, maxReplays - completedCount) : null;

        return QuizAttemptDTO.StartResponse.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .attemptNumber(attempt.getAttemptNumber())
                .maxReplays(maxReplays)
                .replaysRemaining(replaysRemaining)
                .startedAt(attempt.getStartedAt())
                .build();
    }

    private QuizAttemptDTO.AttemptInfo toAttemptInfo(QuizAttempt a, Quiz quiz) {
        Integer maxReplays = quiz.getMaxReplays() != null ? quiz.getMaxReplays() : 0;
        Integer replaysRemaining = maxReplays > 0
                ? Math.max(0, maxReplays - a.getAttemptNumber()) : null;

        return QuizAttemptDTO.AttemptInfo.builder()
                .id(a.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .attemptNumber(a.getAttemptNumber())
                .status(a.getStatus())
                .startedAt(a.getStartedAt())
                .finishedAt(a.getFinishedAt())
                .totalPoints(a.getTotalPoints())
                .correctExercises(a.getCorrectExercises())
                .totalExercises(a.getTotalExercises())
                .maxReplays(maxReplays)
                .replaysRemaining(replaysRemaining)
                .build();
    }
}