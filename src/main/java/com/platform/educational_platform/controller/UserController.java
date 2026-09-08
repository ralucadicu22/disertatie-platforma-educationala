package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.UserSummaryDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping
    public List<UserSummaryDTO> getAll(
            @RequestParam(required = false) UserRole role) {
        List<User> users = role != null
                ? userRepository.findByRole(role)
                : userRepository.findAll();
        return users.stream()
                .map(u -> UserSummaryDTO.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .role(u.getRole())
                        .build())
                .toList();
    }
}