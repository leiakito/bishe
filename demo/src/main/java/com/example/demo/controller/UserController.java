package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.UserService;
import com.example.demo.service.ExcelExportService;
import com.example.demo.util.JwtUtil;
import com.example.demo.config.JwtAuthenticationFilter;
import com.example.demo.dto.UserProfileUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ExcelExportService excelExportService;
    
    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
        
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "注册成功",
                "data", registeredUser
            ));
        } catch (BusinessException e) {
            // 处理业务异常，返回具体的错误信息
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getErrorMessage(),
                "errorCode", e.getErrorCode()
            ));
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "系统发生未知错误，请联系管理员",
                "error", e.getMessage()
            ));
        }
    }
    
    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户名和密码不能为空"
                ));
            }
            
            Optional<User> userOpt = userService.login(username, password);
             if (userOpt.isPresent()) {
                 User user = userOpt.get();
                 
                 // 检查用户状态是否被禁用
                 if (user.getStatus() == User.UserStatus.DISABLED) {
                     return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                         "success", false,
                         "message", "账户已被禁用，请联系管理员"
                     ));
                 }
                 
                 // 检查教师用户的状态
                 if (user.getRole() == User.UserRole.TEACHER && user.getStatus() == User.UserStatus.PENDING) {
                     return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                         "success", false,
                         "message", "管理员未审核，请通过审核之后登录"
                     ));
                 }
                 
                 // 生成JWT token
                 String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().toString());
                 
                 return ResponseEntity.ok(Map.of(
                     "success", true,
                     "message", "登录成功",
                     "token", token,
                     "userInfo", user
                 ));
             } else {
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                     "success", false,
                     "message", "用户名或密码错误"
                 ));
             }
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "系统发生未知错误，请联系管理员",
                "error", e.getMessage()
            ));
        }
    }
    
    // 获取当前用户信息
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            Long currentUserId = JwtAuthenticationFilter.getCurrentUserId();
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "用户未认证"
                ));
            }
            
            Optional<User> user = userService.findById(currentUserId);
            if (user.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", user.get()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户信息失败"
            ));
        }
    }
    
    // 更新当前用户信息
    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentUserProfile(@Valid @RequestBody UserProfileUpdateRequest updateRequest) {
        try {
            Long currentUserId = JwtAuthenticationFilter.getCurrentUserId();
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "用户未认证"
                ));
            }
            
            User updatedUser = userService.updateCurrentUserProfile(currentUserId, updateRequest);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户信息更新成功",
                "data", updatedUser
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "更新用户信息失败"
            ));
        }
    }
    
    // 根据ID获取用户信息
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userService.findById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", user.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户信息失败"
            ));
        }
    }
    
    // 获取所有用户（分页）
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<User> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", users.getContent(),
                "totalElements", users.getTotalElements(),
                "totalPages", users.getTotalPages(),
                "currentPage", users.getNumber(),
                "size", users.getSize()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户列表失败"
            ));
        }
    }
    
    // 根据角色获取用户
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(
            @PathVariable User.UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getUsersByRole(role, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", users.getContent(),
                "totalElements", users.getTotalElements(),
                "totalPages", users.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户列表失败"
            ));
        }
    }
    
    // 根据状态获取用户
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getUsersByStatus(
            @PathVariable User.UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getUsersByStatus(status, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", users.getContent(),
                "totalElements", users.getTotalElements(),
                "totalPages", users.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户列表失败"
            ));
        }
    }
    
    // 搜索用户
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.searchUsers(keyword, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", users.getContent(),
                "totalElements", users.getTotalElements(),
                "totalPages", users.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "搜索用户失败"
            ));
        }
    }
    
    // 更新用户信息
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户信息更新成功",
                "data", updatedUser
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 审核教师申请
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveTeacher(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean approved = request.get("approved");
            if (approved == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "审核结果不能为空"
                ));
            }
            
            User user;
            if (approved) {
                user = userService.approveTeacher(id, 1L); // 假设审核者ID为1
            } else {
                user = userService.rejectTeacher(id, 1L); // 假设审核者ID为1
            }
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", approved ? "教师申请已通过" : "教师申请已拒绝",
                "data", user
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 激活/禁用用户
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "状态不能为空"
                ));
            }
            
            User.UserStatus status = User.UserStatus.valueOf(statusStr.toUpperCase());
            User user = userService.updateUserStatus(id, status);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户状态更新成功",
                "data", user
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
    
    // 修改密码
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "旧密码和新密码不能为空"
                ));
            }
            
            userService.changePassword(id, oldPassword, newPassword);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "密码修改成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 重置密码
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        try {
            // 注意：resetPassword方法返回User对象
            User user = userService.resetPassword(id, "123456"); // 默认密码
            String newPassword = "123456";
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "密码重置成功",
                "newPassword", newPassword
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户删除成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 检查用户名是否存在
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable String username) {
        try {
            boolean exists = userService.existsByUsername(username);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "exists", exists
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "检查用户名失败"
            ));
        }
    }
    
    // 检查邮箱是否存在
    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable String email) {
        try {
            boolean exists = userService.existsByEmail(email);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "exists", exists
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "检查邮箱失败"
            ));
        }
    }
    
    // 检查手机号是否存在
    @GetMapping("/check-phone/{phone}")
    public ResponseEntity<Map<String, Object>> checkPhone(@PathVariable String phone) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = userService.existsByPhone(phone);
        response.put("exists", exists);
        response.put("message", exists ? "手机号已存在" : "手机号可用");
        return ResponseEntity.ok(response);
    }
    
    // 检查学号是否存在
    @GetMapping("/check-student-id/{studentId}")
    public ResponseEntity<Map<String, Object>> checkStudentId(@PathVariable String studentId) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = userService.existsByStudentId(studentId);
        response.put("exists", exists);
        response.put("message", exists ? "学号已存在" : "学号可用");
        return ResponseEntity.ok(response);
    }
    

    
    // 验证Token有效性
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        try {
            String token = jwtUtil.getTokenFromRequest(request);
            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Map<String, Object> response = new HashMap<>();
                    response.put("valid", true);
                    response.put("user", user);
                    return ResponseEntity.ok(response);
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String token = jwtUtil.getTokenFromRequest(request);
            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    String newToken = jwtUtil.generateToken(username, user.getId(), user.getRole().name());
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("token", newToken);
                    response.put("userInfo", user);
                    return ResponseEntity.ok(response);
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Token无效或已过期");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "刷新Token失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            // 这里可以添加Token黑名单逻辑，目前简单返回成功
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登出成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "登出失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 用户修改密码
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest request, @RequestBody Map<String, String> passwordData) {
        try {
            String token = jwtUtil.getTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未提供认证token"
                ));
            }
            
            String username = jwtUtil.getUsernameFromToken(token);
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            User user = userOpt.get();
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "当前密码和新密码不能为空"
                ));
            }
            
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "新密码长度至少6个字符"
                ));
            }
            
            userService.changePassword(user.getId(), currentPassword, newPassword);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "密码修改成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    

    
    // 获取用户统计信息
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            Map<String, Object> stats = userService.getUserStatistics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取统计信息失败"
            ));
        }
    }
    
    // 获取待审核的教师申请
    @GetMapping("/pending-teachers")
    public ResponseEntity<?> getPendingTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<User> allPendingTeachers = userService.getPendingTeachers();
            
            // 手动分页
            int start = page * size;
            int end = Math.min(start + size, allPendingTeachers.size());
            List<User> pageContent = allPendingTeachers.subList(start, end);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", pageContent,
                "totalElements", allPendingTeachers.size(),
                "totalPages", (int) Math.ceil((double) allPendingTeachers.size() / size),
                "currentPage", page,
                "size", size
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取待审核教师列表失败"
            ));
        }
    }
    
    // 批量操作用户状态
    @PutMapping("/batch-status")
    public ResponseEntity<?> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) request.get("userIds");
            String statusStr = (String) request.get("status");
            
            if (userIds == null || userIds.isEmpty() || statusStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户ID列表和状态不能为空"
                ));
            }
            
            User.UserStatus status = User.UserStatus.valueOf(statusStr.toUpperCase());
            List<User> updatedUsers = userService.batchUpdateStatus(userIds, status);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量更新用户状态成功",
                "data", updatedUsers
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
    


    // 导出用户数据为Excel
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        try {
            List<User> users;
            
            // 根据筛选条件获取用户数据
            if (userType != null && !userType.isEmpty()) {
                User.UserRole role = User.UserRole.valueOf(userType.toUpperCase());
                if (status != null && !status.isEmpty()) {
                    User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
                    users = userService.getUsersByRoleAndStatus(role, userStatus);
                } else {
                    users = userService.getUsersByRole(role);
                }
            } else if (status != null && !status.isEmpty()) {
                User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
                users = userService.getUsersByStatus(userStatus);
            } else if (keyword != null && !keyword.isEmpty()) {
                users = userService.searchUsers(keyword);
            } else {
                users = userService.getAllUsers();
            }
            
            // 生成Excel文件
            byte[] excelData = excelExportService.exportUsersToExcel(users);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "用户数据_" + java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
                
        } catch (Exception e) {
            // 返回错误响应
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("导出失败".getBytes());
        }
    }
}