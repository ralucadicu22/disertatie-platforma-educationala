package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.QuizDTO;
import com.platform.educational_platform.model.Quiz;
import com.platform.educational_platform.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;

    public List<QuizDTO> getQuizzesByModule(Long moduleId) {
        return quizRepository.findByModuleIdOrderByOrderIndexAsc(moduleId)
                .stream()
                .map(q -> QuizDTO.builder()
                        .id(q.getId())
                        .title(q.getTitle())
                        .description(q.getDescription())
                        .orderIndex(q.getOrderIndex())
                        .maxAttempts(q.getMaxAttempts())
                        .moduleId(q.getModule().getId())
                        .build())
                .collect(Collectors.toList());
    }

    public QuizDTO getQuizById(Long id) {
        Quiz q = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz-ul nu a fost gasit"));
        return QuizDTO.builder()
                .id(q.getId())
                .title(q.getTitle())
                .description(q.getDescription())
                .orderIndex(q.getOrderIndex())
                .maxAttempts(q.getMaxAttempts())
                .moduleId(q.getModule().getId())
                .build();
    }

    public List<QuizDTO> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(q -> QuizDTO.builder()
                        .id(q.getId())
                        .title(q.getTitle())
                        .description(q.getDescription())
                        .orderIndex(q.getOrderIndex())
                        .maxAttempts(q.getMaxAttempts())
                        .moduleId(q.getModule() != null ? q.getModule().getId() : null)
                        .build())
                .toList();
    }
}