package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.ParentDashboardDTO;
import com.platform.educational_platform.model.Classroom;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.UserProgress;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.ClassroomRepository;
import com.platform.educational_platform.repository.UserProgressRepository;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentDashboardService {

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final ClassroomRepository classroomRepository;

    private static final int RECENT_ATTEMPTS_LIMIT = 20;

    @Transactional(readOnly = true)
    public ParentDashboardDTO.Overview getOverview(User currentUser) {
        checkParent(currentUser);

        List<User> children = userRepository.findChildrenByParentId(currentUser.getId());

        List<ParentDashboardDTO.ChildSummary> childSummaries = children.stream()
                .map(this::buildChildSummary)
                .collect(Collectors.toList());

        return ParentDashboardDTO.Overview.builder()
                .parentId(currentUser.getId())
                .parentName(currentUser.getName())
                .childrenCount(children.size())
                .children(childSummaries)
                .build();
    }
    @Transactional(readOnly = true)
    public ParentDashboardDTO.ChildDetail getChildDetail(Long childId, User currentUser) {
        checkParent(currentUser);

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Copilul nu există"));

        boolean isMyChild = currentUser.getChildren().stream()
                .anyMatch(c -> c.getId().equals(childId));
        if (!isMyChild) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Nu ai acces la datele acestui elev");
        }

        List<UserProgress> allAttempts = userProgressRepository.findByUserIdOrderBySubmittedAtDesc(child.getId());
        long totalAttempts = allAttempts.size();
        long correctAttempts = allAttempts.stream().filter(UserProgress::getCorrect).count();
        long distinctExercises = allAttempts.stream()
                .map(p -> p.getExercise().getId())
                .distinct()
                .count();
        int totalPoints = allAttempts.stream().mapToInt(UserProgress::getPointsEarned).sum();
        double successRate = totalAttempts > 0 ? (correctAttempts * 100.0 / totalAttempts) : 0.0;

        List<UserProgress> recent = userProgressRepository.findRecentByUserId(
                child.getId(), PageRequest.of(0, RECENT_ATTEMPTS_LIMIT));

        List<ParentDashboardDTO.AttemptInfo> recentAttempts = recent.stream()
                .map(this::toAttemptInfo)
                .collect(Collectors.toList());

        List<Classroom> classrooms = classroomRepository.findActiveByStudentId(child.getId());
        List<ParentDashboardDTO.ClassroomProgress> classroomProgress = classrooms.stream()
                .map(c -> buildClassroomProgress(c, child, allAttempts))
                .collect(Collectors.toList());

        return ParentDashboardDTO.ChildDetail.builder()
                .childId(child.getId())
                .childName(child.getName())
                .childEmail(child.getEmail())
                .totalAttempts(totalAttempts)
                .correctAttempts(correctAttempts)
                .distinctExercisesCompleted(distinctExercises)
                .totalPoints(totalPoints)
                .successRate(round(successRate))
                .lastActivityAt(allAttempts.isEmpty() ? null : allAttempts.get(0).getSubmittedAt())
                .recentAttempts(recentAttempts)
                .classroomProgress(classroomProgress)
                .build();
    }

    private ParentDashboardDTO.ChildSummary buildChildSummary(User child) {
        List<UserProgress> attempts = userProgressRepository.findByUserIdOrderBySubmittedAtDesc(child.getId());

        long totalAttempts = attempts.size();
        long correctAttempts = attempts.stream().filter(UserProgress::getCorrect).count();
        long distinctExercises = attempts.stream()
                .map(p -> p.getExercise().getId())
                .distinct()
                .count();
        int totalPoints = attempts.stream().mapToInt(UserProgress::getPointsEarned).sum();
        double successRate = totalAttempts > 0 ? (correctAttempts * 100.0 / totalAttempts) : 0.0;

        UserProgress last = attempts.isEmpty() ? null : attempts.get(0);

        List<Classroom> classrooms = classroomRepository.findActiveByStudentId(child.getId());
        List<String> classroomNames = classrooms.stream()
                .map(Classroom::getName)
                .collect(Collectors.toList());

        return ParentDashboardDTO.ChildSummary.builder()
                .childId(child.getId())
                .childName(child.getName())
                .childEmail(child.getEmail())
                .totalAttempts(totalAttempts)
                .correctAttempts(correctAttempts)
                .distinctExercisesCompleted(distinctExercises)
                .totalPoints(totalPoints)
                .successRate(round(successRate))
                .lastActivityAt(last != null ? last.getSubmittedAt() : null)
                .lastExerciseTitle(last != null ? extractExerciseTitle(last) : null)
                .classroomNames(classroomNames)
                .build();
    }

    private ParentDashboardDTO.ClassroomProgress buildClassroomProgress(
            Classroom classroom, User child, List<UserProgress> allAttempts) {

        int assignedQuizzes = classroom.getAssignedQuizzes().size();

        long completedQuizzes = classroom.getAssignedQuizzes().stream()
                .filter(q -> {
                    if (q.getExercises() == null || q.getExercises().isEmpty()) return false;
                    return q.getExercises().stream().allMatch(ex ->
                            allAttempts.stream().anyMatch(a ->
                                    a.getExercise().getId().equals(ex.getId()) && a.getCorrect()));
                })
                .count();

        int pointsInClassroom = allAttempts.stream()
                .filter(a -> classroom.getAssignedQuizzes().stream()
                        .flatMap(q -> q.getExercises().stream())
                        .anyMatch(ex -> ex.getId().equals(a.getExercise().getId())))
                .mapToInt(UserProgress::getPointsEarned)
                .sum();

        return ParentDashboardDTO.ClassroomProgress.builder()
                .classroomId(classroom.getId())
                .classroomName(classroom.getName())
                .assignedQuizzes(assignedQuizzes)
                .completedQuizzes((int) completedQuizzes)
                .totalPointsInClassroom(pointsInClassroom)
                .build();
    }

    private ParentDashboardDTO.AttemptInfo toAttemptInfo(UserProgress p) {
        return ParentDashboardDTO.AttemptInfo.builder()
                .attemptId(p.getId())
                .exerciseId(p.getExercise().getId())
                .exerciseTitle(extractExerciseTitle(p))
                .correct(p.getCorrect())
                .attemptNumber(p.getAttemptNumber())
                .pointsEarned(p.getPointsEarned())
                .submittedAt(p.getSubmittedAt())
                .build();
    }

    private String extractExerciseTitle(UserProgress p) {
        if (p.getExercise() == null) return "Exercițiu necunoscut";
        String quizTitle = p.getExercise().getQuiz() != null
                ? p.getExercise().getQuiz().getTitle()
                : "?";
        return quizTitle + " — Exercițiu #" + p.getExercise().getId();
    }

    private void checkParent(User user) {
        if (user.getRole() != UserRole.PARINTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Doar părinții au acces la această resursă");
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}