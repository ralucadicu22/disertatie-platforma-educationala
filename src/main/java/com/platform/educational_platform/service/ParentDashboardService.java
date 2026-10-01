package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.ParentDashboardDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.UserProgress;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.ClassroomRepository;
import com.platform.educational_platform.repository.QuizAttemptRepository;
import com.platform.educational_platform.repository.UserProgressRepository;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParentDashboardService {

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final ClassroomRepository classroomRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public ParentDashboardDTO.Overview getOverview(User currentUser) {
        User parent = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Utilizator nu a fost gasit"));

        if (parent.getRole() != UserRole.PARINTE) {
            throw new IllegalStateException("Doar parintii pot accesa dashboard-ul");
        }

        List<User> children = userRepository.findChildrenByParentId(parent.getId());

        List<ParentDashboardDTO.ChildSummary> summaries = children.stream()
                .map(this::buildChildSummary)
                .collect(Collectors.toList());

        return ParentDashboardDTO.Overview.builder()
                .parentId(parent.getId())
                .parentName(parent.getName())
                .childrenCount(children.size())
                .children(summaries)
                .build();
    }

    public ParentDashboardDTO.ChildDetail getChildDetail(Long childId, User currentUser) {
        User parent = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Utilizator nu a fost gasit"));

        if (parent.getRole() != UserRole.PARINTE) {
            throw new IllegalStateException("Doar parintii pot accesa dashboard-ul");
        }

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalStateException("Elev inexistent"));

        boolean isMyChild = userRepository.findChildrenByParentId(parent.getId()).stream()
                .anyMatch(c -> c.getId().equals(childId));

        if (!isMyChild) {
            throw new IllegalStateException("Nu ai acces la acest elev");
        }

        return buildChildDetail(child);
    }

    private ParentDashboardDTO.ChildSummary buildChildSummary(User child) {
        List<UserProgress> progressList = userProgressRepository
                .findByUserIdOrderBySubmittedAtDesc(child.getId());

        long totalAttempts = progressList.size();
        long correctAttempts = progressList.stream().filter(UserProgress::getCorrect).count();
        int totalPoints = progressList.stream().mapToInt(UserProgress::getPointsEarned).sum();

        long distinctExercisesCompleted = progressList.stream()
                .filter(UserProgress::getCorrect)
                .map(p -> p.getExercise().getId())
                .distinct()
                .count();

        double successRate = totalAttempts == 0
                ? 0.0
                : ((double) correctAttempts / totalAttempts) * 100.0;

        LocalDateTime lastActivity = progressList.isEmpty() ? null
                : progressList.get(0).getSubmittedAt();

        String lastExerciseTitle = progressList.isEmpty()
                ? null
                : "Exercitiul #" + progressList.get(0).getExercise().getId();

        List<String> classroomNames = classroomRepository.findActiveByStudentId(child.getId()).stream()
                .map(c -> c.getName())
                .collect(Collectors.toList());

        Long totalSecondsSpent = userProgressRepository.sumDurationByUser(child.getId());
        long totalMinutesSpent = totalSecondsSpent != null ? totalSecondsSpent / 60 : 0;

        return ParentDashboardDTO.ChildSummary.builder()
                .childId(child.getId())
                .childName(child.getName())
                .childEmail(child.getEmail())
                .totalAttempts(totalAttempts)
                .correctAttempts(correctAttempts)
                .distinctExercisesCompleted(distinctExercisesCompleted)
                .totalPoints(totalPoints)
                .successRate(successRate)
                .lastActivityAt(lastActivity)
                .lastExerciseTitle(lastExerciseTitle)
                .classroomNames(classroomNames)
                .totalMinutesSpent(totalMinutesSpent)
                .build();
    }

    private ParentDashboardDTO.ChildDetail buildChildDetail(User child) {
        List<UserProgress> progressList = userProgressRepository
                .findByUserIdOrderBySubmittedAtDesc(child.getId());
        var attempts = quizAttemptRepository.findByUserIdOrderByStartedAtDesc(child.getId());

        long totalAttempts = progressList.size();
        long correctAttempts = progressList.stream().filter(UserProgress::getCorrect).count();
        int totalPoints = progressList.stream().mapToInt(UserProgress::getPointsEarned).sum();

        long distinctExercisesCompleted = progressList.stream()
                .filter(UserProgress::getCorrect)
                .map(p -> p.getExercise().getId())
                .distinct()
                .count();

        double successRate = totalAttempts == 0
                ? 0.0
                : ((double) correctAttempts / totalAttempts) * 100.0;

        LocalDateTime lastActivity = progressList.isEmpty() ? null
                : progressList.get(0).getSubmittedAt();

        Long totalSecondsSpent = userProgressRepository.sumDurationByUser(child.getId());
        long totalMinutesSpent = totalSecondsSpent != null ? totalSecondsSpent / 60 : 0;

        List<ParentDashboardDTO.QuizTimeInfo> quizTimeList = attempts.stream()
                .filter(a -> a.getFinishedAt() != null && a.getStartedAt() != null)
                .map(a -> ParentDashboardDTO.QuizTimeInfo.builder()
                        .attemptId(a.getId())
                        .quizId(a.getQuiz() != null ? a.getQuiz().getId() : null)
                        .quizTitle(a.getQuiz() != null ? a.getQuiz().getTitle() : "Quiz")
                        .startedAt(a.getStartedAt())
                        .finishedAt(a.getFinishedAt())
                        .minutesSpent(Duration.between(a.getStartedAt(), a.getFinishedAt()).toMinutes())
                        .status(a.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        List<ParentDashboardDTO.AttemptInfo> recentAttempts = progressList.stream()
                .limit(20)
                .map(this::mapAttempt)
                .collect(Collectors.toList());

        List<ParentDashboardDTO.ClassroomProgress> classroomProgress = buildClassroomProgress(child, progressList);

        return ParentDashboardDTO.ChildDetail.builder()
                .childId(child.getId())
                .childName(child.getName())
                .childEmail(child.getEmail())
                .totalAttempts(totalAttempts)
                .correctAttempts(correctAttempts)
                .distinctExercisesCompleted(distinctExercisesCompleted)
                .totalPoints(totalPoints)
                .successRate(successRate)
                .lastActivityAt(lastActivity)
                .totalMinutesSpent(totalMinutesSpent)
                .recentAttempts(recentAttempts)
                .classroomProgress(classroomProgress)
                .quizTimeList(quizTimeList)
                .build();
    }

    private ParentDashboardDTO.AttemptInfo mapAttempt(UserProgress p) {
        return ParentDashboardDTO.AttemptInfo.builder()
                .attemptId(p.getId())
                .exerciseId(p.getExercise().getId())
                .exerciseTitle("Exercitiul #" + p.getExercise().getId())
                .correct(p.getCorrect())
                .attemptNumber(p.getAttemptNumber())
                .pointsEarned(p.getPointsEarned())
                .submittedAt(p.getSubmittedAt())
                .build();
    }

    private List<ParentDashboardDTO.ClassroomProgress> buildClassroomProgress(User child, List<UserProgress> progressList) {
        var classrooms = classroomRepository.findActiveByStudentId(child.getId());

        return classrooms.stream().map(c -> {
            int assignedQuizzes = c.getAssignedQuizzes().size();

            long completedQuizzes = c.getAssignedQuizzes().stream().filter(quiz -> {
                Set<Long> quizExerciseIds = quiz.getExercises().stream()
                        .map(e -> e.getId())
                        .collect(Collectors.toSet());

                if (quizExerciseIds.isEmpty()) return false;

                Set<Long> completedInQuiz = progressList.stream()
                        .filter(p -> p.getCorrect() && quizExerciseIds.contains(p.getExercise().getId()))
                        .map(p -> p.getExercise().getId())
                        .collect(Collectors.toSet());

                return completedInQuiz.containsAll(quizExerciseIds);
            }).count();

            int pointsInClassroom = c.getAssignedQuizzes().stream()
                    .flatMap(quiz -> quiz.getExercises().stream())
                    .map(e -> e.getId())
                    .distinct()
                    .mapToInt(exerciseId -> progressList.stream()
                            .filter(p -> p.getExercise().getId().equals(exerciseId))
                            .mapToInt(UserProgress::getPointsEarned)
                            .sum())
                    .sum();

            return ParentDashboardDTO.ClassroomProgress.builder()
                    .classroomId(c.getId())
                    .classroomName(c.getName())
                    .assignedQuizzes(assignedQuizzes)
                    .completedQuizzes((int) completedQuizzes)
                    .totalPointsInClassroom(pointsInClassroom)
                    .build();
        }).collect(Collectors.toList());
    }
}