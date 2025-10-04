package com.example.demo.dto;

import com.example.demo.entity.Team;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TeamDTO {
    private Long id;
    private String name;
    private String description;
    private String status;
    private String inviteCode;
    private Integer maxMembers;
    private Integer currentMembers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> competition;
    private Map<String, Object> leader;

    public TeamDTO() {}

    public TeamDTO(Team team) {
        System.out.println("=== 创建TeamDTO ===");
        System.out.println("Team ID: " + team.getId());
        System.out.println("Team Name: " + team.getName());

        this.id = team.getId();
        this.name = team.getName();
        this.description = team.getDescription();
        this.status = team.getStatus() != null ? team.getStatus().toString() : null;
        this.inviteCode = team.getInviteCode();
        this.maxMembers = team.getMaxMembers();
        this.currentMembers = team.getCurrentMembers();
        this.createdAt = team.getCreatedAt();
        this.updatedAt = team.getUpdatedAt();

        // 手动构建 competition 对象
        try {
            System.out.println("尝试加载 competition...");
            if (team.getCompetition() != null) {
                System.out.println("Competition 不为空");
                this.competition = new HashMap<>();
                this.competition.put("id", team.getCompetition().getId());
                this.competition.put("name", team.getCompetition().getName());
                System.out.println("Competition 已加载: ID=" + team.getCompetition().getId() + ", Name=" + team.getCompetition().getName());
            } else {
                System.out.println("警告: Competition 为 null!");
                this.competition = null;
            }
        } catch (org.hibernate.LazyInitializationException e) {
            System.err.println("LazyInitializationException: Competition 未被加载（延迟加载异常）");
            e.printStackTrace();
            this.competition = null;
        } catch (Exception e) {
            System.err.println("加载 competition 失败: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            this.competition = null;
        }

        // 手动构建 leader 对象
        try {
            System.out.println("尝试加载 leader...");
            if (team.getLeader() != null) {
                System.out.println("Leader 不为空");
                this.leader = new HashMap<>();
                this.leader.put("id", team.getLeader().getId());
                this.leader.put("username", team.getLeader().getUsername());
                this.leader.put("realName", team.getLeader().getRealName());
                System.out.println("Leader 已加载: ID=" + team.getLeader().getId() + ", Username=" + team.getLeader().getUsername());
            } else {
                System.out.println("警告: Leader 为 null!");
                this.leader = null;
            }
        } catch (org.hibernate.LazyInitializationException e) {
            System.err.println("LazyInitializationException: Leader 未被加载（延迟加载异常）");
            e.printStackTrace();
            this.leader = null;
        } catch (Exception e) {
            System.err.println("加载 leader 失败: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            this.leader = null;
        }

        System.out.println("TeamDTO 创建完成: competition=" + (this.competition != null ? "已加载" : "null") + ", leader=" + (this.leader != null ? "已加载" : "null"));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }

    public Integer getCurrentMembers() { return currentMembers; }
    public void setCurrentMembers(Integer currentMembers) { this.currentMembers = currentMembers; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Object> getCompetition() { return competition; }
    public void setCompetition(Map<String, Object> competition) { this.competition = competition; }

    public Map<String, Object> getLeader() { return leader; }
    public void setLeader(Map<String, Object> leader) { this.leader = leader; }
}
