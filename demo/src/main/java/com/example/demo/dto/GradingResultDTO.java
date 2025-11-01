package com.example.demo.dto;

import java.math.BigDecimal;

/**
 * 评分结果DTO
 */
public class GradingResultDTO {
    private BigDecimal objectiveScore = BigDecimal.ZERO;
    private Integer correctCount = 0;
    private Boolean needManualGrading = false;

    // Getters and Setters
    public BigDecimal getObjectiveScore() {
        return objectiveScore;
    }

    public void setObjectiveScore(BigDecimal objectiveScore) {
        this.objectiveScore = objectiveScore;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Boolean getNeedManualGrading() {
        return needManualGrading;
    }

    public void setNeedManualGrading(Boolean needManualGrading) {
        this.needManualGrading = needManualGrading;
    }
}
