package com.example.demo.controller;

import com.example.demo.config.JwtAuthenticationFilter;
import com.example.demo.entity.Grade;
import com.example.demo.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/grades")
@CrossOrigin(origins = "*")
public class GradeController {
    
    @Autowired
    private GradeService gradeService;
    
    // 录入成绩
    @PostMapping
    public ResponseEntity<?> recordGrade(@RequestBody Map<String, Object> request) {
        try {
            Long teamId = Long.valueOf(request.get("teamId").toString());
            Long competitionId = Long.valueOf(request.get("competitionId").toString());
            BigDecimal score = new BigDecimal(request.get("score").toString());
            Long gradedBy = Long.valueOf(request.get("gradedBy").toString());
            String remarks = (String) request.get("remarks");
            
            if (teamId == null || competitionId == null || score == null || gradedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "团队ID、竞赛ID、分数和评分人不能为空"
                ));
            }
            
            Grade grade = gradeService.recordGrade(teamId, competitionId, score, gradedBy, remarks);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "成绩录入成功",
                "data", grade
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 根据ID获取成绩
    @GetMapping("/{id}")
    public ResponseEntity<?> getGradeById(@PathVariable Long id) {
        try {
            Optional<Grade> grade = gradeService.findById(id);
            if (grade.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", grade.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取成绩信息失败"
            ));
        }
    }
    
    // 获取所有成绩（分页）
    @GetMapping
    public ResponseEntity<?> getAllGrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "gradedTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Grade> grades = gradeService.getAllGrades(pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", grades.getContent(),
                "totalElements", grades.getTotalElements(),
                "totalPages", grades.getTotalPages(),
                "currentPage", grades.getNumber(),
                "size", grades.getSize()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取成绩列表失败"
            ));
        }
    }
    
    // 根据团队获取成绩
    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> getGradesByTeam(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("gradedTime").descending());
            Page<Grade> grades = gradeService.getGradesByTeam(teamId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", grades.getContent(),
                "totalElements", grades.getTotalElements(),
                "totalPages", grades.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队成绩失败"
            ));
        }
    }
    
    // 根据竞赛获取成绩
    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<?> getGradesByCompetition(
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("score").descending());
            Page<Grade> grades = gradeService.getGradesByCompetition(competitionId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", grades.getContent(),
                "totalElements", grades.getTotalElements(),
                "totalPages", grades.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取竞赛成绩失败"
            ));
        }
    }
    
    // 根据奖项等级获取成绩
    @GetMapping("/award-level/{awardLevel}")
    public ResponseEntity<List<Grade>> getGradesByAwardLevel(
            @PathVariable Grade.AwardLevel awardLevel) {
        List<Grade> grades = gradeService.getGradesByAwardLevel(awardLevel);
        return ResponseEntity.ok(grades);
    }
    
    // 根据分数范围获取成绩
    @GetMapping("/score-range")
    public ResponseEntity<List<Grade>> getGradesByScoreRange(
            @RequestParam Long competitionId,
            @RequestParam BigDecimal minScore,
            @RequestParam BigDecimal maxScore) {
        List<Grade> grades = gradeService.getGradesByScoreRange(competitionId, minScore, maxScore);
        return ResponseEntity.ok(grades);
    }

    // 获取当前用户成绩
    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUserGrades(
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
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("gradedTime").descending());
            Page<Grade> grades = gradeService.getUserGrades(currentUserId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", grades.getContent(),
                "totalElements", grades.getTotalElements(),
                "totalPages", grades.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户成绩失败"
            ));
        }
    }
    
    // 获取竞赛排行榜
    @GetMapping("/competition/{competitionId}/ranking")
    public ResponseEntity<?> getCompetitionRanking(
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Grade> ranking = gradeService.getCompetitionRanking(competitionId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", ranking.getContent(),
                "totalElements", ranking.getTotalElements(),
                "totalPages", ranking.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取竞赛排行榜失败"
            ));
        }
    }
    
    // 获取用户成绩
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserGrades(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("gradedTime").descending());
            Page<Grade> grades = gradeService.getUserGrades(userId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", grades.getContent(),
                "totalElements", grades.getTotalElements(),
                "totalPages", grades.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取用户成绩失败"
            ));
        }
    }
    
    // 更新成绩
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGrade(@PathVariable Long id,
                                        @RequestBody Map<String, Object> request) {
        try {
            BigDecimal newScore = new BigDecimal(request.get("score").toString());
            Long updatedBy = Long.valueOf(request.get("updatedBy").toString());
            String remarks = (String) request.get("remarks");
            
            if (newScore == null || updatedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "新分数和更新人不能为空"
                ));
            }
            
            Grade grade = gradeService.updateGrade(id, newScore, updatedBy, remarks);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "成绩更新成功",
                "data", grade
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 删除成绩
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGrade(@PathVariable Long id,
                                        @RequestParam Long deletedBy) {
        try {
            gradeService.deleteGrade(id, deletedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "成绩删除成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // 批量录入成绩
    @PostMapping("/batch")
    public ResponseEntity<?> batchRecordGrades(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> gradeData = (List<Map<String, Object>>) request.get("grades");
            Long gradedBy = Long.valueOf(request.get("gradedBy").toString());
            
            if (gradeData == null || gradeData.isEmpty() || gradedBy == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "成绩数据和评分人不能为空"
                ));
            }
            
            List<Grade> grades = gradeService.batchRecordGrades(gradeData, gradedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量录入成绩成功",
                "data", grades
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "批量录入成绩失败"
            ));
        }
    }
    
    // 获取竞赛统计信息
    @GetMapping("/competition/{competitionId}/stats")
    public ResponseEntity<?> getCompetitionGradeStats(@PathVariable Long competitionId) {
        try {
            Map<String, Object> stats = gradeService.getCompetitionGradeStats(competitionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取竞赛成绩统计失败"
            ));
        }
    }
    
    // 获取奖项统计
    @GetMapping("/award-stats")
    public ResponseEntity<?> getAwardStats(
            @RequestParam(required = false) Long competitionId) {
        try {
            Map<String, Object> stats = gradeService.getAwardStats(competitionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取奖项统计失败"
            ));
        }
    }
    
    // 获取优秀成绩
    @GetMapping("/excellent")
    public ResponseEntity<?> getExcellentGrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long competitionId) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("score").descending());
            Page<Grade> grades = gradeService.getExcellentGrades(competitionId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", grades.getContent(),
                "totalElements", grades.getTotalElements(),
                "totalPages", grades.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取优秀成绩失败"
            ));
        }
    }
    
    // 检查团队是否已有成绩
    @GetMapping("/check-grade")
    public ResponseEntity<?> checkGradeExists(@RequestParam Long teamId,
                                             @RequestParam Long competitionId) {
        try {
            boolean exists = gradeService.existsByTeamAndCompetition(teamId, competitionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "exists", exists
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "检查成绩状态失败"
            ));
        }
    }
    
    // 获取团队在竞赛中的成绩
    @GetMapping("/team-competition")
    public ResponseEntity<?> getTeamGradeInCompetition(@RequestParam Long teamId,
                                                      @RequestParam Long competitionId) {
        try {
            Optional<Grade> grade = gradeService.getTeamGradeInCompetition(teamId, competitionId);
            if (grade.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", grade.get()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", null,
                    "message", "该团队在此竞赛中暂无成绩"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取团队成绩失败"
            ));
        }
    }
    
    // 导出成绩数据
    @GetMapping("/export")
    public ResponseEntity<?> exportGrades(
            @RequestParam(required = false) Long competitionId,
            @RequestParam(required = false) String awardLevel) {
        try {
            // 这里应该返回文件下载，简化处理返回数据
            List<Grade> grades = gradeService.exportCompetitionGrades(competitionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导出数据准备完成",
                "data", grades
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "导出成绩数据失败"
            ));
        }
    }
    
    // 获取成绩分布统计
    @GetMapping("/distribution")
    public ResponseEntity<?> getGradeDistribution(
            @RequestParam(required = false) Long competitionId) {
        try {
            Map<String, Object> distribution = gradeService.getGradeDistribution(competitionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", distribution
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "获取成绩分布统计失败"
            ));
        }
    }
}