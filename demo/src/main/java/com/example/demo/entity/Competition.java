package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "competitions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Competition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "竞赛名称不能为空")
    @Size(max = 200, message = "竞赛名称长度不能超过200个字符")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionStatus status = CompetitionStatus.DRAFT;

    @NotNull(message = "报名开始时间不能为空")
    @Column(name = "registration_start_time", nullable = false)
    private LocalDateTime registrationStartTime;

    @NotNull(message = "报名结束时间不能为空")
    @Column(name = "registration_end_time", nullable = false)
    private LocalDateTime registrationEndTime;

    @NotNull(message = "竞赛开始时间不能为空")
    @Column(name = "competition_start_time", nullable = false)
    private LocalDateTime competitionStartTime;

    @NotNull(message = "竞赛结束时间不能为空")
    @Column(name = "competition_end_time", nullable = false)
    private LocalDateTime competitionEndTime;

    @Column(name = "max_team_size")
    private Integer maxTeamSize = 1;

    @Column(name = "min_team_size")
    private Integer minTeamSize = 1;

    @Column(name = "max_teams")
    private Integer maxTeams;

    @Column(name = "registration_fee")
    private Double registrationFee = 0.0;

    @Column(name = "prize_info", columnDefinition = "TEXT")
    private String prizeInfo;

    @Column(name = "rules", columnDefinition = "TEXT")
    private String rules;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "attachment_urls", columnDefinition = "TEXT")
    private String attachmentUrls;

    @Column(name = "location")
    private String location;

    @Column(name = "organizer")
    private String organizer;

    @Column(name = "competition_number", unique = true)
    private String competitionNumber;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "registration_count")
    private Integer registrationCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 多对一关系：竞赛由某个用户创建
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    // 一对多关系：竞赛可以有多个团队参与
    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("competition-teams")
    private List<Team> teams;

    // 一对多关系：竞赛可以有多个报名记录
    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Registration> registrations;

    // 枚举定义
    public enum CompetitionCategory {
        PROGRAMMING, MATHEMATICS, PHYSICS, CHEMISTRY, BIOLOGY, ENGLISH, DESIGN, INNOVATION, OTHER
    }

    public enum CompetitionLevel {
        SCHOOL, CITY, PROVINCE, NATIONAL, INTERNATIONAL
    }

    public enum CompetitionStatus {
        DRAFT, PUBLISHED, REGISTRATION_OPEN, REGISTRATION_CLOSED, IN_PROGRESS, ONGOING, COMPLETED, CANCELLED, PENDING_APPROVAL
    }

    // 构造函数
    public Competition() {}

    public Competition(String name, String description, CompetitionCategory category, 
                      CompetitionLevel level, User creator) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.level = level;
        this.creator = creator;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CompetitionCategory getCategory() {
        return category;
    }

    public void setCategory(CompetitionCategory category) {
        this.category = category;
    }

    public CompetitionLevel getLevel() {
        return level;
    }

    public void setLevel(CompetitionLevel level) {
        this.level = level;
    }

    public CompetitionStatus getStatus() {
        return status;
    }

    public void setStatus(CompetitionStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegistrationStartTime() {
        return registrationStartTime;
    }

    public void setRegistrationStartTime(LocalDateTime registrationStartTime) {
        this.registrationStartTime = registrationStartTime;
    }

    public LocalDateTime getRegistrationEndTime() {
        return registrationEndTime;
    }

    public void setRegistrationEndTime(LocalDateTime registrationEndTime) {
        this.registrationEndTime = registrationEndTime;
    }

    public LocalDateTime getCompetitionStartTime() {
        return competitionStartTime;
    }

    public void setCompetitionStartTime(LocalDateTime competitionStartTime) {
        this.competitionStartTime = competitionStartTime;
    }

    public LocalDateTime getCompetitionEndTime() {
        return competitionEndTime;
    }

    public void setCompetitionEndTime(LocalDateTime competitionEndTime) {
        this.competitionEndTime = competitionEndTime;
    }
    
    // 别名方法，用于向后兼容
    public void setStartTime(LocalDateTime startTime) {
        this.competitionStartTime = startTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.competitionEndTime = endTime;
    }
    
    public LocalDateTime getStartTime() {
        return competitionStartTime;
    }
    
    public LocalDateTime getEndTime() {
        return competitionEndTime;
    }

    public Integer getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(Integer maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public Integer getMinTeamSize() {
        return minTeamSize;
    }

    public void setMinTeamSize(Integer minTeamSize) {
        this.minTeamSize = minTeamSize;
    }

    public Integer getMaxTeams() {
        return maxTeams;
    }

    public void setMaxTeams(Integer maxTeams) {
        this.maxTeams = maxTeams;
    }
    
    // 别名方法，用于向后兼容
    public void setMaxParticipants(Integer maxParticipants) {
        this.maxTeams = maxParticipants;
    }
    
    public Integer getMaxParticipants() {
        return maxTeams;
    }

    public Double getRegistrationFee() {
        return registrationFee;
    }

    public void setRegistrationFee(Double registrationFee) {
        this.registrationFee = registrationFee;
    }

    public String getPrizeInfo() {
        return prizeInfo;
    }

    public void setPrizeInfo(String prizeInfo) {
        this.prizeInfo = prizeInfo;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getAttachmentUrls() {
        return attachmentUrls;
    }

    public void setAttachmentUrls(String attachmentUrls) {
        this.attachmentUrls = attachmentUrls;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getCompetitionNumber() {
        return competitionNumber;
    }

    public void setCompetitionNumber(String competitionNumber) {
        this.competitionNumber = competitionNumber;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getRegistrationCount() {
        return registrationCount;
    }

    public void setRegistrationCount(Integer registrationCount) {
        this.registrationCount = registrationCount;
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    // 别名方法，用于向后兼容
    public User getCreatedBy() {
        return creator;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }
}