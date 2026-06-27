package com.platform.educational_platform.specification;

import com.platform.educational_platform.model.Exercise;
import com.platform.educational_platform.model.Tag;
import com.platform.educational_platform.model.enums.GradeLevel;
import com.platform.educational_platform.model.enums.VersionStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class ExerciseSpecifications {

    public static Specification<Exercise> hasType(String type) {
        return (root, query, cb) -> type == null
                ? null
                : cb.equal(root.get("type"), type);
    }

    public static Specification<Exercise> hasDifficulty(String difficulty) {
        return (root, query, cb) -> difficulty == null
                ? null
                : cb.equal(root.get("difficulty"), difficulty);
    }

    public static Specification<Exercise> hasTopic(String topic) {
        return (root, query, cb) -> {
            if (topic == null || topic.isBlank()) return null;
            return cb.like(cb.lower(root.get("topic")), "%" + topic.toLowerCase() + "%");
        };
    }

    public static Specification<Exercise> hasGradeLevel(GradeLevel gradeLevel) {
        return (root, query, cb) -> gradeLevel == null
                ? null
                : cb.equal(root.get("gradeLevel"), gradeLevel);
    }

    public static Specification<Exercise> hasTagId(Long tagId) {
        return (root, query, cb) -> {
            if (tagId == null) return null;
            Join<Exercise, Tag> tagJoin = root.join("tags");
            query.distinct(true);
            return cb.equal(tagJoin.get("id"), tagId);
        };
    }

    public static Specification<Exercise> hasCurrentVersionStatus(VersionStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("currentVersion").get("status"), status);
        };
    }
}