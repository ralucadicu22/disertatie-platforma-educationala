package com.platform.educational_platform.dto.exercise.submission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckElementResponse {
    private String elementId;
    private boolean correct;
}