package com.example.demo.service;

import com.example.demo.dto.ImportResultDTO;
import com.example.demo.dto.QuestionImportDTO;
import com.example.demo.entity.CompetitionQuestion;
import com.example.demo.entity.Question;
import com.example.demo.repository.CompetitionQuestionRepository;
import com.example.demo.repository.QuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON题目导入服务
 */
@Service
public class JsonImportService {

    private static final Logger logger = LoggerFactory.getLogger(JsonImportService.class);

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CompetitionQuestionRepository competitionQuestionRepository;

    /**
     * 从JSON文件导入题目
     *
     * @param file          JSON文件
     * @param competitionId 竞赛ID(可选,如果提供则自动关联)
     * @param createdBy     创建者ID
     * @return 导入结果
     */
    @Transactional
    public ImportResultDTO importQuestionsFromJson(MultipartFile file, Long competitionId, Long createdBy) {
        ImportResultDTO result = new ImportResultDTO();

        try {
            // 1. 读取JSON文件
            String jsonContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            QuestionImportDTO importData = mapper.readValue(jsonContent, QuestionImportDTO.class);

            if (importData.getQuestions() == null || importData.getQuestions().isEmpty()) {
                throw new RuntimeException("JSON文件中没有找到题目数据");
            }

            List<Question> successList = new ArrayList<>();
            List<ImportResultDTO.FailedReason> failedList = new ArrayList<>();

            // 2. 逐条验证并保存题目
            int lineNumber = 1;
            for (QuestionImportDTO.QuestionDTO dto : importData.getQuestions()) {
                try {
                    // 验证必填字段
                    validateQuestion(dto);

                    // 创建Question实体
                    Question question = new Question();
                    question.setTitle(dto.getTitle());
                    question.setContent(dto.getContent());
                    question.setType(Question.QuestionType.valueOf(dto.getType()));
                    question.setDifficulty(Question.QuestionDifficulty.valueOf(dto.getDifficulty()));
                    question.setCategory(Question.QuestionCategory.valueOf(dto.getCategory()));

                    // 将选项列表转换为 JSON 字符串
                    if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
                        question.setOptions(mapper.writeValueAsString(dto.getOptions()));
                    }

                    question.setCorrectAnswer(dto.getCorrectAnswer());
                    question.setExplanation(dto.getExplanation());
                    question.setScore(dto.getScore());
                    question.setTags(dto.getTags());
                    question.setCreatedBy(createdBy);
                    question.setStatus(Question.QuestionStatus.PUBLISHED);

                    // 保存到数据库
                    Question saved = questionRepository.save(question);
                    successList.add(saved);

                    logger.info("成功导入题目: {}, ID: {}", saved.getTitle(), saved.getId());

                    // 3. 如果指定了竞赛ID,自动关联
                    if (competitionId != null) {
                        CompetitionQuestion cq = new CompetitionQuestion();
                        cq.setCompetitionId(competitionId);
                        cq.setQuestionId(saved.getId());
                        cq.setQuestionOrder(lineNumber);
                        cq.setQuestionScore(BigDecimal.valueOf(dto.getScore()));
                        competitionQuestionRepository.save(cq);

                        logger.info("题目已关联到竞赛: competitionId={}, questionId={}", competitionId, saved.getId());
                    }

                } catch (Exception e) {
                    logger.error("导入题目失败,行号: {}, 原因: {}", lineNumber, e.getMessage());
                    failedList.add(new ImportResultDTO.FailedReason(lineNumber, e.getMessage()));
                }
                lineNumber++;
            }

            // 4. 构建返回结果
            result.setSuccessCount(successList.size());
            result.setFailedCount(failedList.size());
            result.setFailedReasons(failedList);
            result.setImportedQuestionIds(
                    successList.stream().map(Question::getId).collect(Collectors.toList())
            );

            logger.info("题目导入完成: 成功{}题, 失败{}题", result.getSuccessCount(), result.getFailedCount());

        } catch (IOException e) {
            logger.error("JSON文件解析失败", e);
            throw new RuntimeException("JSON文件解析失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 验证题目数据
     */
    private void validateQuestion(QuestionImportDTO.QuestionDTO dto) {
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new IllegalArgumentException("题目标题不能为空");
        }
        if (!StringUtils.hasText(dto.getContent())) {
            throw new IllegalArgumentException("题目内容不能为空");
        }
        if (dto.getScore() == null || dto.getScore() <= 0) {
            throw new IllegalArgumentException("题目分值必须大于0");
        }

        // 验证题目类型
        try {
            Question.QuestionType.valueOf(dto.getType());
        } catch (Exception e) {
            throw new IllegalArgumentException("题目类型不正确: " + dto.getType());
        }

        // 验证难度
        try {
            Question.QuestionDifficulty.valueOf(dto.getDifficulty());
        } catch (Exception e) {
            throw new IllegalArgumentException("题目难度不正确: " + dto.getDifficulty());
        }

        // 验证分类
        try {
            Question.QuestionCategory.valueOf(dto.getCategory());
        } catch (Exception e) {
            throw new IllegalArgumentException("题目分类不正确: " + dto.getCategory());
        }

        // 选择题必须有选项和正确答案
        if (dto.getType().contains("CHOICE") || dto.getType().equals("TRUE_FALSE")) {
            if (dto.getOptions() == null || dto.getOptions().isEmpty()) {
                throw new IllegalArgumentException("选择题必须提供选项");
            }
            if (!StringUtils.hasText(dto.getCorrectAnswer())) {
                throw new IllegalArgumentException("选择题必须提供正确答案");
            }
        }
    }
}
