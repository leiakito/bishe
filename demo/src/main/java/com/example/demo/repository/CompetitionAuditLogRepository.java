package com.example.demo.repository;

import com.example.demo.entity.CompetitionAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionAuditLogRepository extends JpaRepository<CompetitionAuditLog, Long> {
    
    // 根据竞赛ID查找审核日志
    List<CompetitionAuditLog> findByCompetitionIdOrderByCreatedAtDesc(Long competitionId);
    
    // 根据竞赛ID查找审核日志（分页）
    Page<CompetitionAuditLog> findByCompetitionIdOrderByCreatedAtDesc(Long competitionId, Pageable pageable);
    
    // 根据审核员ID查找审核日志
    List<CompetitionAuditLog> findByReviewerIdOrderByCreatedAtDesc(Long reviewerId);
    
    // 根据审核员ID查找审核日志（分页）
    Page<CompetitionAuditLog> findByReviewerIdOrderByCreatedAtDesc(Long reviewerId, Pageable pageable);
    
    // 根据操作类型查找审核日志
    List<CompetitionAuditLog> findByActionOrderByCreatedAtDesc(CompetitionAuditLog.AuditAction action);
    
    // 查找所有审核日志（分页）
    Page<CompetitionAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 根据竞赛ID和操作类型查找审核日志
    @Query("SELECT cal FROM CompetitionAuditLog cal WHERE cal.competition.id = :competitionId AND cal.action = :action ORDER BY cal.createdAt DESC")
    List<CompetitionAuditLog> findByCompetitionIdAndAction(@Param("competitionId") Long competitionId, @Param("action") CompetitionAuditLog.AuditAction action);
    
    // 统计某个竞赛的审核次数
    @Query("SELECT COUNT(cal) FROM CompetitionAuditLog cal WHERE cal.competition.id = :competitionId")
    Long countByCompetitionId(@Param("competitionId") Long competitionId);
    
    // 统计某个审核员的审核次数
    @Query("SELECT COUNT(cal) FROM CompetitionAuditLog cal WHERE cal.reviewer.id = :reviewerId")
    Long countByReviewerId(@Param("reviewerId") Long reviewerId);
    
    // 删除竞赛的所有审核日志
    void deleteByCompetitionId(Long competitionId);
}