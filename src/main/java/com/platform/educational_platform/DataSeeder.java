package com.platform.educational_platform;


import com.platform.educational_platform.dto.config.FindAndCountConfig;
import com.platform.educational_platform.dto.config.HintConfig;
import com.platform.educational_platform.model.*;
import com.platform.educational_platform.model.Module;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.model.enums.VersionStatus;
import com.platform.educational_platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ModuleRepository moduleRepository;
    private final QuizRepository quizRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseVersionRepository exerciseVersionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args)  {

        // ===== USERI DEMO (creati primii ca admin sa fie disponibil pt createdBy) =====
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

        // Daca produsele exista deja, nu mai facem nimic (idempotent)
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

        // ===== CONFIG JSON pentru FIND_AND_COUNT =====
        FindAndCountConfig config = new FindAndCountConfig();
        config.setInstruction("Gaseste si numara animalele!");
        config.setImageUrl("http://localhost:5173/images/exercise1/findandcount.png");
        config.setExplanation("Verifica fiecare animal in parte si numara cu atentie pe fiecare rand.");

        List<FindAndCountConfig.Target> targets = new ArrayList<>();
        targets.add(buildTarget("porci", "Porci", "porc.png", 6));
        targets.add(buildTarget("oi", "Oi", "oaie.png", 5));
        targets.add(buildTarget("pisici", "Pisici", "pisica.png", 5));
        targets.add(buildTarget("catelusi", "Catelusi", "caine.png", 6));
        targets.add(buildTarget("gaini", "Gaini", "gaina.png", 5));
        targets.add(buildTarget("ratuste", "Ratuste", "ratusca.png", 6));
        targets.add(buildTarget("pesti", "Pesti", "peste.png", 5));
        targets.add(buildTarget("papagali", "Papagali", "pasare.png", 6));
        config.setTargets(targets);

        // Hinturi configurabile per exercitiu
        List<HintConfig> hints = new ArrayList<>();
        hints.add(buildHint("Numara cu atentie pe fiecare rand.", 1));
        hints.add(buildHint("Verifica si in colturile imaginii — unele animale sunt mai mici.", 2));
        config.setHints(hints);

        String configJson;
        try {
            configJson = objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // ===== EXERCITIU (doar metadata) =====
        Exercise exercise = exerciseRepository.save(Exercise.builder()
                .type("FIND_AND_COUNT")
                .points(80)
                .difficulty("MEDIUM")
                .orderIndex(1)
                .maxAttempts(3)
                .quiz(q1)
                .build());

        // ===== VERSIUNEA 1 (PUBLISHED) =====
        User admin = userRepository.findByEmail("admin@test.com").orElse(null);

        ExerciseVersion v1 = ExerciseVersion.builder()
                .exercise(exercise)
                .versionNumber(1)
                .config(configJson)
                .status(VersionStatus.PUBLISHED)
                .createdBy(admin)
                .publishedAt(LocalDateTime.now())
                .changeNote("Versiune initiala")
                .build();
        v1 = exerciseVersionRepository.save(v1);

        // Seteaza pointer-ul de currentVersion pe Exercise
        exercise.setCurrentVersion(v1);
        exerciseRepository.save(exercise);

        System.out.println("Seed incarcat cu succes!");
    }

    // ===== HELPERS =====

    private FindAndCountConfig.Target buildTarget(String id, String label, String imageFile, int correct) {
        FindAndCountConfig.Target t = new FindAndCountConfig.Target();
        t.setId(id);
        t.setLabel(label);
        t.setImageUrl("http://localhost:5173/images/exercise1/" + imageFile);
        t.setCorrect(correct);
        return t;
    }

    private HintConfig buildHint(String text, int afterAttempt) {
        HintConfig h = new HintConfig();
        h.setText(text);
        h.setAfterAttempt(afterAttempt);
        h.setType("TEXT");
        return h;
    }
}