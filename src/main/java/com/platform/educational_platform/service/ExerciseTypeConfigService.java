package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.exercisetype.ExerciseTypeConfigDTO;
import com.platform.educational_platform.dto.exercisetype.ProductTypeOverrideDTO;
import com.platform.educational_platform.dto.exercisetype.ResolvedTypeStatusDTO;
import com.platform.educational_platform.model.ExerciseTypeConfig;
import com.platform.educational_platform.model.Product;
import com.platform.educational_platform.model.ProductTypeOverride;
import com.platform.educational_platform.repository.ExerciseTypeConfigRepository;
import com.platform.educational_platform.repository.ProductRepository;
import com.platform.educational_platform.repository.ProductTypeOverrideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseTypeConfigService {

    private final ExerciseTypeConfigRepository configRepo;
    private final ProductTypeOverrideRepository overrideRepo;
    private final ProductRepository productRepo;

    public List<ExerciseTypeConfigDTO> getAllGlobalConfigs() {
        return configRepo.findAllByOrderByExerciseTypeAsc()
                .stream()
                .map(this::toConfigDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public ExerciseTypeConfigDTO setGlobalEnabled(String exerciseType, boolean enabled) {
        ExerciseTypeConfig config = configRepo.findByExerciseType(exerciseType)
                .orElseGet(() -> ExerciseTypeConfig.builder()
                        .exerciseType(exerciseType)
                        .build());
        config.setEnabled(enabled);
        return toConfigDTO(configRepo.save(config));
    }

    public List<ProductTypeOverrideDTO> getOverridesForProduct(Long productId) {
        return overrideRepo.findByProductId(productId)
                .stream()
                .map(this::toOverrideDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public ProductTypeOverrideDTO setProductOverride(Long productId, String exerciseType, Boolean enabled) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));

        if (enabled == null) {
            overrideRepo.deleteByProductIdAndExerciseType(productId, exerciseType);
            return ProductTypeOverrideDTO.builder()
                    .productId(productId)
                    .exerciseType(exerciseType)
                    .enabled(null)
                    .build();
        }

        ProductTypeOverride override = overrideRepo
                .findByProductIdAndExerciseType(productId, exerciseType)
                .orElseGet(() -> ProductTypeOverride.builder()
                        .product(product)
                        .exerciseType(exerciseType)
                        .build());
        override.setEnabled(enabled);
        return toOverrideDTO(overrideRepo.save(override));
    }

    public List<ResolvedTypeStatusDTO> getResolvedStatusForProduct(Long productId) {
        Map<String, Boolean> globalMap = configRepo.findAllByOrderByExerciseTypeAsc()
                .stream()
                .collect(Collectors.toMap(ExerciseTypeConfig::getExerciseType, ExerciseTypeConfig::isEnabled));

        Map<String, Boolean> overrideMap = overrideRepo.findByProductId(productId)
                .stream()
                .filter(o -> o.getEnabled() != null)
                .collect(Collectors.toMap(ProductTypeOverride::getExerciseType, ProductTypeOverride::getEnabled));

        Set<String> allTypes = new LinkedHashSet<>();
        allTypes.addAll(globalMap.keySet());
        allTypes.addAll(overrideMap.keySet());

        return allTypes.stream()
                .sorted()
                .map(type -> {
                    boolean globalEnabled = globalMap.getOrDefault(type, true);
                    Boolean productOverride = overrideMap.getOrDefault(type, null);
                    boolean effective = productOverride != null ? productOverride : globalEnabled;
                    return ResolvedTypeStatusDTO.builder()
                            .exerciseType(type)
                            .globalEnabled(globalEnabled)
                            .productOverride(productOverride)
                            .effectiveEnabled(effective)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public boolean isTypeAllowedForQuiz(Long quizId, String exerciseType) {
        Optional<Long> productIdOpt = productRepo.findProductIdByQuizId(quizId);
        if (productIdOpt.isEmpty()) {
            return configRepo.findByExerciseType(exerciseType)
                    .map(ExerciseTypeConfig::isEnabled)
                    .orElse(true);
        }

        Long productId = productIdOpt.get();
        Optional<ProductTypeOverride> override = overrideRepo
                .findByProductIdAndExerciseType(productId, exerciseType);

        if (override.isPresent() && override.get().getEnabled() != null) {
            return override.get().getEnabled();
        }

        return configRepo.findByExerciseType(exerciseType)
                .map(ExerciseTypeConfig::isEnabled)
                .orElse(true);
    }

    private ExerciseTypeConfigDTO toConfigDTO(ExerciseTypeConfig c) {
        return ExerciseTypeConfigDTO.builder()
                .id(c.getId())
                .exerciseType(c.getExerciseType())
                .enabled(c.isEnabled())
                .build();
    }

    private ProductTypeOverrideDTO toOverrideDTO(ProductTypeOverride o) {
        return ProductTypeOverrideDTO.builder()
                .id(o.getId())
                .productId(o.getProduct().getId())
                .exerciseType(o.getExerciseType())
                .enabled(o.getEnabled())
                .build();
    }
}
