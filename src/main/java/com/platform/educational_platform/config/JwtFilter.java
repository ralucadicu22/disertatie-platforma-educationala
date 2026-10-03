package com.platform.educational_platform.config;

import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String ATTR_REAL_USER = "realAuthenticatedUser";
    public static final String HEADER_IMPERSONATE_ID = "X-Impersonate-User-Id";

    private static final Set<UserRole> IMPERSONATABLE_ROLES =
            EnumSet.of(UserRole.ELEV, UserRole.PROFESOR, UserRole.PARINTE, UserRole.EDITOR);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtUtil.isTokenValid(token)) {
                    String email = jwtUtil.extractEmail(token);
                    User authUser = userRepository.findByEmail(email).orElse(null);

                    if (authUser != null) {
                        request.setAttribute(ATTR_REAL_USER, authUser);
                        User principal = authUser;
                        List<GrantedAuthority> authorities = new ArrayList<>();
                        authorities.add(new SimpleGrantedAuthority(authUser.getRole().name()));
                        if (authUser.getRole() == UserRole.ADMIN) {
                            String impIdHeader = request.getHeader(HEADER_IMPERSONATE_ID);
                            if (impIdHeader != null && !impIdHeader.isBlank()) {
                                try {
                                    Long impId = Long.valueOf(impIdHeader.trim());
                                    User target = userRepository.findById(impId).orElse(null);
                                    if (target != null && IMPERSONATABLE_ROLES.contains(target.getRole())) {
                                        principal = target;
                                        authorities.add(new SimpleGrantedAuthority(target.getRole().name()));
                                    }
                                } catch (NumberFormatException ignored) {}
                            }
                        }

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        filterChain.doFilter(request, response);
    }
}