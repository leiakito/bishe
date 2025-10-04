package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.entity.Competition;
import com.example.demo.entity.Registration;
import com.example.demo.service.UserService;
import com.example.demo.service.CompetitionService;
import com.example.demo.service.RegistrationService;
import com.example.demo.entity.Question;
import com.example.demo.service.QuestionService;
import com.example.demo.entity.Grade;
import com.example.demo.service.GradeService;
import java.math.BigDecimal;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;

/**
 * 教师功能控制器
 * 专门处理教师相关的业务功能
 */
@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "*")
public class TeacherController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CompetitionService competitionService;
    
    @Autowired
    private RegistrationService registrationService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private GradeService gradeService;
    
    /**
     * 教师注册
     * 教师注册后状态为PENDING，需要管理员审核
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerTeacher(@Valid @RequestBody User teacher) {
        try {
            // 设置用户角色为教师
            teacher.setRole(User.UserRole.TEACHER);
            
            // 验证必填字段
            if (teacher.getTeacherId() == null || teacher.getTeacherId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "教师工号不能为空"
                ));
            }
            
            if (teacher.getDepartment() == null || teacher.getDepartment().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "所属学院不能为空"
                ));
            }
            
            User registeredTeacher = userService.registerUser(teacher);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "教师注册成功，请等待管理员审核",
                "data", Map.of(
                    "id", registeredTeacher.getId(),
                    "username", registeredTeacher.getUsername(),
                    "status", registeredTeacher.getStatus()
                )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "注册失败，请稍后重试"
            ));
        }
    }
    
    /**
     * 教师登录
     * 只有状态为APPROVED的教师才能登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginTeacher(@RequestBody Map<String, String> loginRequest) {
        try {
            System.out.println("=== 教师登录开始 ===");
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            System.out.println("登录用户名: " + username);
            
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户名和密码不能为空"
                ));
            }
            
            Optional<User> userOpt = userService.login(username, password);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户名或密码错误"
                ));
            }
            
            User user = userOpt.get();
            
            // 检查是否为教师
            if (user.getRole() != User.UserRole.TEACHER) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "该账户不是教师账户"
                ));
            }
            
            // 检查教师状态
            if (user.getStatus() != User.UserStatus.APPROVED) {
                String statusMessage = switch (user.getStatus()) {
                    case PENDING -> "账户正在审核中，请耐心等待";
                    case REJECTED -> "账户审核未通过，请联系管理员";
                    case DISABLED -> "账户已被禁用，请联系管理员";
                    default -> "账户状态异常，请联系管理员";
                };
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", statusMessage
                ));
            }
            
            // 生成JWT token - 添加详细日志
            System.out.println("准备生成JWT token:");
            System.out.println("  username: " + user.getUsername());
            System.out.println("  userId: " + user.getId());
            System.out.println("  role: " + user.getRole());
            System.out.println("  role.toString(): " + (user.getRole() != null ? user.getRole().toString() : "null"));
            
            // 检查role是否为null
            if (user.getRole() == null) {
                System.err.println("ERROR: user.getRole() is null for user: " + user.getUsername());
                throw new RuntimeException("用户角色为空");
            }
            
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().toString());
            System.out.println("JWT token生成成功: " + (token != null ? "是" : "否"));
            
            // 构建用户信息，处理可能的null值
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName() != null ? user.getRealName() : "");
            userInfo.put("email", user.getEmail() != null ? user.getEmail() : "");
            userInfo.put("teacherId", user.getTeacherId() != null ? user.getTeacherId() : "");
            userInfo.put("department", user.getDepartment() != null ? user.getDepartment() : "");
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "登录成功",
                "token", token,
                "userInfo", userInfo
            ));
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈
            System.err.println("Teacher login error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "登录失败，请稍后重试: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取教师个人信息
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getTeacherProfile(HttpServletRequest request) {
        try {
            System.out.println("=== getTeacherProfile 开始 ===");
            
            String token = extractTokenFromRequest(request);
            System.out.println("提取的token: " + (token != null ? "存在" : "null"));
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            System.out.println("开始从token提取用户名");
            String username = jwtUtil.extractUsername(token);
            System.out.println("提取的用户名: " + username);
            
            System.out.println("开始查找用户");
            Optional<User> userOpt = userService.findByUsername(username);
            System.out.println("用户查找结果: " + (userOpt.isPresent() ? "找到" : "未找到"));
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            System.out.println("用户角色: " + teacher.getRole());
            
            // 确保是教师账户
            if (teacher.getRole() != User.UserRole.TEACHER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "权限不足"
                ));
            }
            
            System.out.println("开始构建响应数据");
            Map<String, Object> teacherData = new HashMap<>();
            teacherData.put("id", teacher.getId());
            teacherData.put("username", teacher.getUsername());
            teacherData.put("realName", teacher.getRealName());
            teacherData.put("email", teacher.getEmail());
            teacherData.put("phoneNumber", teacher.getPhoneNumber());
            teacherData.put("teacherId", teacher.getTeacherId());
            teacherData.put("department", teacher.getDepartment());
            teacherData.put("schoolName", teacher.getSchoolName());
            teacherData.put("avatarUrl", teacher.getAvatarUrl());
            teacherData.put("status", teacher.getStatus());
            teacherData.put("createdAt", teacher.getCreatedAt());
            teacherData.put("updatedAt", teacher.getUpdatedAt());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", teacherData
            ));
        } catch (Exception e) {
            System.err.println("getTeacherProfile 异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取个人信息失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 更新教师个人信息
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateTeacherProfile(HttpServletRequest request, @RequestBody Map<String, Object> updateData) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            // 确保是教师账户
            if (teacher.getRole() != User.UserRole.TEACHER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "权限不足"
                ));
            }
            
            // 更新允许修改的字段
            if (updateData.containsKey("realName")) {
                teacher.setRealName((String) updateData.get("realName"));
            }
            if (updateData.containsKey("email")) {
                teacher.setEmail((String) updateData.get("email"));
            }
            if (updateData.containsKey("phoneNumber")) {
                teacher.setPhoneNumber((String) updateData.get("phoneNumber"));
            }
            if (updateData.containsKey("department")) {
                teacher.setDepartment((String) updateData.get("department"));
            }
            if (updateData.containsKey("schoolName")) {
                teacher.setSchoolName((String) updateData.get("schoolName"));
            }
            if (updateData.containsKey("avatarUrl")) {
                teacher.setAvatarUrl((String) updateData.get("avatarUrl"));
            }
            
            User updatedTeacher = userService.updateUser(teacher.getId(), teacher);
            
            Map<String, Object> updatedTeacherData = new HashMap<>();
            updatedTeacherData.put("id", updatedTeacher.getId());
            updatedTeacherData.put("username", updatedTeacher.getUsername());
            updatedTeacherData.put("realName", updatedTeacher.getRealName());
            updatedTeacherData.put("email", updatedTeacher.getEmail());
            updatedTeacherData.put("phoneNumber", updatedTeacher.getPhoneNumber());
            updatedTeacherData.put("teacherId", updatedTeacher.getTeacherId());
            updatedTeacherData.put("department", updatedTeacher.getDepartment());
            updatedTeacherData.put("schoolName", updatedTeacher.getSchoolName());
            updatedTeacherData.put("avatarUrl", updatedTeacher.getAvatarUrl());
            updatedTeacherData.put("updatedAt", updatedTeacher.getUpdatedAt());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "个人信息更新成功",
                "data", updatedTeacherData
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "更新个人信息失败"
            ));
        }
    }
    
    /**
     * 教师修改密码
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest request, @RequestBody Map<String, String> passwordData) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            // 确保是教师账户
            if (teacher.getRole() != User.UserRole.TEACHER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "权限不足"
                ));
            }
            
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "原密码和新密码不能为空"
                ));
            }
            
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "新密码长度至少6个字符"
                ));
            }
            
            userService.changePassword(teacher.getId(), oldPassword, newPassword);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "密码修改成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "密码修改失败"
            ));
        }
    }
    
    /**
     * 教师退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // 由于使用JWT，服务端无状态，退出登录主要由前端处理
        // 这里返回成功响应，前端需要清除本地存储的token
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "退出登录成功"
        ));
    }
    
    /**
     * 教师创建竞赛
     */
    @PostMapping("/competitions")
    public ResponseEntity<?> createCompetition(HttpServletRequest request, @Valid @RequestBody Map<String, Object> competitionData) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            // 确保是教师账户且状态正常
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "权限不足或账户状态异常"
                ));
            }
            
            // 创建竞赛对象
            Competition competition = new Competition();
            competition.setName((String) competitionData.get("name"));
            competition.setDescription((String) competitionData.get("description"));
            competition.setRules((String) competitionData.get("rules"));
            competition.setMaxParticipants((Integer) competitionData.get("maxParticipants"));
            
            // 定义时间格式解析器，支持前端发送的格式 "YYYY-MM-DD HH:mm:ss"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            competition.setRegistrationStartTime(LocalDateTime.parse((String) competitionData.get("registrationStartTime"), formatter));
            competition.setRegistrationEndTime(LocalDateTime.parse((String) competitionData.get("registrationEndTime"), formatter));
            competition.setStartTime(LocalDateTime.parse((String) competitionData.get("startTime"), formatter));
            competition.setEndTime(LocalDateTime.parse((String) competitionData.get("endTime"), formatter));
            
            // 设置分类和级别
            if (competitionData.get("category") != null) {
                competition.setCategory(Competition.CompetitionCategory.valueOf((String) competitionData.get("category")));
            }
            if (competitionData.get("level") != null) {
                competition.setLevel(Competition.CompetitionLevel.valueOf((String) competitionData.get("level")));
            }
            
            Competition createdCompetition = competitionService.createCompetition(competition, teacher.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "竞赛创建成功",
                "data", Map.of(
                    "id", createdCompetition.getId(),
                    "name", createdCompetition.getName(),
                    "status", createdCompetition.getStatus(),
                    "createdAt", createdCompetition.getCreatedAt()
                )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "创建竞赛失败"
            ));
        }
    }
    
    /**
     * 获取教师创建的竞赛列表
     */
    @GetMapping("/competitions")
    public ResponseEntity<?> getTeacherCompetitions(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "权限不足"
                ));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Competition> competitions = competitionService.getCompetitionsByCreator(teacher.getId(), pageable);
            
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
    
    /**
     * 更新教师创建的竞赛
     */
    @PutMapping("/competitions/{competitionId}")
    public ResponseEntity<?> updateCompetition(HttpServletRequest request, 
            @PathVariable Long competitionId,
            @RequestBody Map<String, Object> updateData) {
        try {
            // 支持多种日期格式的解析
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,  // 支持 2025-10-02T21:23:06
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),  // 支持 2025-10-02 21:23:06
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")  // 支持 2025-10-02T21:23:06
            };
            
            // 辅助方法：尝试多种格式解析日期时间
            Function<String, LocalDateTime> parseDateTime = (dateTimeStr) -> {
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDateTime.parse(dateTimeStr, formatter);
                    } catch (DateTimeParseException e) {
                        // 继续尝试下一个格式
                    }
                }
                throw new IllegalArgumentException("无法解析日期时间格式: " + dateTimeStr);
            };
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "权限不足"
                ));
            }
            
            // 检查竞赛是否存在且是否为该教师创建
            Optional<Competition> competitionOpt = competitionService.findById(competitionId);
            if (competitionOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "竞赛不存在"
                ));
            }
            
            Competition competition = competitionOpt.get();
            
            // 验证权限：只能修改自己创建的竞赛
            if (!competition.getCreatedBy().getId().equals(teacher.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "只能修改自己创建的竞赛"
                ));
            }
            
            // 检查竞赛状态是否允许修改
            if (competition.getStatus() != Competition.CompetitionStatus.DRAFT && 
                competition.getStatus() != Competition.CompetitionStatus.PENDING_APPROVAL) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "当前状态的竞赛不允许修改"
                ));
            }
            
            // 更新竞赛信息
            if (updateData.containsKey("name")) {
                competition.setName((String) updateData.get("name"));
            }
            if (updateData.containsKey("description")) {
                competition.setDescription((String) updateData.get("description"));
            }
            if (updateData.containsKey("rules")) {
                competition.setRules((String) updateData.get("rules"));
            }
            if (updateData.containsKey("maxParticipants")) {
                competition.setMaxParticipants((Integer) updateData.get("maxParticipants"));
            }
            if (updateData.containsKey("location")) {
                competition.setLocation((String) updateData.get("location"));
            }
            if (updateData.containsKey("organizer")) {
                competition.setOrganizer((String) updateData.get("organizer"));
            }
            if (updateData.containsKey("contactInfo")) {
                competition.setContactInfo((String) updateData.get("contactInfo"));
            }
            if (updateData.containsKey("prizeInfo")) {
                competition.setPrizeInfo((String) updateData.get("prizeInfo"));
            }
            if (updateData.containsKey("minTeamSize")) {
                competition.setMinTeamSize((Integer) updateData.get("minTeamSize"));
            }
            if (updateData.containsKey("maxTeamSize")) {
                competition.setMaxTeamSize((Integer) updateData.get("maxTeamSize"));
            }
            if (updateData.containsKey("registrationFee")) {
                Object feeValue = updateData.get("registrationFee");
                if (feeValue instanceof Number) {
                    competition.setRegistrationFee(((Number) feeValue).doubleValue());
                }
            }
            if (updateData.containsKey("registrationStartTime")) {
                competition.setRegistrationStartTime(parseDateTime.apply((String) updateData.get("registrationStartTime")));
            }
            if (updateData.containsKey("registrationEndTime")) {
                competition.setRegistrationEndTime(parseDateTime.apply((String) updateData.get("registrationEndTime")));
            }
            if (updateData.containsKey("startTime")) {
                competition.setStartTime(parseDateTime.apply((String) updateData.get("startTime")));
            }
            if (updateData.containsKey("endTime")) {
                competition.setEndTime(parseDateTime.apply((String) updateData.get("endTime")));
            }
            
            Competition updatedCompetition = competitionService.updateCompetition(competitionId, competition, teacher.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "竞赛更新成功",
                "data", updatedCompetition
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "更新竞赛失败"
            ));
        }
    }
    
    /**
     * 删除教师创建的竞赛
     */
    @DeleteMapping("/competitions/{competitionId}")
    public ResponseEntity<?> deleteCompetition(HttpServletRequest request, @PathVariable Long competitionId) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "权限不足"
                ));
            }
            
            // 检查竞赛是否存在且是否为该教师创建
            Optional<Competition> competitionOpt = competitionService.findById(competitionId);
            if (competitionOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "竞赛不存在"
                ));
            }
            
            Competition competition = competitionOpt.get();
            if (!competition.getCreatedBy().getId().equals(teacher.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "只能删除自己创建的竞赛"
                ));
            }
            
            // 检查竞赛状态是否允许删除
            if (competition.getStatus() == Competition.CompetitionStatus.ONGOING || 
                competition.getStatus() == Competition.CompetitionStatus.COMPLETED) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "进行中或已完成的竞赛不能删除"
                ));
            }
            
            competitionService.deleteCompetition(competitionId, teacher.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "竞赛删除成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "删除竞赛失败"
            ));
        }
    }
    
    /**
     * 获取教师创建竞赛的待审核报名
     */
    @GetMapping("/registrations/pending")
    public ResponseEntity<?> getPendingRegistrations(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long competitionId) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "权限不足"
                ));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Registration> registrations;
            
            if (competitionId != null) {
                // 检查竞赛是否为该教师创建
                Optional<Competition> competitionOpt = competitionService.findById(competitionId);
                if (competitionOpt.isEmpty() || !competitionOpt.get().getCreatedBy().equals(teacher.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "success", false,
                        "message", "无权查看该竞赛的报名信息"
                    ));
                }
                registrations = registrationService.getPendingRegistrationsByCompetition(competitionId, pageable);
            } else {
                // 获取该教师所有竞赛的待审核报名
                registrations = registrationService.getPendingRegistrationsByTeacher(teacher.getId(), pageable);
            }
            
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
                "message", "获取待审核报名失败"
            ));
        }
    }
    
    /**
     * 审核报名申请
     */
    @PutMapping("/registrations/{registrationId}/review")
    public ResponseEntity<?> reviewRegistration(HttpServletRequest request,
            @PathVariable Long registrationId,
            @RequestBody Map<String, Object> reviewData) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "权限不足"
                ));
            }
            
            // 检查报名是否存在
            Optional<Registration> registrationOpt = registrationService.findById(registrationId);
            if (registrationOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "报名记录不存在"
                ));
            }
            
            Registration registration = registrationOpt.get();
            
            // 检查该竞赛是否为该教师创建
            Optional<Competition> competitionOpt = competitionService.findById(registration.getCompetition().getId());
            if (competitionOpt.isEmpty() || !competitionOpt.get().getCreatedBy().equals(teacher.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "无权审核该报名"
                ));
            }
            
            // 检查报名状态是否为待审核
            if (registration.getStatus() != Registration.RegistrationStatus.PENDING) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "该报名已被审核"
                ));
            }
            
            Boolean approved = (Boolean) reviewData.get("approved");
            String remarks = (String) reviewData.get("remarks");
            
            Registration reviewedRegistration = registrationService.reviewRegistration(
                registrationId, approved, teacher.getId(), remarks
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", approved ? "报名审核通过" : "报名审核拒绝",
                "data", reviewedRegistration
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "审核报名失败"
            ));
        }
    }
    
    /**
     * 获取教师竞赛的所有报名（包括已审核的）
     */
    @GetMapping("/registrations")
    public ResponseEntity<?> getCompetitionRegistrations(HttpServletRequest request,
            @RequestParam Long competitionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "权限不足"
                ));
            }
            
            // 检查竞赛是否为该教师创建
            Optional<Competition> competitionOpt = competitionService.findById(competitionId);
            if (competitionOpt.isEmpty() || !competitionOpt.get().getCreatedBy().equals(teacher.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "无权查看该竞赛的报名信息"
                ));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Registration> registrations;
            
            if (status != null && !status.isEmpty()) {
                Registration.RegistrationStatus registrationStatus = Registration.RegistrationStatus.valueOf(status.toUpperCase());
                registrations = registrationService.getRegistrationsByCompetitionAndStatus(competitionId, registrationStatus, pageable);
            } else {
                registrations = registrationService.getRegistrationsByCompetition(competitionId, pageable);
            }
            
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
                "message", "获取报名信息失败"
            ));
        }
    }
    
    /**
     * 批量审核报名
     */
    @PutMapping("/registrations/batch-review")
    public ResponseEntity<?> batchReviewRegistrations(HttpServletRequest request,
            @RequestBody Map<String, Object> batchData) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "权限不足"
                ));
            }
            
            @SuppressWarnings("unchecked")
            List<Long> registrationIds = (List<Long>) batchData.get("registrationIds");
            Boolean approved = (Boolean) batchData.get("approved");
            String remarks = (String) batchData.get("remarks");
            
            // 验证所有报名都属于该教师的竞赛
            for (Long registrationId : registrationIds) {
                Optional<Registration> registrationOpt = registrationService.findById(registrationId);
                if (registrationOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "报名记录 " + registrationId + " 不存在"
                    ));
                }
                
                Registration registration = registrationOpt.get();
                Optional<Competition> competitionOpt = competitionService.findById(registration.getCompetition().getId());
                if (competitionOpt.isEmpty() || !competitionOpt.get().getCreatedBy().equals(teacher.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "success", false,
                        "message", "无权审核报名 " + registrationId
                    ));
                }
            }
            
            List<Registration> reviewedRegistrations = registrationService.batchReviewRegistrations(
                registrationIds, approved, teacher.getId(), remarks
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量审核完成",
                "data", reviewedRegistrations
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "批量审核失败"
            ));
        }
    }
    
    // ==================== 题库管理相关接口 ====================
    
    /**
     * 创建题目
     */
    @PostMapping("/questions")
    public ResponseEntity<?> createQuestion(@RequestBody Question question, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "未提供认证令牌"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "权限不足"));
            }
            
            // 验证必需参数
            if (question.getTitle() == null || question.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "题目标题不能为空"));
            }
            if (question.getContent() == null || question.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "题目内容不能为空"));
            }
            if (question.getType() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "题目类型不能为空"));
            }
            if (question.getDifficulty() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "题目难度不能为空"));
            }
            if (question.getCategory() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "题目分类不能为空"));
            }
            
            question.setCreatedBy(teacher.getId());
            Question createdQuestion = questionService.createQuestion(question);
            
            return ResponseEntity.ok(Map.of(
                    "message", "题目创建成功",
                    "question", createdQuestion
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "创建题目失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取教师创建的题目列表
     */
    @GetMapping("/questions")
    public ResponseEntity<?> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "未提供认证令牌"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "权限不足"));
            }
            
            Page<Question> questions;
            
            // 如果有筛选条件，使用综合筛选
            if (status != null || type != null || category != null || difficulty != null || keyword != null) {
                Question.QuestionStatus questionStatus = status != null ? Question.QuestionStatus.valueOf(status.toUpperCase()) : null;
                Question.QuestionType questionType = type != null ? Question.QuestionType.valueOf(type.toUpperCase()) : null;
                Question.QuestionCategory questionCategory = category != null ? Question.QuestionCategory.valueOf(category.toUpperCase()) : null;
                Question.QuestionDifficulty questionDifficulty = difficulty != null ? Question.QuestionDifficulty.valueOf(difficulty.toUpperCase()) : null;
                
                questions = questionService.filterQuestions(teacher.getId(), questionType, questionCategory, 
                        questionDifficulty, questionStatus, keyword, page, size, sortBy, sortDir);
            } else {
                questions = questionService.getQuestionsByTeacher(teacher.getId(), page, size, sortBy, sortDir);
            }
            
            return ResponseEntity.ok(Map.of(
                    "message", "获取题目列表成功",
                    "questions", questions.getContent(),
                    "totalElements", questions.getTotalElements(),
                    "totalPages", questions.getTotalPages(),
                    "currentPage", questions.getNumber(),
                    "size", questions.getSize()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取题目列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取题目详情
     */
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<?> getQuestionById(@PathVariable Long questionId, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "未提供认证令牌"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "权限不足"));
            }
            
            Optional<Question> questionOpt = questionService.findById(questionId);
            if (questionOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "题目不存在"));
            }
            
            Question question = questionOpt.get();
            
            // 检查权限
            if (!question.getCreatedBy().equals(teacher.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "只能查看自己创建的题目"));
            }
            
            return ResponseEntity.ok(Map.of(
                    "message", "获取题目详情成功",
                    "question", question
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取题目详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新题目
     */
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long questionId, @RequestBody Question updatedQuestion, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "未提供认证令牌"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "权限不足"));
            }
            
            Question question = questionService.updateQuestion(questionId, updatedQuestion, teacher.getId());
            
            return ResponseEntity.ok(Map.of(
                    "message", "题目更新成功",
                    "question", question
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "更新题目失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除题目
     */
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "未提供认证令牌"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "权限不足"));
            }
            
            questionService.deleteQuestion(questionId, teacher.getId());
            
            return ResponseEntity.ok(Map.of("message", "题目删除成功"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "删除题目失败: " + e.getMessage()));
        }
    }
    
    /**
     * 复制题目
     */
    @PostMapping("/questions/{questionId}/duplicate")
    public ResponseEntity<?> duplicateQuestion(@PathVariable Long questionId, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "未提供认证令牌"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "权限不足"));
            }
            
            Question duplicatedQuestion = questionService.duplicateQuestion(questionId, teacher.getId());
            
            return ResponseEntity.ok(Map.of(
                    "message", "题目复制成功",
                    "question", duplicatedQuestion
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "复制题目失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量删除题目
     */
    @DeleteMapping("/questions/batch")
    public ResponseEntity<?> batchDeleteQuestions(@RequestBody Map<String, List<Long>> request, HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "未提供认证令牌"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "权限不足"));
            }
            
            List<Long> questionIds = request.get("questionIds");
            if (questionIds == null || questionIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "请提供要删除的题目ID列表"));
            }
            
            questionService.batchDeleteQuestions(questionIds, teacher.getId());
            
            return ResponseEntity.ok(Map.of("message", "批量删除题目成功"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "批量删除题目失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量更新题目状态
     */
    @PutMapping("/questions/batch/status")
    public ResponseEntity<?> batchUpdateQuestionStatus(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "未提供认证令牌"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "权限不足"));
            }
            
            @SuppressWarnings("unchecked")
            List<Long> questionIds = (List<Long>) request.get("questionIds");
            String statusStr = (String) request.get("status");
            
            if (questionIds == null || questionIds.isEmpty() || statusStr == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "请提供题目ID列表和状态"));
            }
            
            Question.QuestionStatus status = Question.QuestionStatus.valueOf(statusStr.toUpperCase());
            List<Question> updatedQuestions = questionService.batchUpdateQuestionStatus(questionIds, status, teacher.getId());
            
            return ResponseEntity.ok(Map.of(
                    "message", "批量更新题目状态成功",
                    "questions", updatedQuestions
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "批量更新题目状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取题目统计信息
     */
    @GetMapping("/questions/statistics")
    public ResponseEntity<?> getQuestionStatistics(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "未提供认证令牌"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "权限不足"));
            }
            
            QuestionService.QuestionStatistics statistics = questionService.getQuestionStatistics(teacher.getId());
            
            return ResponseEntity.ok(Map.of(
                    "message", "获取题目统计信息成功",
                    "statistics", statistics
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取题目统计信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取最近创建的题目
     */
    @GetMapping("/questions/recent")
    public ResponseEntity<?> getRecentQuestions(
            @RequestParam(defaultValue = "5") int limit,
            HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "未提供认证令牌"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "权限不足"));
            }
            
            List<Question> recentQuestions = questionService.getRecentQuestions(teacher.getId(), limit);
            
            return ResponseEntity.ok(Map.of(
                    "message", "获取最近题目成功",
                    "questions", recentQuestions
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取最近题目失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索题目
     */
    @GetMapping("/questions/search")
    public ResponseEntity<?> searchQuestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "未提供认证令牌"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "权限不足"));
            }
            
            Page<Question> questions = questionService.searchQuestions(teacher.getId(), keyword, page, size);
            
            return ResponseEntity.ok(Map.of(
                    "message", "搜索题目成功",
                    "questions", questions.getContent(),
                    "totalElements", questions.getTotalElements(),
                    "totalPages", questions.getTotalPages(),
                    "currentPage", questions.getNumber(),
                    "size", questions.getSize()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "搜索题目失败: " + e.getMessage()));
        }
    }
    
    // ==================== 成绩管理相关接口 ====================
    
    /**
     * 录入成绩
     */
    @PostMapping("/grades")
    public ResponseEntity<?> recordGrade(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "只有已审核通过的教师可以录入成绩"
                ));
            }
            
            // 验证必需参数
            if (request.get("teamId") == null || request.get("competitionId") == null || request.get("score") == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "缺少必需参数: teamId, competitionId, score"
                ));
            }
            
            Long teamId = Long.valueOf(request.get("teamId").toString());
            Long competitionId = Long.valueOf(request.get("competitionId").toString());
            BigDecimal score = new BigDecimal(request.get("score").toString());
            String remarks = (String) request.get("remarks");
            
            Grade grade = gradeService.recordGrade(teamId, competitionId, score, teacher.getId(), remarks);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "成绩录入成功",
                "data", grade
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "录入成绩失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取教师录入的成绩列表
     */
    @GetMapping("/grades")
    public ResponseEntity<?> getTeacherGrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "gradedTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long competitionId,
            HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "只有已审核通过的教师可以查看成绩"
                ));
            }
            
            Pageable pageable = PageRequest.of(page, size, 
                "desc".equals(sortDir) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());
            
            Page<Grade> grades;
            if (competitionId != null) {
                grades = gradeService.getGradesByGraderAndCompetition(teacher.getId(), competitionId, pageable);
            } else {
                grades = gradeService.getGradesByGrader(teacher.getId(), pageable);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", grades.getContent(),
                "totalElements", grades.getTotalElements(),
                "totalPages", grades.getTotalPages(),
                "currentPage", grades.getNumber()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取成绩列表失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 更新成绩
     */
    @PutMapping("/grades/{gradeId}")
    public ResponseEntity<?> updateGrade(
            @PathVariable Long gradeId,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "只有已审核通过的教师可以更新成绩"
                ));
            }
            
            // 验证必需参数
            if (request.get("score") == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "缺少必需参数: score"
                ));
            }
            
            BigDecimal newScore = new BigDecimal(request.get("score").toString());
            String remarks = (String) request.get("remarks");
            
            Grade updatedGrade = gradeService.updateGrade(gradeId, newScore, teacher.getId(), remarks);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "成绩更新成功",
                "data", updatedGrade
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "更新成绩失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 删除成绩
     */
    @DeleteMapping("/grades/{gradeId}")
    public ResponseEntity<?> deleteGrade(
            @PathVariable Long gradeId,
            HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "只有已审核通过的教师可以删除成绩"
                ));
            }
            
            gradeService.deleteGrade(gradeId, teacher.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "成绩删除成功"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "删除成绩失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 批量录入成绩
     */
    @PostMapping("/grades/batch")
    public ResponseEntity<?> batchRecordGrades(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "只有已审核通过的教师可以批量录入成绩"
                ));
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> gradeData = (List<Map<String, Object>>) request.get("grades");
            
            if (gradeData == null || gradeData.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "成绩数据不能为空"
                ));
            }
            
            List<Grade> grades = gradeService.batchRecordGrades(gradeData, teacher.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量录入成绩成功",
                "data", grades
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "批量录入成绩失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取竞赛成绩统计
     */
    @GetMapping("/grades/statistics")
    public ResponseEntity<?> getGradeStatistics(
            @RequestParam Long competitionId,
            HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "只有已审核通过的教师可以查看成绩统计"
                ));
            }
            
            Map<String, Object> statistics = gradeService.getCompetitionGradeStatistics(competitionId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", statistics
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取成绩统计失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 导出竞赛成绩
     */
    @GetMapping("/grades/export")
    public ResponseEntity<?> exportCompetitionGrades(
            @RequestParam Long competitionId,
            HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "只有已审核通过的教师可以导出成绩"
                ));
            }
            
            List<Grade> grades = gradeService.exportCompetitionGrades(competitionId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导出成绩成功",
                "data", grades
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "导出成绩失败: " + e.getMessage()
            ));
        }
    }
    
    // ==================== 仪表板统计相关接口 ====================
    
    /**
     * 获取教师统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getTeacherStats(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "只有已审核通过的教师可以查看统计数据"
                ));
            }
            
            // 获取教师创建的竞赛数量
            long competitionsCount = competitionService.countByCreator(teacher.getId());
            
            // 获取待审核的报名数量
            long pendingRegistrationsCount = registrationService.countPendingByTeacher(teacher.getId());
            
            // 获取教师创建的题目数量
            long questionsCount = questionService.countByCreator(teacher.getId());
            
            // 获取教师录入的成绩数量
            long gradesCount = gradeService.countByTeacher(teacher.getId());
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("competitions", competitionsCount);
            stats.put("pendingRegistrations", pendingRegistrationsCount);
            stats.put("questions", questionsCount);
            stats.put("grades", gradesCount);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取统计数据失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取教师最近活动
     */
    @GetMapping("/activities")
    public ResponseEntity<?> getTeacherActivities(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User teacher = userOpt.get();
            
            if (teacher.getRole() != User.UserRole.TEACHER || teacher.getStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "只有已审核通过的教师可以查看活动记录"
                ));
            }
            
            // 创建活动列表（模拟数据，实际项目中可以从数据库获取）
            List<Map<String, Object>> activities = new ArrayList<>();
            
            // 获取最近创建的竞赛
            List<Competition> recentCompetitions = competitionService.findRecentByCreator(teacher.getId(), 3);
            for (Competition competition : recentCompetitions) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", "comp_" + competition.getId());
                activity.put("description", "创建了竞赛: " + competition.getName());
                activity.put("createdAt", competition.getCreatedAt());
                activities.add(activity);
            }
            
            // 获取最近创建的题目
            List<Question> recentQuestions = questionService.findRecentByCreator(teacher.getId(), 3);
            for (Question question : recentQuestions) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", "quest_" + question.getId());
                activity.put("description", "创建了题目: " + question.getTitle());
                activity.put("createdAt", question.getCreatedAt());
                activities.add(activity);
            }
            
            // 获取最近录入的成绩
            List<Grade> recentGrades = gradeService.findRecentByTeacher(teacher.getId(), 3);
            for (Grade grade : recentGrades) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", "grade_" + grade.getId());
                activity.put("description", "录入了成绩: " + grade.getScore() + "分");
                activity.put("createdAt", grade.getCreatedAt());
                activities.add(activity);
            }
            
            // 按创建时间排序
            activities.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("createdAt");
                LocalDateTime timeB = (LocalDateTime) b.get("createdAt");
                return timeB.compareTo(timeA);
            });
            
            // 限制返回数量
            if (activities.size() > limit) {
                activities = activities.subList(0, limit);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", activities
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取活动记录失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 从请求中提取JWT token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}