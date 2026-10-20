package com.platform.educational_platform.dto;

import java.util.List;

public class RecommendationDTO {

    public enum RecommendationType {
        WARNING,
        INFO,
        ACTION
    }

    public static class Recommendation {
        private RecommendationType type;
        private String             title;
        private String             message;
        private String             actionLabel;
        private Long               targetId;

        public Recommendation() {}

        public Recommendation(RecommendationType type, String title,
                              String message, String actionLabel, Long targetId) {
            this.type        = type;
            this.title       = title;
            this.message     = message;
            this.actionLabel = actionLabel;
            this.targetId    = targetId;
        }

        public RecommendationType getType()        { return type; }
        public String             getTitle()       { return title; }
        public String             getMessage()     { return message; }
        public String             getActionLabel() { return actionLabel; }
        public Long               getTargetId()    { return targetId; }
    }

    public static class StudentRecommendations {
        private Long                 userId;
        private String               userName;
        private List<Recommendation> recommendations;

        public StudentRecommendations() {}

        public StudentRecommendations(Long userId, String userName,
                                      List<Recommendation> recommendations) {
            this.userId          = userId;
            this.userName        = userName;
            this.recommendations = recommendations;
        }

        public Long                 getUserId()          { return userId; }
        public String               getUserName()        { return userName; }
        public List<Recommendation> getRecommendations() { return recommendations; }
    }
}