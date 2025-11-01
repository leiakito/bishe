package com.example.demo.repository;

import com.example.demo.entity.ExamPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamPaperRepository extends JpaRepository<ExamPaper, Long> {

    /**
     * 根据竞赛ID查找所有考卷
     */
    List<ExamPaper> findByCompetitionId(Long competitionId);

    /**
     * 根据竞赛ID分页查询考卷
     */
    Page<ExamPaper> findByCompetitionId(Long competitionId, Pageable pageable);

    /**
     * 根据参赛者类型和ID查找考卷
     */
    List<ExamPaper> findByParticipantTypeAndParticipantId(
            ExamPaper.ParticipantType participantType,
            Long participantId
    );

    /**
     * 查找某个参赛者在某个竞赛的考卷
     */
    Optional<ExamPaper> findByCompetitionIdAndParticipantTypeAndParticipantId(
            Long competitionId,
            ExamPaper.ParticipantType participantType,
            Long participantId
    );

    /**
     * 检查考卷是否存在
     */
    boolean existsByCompetitionIdAndParticipantTypeAndParticipantId(
            Long competitionId,
            ExamPaper.ParticipantType participantType,
            Long participantId
    );

    /**
     * 根据状态查找考卷
     */
    List<ExamPaper> findByPaperStatus(ExamPaper.PaperStatus status);

    /**
     * 根据竞赛ID和状态查找考卷
     */
    List<ExamPaper> findByCompetitionIdAndPaperStatus(Long competitionId, ExamPaper.PaperStatus status);

    /**
     * 统计竞赛的考卷数量
     */
    long countByCompetitionId(Long competitionId);

    /**
     * 统计竞赛中已提交的考卷数量
     */
    @Query("SELECT COUNT(ep) FROM ExamPaper ep WHERE ep.competitionId = :competitionId " +
           "AND ep.paperStatus IN ('SUBMITTED', 'GRADING', 'GRADED')")
    long countSubmittedByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 查找待评分的考卷
     */
    @Query("SELECT ep FROM ExamPaper ep WHERE ep.paperStatus IN ('SUBMITTED', 'GRADING')")
    List<ExamPaper> findPendingGrading();

    /**
     * 根据竞赛ID查找待评分的考卷
     */
    @Query("SELECT ep FROM ExamPaper ep WHERE ep.competitionId = :competitionId " +
           "AND ep.paperStatus IN ('SUBMITTED', 'GRADING')")
    List<ExamPaper> findPendingGradingByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 计算竞赛的平均分
     */
    @Query("SELECT AVG(ep.totalScore) FROM ExamPaper ep WHERE ep.competitionId = :competitionId " +
           "AND ep.paperStatus = 'GRADED'")
    Double getAverageScoreByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 查找竞赛的最高分
     */
    @Query("SELECT MAX(ep.totalScore) FROM ExamPaper ep WHERE ep.competitionId = :competitionId " +
           "AND ep.paperStatus = 'GRADED'")
    Double getMaxScoreByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 查找竞赛的最低分
     */
    @Query("SELECT MIN(ep.totalScore) FROM ExamPaper ep WHERE ep.competitionId = :competitionId " +
           "AND ep.paperStatus = 'GRADED'")
    Double getMinScoreByCompetitionId(@Param("competitionId") Long competitionId);
    
    /**
     * 删除竞赛的所有考试试卷
     */
    void deleteByCompetitionId(Long competitionId);
}
