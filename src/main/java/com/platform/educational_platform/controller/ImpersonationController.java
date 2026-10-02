package com.platform.educational_platform.controller;

import com.platform.educational_platform.model.ImpersonationLog;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.ImpersonationLogRepository;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/impersonation")
@RequiredArgsConstructor
public class ImpersonationController {

    private final ImpersonationLogRepository logRepository;
    private final UserRepository userRepository;

    private static final Set<UserRole> ALLOWED_ROLES =
            EnumSet.of(UserRole.ELEV, UserRole.PROFESOR, UserRole.PARINTE, UserRole.EDITOR);


    @PostMapping("/start")
    public ResponseEntity<?> startImpersonation(
            @RequestBody Map<String, String> body,
            Authentication auth) {

        UserRole targetRole;
        try {
            targetRole = UserRole.valueOf(body.getOrDefault("role", ""));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Rol invalid. Roluri permise: " + ALLOWED_ROLES));
        }
        if (!ALLOWED_ROLES.contains(targetRole)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Rolul " + targetRole + " nu poate fi impersonat."));
        }

        User admin = resolveAdmin(auth);
        logRepository.save(ImpersonationLog.builder()
                .adminId(admin.getId())
                .adminEmail(admin.getEmail())
                .adminName(admin.getName())
                .impersonatedRole(targetRole)
                .action("START")
                .timestamp(LocalDateTime.now())
                .description("Admin " + admin.getName() + " (" + admin.getEmail() + ")"
                        + " a început vizualizarea ca " + targetRole.name())
                .build());

        return ResponseEntity.ok(Map.of("success", true, "role", targetRole.name()));
    }


    @PostMapping("/end")
    public ResponseEntity<?> endImpersonation(
            @RequestBody Map<String, String> body,
            Authentication auth) {

        UserRole impersonatedRole;
        try {
            impersonatedRole = UserRole.valueOf(body.getOrDefault("role", ""));
        } catch (IllegalArgumentException e) {
            impersonatedRole = null;
        }

        User admin = resolveAdmin(auth);
        String roleLabel = impersonatedRole != null ? impersonatedRole.name() : "NECUNOSCUT";

        logRepository.save(ImpersonationLog.builder()
                .adminId(admin.getId())
                .adminEmail(admin.getEmail())
                .adminName(admin.getName())
                .impersonatedRole(impersonatedRole)
                .action("END")
                .timestamp(LocalDateTime.now())
                .description("Admin " + admin.getName() + " (" + admin.getEmail() + ")"
                        + " a încheiat vizualizarea ca " + roleLabel)
                .build());

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ImpersonationLog>> getLogs() {
        return ResponseEntity.ok(logRepository.findTop50ByOrderByTimestampDesc());
    }



    private User resolveAdmin(Authentication auth) {
        if (auth.getPrincipal() instanceof User user) {
            return user;
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Admin user not found by email: " + email));
    }
}
