package com.example.demo.controller;

import com.example.demo.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/logs")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:5173"})
@PreAuthorize("hasRole('ADMIN')")
public class LogController {

    @Autowired
    private LogService logService;

    /**
     * 获取系统日志列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        System.out.println("🔍 [DEBUG] LogController.getLogs 被调用");
        System.out.println("🔍 [DEBUG] 请求参数: page=" + page + ", size=" + size + ", level=" + level + ", keyword=" + keyword);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
            Page<Map<String, Object>> logs = logService.getLogs(pageable, level, keyword, startDate, endDate);
            
            System.out.println("🔍 [DEBUG] 获取到的日志数量: " + logs.getContent().size());
            System.out.println("🔍 [DEBUG] 总日志数: " + logs.getTotalElements());
            
            // 返回符合前端期望的Spring Data Page格式
            Map<String, Object> response = new HashMap<>();
            response.put("content", logs.getContent());
            response.put("totalElements", logs.getTotalElements());
            response.put("totalPages", logs.getTotalPages());
            response.put("number", logs.getNumber());
            response.put("size", logs.getSize());
            response.put("first", logs.isFirst());
            response.put("last", logs.isLast());
            response.put("empty", logs.isEmpty());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取日志失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取日志统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getLogStats() {
        try {
            Map<String, Object> stats = logService.getLogStats();
            
            // 包装响应格式，符合前端期望的ApiResponse格式
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "获取日志统计成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "获取日志统计失败: " + e.getMessage());
            errorResponse.put("message", "获取日志统计失败");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 清空日志
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearLogs() {
        try {
            logService.clearLogs();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "日志清空成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "清空日志失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 导出日志
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        try {
            byte[] logData = logService.exportLogs(level, keyword, startDate, endDate);
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=system-logs.txt")
                    .header("Content-Type", "text/plain")
                    .body(logData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取日志级别列表
     */
    @GetMapping("/levels")
    public ResponseEntity<List<String>> getLogLevels() {
        try {
            List<String> levels = logService.getLogLevels();
            return ResponseEntity.ok(levels);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}