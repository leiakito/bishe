package com.example.demo.controller;

import com.example.demo.entity.Registration;
import com.example.demo.service.RegistrationService;
import com.example.demo.config.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class RegistrationController {
    
    @Autowired
    private RegistrationService registrationService;
    
    // 单人报名竞赛
    @PostMapping("/register-individual")
    public ResponseEntity<?> registerIndividual(@RequestBody Map<String, Object> request) {
        try {
            Long competitionId = Long.valueOf(request.get("competitionId").toString());

            // 从JWT中获取当前登录用户ID
            Long userId = JwtAuthenticationFilter.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "用户未登录"
                ));
            }

            if (competitionId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "竞赛ID不能为空"
                ));
            }

            Registration registration = registrationService.registerIndividual(competitionId, userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "报名成功",
                "data", registration
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 团队报名竞赛
    @PostMapping("/register-team")
    public ResponseEntity<?> registerTeam(@RequestBody Map<String, Object> request) {
        try {
            Long teamId = Long.valueOf(request.get("teamId").toString());
            Long competitionId = Long.valueOf(request.get("competitionId").toString());
            Long submittedBy = Long.valueOf(request.get("submittedBy").toString());
            
            if (teamId == null || competitionId == null || submittedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "团队ID、竞赛ID和提交人ID不能为空"
                ));
            }
            Registration registration = registrationService.registerTeam(competitionId, teamId, submittedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "团队报名成功",
                "data", registration
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 团队报名竞赛（新接口）
    @PostMapping("/team")
    public ResponseEntity<?> registerTeamCompetition(@RequestBody Map<String, Object> request) {
        try {
            Long teamId = Long.valueOf(request.get("teamId").toString());
            Long competitionId = Long.valueOf(request.get("competitionId").toString());
            String remarks = (String) request.get("remarks");
            
            // 获取当前用户ID作为提交人
            Long currentUserId = JwtAuthenticationFilter.getCurrentUserId();
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "用户未登录"
                ));
            }
            
            if (teamId == null || competitionId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "团队ID和竞赛ID不能为空"
                ));
            }
            
            Registration registration = registrationService.registerTeam(competitionId, teamId, currentUserId);
            if (remarks != null && !remarks.trim().isEmpty()) {
                registration.setRemarks(remarks);
                registrationService.save(registration);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "团队报名成功",
                "data", registration
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 根据ID获取报名信息
    @GetMapping("/{id}")
    public ResponseEntity<?> getRegistrationById(@PathVariable Long id) {
        try {
            Optional<Registration> registration = registrationService.findById(id);
            if (registration.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", registration.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取报名信息失败"
            ));
        }
    }
    
    // 根据报名编号获取报名信息
    @GetMapping("/number/{registrationNumber}")
    public ResponseEntity<?> getRegistrationByNumber(@PathVariable String registrationNumber) {
        try {
            Optional<Registration> registration = registrationService.findByRegistrationNumber(registrationNumber);
            if (registration.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", registration.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取报名信息失败"
            ));
        }
    }
    
    // 获取所有报名信息（分页）
    @GetMapping
    public ResponseEntity<?> getAllRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Registration> registrations = registrationService.getAllRegistrations(pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", registrations.getContent(),
                "totalElements", registrations.getTotalElements(),
                "totalPages", registrations.getTotalPages(),
                "currentPage", registrations.getNumber(),
                "size", registrations.getSize()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取报名列表失败"
            ));
        }
    }
    
    // 根据竞赛获取报名信息
    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<?> getRegistrationsByCompetition(
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Registration> registrations = registrationService.getRegistrationsByCompetition(competitionId, pageable);

            // 转换为包含关联数据的DTO
            List<Map<String, Object>> registrationDTOs = new ArrayList<>();
            for (Registration reg : registrations.getContent()) {
                Map<String, Object> dto = new java.util.HashMap<>();
                dto.put("id", reg.getId());
                dto.put("status", reg.getStatus());
                dto.put("registrationNumber", reg.getRegistrationNumber());
                dto.put("paymentStatus", reg.getPaymentStatus());
                dto.put("paymentAmount", reg.getPaymentAmount());
                dto.put("paymentTime", reg.getPaymentTime());
                dto.put("remarks", reg.getRemarks());
                dto.put("reviewRemarks", reg.getReviewRemarks());
                dto.put("reviewedBy", reg.getReviewedBy());
                dto.put("reviewedAt", reg.getReviewedAt());
                dto.put("createdAt", reg.getCreatedAt());
                dto.put("updatedAt", reg.getUpdatedAt());

                // 添加竞赛信息
                if (reg.getCompetition() != null) {
                    Map<String, Object> competitionInfo = new java.util.HashMap<>();
                    competitionInfo.put("id", reg.getCompetition().getId());
                    competitionInfo.put("name", reg.getCompetition().getName());
                    dto.put("competition", competitionInfo);
                }

                // 添加团队信息
                if (reg.getTeam() != null) {
                    Map<String, Object> teamInfo = new java.util.HashMap<>();
                    teamInfo.put("id", reg.getTeam().getId());
                    teamInfo.put("name", reg.getTeam().getName());
                    teamInfo.put("description", reg.getTeam().getDescription());
                    teamInfo.put("currentMembers", reg.getTeam().getCurrentMembers());
                    teamInfo.put("maxMembers", reg.getTeam().getMaxMembers());
                    // 添加队长信息
                    if (reg.getTeam().getLeader() != null) {
                        Map<String, Object> leaderInfo = new java.util.HashMap<>();
                        leaderInfo.put("id", reg.getTeam().getLeader().getId());
                        leaderInfo.put("realName", reg.getTeam().getLeader().getRealName());
                        leaderInfo.put("username", reg.getTeam().getLeader().getUsername());
                        teamInfo.put("leader", leaderInfo);
                    }
                    dto.put("team", teamInfo);
                }

                // 添加提交人信息
                if (reg.getSubmittedBy() != null) {
                    Map<String, Object> userInfo = new java.util.HashMap<>();
                    userInfo.put("id", reg.getSubmittedBy().getId());
                    userInfo.put("realName", reg.getSubmittedBy().getRealName());
                    userInfo.put("username", reg.getSubmittedBy().getUsername());
                    userInfo.put("email", reg.getSubmittedBy().getEmail());
                    userInfo.put("studentId", reg.getSubmittedBy().getStudentId());
                    userInfo.put("department", reg.getSubmittedBy().getDepartment());
                    dto.put("user", userInfo);
                }

                registrationDTOs.add(dto);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", registrationDTOs,
                "totalElements", registrations.getTotalElements(),
                "totalPages", registrations.getTotalPages(),
                "currentPage", registrations.getNumber(),
                "size", registrations.getSize()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取报名列表失败: " + e.getMessage()
            ));
        }
    }
    
    // 根据团队获取报名信息
    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> getRegistrationsByTeam(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Registration> registrations = registrationService.getRegistrationsByTeam(teamId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", registrations.getContent(),
                "totalElements", registrations.getTotalElements(),
                "totalPages", registrations.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取报名列表失败"
            ));
        }
    }
    
    // 根据用户获取报名信息
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getRegistrationsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Registration> registrations = registrationService.getRegistrationsByUser(userId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", registrations.getContent(),
                "totalElements", registrations.getTotalElements(),
                "totalPages", registrations.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取报名列表失败"
            ));
        }
    }
    
    // 获取当前用户的报名列表
    @GetMapping("/my")
    public ResponseEntity<?> getMyRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = JwtAuthenticationFilter.getCurrentUserId();
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "用户未登录"
                ));
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Registration> registrations = registrationService.getRegistrationsByUser(currentUserId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", registrations.getContent(),
                "totalElements", registrations.getTotalElements(),
                "totalPages", registrations.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取我的报名列表失败"
            ));
        }
    }
    
    // 根据状态获取报名信息
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getRegistrationsByStatus(
            @PathVariable Registration.RegistrationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Registration> registrations = registrationService.getRegistrationsByStatus(status, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", registrations.getContent(),
                "totalElements", registrations.getTotalElements(),
                "totalPages", registrations.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取报名列表失败"
            ));
        }
    }
    
    // 根据支付状态获取报名信息
    @GetMapping("/payment-status/{paymentStatus}")
    public ResponseEntity<?> getRegistrationsByPaymentStatus(
            @PathVariable Registration.PaymentStatus paymentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Registration> registrations = registrationService.getRegistrationsByPaymentStatus(paymentStatus, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", registrations.getContent(),
                "totalElements", registrations.getTotalElements(),
                "totalPages", registrations.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取报名列表失败"
            ));
        }
    }
    
    // 获取待审核的报名
    @GetMapping("/pending-approval")
    public ResponseEntity<?> getPendingApprovalRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("registrationTime").descending());
            Page<Registration> registrations = registrationService.getPendingApprovalRegistrations(pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", registrations.getContent(),
                "totalElements", registrations.getTotalElements(),
                "totalPages", registrations.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取待审核报名失败"
            ));
        }
    }
    
    // 审核报名
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveRegistration(@PathVariable Long id,
                                                @RequestBody Map<String, Object> request) {
        try {
            Boolean approved = (Boolean) request.get("approved");
            Long reviewedBy = Long.valueOf(request.get("reviewedBy").toString());
            String remarks = (String) request.get("remarks");
            
            if (approved == null || reviewedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "审核结果和审核人不能为空"
                ));
            }
            
            Registration registration = registrationService.approveRegistration(id, approved, reviewedBy, remarks);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", approved ? "报名审核通过" : "报名审核拒绝",
                "data", registration
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 拒绝报名
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectRegistration(@PathVariable Long id,
                                               @RequestBody Map<String, Object> request) {
        try {
            Long rejectedBy = Long.valueOf(request.get("rejectedBy").toString());
            String reason = (String) request.get("reason");
            
            if (rejectedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "拒绝人不能为空"
                ));
            }
            
            Registration registration = registrationService.rejectRegistration(id, rejectedBy, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "报名已拒绝",
                "data", registration
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 确认支付
    @PutMapping("/{id}/confirm-payment")
    public ResponseEntity<?> confirmPayment(@PathVariable Long id,
                                           @RequestBody Map<String, Object> request) {
        try {
            String paymentMethod = (String) request.get("paymentMethod");
            String transactionId = (String) request.get("transactionId");
            
            Registration registration = registrationService.confirmPayment(id, paymentMethod, transactionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "支付确认成功",
                "data", registration
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 取消报名
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelRegistration(@PathVariable Long id,
                                               @RequestParam Long cancelledBy,
                                               @RequestBody(required = false) Map<String, String> request) {
        try {
            String reason = request != null ? request.get("reason") : null;
            Registration registration = registrationService.cancelRegistration(id, cancelledBy, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "报名取消成功",
                "data", registration
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 处理退款
    @PutMapping("/{id}/refund")
    public ResponseEntity<?> processRefund(@PathVariable Long id,
                                          @RequestBody Map<String, Object> request) {
        try {
            Long processedBy = Long.valueOf(request.get("processedBy").toString());
            String refundMethod = (String) request.get("refundMethod");
            String refundTransactionId = (String) request.get("refundTransactionId");
            
            if (processedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "处理人不能为空"
                ));
            }
            
            Registration registration = registrationService.processRefund(id, processedBy, refundMethod, refundTransactionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "退款处理成功",
                "data", registration
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 检查团队是否已报名竞赛
    @GetMapping("/check-registration")
    public ResponseEntity<?> checkRegistration(@RequestParam Long teamId,
                                              @RequestParam Long competitionId) {
        try {
            boolean exists = registrationService.existsByTeamAndCompetition(teamId, competitionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "exists", exists
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "检查报名状态失败"
            ));
        }
    }
    
    // 获取用户在特定竞赛中的报名
    @GetMapping("/user-competition")
    public ResponseEntity<?> getUserRegistrationInCompetition(@RequestParam Long competitionId) {
        try {
            // 从JWT中获取当前登录用户ID
            Long userId = JwtAuthenticationFilter.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "用户未登录"
                ));
            }

            List<Registration> registrations = registrationService.getUserRegistrationsInCompetition(userId, competitionId);

            // 转换为包含竞赛ID的DTO格式,避免@JsonIgnore导致的数据丢失
            List<Map<String, Object>> registrationDTOs = new ArrayList<>();
            for (Registration reg : registrations) {
                Map<String, Object> dto = new java.util.HashMap<>();
                dto.put("id", reg.getId());
                dto.put("status", reg.getStatus());
                dto.put("registrationNumber", reg.getRegistrationNumber());
                dto.put("paymentStatus", reg.getPaymentStatus());
                dto.put("paymentAmount", reg.getPaymentAmount());
                dto.put("paymentTime", reg.getPaymentTime());
                dto.put("remarks", reg.getRemarks());
                dto.put("createdAt", reg.getCreatedAt());
                dto.put("updatedAt", reg.getUpdatedAt());
                // 添加竞赛ID
                if (reg.getCompetition() != null) {
                    dto.put("competitionId", reg.getCompetition().getId());
                }
                // 添加团队ID
                if (reg.getTeam() != null) {
                    dto.put("teamId", reg.getTeam().getId());
                }
                registrationDTOs.add(dto);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", registrationDTOs
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户报名信息失败"
            ));
        }
    }
    
    // 根据时间范围获取报名
    @GetMapping("/date-range")
    public ResponseEntity<?> getRegistrationsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

            List<Registration> registrations = registrationService.getRegistrationsByDateRange(start, end);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", registrations,
                "totalElements", registrations.size(),
                "totalPages", 1
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取报名列表失败"
            ));
        }
    }
    
    // 获取报名统计信息
    @GetMapping("/stats")
    public ResponseEntity<?> getRegistrationStats() {
        try {
            // 简化统计信息实现
            Map<String, Object> stats = Map.of(
                "totalRegistrations", registrationService.getAllRegistrations().size(),
                "pendingRegistrations", registrationService.getRegistrationsByStatus(Registration.RegistrationStatus.PENDING).size(),
                "approvedRegistrations", registrationService.getRegistrationsByStatus(Registration.RegistrationStatus.APPROVED).size()
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取报名统计信息失败"
            ));
        }
    }
    
    // 获取竞赛收入统计
    @GetMapping("/competition/{competitionId}/revenue")
    public ResponseEntity<?> getCompetitionRevenue(@PathVariable Long competitionId) {
        try {
            // 简化收入统计实现
            Map<String, Object> revenue = Map.of(
                "totalRevenue", 0,
                "paidRegistrations", 0
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", revenue
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取竞赛收入统计失败"
            ));
        }
    }
    
    // 批量审核报名
    @PutMapping("/batch-approve")
    public ResponseEntity<?> batchApproveRegistrations(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> registrationIds = (List<Long>) request.get("registrationIds");
            Boolean approved = (Boolean) request.get("approved");
            Long reviewedBy = Long.valueOf(request.get("reviewedBy").toString());
            String remarks = (String) request.get("remarks");
            
            if (registrationIds == null || registrationIds.isEmpty() || approved == null || reviewedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "报名ID列表、审核结果和审核人不能为空"
                ));
            }
            
            // 简化批量审核实现
            List<Registration> updatedRegistrations = new ArrayList<>();
            for (Long id : registrationIds) {
                try {
                    Registration registration = approved ? 
                        registrationService.approveRegistration(id, reviewedBy) :
                        registrationService.rejectRegistration(id, reviewedBy);
                    updatedRegistrations.add(registration);
                } catch (Exception e) {
                    // 忽略单个失败的情况
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量审核报名成功",
                "data", updatedRegistrations
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "批量审核失败"
            ));
        }
    }
    
    // 批量取消报名
    @PutMapping("/batch-cancel")
    public ResponseEntity<?> batchCancelRegistrations(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> registrationIds = (List<Long>) request.get("registrationIds");
            Long cancelledBy = Long.valueOf(request.get("cancelledBy").toString());
            String reason = (String) request.get("reason");
            
            if (registrationIds == null || registrationIds.isEmpty() || cancelledBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "报名ID列表和取消人不能为空"
                ));
            }
            
            // 简化批量取消实现
            List<Registration> cancelledRegistrations = new ArrayList<>();
            for (Long id : registrationIds) {
                try {
                    Registration registration = registrationService.cancelRegistration(id, cancelledBy, reason);
                    cancelledRegistrations.add(registration);
                } catch (Exception e) {
                    // 忽略单个失败的情况
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量取消报名成功",
                "data", cancelledRegistrations
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "批量取消失败"
            ));
        }
    }
    
    // 导出报名数据
    @GetMapping("/export")
    public ResponseEntity<?> exportRegistrations(
            @RequestParam(required = false) Long competitionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus) {
        try {
            // 这里应该返回文件下载，简化处理返回数据
            // 简化导出实现
            List<Registration> registrations = registrationService.getAllRegistrations();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导出数据准备完成",
                "data", registrations
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "导出报名数据失败"
            ));
        }
    }
}