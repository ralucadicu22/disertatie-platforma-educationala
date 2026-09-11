package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.UserSummaryDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<UserSummaryDTO> getAll(
            @RequestParam(required = false) UserRole role) {
        List<User> users = role != null
                ? userRepository.findByRole(role)
                : userRepository.findAll();
        return users.stream().map(this::toDTO).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserSummaryDTO> getOne(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(toDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserSummaryDTO> updateRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizator negăsit"));
        user.setRole(req.getRole());
        return ResponseEntity.ok(toDTO(userRepository.save(user)));
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<UserSummaryDTO> toggleActive(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizator negăsit"));
        user.setActive(user.getActive() == null || !user.getActive());
        return ResponseEntity.ok(toDTO(userRepository.save(user)));
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long id,
            @RequestBody ResetPasswordRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizator negăsit"));
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id))
            return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private UserSummaryDTO toDTO(User u) {
        return UserSummaryDTO.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole())
                .active(u.getActive() == null || u.getActive())
                .createdAt(u.getCreatedAt())
                .lastActivity(userRepository.findLastActivityByUserId(u.getId()))
                .build();
    }

    @Data
    public static class UpdateRoleRequest {
        private UserRole role;
    }

    @Data
    public static class ResetPasswordRequest {
        private String newPassword;
    }
}