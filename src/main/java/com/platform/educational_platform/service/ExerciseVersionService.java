package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.exercise.CreateVersionRequest;
import com.platform.educational_platform.dto.exercise.ExerciseVersionDTO;
import com.platform.educational_platform.dto.exercise.ReviewActionRequest;
import com.platform.educational_platform.model.Exercise;
import com.platform.educational_platform.model.ExerciseVersion;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.VersionStatus;
import com.platform.educational_platform.repository.ExerciseRepository;
import com.platform.educational_platform.repository.ExerciseVersionRepository;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseVersionService {

    private final ExerciseVersionRepository versionRepo;
    private final ExerciseRepository exerciseRepo;
    private final UserRepository userRepo;

    public List<ExerciseVersionDTO> listVersions(Long exerciseId) {
        return versionRepo.findByExerciseIdOrderByVersionNumberDesc(exerciseId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExerciseVersionDTO createDraft(Long exerciseId, CreateVersionRequest req, String userEmail) {
        Exercise exercise = exerciseRepo.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercitiul nu exista"));
        User creator = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilizator inexistent"));

        Integer nextVersion = versionRepo.findMaxVersionNumber(exerciseId) + 1;

        ExerciseVersion v = ExerciseVersion.builder()
                .exercise(exercise)
                .versionNumber(nextVersion)
                .config(req.getConfig())
                .status(VersionStatus.DRAFT)
                .createdBy(creator)
                .changeNote(req.getChangeNote())
                .build();

        return toDTO(versionRepo.save(v));
    }

    @Transactional
    public ExerciseVersionDTO submitForReview(Long versionId, ReviewActionRequest req) {
        ExerciseVersion v = versionRepo.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Versiunea nu exista"));

        if (v.getStatus() != VersionStatus.DRAFT) {
            throw new RuntimeException("Doar versiunile in DRAFT pot fi trimise spre review");
        }

        v.setStatus(VersionStatus.PENDING_REVIEW);
        if (req != null && req.getReviewNote() != null && !req.getReviewNote().isBlank()) {
            v.setChangeNote(req.getReviewNote());
        }
        return toDTO(versionRepo.save(v));
    }

    @Transactional
    public ExerciseVersionDTO publish(Long versionId) {
        ExerciseVersion v = versionRepo.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Versiunea nu exista"));

        if (v.getStatus() == VersionStatus.PUBLISHED) {
            throw new RuntimeException("Versiunea este deja publicata");
        }
        if (v.getStatus() == VersionStatus.ARCHIVED) {
            throw new RuntimeException("Nu se poate publica o versiune arhivata");
        }

        versionRepo.findByExerciseIdAndStatus(v.getExercise().getId(), VersionStatus.PUBLISHED)
                .ifPresent(prev -> {
                    prev.setStatus(VersionStatus.ARCHIVED);
                    prev.setArchivedAt(LocalDateTime.now());
                    versionRepo.save(prev);
                });

        v.setStatus(VersionStatus.PUBLISHED);
        v.setPublishedAt(LocalDateTime.now());
        versionRepo.save(v);

        Exercise exercise = v.getExercise();
        exercise.setCurrentVersion(v);
        exerciseRepo.save(exercise);

        return toDTO(v);
    }

    @Transactional
    public ExerciseVersionDTO approve(Long versionId, ReviewActionRequest req, String adminEmail) {
        ExerciseVersion v = versionRepo.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Versiunea nu exista"));

        if (v.getStatus() != VersionStatus.PENDING_REVIEW) {
            throw new RuntimeException("Doar versiunile in PENDING_REVIEW pot fi aprobate");
        }

        User admin = userRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin inexistent"));

        versionRepo.findByExerciseIdAndStatus(v.getExercise().getId(), VersionStatus.PUBLISHED)
                .ifPresent(prev -> {
                    prev.setStatus(VersionStatus.ARCHIVED);
                    prev.setArchivedAt(LocalDateTime.now());
                    versionRepo.save(prev);
                });

        v.setStatus(VersionStatus.PUBLISHED);
        v.setPublishedAt(LocalDateTime.now());
        v.setReviewedBy(admin);
        v.setReviewedAt(LocalDateTime.now());
        if (req != null) v.setReviewNote(req.getReviewNote());
        versionRepo.save(v);

        Exercise exercise = v.getExercise();
        exercise.setCurrentVersion(v);
        exerciseRepo.save(exercise);

        return toDTO(v);
    }

    @Transactional
    public ExerciseVersionDTO reject(Long versionId, ReviewActionRequest req, String adminEmail) {
        if (req == null || req.getReviewNote() == null || req.getReviewNote().isBlank()) {
            throw new RuntimeException("Motivul respingerii este obligatoriu");
        }

        ExerciseVersion v = versionRepo.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Versiunea nu exista"));

        if (v.getStatus() != VersionStatus.PENDING_REVIEW) {
            throw new RuntimeException("Doar versiunile in PENDING_REVIEW pot fi respinse");
        }

        User admin = userRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin inexistent"));

        v.setStatus(VersionStatus.DRAFT);
        v.setReviewedBy(admin);
        v.setReviewedAt(LocalDateTime.now());
        v.setReviewNote(req.getReviewNote());
        return toDTO(versionRepo.save(v));
    }
    public List<ExerciseVersionDTO> listPendingReview() {
        return versionRepo.findByStatusOrderByCreatedAtAsc(VersionStatus.PENDING_REVIEW)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExerciseVersionDTO archive(Long versionId) {
        ExerciseVersion v = versionRepo.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Versiunea nu exista"));

        v.setStatus(VersionStatus.ARCHIVED);
        v.setArchivedAt(LocalDateTime.now());
        versionRepo.save(v);

        Exercise exercise = v.getExercise();
        if (exercise.getCurrentVersion() != null
                && exercise.getCurrentVersion().getId().equals(v.getId())) {
            exercise.setCurrentVersion(null);
            exerciseRepo.save(exercise);
        }

        return toDTO(v);
    }

    private ExerciseVersionDTO toDTO(ExerciseVersion v) {
        return ExerciseVersionDTO.builder()
                .id(v.getId())
                .exerciseId(v.getExercise().getId())
                .versionNumber(v.getVersionNumber())
                .config(v.getConfig())
                .status(v.getStatus())
                .createdByName(v.getCreatedBy() != null ? v.getCreatedBy().getName() : null)
                .createdAt(v.getCreatedAt())
                .publishedAt(v.getPublishedAt())
                .archivedAt(v.getArchivedAt())
                .changeNote(v.getChangeNote())
                .reviewedByName(v.getReviewedBy() != null ? v.getReviewedBy().getName() : null)
                .reviewedAt(v.getReviewedAt())
                .reviewNote(v.getReviewNote())
                .build();
    }
}