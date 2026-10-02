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
import java.util.List;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

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
                    User user = userRepository.findByEmail(email).orElse(null);

                    if (user != null) {
                        List<GrantedAuthority> authorities = new ArrayList<>();
                        authorities.add(new SimpleGrantedAuthority(user.getRole().name()));



                        if (user.getRole() == UserRole.ADMIN) {
                            String impersonatingAs = request.getHeader("X-Impersonating-As");
                            if (impersonatingAs != null && !impersonatingAs.isBlank()) {
                                try {
                                    UserRole.valueOf(impersonatingAs);
                                    authorities.add(new SimpleGrantedAuthority(impersonatingAs));
                                } catch (IllegalArgumentException ignored) {

                                }
                            }
                        }

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        authorities
                                );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        filterChain.doFilter(request, response);
    }
}
