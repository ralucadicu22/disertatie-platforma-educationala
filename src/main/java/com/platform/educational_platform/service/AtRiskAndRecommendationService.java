package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.AtRiskDTO;

import com.platform.educational_platform.dto.RecommendationDTO;
import com.platform.educational_platform.model.QuizAttempt;
import com.platform.educational_platform.repository.QuizAttemptRepository;
import com.platform.educational_platform.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AtRiskAndRecommendationService {

    private final UserProgressRepository userProgressRepository;
    private final QuizAttemptRepository  quizAttemptRepository;

    private static final int    INACTIVE_DAYS_HIGH   = 14;
    private static final int    INACTIVE_DAYS_MEDIUM = 7;
    private static final double SUCCESS_RATE_LOW     = 0.40;
    private static final double SUCCESS_RATE_MEDIUM  = 0.60;
    private static final int    MIN_ATTEMPTS         = 3;

    public List<AtRiskDTO.AtRiskStudent> getAtRiskStudents() {

        Map<Long, StudentRaw> studentMap = new HashMap<>();

        for (Object[] row : userProgressRepository.findLastActivityPerStudent()) {
            Long          userId       = toLong(row[0]);
            String        userName     = (String) row[1];
            String        userEmail    = (String) row[2];
            LocalDateTime lastActivity = (LocalDateTime) row[3];
            studentMap.put(userId, new StudentRaw(userId, userName, userEmail, lastActivity));
        }

        for (Object[] row : userProgressRepository.findSuccessRatePerStudent()) {
            Long userId       = toLong(row[0]);
            long total        = toLong(row[1]);
            long correct      = toLong(row[2]);
            double successRate = total > 0 ? (double) correct / total : 0.0;

            StudentRaw sr = studentMap.get(userId);
            if (sr != null) {
                sr.totalAttempts = (int) total;
                sr.successRate   = successRate;
            }
        }

        Map<Long, Integer> abandonedMap = new HashMap<>();
        for (Object[] row : quizAttemptRepository.findAbandonedQuizzesPerStudent()) {
            abandonedMap.put(toLong(row[0]), (int)(long) toLong(row[1]));
        }

        List<AtRiskDTO.AtRiskStudent> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (StudentRaw sr : studentMap.values()) {
            List<String> flags    = new ArrayList<>();
            String       riskLevel = null;

            long daysSinceActivity = sr.lastActivity != null
                    ? ChronoUnit.DAYS.between(sr.lastActivity, now) : 999;

            if (daysSinceActivity >= INACTIVE_DAYS_HIGH) {
                flags.add("Inactiv de " + daysSinceActivity + " zile");
            } else if (daysSinceActivity >= INACTIVE_DAYS_MEDIUM) {
                flags.add("Inactiv de " + daysSinceActivity + " zile");
            }

            if (sr.totalAttempts >= MIN_ATTEMPTS && sr.successRate < SUCCESS_RATE_LOW) {
                flags.add("Rată succes foarte scăzută: " + Math.round(sr.successRate * 100) + "%");
            } else if (sr.totalAttempts >= MIN_ATTEMPTS && sr.successRate < SUCCESS_RATE_MEDIUM) {
                flags.add("Rată succes scăzută: " + Math.round(sr.successRate * 100) + "%");
            }

            int abandoned = abandonedMap.getOrDefault(sr.userId, 0);
            if (abandoned > 0) {
                flags.add("Quiz-uri neterminate: " + abandoned);
            }

            if (flags.isEmpty()) continue;

            boolean highInactive  = daysSinceActivity >= INACTIVE_DAYS_HIGH;
            boolean mediumInactive = daysSinceActivity >= INACTIVE_DAYS_MEDIUM;
            boolean lowSuccess    = sr.totalAttempts >= MIN_ATTEMPTS && sr.successRate < SUCCESS_RATE_LOW;
            boolean mediumSuccess = sr.totalAttempts >= MIN_ATTEMPTS && sr.successRate < SUCCESS_RATE_MEDIUM;
            boolean hasAbandoned  = abandoned > 0;

            if ((lowSuccess && highInactive) || (lowSuccess && hasAbandoned) || (lowSuccess && mediumInactive)) {
                riskLevel = "HIGH";
            } else if (lowSuccess || mediumSuccess || hasAbandoned) {
                riskLevel = "MEDIUM";
            } else {
                riskLevel = "LOW";
            }

            result.add(new AtRiskDTO.AtRiskStudent(
                    sr.userId,
                    sr.userName,
                    sr.userEmail,
                    flags,
                    riskLevel,
                    sr.lastActivity,
                    Math.round(sr.successRate * 1000.0) / 10.0,
                    sr.totalAttempts,
                    abandoned
            ));
        }

        result.sort(Comparator.comparingInt(s -> {
            return switch (s.getRiskLevel()) {
                case "HIGH"   -> 0;
                case "MEDIUM" -> 1;
                default       -> 2;
            };
        }));

        return result;
    }

    public RecommendationDTO.StudentRecommendations getRecommendationsForStudent(Long studentId) {

        List<RecommendationDTO.Recommendation> recs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        List<Object[]> lastActivityRows = userProgressRepository.findLastActivityPerStudent();
        String userName = "Student";

        for (Object[] row : lastActivityRows) {
            if (!toLong(row[0]).equals(studentId)) continue;
            userName = (String) row[1];
            LocalDateTime lastActivity = (LocalDateTime) row[3];
            if (lastActivity != null) {
                long days = ChronoUnit.DAYS.between(lastActivity, now);
                if (days >= INACTIVE_DAYS_HIGH) {
                    recs.add(new RecommendationDTO.Recommendation(
                            RecommendationDTO.RecommendationType.WARNING,
                            "Pauză lungă detectată",
                            "Nu ai mai rezolvat exerciții de " + days + " zile. " +
                                    "Recomandăm să reiei activitatea pentru a nu uita materia.",
                            "Reia activitatea",
                            null
                    ));
                } else if (days >= INACTIVE_DAYS_MEDIUM) {
                    recs.add(new RecommendationDTO.Recommendation(
                            RecommendationDTO.RecommendationType.INFO,
                            "Ai o pauză de " + days + " zile",
                            "Încearcă să rezolvi cel puțin un exercițiu astăzi pentru a menține ritmul.",
                            null,
                            null
                    ));
                }
            }
            break;
        }

        List<Object[]> hardExercises = userProgressRepository.findHardExercisesForStudent(studentId);
        if (!hardExercises.isEmpty()) {
            Object[] hardest = hardExercises.get(0);
            Long exerciseId  = toLong(hardest[1]);
            long attempts    = toLong(hardest[2]);
            recs.add(new RecommendationDTO.Recommendation(
                    RecommendationDTO.RecommendationType.ACTION,
                    "Exercițiu dificil identificat",
                    "Ai încercat Exercițiul #" + exerciseId + " de " + attempts +
                            " ori. Încearcă să ceri ajutorul profesorului sau să recitești indiciile.",
                    "Vezi indicii",
                    exerciseId
            ));
        }

        List<QuizAttempt> abandoned = quizAttemptRepository.findAbandonedQuizzesForStudent(studentId);
        if (!abandoned.isEmpty()) {
            QuizAttempt latest = abandoned.get(0);
            recs.add(new RecommendationDTO.Recommendation(
                    RecommendationDTO.RecommendationType.ACTION,
                    "Quiz neterminat",
                    "Ai un quiz început și neterminat: \"" +
                            (latest.getQuiz() != null ? latest.getQuiz().getTitle() : "Quiz #" + latest.getId()) +
                            "\". Continuă de unde ai rămas.",
                    "Continuă quiz-ul",
                    latest.getQuiz() != null ? latest.getQuiz().getId() : null
            ));
        }

        List<Object[]> successRows = userProgressRepository.findSuccessRatePerStudent();
        for (Object[] row : successRows) {
            if (!toLong(row[0]).equals(studentId)) continue;
            long total   = toLong(row[1]);
            long correct = toLong(row[2]);
            if (total >= MIN_ATTEMPTS) {
                double rate = (double) correct / total;
                if (rate < SUCCESS_RATE_LOW) {
                    recs.add(new RecommendationDTO.Recommendation(
                            RecommendationDTO.RecommendationType.WARNING,
                            "Rată de succes scăzută",
                            "Rata ta de succes este de " + Math.round(rate * 100) +
                                    "%. Încearcă să parcurgi materialul teoretic înainte de a rezolva exercițiile.",
                            null,
                            null
                    ));
                } else if (rate >= 0.85) {
                    recs.add(new RecommendationDTO.Recommendation(
                            RecommendationDTO.RecommendationType.INFO,
                            "Performanță excelentă! 🎉",
                            "Rata ta de succes este de " + Math.round(rate * 100) +
                                    "%. Continuă tot așa!",
                            null,
                            null
                    ));
                }
            }
            break;
        }

        if (recs.isEmpty()) {
            recs.add(new RecommendationDTO.Recommendation(
                    RecommendationDTO.RecommendationType.INFO,
                    "Totul arată bine!",
                    "Nu avem recomandări speciale pentru tine momentan. Continuă să rezolvi exerciții!",
                    null,
                    null
            ));
        }

        return new RecommendationDTO.StudentRecommendations(studentId, userName, recs);
    }

    private static class StudentRaw {
        Long          userId;
        String        userName;
        String        userEmail;
        LocalDateTime lastActivity;
        int           totalAttempts = 0;
        double        successRate   = 0.0;

        StudentRaw(Long userId, String userName, String userEmail, LocalDateTime lastActivity) {
            this.userId       = userId;
            this.userName     = userName;
            this.userEmail    = userEmail;
            this.lastActivity = lastActivity;
        }
    }

    private Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Long)    return (Long) o;
        if (o instanceof Integer) return ((Integer) o).longValue();
        if (o instanceof Number)  return ((Number) o).longValue();
        return 0L;
    }
}