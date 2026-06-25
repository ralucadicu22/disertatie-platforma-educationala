package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.UserProgressDTO;
import com.platform.educational_platform.dto.UserStatsDTO;
import com.platform.educational_platform.model.Exercise;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.UserProgress;
import com.platform.educational_platform.repository.UserProgressRepository;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProgressService {

    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;

    public void recordAttempt(User user, Exercise exercise,
                              boolean correct, int attemptNumber, int pointsEarned) {
        UserProgress progress = UserProgress.builder()
                .user(user)
                .exercise(exercise)
                .correct(correct)
                .attemptNumber(attemptNumber)
                .pointsEarned(pointsEarned)
                .build();
        userProgressRepository.save(progress);
    }

    public List<UserProgressDTO> getUserHistory(Long userId) {
        return userProgressRepository.findByUserIdOrderBySubmittedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserProgressDTO> getExerciseAttempts(Long exerciseId) {
        return userProgressRepository.findByExerciseIdOrderBySubmittedAtDesc(exerciseId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserStatsDTO getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizator negasit"));

        Long completed = userProgressRepository.countCompletedExercisesByUser(userId);
        Long totalPoints = userProgressRepository.sumPointsByUser(userId);
        Long totalSubmissions = (long) userProgressRepository
                .findByUserIdOrderBySubmittedAtDesc(userId).size();

        return UserStatsDTO.builder()
                .userId(user.getId())
                .userName(user.getName())
                .completedExercises(completed)
                .totalSubmissions(totalSubmissions)
                .totalPoints(totalPoints)
                .build();
    }

    public List<UserStatsDTO> getAllElevStats() {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole().name().equals("ELEV"))
                .map(u -> getUserStats(u.getId()))
                .collect(Collectors.toList());
    }

    private UserProgressDTO toDTO(UserProgress up) {
        return UserProgressDTO.builder()
                .id(up.getId())
                .userId(up.getUser().getId())
                .userName(up.getUser().getName())
                .exerciseId(up.getExercise().getId())
                .correct(up.getCorrect())
                .attemptNumber(up.getAttemptNumber())
                .pointsEarned(up.getPointsEarned())
                .submittedAt(up.getSubmittedAt())
                .build();
    }
}