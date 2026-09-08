package com.platform.educational_platform.dto;

import com.platform.educational_platform.model.enums.HintType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HintDTO {
    private String text;
    private Integer afterAttempt;
    private HintType type;
    private String targetElementId;
}