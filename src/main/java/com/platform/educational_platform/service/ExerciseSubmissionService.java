package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.BadgeDTO;
import com.platform.educational_platform.dto.exercise.submission.*;
import com.platform.educational_platform.model.Exercise;
import com.platform.educational_platform.model.ExerciseVersion;
import com.platform.educational_platform.model.QuizAttempt;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.QuizAttemptStatus;
import com.platform.educational_platform.repository.ExerciseRepository;
import com.platform.educational_platform.repository.QuizAttemptRepository;
import com.platform.educational_platform.repository.UserProgressRepository;
import com.platform.educational_platform.repository.UserRepository;
import com.platform.educational_platform.validation.ExerciseValidator;
import com.platform.educational_platform.validation.ExerciseValidatorRegistry;
import com.platform.educational_platform.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExerciseSubmissionService {

    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserProgressService userProgressService;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ExerciseValidatorRegistry validatorRegistry;
    private final ObjectMapper objectMapper;
    private final BadgeService badgeService;

    private JsonNode getPublishedConfig(Exercise exercise) {
        ExerciseVersion published = exercise.getCurrentVersion();
        if (published == null) {
            throw new RuntimeException("Exercitiul nu are o versiune publicata");
        }
        return objectMapper.readTree(published.getConfig());
    }

    public CheckElementResponse checkElement(CheckElementRequest request) {
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercitiul nu exista"));

        JsonNode config = getPublishedConfig(exercise);
        ExerciseValidator validator = validatorRegistry.getValidator(exercise.getType());
        boolean correct = validator.isElementCorrect(config, request.getElementId());

        return CheckElementResponse.builder()
                .elementId(request.getElementId())
                .correct(correct)
                .build();
    }

    @Transactional
    public SubmitAnswerResponse submit(SubmitAnswerRequest request, String userEmail) {
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercitiul nu exista"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilizator inexistent"));

        JsonNode config = getPublishedConfig(exercise);

        Integer maxAttempts = exercise.getMaxAttempts();
        int attemptsUsed = userProgressRepository.countByUserIdAndExerciseId(user.getId(), exercise.getId());

        boolean unlimited = maxAttempts == null || maxAttempts == 0;
        if (!unlimited && attemptsUsed >= maxAttempts) {
            throw new RuntimeException("Numarul maxim de incercari a fost atins");
        }

        ExerciseValidator validator = validatorRegistry.getValidator(exercise.getType());
        ValidationResult result = validator.validate(config, request.getAnswer());

        int attemptNumber = attemptsUsed + 1;
        int pointsEarned = result.isCorrect() ? exercise.getPoints() : 0;

        QuizAttempt activeAttempt = null;
        if (exercise.getQuiz() != null) {
            Optional<QuizAttempt> attemptOpt = quizAttemptRepository
                    .findFirstByUserIdAndQuizIdAndStatusOrderByAttemptNumberDesc(
                            user.getId(), exercise.getQuiz().getId(), QuizAttemptStatus.IN_PROGRESS);
            activeAttempt = attemptOpt.orElse(null);
        }

        userProgressService.recordAttempt(user, exercise, result.isCorrect(), attemptNumber, pointsEarned, activeAttempt);
        List<BadgeDTO> newBadges = badgeService.checkAndAwardBadges(
                user, request.getExerciseId(), result.isCorrect(), result.getScore()
        );

        Integer attemptsRemaining = unlimited ? null :
                Math.max(0, maxAttempts - attemptNumber);

        return SubmitAnswerResponse.builder()
                .correct(result.isCorrect())
                .score(result.getScore())
                .details(result.getDetails())
                .maxAttempts(maxAttempts)
                .attemptsRemaining(attemptsRemaining)
                .newBadges(newBadges)
                .build();
    }

    public CheckPlacementResponse checkPlacement(CheckPlacementRequest request) {
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercitiul nu exista"));

        JsonNode config = getPublishedConfig(exercise);
        ExerciseValidator validator = validatorRegistry.getValidator(exercise.getType());
        boolean correct = validator.isPlacementCorrect(config, request.getDraggableId(), request.getTargetId());

        return CheckPlacementResponse.builder()
                .draggableId(request.getDraggableId())
                .targetId(request.getTargetId())
                .correct(correct)
                .build();
    }
}