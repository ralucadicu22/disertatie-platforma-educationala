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
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (productRepository.count() > 0) {
            System.out.println("Datele exista deja, seed-ul nu se mai ruleaza.");
            return;
        }

        // ===== PRODUS =====
        Product p1 = productRepository.save(Product.builder()
                .title("Matematica - Clasa Pregatitoare")
                .description("Exercitii distractive pentru copii de 5-7 ani")
                .orderIndex(1)
                .coverImageUrl("http://localhost:5173/images/products/math-prep.png")
                .targetAgeGroup("5-7 ani")
                .subject("Matematica")
                .build());

        // ===== MODUL =====
        Module m1 = moduleRepository.save(Module.builder()
                .title("Animalele de la ferma si din ocean")
                .description("Exercitii distractive de numarare cu animale")
                .orderIndex(1)
                .product(p1)
                .build());

        // ===== QUIZ =====
        Quiz q1 = quizRepository.save(Quiz.builder()
                .title("Gaseste si numara animalele")
                .description("Cate animale din fiecare fel sunt in imagine?")
                .orderIndex(1)
                .maxAttempts(3)
                .module(m1)
                .build());

        // ===== EXERCITIU =====
        String config = """
                {
                  "targets": [
                    {"id": 1, "label": "Porci", "imageUrl": "http://localhost:5173/images/exercise1/porc.png", "correct": 6},
                    {"id": 2, "label": "Oi", "imageUrl": "http://localhost:5173/images/exercise1/oaie.png", "correct": 5},
                    {"id": 3, "label": "Pisici", "imageUrl": "http://localhost:5173/images/exercise1/pisica.png", "correct": 5},
                    {"id": 4, "label": "Catelusi", "imageUrl": "http://localhost:5173/images/exercise1/caine.png", "correct": 6},
                    {"id": 5, "label": "Gaini", "imageUrl": "http://localhost:5173/images/exercise1/gaina.png", "correct": 5},
                    {"id": 6, "label": "Ratuste", "imageUrl": "http://localhost:5173/images/exercise1/ratusca.png", "correct": 6},
                    {"id": 7, "label": "Pesti", "imageUrl": "http://localhost:5173/images/exercise1/peste.png", "correct": 5},
                    {"id": 8, "label": "Papagali", "imageUrl": "http://localhost:5173/images/exercise1/pasare.png", "correct": 6}
                  ]
                }
                """;

        exerciseRepository.save(Exercise.builder()
                .type("FIND_AND_COUNT")
                .questionText("Gaseste si numara animalele!")
                .imageUrl("http://localhost:5173/images/exercise1/findandcount.png")
                .config(config)
                .hint("Numara cu atentie pe fiecare rand.")
                .explanation("Verifica fiecare animal in parte.")
                .points(80)
                .difficulty("MEDIUM")
                .orderIndex(1)
                .maxAttempts(3)
                .quiz(q1)
                .build());

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

        System.out.println("Seed incarcat cu succes!");
    }
}