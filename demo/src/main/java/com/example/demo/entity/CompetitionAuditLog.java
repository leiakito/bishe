package com.example.demo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "competition_audit_logs")
public class CompetitionAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 多对一关系：审核日志属于某个竞赛
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    // 多对一关系：审核由某个用户执行
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private Competition.CompetitionStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private Competition.CompetitionStatus newStatus;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 审核操作枚举
    public enum AuditAction {
        APPROVE,    // 通过审核
        REJECT,     // 拒绝审核
        MODIFY,     // 修改状态
        SUBMIT      // 提交审核
    }

    // 构造函数
    public CompetitionAuditLog() {}

    public CompetitionAuditLog(Competition competition, User reviewer, AuditAction action, 
                              Competition.CompetitionStatus oldStatus, Competition.CompetitionStatus newStatus, String remarks) {
        this.competition = competition;
        this.reviewer = reviewer;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.remarks = remarks;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public Competition.CompetitionStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(Competition.CompetitionStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public Competition.CompetitionStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(Competition.CompetitionStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}