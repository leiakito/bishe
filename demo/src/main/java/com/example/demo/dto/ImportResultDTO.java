package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果DTO
 */
public class ImportResultDTO {
    private Integer successCount = 0;
    private Integer failedCount = 0;
    private List<FailedReason> failedReasons = new ArrayList<>();
    private List<Long> importedQuestionIds = new ArrayList<>();

    // Getters and Setters
    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public List<FailedReason> getFailedReasons() {
        return failedReasons;
    }

    public void setFailedReasons(List<FailedReason> failedReasons) {
        this.failedReasons = failedReasons;
    }

    public List<Long> getImportedQuestionIds() {
        return importedQuestionIds;
    }

    public void setImportedQuestionIds(List<Long> importedQuestionIds) {
        this.importedQuestionIds = importedQuestionIds;
    }

    /**
     * 失败原因
     */
    public static class FailedReason {
        private Integer line;
        private String reason;

        public FailedReason() {
        }

        public FailedReason(Integer line, String reason) {
            this.line = line;
            this.reason = reason;
        }

        public Integer getLine() {
            return line;
        }

        public void setLine(Integer line) {
            this.line = line;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
