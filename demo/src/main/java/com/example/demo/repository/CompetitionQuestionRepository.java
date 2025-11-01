package com.example.demo.repository;

import com.example.demo.entity.CompetitionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitionQuestionRepository extends JpaRepository<CompetitionQuestion, Long> {

    /**
     * 根据竞赛ID查找所有题目(按顺序排列)
     */
    List<CompetitionQuestion> findByCompetitionIdOrderByQuestionOrder(Long competitionId);

    /**
     * 根据竞赛ID和题目ID查找关联记录
     */
    Optional<CompetitionQuestion> findByCompetitionIdAndQuestionId(Long competitionId, Long questionId);

    /**
     * 检查题目是否已关联到竞赛
     */
    boolean existsByCompetitionIdAndQuestionId(Long competitionId, Long questionId);

    /**
     * 统计某个竞赛的题目数量
     */
    long countByCompetitionId(Long competitionId);

    /**
     * 统计某个题目被多少个竞赛使用
     */
    @Query("SELECT COUNT(cq) FROM CompetitionQuestion cq WHERE cq.questionId = :questionId")
    long countByQuestionId(@Param("questionId") Long questionId);

    /**
     * 查找某个题目关联的所有竞赛ID
     */
    @Query("SELECT cq.competitionId FROM CompetitionQuestion cq WHERE cq.questionId = :questionId")
    List<Long> findCompetitionIdsByQuestionId(@Param("questionId") Long questionId);

    /**
     * 删除竞赛的所有题目关联
     */
    @Transactional
    @Modifying
    void deleteByCompetitionId(Long competitionId);

    /**
     * 删除指定竞赛的指定题目
     */
    @Transactional
    @Modifying
    void deleteByCompetitionIdAndQuestionId(Long competitionId, Long questionId);

    /**
     * 计算竞赛的总分
     */
    @Query("SELECT SUM(cq.questionScore) FROM CompetitionQuestion cq WHERE cq.competitionId = :competitionId AND cq.isActive = true")
    Double sumScoreByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 查找竞赛中启用的题目
     */
    List<CompetitionQuestion> findByCompetitionIdAndIsActiveTrueOrderByQuestionOrder(Long competitionId);
}
