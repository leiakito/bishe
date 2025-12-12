package com.example.demo.controller;

import com.example.demo.entity.Competition;
import com.example.demo.entity.CompetitionAuditLog;
import com.example.demo.entity.User;
import com.example.demo.service.CompetitionService;
import com.example.demo.service.RegistrationService;
import com.example.demo.service.TeamService;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/competitions")
@CrossOrigin(origins = "*")
public class CompetitionController {
    
    @Autowired
    private CompetitionService competitionService;
    
    @Autowired
    private RegistrationService registrationService;
    
    @Autowired
    private TeamService teamService;
    
    @Autowired
    private UserRepository userRepository;
    
    // 创建竞赛
    @PostMapping
    public ResponseEntity<?> createCompetition(@Valid @RequestBody Competition competition, 
                                             @RequestParam Long createdBy) {
        try {
            Competition createdCompetition = competitionService.createCompetition(competition, createdBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "竞赛创建成功",
                "data", createdCompetition
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 导出竞赛数据
    @GetMapping("/export")
    public ResponseEntity<?> exportCompetitions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Competition.CompetitionStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "excel") String format) {
        try {
            // 构建筛选条件
            LocalDateTime start = null;
            LocalDateTime end = null;
            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDateTime.parse(startDate + "T00:00:00");
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDateTime.parse(endDate + "T23:59:59");
            }
            
            // 获取筛选后的竞赛数据
            List<Competition> competitions = competitionService.getCompetitionsForExport(
                keyword, category, status, start, end);
            
            if ("excel".equalsIgnoreCase(format)) {
                // 生成Excel文件
                byte[] excelData = competitionService.exportCompetitionsToExcel(competitions);
                
                // 设置响应头
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                headers.setContentDispositionFormData("attachment", "competitions_export_" + System.currentTimeMillis() + ".xlsx");
                headers.add("Access-Control-Expose-Headers", "Content-Disposition");
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(excelData);
            } else {
                // 返回JSON格式数据
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "导出数据准备完成",
                    "data", competitions
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "导出竞赛数据失败: " + e.getMessage()
            ));
        }
    }
    
    // 根据ID获取竞赛详情
    @GetMapping("/{id}")
    public ResponseEntity<?> getCompetitionById(@PathVariable Long id) {
        try {
            Optional<Competition> competitionOpt = competitionService.findById(id);
            if (competitionOpt.isPresent()) {
                Competition competition = competitionOpt.get();
                
                // 构建完整的竞赛详情数据
                Map<String, Object> competitionDetail = new HashMap<>();
                
                // 基本竞赛信息
                competitionDetail.put("id", competition.getId());
                competitionDetail.put("name", competition.getName());
                competitionDetail.put("description", competition.getDescription());
                competitionDetail.put("category", competition.getCategory());
                competitionDetail.put("level", competition.getLevel());
                competitionDetail.put("status", competition.getStatus());
                competitionDetail.put("organizer", competition.getOrganizer());
                competitionDetail.put("location", competition.getLocation());
                competitionDetail.put("contactInfo", competition.getContactInfo());
                competitionDetail.put("competitionNumber", competition.getCompetitionNumber());
                competitionDetail.put("registrationStartTime", competition.getRegistrationStartTime());
                competitionDetail.put("registrationEndTime", competition.getRegistrationEndTime());
                competitionDetail.put("competitionStartTime", competition.getCompetitionStartTime());
                competitionDetail.put("competitionEndTime", competition.getCompetitionEndTime());
                competitionDetail.put("minTeamSize", competition.getMinTeamSize());
                competitionDetail.put("maxTeamSize", competition.getMaxTeamSize());
                competitionDetail.put("registrationFee", competition.getRegistrationFee());
                competitionDetail.put("prizeInfo", competition.getPrizeInfo());
                competitionDetail.put("rules", competition.getRules());
                competitionDetail.put("createdAt", competition.getCreatedAt());
                competitionDetail.put("updatedAt", competition.getUpdatedAt());
                
                // 创建者信息
                if (competition.getCreator() != null) {
                    Map<String, Object> creator = new HashMap<>();
                    creator.put("id", competition.getCreator().getId());
                    creator.put("username", competition.getCreator().getUsername());
                    creator.put("realName", competition.getCreator().getRealName());
                    creator.put("email", competition.getCreator().getEmail());
                    creator.put("role", competition.getCreator().getRole());
                    competitionDetail.put("creator", creator);
                }
                
                // 统计信息
                Map<String, Object> statistics = new HashMap<>();
                statistics.put("viewCount", competition.getViewCount());
                statistics.put("registrationCount", registrationService.getRegistrationCount(id));
                statistics.put("teamCount", teamService.getTeamCountByCompetition(id));
                
                // 计算已完成和待审核的报名数量
                long totalRegistrations = registrationService.getRegistrationCount(id);
                statistics.put("completedRegistrations", totalRegistrations);
                statistics.put("pendingRegistrations", 0L); // 这里可以根据需要添加具体的逻辑
                
                competitionDetail.put("statistics", statistics);
                
                // 审核信息
                Map<String, Object> auditInfo = new HashMap<>();
                // 根据竞赛状态设置审核状态
                if (competition.getStatus() == Competition.CompetitionStatus.DRAFT) {
                    auditInfo.put("status", "pending");
                } else {
                    auditInfo.put("status", "approved");
                }
                auditInfo.put("reviewer", null);
                auditInfo.put("reviewedAt", null);
                auditInfo.put("remarks", null);
                competitionDetail.put("auditInfo", auditInfo);
                
                // 审核日志
                List<CompetitionAuditLog> auditLogs = competitionService.getCompetitionAuditLogs(id);
                competitionDetail.put("auditLogs", auditLogs);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", competitionDetail
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取竞赛信息失败: " + e.getMessage()
            ));
        }
    }
    
    // 获取所有竞赛（分页）
    @GetMapping
    public ResponseEntity<?> getAllCompetitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Competition> competitions = competitionService.getAllCompetitions(pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", competitions.getContent(),
                "totalElements", competitions.getTotalElements(),
                "totalPages", competitions.getTotalPages(),
                "currentPage", competitions.getNumber(),
                "size", competitions.getSize()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取竞赛列表失败"
            ));
        }
    }
    
    // 根据状态获取竞赛
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getCompetitionsByStatus(
            @PathVariable Competition.CompetitionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
            Page<Competition> competitions = competitionService.getCompetitionsByStatus(status, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", competitions.getContent(),
                "totalElements", competitions.getTotalElements(),
                "totalPages", competitions.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取竞赛列表失败"
            ));
        }
    }
    
    // 根据分类获取竞赛
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getCompetitionsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
            Page<Competition> competitions = competitionService.getCompetitionsByCategory(category, Competition.CompetitionStatus.PUBLISHED, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", competitions.getContent(),
                "totalElements", competitions.getTotalElements(),
                "totalPages", competitions.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取竞赛列表失败"
            ));
        }
    }
    
    // 根据级别获取竞赛
    @GetMapping("/level/{level}")
    public ResponseEntity<?> getCompetitionsByLevel(
            @PathVariable Competition.CompetitionLevel level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
            Page<Competition> competitions = competitionService.getCompetitionsByLevel(level, Competition.CompetitionStatus.PUBLISHED, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", competitions.getContent(),
                "totalElements", competitions.getTotalElements(),
                "totalPages", competitions.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取竞赛列表失败"
            ));
        }
    }
    
    // 搜索竞赛
    @GetMapping("/search")
    public ResponseEntity<?> searchCompetitions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
            Page<Competition> competitions = competitionService.searchCompetitions(keyword, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", competitions.getContent(),
                "totalElements", competitions.getTotalElements(),
                "totalPages", competitions.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "搜索竞赛失败"
            ));
        }
    }
    
    // 综合筛选竞赛
    @GetMapping("/filter")
    public ResponseEntity<?> filterCompetitions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Competition.CompetitionStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            LocalDateTime start = null;
            LocalDateTime end = null;
            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDateTime.parse(startDate + "T00:00:00");
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDateTime.parse(endDate + "T23:59:59");
            }
            
            Page<Competition> competitions = competitionService.filterCompetitions(
                keyword, category, status, start, end, pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", competitions.getContent(),
                "totalElements", competitions.getTotalElements(),
                "totalPages", competitions.getTotalPages(),
                "currentPage", competitions.getNumber(),
                "size", competitions.getSize()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "筛选竞赛失败: " + e.getMessage()
            ));
        }
    }
    
    // 获取热门竞赛
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularCompetitions(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Competition> competitions = competitionService.getPopularCompetitions(limit);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", competitions
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取热门竞赛失败"
            ));
        }
    }
    
    // 获取即将开始的竞赛
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingCompetitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Competition> competitions = competitionService.getUpcomingCompetitions(pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", competitions.getContent(),
                "totalElements", competitions.getTotalElements(),
                "totalPages", competitions.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取即将开始的竞赛失败"
            ));
        }
    }
    
    // 获取用户创建的竞赛
    @GetMapping("/created-by/{userId}")
    public ResponseEntity<?> getCompetitionsCreatedByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
            Page<Competition> competitions = competitionService.getCompetitionsByCreator(userId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", competitions.getContent(),
                "totalElements", competitions.getTotalElements(),
                "totalPages", competitions.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户创建的竞赛失败"
            ));
        }
    }
    
    // 更新竞赛信息
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompetition(@PathVariable Long id, 
                                              @Valid @RequestBody Competition competition,
                                              @RequestParam Long updatedBy) {
        try {
            Competition updatedCompetition = competitionService.updateCompetition(id, competition, updatedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "竞赛信息更新成功",
                "data", updatedCompetition
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 审核竞赛
    @PostMapping("/approve")
    public ResponseEntity<?> approveCompetition(@RequestBody Map<String, Object> request) {
        try {
            Long competitionId = Long.valueOf(request.get("competitionId").toString());
            Long reviewerId = Long.valueOf(request.get("reviewerId").toString());
            String remarks = request.get("remarks") != null ? request.get("remarks").toString() : "";
            
            competitionService.approveCompetition(competitionId, reviewerId, remarks);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "竞赛审核通过"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 拒绝竞赛
    @PostMapping("/reject")
    public ResponseEntity<?> rejectCompetition(@RequestBody Map<String, Object> request) {
        try {
            Long competitionId = Long.valueOf(request.get("competitionId").toString());
            Long reviewerId = Long.valueOf(request.get("reviewerId").toString());
            String remarks = request.get("remarks") != null ? request.get("remarks").toString() : "";
            
            competitionService.rejectCompetition(competitionId, reviewerId, remarks);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "竞赛已拒绝"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 获取竞赛审核日志
    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<?> getCompetitionAuditLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CompetitionAuditLog> auditLogs = competitionService.getCompetitionAuditLogs(id, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", auditLogs.getContent(),
                "totalElements", auditLogs.getTotalElements(),
                "totalPages", auditLogs.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取审核日志失败"
            ));
        }
    }
    
    // 获取所有审核日志
    @GetMapping("/audit-logs")
    public ResponseEntity<?> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CompetitionAuditLog> auditLogs = competitionService.getAllAuditLogs(pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", auditLogs.getContent(),
                "totalElements", auditLogs.getTotalElements(),
                "totalPages", auditLogs.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取审核日志失败"
            ));
        }
    }
    
    // 更新竞赛状态
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateCompetitionStatus(@PathVariable Long id, 
                                                    @RequestBody Map<String, String> request,
                                                    @RequestParam Long updatedBy) {
        try {
            System.out.println("=== 开始更新竞赛状态 ===");
            System.out.println("竞赛ID: " + id);
            System.out.println("更新者ID: " + updatedBy);
            System.out.println("请求体: " + request);
            
            String statusStr = request.get("status");
            if (statusStr == null) {
                System.out.println("错误: 状态不能为空");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "状态不能为空"
                ));
            }
            
            System.out.println("新状态: " + statusStr);
            
            // 验证用户权限
            Optional<User> userOpt = userRepository.findById(updatedBy);
            if (userOpt.isEmpty()) {
                System.out.println("错误: 用户不存在 - " + updatedBy);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User user = userOpt.get();
            System.out.println("用户信息: " + user.getUsername() + ", 角色: " + user.getRole());
            
            if (user.getRole() != User.UserRole.ADMIN) {
                System.out.println("错误: 权限不足 - " + user.getRole());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "只有管理员可以修改竞赛状态"
                ));
            }
            
            Competition.CompetitionStatus status = Competition.CompetitionStatus.valueOf(statusStr.toUpperCase());
            System.out.println("解析后的状态枚举: " + status);
            
            Optional<Competition> competitionOpt = competitionService.findById(id);
            if (competitionOpt.isEmpty()) {
                System.out.println("错误: 竞赛不存在 - " + id);
                throw new RuntimeException("竞赛不存在");
            }
            
            Competition competition = competitionOpt.get();
            System.out.println("竞赛信息: " + competition.getName() + ", 当前状态: " + competition.getStatus());
            
            competition.setStatus(status);
            competition.setUpdatedAt(LocalDateTime.now());
            
            System.out.println("准备保存竞赛，新状态: " + competition.getStatus());
            competition = competitionService.save(competition);
            System.out.println("竞赛保存成功，最终状态: " + competition.getStatus());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "竞赛状态更新成功",
                "data", competition
            ));
        } catch (IllegalArgumentException e) {
            System.out.println("错误: 无效的状态值 - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "无效的状态值"
            ));
        } catch (RuntimeException e) {
            System.out.println("运行时错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 删除竞赛
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompetition(@PathVariable Long id, @RequestParam Long deletedBy) {
        try {
            competitionService.deleteCompetition(id, deletedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "竞赛删除成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 获取竞赛统计信息
    @GetMapping("/stats")
    public ResponseEntity<?> getCompetitionStats() {
        try {
            Map<String, Object> stats = competitionService.getCompetitionStats();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取竞赛统计信息失败"
            ));
        }
    }
    
    // 获取待审核的竞赛
    @GetMapping("/pending-approval")
    public ResponseEntity<?> getPendingApprovalCompetitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
            Page<Competition> competitions = competitionService.getPendingApprovalCompetitions(pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", competitions.getContent(),
                "totalElements", competitions.getTotalElements(),
                "totalPages", competitions.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取待审核竞赛失败"
            ));
        }
    }
    
    // 根据时间范围获取竞赛
    @GetMapping("/date-range")
    public ResponseEntity<?> getCompetitionsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
            
            Page<Competition> competitions = competitionService.getCompetitionsByDateRange(start, end, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", competitions.getContent(),
                "totalElements", competitions.getTotalElements(),
                "totalPages", competitions.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取竞赛列表失败"
            ));
        }
    }
    
    // 检查竞赛名称是否存在
    @GetMapping("/check-name/{name}")
    public ResponseEntity<?> checkCompetitionName(@PathVariable String name) {
        try {
            boolean exists = competitionService.existsByName(name);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "exists", exists
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "检查竞赛名称失败"
            ));
        }
    }
    
    // 批量更新竞赛状态
    @PutMapping("/batch-status")
    public ResponseEntity<?> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> competitionIds = (List<Long>) request.get("competitionIds");
            String statusStr = (String) request.get("status");
            
            if (competitionIds == null || competitionIds.isEmpty() || statusStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "竞赛ID列表和状态不能为空"
                ));
            }
            
            Competition.CompetitionStatus status = Competition.CompetitionStatus.valueOf(statusStr.toUpperCase());
            competitionService.batchUpdateStatus(competitionIds, status);
            List<Competition> updatedCompetitions = competitionService.findByIds(competitionIds);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量更新竞赛状态成功",
                "data", updatedCompetitions
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
    
    // 获取竞赛详细信息（包含统计数据）
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getCompetitionDetails(@PathVariable Long id) {
        try {
            Competition competition = competitionService.findById(id).orElseThrow(() -> new RuntimeException("竞赛不存在"));
            Map<String, Object> details = new HashMap<>();
            details.put("competition", competition);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", details
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取竞赛详情失败"
            ));
        }
    }
    
    // 导出竞赛数据
    // 自动更新竞赛状态
    @PostMapping("/update-status")
    public ResponseEntity<?> autoUpdateCompetitionStatus() {
        try {
            competitionService.updateCompetitionStatusAutomatically();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "竞赛状态自动更新完成"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "自动更新竞赛状态失败"
            ));
        }
    }
}
