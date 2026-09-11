package com.platform.educational_platform.controller;

import com.platform.educational_platform.model.Classroom;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.UserProgress;
import com.platform.educational_platform.repository.ClassroomRepository;
import com.platform.educational_platform.repository.UserProgressRepository;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ExportController {

    private final ClassroomRepository classroomRepository;
    private final UserProgressRepository userProgressRepository;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @GetMapping("/classroom/{classroomId}/csv")
    public void exportClassroomCsv(
            @PathVariable Long classroomId,
            @AuthenticationPrincipal User currentUser,
            HttpServletResponse response) throws IOException {

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Clasa nu există"));

        boolean isTeacher = classroom.getTeachers().stream()
                .anyMatch(t -> t.getId().equals(currentUser.getId()));
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        if (!isTeacher && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces interzis");
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"rezultate_" + classroom.getName().replaceAll("\\s+", "_") + ".csv\"");

        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{
                    "Elev", "Email", "Exercițiu ID", "Corect", "Puncte obținute",
                    "Număr încercare", "Data submisiei"
            });


            Set<User> students = classroom.getStudents();
            for (User student : students) {
                List<UserProgress> progresses = userProgressRepository
                        .findByUserIdOrderBySubmittedAtDesc(student.getId());

                if (progresses.isEmpty()) {
                    writer.writeNext(new String[]{
                            student.getName(), student.getEmail(),
                            "—", "—", "—", "—", "—"
                    });
                } else {
                    for (UserProgress p : progresses) {
                        writer.writeNext(new String[]{
                                student.getName(),
                                student.getEmail(),
                                String.valueOf(p.getExercise().getId()),
                                p.getCorrect() ? "Da" : "Nu",
                                String.valueOf(p.getPointsEarned()),
                                String.valueOf(p.getAttemptNumber()),
                                p.getSubmittedAt() != null
                                        ? p.getSubmittedAt().format(FORMATTER)
                                        : "—"
                        });
                    }
                }
            }
        }
    }

    @GetMapping("/classroom/{classroomId}/summary-csv")
    public void exportClassroomSummaryCsv(
            @PathVariable Long classroomId,
            @AuthenticationPrincipal User currentUser,
            HttpServletResponse response) throws IOException {

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Clasa nu există"));

        boolean isTeacher = classroom.getTeachers().stream()
                .anyMatch(t -> t.getId().equals(currentUser.getId()));
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        if (!isTeacher && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces interzis");
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"sumar_" + classroom.getName().replaceAll("\\s+", "_") + ".csv\"");
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {

            writer.writeNext(new String[]{
                    "Elev", "Email", "Exerciții rezolvate", "Total încercări",
                    "Total puncte", "Medie puncte"
            });

            for (User student : classroom.getStudents()) {
                List<UserProgress> progresses = userProgressRepository
                        .findByUserIdOrderBySubmittedAtDesc(student.getId());

                long completed = progresses.stream()
                        .filter(UserProgress::getCorrect)
                        .map(p -> p.getExercise().getId())
                        .distinct().count();
                int totalPoints = progresses.stream()
                        .mapToInt(UserProgress::getPointsEarned).sum();
                double avgPoints = progresses.isEmpty() ? 0
                        : (double) totalPoints / progresses.size();

                writer.writeNext(new String[]{
                        student.getName(),
                        student.getEmail(),
                        String.valueOf(completed),
                        String.valueOf(progresses.size()),
                        String.valueOf(totalPoints),
                        String.format("%.1f", avgPoints)
                });
            }
        }
    }
}