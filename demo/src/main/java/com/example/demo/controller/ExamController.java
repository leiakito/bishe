package com.example.demo.controller;

import com.example.demo.dto.DistributionResultDTO;
import com.example.demo.dto.GradingResultDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.ExamDistributionService;
import com.example.demo.service.ExamGradingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试管理Controller
 */
@RestController
@RequestMapping("/api/exams")
@CrossOrigin(origins = "*")
public class ExamController {

    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);

    @Autowired
    private ExamPaperRepository examPaperRepository;

    @Autowired
    private ExamAnswerRepository examAnswerRepository;

    @Autowired
    private CompetitionQuestionRepository competitionQuestionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExamDistributionService examDistributionService;

    @Autowired
    private ExamGradingService examGradingService;

    /**
     * 触发题目下发 (管理员/教师操作)
     */
    @PostMapping("/competitions/{competitionId}/distribute")
    public ResponseEntity<Map<String, Object>> distributeExams(@PathVariable Long competitionId) {
        logger.info("开始下发考卷: competitionId={}", competitionId);

        try {
            DistributionResultDTO result = examDistributionService.distributeExamPapers(competitionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "成功下发考卷");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("下发考卷失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 学生获取自己的考卷
     */
    @GetMapping("/my-paper/{competitionId}")
    public ResponseEntity<Map<String, Object>> getMyPaper(
            @PathVariable Long competitionId,
            Authentication authentication) {

        // 获取当前登录用户
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        logger.info("获取我的考卷: competitionId={}, userId={}", competitionId, currentUser.getId());

        try {
            // 查找用户的考卷(个人赛或团队赛)
            ExamPaper paper = null;

            // 先尝试个人赛
            Optional<ExamPaper> individualPaper = examPaperRepository
                    .findByCompetitionIdAndParticipantTypeAndParticipantId(
                            competitionId,
                            ExamPaper.ParticipantType.INDIVIDUAL,
                            currentUser.getId()
                    );

            if (individualPaper.isPresent()) {
                paper = individualPaper.get();
            } else {
                // 查找用户在该竞赛中的团队
                Optional<Team> userTeam = teamRepository.findUserTeamInCompetition(currentUser.getId(), competitionId);

                if (userTeam.isPresent()) {
                    Optional<ExamPaper> teamPaper = examPaperRepository
                            .findByCompetitionIdAndParticipantTypeAndParticipantId(
                                    competitionId,
                                    ExamPaper.ParticipantType.TEAM,
                                    userTeam.get().getId()
                            );
                    if (teamPaper.isPresent()) {
                        paper = teamPaper.get();
                    }
                }
            }

            if (paper == null) {
                throw new RuntimeException("未找到您的考卷,请联系管理员");
            }

            // 获取题目列表
            List<CompetitionQuestion> competitionQuestions = competitionQuestionRepository
                    .findByCompetitionIdAndIsActiveTrueOrderByQuestionOrder(competitionId);

            List<Map<String, Object>> questions = new ArrayList<>();

            for (CompetitionQuestion cq : competitionQuestions) {
                questionRepository.findById(cq.getQuestionId()).ifPresent(question -> {
                    Map<String, Object> questionData = new HashMap<>();
                    questionData.put("id", question.getId());
                    questionData.put("order", cq.getQuestionOrder());
                    questionData.put("title", question.getTitle());
                    questionData.put("content", question.getContent());
                    questionData.put("type", question.getType());
                    questionData.put("score", cq.getQuestionScore());

                    // 只有选择题才返回选项
                    if (question.getType().toString().contains("CHOICE") ||
                            question.getType() == Question.QuestionType.TRUE_FALSE) {
                        questionData.put("options", question.getOptions());
                    }

                    // 不返回正确答案和解析(防作弊)
                    // questionData.put("correctAnswer", question.getCorrectAnswer());
                    // questionData.put("explanation", question.getExplanation());

                    questions.add(questionData);
                });
            }

            Map<String, Object> data = new HashMap<>();
            data.put("paperId", paper.getId());
            data.put("competitionId", competitionId);
            data.put("participantType", paper.getParticipantType());
            data.put("paperStatus", paper.getPaperStatus());
            data.put("startTime", paper.getStartTime());
            data.put("totalQuestionCount", paper.getTotalQuestionCount());
            data.put("questions", questions);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取考卷失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 开始答题
     */
    @PostMapping("/{paperId}/start")
    public ResponseEntity<Map<String, Object>> startExam(
            @PathVariable Long paperId,
            HttpServletRequest request) {

        logger.info("开始答题: paperId={}", paperId);

        try {
            ExamPaper paper = examPaperRepository.findById(paperId)
                    .orElseThrow(() -> new RuntimeException("考卷不存在"));

            if (paper.getPaperStatus() != ExamPaper.PaperStatus.NOT_STARTED) {
                throw new RuntimeException("考卷已经开始或已提交");
            }

            paper.setPaperStatus(ExamPaper.PaperStatus.IN_PROGRESS);
            paper.setStartTime(LocalDateTime.now());
            paper.setIpAddress(request.getRemoteAddr());
            paper.setUserAgent(request.getHeader("User-Agent"));
            examPaperRepository.save(paper);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "答题已开始");
            response.put("data", Map.of("startTime", paper.getStartTime()));

            logger.info("答题开始成功: paperId={}, startTime={}", paperId, paper.getStartTime());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("开始答题失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 保存答案草稿
     */
    @PutMapping("/{paperId}/draft")
    public ResponseEntity<Map<String, Object>> saveDraft(
            @PathVariable Long paperId,
            @RequestBody Map<String, Object> requestBody) {

        logger.info("保存答案草稿: paperId={}", paperId);

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> answers = (List<Map<String, Object>>) requestBody.get("answers");

            for (Map<String, Object> answerData : answers) {
                Long questionId = ((Number) answerData.get("questionId")).longValue();
                String answerContent = (String) answerData.get("answerContent");

                Optional<ExamAnswer> existingAnswer = examAnswerRepository
                        .findByExamPaperIdAndQuestionId(paperId, questionId);

                if (existingAnswer.isPresent()) {
                    ExamAnswer answer = existingAnswer.get();
                    answer.setAnswerContent(answerContent);
                    examAnswerRepository.save(answer);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "答案已保存");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("保存答案草稿失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 提交答卷
     */
    @PostMapping("/{paperId}/submit")
    public ResponseEntity<Map<String, Object>> submitExam(
            @PathVariable Long paperId,
            @RequestBody Map<String, Object> requestBody) {

        logger.info("提交答卷: paperId={}", paperId);

        try {
            ExamPaper paper = examPaperRepository.findById(paperId)
                    .orElseThrow(() -> new RuntimeException("考卷不存在"));

            if (paper.getPaperStatus() == ExamPaper.PaperStatus.SUBMITTED ||
                    paper.getPaperStatus() == ExamPaper.PaperStatus.GRADED) {
                throw new RuntimeException("答卷已提交");
            }

            // 保存所有答案
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> answers = (List<Map<String, Object>>) requestBody.get("answers");

            for (Map<String, Object> answerData : answers) {
                Long questionId = ((Number) answerData.get("questionId")).longValue();
                String answerContent = (String) answerData.get("answerContent");

                Optional<ExamAnswer> existingAnswer = examAnswerRepository
                        .findByExamPaperIdAndQuestionId(paperId, questionId);

                if (existingAnswer.isPresent()) {
                    ExamAnswer answer = existingAnswer.get();
                    answer.setAnswerContent(answerContent);
                    examAnswerRepository.save(answer);
                }
            }

            // 更新考卷状态
            paper.setPaperStatus(ExamPaper.PaperStatus.SUBMITTED);
            paper.setSubmitTime(LocalDateTime.now());
            examPaperRepository.save(paper);

            // 自动评分
            GradingResultDTO gradingResult = examGradingService.autoGrade(paperId);

            Map<String, Object> data = new HashMap<>();
            data.put("paperId", paperId);
            data.put("submitTime", paper.getSubmitTime());
            data.put("paperStatus", paper.getPaperStatus());
            data.put("objectiveScore", gradingResult.getObjectiveScore());
            data.put("needManualGrading", gradingResult.getNeedManualGrading());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "答卷提交成功,正在自动评分中");
            response.put("data", data);

            logger.info("答卷提交成功: paperId={}, objectiveScore={}, needManualGrading={}",
                    paperId, gradingResult.getObjectiveScore(), gradingResult.getNeedManualGrading());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("提交答卷失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
