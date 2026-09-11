package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.BadgeDTO;
import com.platform.educational_platform.model.Badge;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.BadgeCriteria;
import com.platform.educational_platform.repository.BadgeRepository;
import com.platform.educational_platform.repository.UserProgressRepository;
import com.platform.educational_platform.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserProgressRepository userProgressRepository;
    private final QuizRepository quizRepository;

    private static final List<BadgeDefinition> BADGE_DEFINITIONS = List.of(
            new BadgeDefinition(BadgeCriteria.FIRST_EXERCISE,   "🌟", "Primul pas",       "Ai rezolvat primul tău exercițiu!"),
            new BadgeDefinition(BadgeCriteria.THREE_IN_A_ROW,   "🔥", "În serie",          "3 exerciții corecte la rând!"),
            new BadgeDefinition(BadgeCriteria.PERFECT_SCORE,    "💯", "Perfecționist",     "Scor perfect la un exercițiu!"),
            new BadgeDefinition(BadgeCriteria.TEN_EXERCISES,    "🏆", "Harnic",            "Ai rezolvat 10 exerciții!"),
            new BadgeDefinition(BadgeCriteria.QUIZ_COMPLETE,    "✅", "Quiz complet",      "Ai terminat toate exercițiile unui quiz!"),
            new BadgeDefinition(BadgeCriteria.FIVE_DAYS_STREAK, "📅", "Constant",          "5 zile consecutive de activitate!")
    );

    public List<BadgeDTO> checkAndAwardBadges(User user, Long exerciseId, boolean correct, double score) {
        List<BadgeDTO> newBadges = new ArrayList<>();

        for (BadgeDefinition def : BADGE_DEFINITIONS) {
            if (badgeRepository.existsByUserIdAndCriteria(user.getId(), def.criteria)) continue;

            if (shouldAward(def.criteria, user, exerciseId, correct, score)) {
                Badge badge = Badge.builder()
                        .user(user)
                        .criteria(def.criteria)
                        .name(def.name)
                        .icon(def.icon)
                        .description(def.description)
                        .build();
                badgeRepository.save(badge);
                newBadges.add(toDTO(badge));
                log.info("Badge acordat: {} → {}", user.getEmail(), def.name);
            }
        }

        return newBadges;
    }

    private boolean shouldAward(BadgeCriteria criteria, User user, Long exerciseId,
                                boolean correct, double score) {
        return switch (criteria) {
            case FIRST_EXERCISE -> correct &&
                    userProgressRepository.countCompletedExercisesByUser(user.getId()) >= 1;

            case THREE_IN_A_ROW -> {
                var recent = userProgressRepository
                        .findByUserIdOrderBySubmittedAtDesc(user.getId());
                if (recent.size() < 3) yield false;
                yield recent.subList(0, 3).stream().allMatch(p -> p.getCorrect());
            }

            case PERFECT_SCORE -> correct && score >= 1.0;

            case TEN_EXERCISES ->
                    userProgressRepository.countCompletedExercisesByUser(user.getId()) >= 10;

            case QUIZ_COMPLETE -> {
                try {
                    var exercise = userProgressRepository
                            .findByUserIdOrderBySubmittedAtDesc(user.getId())
                            .stream()
                            .filter(p -> p.getExercise().getId().equals(exerciseId))
                            .findFirst();
                    if (exercise.isEmpty()) yield false;

                    Long quizId = exercise.get().getExercise().getQuiz().getId();
                    var quiz = quizRepository.findById(quizId);
                    if (quiz.isEmpty()) yield false;

                    long totalExercises = quiz.get().getExercises().size();
                    long completedExercises = userProgressRepository
                            .findByUserIdOrderBySubmittedAtDesc(user.getId())
                            .stream()
                            .filter(p -> p.getExercise().getQuiz().getId().equals(quizId))
                            .filter(p -> p.getCorrect())
                            .map(p -> p.getExercise().getId())
                            .distinct()
                            .count();

                    yield totalExercises > 0 && completedExercises >= totalExercises;
                } catch (Exception e) {
                    yield false;
                }
            }

            case FIVE_DAYS_STREAK -> {
                var history = userProgressRepository
                        .findByUserIdOrderBySubmittedAtDesc(user.getId());
                if (history.isEmpty()) yield false;

                var days = history.stream()
                        .map(p -> p.getSubmittedAt().toLocalDate())
                        .distinct()
                        .sorted()
                        .toList();

                if (days.size() < 5) yield false;

                int streak = 1;
                for (int i = days.size() - 1; i > 0; i--) {
                    if (days.get(i).minusDays(1).equals(days.get(i - 1))) {
                        streak++;
                        if (streak >= 5) yield true;
                    } else {
                        streak = 1;
                    }
                }
                yield false;
            }
        };
    }

    public List<BadgeDTO> getUserBadges(Long userId) {
        return badgeRepository.findByUserIdOrderByEarnedAtDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private BadgeDTO toDTO(Badge b) {
        return BadgeDTO.builder()
                .id(b.getId())
                .name(b.getName())
                .icon(b.getIcon())
                .description(b.getDescription())
                .criteria(b.getCriteria().name())
                .earnedAt(b.getEarnedAt())
                .build();
    }

    private record BadgeDefinition(BadgeCriteria criteria, String icon, String name, String description) {}
}