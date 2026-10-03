package com.platform.educational_platform.controller;

import com.platform.educational_platform.config.JwtFilter;
import com.platform.educational_platform.model.ImpersonationLog;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.ImpersonationLogRepository;
import com.platform.educational_platform.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> startImpersonation(@RequestBody Map<String, Object> body,
                                                HttpServletRequest request) {
        Long targetId = parseLong(body.get("userId"));
        if (targetId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "userId este necesar"));
        }

        User target = userRepository.findById(targetId).orElse(null);
        if (target == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Utilizator negăsit"));
        }
        if (!ALLOWED_ROLES.contains(target.getRole())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Rolul " + target.getRole() + " nu poate fi impersonat"));
        }

        User admin = resolveAdmin(request);
        logRepository.save(ImpersonationLog.builder()
                .adminId(admin.getId())
                .adminEmail(admin.getEmail())
                .adminName(admin.getName())
                .impersonatedUserId(target.getId())
                .impersonatedUserEmail(target.getEmail())
                .impersonatedUserName(target.getName())
                .impersonatedRole(target.getRole())
                .action("START")
                .timestamp(LocalDateTime.now())
                .description("Admin " + admin.getName() + " (" + admin.getEmail() + ")"
                        + " a început vizualizarea ca " + target.getName()
                        + " (" + target.getRole().name() + ")")
                .build());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                        "id", target.getId(),
                        "name", target.getName(),
                        "email", target.getEmail(),
                        "role", target.getRole().name()
                )
        ));
    }

    @PostMapping("/end")
    public ResponseEntity<?> endImpersonation(@RequestBody Map<String, Object> body,
                                              HttpServletRequest request) {
        Long targetId = parseLong(body.get("userId"));
        User target = (targetId != null)
                ? userRepository.findById(targetId).orElse(null)
                : null;

        User admin = resolveAdmin(request);

        ImpersonationLog.ImpersonationLogBuilder builder = ImpersonationLog.builder()
                .adminId(admin.getId())
                .adminEmail(admin.getEmail())
                .adminName(admin.getName())
                .action("END")
                .timestamp(LocalDateTime.now());

        if (target != null) {
            builder.impersonatedUserId(target.getId())
                    .impersonatedUserEmail(target.getEmail())
                    .impersonatedUserName(target.getName())
                    .impersonatedRole(target.getRole())
                    .description("Admin " + admin.getName() + " a încheiat vizualizarea ca "
                            + target.getName() + " (" + target.getRole().name() + ")");
        } else {
            builder.description("Admin " + admin.getName() + " a încheiat vizualizarea");
        }

        logRepository.save(builder.build());
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ImpersonationLog>> getLogs() {
        return ResponseEntity.ok(logRepository.findTop50ByOrderByTimestampDesc());
    }

    private User resolveAdmin(HttpServletRequest request) {
        Object attr = request.getAttribute(JwtFilter.ATTR_REAL_USER);
        if (attr instanceof User u) return u;
        throw new IllegalStateException("Admin user not available in request attributes");
    }

    private Long parseLong(Object raw) {
        if (raw == null) return null;
        try { return Long.valueOf(raw.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}