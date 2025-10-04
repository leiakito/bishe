package com.example.demo.repository;

import com.example.demo.entity.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    // 根据团队ID查找成绩
    List<Grade> findByTeamId(Long teamId);
    
    // 根据团队ID查找成绩（分页）
    Page<Grade> findByTeamId(Long teamId, Pageable pageable);
    
    // 根据竞赛查找成绩
    List<Grade> findByCompetitionId(Long competitionId);
    
    // 根据团队和竞赛查找成绩
    Optional<Grade> findByTeamIdAndCompetitionId(Long teamId, Long competitionId);
    
    // 根据竞赛查找成绩（分页，按分数降序）
    Page<Grade> findByCompetitionIdOrderByScoreDesc(Long competitionId, Pageable pageable);
    
    // 根据竞赛查找成绩（按排名升序）
    List<Grade> findByCompetitionIdOrderByRankingAsc(Long competitionId);
    
    // 根据奖项等级查找成绩
    List<Grade> findByAwardLevel(Grade.AwardLevel awardLevel);
    
    // 根据竞赛和奖项等级查找成绩
    List<Grade> findByCompetitionIdAndAwardLevel(Long competitionId, Grade.AwardLevel awardLevel);
    
    // 查找最终成绩
    List<Grade> findByIsFinalTrue();
    
    // 根据竞赛查找最终成绩
    List<Grade> findByCompetitionIdAndIsFinalTrue(Long competitionId);
    
    // 根据评分人查找成绩
    List<Grade> findByGradedBy(Long graderId);
    
    // 根据评分人查找成绩（分页）
    Page<Grade> findByGradedBy(Long graderId, Pageable pageable);
    
    // 根据评分人和竞赛查找成绩（分页）
    Page<Grade> findByGradedByAndCompetitionId(Long graderId, Long competitionId, Pageable pageable);
    
    // 统计评分人录入的成绩数量
    long countByGradedBy(Long graderId);
    
    // 查找团队的最高分
    @Query("SELECT MAX(g.score) FROM Grade g WHERE g.team.id = :teamId")
    Optional<BigDecimal> findMaxScoreByTeamId(@Param("teamId") Long teamId);
    
    // 查找竞赛的最高分
    @Query("SELECT MAX(g.score) FROM Grade g WHERE g.competition.id = :competitionId")
    Optional<BigDecimal> findMaxScoreByCompetitionId(@Param("competitionId") Long competitionId);
    
    // 查找竞赛的平均分
    @Query("SELECT AVG(g.score) FROM Grade g WHERE g.competition.id = :competitionId")
    Optional<BigDecimal> findAverageScoreByCompetitionId(@Param("competitionId") Long competitionId);
    
    // 查找分数范围内的成绩
    @Query("SELECT g FROM Grade g WHERE g.competition.id = :competitionId AND g.score BETWEEN :minScore AND :maxScore")
    List<Grade> findByCompetitionIdAndScoreBetween(@Param("competitionId") Long competitionId, 
                                                  @Param("minScore") BigDecimal minScore, 
                                                  @Param("maxScore") BigDecimal maxScore);
    
    // 获取竞赛排行榜（分页）
    @Query("SELECT g FROM Grade g WHERE g.competition.id = :competitionId ORDER BY g.score DESC")
    Page<Grade> findCompetitionRanking(@Param("competitionId") Long competitionId, Pageable pageable);
    
    // 获取竞赛排行榜（不分页）
    @Query("SELECT g FROM Grade g WHERE g.competition.id = :competitionId ORDER BY g.score DESC")
    List<Grade> findCompetitionRankingList(@Param("competitionId") Long competitionId);
    
    // 根据团队成员用户ID查找成绩
    Page<Grade> findByTeamMembersUserId(Long userId, Pageable pageable);
    
    // 根据团队成员用户ID查找成绩（非分页）
    List<Grade> findByTeamMembersUserId(Long userId);
    

    
    // 获取所有成绩按分数降序排列（分页）
    Page<Grade> findAllByOrderByScoreDesc(Pageable pageable);
    
    // 统计竞赛各奖项数量
    @Query("SELECT g.awardLevel, COUNT(g) FROM Grade g WHERE g.competition.id = :competitionId GROUP BY g.awardLevel")
    List<Object[]> countAwardsByCompetition(@Param("competitionId") Long competitionId);
    
    // 统计总体各奖项数量
    @Query("SELECT g.awardLevel, COUNT(g) FROM Grade g GROUP BY g.awardLevel")
    List<Object[]> countAwardsByLevel();
    
    // 查找有证书的成绩
    @Query("SELECT g FROM Grade g WHERE g.certificateUrl IS NOT NULL AND g.certificateUrl != ''")
    List<Grade> findGradesWithCertificate();
    
    // 根据竞赛查找有证书的成绩
    @Query("SELECT g FROM Grade g WHERE g.competition.id = :competitionId AND g.certificateUrl IS NOT NULL AND g.certificateUrl != ''")
    List<Grade> findGradesWithCertificateByCompetition(@Param("competitionId") Long competitionId);
    
    // 查找用户的所有成绩（通过团队成员关系）
    @Query("SELECT g FROM Grade g WHERE g.team.id IN (SELECT tm.team.id FROM TeamMember tm WHERE tm.user.id = :userId)")
    List<Grade> findGradesByUserId(@Param("userId") Long userId);
    
    // 查找用户在特定竞赛中的成绩
    @Query("SELECT g FROM Grade g WHERE g.competition.id = :competitionId AND g.team.id IN (SELECT tm.team.id FROM TeamMember tm WHERE tm.user.id = :userId)")
    Optional<Grade> findUserGradeInCompetition(@Param("userId") Long userId, @Param("competitionId") Long competitionId);
    
    // 统计竞赛参与团队数量
    @Query("SELECT COUNT(DISTINCT g.team.id) FROM Grade g WHERE g.competition.id = :competitionId")
    Long countTeamsByCompetitionId(@Param("competitionId") Long competitionId);
    
    // 查找最近评分的成绩
    @Query("SELECT g FROM Grade g ORDER BY g.gradedAt DESC")
    List<Grade> findRecentGrades(Pageable pageable);
    
    // 检查团队在竞赛中是否有成绩
    boolean existsByTeamIdAndCompetitionId(Long teamId, Long competitionId);
    
    // 查找需要排名的成绩（分数不为空但排名为空）
    @Query("SELECT g FROM Grade g WHERE g.competition.id = :competitionId AND g.score IS NOT NULL AND g.ranking IS NULL")
    List<Grade> findGradesNeedingRanking(@Param("competitionId") Long competitionId);
}