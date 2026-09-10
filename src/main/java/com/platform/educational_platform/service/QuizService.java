package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.QuizDTO;
import com.platform.educational_platform.model.Classroom;
import com.platform.educational_platform.model.Module;
import com.platform.educational_platform.model.Quiz;
import com.platform.educational_platform.repository.ClassroomRepository;
import com.platform.educational_platform.repository.ModuleRepository;
import com.platform.educational_platform.repository.QuizAttemptRepository;
import com.platform.educational_platform.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final ModuleRepository moduleRepository;
    private final ClassroomRepository classroomRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public List<QuizDTO> getQuizzesByModule(Long moduleId) {
        return quizRepository.findByModuleIdOrderByOrderIndexAsc(moduleId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public QuizDTO getQuizById(Long id) {
        return toDTO(quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz-ul nu a fost gasit")));
    }

    public List<QuizDTO> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public QuizDTO createQuiz(Long moduleId, QuizDTO dto) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Modulul nu a fost gasit"));
        Quiz q = Quiz.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .orderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0)
                .maxAttempts(dto.getMaxAttempts() != null ? dto.getMaxAttempts() : 0)
                .maxReplays(dto.getMaxReplays() != null ? dto.getMaxReplays() : 0)
                .module(module)
                .build();
        return toDTO(quizRepository.save(q));
    }

    public QuizDTO updateQuiz(Long id, QuizDTO dto) {
        Quiz q = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz-ul nu a fost gasit"));
        if (dto.getTitle() != null) q.setTitle(dto.getTitle());
        if (dto.getDescription() != null) q.setDescription(dto.getDescription());
        if (dto.getOrderIndex() != null) q.setOrderIndex(dto.getOrderIndex());
        if (dto.getMaxAttempts() != null) q.setMaxAttempts(dto.getMaxAttempts());
        if (dto.getMaxReplays() != null) q.setMaxReplays(dto.getMaxReplays());
        return toDTO(quizRepository.save(q));
    }

    @Transactional
    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz-ul nu a fost gasit"));


        quizAttemptRepository.deleteByQuizId(id);
        List<Classroom> classrooms = classroomRepository.findAll().stream()
                .filter(c -> c.getAssignedQuizzes().stream()
                        .anyMatch(q -> q.getId().equals(id)))
                .toList();
        for (Classroom c : classrooms) {
            c.getAssignedQuizzes().removeIf(q -> q.getId().equals(id));
            classroomRepository.save(c);
        }

        quizRepository.delete(quiz);
    }

    private QuizDTO toDTO(Quiz q) {
        return QuizDTO.builder()
                .id(q.getId())
                .title(q.getTitle())
                .description(q.getDescription())
                .orderIndex(q.getOrderIndex())
                .maxAttempts(q.getMaxAttempts())
                .maxReplays(q.getMaxReplays())
                .moduleId(q.getModule() != null ? q.getModule().getId() : null)
                .build();
    }
}