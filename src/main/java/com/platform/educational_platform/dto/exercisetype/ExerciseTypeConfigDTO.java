package com.platform.educational_platform.dto.exercisetype;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseTypeConfigDTO {
    private Long id;
    private String exerciseType;
    private boolean enabled;
}
