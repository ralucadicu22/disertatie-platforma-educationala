package com.platform.educational_platform.dto.config;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindAndCountConfig {
    private List<Target> targets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Target {
        private Long id;
        private String label;
        private String imageUrl;
        private Integer correct;
    }
}