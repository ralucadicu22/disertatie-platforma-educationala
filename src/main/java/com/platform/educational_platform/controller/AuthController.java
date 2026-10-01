package com.platform.educational_platform.controller;

import com.platform.educational_platform.config.JwtUtil;
import com.platform.educational_platform.dto.auth.AuthResponse;
import com.platform.educational_platform.dto.auth.LoginRequest;
import com.platform.educational_platform.dto.auth.RegisterRequest;
import com.platform.educational_platform.dto.auth.UserDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, "Email deja înregistrat"));
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(request.getRole() != null ? request.getRole() : UserRole.ELEV)
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token, "Înregistrare reușită"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilizator negăsit"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, "Parolă greșită"));
        }

        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, "Login reușit"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow();

        return ResponseEntity.ok(new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().toString()
        ));
    }
}