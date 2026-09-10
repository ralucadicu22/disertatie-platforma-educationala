package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.ModuleDTO;
import com.platform.educational_platform.model.Classroom;
import com.platform.educational_platform.model.Module;
import com.platform.educational_platform.model.Product;
import com.platform.educational_platform.model.Quiz;
import com.platform.educational_platform.repository.ClassroomRepository;
import com.platform.educational_platform.repository.ModuleRepository;
import com.platform.educational_platform.repository.ProductRepository;
import com.platform.educational_platform.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final ProductRepository productRepository;
    private final ClassroomRepository classroomRepository;
    private final QuizRepository quizRepository;

    public List<ModuleDTO> getAllModules() {
        return moduleRepository.findAllByOrderByOrderIndexAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ModuleDTO getModuleById(Long id) {
        return toDTO(moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modulul nu a fost gasit")));
    }

    public List<ModuleDTO> getModulesByProduct(Long productId) {
        return moduleRepository.findByProductIdOrderByOrderIndexAsc(productId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ModuleDTO createModule(Long productId, ModuleDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost gasit"));
        Module m = Module.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .orderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0)
                .product(product)
                .build();
        return toDTO(moduleRepository.save(m));
    }

    public ModuleDTO updateModule(Long id, ModuleDTO dto) {
        Module m = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modulul nu a fost gasit"));
        if (dto.getTitle() != null) m.setTitle(dto.getTitle());
        if (dto.getDescription() != null) m.setDescription(dto.getDescription());
        if (dto.getOrderIndex() != null) m.setOrderIndex(dto.getOrderIndex());
        return toDTO(moduleRepository.save(m));
    }

    @Transactional
    public void deleteModule(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modulul nu a fost gasit"));

        List<Long> quizIds = module.getQuizzes().stream()
                .map(Quiz::getId).toList();

        if (!quizIds.isEmpty()) {
            List<Classroom> classrooms = classroomRepository.findAll().stream()
                    .filter(c -> c.getAssignedQuizzes().stream()
                            .anyMatch(q -> quizIds.contains(q.getId())))
                    .toList();

            for (Classroom c : classrooms) {
                c.getAssignedQuizzes().removeIf(q -> quizIds.contains(q.getId()));
                classroomRepository.save(c);
            }
        }

        moduleRepository.delete(module);
    }

    private ModuleDTO toDTO(Module m) {
        return ModuleDTO.builder()
                .id(m.getId())
                .title(m.getTitle())
                .description(m.getDescription())
                .orderIndex(m.getOrderIndex())
                .productId(m.getProduct() != null ? m.getProduct().getId() : null)
                .build();
    }
}