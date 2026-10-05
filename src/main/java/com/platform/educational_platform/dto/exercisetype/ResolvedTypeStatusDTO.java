package com.platform.educational_platform.dto.exercisetype;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolvedTypeStatusDTO {
    private String exerciseType;
    private boolean globalEnabled;
    private Boolean productOverride;
    private boolean effectiveEnabled;
}
