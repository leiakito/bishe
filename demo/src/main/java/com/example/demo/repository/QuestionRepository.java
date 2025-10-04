package com.example.demo.repository;

import com.example.demo.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题目数据访问层
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    /**
     * 根据创建者ID查找题目
     */
    Page<Question> findByCreatedBy(Long createdBy, Pageable pageable);
    
    /**
     * 根据创建者ID和状态查找题目
     */
    Page<Question> findByCreatedByAndStatus(Long createdBy, Question.QuestionStatus status, Pageable pageable);
    
    /**
     * 根据题目类型查找题目
     */
    Page<Question> findByType(Question.QuestionType type, Pageable pageable);
    
    /**
     * 根据题目分类查找题目
     */
    Page<Question> findByCategory(Question.QuestionCategory category, Pageable pageable);
    
    /**
     * 根据题目难度查找题目
     */
    Page<Question> findByDifficulty(Question.QuestionDifficulty difficulty, Pageable pageable);
    
    /**
     * 根据创建者ID、类型和状态查找题目
     */
    Page<Question> findByCreatedByAndTypeAndStatus(Long createdBy, Question.QuestionType type, Question.QuestionStatus status, Pageable pageable);
    
    /**
     * 根据创建者ID、分类和状态查找题目
     */
    Page<Question> findByCreatedByAndCategoryAndStatus(Long createdBy, Question.QuestionCategory category, Question.QuestionStatus status, Pageable pageable);
    
    /**
     * 根据创建者ID、难度和状态查找题目
     */
    Page<Question> findByCreatedByAndDifficultyAndStatus(Long createdBy, Question.QuestionDifficulty difficulty, Question.QuestionStatus status, Pageable pageable);
    
    /**
     * 根据标题模糊查询题目
     */
    @Query("SELECT q FROM Question q WHERE q.title LIKE %:keyword% AND q.createdBy = :createdBy")
    Page<Question> findByTitleContainingAndCreatedBy(@Param("keyword") String keyword, @Param("createdBy") Long createdBy, Pageable pageable);
    
    /**
     * 根据标题或内容模糊查询题目
     */
    @Query("SELECT q FROM Question q WHERE (q.title LIKE %:keyword% OR q.content LIKE %:keyword%) AND q.createdBy = :createdBy")
    Page<Question> findByTitleOrContentContainingAndCreatedBy(@Param("keyword") String keyword, @Param("createdBy") Long createdBy, Pageable pageable);
    
    /**
     * 根据标签查找题目
     */
    @Query("SELECT q FROM Question q WHERE q.tags LIKE %:tag% AND q.createdBy = :createdBy")
    Page<Question> findByTagsContainingAndCreatedBy(@Param("tag") String tag, @Param("createdBy") Long createdBy, Pageable pageable);
    
    /**
     * 综合筛选题目
     */
    @Query("SELECT q FROM Question q WHERE q.createdBy = :createdBy " +
           "AND (:type IS NULL OR q.type = :type) " +
           "AND (:category IS NULL OR q.category = :category) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:status IS NULL OR q.status = :status) " +
           "AND (:keyword IS NULL OR q.title LIKE %:keyword% OR q.content LIKE %:keyword%)")
    Page<Question> findByFilters(@Param("createdBy") Long createdBy,
                                @Param("type") Question.QuestionType type,
                                @Param("category") Question.QuestionCategory category,
                                @Param("difficulty") Question.QuestionDifficulty difficulty,
                                @Param("status") Question.QuestionStatus status,
                                @Param("keyword") String keyword,
                                Pageable pageable);
    
    /**
     * 统计教师创建的题目数量
     */
    long countByCreatedBy(Long createdBy);
    
    /**
     * 统计教师各状态题目数量
     */
    long countByCreatedByAndStatus(Long createdBy, Question.QuestionStatus status);
    
    /**
     * 统计教师各类型题目数量
     */
    long countByCreatedByAndType(Long createdBy, Question.QuestionType type);
    
    /**
     * 统计教师各分类题目数量
     */
    long countByCreatedByAndCategory(Long createdBy, Question.QuestionCategory category);
    
    /**
     * 获取最近创建的题目
     */
    @Query("SELECT q FROM Question q WHERE q.createdBy = :createdBy ORDER BY q.createdAt DESC")
    List<Question> findRecentQuestionsByCreatedBy(@Param("createdBy") Long createdBy, Pageable pageable);
    
    /**
     * 获取使用次数最多的题目
     */
    @Query("SELECT q FROM Question q WHERE q.createdBy = :createdBy ORDER BY q.usageCount DESC")
    List<Question> findMostUsedQuestionsByCreatedBy(@Param("createdBy") Long createdBy, Pageable pageable);
    
    /**
     * 检查题目标题是否已存在（同一创建者）
     */
    boolean existsByTitleAndCreatedBy(String title, Long createdBy);
    
    /**
     * 获取所有已发布的题目（供其他教师参考）
     */
    Page<Question> findByStatus(Question.QuestionStatus status, Pageable pageable);
    
    /**
     * 根据多个ID批量查询题目
     */
    List<Question> findByIdIn(List<Long> ids);
}