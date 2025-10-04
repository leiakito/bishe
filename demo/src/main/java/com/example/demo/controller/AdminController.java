package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.service.ExcelExportService;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:5173"})
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ExcelExportService excelExportService;

    // 获取所有用户列表
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<User> users = userService.getAllUsers(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", users.getContent());
            response.put("currentPage", users.getNumber());
            response.put("totalItems", users.getTotalElements());
            response.put("totalPages", users.getTotalPages());
            response.put("hasNext", users.hasNext());
            response.put("hasPrevious", users.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取用户列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 获取用户统计信息
    @GetMapping("/users/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            Map<String, Object> stats = userService.getUserStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取用户统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 根据角色获取用户
    @GetMapping("/users/role/{role}")
    public ResponseEntity<?> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getUsersByRole(userRole, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", users.getContent());
            response.put("currentPage", users.getNumber());
            response.put("totalElements", users.getTotalElements());
            response.put("totalPages", users.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取用户列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 检查管理员账户状态
    @GetMapping("/check-admin")
    public ResponseEntity<?> checkAdminAccount() {
        try {
            var adminUser = userService.findByUsername("admin");
            Map<String, Object> response = new HashMap<>();
            
            if (adminUser.isPresent()) {
                User admin = adminUser.get();
                response.put("exists", true);
                response.put("username", admin.getUsername());
                response.put("email", admin.getEmail());
                response.put("realName", admin.getRealName());
                response.put("role", admin.getRole());
                response.put("status", admin.getStatus());
                response.put("createdAt", admin.getCreatedAt());
            } else {
                response.put("exists", false);
                response.put("message", "管理员账户不存在");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "检查管理员账户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 审核教师申请
    @PutMapping("/users/{id}/approve")
    public ResponseEntity<?> approveTeacher(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean approved = request.get("approved");
            if (approved == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "缺少approved参数");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            User updatedUser = userService.approveTeacher(id, approved);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", approved ? "教师申请已通过" : "教师申请已拒绝");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "审核失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 调试端点：检查教师数据
    @GetMapping("/debug/teachers")
    public ResponseEntity<?> debugTeachers() {
        try {
            List<User> allUsers = userService.getAllUsers();
            List<User> teachers = userService.getUsersByRole(User.UserRole.TEACHER);
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", allUsers.size());
            response.put("totalTeachers", teachers.size());
            response.put("teachers", teachers);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "调试失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 修改用户信息
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userUpdateData) {
        try {
            User updatedUser = userService.updateUser(id, userUpdateData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户信息修改成功");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "修改用户信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 删除用户
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户删除成功");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "删除用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 切换用户状态
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            if (status == null || status.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "缺少status参数");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 验证状态值
            User.UserStatus userStatus;
            try {
                userStatus = User.UserStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "无效的状态值: " + status);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            User updatedUser = userService.updateUserStatus(id, userStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户状态更新成功");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "更新用户状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 重置用户密码
    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        try {
            // 获取新密码，如果没有提供则使用默认密码
            String newPassword = "123456";
            if (request != null && request.containsKey("newPassword")) {
                newPassword = request.get("newPassword");
            }

            User updatedUser = userService.resetPassword(id, newPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "密码重置成功");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "重置密码失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 导出用户数据
    @GetMapping("/users/export")
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String collegeName,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String className) {
        try {
            // 构建筛选条件
            Map<String, String> filters = new HashMap<>();
            if (userType != null && !userType.isEmpty()) {
                filters.put("userType", userType);
            }
            if (status != null && !status.isEmpty()) {
                filters.put("status", status);
            }
            if (collegeName != null && !collegeName.isEmpty()) {
                filters.put("collegeName", collegeName);
            }
            if (major != null && !major.isEmpty()) {
                filters.put("major", major);
            }
            if (className != null && !className.isEmpty()) {
                filters.put("className", className);
            }
            
            // 生成Excel文件
            byte[] excelData = excelExportService.exportUsersToExcel(filters);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "users_export_" + System.currentTimeMillis() + ".xlsx");
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 导出学生数据
    @GetMapping("/students/export")
    public ResponseEntity<byte[]> exportStudents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir
    ) {
        try {
            Map<String, String> filters = new HashMap<>();
            filters.put("userType", "STUDENT");
            if (status != null) filters.put("status", status);
            if (keyword != null) filters.put("keyword", keyword);
            if (college != null) filters.put("college", college);
            if (grade != null) filters.put("grade", grade);
            if (sortBy != null) filters.put("sortBy", sortBy);
            if (sortDir != null) filters.put("sortDir", sortDir);

            byte[] excelData = excelExportService.exportStudentsToExcel(userService.getUsersWithFilters(filters));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "students_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");
            headers.setContentLength(excelData.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}