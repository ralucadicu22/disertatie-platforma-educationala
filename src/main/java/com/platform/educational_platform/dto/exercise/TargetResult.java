package com.platform.educational_platform.dto.exercise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetResult {
    private String targetId;
    private String label;
    private boolean correct;
    private Integer givenValue;
    private Integer correctValue;
}