package com.platform.educational_platform.dto;

import com.platform.educational_platform.model.enums.UserRole;
import lombok.*;
import java.util.List;

public class ParentChildDTO {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LinkRequest {
        private Long parentId;
        private Long childId;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PersonInfo {
        private Long id;
        private String name;
        private String email;
        private UserRole role;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParentWithChildren {
        private Long parentId;
        private String parentName;
        private String parentEmail;
        private List<PersonInfo> children;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChildWithParents {
        private Long childId;
        private String childName;
        private String childEmail;
        private List<PersonInfo> parents;
    }
}