package com.example.demo.controller;

import com.example.demo.entity.Competition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DataController {

    // 获取专业列表
    @GetMapping("/majors")
    public ResponseEntity<?> getMajors() {
        try {
            // 返回常见的专业列表
            List<Map<String, Object>> majors = Arrays.asList(
                Map.of("id", 1, "name", "计算机科学与技术", "code", "CS"),
                Map.of("id", 2, "name", "软件工程", "code", "SE"),
                Map.of("id", 3, "name", "信息安全", "code", "IS"),
                Map.of("id", 4, "name", "数据科学与大数据技术", "code", "DS"),
                Map.of("id", 5, "name", "人工智能", "code", "AI"),
                Map.of("id", 6, "name", "电子信息工程", "code", "EIE"),
                Map.of("id", 7, "name", "通信工程", "code", "CE"),
                Map.of("id", 8, "name", "自动化", "code", "AUTO"),
                Map.of("id", 9, "name", "机械工程", "code", "ME"),
                Map.of("id", 10, "name", "电气工程及其自动化", "code", "EE"),
                Map.of("id", 11, "name", "数学与应用数学", "code", "MATH"),
                Map.of("id", 12, "name", "物理学", "code", "PHYSICS"),
                Map.of("id", 13, "name", "化学", "code", "CHEMISTRY"),
                Map.of("id", 14, "name", "生物科学", "code", "BIOLOGY"),
                Map.of("id", 15, "name", "英语", "code", "ENGLISH"),
                Map.of("id", 16, "name", "设计学", "code", "DESIGN"),
                Map.of("id", 17, "name", "工商管理", "code", "BA"),
                Map.of("id", 18, "name", "经济学", "code", "ECONOMICS"),
                Map.of("id", 19, "name", "法学", "code", "LAW"),
                Map.of("id", 20, "name", "其他", "code", "OTHER")
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", majors,
                "message", "获取专业列表成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "获取专业列表失败: " + e.getMessage()
            ));
        }
    }

    // 获取竞赛类别列表
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        try {
            // 基于Competition.CompetitionCategory枚举返回类别列表
            List<Map<String, Object>> categories = Arrays.stream(Competition.CompetitionCategory.values())
                .map(category -> {
                    String displayName = getCategoryDisplayName(category);
                    Map<String, Object> categoryMap = new HashMap<>();
                    categoryMap.put("id", category.ordinal() + 1);
                    categoryMap.put("name", displayName);
                    categoryMap.put("code", category.name());
                    return categoryMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", categories,
                "message", "获取竞赛类别成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "获取竞赛类别失败: " + e.getMessage()
            ));
        }
    }

    // 获取竞赛级别列表
    @GetMapping("/levels")
    public ResponseEntity<?> getLevels() {
        try {
            List<Map<String, Object>> levels = Arrays.stream(Competition.CompetitionLevel.values())
                .map(level -> {
                    String displayName = getLevelDisplayName(level);
                    Map<String, Object> levelMap = new HashMap<>();
                    levelMap.put("id", level.ordinal() + 1);
                    levelMap.put("name", displayName);
                    levelMap.put("code", level.name());
                    return levelMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", levels,
                "message", "获取竞赛级别成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "获取竞赛级别失败: " + e.getMessage()
            ));
        }
    }

    // 获取学院列表
    @GetMapping("/departments")
    public ResponseEntity<?> getDepartments() {
        try {
            // 返回常见的学院列表
            List<Map<String, Object>> departments = Arrays.asList(
                Map.of("id", 1, "name", "计算机科学与技术学院", "code", "CS"),
                Map.of("id", 2, "name", "电子信息工程学院", "code", "EIE"),
                Map.of("id", 3, "name", "机械工程学院", "code", "ME"),
                Map.of("id", 4, "name", "数学与统计学院", "code", "MATH"),
                Map.of("id", 5, "name", "物理与电子学院", "code", "PHYSICS"),
                Map.of("id", 6, "name", "化学与化工学院", "code", "CHEMISTRY"),
                Map.of("id", 7, "name", "生命科学学院", "code", "BIOLOGY"),
                Map.of("id", 8, "name", "外国语学院", "code", "FOREIGN"),
                Map.of("id", 9, "name", "艺术与设计学院", "code", "ART"),
                Map.of("id", 10, "name", "经济管理学院", "code", "ECONOMICS"),
                Map.of("id", 11, "name", "法学院", "code", "LAW"),
                Map.of("id", 12, "name", "教育学院", "code", "EDUCATION"),
                Map.of("id", 13, "name", "医学院", "code", "MEDICINE"),
                Map.of("id", 14, "name", "建筑与土木工程学院", "code", "CIVIL"),
                Map.of("id", 15, "name", "材料科学与工程学院", "code", "MATERIAL")
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", departments,
                "message", "获取学院列表成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "获取学院列表失败: " + e.getMessage()
            ));
        }
    }

    // 获取竞赛类别的显示名称
    private String getCategoryDisplayName(Competition.CompetitionCategory category) {
        switch (category) {
            case PROGRAMMING: return "编程竞赛";
            case MATHEMATICS: return "数学竞赛";
            case PHYSICS: return "物理竞赛";
            case CHEMISTRY: return "化学竞赛";
            case BIOLOGY: return "生物竞赛";
            case ENGLISH: return "英语竞赛";
            case DESIGN: return "设计竞赛";
            case INNOVATION: return "创新竞赛";
            case OTHER: return "其他";
            default: return category.name();
        }
    }

    // 获取竞赛级别的显示名称
    private String getLevelDisplayName(Competition.CompetitionLevel level) {
        switch (level) {
            case SCHOOL: return "校级";
            case CITY: return "市级";
            case PROVINCE: return "省级";
            case NATIONAL: return "国家级";
            case INTERNATIONAL: return "国际级";
            default: return level.name();
        }
    }
}