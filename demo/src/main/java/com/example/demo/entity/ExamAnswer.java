package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 答题记录实体类
 * 记录学生对每道题的答案
 */
@Entity
@Table(name = "exam_answers")
public class ExamAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "考卷ID不能为空")
    @Column(name = "exam_paper_id", nullable = false)
    private Long examPaperId;

    @NotNull(message = "题目ID不能为空")
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "answer_content", columnDefinition = "TEXT")
    private String answerContent;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "score", precision = 10, scale = 2)
    private BigDecimal score = BigDecimal.ZERO;

    @Column(name = "max_score", precision = 10, scale = 2)
    private BigDecimal maxScore;

    @NotNull(message = "评分状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "grading_status", nullable = false)
    private GradingStatus gradingStatus = GradingStatus.PENDING;

    @Column(name = "grading_remarks", columnDefinition = "TEXT")
    private String gradingRemarks;

    @Column(name = "graded_by")
    private Long gradedBy;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "time_spent")
    private Integer timeSpent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_paper_id", insertable = false, updatable = false)
    @JsonIgnore
    private ExamPaper examPaper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    @JsonIgnore
    private Question question;

    // 评分状态枚举
    public enum GradingStatus {
        PENDING("待评分"),
        AUTO_GRADED("自动评分"),
        MANUAL_GRADING("人工评分中"),
        MANUAL_GRADED("人工已评分"),
        COMPLETED("已完成");

        private final String displayName;

        GradingStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 构造函数
    public ExamAnswer() {
    }

    public ExamAnswer(Long examPaperId, Long questionId) {
        this.examPaperId = examPaperId;
        this.questionId = questionId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamPaperId() {
        return examPaperId;
    }

    public void setExamPaperId(Long examPaperId) {
        this.examPaperId = examPaperId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswerContent() {
        return answerContent;
    }

    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public GradingStatus getGradingStatus() {
        return gradingStatus;
    }

    public void setGradingStatus(GradingStatus gradingStatus) {
        this.gradingStatus = gradingStatus;
    }

    public String getGradingRemarks() {
        return gradingRemarks;
    }

    public void setGradingRemarks(String gradingRemarks) {
        this.gradingRemarks = gradingRemarks;
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

    public Integer getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
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

    public ExamPaper getExamPaper() {
        return examPaper;
    }

    public void setExamPaper(ExamPaper examPaper) {
        this.examPaper = examPaper;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
