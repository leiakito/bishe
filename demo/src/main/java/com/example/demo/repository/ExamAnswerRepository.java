package com.example.demo.repository;

import com.example.demo.entity.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {

    /**
     * 根据考卷ID查找所有答案
     */
    List<ExamAnswer> findByExamPaperId(Long examPaperId);

    /**
     * 根据考卷ID和题目ID查找答案
     */
    Optional<ExamAnswer> findByExamPaperIdAndQuestionId(Long examPaperId, Long questionId);

    /**
     * 查找待评分的答案
     */
    @Query("SELECT ea FROM ExamAnswer ea WHERE ea.gradingStatus = 'PENDING' OR ea.gradingStatus = 'MANUAL_GRADING'")
    List<ExamAnswer> findPendingGrading();

    /**
     * 根据考卷ID查找待评分的答案
     */
    @Query("SELECT ea FROM ExamAnswer ea WHERE ea.examPaperId = :examPaperId " +
           "AND (ea.gradingStatus = 'PENDING' OR ea.gradingStatus = 'MANUAL_GRADING')")
    List<ExamAnswer> findPendingGradingByExamPaperId(@Param("examPaperId") Long examPaperId);

    /**
     * 统计考卷中待评分的答案数量
     */
    @Query("SELECT COUNT(ea) FROM ExamAnswer ea WHERE ea.examPaperId = :examPaperId " +
           "AND (ea.gradingStatus = 'PENDING' OR ea.gradingStatus = 'MANUAL_GRADING')")
    long countPendingGradingByExamPaperId(@Param("examPaperId") Long examPaperId);

    /**
     * 统计考卷中已评分的答案数量
     */
    @Query("SELECT COUNT(ea) FROM ExamAnswer ea WHERE ea.examPaperId = :examPaperId " +
           "AND ea.gradingStatus IN ('AUTO_GRADED', 'MANUAL_GRADED', 'COMPLETED')")
    long countGradedByExamPaperId(@Param("examPaperId") Long examPaperId);

    /**
     * 统计考卷中正确的答案数量
     */
    @Query("SELECT COUNT(ea) FROM ExamAnswer ea WHERE ea.examPaperId = :examPaperId AND ea.isCorrect = true")
    long countCorrectByExamPaperId(@Param("examPaperId") Long examPaperId);

    /**
     * 计算考卷的总分
     */
    @Query("SELECT SUM(ea.score) FROM ExamAnswer ea WHERE ea.examPaperId = :examPaperId")
    Double sumScoreByExamPaperId(@Param("examPaperId") Long examPaperId);

    /**
     * 计算考卷的客观题得分
     */
    @Query("SELECT SUM(ea.score) FROM ExamAnswer ea WHERE ea.examPaperId = :examPaperId " +
           "AND ea.gradingStatus = 'AUTO_GRADED'")
    Double sumObjectiveScoreByExamPaperId(@Param("examPaperId") Long examPaperId);

    /**
     * 计算考卷的主观题得分
     */
    @Query("SELECT SUM(ea.score) FROM ExamAnswer ea WHERE ea.examPaperId = :examPaperId " +
           "AND ea.gradingStatus IN ('MANUAL_GRADED', 'COMPLETED')")
    Double sumSubjectiveScoreByExamPaperId(@Param("examPaperId") Long examPaperId);

    /**
     * 根据题目ID查找所有答案(用于统计题目正确率)
     */
    List<ExamAnswer> findByQuestionId(Long questionId);

    /**
     * 统计某道题的正确率
     */
    @Query("SELECT COUNT(ea) * 1.0 / (SELECT COUNT(ea2) FROM ExamAnswer ea2 WHERE ea2.questionId = :questionId) " +
           "FROM ExamAnswer ea WHERE ea.questionId = :questionId AND ea.isCorrect = true")
    Double getCorrectRateByQuestionId(@Param("questionId") Long questionId);

    /**
     * 删除考卷的所有答案
     */
    void deleteByExamPaperId(Long examPaperId);
}
