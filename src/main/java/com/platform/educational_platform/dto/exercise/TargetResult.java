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
    private Long targetId;
    private String label;
    private boolean correct;
    private Integer correctValue;
    private Integer givenValue;
}