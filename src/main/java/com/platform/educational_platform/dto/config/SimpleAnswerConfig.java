package com.platform.educational_platform.dto.config;

import lombok.Data;
import java.util.List;

@Data
public class SimpleAnswerConfig {
    private String instruction;
    private String correctAnswer;
    private List<String> alternativeAnswers;
    private List<HintConfig> hints;
    private String explanation;
}