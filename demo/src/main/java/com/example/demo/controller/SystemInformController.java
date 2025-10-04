package com.example.demo.controller;

import com.example.demo.entity.SystemInform;
import com.example.demo.service.SystemInformService;
import com.example.demo.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 系统通知控制器
 * 提供系统通知的CRUD操作，无需JWT验证
 */
@RestController
@RequestMapping("/api/systeminform")
@CrossOrigin(origins = "*")
public class SystemInformController {

    @Autowired
    private SystemInformService systemInformService;

    /**
     * 创建新的系统通知
     * POST /api/systeminform
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(@Valid @RequestBody SystemInform systemInform) {
        Map<String, Object> response = new HashMap<>();
        try {
            SystemInform createdNotification = systemInformService.createNotification(systemInform);
            response.put("success", true);
            response.put("message", "通知创建成功");
            response.put("data", createdNotification);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建通知失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取所有系统通知
     * GET /api/systeminform
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SystemInform> notifications;
            
            if ("updatedAt".equals(sortBy)) {
                notifications = systemInformService.getAllNotificationsByUpdatedAt(pageable);
            } else {
                notifications = systemInformService.getAllNotificationsByCreatedAt(pageable);
            }
            
            response.put("success", true);
            response.put("message", "获取通知列表成功");
            response.put("data", notifications.getContent());
            response.put("totalElements", notifications.getTotalElements());
            response.put("totalPages", notifications.getTotalPages());
            response.put("currentPage", notifications.getNumber());
            response.put("pageSize", notifications.getSize());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取通知列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据ID获取系统通知
     * GET /api/systeminform/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getNotificationById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<SystemInform> notificationOpt = systemInformService.getNotificationById(id);
            if (!notificationOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "通知不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            SystemInform notification = notificationOpt.get();
            response.put("success", true);
            response.put("message", "获取通知详情成功");
            response.put("data", notification);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取通知详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取最新的系统通知
     * GET /api/systeminform/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestNotifications(
            @RequestParam(defaultValue = "5") int limit) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<SystemInform> notifications = systemInformService.getLatestNotifications(limit);
            response.put("success", true);
            response.put("message", "获取最新通知成功");
            response.put("data", notifications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取最新通知失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 搜索系统通知
     * GET /api/systeminform/search
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchNotifications(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SystemInform> notifications = systemInformService.searchNotificationsByContent(keyword, pageable);
            
            response.put("success", true);
            response.put("message", "搜索通知成功");
            response.put("data", notifications.getContent());
            response.put("totalElements", notifications.getTotalElements());
            response.put("totalPages", notifications.getTotalPages());
            response.put("currentPage", notifications.getNumber());
            response.put("pageSize", notifications.getSize());
            response.put("keyword", keyword);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索通知失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新系统通知
     * PUT /api/systeminform/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateNotification(
            @PathVariable Long id, 
            @Valid @RequestBody SystemInform systemInform) {
        Map<String, Object> response = new HashMap<>();
        try {
            SystemInform updatedNotification = systemInformService.updateNotification(id, systemInform);
            response.put("success", true);
            response.put("message", "通知更新成功");
            response.put("data", updatedNotification);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新通知失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除系统通知
     * DELETE /api/systeminform/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            systemInformService.deleteNotification(id);
            response.put("success", true);
            response.put("message", "通知删除成功");
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除通知失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 批量删除系统通知
     * DELETE /api/systeminform/batch
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> deleteNotifications(@RequestBody List<Long> ids) {
        Map<String, Object> response = new HashMap<>();
        try {
            systemInformService.deleteNotifications(ids);
            response.put("success", true);
            response.put("message", "批量删除通知成功");
            response.put("deletedCount", ids.size());
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量删除通知失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取通知统计信息
     * GET /api/systeminform/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            long totalCount = systemInformService.getTotalNotificationCount();
            List<SystemInform> latestNotifications = systemInformService.getLatestNotifications(3);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCount", totalCount);
            stats.put("latestNotifications", latestNotifications);
            
            response.put("success", true);
            response.put("message", "获取通知统计成功");
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取通知统计失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 清空所有系统通知
     * DELETE /api/systeminform/all
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllNotifications() {
        Map<String, Object> response = new HashMap<>();
        try {
            systemInformService.deleteAllNotifications();
            response.put("success", true);
            response.put("message", "清空所有通知成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清空通知失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}