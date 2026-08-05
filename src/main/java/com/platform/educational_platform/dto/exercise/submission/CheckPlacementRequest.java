package com.platform.educational_platform.dto.exercise.submission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckPlacementRequest {
    private Long exerciseId;
    private String draggableId;
    private String targetId;
}