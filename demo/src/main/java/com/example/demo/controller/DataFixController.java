package com.example.demo.controller;

import com.example.demo.service.TeamDataFixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据修复 Controller
 * 提供数据修复相关的 API 端点
 */
@RestController
@RequestMapping("/api/admin/data-fix")
@CrossOrigin(origins = "*")
public class DataFixController {
    
    @Autowired
    private TeamDataFixService teamDataFixService;
    
    /**
     * 修复所有团队的 currentMembers 字段
     */
    @PostMapping("/fix-all-teams")
    public ResponseEntity<?> fixAllTeams() {
        try {
            teamDataFixService.fixAllTeamsCurrentMembers();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "团队数据修复完成，请查看控制台日志获取详细信息"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "修复失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 修复指定团队的 currentMembers 字段
     */
    @PostMapping("/fix-team/{teamId}")
    public ResponseEntity<?> fixTeam(@PathVariable Long teamId) {
        try {
            teamDataFixService.fixTeamCurrentMembers(teamId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "团队数据修复完成"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "修复失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 检查所有团队的数据一致性
     */
    @GetMapping("/check-teams")
    public ResponseEntity<?> checkTeams() {
        try {
            teamDataFixService.checkAllTeamsDataConsistency();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "数据一致性检查完成，请查看控制台日志获取详细信息"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "检查失败: " + e.getMessage()
            ));
        }
    }
}

