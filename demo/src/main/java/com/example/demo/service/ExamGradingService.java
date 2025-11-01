package com.example.demo.service;

import com.example.demo.dto.GradingResultDTO;
import com.example.demo.entity.ExamAnswer;
import com.example.demo.entity.ExamPaper;
import com.example.demo.entity.Question;
import com.example.demo.repository.ExamAnswerRepository;
import com.example.demo.repository.ExamPaperRepository;
import com.example.demo.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 自动评分服务
 */
@Service
public class ExamGradingService {

    private static final Logger logger = LoggerFactory.getLogger(ExamGradingService.class);

    @Autowired
    private ExamAnswerRepository examAnswerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ExamPaperRepository examPaperRepository;

    /**
     * 自动评分 - 仅评客观题
     *
     * @param paperId 考卷ID
     * @return 评分结果
     */
    @Transactional
    public GradingResultDTO autoGrade(Long paperId) {
        logger.info("开始自动评分: paperId={}", paperId);

        GradingResultDTO result = new GradingResultDTO();

        // 1. 获取考卷
        ExamPaper paper = examPaperRepository.findById(paperId)
                .orElseThrow(() -> new RuntimeException("考卷不存在"));

        // 2. 获取该考卷的所有答案
        List<ExamAnswer> answers = examAnswerRepository.findByExamPaperId(paperId);

        if (answers.isEmpty()) {
            logger.warn("考卷没有答案记录: paperId={}", paperId);
            return result;
        }

        BigDecimal objectiveScore = BigDecimal.ZERO;
        int correctCount = 0;
        boolean needManualGrading = false;

        // 3. 逐题评分
        for (ExamAnswer answer : answers) {
            // 如果学生没有作答,跳过
            if (!StringUtils.hasText(answer.getAnswerContent())) {
                continue;
            }

            Question question = questionRepository.findById(answer.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("题目不存在: questionId=" + answer.getQuestionId()));

            // 根据题型决定评分方式
            switch (question.getType()) {
                case SINGLE_CHOICE:
                case TRUE_FALSE:
                    // 单选题/判断题: 完全匹配
                    boolean isSingleCorrect = question.getCorrectAnswer()
                            .trim()
                            .equalsIgnoreCase(answer.getAnswerContent().trim());
                    answer.setIsCorrect(isSingleCorrect);

                    if (isSingleCorrect) {
                        answer.setScore(answer.getMaxScore());
                        objectiveScore = objectiveScore.add(answer.getMaxScore());
                        correctCount++;
                    } else {
                        answer.setScore(BigDecimal.ZERO);
                    }
                    answer.setGradingStatus(ExamAnswer.GradingStatus.AUTO_GRADED);

                    logger.debug("单选/判断题评分: questionId={}, correct={}, score={}",
                            question.getId(), isSingleCorrect, answer.getScore());
                    break;

                case MULTIPLE_CHOICE:
                    // 多选题: 完全正确得满分,部分正确得部分分
                    String[] correctAnswers = question.getCorrectAnswer().split(",");
                    String[] studentAnswers = answer.getAnswerContent().split(",");

                    Set<String> correctSet = new HashSet<>();
                    for (String ca : correctAnswers) {
                        correctSet.add(ca.trim().toUpperCase());
                    }

                    Set<String> studentSet = new HashSet<>();
                    for (String sa : studentAnswers) {
                        studentSet.add(sa.trim().toUpperCase());
                    }

                    if (correctSet.equals(studentSet)) {
                        // 完全正确
                        answer.setScore(answer.getMaxScore());
                        objectiveScore = objectiveScore.add(answer.getMaxScore());
                        answer.setIsCorrect(true);
                        correctCount++;

                        logger.debug("多选题完全正确: questionId={}, score={}", question.getId(), answer.getScore());
                    } else {
                        // 部分正确: 按比例给分(答对比例 * 50%满分)
                        int correctInStudent = 0;
                        int wrongInStudent = 0;

                        for (String sa : studentSet) {
                            if (correctSet.contains(sa)) {
                                correctInStudent++;
                            } else {
                                wrongInStudent++;
                            }
                        }

                        // 如果有错误选项,不给分
                        if (wrongInStudent > 0) {
                            answer.setScore(BigDecimal.ZERO);
                        } else {
                            // 只有正确选项,按比例给分
                            double ratio = (double) correctInStudent / correctAnswers.length;
                            BigDecimal partialScore = answer.getMaxScore()
                                    .multiply(BigDecimal.valueOf(ratio))
                                    .multiply(BigDecimal.valueOf(0.5)); // 部分分50%
                            answer.setScore(partialScore);
                            objectiveScore = objectiveScore.add(partialScore);
                        }
                        answer.setIsCorrect(false);

                        logger.debug("多选题部分正确: questionId={}, correct={}/{}, score={}",
                                question.getId(), correctInStudent, correctAnswers.length, answer.getScore());
                    }
                    answer.setGradingStatus(ExamAnswer.GradingStatus.AUTO_GRADED);
                    break;

                case FILL_BLANK:
                case SHORT_ANSWER:
                case ESSAY:
                case PROGRAMMING:
                    // 主观题: 需要人工评分
                    answer.setGradingStatus(ExamAnswer.GradingStatus.PENDING);
                    needManualGrading = true;

                    logger.debug("主观题待人工评分: questionId={}, type={}", question.getId(), question.getType());
                    break;
            }

            examAnswerRepository.save(answer);
        }

        // 4. 更新考卷状态和分数
        paper.setObjectiveScore(objectiveScore);
        paper.setCorrectCount(correctCount);

        if (needManualGrading) {
            paper.setPaperStatus(ExamPaper.PaperStatus.GRADING);
            logger.info("考卷需要人工评分: paperId={}, objectiveScore={}", paperId, objectiveScore);
        } else {
            // 没有主观题,直接完成评分
            paper.setTotalScore(objectiveScore);
            paper.setPaperStatus(ExamPaper.PaperStatus.GRADED);
            paper.setGradedAt(LocalDateTime.now());
            logger.info("考卷自动评分完成: paperId={}, totalScore={}", paperId, objectiveScore);
        }

        examPaperRepository.save(paper);

        result.setObjectiveScore(objectiveScore);
        result.setCorrectCount(correctCount);
        result.setNeedManualGrading(needManualGrading);

        logger.info("自动评分完成: paperId={}, objectiveScore={}, correctCount={}, needManualGrading={}",
                paperId, objectiveScore, correctCount, needManualGrading);

        return result;
    }

    /**
     * 人工评分主观题
     *
     * @param paperId       考卷ID
     * @param answerScores  答案分数Map (questionId -> score)
     * @param answerRemarks 答案备注Map (questionId -> remarks)
     * @param gradedBy      评分人ID
     */
    @Transactional
    public void manualGrade(Long paperId, Map<Long, BigDecimal> answerScores,
                            Map<Long, String> answerRemarks, Long gradedBy) {
        logger.info("开始人工评分: paperId={}, gradedBy={}", paperId, gradedBy);

        ExamPaper paper = examPaperRepository.findById(paperId)
                .orElseThrow(() -> new RuntimeException("考卷不存在"));

        // 获取待评分的答案
        List<ExamAnswer> pendingAnswers = examAnswerRepository.findPendingGradingByExamPaperId(paperId);

        BigDecimal subjectiveScore = BigDecimal.ZERO;

        for (ExamAnswer answer : pendingAnswers) {
            Long questionId = answer.getQuestionId();

            if (answerScores.containsKey(questionId)) {
                BigDecimal score = answerScores.get(questionId);
                answer.setScore(score);
                answer.setGradingStatus(ExamAnswer.GradingStatus.MANUAL_GRADED);
                answer.setGradedBy(gradedBy);
                answer.setGradedAt(LocalDateTime.now());

                if (answerRemarks.containsKey(questionId)) {
                    answer.setGradingRemarks(answerRemarks.get(questionId));
                }

                subjectiveScore = subjectiveScore.add(score);
                examAnswerRepository.save(answer);

                logger.debug("主观题评分: questionId={}, score={}", questionId, score);
            }
        }

        // 更新考卷总分
        paper.setSubjectiveScore(subjectiveScore);
        paper.setTotalScore(paper.getObjectiveScore().add(subjectiveScore));
        paper.setPaperStatus(ExamPaper.PaperStatus.GRADED);
        paper.setGradedBy(gradedBy);
        paper.setGradedAt(LocalDateTime.now());
        examPaperRepository.save(paper);

        logger.info("人工评分完成: paperId={}, subjectiveScore={}, totalScore={}",
                paperId, subjectiveScore, paper.getTotalScore());
    }
}
