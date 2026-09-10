package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.ProductDTO;
import com.platform.educational_platform.model.Classroom;
import com.platform.educational_platform.model.Product;
import com.platform.educational_platform.repository.ClassroomRepository;
import com.platform.educational_platform.repository.ModuleRepository;
import com.platform.educational_platform.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ClassroomRepository classroomRepository;
    private final ModuleRepository moduleRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAllByOrderByOrderIndexAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        return toDTO(productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost gasit")));
    }

    public ProductDTO createProduct(ProductDTO dto) {
        Product p = Product.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .orderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0)
                .coverImageUrl(dto.getCoverImageUrl())
                .targetAgeGroup(dto.getTargetAgeGroup())
                .subject(dto.getSubject())
                .build();
        return toDTO(productRepository.save(p));
    }

    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost gasit"));
        if (dto.getTitle() != null) p.setTitle(dto.getTitle());
        if (dto.getDescription() != null) p.setDescription(dto.getDescription());
        if (dto.getOrderIndex() != null) p.setOrderIndex(dto.getOrderIndex());
        if (dto.getCoverImageUrl() != null) p.setCoverImageUrl(dto.getCoverImageUrl());
        if (dto.getTargetAgeGroup() != null) p.setTargetAgeGroup(dto.getTargetAgeGroup());
        if (dto.getSubject() != null) p.setSubject(dto.getSubject());
        return toDTO(productRepository.save(p));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost gasit"));

        List<Long> quizIds = product.getModules().stream()
                .flatMap(m -> m.getQuizzes().stream())
                .map(q -> q.getId())
                .toList();

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

        productRepository.delete(product);
    }

    private ProductDTO toDTO(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .orderIndex(p.getOrderIndex())
                .coverImageUrl(p.getCoverImageUrl())
                .targetAgeGroup(p.getTargetAgeGroup())
                .subject(p.getSubject())
                .build();
    }
}