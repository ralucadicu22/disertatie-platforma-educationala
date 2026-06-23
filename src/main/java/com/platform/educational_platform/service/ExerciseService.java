package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.*;
import com.platform.educational_platform.dto.config.FindAndCountConfig;
import com.platform.educational_platform.dto.exercise.ExerciseDTO;
import com.platform.educational_platform.dto.exercise.FeedbackResponse;
import com.platform.educational_platform.dto.exercise.TargetResult;
import com.platform.educational_platform.model.Exercise;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.repository.ExerciseRepository;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
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
        return ExerciseDTO.builder()
                .id(e.getId())
                .type(e.getType())
                .questionText(e.getQuestionText())
                .imageUrl(e.getImageUrl())
                .config(e.getConfig())
                .hint(e.getHint())
                .points(e.getPoints())
                .difficulty(e.getDifficulty())
                .orderIndex(e.getOrderIndex())
                .maxAttempts(e.getMaxAttempts())
                .build();
    }

    public FeedbackResponse checkAnswer(AnswerRequest request, String userEmail) {
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercitiul nu a fost gasit"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilizator negasit"));

        FeedbackResponse feedback = switch (exercise.getType()) {
            case "FIND_AND_COUNT" -> checkFindAndCount(exercise, request);
            default -> checkSimple(exercise, request);
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

    private FeedbackResponse checkFindAndCount(Exercise exercise, AnswerRequest request) {
        try {
            FindAndCountConfig config = objectMapper.readValue(
                    exercise.getConfig(), FindAndCountConfig.class);

            int maxAttempts = exercise.getMaxAttempts();
            int currentAttempt = request.getAttemptNumber();

            boolean isCorrectThisTime = false;

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

            boolean showHint = !allCorrect && !attemptsExhausted && currentAttempt < maxAttempts;
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
                    .hint(showHint ? exercise.getHint() : null)
                    .explanation(showExplanation ? exercise.getExplanation() : null)
                    .pointsEarned(allCorrect ? exercise.getPoints() : 0)
                    .showHint(showHint)
                    .showExplanation(showExplanation)
                    .targetResults(results)
                    .correctTargets(correctCount)
                    .totalTargets(total)
                    .attemptsExhausted(attemptsExhausted)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Eroare la procesarea exercitiului: " + e.getMessage());
        }
    }

    private FeedbackResponse checkSimple(Exercise exercise, AnswerRequest request) {
        boolean isCorrect = exercise.getCorrectAnswer() != null &&
                exercise.getCorrectAnswer().trim()
                        .equalsIgnoreCase(
                                request.getSimpleAnswer() != null
                                        ? request.getSimpleAnswer().trim()
                                        : "");

        boolean showHint = !isCorrect
                && exercise.getHint() != null
                && request.getAttemptNumber() >= 2;

        boolean showExplanation = isCorrect
                || (exercise.getMaxAttempts() > 0
                && request.getAttemptNumber() >= exercise.getMaxAttempts());

        return FeedbackResponse.builder()
                .correct(isCorrect)
                .message(isCorrect ? "Corect! Bravo!" : "Raspuns gresit. Mai incearca!")
                .hint(showHint ? exercise.getHint() : null)
                .explanation(showExplanation ? exercise.getExplanation() : null)
                .pointsEarned(isCorrect ? exercise.getPoints() : 0)
                .showHint(showHint)
                .showExplanation(showExplanation)
                .build();
    }
}