package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.UserProgressDTO;
import com.platform.educational_platform.dto.UserStatsDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.repository.UserRepository;
import com.platform.educational_platform.service.UserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserProgressController {

    private final UserProgressService userProgressService;
    private final UserRepository userRepository;


    @GetMapping("/me")
    public ResponseEntity<List<UserProgressDTO>> getMyHistory(
            Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(userProgressService.getUserHistory(user.getId()));
    }


    @GetMapping("/me/stats")
    public ResponseEntity<UserStatsDTO> getMyStats(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(userProgressService.getUserStats(user.getId()));
    }


    @GetMapping("/exercise/{exerciseId}")
    public ResponseEntity<List<UserProgressDTO>> getExerciseAttempts(
            @PathVariable Long exerciseId) {
        return ResponseEntity.ok(userProgressService.getExerciseAttempts(exerciseId));
    }


    @GetMapping("/teacher/all-students")
    public ResponseEntity<List<UserStatsDTO>> getAllStudentsStats() {
        return ResponseEntity.ok(userProgressService.getAllElevStats());
    }
}