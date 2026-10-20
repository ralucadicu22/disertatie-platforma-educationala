package com.platform.educational_platform.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AtRiskDTO {

    public static class AtRiskStudent {
        private Long          userId;
        private String        userName;
        private String        userEmail;
        private List<String> riskFlags;
        private String        riskLevel;
        private LocalDateTime lastActivity;
        private Double        successRate;
        private Integer       totalAttempts;
        private Integer       abandonedQuizzes;

        public AtRiskStudent() {}

        public AtRiskStudent(Long userId, String userName, String userEmail,
                             List<String> riskFlags, String riskLevel,
                             LocalDateTime lastActivity, Double successRate,
                             Integer totalAttempts, Integer abandonedQuizzes) {
            this.userId           = userId;
            this.userName         = userName;
            this.userEmail        = userEmail;
            this.riskFlags        = riskFlags;
            this.riskLevel        = riskLevel;
            this.lastActivity     = lastActivity;
            this.successRate      = successRate;
            this.totalAttempts    = totalAttempts;
            this.abandonedQuizzes = abandonedQuizzes;
        }

        public Long          getUserId()           { return userId; }
        public String        getUserName()         { return userName; }
        public String        getUserEmail()        { return userEmail; }
        public List<String>  getRiskFlags()        { return riskFlags; }
        public String        getRiskLevel()        { return riskLevel; }
        public LocalDateTime getLastActivity()     { return lastActivity; }
        public Double        getSuccessRate()      { return successRate; }
        public Integer       getTotalAttempts()    { return totalAttempts; }
        public Integer       getAbandonedQuizzes() { return abandonedQuizzes; }
    }
}