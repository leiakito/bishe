package com.example.demo.dto;

import com.example.demo.entity.TeamMember;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TeamMemberDTO {
    private Long id;
    private String role;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private String joinReason;
    private Map<String, Object> user;

    public TeamMemberDTO() {}

    public TeamMemberDTO(TeamMember member) {
        System.out.println("=== 创建TeamMemberDTO ===");
        System.out.println("Member ID: " + member.getId());

        this.id = member.getId();
        this.role = member.getRole() != null ? member.getRole().toString() : null;
        this.status = member.getStatus() != null ? member.getStatus().toString() : null;
        this.joinedAt = member.getJoinedAt();
        this.leftAt = member.getLeftAt();
        this.joinReason = member.getJoinReason();

        // 手动构建 user 对象
        try {
            System.out.println("尝试加载 user...");
            if (member.getUser() != null) {
                System.out.println("User 不为空");
                this.user = new HashMap<>();
                this.user.put("id", member.getUser().getId());
                this.user.put("username", member.getUser().getUsername());
                this.user.put("realName", member.getUser().getRealName());
                this.user.put("email", member.getUser().getEmail());
                this.user.put("studentId", member.getUser().getStudentId());
                this.user.put("department", member.getUser().getDepartment());
                this.user.put("phoneNumber", member.getUser().getPhoneNumber());
                this.user.put("role", member.getUser().getRole() != null ?
                    member.getUser().getRole().toString() : null);
                this.user.put("status", member.getUser().getStatus() != null ?
                    member.getUser().getStatus().toString() : null);
                System.out.println("User 已加载: ID=" + member.getUser().getId() + ", Username=" + member.getUser().getUsername());
            } else {
                System.out.println("警告: User 为 null!");
                this.user = null;
            }
        } catch (org.hibernate.LazyInitializationException e) {
            System.err.println("LazyInitializationException: User 未被加载（延迟加载异常）");
            e.printStackTrace();
            this.user = null;
        } catch (Exception e) {
            System.err.println("加载 user 失败: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            this.user = null;
        }

        System.out.println("TeamMemberDTO 创建完成: user=" + (this.user != null ? "已加载" : "null"));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getLeftAt() { return leftAt; }
    public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }

    public String getJoinReason() { return joinReason; }
    public void setJoinReason(String joinReason) { this.joinReason = joinReason; }

    public Map<String, Object> getUser() { return user; }
    public void setUser(Map<String, Object> user) { this.user = user; }
}
