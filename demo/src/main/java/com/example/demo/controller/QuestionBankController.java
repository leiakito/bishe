package com.example.demo.controller;

import com.example.demo.dto.ImportResultDTO;
import com.example.demo.entity.CompetitionQuestion;
import com.example.demo.entity.Question;
import com.example.demo.entity.User;
import com.example.demo.repository.CompetitionQuestionRepository;
import com.example.demo.repository.CompetitionRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.service.JsonImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题库管理Controller
 */
@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionBankController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionBankController.class);

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CompetitionQuestionRepository competitionQuestionRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private JsonImportService jsonImportService;

    @Autowired
    private com.example.demo.repository.UserRepository userRepository;

    /**
     * 批量导入题目 (JSON文件)
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importQuestions(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "competitionId", required = false) Long competitionId,
            Authentication authentication) {

        logger.info("开始导入题目: filename={}, competitionId={}", file.getOriginalFilename(), competitionId);

        // 获取当前登录用户
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        try {
            ImportResultDTO result = jsonImportService.importQuestionsFromJson(file, competitionId, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("成功导入%d题,失败%d题", result.getSuccessCount(), result.getFailedCount()));
            response.put("data", result);

            logger.info("题目导入完成: success={}, failed={}", result.getSuccessCount(), result.getFailedCount());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("题目导入失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "题目导入失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取题库列表 (分页)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {

        logger.info("查询题库列表: page={}, size={}, category={}, difficulty={}, type={}, status={}",
                page, size, category, difficulty, type, status);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            // 将字符串参数转换为枚举类型
            Question.QuestionType tempType = null;
            if (type != null && !type.isEmpty()) {
                try {
                    tempType = Question.QuestionType.valueOf(type);
                } catch (IllegalArgumentException e) {
                    logger.warn("无效的题目类型: {}", type);
                }
            }
            final Question.QuestionType questionType = tempType;

            Question.QuestionCategory tempCategory = null;
            if (category != null && !category.isEmpty()) {
                try {
                    tempCategory = Question.QuestionCategory.valueOf(category);
                } catch (IllegalArgumentException e) {
                    logger.warn("无效的题目分类: {}", category);
                }
            }
            final Question.QuestionCategory questionCategory = tempCategory;

            Question.QuestionDifficulty tempDifficulty = null;
            if (difficulty != null && !difficulty.isEmpty()) {
                try {
                    tempDifficulty = Question.QuestionDifficulty.valueOf(difficulty);
                } catch (IllegalArgumentException e) {
                    logger.warn("无效的题目难度: {}", difficulty);
                }
            }
            final Question.QuestionDifficulty questionDifficulty = tempDifficulty;

            Question.QuestionStatus tempStatus = null;
            if (status != null && !status.isEmpty()) {
                try {
                    tempStatus = Question.QuestionStatus.valueOf(status);
                } catch (IllegalArgumentException e) {
                    logger.warn("无效的题目状态: {}", status);
                }
            }
            final Question.QuestionStatus questionStatus = tempStatus;

            // 根据筛选条件查询
            Page<Question> questionPage;

            // 使用不同的查询方法根据筛选条件
            if (questionType != null && questionCategory != null && questionDifficulty != null) {
                // 三个条件都有 - 使用Specification动态查询
                questionPage = questionRepository.findAll((root, query, criteriaBuilder) -> {
                    var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
                    predicates.add(criteriaBuilder.equal(root.get("type"), questionType));
                    predicates.add(criteriaBuilder.equal(root.get("category"), questionCategory));
                    predicates.add(criteriaBuilder.equal(root.get("difficulty"), questionDifficulty));
                    if (questionStatus != null) {
                        predicates.add(criteriaBuilder.equal(root.get("status"), questionStatus));
                    }
                    return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                }, pageable);
            } else if (questionType != null) {
                questionPage = questionRepository.findByType(questionType, pageable);
            } else if (questionCategory != null) {
                questionPage = questionRepository.findByCategory(questionCategory, pageable);
            } else if (questionDifficulty != null) {
                questionPage = questionRepository.findByDifficulty(questionDifficulty, pageable);
            } else if (questionStatus != null) {
                questionPage = questionRepository.findByStatus(questionStatus, pageable);
            } else {
                questionPage = questionRepository.findAll(pageable);
            }

            // 为每个题目添加关联竞赛信息
            List<Map<String, Object>> questionsWithCompetitions = questionPage.getContent().stream()
                    .map(question -> {
                        Map<String, Object> questionData = new HashMap<>();
                        questionData.put("id", question.getId());
                        questionData.put("title", question.getTitle());
                        questionData.put("content", question.getContent());
                        questionData.put("type", question.getType());
                        questionData.put("difficulty", question.getDifficulty());
                        questionData.put("category", question.getCategory());
                        questionData.put("score", question.getScore());
                        questionData.put("usageCount", question.getUsageCount());
                        questionData.put("status", question.getStatus());
                        questionData.put("createdAt", question.getCreatedAt());

                        // 查询关联的竞赛
                        List<Long> competitionIds = competitionQuestionRepository
                                .findCompetitionIdsByQuestionId(question.getId());
                        List<Map<String, Object>> linkedCompetitions = new ArrayList<>();

                        for (Long compId : competitionIds) {
                            competitionRepository.findById(compId).ifPresent(comp -> {
                                Map<String, Object> compData = new HashMap<>();
                                compData.put("id", comp.getId());
                                compData.put("name", comp.getName());
                                compData.put("status", comp.getStatus());
                                linkedCompetitions.add(compData);
                            });
                        }

                        questionData.put("linkedCompetitions", linkedCompetitions);
                        return questionData;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("content", questionsWithCompetitions);
            result.put("totalElements", questionPage.getTotalElements());
            result.put("totalPages", questionPage.getTotalPages());
            result.put("currentPage", page);
            result.put("pageSize", size);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("查询题库列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getQuestionDetail(@PathVariable Long id) {
        logger.info("查询题目详情: id={}", id);

        try {
            Question question = questionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("题目不存在"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", question);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("查询题目详情失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除题目 (带关联检查)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(@PathVariable Long id) {
        logger.info("删除题目: id={}", id);

        try {
            // 检查题目是否存在
            Question question = questionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("题目不存在"));

            // 检查是否关联了竞赛
            long linkedCount = competitionQuestionRepository.countByQuestionId(id);

            if (linkedCount > 0) {
                // 查询关联的竞赛信息
                List<Long> competitionIds = competitionQuestionRepository.findCompetitionIdsByQuestionId(id);
                List<Map<String, Object>> linkedCompetitions = new ArrayList<>();

                for (Long compId : competitionIds) {
                    competitionRepository.findById(compId).ifPresent(comp -> {
                        Map<String, Object> compData = new HashMap<>();
                        compData.put("id", comp.getId());
                        compData.put("name", comp.getName());
                        compData.put("status", comp.getStatus());
                        linkedCompetitions.add(compData);
                    });
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", String.format("该题目已关联到%d个竞赛,无法删除", linkedCount));

                Map<String, Object> data = new HashMap<>();
                data.put("linkedCompetitions", linkedCompetitions);
                response.put("data", data);

                logger.warn("题目已关联竞赛,无法删除: id={}, linkedCount={}", id, linkedCount);
                return ResponseEntity.badRequest().body(response);
            }

            // 删除题目
            questionRepository.deleteById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "题目删除成功");

            logger.info("题目删除成功: id={}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("删除题目失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 为竞赛添加题目
     */
    @PostMapping("/competitions/{competitionId}/questions")
    public ResponseEntity<Map<String, Object>> addQuestionsToCompetition(
            @PathVariable Long competitionId,
            @RequestBody Map<String, Object> requestBody) {

        logger.info("为竞赛添加题目: competitionId={}, requestBody={}", competitionId, requestBody);

        try {
            // 更健壮的类型处理
            Object questionIdsObj = requestBody.get("questionIds");
            List<Long> questionIds = new ArrayList<>();

            if (questionIdsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<?> rawList = (List<?>) questionIdsObj;
                for (Object obj : rawList) {
                    if (obj instanceof Number) {
                        questionIds.add(((Number) obj).longValue());
                    } else if (obj instanceof String) {
                        questionIds.add(Long.parseLong((String) obj));
                    }
                }
            }

            Object scoresObj = requestBody.get("scores");
            List<Number> scores = null;
            if (scoresObj instanceof List) {
                scores = (List<Number>) scoresObj;
            }

            if (questionIds == null || questionIds.isEmpty()) {
                throw new RuntimeException("题目ID列表不能为空");
            }

            logger.info("解析后的questionIds: {}, scores: {}", questionIds, scores);

            // 检查竞赛是否存在
            competitionRepository.findById(competitionId)
                    .orElseThrow(() -> new RuntimeException("竞赛不存在"));

            // 获取竞赛当前最大的题目顺序
            List<CompetitionQuestion> existingQuestions = competitionQuestionRepository
                    .findByCompetitionIdOrderByQuestionOrder(competitionId);
            int order = existingQuestions.isEmpty() ? 1 :
                    existingQuestions.stream()
                            .mapToInt(CompetitionQuestion::getQuestionOrder)
                            .max()
                            .orElse(0) + 1;

            BigDecimal totalScore = BigDecimal.ZERO;
            int addedCount = 0;

            for (int i = 0; i < questionIds.size(); i++) {
                Long questionId = questionIds.get(i);

                // 检查题目是否存在
                Question question = questionRepository.findById(questionId)
                        .orElseThrow(() -> new RuntimeException("题目不存在: " + questionId));

                // 检查是否已关联
                if (competitionQuestionRepository.existsByCompetitionIdAndQuestionId(competitionId, questionId)) {
                    logger.warn("题目已关联到该竞赛: questionId={}", questionId);
                    continue;
                }

                // 确定分值(如果提供了自定义分值,使用自定义分值,否则使用题目默认分值)
                BigDecimal questionScore = question.getScore() != null ?
                        BigDecimal.valueOf(question.getScore()) : BigDecimal.TEN;

                if (scores != null && i < scores.size() && scores.get(i) != null) {
                    questionScore = BigDecimal.valueOf(scores.get(i).doubleValue());
                }

                // 创建关联
                CompetitionQuestion cq = new CompetitionQuestion();
                cq.setCompetitionId(competitionId);
                cq.setQuestionId(questionId);
                cq.setQuestionOrder(order++);
                cq.setQuestionScore(questionScore);
                competitionQuestionRepository.save(cq);

                totalScore = totalScore.add(questionScore);
                addedCount++;

                // 更新题目使用次数
                question.setUsageCount(question.getUsageCount() + 1);
                questionRepository.save(question);

                logger.info("题目关联成功: competitionId={}, questionId={}, score={}",
                        competitionId, questionId, questionScore);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("totalScore", totalScore);
            data.put("questionCount", addedCount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("成功添加%d道题目", addedCount));
            response.put("data", data);

            logger.info("为竞赛添加题目完成: competitionId={}, count={}, totalScore={}",
                    competitionId, addedCount, totalScore);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("为竞赛添加题目失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "添加失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取竞赛的题目列表
     */
    @GetMapping("/competitions/{competitionId}/questions")
    public ResponseEntity<Map<String, Object>> getCompetitionQuestions(@PathVariable Long competitionId) {
        logger.info("获取竞赛题目列表: competitionId={}", competitionId);

        try {
            List<CompetitionQuestion> competitionQuestions = competitionQuestionRepository
                    .findByCompetitionIdOrderByQuestionOrder(competitionId);

            List<Map<String, Object>> questionList = new ArrayList<>();
            BigDecimal totalScore = BigDecimal.ZERO;

            for (CompetitionQuestion cq : competitionQuestions) {
                questionRepository.findById(cq.getQuestionId()).ifPresent(question -> {
                    Map<String, Object> questionData = new HashMap<>();
                    questionData.put("id", question.getId());
                    questionData.put("order", cq.getQuestionOrder());
                    questionData.put("title", question.getTitle());
                    questionData.put("content", question.getContent());
                    questionData.put("type", question.getType());
                    questionData.put("difficulty", question.getDifficulty());
                    questionData.put("category", question.getCategory());
                    questionData.put("score", cq.getQuestionScore());
                    questionData.put("isActive", cq.getIsActive());
                    questionList.add(questionData);
                });

                totalScore = totalScore.add(cq.getQuestionScore());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("competitionId", competitionId);
            data.put("totalScore", totalScore);
            data.put("questions", questionList);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取竞赛题目列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 从竞赛中移除题目关联
     */
    @DeleteMapping("/competitions/{competitionId}/questions/{questionId}")
    public ResponseEntity<Map<String, Object>> removeQuestionFromCompetition(
            @PathVariable Long competitionId,
            @PathVariable Long questionId) {

        logger.info("从竞赛中移除题目: competitionId={}, questionId={}", competitionId, questionId);

        try {
            // 检查关联是否存在
            if (!competitionQuestionRepository.existsByCompetitionIdAndQuestionId(competitionId, questionId)) {
                throw new RuntimeException("题目未关联到该竞赛");
            }

            // 删除关联
            competitionQuestionRepository.deleteByCompetitionIdAndQuestionId(competitionId, questionId);

            // 更新题目使用次数
            questionRepository.findById(questionId).ifPresent(question -> {
                int usageCount = Math.max(0, question.getUsageCount() - 1);
                question.setUsageCount(usageCount);
                questionRepository.save(question);
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "题目关联已移除");

            logger.info("题目关联移除成功: competitionId={}, questionId={}", competitionId, questionId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("移除题目关联失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "移除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取教师可以关联题目的竞赛列表
     */
    @GetMapping("/available-competitions")
    public ResponseEntity<Map<String, Object>> getAvailableCompetitions(Authentication authentication) {
        logger.info("获取可用竞赛列表");

        try {
            // 获取当前登录用户
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 查询该教师创建的或者状态为报名中、进行中的竞赛
            List<com.example.demo.entity.Competition> competitions = competitionRepository.findAll().stream()
                    .filter(comp -> {
                        // 教师可以看到自己创建的所有竞赛
                        if (comp.getCreator().getId().equals(currentUser.getId())) {
                            return true;
                        }
                        // 管理员可以看到所有竞赛
                        if (currentUser.getRole() == User.UserRole.ADMIN) {
                            return true;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            List<Map<String, Object>> competitionList = competitions.stream()
                    .map(comp -> {
                        Map<String, Object> compData = new HashMap<>();
                        compData.put("id", comp.getId());
                        compData.put("name", comp.getName());
                        compData.put("status", comp.getStatus());
                        compData.put("competitionNumber", comp.getCompetitionNumber());

                        // 查询该竞赛已有的题目数量
                        long questionCount = competitionQuestionRepository.countByCompetitionId(comp.getId());
                        compData.put("questionCount", questionCount);

                        return compData;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", competitionList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取可用竞赛列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
