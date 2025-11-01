package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.ExamGradingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 成绩管理Controller
 */
@RestController
@RequestMapping("/api/scores")
@CrossOrigin(origins = "*")
public class ScoreController {

    private static final Logger logger = LoggerFactory.getLogger(ScoreController.class);

    @Autowired
    private ExamPaperRepository examPaperRepository;

    @Autowired
    private ExamAnswerRepository examAnswerRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ExamGradingService examGradingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private com.example.demo.service.GradeService gradeService;

    /**
     * 获取待评分列表
     */
    @GetMapping("/pending-grading")
    public ResponseEntity<Map<String, Object>> getPendingGrading(
            @RequestParam(required = false) Long competitionId) {

        logger.info("获取待评分列表: competitionId={}", competitionId);

        try {
            List<ExamPaper> pendingPapers;

            if (competitionId != null) {
                pendingPapers = examPaperRepository.findPendingGradingByCompetitionId(competitionId);
            } else {
                pendingPapers = examPaperRepository.findPendingGrading();
            }

            List<Map<String, Object>> paperList = new ArrayList<>();

            for (ExamPaper paper : pendingPapers) {
                Map<String, Object> paperData = new HashMap<>();
                paperData.put("paperId", paper.getId());
                paperData.put("competitionId", paper.getCompetitionId());
                paperData.put("participantType", paper.getParticipantType());
                paperData.put("submitTime", paper.getSubmitTime());
                paperData.put("objectiveScore", paper.getObjectiveScore());

                // 获取参赛者名称
                if (paper.getParticipantType() == ExamPaper.ParticipantType.TEAM) {
                    teamRepository.findById(paper.getParticipantId()).ifPresent(team -> {
                        paperData.put("participantName", team.getName());
                    });
                } else {
                    // 获取个人参赛者姓名
                    userRepository.findById(paper.getParticipantId()).ifPresent(user -> {
                        paperData.put("participantName", user.getRealName() + " (" + user.getUsername() + ")");
                    });
                }

                // 统计主观题数量
                long pendingCount = examAnswerRepository.countPendingGradingByExamPaperId(paper.getId());
                long gradedCount = examAnswerRepository.countGradedByExamPaperId(paper.getId());

                paperData.put("pendingCount", pendingCount);
                paperData.put("gradedCount", gradedCount);
                paperData.put("totalCount", paper.getTotalQuestionCount());

                paperList.add(paperData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", paperList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取待评分列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 人工评分
     */
    @PostMapping("/manual-grade")
    public ResponseEntity<Map<String, Object>> manualGrade(
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {

        // 获取当前登录用户
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Long paperId = ((Number) requestBody.get("paperId")).longValue();

        logger.info("开始人工评分: paperId={}, gradedBy={}", paperId, currentUser.getId());

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> answers = (List<Map<String, Object>>) requestBody.get("answers");

            Map<Long, BigDecimal> answerScores = new HashMap<>();
            Map<Long, String> answerRemarks = new HashMap<>();

            for (Map<String, Object> answer : answers) {
                Long questionId = ((Number) answer.get("questionId")).longValue();
                BigDecimal score = new BigDecimal(answer.get("score").toString());
                String remarks = (String) answer.get("remarks");

                answerScores.put(questionId, score);
                if (remarks != null) {
                    answerRemarks.put(questionId, remarks);
                }
            }

            examGradingService.manualGrade(paperId, answerScores, answerRemarks, currentUser.getId());

            // 获取更新后的考卷信息
            ExamPaper paper = examPaperRepository.findById(paperId).orElseThrow();

            Map<String, Object> data = new HashMap<>();
            data.put("paperId", paperId);
            data.put("totalScore", paper.getTotalScore());
            data.put("objectiveScore", paper.getObjectiveScore());
            data.put("subjectiveScore", paper.getSubjectiveScore());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "评分完成");
            response.put("data", data);

            logger.info("人工评分完成: paperId={}, totalScore={}", paperId, paper.getTotalScore());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("人工评分失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 发布成绩
     */
    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publishScores(
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {

        Long competitionId = ((Number) requestBody.get("competitionId")).longValue();
        Boolean notifyStudents = (Boolean) requestBody.getOrDefault("notifyStudents", false);

        // 获取当前登录用户信息
        String username = authentication != null ? authentication.getName() : "unknown";
        logger.info("发布成绩请求: competitionId={}, notifyStudents={}, requestUser={}", 
            competitionId, notifyStudents, username);

        // 验证认证信息
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("发布成绩失败 - 用户未认证: competitionId={}", competitionId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户未认证，请重新登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 获取该竞赛所有已评分的考卷
            List<ExamPaper> gradedPapers = examPaperRepository
                    .findByCompetitionIdAndPaperStatus(competitionId, ExamPaper.PaperStatus.GRADED);

            if (gradedPapers.isEmpty()) {
                throw new RuntimeException("没有已评分的考卷");
            }

            int publishedCount = 0;
            double totalScore = 0;
            double maxScore = 0;
            double minScore = Double.MAX_VALUE;

            // 为每个考卷创建或更新Grade记录
            for (ExamPaper paper : gradedPapers) {
                // 检查是否已存在Grade记录
                Optional<Grade> existingGrade = gradeRepository
                        .findByTeamIdAndCompetitionId(paper.getParticipantId(), competitionId);

                Grade grade;
                if (existingGrade.isPresent()) {
                    grade = existingGrade.get();
                } else {
                    grade = new Grade();
                    Team team = teamRepository.findById(paper.getParticipantId())
                            .orElseThrow(() -> new RuntimeException("团队不存在"));
                    Competition competition = competitionRepository.findById(competitionId)
                            .orElseThrow(() -> new RuntimeException("竞赛不存在"));

                    grade.setTeam(team);
                    grade.setCompetition(competition);
                }

                grade.setScore(paper.getTotalScore());
                grade.setIsFinal(true);
                grade.setGradedAt(LocalDateTime.now());

                // 获取当前登录用户
                User currentUser = userRepository.findByUsername(username)
                        .orElseThrow(() -> {
                            logger.error("发布成绩失败 - 用户不存在: username={}", username);
                            return new RuntimeException("当前用户不存在，请重新登录");
                        });
                
                logger.debug("设置成绩评分人: userId={}, username={}, role={}", 
                    currentUser.getId(), currentUser.getUsername(), currentUser.getRole());
                grade.setGradedBy(currentUser.getId());

                gradeRepository.save(grade);

                publishedCount++;
                double score = paper.getTotalScore().doubleValue();
                totalScore += score;
                maxScore = Math.max(maxScore, score);
                minScore = Math.min(minScore, score);

                logger.info("成绩已发布: paperId={}, teamId={}, score={}",
                        paper.getId(), paper.getParticipantId(), score);
            }

            // 发布后计算并持久化竞赛排名（支持并列分数同名次）
            logger.info("开始计算竞赛排名: competitionId={}", competitionId);
            gradeService.computeAndPersistRanking(competitionId);
            logger.info("竞赛排名计算完成: competitionId={}", competitionId);

            // 更新竞赛状态为已结束
            Competition competition = competitionRepository.findById(competitionId)
                    .orElseThrow(() -> new RuntimeException("竞赛不存在"));
            competition.setStatus(Competition.CompetitionStatus.COMPLETED);
            competitionRepository.save(competition);

            // TODO: 发送通知给学生

            double averageScore = totalScore / publishedCount;

            Map<String, Object> data = new HashMap<>();
            data.put("publishedCount", publishedCount);
            data.put("averageScore", averageScore);
            data.put("highestScore", maxScore);
            data.put("lowestScore", minScore);
            data.put("competitionStatus", competition.getStatus());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("成绩已发布,共%d份", publishedCount));
            response.put("data", data);

            logger.info("成绩发布完成: competitionId={}, count={}, avgScore={}",
                    competitionId, publishedCount, averageScore);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("发布成绩失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取我的成绩（学生查看自己的成绩）
     */
    @GetMapping("/my-scores")
    public ResponseEntity<Map<String, Object>> getMyScores(Authentication authentication) {
        // 获取当前登录用户
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        logger.info("获取我的成绩: userId={}", currentUser.getId());

        try {
            // 通过团队成员关系查询用户的所有成绩
            List<Grade> grades = gradeRepository.findGradesByUserId(currentUser.getId());

            List<Map<String, Object>> scoreList = new ArrayList<>();

            for (Grade grade : grades) {
                Map<String, Object> scoreData = new HashMap<>();
                scoreData.put("id", grade.getId());
                scoreData.put("competitionId", grade.getCompetition().getId());
                scoreData.put("competitionName", grade.getCompetition().getName());
                scoreData.put("teamId", grade.getTeam().getId());
                scoreData.put("teamName", grade.getTeam().getName());
                scoreData.put("score", grade.getScore());
                scoreData.put("ranking", grade.getRanking());
                scoreData.put("isFinal", grade.getIsFinal());
                scoreData.put("gradedAt", grade.getGradedAt());
                scoreData.put("certificateUrl", grade.getCertificateUrl());
                
                scoreList.add(scoreData);
            }

            logger.info("用户 {} 的成绩数: {}", currentUser.getId(), scoreList.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", scoreList);
            response.put("total", scoreList.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取成绩失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取已评分成绩列表（教师查看）
     * 优化：直接从ExamPaper表查询已评分的试卷，无需等待发布
     */
    @GetMapping("/graded-scores")
    public ResponseEntity<Map<String, Object>> getGradedScores(
            @RequestParam(required = false) Long competitionId) {

        logger.info("获取已评分成绩列表: competitionId={}", competitionId);

        try {
            List<Map<String, Object>> scoreList = new ArrayList<>();

            if (competitionId != null) {
                // 查询该竞赛所有已评分的考卷
                List<ExamPaper> gradedPapers = examPaperRepository
                        .findByCompetitionIdAndPaperStatus(competitionId, ExamPaper.PaperStatus.GRADED);

                logger.info("竞赛 {} 的已评分考卷数: {}", competitionId, gradedPapers.size());

                for (ExamPaper paper : gradedPapers) {
                    Map<String, Object> scoreData = new HashMap<>();

                    // 基本信息
                    scoreData.put("id", paper.getId());
                    scoreData.put("competitionId", paper.getCompetitionId());

                    // 获取竞赛名称
                    competitionRepository.findById(paper.getCompetitionId()).ifPresent(comp -> {
                        scoreData.put("competitionName", comp.getName());
                    });

                    // 只处理团队参赛
                    if (paper.getParticipantType() == ExamPaper.ParticipantType.TEAM) {
                        Long teamId = paper.getParticipantId();
                        scoreData.put("teamId", teamId);

                        // 获取团队信息
                        teamRepository.findById(teamId).ifPresent(team -> {
                            scoreData.put("teamName", team.getName());
                        });

                        // 获取团队成员信息
                        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
                        List<Map<String, Object>> memberList = new ArrayList<>();
                        for (TeamMember member : members) {
                            Map<String, Object> memberData = new HashMap<>();
                            memberData.put("userId", member.getUser().getId());
                            memberData.put("username", member.getUser().getUsername());
                            memberData.put("realName", member.getUser().getRealName());
                            memberData.put("role", member.getRole());
                            memberList.add(memberData);
                        }
                        scoreData.put("members", memberList);

                        // 成绩信息
                        scoreData.put("score", paper.getTotalScore());

                        // 检查是否已发布成绩（存在Grade记录）
                        Optional<Grade> gradeOpt = gradeRepository
                                .findByTeamIdAndCompetitionId(teamId, competitionId);

                        if (gradeOpt.isPresent()) {
                            Grade grade = gradeOpt.get();
                            scoreData.put("ranking", grade.getRanking());
                            scoreData.put("isFinal", grade.getIsFinal());
                            scoreData.put("gradedAt", grade.getGradedAt());
                        } else {
                            // 未发布成绩
                            scoreData.put("ranking", null);
                            scoreData.put("isFinal", false);
                            scoreData.put("gradedAt", paper.getSubmitTime());
                        }

                        scoreList.add(scoreData);
                    }
                }
            } else {
                // 获取所有已评分的考卷
                List<ExamPaper> gradedPapers = examPaperRepository
                        .findByPaperStatus(ExamPaper.PaperStatus.GRADED);

                for (ExamPaper paper : gradedPapers) {
                    // 同样的处理逻辑...
                    if (paper.getParticipantType() == ExamPaper.ParticipantType.TEAM) {
                        Map<String, Object> scoreData = new HashMap<>();
                        Long teamId = paper.getParticipantId();
                        Long compId = paper.getCompetitionId();
                        
                        scoreData.put("id", paper.getId());
                        scoreData.put("competitionId", compId);
                        scoreData.put("score", paper.getTotalScore());
                        scoreData.put("teamId", teamId);
                        
                        teamRepository.findById(teamId).ifPresent(team -> {
                            scoreData.put("teamName", team.getName());
                        });
                        
                        competitionRepository.findById(compId).ifPresent(comp -> {
                            scoreData.put("competitionName", comp.getName());
                        });

                        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
                        List<Map<String, Object>> memberList = new ArrayList<>();
                        for (TeamMember member : members) {
                            Map<String, Object> memberData = new HashMap<>();
                            memberData.put("userId", member.getUser().getId());
                            memberData.put("username", member.getUser().getUsername());
                            memberData.put("realName", member.getUser().getRealName());
                            memberData.put("role", member.getRole());
                            memberList.add(memberData);
                        }
                        scoreData.put("members", memberList);

                        // 检查是否已发布成绩（存在Grade记录）
                        Optional<Grade> gradeOpt = gradeRepository
                                .findByTeamIdAndCompetitionId(teamId, compId);

                        if (gradeOpt.isPresent()) {
                            Grade grade = gradeOpt.get();
                            scoreData.put("ranking", grade.getRanking());
                            scoreData.put("isFinal", grade.getIsFinal());
                            scoreData.put("gradedAt", grade.getGradedAt());
                        } else {
                            // 未发布成绩
                            scoreData.put("ranking", null);
                            scoreData.put("isFinal", false);
                            scoreData.put("gradedAt", paper.getSubmitTime());
                        }

                        scoreList.add(scoreData);
                    }
                }
            }

            logger.info("已评分成绩数: {}", scoreList.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", scoreList);
            response.put("total", scoreList.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取已评分成绩列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取竞赛排名列表
     * 返回指定竞赛的所有成绩，按排名升序排列
     */
    @GetMapping("/competition-ranking")
    public ResponseEntity<Map<String, Object>> getCompetitionRanking(
            @RequestParam Long competitionId) {
        
        logger.info("获取竞赛排名: competitionId={}", competitionId);

        try {
            // 按排名升序查询成绩
            List<Grade> grades = gradeRepository.findByCompetitionIdOrderByRankingAsc(competitionId);
            
            List<Map<String, Object>> rankingList = new ArrayList<>();

            for (Grade grade : grades) {
                Map<String, Object> rankingData = new HashMap<>();
                
                // 基本信息
                rankingData.put("id", grade.getId());
                rankingData.put("competitionId", grade.getCompetition().getId());
                rankingData.put("competitionName", grade.getCompetition().getName());
                rankingData.put("teamId", grade.getTeam().getId());
                rankingData.put("teamName", grade.getTeam().getName());
                
                // 成绩和排名
                rankingData.put("score", grade.getScore());
                rankingData.put("ranking", grade.getRanking());
                
                // 发布状态和时间
                rankingData.put("isFinal", grade.getIsFinal());
                rankingData.put("gradedAt", grade.getGradedAt());
                
                // 获取团队成员信息
                List<TeamMember> members = teamMemberRepository.findByTeamId(grade.getTeam().getId());
                List<Map<String, Object>> memberList = new ArrayList<>();
                for (TeamMember member : members) {
                    Map<String, Object> memberData = new HashMap<>();
                    memberData.put("userId", member.getUser().getId());
                    memberData.put("username", member.getUser().getUsername());
                    memberData.put("realName", member.getUser().getRealName());
                    memberData.put("role", member.getRole());
                    memberList.add(memberData);
                }
                rankingData.put("members", memberList);
                
                rankingList.add(rankingData);
            }

            logger.info("竞赛排名查询成功: competitionId={}, count={}", competitionId, rankingList.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", rankingList);
            response.put("total", rankingList.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取竞赛排名失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 手动重新计算竞赛排名
     * 用于修复旧数据或重新计算排名
     */
    @PostMapping("/recalculate-ranking")
    public ResponseEntity<Map<String, Object>> recalculateRanking(
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {
        
        Long competitionId = ((Number) requestBody.get("competitionId")).longValue();
        
        logger.info("重新计算竞赛排名请求: competitionId={}", competitionId);

        try {
            // 验证竞赛是否存在
            Competition competition = competitionRepository.findById(competitionId)
                    .orElseThrow(() -> new RuntimeException("竞赛不存在"));

            // 重新计算并持久化排名
            gradeService.computeAndPersistRanking(competitionId);
            
            // 获取更新后的成绩数量
            List<Grade> grades = gradeRepository.findByCompetitionIdOrderByRankingAsc(competitionId);
            
            logger.info("竞赛排名重新计算完成: competitionId={}, gradesCount={}", 
                    competitionId, grades.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "排名重新计算完成");
            response.put("competitionId", competitionId);
            response.put("competitionName", competition.getName());
            response.put("gradesCount", grades.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("重新计算排名失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量重新计算所有竞赛的排名
     * 用于系统维护或数据修复
     */
    @PostMapping("/recalculate-all-rankings")
    public ResponseEntity<Map<String, Object>> recalculateAllRankings(Authentication authentication) {
        
        logger.info("批量重新计算所有竞赛排名");

        try {
            // 获取所有有成绩的竞赛
            List<Competition> competitions = competitionRepository.findAll();
            int processedCount = 0;
            int updatedGradesCount = 0;

            for (Competition competition : competitions) {
                List<Grade> grades = gradeRepository.findByCompetitionId(competition.getId());
                if (!grades.isEmpty()) {
                    gradeService.computeAndPersistRanking(competition.getId());
                    processedCount++;
                    updatedGradesCount += grades.size();
                    logger.info("已处理竞赛: competitionId={}, name={}, gradesCount={}", 
                            competition.getId(), competition.getName(), grades.size());
                }
            }

            logger.info("所有竞赛排名重新计算完成: processedCompetitions={}, totalGrades={}", 
                    processedCount, updatedGradesCount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "所有排名重新计算完成");
            response.put("processedCompetitions", processedCount);
            response.put("updatedGrades", updatedGradesCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("批量重新计算排名失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
