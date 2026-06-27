package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.metadata.CreateTagRequest;
import com.platform.educational_platform.dto.metadata.TagDTO;
import com.platform.educational_platform.model.Tag;
import com.platform.educational_platform.repository.TagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepo;

    public List<TagDTO> listAll() {
        return tagRepo.findAllByOrderByNameAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TagDTO create(CreateTagRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new RuntimeException("Numele tag-ului este obligatoriu");
        }
        String normalized = req.getName().trim();
        if (tagRepo.existsByNameIgnoreCase(normalized)) {
            throw new RuntimeException("Un tag cu acest nume exista deja");
        }

        Tag tag = Tag.builder()
                .name(normalized)
                .description(req.getDescription())
                .build();
        return toDTO(tagRepo.save(tag));
    }

    @Transactional
    public void delete(Long tagId) {
        Tag tag = tagRepo.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag-ul nu exista"));

        if (!tag.getExercises().isEmpty()) {
            throw new RuntimeException(
                    "Tag-ul este folosit de " + tag.getExercises().size()
                            + " exercitii. Sterge-l intai de pe acestea.");
        }
        tagRepo.delete(tag);
    }

    private TagDTO toDTO(Tag t) {
        return TagDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .description(t.getDescription())
                .exerciseCount(t.getExercises() != null ? t.getExercises().size() : 0)
                .build();
    }


    public java.util.Set<Tag> resolveTagIds(java.util.Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new java.util.HashSet<>();
        }
        java.util.Set<Tag> result = new java.util.HashSet<>(tagRepo.findAllById(tagIds));
        if (result.size() != tagIds.size()) {
            throw new RuntimeException("Unul sau mai multe tag-uri nu exista");
        }
        return result;
    }
}