package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考卷实体类
 * 记录学生或团队的考试卷
 */
@Entity
@Table(name = "exam_papers")
public class ExamPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "竞赛ID不能为空")
    @Column(name = "competition_id", nullable = false)
    private Long competitionId;

    @NotNull(message = "参赛类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "participant_type", nullable = false)
    private ParticipantType participantType;

    @NotNull(message = "参赛者ID不能为空")
    @Column(name = "participant_id", nullable = false)
    private Long participantId;

    @NotNull(message = "考卷状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "paper_status", nullable = false)
    private PaperStatus paperStatus = PaperStatus.NOT_STARTED;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    @Column(name = "total_score", precision = 10, scale = 2)
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Column(name = "objective_score", precision = 10, scale = 2)
    private BigDecimal objectiveScore = BigDecimal.ZERO;

    @Column(name = "subjective_score", precision = 10, scale = 2)
    private BigDecimal subjectiveScore = BigDecimal.ZERO;

    @Column(name = "correct_count")
    private Integer correctCount = 0;

    @Column(name = "total_question_count")
    private Integer totalQuestionCount = 0;

    @Column(name = "graded_by")
    private Long gradedBy;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", insertable = false, updatable = false)
    @JsonIgnore
    private Competition competition;

    @OneToMany(mappedBy = "examPaper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ExamAnswer> answers;

    // 参赛类型枚举
    public enum ParticipantType {
        INDIVIDUAL("个人"),
        TEAM("团队");

        private final String displayName;

        ParticipantType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 考卷状态枚举
    public enum PaperStatus {
        NOT_STARTED("未开始"),
        IN_PROGRESS("答题中"),
        SUBMITTED("已提交"),
        GRADING("评分中"),
        GRADED("已评分");

        private final String displayName;

        PaperStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 构造函数
    public ExamPaper() {
    }

    public ExamPaper(Long competitionId, ParticipantType participantType, Long participantId) {
        this.competitionId = competitionId;
        this.participantType = participantType;
        this.participantId = participantId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public ParticipantType getParticipantType() {
        return participantType;
    }

    public void setParticipantType(ParticipantType participantType) {
        this.participantType = participantType;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public PaperStatus getPaperStatus() {
        return paperStatus;
    }

    public void setPaperStatus(PaperStatus paperStatus) {
        this.paperStatus = paperStatus;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public BigDecimal getObjectiveScore() {
        return objectiveScore;
    }

    public void setObjectiveScore(BigDecimal objectiveScore) {
        this.objectiveScore = objectiveScore;
    }

    public BigDecimal getSubjectiveScore() {
        return subjectiveScore;
    }

    public void setSubjectiveScore(BigDecimal subjectiveScore) {
        this.subjectiveScore = subjectiveScore;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Integer getTotalQuestionCount() {
        return totalQuestionCount;
    }

    public void setTotalQuestionCount(Integer totalQuestionCount) {
        this.totalQuestionCount = totalQuestionCount;
    }

    public Long getGradedBy() {
        return gradedBy;
    }

    public void setGradedBy(Long gradedBy) {
        this.gradedBy = gradedBy;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public List<ExamAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ExamAnswer> answers) {
        this.answers = answers;
    }
}
