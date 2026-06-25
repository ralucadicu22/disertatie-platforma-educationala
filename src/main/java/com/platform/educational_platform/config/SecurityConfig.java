package com.platform.educational_platform.config;

import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth


                        .requestMatchers("/api/auth/**").permitAll()


                                .requestMatchers(HttpMethod.GET, "/api/exercises/*/versions")
                                .hasAnyAuthority("EDITOR", "ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/exercises/*/versions")
                                .hasAnyAuthority("EDITOR", "ADMIN")


                                .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/submit-for-review")
                                .hasAnyAuthority("EDITOR", "ADMIN")


                                .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/approve")
                                .hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/reject")
                                .hasAuthority("ADMIN")


                                .requestMatchers(HttpMethod.GET, "/api/exercises/versions/pending-review")
                                .hasAuthority("ADMIN")


                                .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/publish")
                                .hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/archive")
                                .hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/duplicate")
                                .hasAnyAuthority("EDITOR", "ADMIN")


                                .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/template")
                                .hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/exercises/versions/*/template")
                                .hasAuthority("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/exercises/versions/templates")
                                .hasAnyAuthority("EDITOR", "ADMIN")


                                .requestMatchers(HttpMethod.POST, "/api/exercises/from-template")
                                .hasAnyAuthority("EDITOR", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/exercises/submit")
                        .hasAuthority("ELEV")


                        .requestMatchers(HttpMethod.GET, "/api/modules/**")
                        .hasAnyAuthority("ELEV", "PROFESOR", "EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/**")
                        .hasAnyAuthority("ELEV", "PROFESOR", "EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/exercises/**")
                        .hasAnyAuthority("ELEV", "PROFESOR", "EDITOR", "ADMIN")


                        .requestMatchers(HttpMethod.POST, "/api/modules/**").hasAnyAuthority("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/modules/**").hasAnyAuthority("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/modules/**").hasAnyAuthority("EDITOR", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/quizzes/**").hasAnyAuthority("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/quizzes/**").hasAnyAuthority("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/**").hasAnyAuthority("EDITOR", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/exercises/**").hasAnyAuthority("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/exercises/**").hasAnyAuthority("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/exercises/**").hasAnyAuthority("EDITOR", "ADMIN")


                        .requestMatchers(HttpMethod.GET, "/api/products/**")
                        .hasAnyAuthority("ELEV", "PROFESOR", "EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyAuthority("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyAuthority("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyAuthority("EDITOR", "ADMIN")


                        .requestMatchers("/api/teacher/**").hasAnyAuthority("PROFESOR", "ADMIN")
                        .requestMatchers("/api/editor/**").hasAnyAuthority("EDITOR", "ADMIN")
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtFilter(jwtUtil, userRepository),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}