package com.example.demo.controller;

import com.example.demo.entity.CompetitionCategoryEntity;
import com.example.demo.service.CompetitionCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:5173"})
public class CompetitionCategoryController {

    @Autowired
    private CompetitionCategoryService categoryService;

    // 管理端：分类列表（含停用、模糊搜索、使用次数）
    @GetMapping("/api/admin/categories")
    public ResponseEntity<?> listAdminCategories(@RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String keyword) {
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", categoryService.listWithUsage(status, keyword)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // 公共端：仅返回启用的分类供选择
    @GetMapping("/api/competitions/categories")
    public ResponseEntity<?> listActiveCategories() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", categoryService.listActiveOptions()
        ));
    }

    @PostMapping("/api/admin/categories")
    public ResponseEntity<?> createCategory(@RequestBody CompetitionCategoryEntity payload) {
        try {
            var created = categoryService.create(payload);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "分类创建成功",
                    "data", created
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/api/admin/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CompetitionCategoryEntity payload) {
        try {
            var updated = categoryService.update(id, payload);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "分类更新成功",
                    "data", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PatchMapping("/api/admin/categories/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String status = payload.getOrDefault("status", "ACTIVE");
            var updated = categoryService.updateStatus(id, CompetitionCategoryEntity.Status.valueOf(status.toUpperCase()));
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "状态更新成功",
                    "data", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/api/admin/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.delete(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "分类删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
