package com.platform.educational_platform.dto.config;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class FindAndCountConfig {
    private String instruction;
    private String imageUrl;
    private List<Target> targets;
    private List<HintConfig> hints;
    private String explanation;

    @Data
    public static class Target {
        private String id;
        private String label;
        private String imageUrl;
        private int correct;
    }
}