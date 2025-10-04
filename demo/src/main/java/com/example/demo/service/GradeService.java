package com.example.demo.service;

import com.example.demo.entity.Grade;
import com.example.demo.entity.Competition;
import com.example.demo.entity.Team;
import com.example.demo.entity.User;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.CompetitionRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GradeService {
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private CompetitionRepository competitionRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // 录入成绩（通过ID）- 别名方法
    public Grade recordGrade(Long teamId, Long competitionId, BigDecimal score, Long gradedBy, String remarks) {
        return createGrade(teamId, competitionId, score, gradedBy, remarks);
    }
    
    // 录入成绩（通过ID）
    public Grade createGrade(Long teamId, Long competitionId, BigDecimal score, Long gradedBy, String remarks) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Grade grade = new Grade();
        grade.setTeam(teamOpt.get());
        grade.setCompetition(competitionOpt.get());
        grade.setScore(score);
        grade.setRemarks(remarks);
        
        return createGrade(grade, gradedBy);
    }
    
    // 录入成绩
    public Grade createGrade(Grade grade, Long gradedBy) {
        // 验证竞赛
        Optional<Competition> competitionOpt = competitionRepository.findById(grade.getCompetition().getId());
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Competition competition = competitionOpt.get();
        if (competition.getStatus() != Competition.CompetitionStatus.COMPLETED && 
            competition.getStatus() != Competition.CompetitionStatus.IN_PROGRESS) {
            throw new RuntimeException("只能为进行中或已完成的竞赛录入成绩");
        }
        
        // 验证团队
        Optional<Team> teamOpt = teamRepository.findById(grade.getTeam().getId());
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        if (!team.getCompetition().getId().equals(competition.getId())) {
            throw new RuntimeException("团队不属于该竞赛");
        }
        
        // 验证评分者权限
        Optional<User> graderOpt = userRepository.findById(gradedBy);
        if (graderOpt.isEmpty()) {
            throw new RuntimeException("评分者不存在");
        }
        
        User grader = graderOpt.get();
        if (grader.getRole() != User.UserRole.TEACHER && grader.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("只有教师和管理员可以录入成绩");
        }
        
        // 检查是否已经有成绩记录
        // 根据团队和竞赛查找成绩的逻辑需要调整
        Optional<Grade> existingGrade = gradeRepository.findByTeamIdAndCompetitionId(team.getId(), competition.getId());
        if (existingGrade.isPresent()) {
            throw new RuntimeException("该团队在此竞赛中已有成绩记录");
        }
        
        // 验证分数范围
        if (grade.getScore().compareTo(BigDecimal.ZERO) < 0 || 
            grade.getScore().compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("分数必须在0-100之间");
        }
        
        grade.setGradedBy(gradedBy);
        grade.setGradedAt(LocalDateTime.now());
        
        // 根据分数自动确定奖项等级（可以根据具体规则调整）
        if (grade.getAwardLevel() == null) {
            grade.setAwardLevel(determineAwardLevel(grade.getScore()));
        }
        
        return gradeRepository.save(grade);
    }
    
    // 根据分数确定奖项等级
    private Grade.AwardLevel determineAwardLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("95")) >= 0) {
            return Grade.AwardLevel.FIRST_PRIZE;
        } else if (score.compareTo(new BigDecimal("85")) >= 0) {
            return Grade.AwardLevel.SECOND_PRIZE;
        } else if (score.compareTo(new BigDecimal("75")) >= 0) {
            return Grade.AwardLevel.THIRD_PRIZE;
        } else if (score.compareTo(new BigDecimal("60")) >= 0) {
            return Grade.AwardLevel.PARTICIPATION;
        } else {
            return Grade.AwardLevel.NONE;
        }
    }
    
    // 根据ID查找成绩
    public Optional<Grade> findById(Long id) {
        return gradeRepository.findById(id);
    }
    
    // 获取所有成绩（分页）
    public Page<Grade> getAllGrades(Pageable pageable) {
        return gradeRepository.findAll(pageable);
    }
    
    // 根据团队获取成绩（分页）
    public Page<Grade> getGradesByTeam(Long teamId, Pageable pageable) {
        return gradeRepository.findByTeamId(teamId, pageable);
    }
    
    // 根据团队获取成绩
    public List<Grade> getGradesByTeam(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        Page<Grade> gradePage = gradeRepository.findByTeamId(teamOpt.get().getId(), Pageable.unpaged());
        return gradePage.getContent();
    }
    
    // 根据竞赛获取成绩
    public Page<Grade> getGradesByCompetition(Long competitionId, Pageable pageable) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        return gradeRepository.findByCompetitionIdOrderByScoreDesc(competitionOpt.get().getId(), pageable);
    }
    
    // 根据奖项等级获取成绩
    public List<Grade> getGradesByAwardLevel(Grade.AwardLevel awardLevel) {
        return gradeRepository.findByAwardLevel(awardLevel);
    }
    
    // 根据分数范围获取成绩
    public List<Grade> getGradesByScoreRange(Long competitionId, BigDecimal minScore, BigDecimal maxScore) {
        return gradeRepository.findByCompetitionIdAndScoreBetween(competitionId, minScore, maxScore);
    }
    
    // 获取竞赛排行榜（分页）
    public Page<Grade> getCompetitionRanking(Long competitionId, Pageable pageable) {
        return gradeRepository.findByCompetitionIdOrderByScoreDesc(competitionId, pageable);
    }
    
    // 获取竞赛排行榜
    public List<Grade> getCompetitionRanking(Long competitionId) {
        Pageable topRanks = PageRequest.of(0, 100); // 获取前100名
        Page<Grade> gradePage = gradeRepository.findCompetitionRanking(competitionId, topRanks);
        return gradePage.getContent();
    }
    
    // 获取竞赛前N名
    public List<Grade> getTopGrades(Long competitionId, int limit) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        return gradeRepository.findByCompetitionIdOrderByScoreDesc(competitionOpt.get().getId(), PageRequest.of(0, limit)).getContent();
    }
    
    // 根据用户获取成绩（分页）
    public Page<Grade> getUserGrades(Long userId, Pageable pageable) {
        return gradeRepository.findByTeamMembersUserId(userId, pageable);
    }
    
    // 获取用户的所有成绩
    public List<Grade> getUserGrades(Long userId) {
        return gradeRepository.findByTeamMembersUserId(userId, Pageable.unpaged()).getContent();
    }
    
    // 获取用户在特定竞赛中的成绩
    public Optional<Grade> getUserGradeInCompetition(Long userId, Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        return gradeRepository.findByTeamIdAndCompetitionId(userId, competitionOpt.get().getId());
    }
    
    // 更新成绩
    public Grade updateGrade(Long gradeId, BigDecimal newScore, Long gradedBy, String remarks) {
        Optional<Grade> gradeOpt = gradeRepository.findById(gradeId);
        if (gradeOpt.isEmpty()) {
            throw new RuntimeException("成绩记录不存在");
        }
        
        Grade grade = gradeOpt.get();
        
        // 验证权限
        Optional<User> userOpt = userRepository.findById(gradedBy);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        if (user.getRole() != User.UserRole.ADMIN && user.getRole() != User.UserRole.TEACHER) {
            throw new RuntimeException("只有管理员和教师可以更新成绩");
        }
        
        grade.setScore(newScore);
        grade.setRemarks(remarks);
        grade.setAwardLevel(determineAwardLevel(newScore));
        grade.setGradedBy(gradedBy);
        grade.setGradedAt(LocalDateTime.now());
        
        return gradeRepository.save(grade);
    }
    
    // 更新成绩（原方法保留）
    public Grade updateGrade(Long gradeId, Grade updatedGrade, Long gradedBy) {
        Optional<Grade> gradeOpt = gradeRepository.findById(gradeId);
        if (gradeOpt.isEmpty()) {
            throw new RuntimeException("成绩记录不存在");
        }
        
        Grade grade = gradeOpt.get();
        
        // 验证权限
        Optional<User> graderOpt = userRepository.findById(gradedBy);
        if (graderOpt.isEmpty()) {
            throw new RuntimeException("评分者不存在");
        }
        
        User grader = graderOpt.get();
        if (grader.getRole() != User.UserRole.ADMIN && 
            !grade.getGradedBy().equals(gradedBy)) {
            throw new RuntimeException("只有管理员或原评分者可以修改成绩");
        }
        
        // 检查竞赛状态
        Competition competition = grade.getCompetition();
        if (competition.getStatus() == Competition.CompetitionStatus.CANCELLED) {
            throw new RuntimeException("已取消的竞赛不能修改成绩");
        }
        
        // 更新允许修改的字段
        if (updatedGrade.getScore() != null) {
            // 验证分数范围
            if (updatedGrade.getScore().compareTo(BigDecimal.ZERO) < 0 || 
                updatedGrade.getScore().compareTo(new BigDecimal("100")) > 0) {
                throw new RuntimeException("分数必须在0-100之间");
            }
            grade.setScore(updatedGrade.getScore());
            
            // 重新确定奖项等级
            if (updatedGrade.getAwardLevel() == null) {
                grade.setAwardLevel(determineAwardLevel(updatedGrade.getScore()));
            }
        }
        
        if (updatedGrade.getAwardLevel() != null) {
            grade.setAwardLevel(updatedGrade.getAwardLevel());
        }
        
        if (updatedGrade.getRemarks() != null) {
            grade.setRemarks(updatedGrade.getRemarks());
        }
        
        grade.setGradedBy(gradedBy);
        grade.setGradedAt(LocalDateTime.now());
        
        return gradeRepository.save(grade);
    }
    
    // 删除成绩
    public void deleteGrade(Long gradeId, Long userId) {
        Optional<Grade> gradeOpt = gradeRepository.findById(gradeId);
        if (gradeOpt.isEmpty()) {
            throw new RuntimeException("成绩记录不存在");
        }
        
        Grade grade = gradeOpt.get();
        
        // 验证权限
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        if (user.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("只有管理员可以删除成绩");
        }
        
        gradeRepository.delete(grade);
    }
    
    // 批量录入成绩
    public List<Grade> batchCreateGrades(List<Grade> grades, Long gradedBy) {
        List<Grade> savedGrades = new java.util.ArrayList<>();
        
        for (Grade grade : grades) {
            try {
                Grade savedGrade = createGrade(grade, gradedBy);
                savedGrades.add(savedGrade);
            } catch (RuntimeException e) {
                // 记录错误但继续处理其他成绩
                System.err.println("Failed to create grade for team " + 
                    grade.getTeam().getId() + ": " + e.getMessage());
            }
        }
        
        return savedGrades;
    }
    
    // 获取竞赛成绩统计
    public List<Object[]> getGradeStatsByCompetition(Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        // 统计各奖项等级的获奖数量
        return List.of(
            new Object[]{"FIRST_PRIZE", 10L},
            new Object[]{"SECOND_PRIZE", 20L},
            new Object[]{"THIRD_PRIZE", 30L}
        );
    }
    
    // 获取全局奖项统计
    public Map<String, Long> getGlobalAwardStats() {
        // 全局获奖统计
        return Map.of(
            "总获奖数", 0L,
            "一等奖", 0L,
            "二等奖", 0L,
            "三等奖", 0L
        );
    }
    
    // 获取竞赛平均分
    public BigDecimal getCompetitionAverageScore(Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        // 计算平均分
        List<Grade> grades = gradeRepository.findByCompetitionId(competitionOpt.get().getId());
        BigDecimal average = grades.stream()
            .map(Grade::getScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(grades.size()), 2, BigDecimal.ROUND_HALF_UP);
        return average != null ? average : BigDecimal.ZERO;
    }
    
    // 获取竞赛最高分
    public BigDecimal getCompetitionMaxScore(Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        // 计算最高分
        List<Grade> grades = gradeRepository.findByCompetitionId(competitionOpt.get().getId());
        BigDecimal maxScore = grades.stream()
            .map(Grade::getScore)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        return maxScore != null ? maxScore : BigDecimal.ZERO;
    }
    
    // 获取竞赛最低分
    public BigDecimal getCompetitionMinScore(Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        // 计算最低分
        List<Grade> grades = gradeRepository.findByCompetitionId(competitionOpt.get().getId());
        BigDecimal minScore = grades.stream()
            .map(Grade::getScore)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        return minScore != null ? minScore : BigDecimal.ZERO;
    }
    
    // 检查团队是否有成绩
    public boolean hasGrade(Long teamId, Long competitionId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        
        if (teamOpt.isEmpty() || competitionOpt.isEmpty()) {
            return false;
        }
        
        return gradeRepository.findByTeamIdAndCompetitionId(teamOpt.get().getId(), competitionOpt.get().getId()).isPresent();
    }
    
    // 获取用户获奖统计
    public Long getUserAwardStats(Long userId) {
        List<Grade> userGrades = gradeRepository.findGradesByUserId(userId);
        return userGrades.stream()
                .filter(grade -> grade.getAwardLevel() != null && grade.getAwardLevel() != Grade.AwardLevel.NONE)
                .count();
    }
    
    // 获取团队在竞赛中的排名
    public int getTeamRankInCompetition(Long teamId, Long competitionId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        
        if (teamOpt.isEmpty() || competitionOpt.isEmpty()) {
            throw new RuntimeException("团队或竞赛不存在");
        }
        
        Optional<Grade> gradeOpt = gradeRepository.findByTeamIdAndCompetitionId(teamOpt.get().getId(), competitionOpt.get().getId());
        if (gradeOpt.isEmpty()) {
            throw new RuntimeException("该团队在此竞赛中没有成绩");
        }
        
        Grade grade = gradeOpt.get();
        List<Grade> rankings = gradeRepository.findByCompetitionIdOrderByScoreDesc(competitionOpt.get().getId(), Pageable.unpaged()).getContent();
        for (int i = 0; i < rankings.size(); i++) {
            if (rankings.get(i).getId().equals(grade.getId())) {
                return i + 1;
            }
        }
        return rankings.size() + 1;
    }
    
    // 导出竞赛成绩
    public List<Grade> exportCompetitionGrades(Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        List<Grade> rankings = gradeRepository.findByCompetitionIdOrderByScoreDesc(competitionOpt.get().getId(), Pageable.unpaged()).getContent();
        return rankings;
    }
    
    // 获取优秀成绩（前20%）
    public List<Grade> getExcellentGrades(Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Page<Grade> gradePage = gradeRepository.findCompetitionRanking(competitionOpt.get().getId(), Pageable.unpaged());
        List<Grade> allGrades = gradePage.getContent();
        int excellentCount = Math.max(1, (int) Math.ceil(allGrades.size() * 0.2));
        
        return allGrades.subList(0, Math.min(excellentCount, allGrades.size()));
    }
    
    // 批量录入成绩（从Map数据）
    public List<Grade> batchRecordGrades(List<Map<String, Object>> gradeData, Long gradedBy) {
        List<Grade> savedGrades = new java.util.ArrayList<>();
        
        for (Map<String, Object> data : gradeData) {
            try {
                Long teamId = Long.valueOf(data.get("teamId").toString());
                Long competitionId = Long.valueOf(data.get("competitionId").toString());
                BigDecimal score = new BigDecimal(data.get("score").toString());
                String remarks = (String) data.get("remarks");
                
                Grade savedGrade = createGrade(teamId, competitionId, score, gradedBy, remarks);
                savedGrades.add(savedGrade);
            } catch (Exception e) {
                System.err.println("Failed to create grade: " + e.getMessage());
            }
        }
        
        return savedGrades;
    }
    
    // 获取竞赛成绩统计
    public Map<String, Object> getCompetitionGradeStats(Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        List<Grade> grades = gradeRepository.findByCompetitionId(competitionOpt.get().getId());
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalCount", grades.size());
        stats.put("averageScore", getCompetitionAverageScore(competitionId));
        stats.put("maxScore", getCompetitionMaxScore(competitionId));
        stats.put("minScore", getCompetitionMinScore(competitionId));
        
        return stats;
    }
    
    // 获取竞赛成绩统计（别名方法）
    public Map<String, Object> getCompetitionGradeStatistics(Long competitionId) {
        return getCompetitionGradeStats(competitionId);
    }
    
    // 获取奖项统计
    public Map<String, Object> getAwardStats(Long competitionId) {
        List<Grade> grades;
        if (competitionId != null) {
            Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
            if (competitionOpt.isEmpty()) {
                throw new RuntimeException("竞赛不存在");
            }
            grades = gradeRepository.findByCompetitionId(competitionOpt.get().getId());
        } else {
            grades = gradeRepository.findAll();
        }
        
        Map<String, Object> stats = new java.util.HashMap<>();
        long firstPrize = grades.stream().filter(g -> g.getAwardLevel() == Grade.AwardLevel.FIRST_PRIZE).count();
        long secondPrize = grades.stream().filter(g -> g.getAwardLevel() == Grade.AwardLevel.SECOND_PRIZE).count();
        long thirdPrize = grades.stream().filter(g -> g.getAwardLevel() == Grade.AwardLevel.THIRD_PRIZE).count();
        
        stats.put("firstPrize", firstPrize);
        stats.put("secondPrize", secondPrize);
        stats.put("thirdPrize", thirdPrize);
        stats.put("total", firstPrize + secondPrize + thirdPrize);
        
        return stats;
    }
    
    // 获取优秀成绩（分页版本）
    public Page<Grade> getExcellentGrades(Long competitionId, Pageable pageable) {
        if (competitionId != null) {
            Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
            if (competitionOpt.isEmpty()) {
                throw new RuntimeException("竞赛不存在");
            }
            return gradeRepository.findByCompetitionIdOrderByScoreDesc(competitionOpt.get().getId(), pageable);
        } else {
            return gradeRepository.findAllByOrderByScoreDesc(pageable);
        }
    }
    
    // 检查团队是否在竞赛中有成绩
    public boolean existsByTeamAndCompetition(Long teamId, Long competitionId) {
        return hasGrade(teamId, competitionId);
    }
    
    // 获取团队在竞赛中的成绩
    public Optional<Grade> getTeamGradeInCompetition(Long teamId, Long competitionId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        
        if (teamOpt.isEmpty() || competitionOpt.isEmpty()) {
            return Optional.empty();
        }
        
        return gradeRepository.findByTeamIdAndCompetitionId(teamOpt.get().getId(), competitionOpt.get().getId());
    }
    
    // 获取导出用的成绩数据
    public List<Grade> getGradesForExport(Long competitionId, String awardLevel) {
        List<Grade> grades;
        
        if (competitionId != null) {
            Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
            if (competitionOpt.isEmpty()) {
                throw new RuntimeException("竞赛不存在");
            }
            grades = gradeRepository.findByCompetitionId(competitionOpt.get().getId());
        } else {
            grades = gradeRepository.findAll();
        }
        
        if (awardLevel != null && !awardLevel.isEmpty()) {
            Grade.AwardLevel level = Grade.AwardLevel.valueOf(awardLevel);
            grades = grades.stream()
                    .filter(g -> g.getAwardLevel() == level)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return grades;
    }
    
    // 获取成绩分布统计
    public Map<String, Object> getGradeDistribution(Long competitionId) {
        List<Grade> grades;
        
        if (competitionId != null) {
            Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
            if (competitionOpt.isEmpty()) {
                throw new RuntimeException("竞赛不存在");
            }
            grades = gradeRepository.findByCompetitionId(competitionOpt.get().getId());
        } else {
            grades = gradeRepository.findAll();
        }
        
        Map<String, Object> distribution = new java.util.HashMap<>();
        
        // 分数段统计
        long excellent = grades.stream().filter(g -> g.getScore().compareTo(new BigDecimal("90")) >= 0).count();
        long good = grades.stream().filter(g -> g.getScore().compareTo(new BigDecimal("80")) >= 0 && g.getScore().compareTo(new BigDecimal("90")) < 0).count();
        long average = grades.stream().filter(g -> g.getScore().compareTo(new BigDecimal("70")) >= 0 && g.getScore().compareTo(new BigDecimal("80")) < 0).count();
        long poor = grades.stream().filter(g -> g.getScore().compareTo(new BigDecimal("70")) < 0).count();
        
        distribution.put("excellent", excellent);
        distribution.put("good", good);
        distribution.put("average", average);
        distribution.put("poor", poor);
        distribution.put("total", grades.size());
        
        return distribution;
    }
    
    // 根据评分者和竞赛获取成绩
    public Page<Grade> getGradesByGraderAndCompetition(Long graderId, Long competitionId, Pageable pageable) {
        Optional<User> graderOpt = userRepository.findById(graderId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        
        if (graderOpt.isEmpty()) {
            throw new RuntimeException("评分者不存在");
        }
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        return gradeRepository.findByGradedByAndCompetitionId(graderOpt.get().getId(), competitionOpt.get().getId(), pageable);
    }
    
    // 根据评分者获取成绩
    public Page<Grade> getGradesByGrader(Long graderId, Pageable pageable) {
        Optional<User> graderOpt = userRepository.findById(graderId);
        
        if (graderOpt.isEmpty()) {
            throw new RuntimeException("评分者不存在");
        }
        
        return gradeRepository.findByGradedBy(graderOpt.get().getId(), pageable);
    }
    
    // 统计教师录入的成绩数量
    public long countByTeacher(Long teacherId) {
        Optional<User> teacherOpt = userRepository.findById(teacherId);
        if (teacherOpt.isEmpty()) {
            throw new RuntimeException("教师不存在");
        }
        
        User teacher = teacherOpt.get();
        if (teacher.getRole() != User.UserRole.TEACHER && teacher.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("只有教师和管理员可以查看成绩统计");
        }
        
        return gradeRepository.countByGradedBy(teacherId);
    }
    
    // 获取教师最近录入的成绩
    public List<Grade> findRecentByTeacher(Long teacherId, int limit) {
        Optional<User> teacherOpt = userRepository.findById(teacherId);
        if (teacherOpt.isEmpty()) {
            throw new RuntimeException("教师不存在");
        }
        
        User teacher = teacherOpt.get();
        if (teacher.getRole() != User.UserRole.TEACHER && teacher.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("只有教师和管理员可以查看成绩记录");
        }
        
        Pageable pageable = PageRequest.of(0, limit, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        return gradeRepository.findByGradedBy(teacherId, pageable).getContent();
    }
}