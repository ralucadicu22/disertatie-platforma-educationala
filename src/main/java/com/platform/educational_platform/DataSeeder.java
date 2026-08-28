package com.platform.educational_platform;

import com.platform.educational_platform.model.*;
import com.platform.educational_platform.model.Module;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ModuleRepository moduleRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ===== USERI DEMO =====
        if (userRepository.findByEmail("elev@test.com").isEmpty()) {
            userRepository.save(User.builder()
                    .email("elev@test.com")
                    .password(passwordEncoder.encode("parola123"))
                    .name("Ion Elev")
                    .role(UserRole.ELEV)
                    .build());

            userRepository.save(User.builder()
                    .email("profesor@test.com")
                    .password(passwordEncoder.encode("parola123"))
                    .name("Ana Profesor")
                    .role(UserRole.PROFESOR)
                    .build());

            userRepository.save(User.builder()
                    .email("editor@test.com")
                    .password(passwordEncoder.encode("parola123"))
                    .name("Mihai Editor")
                    .role(UserRole.EDITOR)
                    .build());

            userRepository.save(User.builder()
                    .email("admin@test.com")
                    .password(passwordEncoder.encode("parola123"))
                    .name("Admin")
                    .role(UserRole.ADMIN)
                    .build());
        }


        if (productRepository.count() > 0) {
            System.out.println("Datele exista deja, seed-ul nu se mai ruleaza.");
            return;
        }

        Product p1 = productRepository.save(Product.builder()
                .title("Matematica - Clasa Pregatitoare")
                .description("Exercitii distractive pentru copii de 5-7 ani")
                .orderIndex(1)
                .coverImageUrl("http://localhost:5173/images/products/math-prep.png")
                .targetAgeGroup("5-7 ani")
                .subject("Matematica")
                .build());

        Module m1 = moduleRepository.save(Module.builder()
                .title("Animalele de la ferma si din ocean")
                .description("Exercitii distractive de numarare cu animale")
                .orderIndex(1)
                .product(p1)
                .build());

        quizRepository.save(Quiz.builder()
                .title("Gaseste si numara animalele")
                .description("Cate animale din fiecare fel sunt in imagine?")
                .orderIndex(1)
                .maxAttempts(3)
                .module(m1)
                .build());

        System.out.println("Seed minimal incarcat: 4 useri + 1 produs + 1 modul + 1 quiz.");
    }
}