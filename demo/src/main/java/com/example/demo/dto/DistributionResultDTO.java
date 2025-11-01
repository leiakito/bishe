package com.example.demo.dto;

/**
 * 考卷下发结果DTO
 */
public class DistributionResultDTO {
    private Integer individualPapers = 0;
    private Integer teamPapers = 0;
    private Integer totalParticipants = 0;

    // Getters and Setters
    public Integer getIndividualPapers() {
        return individualPapers;
    }

    public void setIndividualPapers(Integer individualPapers) {
        this.individualPapers = individualPapers;
    }

    public Integer getTeamPapers() {
        return teamPapers;
    }

    public void setTeamPapers(Integer teamPapers) {
        this.teamPapers = teamPapers;
    }

    public Integer getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(Integer totalParticipants) {
        this.totalParticipants = totalParticipants;
    }
}
