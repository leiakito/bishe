package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目实体类
 */
@Entity
@Table(name = "questions")
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "题目标题不能为空")
    @Column(nullable = false, length = 500)
    private String title;
    
    @NotBlank(message = "题目内容不能为空")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @NotNull(message = "题目类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;
    
    @NotNull(message = "题目难度不能为空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionDifficulty difficulty;
    
    @NotNull(message = "题目分类不能为空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionCategory category;
    
    // 选择题选项（JSON格式存储）
    @Column(columnDefinition = "TEXT")
    private String options;
    
    // 正确答案
    @Column(columnDefinition = "TEXT")
    private String correctAnswer;
    
    // 答案解析
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    // 题目分值
    @Column(nullable = false)
    private Integer score = 10;
    
    // 题目标签（多个标签用逗号分隔）
    @Column(length = 500)
    private String tags;
    
    // 创建者ID
    @Column(nullable = false)
    private Long createdBy;
    
    // 题目状态
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionStatus status = QuestionStatus.DRAFT;
    
    // 使用次数
    @Column(nullable = false)
    private Integer usageCount = 0;
    
    // 创建时间
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 更新时间
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // 题目类型枚举
    public enum QuestionType {
        SINGLE_CHOICE("单选题"),
        MULTIPLE_CHOICE("多选题"),
        TRUE_FALSE("判断题"),
        FILL_BLANK("填空题"),
        SHORT_ANSWER("简答题"),
        ESSAY("论述题"),
        PROGRAMMING("编程题");
        
        private final String displayName;
        
        QuestionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 题目难度枚举
    public enum QuestionDifficulty {
        EASY("简单"),
        MEDIUM("中等"),
        HARD("困难"),
        EXPERT("专家级");
        
        private final String displayName;
        
        QuestionDifficulty(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 题目分类枚举
    public enum QuestionCategory {
        PROGRAMMING("编程"),
        MATHEMATICS("数学"),
        PHYSICS("物理"),
        CHEMISTRY("化学"),
        BIOLOGY("生物"),
        ENGLISH("英语"),
        COMPUTER_SCIENCE("计算机科学"),
        DATA_STRUCTURE("数据结构"),
        ALGORITHM("算法"),
        DATABASE("数据库"),
        NETWORK("网络"),
        SOFTWARE_ENGINEERING("软件工程"),
        OPERATING_SYSTEM("操作系统"),
        COMPUTER_ARCHITECTURE("计算机组成原理"),
        COMPILER_PRINCIPLE("编译原理"),
        ARTIFICIAL_INTELLIGENCE("人工智能"),
        MACHINE_LEARNING("机器学习"),
        WEB_DEVELOPMENT("Web开发"),
        MOBILE_DEVELOPMENT("移动开发"),
        CLOUD_COMPUTING("云计算"),
        CYBERSECURITY("网络安全"),
        DESIGN("设计竞赛"),
        // 编程语言分类
        PYTHON("Python编程"),
        JAVA("Java编程"),
        JAVASCRIPT("JavaScript编程"),
        CPP("C++编程"),
        C("C语言编程"),
        CSHARP("C#编程"),
        GO("Go语言编程"),
        RUST("Rust编程"),
        PHP("PHP编程"),
        RUBY("Ruby编程"),
        OTHER("其他");
        
        private final String displayName;
        
        QuestionCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 题目状态枚举
    public enum QuestionStatus {
        DRAFT("草稿"),
        PUBLISHED("已发布"),
        ARCHIVED("已归档");
        
        private final String displayName;
        
        QuestionStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 构造函数
    public Question() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public QuestionType getType() {
        return type;
    }
    
    public void setType(QuestionType type) {
        this.type = type;
    }
    
    public QuestionDifficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(QuestionDifficulty difficulty) {
        this.difficulty = difficulty;
    }
    
    public QuestionCategory getCategory() {
        return category;
    }
    
    public void setCategory(QuestionCategory category) {
        this.category = category;
    }
    
    public String getOptions() {
        return options;
    }
    
    public void setOptions(String options) {
        this.options = options;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public QuestionStatus getStatus() {
        return status;
    }
    
    public void setStatus(QuestionStatus status) {
        this.status = status;
    }
    
    public Integer getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
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
}