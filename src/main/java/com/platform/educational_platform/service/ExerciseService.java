package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.*;
import com.platform.educational_platform.dto.config.FindAndCountConfig;
import com.platform.educational_platform.dto.config.HintConfig;
import com.platform.educational_platform.dto.config.SimpleAnswerConfig;
import com.platform.educational_platform.dto.exercise.ExerciseDTO;
import com.platform.educational_platform.dto.exercise.FeedbackResponse;
import com.platform.educational_platform.dto.exercise.TargetResult;
import com.platform.educational_platform.model.Exercise;
import com.platform.educational_platform.model.ExerciseVersion;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.VersionStatus;
import com.platform.educational_platform.repository.ExerciseRepository;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ObjectMapper objectMapper;
    private final UserProgressService userProgressService;
    private final UserRepository userRepository;


    public List<ExerciseDTO> getExercisesByQuiz(Long quizId) {
        return exerciseRepository.findByQuizIdOrderByOrderIndexAsc(quizId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ExerciseDTO getExerciseById(Long id) {
        Exercise e = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercitiul nu a fost gasit"));
        return toDTO(e);
    }

    private ExerciseDTO toDTO(Exercise e) {
        ExerciseVersion active = e.getCurrentVersion();
        if (active == null || active.getStatus() != VersionStatus.PUBLISHED) {
            throw new RuntimeException("Exercitiul nu are o versiune publicata");
        }

        return ExerciseDTO.builder()
                .id(e.getId())
                .type(e.getType())
                .config(active.getConfig())
                .points(e.getPoints())
                .difficulty(e.getDifficulty())
                .orderIndex(e.getOrderIndex())
                .maxAttempts(e.getMaxAttempts())
                .build();
    }



    public FeedbackResponse checkAnswer(AnswerRequest request, String userEmail) {
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercitiul nu a fost gasit"));

        ExerciseVersion activeVersion = exercise.getCurrentVersion();
        if (activeVersion == null || activeVersion.getStatus() != VersionStatus.PUBLISHED) {
            throw new RuntimeException("Exercitiul nu este disponibil momentan");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilizator negasit"));

        FeedbackResponse feedback = switch (exercise.getType()) {
            case "FIND_AND_COUNT" -> checkFindAndCount(exercise, activeVersion, request);
            default -> checkSimple(exercise, activeVersion, request);
        };

        userProgressService.recordAttempt(
                user,
                exercise,
                feedback.isCorrect(),
                request.getAttemptNumber(),
                feedback.getPointsEarned()
        );

        return feedback;
    }

    private FeedbackResponse checkFindAndCount(Exercise exercise,
                                               ExerciseVersion version,
                                               AnswerRequest request) {
        FindAndCountConfig config = parseConfig(version.getConfig(), FindAndCountConfig.class);

        int maxAttempts = exercise.getMaxAttempts() != null ? exercise.getMaxAttempts() : 0;
        int currentAttempt = request.getAttemptNumber();

        List<TargetResult> results = new ArrayList<>();
        int correctCount = 0;

        for (FindAndCountConfig.Target target : config.getTargets()) {
            AnswerRequest.TargetAnswer given = request.getAnswers()
                    .stream()
                    .filter(a -> a.getTargetId().equals(target.getId()))
                    .findFirst()
                    .orElse(null);

            int givenValue = given != null ? given.getValue() : -1;
            boolean isCorrect = givenValue == target.getCorrect();
            if (isCorrect) correctCount++;

            results.add(TargetResult.builder()
                    .targetId(target.getId())
                    .label(target.getLabel())
                    .correct(isCorrect)
                    .givenValue(givenValue)
                    .build());
        }

        int total = config.getTargets().size();
        boolean allCorrect = correctCount == total;
        boolean attemptsExhausted = !allCorrect && maxAttempts > 0 && currentAttempt >= maxAttempts;

        if (attemptsExhausted) {
            for (TargetResult r : results) {
                config.getTargets().stream()
                        .filter(t -> t.getId().equals(r.getTargetId()))
                        .findFirst()
                        .ifPresent(t -> r.setCorrectValue(t.getCorrect()));
            }
        }

        String hintText = selectHint(config.getHints(), currentAttempt, allCorrect, attemptsExhausted);
        boolean showHint = hintText != null;
        boolean showExplanation = allCorrect || attemptsExhausted;

        String message;
        if (allCorrect) {
            message = "Felicitari! Ai numarat corect tot!";
        } else if (attemptsExhausted) {
            message = "Ai epuizat incercarile. Iata raspunsurile corecte!";
        } else {
            message = correctCount + " din " + total + " corecte. Mai incearca!";
        }

        return FeedbackResponse.builder()
                .correct(allCorrect)
                .message(message)
                .hint(hintText)
                .explanation(showExplanation ? config.getExplanation() : null)
                .pointsEarned(allCorrect ? exercise.getPoints() : 0)
                .showHint(showHint)
                .showExplanation(showExplanation)
                .targetResults(results)
                .correctTargets(correctCount)
                .totalTargets(total)
                .attemptsExhausted(attemptsExhausted)
                .build();
    }

    private FeedbackResponse checkSimple(Exercise exercise,
                                         ExerciseVersion version,
                                         AnswerRequest request) {
        SimpleAnswerConfig config = parseConfig(version.getConfig(), SimpleAnswerConfig.class);

        String userAnswer = request.getSimpleAnswer() != null
                ? request.getSimpleAnswer().trim()
                : "";

        boolean isCorrect = matchesAnswer(userAnswer, config);

        int maxAttempts = exercise.getMaxAttempts() != null ? exercise.getMaxAttempts() : 0;
        int currentAttempt = request.getAttemptNumber();
        boolean attemptsExhausted = !isCorrect && maxAttempts > 0 && currentAttempt >= maxAttempts;

        String hintText = selectHint(config.getHints(), currentAttempt, isCorrect, attemptsExhausted);
        boolean showHint = hintText != null;
        boolean showExplanation = isCorrect || attemptsExhausted;

        String message;
        if (isCorrect) {
            message = "Corect! Bravo!";
        } else if (attemptsExhausted) {
            message = "Ai epuizat incercarile. Raspunsul corect era: " + config.getCorrectAnswer();
        } else {
            message = "Raspuns gresit. Mai incearca!";
        }

        return FeedbackResponse.builder()
                .correct(isCorrect)
                .message(message)
                .hint(hintText)
                .explanation(showExplanation ? config.getExplanation() : null)
                .pointsEarned(isCorrect ? exercise.getPoints() : 0)
                .showHint(showHint)
                .showExplanation(showExplanation)
                .build();
    }


    private String selectHint(List<HintConfig> hints,
                              int currentAttempt,
                              boolean isCorrect,
                              boolean attemptsExhausted) {
        if (hints == null || hints.isEmpty()) return null;
        if (isCorrect || attemptsExhausted) return null;

        return hints.stream()
                .filter(h -> currentAttempt >= h.getAfterAttempt())
                .max(Comparator.comparingInt(HintConfig::getAfterAttempt))
                .map(HintConfig::getText)
                .orElse(null);
    }

    private boolean matchesAnswer(String userAnswer, SimpleAnswerConfig config) {
        if (config.getCorrectAnswer() != null
                && config.getCorrectAnswer().trim().equalsIgnoreCase(userAnswer)) {
            return true;
        }
        if (config.getAlternativeAnswers() != null) {
            return config.getAlternativeAnswers().stream()
                    .anyMatch(alt -> alt != null && alt.trim().equalsIgnoreCase(userAnswer));
        }
        return false;
    }

    private <T> T parseConfig(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Eroare la procesarea configuratiei exercitiului: " + e.getMessage());
        }
    }
}