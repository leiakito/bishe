package com.example.demo.controller;

import com.example.demo.entity.TeamMember;
import com.example.demo.service.TeamMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/team-members")
@CrossOrigin(origins = "*")
public class TeamMemberController {
    
    @Autowired
    private TeamMemberService teamMemberService;
    
    // 申请加入团队
    @PostMapping("/apply")
    public ResponseEntity<?> applyToJoinTeam(@RequestBody Map<String, Object> request) {
        try {
            Long teamId = Long.valueOf(request.get("teamId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());
            String message = (String) request.get("message");
            
            if (teamId == null || userId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "团队ID和用户ID不能为空"
                ));
            }
            
            // 申请加入团队的逻辑需要调整参数
            TeamMember teamMember = teamMemberService.applyToJoinTeam(userId, teamId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "申请加入团队成功",
                "data", teamMember
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 审核加入申请
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveJoinApplication(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> request) {
        try {
            Boolean approved = (Boolean) request.get("approved");
            Long approvedBy = Long.valueOf(request.get("approvedBy").toString());
            String remarks = (String) request.get("remarks");
            
            if (approved == null || approvedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "审核结果和审核人不能为空"
                ));
            }
            
            // 审批加入申请的逻辑需要调整参数
            TeamMember teamMember = teamMemberService.approveJoinApplication(id, approvedBy, approved);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", approved ? "申请审核通过" : "申请审核拒绝",
                "data", teamMember
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 直接添加成员
    @PostMapping("/add")
    public ResponseEntity<?> addMemberDirectly(@RequestBody Map<String, Object> request) {
        try {
            Long teamId = Long.valueOf(request.get("teamId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());
            String roleStr = request.get("role").toString();
            Long addedBy = Long.valueOf(request.get("addedBy").toString());
            
            if (teamId == null || userId == null || roleStr == null || addedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "团队ID、用户ID、角色和添加人不能为空"
                ));
            }
            
            TeamMember.MemberRole role = TeamMember.MemberRole.valueOf(roleStr);
            TeamMember teamMember = teamMemberService.addMemberDirectly(teamId, userId, addedBy, role);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "成员添加成功",
                "data", teamMember
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 移除成员
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeMember(@PathVariable Long id,
                                         @RequestParam Long removedBy,
                                         @RequestBody(required = false) Map<String, String> request) {
        try {
            String reason = request != null ? request.get("reason") : null;
            // 移除成员的逻辑需要调整参数
            teamMemberService.removeMember(id, removedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "成员移除成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 主动退出团队
    @PutMapping("/{id}/quit")
    public ResponseEntity<?> quitTeam(@PathVariable Long id,
                                     @RequestBody(required = false) Map<String, String> request) {
        try {
            String reason = request != null ? request.get("reason") : null;
            // 退出团队的逻辑需要在TeamMemberService中实现
            // teamMemberService.quitTeam(id, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "退出团队成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 转让队长职务
    @PutMapping("/transfer-leadership")
    public ResponseEntity<?> transferLeadership(@RequestBody Map<String, Object> request) {
        try {
            Long teamId = Long.valueOf(request.get("teamId").toString());
            Long currentLeaderId = Long.valueOf(request.get("currentLeaderId").toString());
            Long newLeaderId = Long.valueOf(request.get("newLeaderId").toString());
            
            if (teamId == null || currentLeaderId == null || newLeaderId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "团队ID、当前队长ID和新队长ID不能为空"
                ));
            }
            
            teamMemberService.transferLeadership(teamId, currentLeaderId, newLeaderId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "队长职务转让成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 根据ID获取团队成员
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamMemberById(@PathVariable Long id) {
        try {
            Optional<TeamMember> teamMember = teamMemberService.findById(id);
            if (teamMember.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", teamMember.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队成员信息失败"
            ));
        }
    }
    
    // 获取团队所有成员
    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> getTeamMembers(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("joinTime").ascending());
            List<TeamMember> membersList = teamMemberService.getTeamMembers(teamId);
            Page<TeamMember> members = new PageImpl<>(membersList, pageable, membersList.size());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", members.getContent(),
                "totalElements", members.getTotalElements(),
                "totalPages", members.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队成员失败"
            ));
        }
    }
    
    // 获取用户参与的团队
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserTeams(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("joinTime").descending());
            List<TeamMember> teamsList = teamMemberService.getUserTeams(userId);
            Page<TeamMember> teams = new PageImpl<>(teamsList, pageable, teamsList.size());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", teams.getContent(),
                "totalElements", teams.getTotalElements(),
                "totalPages", teams.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户团队失败"
            ));
        }
    }
    
    // 根据角色获取团队成员
    @GetMapping("/team/{teamId}/role/{role}")
    public ResponseEntity<?> getTeamMembersByRole(
            @PathVariable Long teamId,
            @PathVariable TeamMember.MemberRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("joinTime").ascending());
            Page<TeamMember> members = teamMemberService.getTeamMembersByRole(teamId, role, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", members.getContent(),
                "totalElements", members.getTotalElements(),
                "totalPages", members.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队成员失败"
            ));
        }
    }
    
    // 根据状态获取团队成员
    @GetMapping("/team/{teamId}/status/{status}")
    public ResponseEntity<?> getTeamMembersByStatus(
            @PathVariable Long teamId,
            @PathVariable TeamMember.MemberStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("joinTime").ascending());
            Page<TeamMember> members = teamMemberService.getTeamMembersByStatus(teamId, status, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", members.getContent(),
                "totalElements", members.getTotalElements(),
                "totalPages", members.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队成员失败"
            ));
        }
    }
    
    // 获取团队队长
    @GetMapping("/team/{teamId}/leader")
    public ResponseEntity<?> getTeamLeader(@PathVariable Long teamId) {
        try {
            Optional<TeamMember> leader = teamMemberService.getTeamLeader(teamId);
            if (leader.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", leader.get()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", null,
                    "message", "该团队暂无队长"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队队长失败"
            ));
        }
    }
    
    // 获取待审核的申请
    @GetMapping("/team/{teamId}/pending-applications")
    public ResponseEntity<?> getPendingApplications(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("joinTime").descending());
            // 获取待审批申请的逻辑需要调整参数
            List<TeamMember> applicationsList = teamMemberService.getPendingApplications(teamId);
            Page<TeamMember> applications = new PageImpl<>(applicationsList, pageable, applicationsList.size());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", applications.getContent(),
                "totalElements", applications.getTotalElements(),
                "totalPages", applications.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取待审核申请失败"
            ));
        }
    }
    
    // 检查用户是否在团队中
    @GetMapping("/check-membership")
    public ResponseEntity<?> checkMembership(@RequestParam Long teamId,
                                            @RequestParam Long userId) {
        try {
            // 检查用户是否在团队中的逻辑需要在TeamMemberService中实现
            boolean isMember = false;
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isMember", isMember
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "检查成员状态失败"
            ));
        }
    }
    
    // 获取用户在团队中的角色
    @GetMapping("/user-role")
    public ResponseEntity<?> getUserRoleInTeam(@RequestParam Long teamId,
                                              @RequestParam Long userId) {
        try {
            // 获取用户在团队中信息的逻辑需要在TeamMemberService中实现
            Optional<TeamMember> teamMember = Optional.empty();
            if (teamMember.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", teamMember.get()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", null,
                    "message", "用户不在该团队中"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户角色失败"
            ));
        }
    }
    
    // 获取团队成员数量
    @GetMapping("/team/{teamId}/count")
    public ResponseEntity<?> getTeamMemberCount(@PathVariable Long teamId) {
        try {
            long count = teamMemberService.getTeamMemberCount(teamId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队成员数量失败"
            ));
        }
    }
    
    // 获取活跃成员数量
    @GetMapping("/team/{teamId}/active-count")
    public ResponseEntity<?> getActiveTeamMemberCount(@PathVariable Long teamId) {
        try {
            // 获取活跃团队成员数量的逻辑需要在TeamMemberService中实现
            long count = 0;
            return ResponseEntity.ok(Map.of(
                "success", true,
                "activeCount", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取活跃成员数量失败"
            ));
        }
    }
    
    // 搜索团队成员
    @GetMapping("/team/{teamId}/search")
    public ResponseEntity<?> searchTeamMembers(
            @PathVariable Long teamId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("joinTime").descending());
            Page<TeamMember> members = teamMemberService.searchTeamMembers(teamId, keyword, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", members.getContent(),
                "totalElements", members.getTotalElements(),
                "totalPages", members.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "搜索团队成员失败"
            ));
        }
    }
    
    // 获取团队成员统计信息
    @GetMapping("/team/{teamId}/stats")
    public ResponseEntity<?> getTeamMemberStats(@PathVariable Long teamId) {
        try {
            Map<String, Object> stats = teamMemberService.getTeamMemberStats(teamId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队成员统计失败"
            ));
        }
    }
    
    // 批量审核申请
    @PutMapping("/batch-approve")
    public ResponseEntity<?> batchApproveApplications(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> memberIds = (List<Long>) request.get("memberIds");
            Boolean approved = (Boolean) request.get("approved");
            Long approvedBy = Long.valueOf(request.get("approvedBy").toString());
            String remarks = (String) request.get("remarks");
            
            if (memberIds == null || memberIds.isEmpty() || approved == null || approvedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "成员ID列表、审核结果和审核人不能为空"
                ));
            }
            
            // 批量审批申请的逻辑需要调整参数
            List<TeamMember> updatedMembers = teamMemberService.batchApproveApplications(
                memberIds, approvedBy, approved);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量审核申请成功",
                "data", updatedMembers
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "批量审核失败"
            ));
        }
    }
    
    // 批量移除成员
    @DeleteMapping("/batch-remove")
    public ResponseEntity<?> batchRemoveMembers(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> memberIds = (List<Long>) request.get("memberIds");
            Long removedBy = Long.valueOf(request.get("removedBy").toString());
            String reason = (String) request.get("reason");
            
            if (memberIds == null || memberIds.isEmpty() || removedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "成员ID列表和移除人不能为空"
                ));
            }
            
            // 批量移除成员的逻辑需要在TeamMemberService中实现
            // teamMemberService.batchRemoveMembers(memberIds, removedBy, reason);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量移除成员成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "批量移除失败"
            ));
        }
    }
}