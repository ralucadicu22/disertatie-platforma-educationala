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

    private static final String[] ALL_ROLES   = {"ELEV", "PROFESOR", "PARINTE", "EDITOR", "ADMIN"};
    private static final String[] STAFF       = {"PROFESOR", "EDITOR", "ADMIN"};
    private static final String[] EDITORS_UP  = {"EDITOR", "ADMIN"};
    private static final String[] TEACHERS_UP = {"PROFESOR", "ADMIN"};
    private static final String[] LEARNERS    = {"ELEV", "PROFESOR", "EDITOR", "ADMIN"};

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtFilter(jwtUtil, userRepository),
                        UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth

                        // ── Public ───────────────────────────────────────────────────
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/count", "/api/exercises/count").hasAuthority("ADMIN")
                        // ── Assets ───────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/assets").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.POST,   "/api/assets/upload").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.DELETE, "/api/assets/**").hasAnyAuthority(EDITORS_UP)

                        // ── Products ─────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/products/**").hasAnyAuthority(LEARNERS)
                        .requestMatchers(HttpMethod.POST,   "/api/products/**").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.PUT,    "/api/products/**").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyAuthority(EDITORS_UP)

                        // ── Modules ──────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/modules/**").hasAnyAuthority(LEARNERS)
                        .requestMatchers(HttpMethod.POST,   "/api/modules/**").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.PUT,    "/api/modules/**").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.DELETE, "/api/modules/**").hasAnyAuthority(EDITORS_UP)

                        // ── Quizzes ──────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/quizzes/**").hasAnyAuthority(LEARNERS)
                        .requestMatchers(HttpMethod.POST,   "/api/quizzes/**").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.PUT,    "/api/quizzes/**").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/**").hasAnyAuthority(EDITORS_UP)

                        // ── Tags ─────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/tags").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.POST,   "/api/tags").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tags/**").hasAuthority("ADMIN")

                        // ── Exercises — draft explicit (înainte de catch-all) ─────────
                        .requestMatchers(HttpMethod.POST, "/api/exercises/draft/new").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.PUT,  "/api/exercises/*/draft").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.GET,  "/api/exercises/*/draft").hasAnyAuthority(EDITORS_UP)

                        // ── Exercises — versioning ────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,  "/api/exercises/versions/pending-review").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/exercises/versions/templates").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.GET,  "/api/exercises/*/versions").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.POST, "/api/exercises/*/versions").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/submit-for-review").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/approve").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/reject").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/publish").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/archive").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/duplicate").hasAnyAuthority(EDITORS_UP)

                        .requestMatchers(HttpMethod.POST, "/api/exercises/versions/*/template").hasAnyAuthority("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/exercises/versions/*/template").hasAnyAuthority("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/exercises/from-template").hasAnyAuthority(EDITORS_UP)

                        // ── Exercises — metadata & filter ─────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/exercises/filter").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.PUT, "/api/exercises/*/metadata").hasAnyAuthority(EDITORS_UP)

                        // ── Exercises — hints ─────────────────────────────────────────
                        .requestMatchers("/api/exercises/*/hints/available").authenticated()
                        .requestMatchers("/api/exercises/*/hints/all").hasAnyAuthority(LEARNERS)

                        // ── Exercises — submission (elevi) ────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/exercises/submit").hasAnyAuthority(LEARNERS)
                        .requestMatchers(HttpMethod.POST, "/api/exercises/check-element").hasAnyAuthority(LEARNERS)
                        .requestMatchers(HttpMethod.POST, "/api/exercises/check-placement").hasAnyAuthority(LEARNERS)

                        // ── Exercises — general catch-all ─────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/exercises/**").hasAnyAuthority(LEARNERS)
                        .requestMatchers(HttpMethod.POST,   "/api/exercises/**").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.PUT,    "/api/exercises/**").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers(HttpMethod.DELETE, "/api/exercises/**").hasAnyAuthority(EDITORS_UP)

                        // ── Classrooms ────────────────────────────────────────────────

                        .requestMatchers("/api/classrooms/*/teachers/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/classrooms/*/students/**").hasAnyAuthority(TEACHERS_UP)

                        .requestMatchers(HttpMethod.GET, "/api/classrooms/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/classrooms/**").authenticated()
                        //.requestMatchers(HttpMethod.GET, "/api/classrooms/**").hasAnyAuthority("ADMIN", "PROFESOR")
                        .requestMatchers(HttpMethod.POST, "/api/classrooms").hasAnyAuthority(TEACHERS_UP)
                        .requestMatchers(HttpMethod.PUT,    "/api/classrooms/**").hasAnyAuthority(TEACHERS_UP)
                        .requestMatchers(HttpMethod.DELETE, "/api/classrooms/**").hasAuthority("ADMIN")

                        // ── Quiz assignments ──────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/quiz-assignments/assign").hasAnyAuthority(TEACHERS_UP)
                        .requestMatchers(HttpMethod.DELETE, "/api/quiz-assignments/**").hasAnyAuthority(TEACHERS_UP)
                        .requestMatchers(HttpMethod.GET,    "/api/quiz-assignments/my-classrooms").hasAnyAuthority(TEACHERS_UP)
                        .requestMatchers(HttpMethod.GET,    "/api/quiz-assignments/my-quizzes").hasAuthority("ELEV")

                        // ── Parent-child associations ─────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/parent-child/link").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/parent-child/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/parent-child/all").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/parent-child/parent/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/parent-child/my-children").hasAuthority("PARINTE")
                        .requestMatchers(HttpMethod.GET,    "/api/parent-child/my-parents").hasAuthority("ELEV")

                        // ── Parent dashboard ──────────────────────────────────────────
                        .requestMatchers("/api/parent-dashboard/**").hasAuthority("PARINTE")

                        // ── Quiz attempts ─────────────────────────────────────────────
                        .requestMatchers("/api/quiz-attempts/**").hasAuthority("ELEV")

                        // ── Teacher feedback ──────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/teacher-feedback").hasAnyAuthority(TEACHERS_UP)
                        .requestMatchers(HttpMethod.PUT,    "/api/teacher-feedback/**").hasAnyAuthority(TEACHERS_UP)
                        .requestMatchers(HttpMethod.DELETE, "/api/teacher-feedback/**").hasAnyAuthority(TEACHERS_UP)
                        .requestMatchers(HttpMethod.POST,   "/api/teacher-feedback/*/mark-read").hasAuthority("ELEV")
                        .requestMatchers(HttpMethod.GET,    "/api/teacher-feedback/my-received/**").hasAuthority("ELEV")
                        .requestMatchers(HttpMethod.GET,    "/api/teacher-feedback/my-given").hasAnyAuthority(TEACHERS_UP)
                        .requestMatchers(HttpMethod.GET,    "/api/teacher-feedback/student/**").hasAnyAuthority(TEACHERS_UP)
                        .requestMatchers("/api/teacher-feedback/attempt/**").authenticated()

                        // ── Student feedback ──────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/student-feedback").hasAuthority("ELEV")
                        .requestMatchers(HttpMethod.DELETE, "/api/student-feedback/my/**").hasAuthority("ELEV")
                        .requestMatchers(HttpMethod.GET,    "/api/student-feedback/my/**").hasAuthority("ELEV")
                        .requestMatchers("/api/student-feedback/exercise/**").hasAnyAuthority(STAFF)

                        // ── Progress ──────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/progress/student/**").hasAnyAuthority(TEACHERS_UP)
                        .requestMatchers("/api/progress/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/admin/impersonation/start").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/impersonation/end").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/admin/impersonation/logs").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/bulk-import/preview").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/bulk-import/import").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/admin/bulk-import/history").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/admin/bulk-import/template").hasAuthority("ADMIN")

                        // ── Admin / Editor / Teacher catch-all ────────────────────────
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/editor/**").hasAnyAuthority(EDITORS_UP)
                        .requestMatchers("/api/teacher/**").hasAnyAuthority(TEACHERS_UP)

                        .requestMatchers(HttpMethod.GET, "/api/export/**").hasAnyAuthority(TEACHERS_UP)

                        .requestMatchers(HttpMethod.GET,    "/api/users/**").hasAnyAuthority(TEACHERS_UP)
                        .requestMatchers(HttpMethod.PUT,    "/api/users/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("ADMIN")



                        .requestMatchers("/api/badges/**").hasAnyAuthority(LEARNERS)

                        // ── Default ───────────────────────────────────────────────────
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Impersonate-User-Id"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}