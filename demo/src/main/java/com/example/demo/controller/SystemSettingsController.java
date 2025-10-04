package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:5173"})
public class SystemSettingsController {

    // 获取系统设置
    @GetMapping("/settings")
    public ResponseEntity<?> getSystemSettings() {
        try {
            Map<String, Object> settings = new HashMap<>();
            
            // 基本设置
            Map<String, Object> basicSettings = new HashMap<>();
            basicSettings.put("systemName", "毕业设计管理系统");
            basicSettings.put("systemVersion", "1.0.0");
            basicSettings.put("systemDescription", "基于Spring Boot和Vue的毕业设计管理系统");
            basicSettings.put("maintenanceMode", false);
            basicSettings.put("registrationEnabled", true);
            settings.put("basic", basicSettings);
            
            // 邮件设置
            Map<String, Object> emailSettings = new HashMap<>();
            emailSettings.put("smtpHost", "smtp.example.com");
            emailSettings.put("smtpPort", 587);
            emailSettings.put("smtpUsername", "");
            emailSettings.put("smtpPassword", "");
            emailSettings.put("enableTLS", true);
            emailSettings.put("fromEmail", "noreply@example.com");
            emailSettings.put("fromName", "毕业设计管理系统");
            settings.put("email", emailSettings);
            
            // 文件上传设置
            Map<String, Object> fileSettings = new HashMap<>();
            fileSettings.put("maxFileSize", 10);
            fileSettings.put("allowedFileTypes", "pdf,doc,docx,xls,xlsx,ppt,pptx,zip,rar");
            fileSettings.put("uploadPath", "/uploads");
            fileSettings.put("enableFileCompression", true);
            settings.put("file", fileSettings);
            
            // 安全设置
            Map<String, Object> securitySettings = new HashMap<>();
            securitySettings.put("sessionTimeout", 30);
            securitySettings.put("maxLoginAttempts", 5);
            securitySettings.put("lockoutDuration", 15);
            securitySettings.put("passwordMinLength", 8);
            securitySettings.put("requireSpecialChars", true);
            securitySettings.put("enableTwoFactor", false);
            settings.put("security", securitySettings);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", settings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取系统设置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 更新系统设置
    @PutMapping("/settings")
    public ResponseEntity<?> updateSystemSettings(@RequestBody Map<String, Object> settingsData) {
        try {
            // 这里应该实现实际的设置保存逻辑
            // 目前只是返回成功响应
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "系统设置保存成功");
            response.put("data", settingsData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "保存系统设置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 重置系统设置
    @PostMapping("/settings/reset")
    public ResponseEntity<?> resetSystemSettings() {
        try {
            // 这里应该实现重置到默认设置的逻辑
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "系统设置已重置为默认值");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "重置系统设置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 获取系统状态
    @GetMapping("/settings/status")
    public ResponseEntity<?> getSystemStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 系统运行状态
            status.put("uptime", System.currentTimeMillis());
            status.put("javaVersion", System.getProperty("java.version"));
            status.put("osName", System.getProperty("os.name"));
            status.put("osVersion", System.getProperty("os.version"));
            
            // 内存使用情况
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            Map<String, Object> memoryInfo = new HashMap<>();
            memoryInfo.put("total", totalMemory / 1024 / 1024); // MB
            memoryInfo.put("used", usedMemory / 1024 / 1024); // MB
            memoryInfo.put("free", freeMemory / 1024 / 1024); // MB
            memoryInfo.put("usage", (double) usedMemory / totalMemory * 100); // 百分比
            
            status.put("memory", memoryInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", status);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取系统状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}