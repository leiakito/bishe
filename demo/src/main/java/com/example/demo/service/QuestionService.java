package com.example.demo.service;

import com.example.demo.entity.Question;
import com.example.demo.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 题目服务类
 */
@Service
@Transactional
public class QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    /**
     * 创建题目
     */
    public Question createQuestion(Question question) {
        // 检查题目标题是否已存在
        if (questionRepository.existsByTitleAndCreatedBy(question.getTitle(), question.getCreatedBy())) {
            throw new RuntimeException("题目标题已存在");
        }
        
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        question.setUsageCount(0);
        
        return questionRepository.save(question);
    }
    
    /**
     * 更新题目
     */
    public Question updateQuestion(Long questionId, Question updatedQuestion, Long teacherId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isEmpty()) {
            throw new RuntimeException("题目不存在");
        }
        
        Question existingQuestion = questionOpt.get();
        
        // 检查权限
        if (!existingQuestion.getCreatedBy().equals(teacherId)) {
            throw new RuntimeException("只能修改自己创建的题目");
        }
        
        // 如果修改了标题，检查新标题是否已存在
        if (!existingQuestion.getTitle().equals(updatedQuestion.getTitle()) &&
            questionRepository.existsByTitleAndCreatedBy(updatedQuestion.getTitle(), teacherId)) {
            throw new RuntimeException("题目标题已存在");
        }
        
        // 更新字段
        existingQuestion.setTitle(updatedQuestion.getTitle());
        existingQuestion.setContent(updatedQuestion.getContent());
        existingQuestion.setType(updatedQuestion.getType());
        existingQuestion.setDifficulty(updatedQuestion.getDifficulty());
        existingQuestion.setCategory(updatedQuestion.getCategory());
        existingQuestion.setOptions(updatedQuestion.getOptions());
        existingQuestion.setCorrectAnswer(updatedQuestion.getCorrectAnswer());
        existingQuestion.setExplanation(updatedQuestion.getExplanation());
        existingQuestion.setScore(updatedQuestion.getScore());
        existingQuestion.setTags(updatedQuestion.getTags());
        existingQuestion.setStatus(updatedQuestion.getStatus());
        existingQuestion.setUpdatedAt(LocalDateTime.now());
        
        return questionRepository.save(existingQuestion);
    }
    
    /**
     * 删除题目
     */
    public void deleteQuestion(Long questionId, Long teacherId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isEmpty()) {
            throw new RuntimeException("题目不存在");
        }
        
        Question question = questionOpt.get();
        
        // 检查权限
        if (!question.getCreatedBy().equals(teacherId)) {
            throw new RuntimeException("只能删除自己创建的题目");
        }
        
        questionRepository.deleteById(questionId);
    }
    
    /**
     * 根据ID查找题目
     */
    public Optional<Question> findById(Long questionId) {
        return questionRepository.findById(questionId);
    }
    
    /**
     * 获取教师创建的题目列表
     */
    public Page<Question> getQuestionsByTeacher(Long teacherId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return questionRepository.findByCreatedBy(teacherId, pageable);
    }
    
    /**
     * 根据状态获取教师创建的题目
     */
    public Page<Question> getQuestionsByTeacherAndStatus(Long teacherId, Question.QuestionStatus status, 
                                                        int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return questionRepository.findByCreatedByAndStatus(teacherId, status, pageable);
    }
    
    /**
     * 搜索题目
     */
    public Page<Question> searchQuestions(Long teacherId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return questionRepository.findByCreatedBy(teacherId, pageable);
        }
        
        return questionRepository.findByTitleOrContentContainingAndCreatedBy(keyword.trim(), teacherId, pageable);
    }
    
    /**
     * 综合筛选题目
     */
    public Page<Question> filterQuestions(Long teacherId, Question.QuestionType type, 
                                         Question.QuestionCategory category, Question.QuestionDifficulty difficulty,
                                         Question.QuestionStatus status, String keyword, 
                                         int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return questionRepository.findByFilters(teacherId, type, category, difficulty, status, keyword, pageable);
    }
    
    /**
     * 根据标签搜索题目
     */
    public Page<Question> searchQuestionsByTag(Long teacherId, String tag, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        return questionRepository.findByTagsContainingAndCreatedBy(tag, teacherId, pageable);
    }
    
    /**
     * 批量删除题目
     */
    public void batchDeleteQuestions(List<Long> questionIds, Long teacherId) {
        for (Long questionId : questionIds) {
            Optional<Question> questionOpt = questionRepository.findById(questionId);
            if (questionOpt.isPresent()) {
                Question question = questionOpt.get();
                if (!question.getCreatedBy().equals(teacherId)) {
                    throw new RuntimeException("无权删除题目: " + questionId);
                }
            }
        }
        
        // 验证通过后批量删除
        questionRepository.deleteAllById(questionIds);
    }
    
    /**
     * 批量更新题目状态
     */
    public List<Question> batchUpdateQuestionStatus(List<Long> questionIds, Question.QuestionStatus status, Long teacherId) {
        List<Question> questions = questionRepository.findByIdIn(questionIds);
        
        for (Question question : questions) {
            if (!question.getCreatedBy().equals(teacherId)) {
                throw new RuntimeException("无权修改题目: " + question.getId());
            }
            question.setStatus(status);
            question.setUpdatedAt(LocalDateTime.now());
        }
        
        return questionRepository.saveAll(questions);
    }
    
    /**
     * 复制题目
     */
    public Question duplicateQuestion(Long questionId, Long teacherId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isEmpty()) {
            throw new RuntimeException("题目不存在");
        }
        
        Question originalQuestion = questionOpt.get();
        
        // 检查权限
        if (!originalQuestion.getCreatedBy().equals(teacherId)) {
            throw new RuntimeException("只能复制自己创建的题目");
        }
        
        // 创建副本
        Question duplicatedQuestion = new Question();
        duplicatedQuestion.setTitle(originalQuestion.getTitle() + " (副本)");
        duplicatedQuestion.setContent(originalQuestion.getContent());
        duplicatedQuestion.setType(originalQuestion.getType());
        duplicatedQuestion.setDifficulty(originalQuestion.getDifficulty());
        duplicatedQuestion.setCategory(originalQuestion.getCategory());
        duplicatedQuestion.setOptions(originalQuestion.getOptions());
        duplicatedQuestion.setCorrectAnswer(originalQuestion.getCorrectAnswer());
        duplicatedQuestion.setExplanation(originalQuestion.getExplanation());
        duplicatedQuestion.setScore(originalQuestion.getScore());
        duplicatedQuestion.setTags(originalQuestion.getTags());
        duplicatedQuestion.setCreatedBy(teacherId);
        duplicatedQuestion.setStatus(Question.QuestionStatus.DRAFT);
        duplicatedQuestion.setUsageCount(0);
        duplicatedQuestion.setCreatedAt(LocalDateTime.now());
        duplicatedQuestion.setUpdatedAt(LocalDateTime.now());
        
        return questionRepository.save(duplicatedQuestion);
    }
    
    /**
     * 获取题目统计信息
     */
    public QuestionStatistics getQuestionStatistics(Long teacherId) {
        QuestionStatistics stats = new QuestionStatistics();
        
        stats.setTotalCount(questionRepository.countByCreatedBy(teacherId));
        stats.setDraftCount(questionRepository.countByCreatedByAndStatus(teacherId, Question.QuestionStatus.DRAFT));
        stats.setPublishedCount(questionRepository.countByCreatedByAndStatus(teacherId, Question.QuestionStatus.PUBLISHED));
        stats.setArchivedCount(questionRepository.countByCreatedByAndStatus(teacherId, Question.QuestionStatus.ARCHIVED));
        
        // 按类型统计
        for (Question.QuestionType type : Question.QuestionType.values()) {
            long count = questionRepository.countByCreatedByAndType(teacherId, type);
            stats.getTypeStatistics().put(type.name(), count);
        }
        
        // 按分类统计
        for (Question.QuestionCategory category : Question.QuestionCategory.values()) {
            long count = questionRepository.countByCreatedByAndCategory(teacherId, category);
            stats.getCategoryStatistics().put(category.name(), count);
        }
        
        return stats;
    }
    
    /**
     * 获取最近创建的题目
     */
    public List<Question> getRecentQuestions(Long teacherId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return questionRepository.findRecentQuestionsByCreatedBy(teacherId, pageable);
    }
    
    /**
     * 获取使用次数最多的题目
     */
    public List<Question> getMostUsedQuestions(Long teacherId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return questionRepository.findMostUsedQuestionsByCreatedBy(teacherId, pageable);
    }
    
    /**
     * 增加题目使用次数
     */
    public void incrementUsageCount(Long questionId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            question.setUsageCount(question.getUsageCount() + 1);
            questionRepository.save(question);
        }
    }
    
    /**
     * 统计教师创建的题目数量
     */
    public Long countByCreator(Long teacherId) {
        return questionRepository.countByCreatedBy(teacherId);
    }
    
    /**
     * 获取教师最近创建的题目
     */
    public List<Question> findRecentByCreator(Long teacherId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return questionRepository.findRecentQuestionsByCreatedBy(teacherId, pageable);
    }
    
    /**
     * 题目统计信息内部类
     */
    public static class QuestionStatistics {
        private long totalCount;
        private long draftCount;
        private long publishedCount;
        private long archivedCount;
        private java.util.Map<String, Long> typeStatistics = new java.util.HashMap<>();
        private java.util.Map<String, Long> categoryStatistics = new java.util.HashMap<>();
        
        // Getters and Setters
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        
        public long getDraftCount() { return draftCount; }
        public void setDraftCount(long draftCount) { this.draftCount = draftCount; }
        
        public long getPublishedCount() { return publishedCount; }
        public void setPublishedCount(long publishedCount) { this.publishedCount = publishedCount; }
        
        public long getArchivedCount() { return archivedCount; }
        public void setArchivedCount(long archivedCount) { this.archivedCount = archivedCount; }
        
        public java.util.Map<String, Long> getTypeStatistics() { return typeStatistics; }
        public void setTypeStatistics(java.util.Map<String, Long> typeStatistics) { this.typeStatistics = typeStatistics; }
        
        public java.util.Map<String, Long> getCategoryStatistics() { return categoryStatistics; }
        public void setCategoryStatistics(java.util.Map<String, Long> categoryStatistics) { this.categoryStatistics = categoryStatistics; }
    }
}