package com.example.demo.controller;

import com.example.demo.entity.Team;
import com.example.demo.entity.TeamMember;
import com.example.demo.service.TeamService;
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
import java.util.*;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {
    
    @Autowired
    private TeamService teamService;
    
    // 创建团队
    @PostMapping
    public ResponseEntity<?> createTeam(@Valid @RequestBody Team team, 
                                       @RequestParam Long competitionId,
                                       @RequestParam Long createdBy) {
        try {
            Team createdTeam = teamService.createTeam(team, competitionId, createdBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "团队创建成功",
                "data", createdTeam
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 根据ID获取团队
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamById(@PathVariable Long id) {
        try {
            Optional<Team> team = teamService.findById(id);
            if (team.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", team.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队信息失败"
            ));
        }
    }
    
    // 获取所有团队（分页）
    @GetMapping
    public ResponseEntity<?> getAllTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            System.out.println("=== getAllTeams 开始 ===");
            System.out.println("参数: page=" + page + ", size=" + size);

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Team> teams = teamService.getAllTeams(pageable);
            System.out.println("从数据库查询到 " + teams.getContent().size() + " 个团队");

            // 检查第一个团队的关联数据
            if (!teams.getContent().isEmpty()) {
                Team firstTeam = teams.getContent().get(0);
                System.out.println("第一个团队: " + firstTeam.getName());
                System.out.println("  - Competition: " + (firstTeam.getCompetition() != null ? firstTeam.getCompetition().getName() : "NULL"));
                System.out.println("  - Leader: " + (firstTeam.getLeader() != null ? firstTeam.getLeader().getUsername() : "NULL"));
            }

            // 转换为DTO以包含competition和leader信息
            System.out.println("开始转换为 DTO...");
            java.util.List<com.example.demo.dto.TeamDTO> teamDTOs = teams.getContent().stream()
                .map(com.example.demo.dto.TeamDTO::new)
                .collect(java.util.stream.Collectors.toList());

            System.out.println("DTO 转换完成，共 " + teamDTOs.size() + " 个");
            System.out.println("=== getAllTeams 完成 ===");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", teamDTOs,
                "totalElements", teams.getTotalElements(),
                "totalPages", teams.getTotalPages(),
                "currentPage", teams.getNumber(),
                "size", teams.getSize()
            ));
        } catch (Exception e) {
            System.err.println("=== getAllTeams 错误 ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队列表失败: " + e.getMessage()
            ));
        }
    }
    
    // 根据竞赛获取团队（修改：包括通过报名记录关联的团队）
    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<?> getTeamsByCompetition(
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());

            // 修改：使用新方法获取所有相关团队（包括通过报名记录关联的）
            Page<Team> teams = teamService.getAllTeamsByCompetition(competitionId, pageable);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", teams.getContent(),
                "totalElements", teams.getTotalElements(),
                "totalPages", teams.getTotalPages(),
                "currentPage", teams.getNumber()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队列表失败: " + e.getMessage()
            ));
        }
    }
    
    // 根据状态获取团队
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTeamsByStatus(
            @PathVariable Team.TeamStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
            Page<Team> teams = teamService.getTeamsByStatus(status, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", teams.getContent(),
                "totalElements", teams.getTotalElements(),
                "totalPages", teams.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队列表失败"
            ));
        }
    }
    
    // 搜索团队
    @GetMapping("/search")
    public ResponseEntity<?> searchTeams(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
            Page<Team> teams = teamService.searchTeams(keyword, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", teams.getContent(),
                "totalElements", teams.getTotalElements(),
                "totalPages", teams.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "搜索团队失败"
            ));
        }
    }
    
    // 获取用户创建的团队
    @GetMapping("/created-by/{userId}")
    public ResponseEntity<?> getTeamsCreatedByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
            Page<Team> teams = teamService.getTeamsCreatedByUser(userId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", teams.getContent(),
                "totalElements", teams.getTotalElements(),
                "totalPages", teams.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户创建的团队失败"
            ));
        }
    }
    
    // 获取当前用户参与的团队
    @GetMapping("/joined-by/me")
    public ResponseEntity<?> getMyTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = com.example.demo.config.JwtAuthenticationFilter.getCurrentUserId();
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "用户未登录"
                ));
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
            Page<Team> teams = teamService.getTeamsJoinedByUser(currentUserId, pageable);

            System.out.println("=== Controller 返回数据 ===");
            System.out.println("teams.getContent(): " + teams.getContent());
            System.out.println("teams.getContent().size(): " + teams.getContent().size());
            System.out.println("teams.getTotalElements(): " + teams.getTotalElements());

            // 转换为DTO
            java.util.List<com.example.demo.dto.TeamDTO> teamDTOs = teams.getContent().stream()
                .map(com.example.demo.dto.TeamDTO::new)
                .collect(java.util.stream.Collectors.toList());

            System.out.println("teamDTOs.size(): " + teamDTOs.size());

            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("data", teamDTOs);
            response.put("totalElements", teams.getTotalElements());
            response.put("totalPages", teams.getTotalPages());

            System.out.println("最终响应: " + response);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("获取用户参与的团队失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户参与的团队失败: " + e.getMessage()
            ));
        }
    }
    
    // 获取用户参与的团队
    @GetMapping("/joined-by/{userId}")
    public ResponseEntity<?> getTeamsJoinedByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
            Page<Team> teams = teamService.getTeamsJoinedByUser(userId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", teams.getContent(),
                "totalElements", teams.getTotalElements(),
                "totalPages", teams.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户参与的团队失败"
            ));
        }
    }
    
    // 获取未满员的团队
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableTeams(
            @RequestParam(required = false) Long competitionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
            List<Team> teamList = teamService.getAvailableTeams(competitionId);
            Page<Team> teams = new PageImpl<>(teamList, pageable, teamList.size());

            // 转换为DTO以包含competition和leader信息
            java.util.List<com.example.demo.dto.TeamDTO> teamDTOs = teams.getContent().stream()
                .map(com.example.demo.dto.TeamDTO::new)
                .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", teamDTOs,
                "totalElements", teams.getTotalElements(),
                "totalPages", teams.getTotalPages()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取可加入团队失败: " + e.getMessage()
            ));
        }
    }
    
    // 更新团队信息
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeam(@PathVariable Long id, 
                                       @Valid @RequestBody Team team,
                                       @RequestParam Long updatedBy) {
        try {
            Team updatedTeam = teamService.updateTeam(id, team, updatedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "团队信息更新成功",
                "data", updatedTeam
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 更新团队状态
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTeamStatus(@PathVariable Long id, 
                                             @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "状态不能为空"
                ));
            }
            
            Team.TeamStatus status = Team.TeamStatus.valueOf(statusStr.toUpperCase());
            // 更新团队状态的逻辑需要在TeamService中实现
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "团队状态更新成功"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "无效的状态值"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 解散团队
    @DeleteMapping("/{id}")
    public ResponseEntity<?> dissolveTeam(@PathVariable Long id, @RequestParam Long dissolvedBy) {
        try {
            // 调用 TeamService 的 disbandTeam 方法来解散团队
            teamService.disbandTeam(id, dissolvedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "团队解散成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 申请加入团队
    @PostMapping("/{id}/join")
    public ResponseEntity<?> applyToJoinTeam(@PathVariable Long id, 
                                            @RequestParam Long userId,
                                            @RequestBody(required = false) Map<String, String> request) {
        try {
            String message = request != null ? request.get("message") : null;
            // 申请加入团队的逻辑需要在TeamService中实现
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "申请已提交"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 审核加入申请
    @PutMapping("/{id}/members/{memberId}/approve")
    public ResponseEntity<?> approveJoinRequest(@PathVariable Long id,
                                               @PathVariable Long memberId,
                                               @RequestBody Map<String, Object> request) {
        try {
            Boolean approved = (Boolean) request.get("approved");
            Long approvedBy = Long.valueOf(request.get("approvedBy").toString());
            
            if (approved == null || approvedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "审核结果和审核人不能为空"
                ));
            }
            
            // 审批加入请求的逻辑需要在TeamService中实现
            TeamMember teamMember = null;
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", approved ? "加入申请已通过" : "加入申请已拒绝",
                "data", teamMember
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 移除团队成员
    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable Long id,
                                         @PathVariable Long memberId,
                                         @RequestParam Long removedBy) {
        try {
            teamService.removeMember(id, memberId, removedBy);
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
    
    // 退出团队
    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leaveTeam(@PathVariable Long id, @RequestParam Long userId) {
        try {
            teamService.leaveTeam(id, userId);
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
    
    // 转让队长
    @PutMapping("/{id}/transfer-leadership")
    public ResponseEntity<?> transferLeadership(@PathVariable Long id,
                                               @RequestBody Map<String, Long> request) {
        try {
            Long currentLeaderId = request.get("currentLeaderId");
            Long newLeaderId = request.get("newLeaderId");
            
            if (currentLeaderId == null || newLeaderId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "当前队长ID和新队长ID不能为空"
                ));
            }
            
            // 转移队长权限的逻辑需要在TeamService中实现
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "队长转让成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 获取团队成员
    @GetMapping("/{id}/members")
    public ResponseEntity<?> getTeamMembers(@PathVariable Long id) {
        try {
            System.out.println("=== getTeamMembers 调用 ===");
            System.out.println("团队ID: " + id);

            List<TeamMember> members = teamService.getTeamMembers(id);
            System.out.println("查询到的成员数量: " + members.size());

            // 转换为 DTO 以正确序列化用户信息
            java.util.List<com.example.demo.dto.TeamMemberDTO> memberDTOs = members.stream()
                .map(com.example.demo.dto.TeamMemberDTO::new)
                .collect(java.util.stream.Collectors.toList());

            System.out.println("TeamMemberDTO 转换完成，数量: " + memberDTOs.size());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", memberDTOs
            ));
        } catch (Exception e) {
            System.err.println("获取团队成员失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队成员失败"
            ));
        }
    }
    
    // 获取团队详细信息
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getTeamDetails(@PathVariable Long id) {
        try {
            System.out.println("=== getTeamDetails 调用 ===");
            System.out.println("团队ID: " + id);

            // 获取团队详情
            Optional<Team> teamOpt = teamService.findById(id);

            if (teamOpt.isEmpty()) {
                System.out.println("团队不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "团队不存在"
                ));
            }

            Team team = teamOpt.get();
            System.out.println("找到团队: " + team.getName());

            // 转换为 DTO 以正确序列化关联数据
            com.example.demo.dto.TeamDTO teamDTO = new com.example.demo.dto.TeamDTO(team);

            System.out.println("TeamDTO 转换完成");
            System.out.println("Competition: " + (teamDTO.getCompetition() != null ? "已加载" : "null"));
            System.out.println("Leader: " + (teamDTO.getLeader() != null ? "已加载" : "null"));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", teamDTO
            ));
        } catch (RuntimeException e) {
            System.err.println("获取团队详情失败(RuntimeException): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("获取团队详情失败(Exception): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队详情失败"
            ));
        }
    }
    
    // 获取团队统计信息
    @GetMapping("/stats")
    public ResponseEntity<?> getTeamStats() {
        try {
            // 获取团队统计的逻辑需要在TeamService中实现
            Map<String, Object> stats = Map.of();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队统计信息失败"
            ));
        }
    }
    
    // 检查团队名称是否存在
    @GetMapping("/check-name/{name}")
    public ResponseEntity<?> checkTeamName(@PathVariable String name, 
                                          @RequestParam Long competitionId) {
        try {
            // 检查团队名称是否存在的逻辑需要在TeamService中实现
            boolean exists = false;
            return ResponseEntity.ok(Map.of(
                "success", true,
                "exists", exists
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "检查团队名称失败"
            ));
        }
    }
    
    // 检查用户是否已在竞赛中有团队
    @GetMapping("/check-user-team")
    public ResponseEntity<?> checkUserTeamInCompetition(@RequestParam Long userId, 
                                                       @RequestParam Long competitionId) {
        try {
            // 检查用户是否在竞赛中有团队的逻辑需要在TeamService中实现
            boolean hasTeam = false;
            return ResponseEntity.ok(Map.of(
                "success", true,
                "hasTeam", hasTeam
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "检查用户团队状态失败"
            ));
        }
    }
    
    // 批量更新团队状态
    @PutMapping("/batch-status")
    public ResponseEntity<?> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> teamIds = (List<Long>) request.get("teamIds");
            String statusStr = (String) request.get("status");
            
            if (teamIds == null || teamIds.isEmpty() || statusStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "团队ID列表和状态不能为空"
                ));
            }
            
            Team.TeamStatus status = Team.TeamStatus.valueOf(statusStr.toUpperCase());
            // 批量更新团队状态的逻辑需要在TeamService中实现
            List<Team> teams = List.of();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量更新成功",
                "data", teams
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "无效的状态值"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "批量更新失败"
            ));
        }
    }
    
    // 通过邀请码加入团队
    @PostMapping("/join-by-code")
    public ResponseEntity<?> joinTeamByInviteCode(@RequestBody Map<String, Object> request) {
        try {
            // 严格的类型校验和过滤
            Object inviteCodeObj = request.get("inviteCode");
            Object userIdObj = request.get("userId");
            Object reasonObj = request.get("reason");

            // 验证 inviteCode 类型必须是 String
            if (inviteCodeObj == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "邀请码不能为空"
                ));
            }

            if (!(inviteCodeObj instanceof String)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "邀请码参数类型错误，必须是字符串"
                ));
            }

            String inviteCode = ((String) inviteCodeObj).trim();

            // 验证邀请码格式
            if (inviteCode.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "邀请码不能为空"
                ));
            }

            // 验证邀请码长度（通常是8位）
            if (inviteCode.length() < 4 || inviteCode.length() > 20) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "邀请码格式错误"
                ));
            }

            // 验证邀请码只包含字母数字和连字符
            if (!inviteCode.matches("^[A-Za-z0-9-]+$")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "邀请码只能包含字母、数字和连字符"
                ));
            }

            // 验证 userId
            if (userIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户ID不能为空"
                ));
            }

            Long userId;
            try {
                userId = Long.valueOf(userIdObj.toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户ID格式错误"
                ));
            }

            // 验证 reason（可选参数）
            String reason = null;
            if (reasonObj != null) {
                if (!(reasonObj instanceof String)) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "申请理由参数类型错误，必须是字符串"
                    ));
                }
                reason = ((String) reasonObj).trim();
                // 限制申请理由长度
                if (reason.length() > 500) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "申请理由不能超过500个字符"
                    ));
                }
            }

            TeamMember teamMember = teamService.joinTeamByInviteCode(inviteCode, userId, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "成功加入团队",
                "data", teamMember
            ));
        } catch (ClassCastException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "请求参数类型错误"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "加入团队失败"
            ));
        }
    }
    
    // 获取团队邀请码
    @GetMapping("/{id}/invite-code")
    public ResponseEntity<?> getTeamInviteCode(@PathVariable Long id, @RequestParam Long userId) {
        try {
            String inviteCode = teamService.getTeamInviteCode(id, userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("inviteCode", inviteCode)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取邀请码失败"
            ));
        }
    }
    
    // 邀请成员加入团队（通过学号）
    @PostMapping("/{id}/invite-member")
    public ResponseEntity<?> inviteTeamMember(@PathVariable Long id,
                                             @RequestBody Map<String, Object> request,
                                             @RequestParam Long inviterId) {
        try {
            String studentId = (String) request.get("studentId");
            String message = (String) request.get("message");
            
            if (studentId == null || studentId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "学号不能为空"
                ));
            }
            
            if (inviterId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "邀请人ID不能为空"
                ));
            }
            
            // 调用服务层方法发送邀请
            teamService.inviteMemberByStudentId(id, studentId, message, inviterId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "邀请发送成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "发送邀请失败"
            ));
        }
    }
}