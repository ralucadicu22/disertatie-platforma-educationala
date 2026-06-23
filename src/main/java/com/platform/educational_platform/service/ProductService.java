package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.ProductDTO;
import com.platform.educational_platform.model.Product;
import com.platform.educational_platform.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAllByOrderByOrderIndexAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost gasit"));
        return toDTO(p);
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