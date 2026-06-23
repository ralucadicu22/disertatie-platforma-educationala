package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.ModuleDTO;
import com.platform.educational_platform.model.Module;
import com.platform.educational_platform.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public List<ModuleDTO> getAllModules() {
        return moduleRepository.findAllByOrderByOrderIndexAsc()
                .stream()
                .map(m -> ModuleDTO.builder()
                        .id(m.getId())
                        .title(m.getTitle())
                        .description(m.getDescription())
                        .orderIndex(m.getOrderIndex())
                        .build())
                .collect(Collectors.toList());
    }

    public ModuleDTO getModuleById(Long id) {
        Module m = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modulul nu a fost gasit"));
        return ModuleDTO.builder()
                .id(m.getId())
                .title(m.getTitle())
                .description(m.getDescription())
                .orderIndex(m.getOrderIndex())
                .build();
    }
    public List<ModuleDTO> getModulesByProduct(Long productId) {
        return moduleRepository.findByProductIdOrderByOrderIndexAsc(productId)
                .stream()
                .map(m -> ModuleDTO.builder()
                        .id(m.getId())
                        .title(m.getTitle())
                        .description(m.getDescription())
                        .orderIndex(m.getOrderIndex())
                        .build())
                .collect(Collectors.toList());
    }
}