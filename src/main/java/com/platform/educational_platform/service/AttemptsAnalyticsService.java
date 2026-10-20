package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.AttemptsAnalyticsDto.*;
import com.platform.educational_platform.model.Exercise;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.repository.ExerciseRepository;
import com.platform.educational_platform.repository.UserProgressRepository;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttemptsAnalyticsService {

    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    public StudentDashboard getStudentDashboard(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Utilizator negăsit: " + studentId));

        List<Object[]> studentRows = userProgressRepository.findAttemptsPerExerciseForStudent(studentId);

        List<ExerciseAttemptsWithClassAvg> exercises = studentRows.stream()
                .map(this::buildExerciseRow)
                .collect(Collectors.toList());

        double overallAvg = exercises.isEmpty() ? 0.0
                : Math.round(exercises.stream()
                .mapToInt(ExerciseAttemptsWithClassAvg::getAttemptsUsed)
                .average()
                .orElse(0.0) * 10.0) / 10.0;

        return StudentDashboard.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .exercises(exercises)
                .overallAvgAttempts(overallAvg)
                .build();
    }

    public List<ClassAverageAttempts> getClassAverage() {
        List<Object[]> rows = userProgressRepository.findAttemptsStatsPerExercise();
        return rows.stream()
                .map(this::buildClassAverage)
                .collect(Collectors.toList());
    }

    public AdvancedAnalyticsResponse getAdvancedAnalytics() {

        List<Object[]> scoreRows = userProgressRepository.findScoreStatsPerExercise();
        List<ExerciseScoreRow> exerciseScores = scoreRows.stream()
                .map(row -> new ExerciseScoreRow(
                        toLong(row[0]),
                        toDouble(row[1]),
                        toDouble(row[2]),
                        toDouble(row[3]),
                        toLong(row[4]),
                        toLong(row[5]),
                        toLong(row[6])
                ))
                .collect(Collectors.toList());
        List<Object[]> patternRows = userProgressRepository.findErrorPatternPerStudent();
        List<StudentErrorPattern> studentPatterns = patternRows.stream()
                .map(row -> new StudentErrorPattern(
                        toLong(row[0]),
                        (String) row[1],
                        toLong(row[2]),
                        toLong(row[3]),
                        toLong(row[4]),
                        toLong(row[5]),
                        toDouble(row[6])
                ))
                .collect(Collectors.toList());

        return new AdvancedAnalyticsResponse(exerciseScores, studentPatterns);
    }

    private ExerciseAttemptsWithClassAvg buildExerciseRow(Object[] row) {
        Long exerciseId  = ((Number) row[0]).longValue();
        int attemptsUsed = ((Number) row[1]).intValue();
        boolean solved   = (Boolean) row[2];
        Double bestScore = row[3] != null ? ((Number) row[3]).doubleValue() : null;

        Exercise exercise   = exerciseRepository.findById(exerciseId).orElse(null);
        Integer maxAttempts = exercise != null ? exercise.getMaxAttempts() : null;

        boolean showGroupAverage = exercise != null && isShowGroupAverage(exercise);
        Double classAvg = null;
        if (showGroupAverage) {
            Double raw = userProgressRepository.getAvgAttemptsPerUserForExercise(exerciseId);
            classAvg = raw != null ? Math.round(raw * 10.0) / 10.0 : null;
        }

        return ExerciseAttemptsWithClassAvg.builder()
                .exerciseId(exerciseId)
                .exerciseTitle("Exercițiul #" + exerciseId)
                .attemptsUsed(attemptsUsed)
                .maxAttempts(maxAttempts)
                .solved(solved)
                .bestScore(bestScore)
                .classAvgAttempts(classAvg)
                .showGroupAverage(showGroupAverage)
                .build();
    }

    private ClassAverageAttempts buildClassAverage(Object[] row) {
        Long exerciseId    = ((Number) row[0]).longValue();
        long totalStudents = ((Number) row[1]).longValue();
        Double avgAttempts = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;

        Exercise exercise   = exerciseRepository.findById(exerciseId).orElse(null);
        Integer maxAttempts = exercise != null ? exercise.getMaxAttempts() : null;

        return ClassAverageAttempts.builder()
                .exerciseId(exerciseId)
                .exerciseTitle("Exercițiul #" + exerciseId)
                .avgAttemptsPerStudent(Math.round(avgAttempts * 10.0) / 10.0)
                .totalStudents((int) totalStudents)
                .maxAttempts(maxAttempts)
                .build();
    }

    private boolean isShowGroupAverage(Exercise exercise) {
        if (exercise.getVersions() == null || exercise.getVersions().isEmpty()) return false;
        try {
            String config = exercise.getVersions()
                    .get(exercise.getVersions().size() - 1)
                    .getConfig();
            return config != null && config.contains("\"showGroupAverage\":true");
        } catch (Exception e) {
            return false;
        }
    }

    private Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Long)    return (Long) o;
        if (o instanceof Integer) return ((Integer) o).longValue();
        if (o instanceof Number)  return ((Number) o).longValue();
        return 0L;
    }

    private Double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Double)  return (Double) o;
        if (o instanceof Float)   return ((Float) o).doubleValue();
        if (o instanceof Number)  return ((Number) o).doubleValue();
        return 0.0;
    }
}