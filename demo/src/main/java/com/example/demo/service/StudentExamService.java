package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 学生答题服务
 */
@Service
@Transactional
public class StudentExamService {

    private static final Logger logger = LoggerFactory.getLogger(StudentExamService.class);

    @Autowired
    private ExamPaperRepository examPaperRepository;

    @Autowired
    private ExamAnswerRepository examAnswerRepository;

    @Autowired
    private CompetitionQuestionRepository competitionQuestionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    /**
     * 开始答题 - 创建或获取考卷
     */
    public Map<String, Object> startExam(Long competitionId, Long userId) {
        logger.info("用户{}开始竞赛{}的答题", userId, competitionId);

        // 检查竞赛是否存在
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new RuntimeException("竞赛不存在"));

        // 检查竞赛状态
        if (competition.getStatus() != Competition.CompetitionStatus.IN_PROGRESS &&
            competition.getStatus() != Competition.CompetitionStatus.ONGOING) {
            throw new RuntimeException("竞赛还未开始或已结束");
        }

        // 检查用户是否已报名（支持个人报名和团队报名）
        Registration registration = checkUserRegistration(competitionId, userId);
        if (registration == null) {
            throw new RuntimeException("您未报名此竞赛");
        }

        if (registration.getStatus() != Registration.RegistrationStatus.APPROVED) {
            throw new RuntimeException("您的报名尚未通过审核，当前状态：" + registration.getStatus());
        }

        // 根据报名类型确定参赛类型和参赛者ID
        ExamPaper.ParticipantType participantType;
        Long participantId;
        
        if (registration.getTeam() != null) {
            // 团队报名
            participantType = ExamPaper.ParticipantType.TEAM;
            participantId = registration.getTeam().getId();
            logger.info("用户 {} 通过团队 {} 参赛竞赛 {}", userId, participantId, competitionId);
        } else {
            // 个人报名（如果系统支持个人报名）
            participantType = ExamPaper.ParticipantType.INDIVIDUAL;
            participantId = userId;
            logger.info("用户 {} 个人参赛竞赛 {}", userId, competitionId);
        }

        // 查找或创建考卷
        Optional<ExamPaper> existingPaperOpt = examPaperRepository
                .findByCompetitionIdAndParticipantTypeAndParticipantId(
                        competitionId,
                        participantType,
                        participantId
                );

        ExamPaper examPaper;
        boolean isNewPaper = false;

        if (existingPaperOpt.isPresent()) {
            examPaper = existingPaperOpt.get();
            logger.info("找到已存在的考卷: paperId={}, status={}", examPaper.getId(), examPaper.getPaperStatus());

            // 如果已经提交，不允许再次答题
            if (examPaper.getPaperStatus() == ExamPaper.PaperStatus.SUBMITTED ||
                examPaper.getPaperStatus() == ExamPaper.PaperStatus.GRADED) {
                throw new RuntimeException("您已提交答卷，不能重复答题");
            }

            // 如果是首次开始答题，记录开始时间
            if (examPaper.getPaperStatus() == ExamPaper.PaperStatus.NOT_STARTED) {
                examPaper.setStartTime(LocalDateTime.now());
                examPaper.setPaperStatus(ExamPaper.PaperStatus.IN_PROGRESS);
            }
        } else {
            // 创建新考卷
            examPaper = new ExamPaper(competitionId, participantType, participantId);
            examPaper.setStartTime(LocalDateTime.now());
            examPaper.setPaperStatus(ExamPaper.PaperStatus.IN_PROGRESS);
            isNewPaper = true;
            logger.info("创建新考卷: competitionId={}, participantType={}, participantId={}", 
                competitionId, participantType, participantId);
        }

        examPaper = examPaperRepository.save(examPaper);

        // 如果是新考卷，初始化答题记录
        if (isNewPaper) {
            initializeExamAnswers(examPaper);
        }

        // 获取题目列表
        List<CompetitionQuestion> competitionQuestions = competitionQuestionRepository
                .findByCompetitionIdOrderByQuestionOrder(competitionId);

        List<Map<String, Object>> questions = new ArrayList<>();
        for (CompetitionQuestion cq : competitionQuestions) {
            ExamPaper finalExamPaper = examPaper;
            questionRepository.findById(cq.getQuestionId()).ifPresent(question -> {
                Map<String, Object> questionData = new HashMap<>();
                questionData.put("id", question.getId());
                questionData.put("order", cq.getQuestionOrder());
                questionData.put("title", question.getTitle());
                questionData.put("content", question.getContent());
                questionData.put("type", question.getType());
                questionData.put("options", question.getOptions());
                questionData.put("score", cq.getQuestionScore());

                // 获取已保存的答案
                examAnswerRepository.findByExamPaperIdAndQuestionId(finalExamPaper.getId(), question.getId())
                        .ifPresent(answer -> {
                            questionData.put("userAnswer", answer.getAnswerContent());
                        });

                questions.add(questionData);
            });
        }

        Map<String, Object> result = new HashMap<>();
        result.put("examPaperId", examPaper.getId());
        result.put("competitionId", competitionId);
        result.put("competitionName", competition.getName());
        result.put("startTime", examPaper.getStartTime());
        result.put("questions", questions);
        result.put("totalQuestions", questions.size());

        logger.info("用户{}开始答题，考卷ID: {}", userId, examPaper.getId());
        return result;
    }

    /**
     * 初始化答题记录
     */
    private void initializeExamAnswers(ExamPaper examPaper) {
        List<CompetitionQuestion> questions = competitionQuestionRepository
                .findByCompetitionIdOrderByQuestionOrder(examPaper.getCompetitionId());

        int totalCount = 0;
        for (CompetitionQuestion cq : questions) {
            ExamAnswer answer = new ExamAnswer(examPaper.getId(), cq.getQuestionId());
            answer.setMaxScore(cq.getQuestionScore());
            answer.setGradingStatus(ExamAnswer.GradingStatus.PENDING);
            examAnswerRepository.save(answer);
            totalCount++;
        }

        examPaper.setTotalQuestionCount(totalCount);
        examPaperRepository.save(examPaper);
    }

    /**
     * 保存答案（自动保存功能）
     */
    public void saveAnswer(Long examPaperId, Long questionId, String answerContent, Long userId) {
        logger.info("保存答案: examPaperId={}, questionId={}, userId={}", examPaperId, questionId, userId);

        // 检查考卷
        ExamPaper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new RuntimeException("考卷不存在"));

        // 验证考卷所有权（支持团队和个人）
        if (!hasPermissionToAccessPaper(examPaper, userId)) {
            throw new RuntimeException("无权限操作此考卷");
        }

        // 检查考卷状态
        if (examPaper.getPaperStatus() != ExamPaper.PaperStatus.IN_PROGRESS) {
            throw new RuntimeException("考卷状态不正确");
        }

        // 查找或创建答案记录
        ExamAnswer answer = examAnswerRepository
                .findByExamPaperIdAndQuestionId(examPaperId, questionId)
                .orElseGet(() -> {
                    ExamAnswer newAnswer = new ExamAnswer(examPaperId, questionId);
                    newAnswer.setGradingStatus(ExamAnswer.GradingStatus.PENDING);
                    return newAnswer;
                });

        answer.setAnswerContent(answerContent);
        examAnswerRepository.save(answer);

        logger.info("答案保存成功");
    }

    /**
     * 提交答卷并自动判分
     */
    public Map<String, Object> submitExam(Long examPaperId, Long userId) {
        logger.info("提交答卷: examPaperId={}, userId={}", examPaperId, userId);

        // 检查考卷
        ExamPaper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new RuntimeException("考卷不存在"));

        // 验证考卷所有权（支持团队和个人）
        if (!hasPermissionToAccessPaper(examPaper, userId)) {
            throw new RuntimeException("无权限操作此考卷");
        }

        // 检查考卷状态
        if (examPaper.getPaperStatus() != ExamPaper.PaperStatus.IN_PROGRESS) {
            throw new RuntimeException("考卷状态不正确，无法提交");
        }

        // 更新考卷状态
        examPaper.setSubmitTime(LocalDateTime.now());
        examPaper.setPaperStatus(ExamPaper.PaperStatus.SUBMITTED);

        // 自动判分
        gradeExam(examPaper);

        examPaperRepository.save(examPaper);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("examPaperId", examPaper.getId());
        result.put("totalScore", examPaper.getTotalScore());
        result.put("objectiveScore", examPaper.getObjectiveScore());
        result.put("correctCount", examPaper.getCorrectCount());
        result.put("totalCount", examPaper.getTotalQuestionCount());
        result.put("submitTime", examPaper.getSubmitTime());

        logger.info("答卷提交成功，总分: {}", examPaper.getTotalScore());
        return result;
    }

    /**
     * 自动判分
     */
    private void gradeExam(ExamPaper examPaper) {
        List<ExamAnswer> answers = examAnswerRepository.findByExamPaperId(examPaper.getId());

        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal objectiveScore = BigDecimal.ZERO;
        int correctCount = 0;

        for (ExamAnswer answer : answers) {
            questionRepository.findById(answer.getQuestionId()).ifPresent(question -> {
                // 只有客观题才能自动判分
                if (isObjectiveQuestion(question.getType())) {
                    boolean isCorrect = checkAnswer(question, answer.getAnswerContent());
                    answer.setIsCorrect(isCorrect);

                    if (isCorrect) {
                        answer.setScore(answer.getMaxScore());
                    } else {
                        answer.setScore(BigDecimal.ZERO);
                    }

                    answer.setGradingStatus(ExamAnswer.GradingStatus.AUTO_GRADED);
                    answer.setGradedAt(LocalDateTime.now());
                } else {
                    // 主观题需要人工评分
                    answer.setGradingStatus(ExamAnswer.GradingStatus.MANUAL_GRADING);
                }

                examAnswerRepository.save(answer);
            });
        }

        // 重新计算总分
        answers = examAnswerRepository.findByExamPaperId(examPaper.getId());
        for (ExamAnswer answer : answers) {
            if (answer.getScore() != null) {
                totalScore = totalScore.add(answer.getScore());

                if (answer.getGradingStatus() == ExamAnswer.GradingStatus.AUTO_GRADED) {
                    objectiveScore = objectiveScore.add(answer.getScore());
                }

                if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                    correctCount++;
                }
            }
        }

        examPaper.setTotalScore(totalScore);
        examPaper.setObjectiveScore(objectiveScore);
        examPaper.setCorrectCount(correctCount);

        // 检查是否所有题目都已评分
        boolean allGraded = answers.stream()
                .allMatch(a -> a.getGradingStatus() == ExamAnswer.GradingStatus.AUTO_GRADED ||
                             a.getGradingStatus() == ExamAnswer.GradingStatus.MANUAL_GRADED);

        if (allGraded) {
            examPaper.setPaperStatus(ExamPaper.PaperStatus.GRADED);
        } else {
            examPaper.setPaperStatus(ExamPaper.PaperStatus.GRADING);
        }
    }

    /**
     * 判断是否为客观题
     */
    private boolean isObjectiveQuestion(Question.QuestionType type) {
        return type == Question.QuestionType.SINGLE_CHOICE ||
               type == Question.QuestionType.MULTIPLE_CHOICE ||
               type == Question.QuestionType.TRUE_FALSE;
    }

    /**
     * 检查答案是否正确
     */
    private boolean checkAnswer(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }

        String correctAnswer = question.getCorrectAnswer();
        if (correctAnswer == null) {
            return false;
        }

        // 去除空格并统一大小写
        userAnswer = userAnswer.trim().toUpperCase();
        correctAnswer = correctAnswer.trim().toUpperCase();

        // 多选题特殊处理（答案可能是逗号分隔的）
        if (question.getType() == Question.QuestionType.MULTIPLE_CHOICE) {
            Set<String> userAnswers = new HashSet<>(Arrays.asList(userAnswer.split(",")));
            Set<String> correctAnswers = new HashSet<>(Arrays.asList(correctAnswer.split(",")));
            return userAnswers.equals(correctAnswers);
        }

        // 单选题和判断题直接比较
        return userAnswer.equals(correctAnswer);
    }

    /**
     * 获取答题进度
     */
    public Map<String, Object> getExamProgress(Long examPaperId, Long userId) {
        ExamPaper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new RuntimeException("考卷不存在"));

        // 验证考卷所有权（支持团队和个人）
        if (!hasPermissionToAccessPaper(examPaper, userId)) {
            throw new RuntimeException("无权限查看此考卷");
        }

        List<ExamAnswer> answers = examAnswerRepository.findByExamPaperId(examPaperId);
        long answeredCount = answers.stream()
                .filter(a -> a.getAnswerContent() != null && !a.getAnswerContent().trim().isEmpty())
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("examPaperId", examPaper.getId());
        result.put("status", examPaper.getPaperStatus());
        result.put("totalCount", examPaper.getTotalQuestionCount());
        result.put("answeredCount", answeredCount);
        result.put("startTime", examPaper.getStartTime());
        result.put("submitTime", examPaper.getSubmitTime());

        if (examPaper.getPaperStatus() == ExamPaper.PaperStatus.GRADED ||
            examPaper.getPaperStatus() == ExamPaper.PaperStatus.SUBMITTED) {
            result.put("totalScore", examPaper.getTotalScore());
            result.put("correctCount", examPaper.getCorrectCount());
        }

        return result;
    }

    /**
     * 检查用户是否有权限访问考卷
     * @param examPaper 考卷
     * @param userId 用户ID
     * @return 是否有权限
     */
    private boolean hasPermissionToAccessPaper(ExamPaper examPaper, Long userId) {
        if (examPaper.getParticipantType() == ExamPaper.ParticipantType.INDIVIDUAL) {
            // 个人参赛：检查是否是本人
            boolean hasPermission = examPaper.getParticipantId().equals(userId);
            logger.info("个人考卷权限检查: paperId={}, userId={}, hasPermission={}", 
                examPaper.getId(), userId, hasPermission);
            return hasPermission;
        } else {
            // 团队参赛：检查用户是否是团队成员
            Long teamId = examPaper.getParticipantId();
            boolean isMember = teamMemberRepository.existsByTeamIdAndUserId(teamId, userId);
            logger.info("团队考卷权限检查: paperId={}, teamId={}, userId={}, isMember={}", 
                examPaper.getId(), teamId, userId, isMember);
            return isMember;
        }
    }

    /**
     * 检查用户是否已报名竞赛（支持个人报名和团队报名）
     * @param competitionId 竞赛ID
     * @param userId 用户ID
     * @return 报名记录，如果未报名则返回null
     */
    private Registration checkUserRegistration(Long competitionId, Long userId) {
        logger.info("检查用户 {} 是否报名竞赛 {}", userId, competitionId);

        // 方式1: 检查用户直接提交的报名
        Optional<Registration> directRegistration = 
            registrationRepository.findByCompetitionAndUser(competitionId, userId);
        
        if (directRegistration.isPresent()) {
            logger.info("用户 {} 直接报名了竞赛 {}", userId, competitionId);
            return directRegistration.get();
        }

        // 方式2: 检查用户作为团队成员的报名
        List<TeamMember> userTeamMemberships = teamMemberRepository
            .findActiveTeamsByUser(userId);
        
        logger.info("用户 {} 的活跃团队成员身份数: {}", userId, userTeamMemberships.size());

        for (TeamMember membership : userTeamMemberships) {
            Long teamId = membership.getTeam().getId();
            logger.info("检查团队 {} 的报名记录", teamId);
            
            // 查找该团队的报名记录
            Optional<Registration> teamRegistration = registrationRepository.findByTeamId(teamId);
            
            if (teamRegistration.isPresent()) {
                Registration reg = teamRegistration.get();
                logger.info("团队 {} 的报名记录 - 竞赛ID: {}, 状态: {}", 
                    teamId, reg.getCompetition().getId(), reg.getStatus());
                
                // 检查是否是目标竞赛
                if (reg.getCompetition().getId().equals(competitionId)) {
                    logger.info("用户 {} 通过团队 {} 报名了竞赛 {}", userId, teamId, competitionId);
                    return reg;
                }
            }
        }

        logger.warn("用户 {} 未报名竞赛 {} (既无直接报名，也无团队报名)", userId, competitionId);
        return null;
    }

    /**
     * 根据竞赛ID和团队ID获取考卷ID
     */
    public Map<String, Object> getPaperIdByCompetitionAndTeam(Long competitionId, Long teamId) {
        logger.info("查询考卷: competitionId={}, teamId={}", competitionId, teamId);
        
        Optional<ExamPaper> paperOpt = examPaperRepository.findByCompetitionIdAndParticipantTypeAndParticipantId(
            competitionId, ExamPaper.ParticipantType.TEAM, teamId);
        
        if (paperOpt.isEmpty()) {
            throw new RuntimeException("未找到考卷");
        }
        
        ExamPaper paper = paperOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("paperId", paper.getId());
        result.put("paperStatus", paper.getPaperStatus());
        result.put("totalScore", paper.getTotalScore());
        
        return result;
    }

    /**
     * 根据考卷ID获取答题详情
     */
    public List<Map<String, Object>> getAnswerDetailsByPaperId(Long paperId) {
        logger.info("查询答题详情: paperId={}", paperId);
        
        // 检查考卷是否存在
        ExamPaper paper = examPaperRepository.findById(paperId)
            .orElseThrow(() -> new RuntimeException("考卷不存在"));
        
        // 获取所有答题记录
        List<ExamAnswer> answers = examAnswerRepository.findByExamPaperId(paperId);
        
        List<Map<String, Object>> detailList = new ArrayList<>();
        
        for (ExamAnswer answer : answers) {
            Map<String, Object> detail = new HashMap<>();
            
            // 获取题目信息
            Question question = questionRepository.findById(answer.getQuestionId())
                .orElse(null);
            
            if (question != null) {
                detail.put("questionType", question.getType().toString());
                detail.put("questionContent", question.getContent());
                detail.put("correctAnswer", question.getCorrectAnswer());
            } else {
                detail.put("questionType", "未知");
                detail.put("questionContent", "题目不存在");
                detail.put("correctAnswer", "");
            }
            
            detail.put("studentAnswer", answer.getAnswerContent());
            detail.put("isCorrect", answer.getIsCorrect());
            detail.put("score", answer.getScore() != null ? answer.getScore() : BigDecimal.ZERO);
            detail.put("maxScore", question != null ? question.getScore() : BigDecimal.ZERO);
            
            detailList.add(detail);
        }
        
        logger.info("考卷 {} 的答题详情数: {}", paperId, detailList.size());
        return detailList;
    }
}
