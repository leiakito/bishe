package com.example.demo.repository;

import com.example.demo.entity.Competition;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    
    // 根据竞赛名称查找
    List<Competition> findByNameContaining(String name);
    
    // 根据分类查找竞赛
    List<Competition> findByCategory(Competition.CompetitionCategory category);
    
    // 根据级别查找竞赛
    List<Competition> findByLevel(Competition.CompetitionLevel level);
    
    // 根据状态查找竞赛
    List<Competition> findByStatus(Competition.CompetitionStatus status);
    
    // 根据状态统计竞赛数量
    Long countByStatus(Competition.CompetitionStatus status);
    
    // 根据创建者查找竞赛
    Page<Competition> findByCreatorId(Long creatorId, Pageable pageable);
    
    // 根据创建者查找竞赛，按创建时间降序排列
    Page<Competition> findByCreatorOrderByCreatedAtDesc(User creator, Pageable pageable);
    Page<Competition> findByCreatorOrderByCreatedAtDesc(Long creatorId, Pageable pageable);
    
    // 查找用户可报名的竞赛
    @Query("SELECT c FROM Competition c WHERE c.status = 'REGISTRATION_OPEN' AND c.registrationEndTime > :now")
    List<Competition> findAvailableForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    // 查找热门竞赛
    @Query("SELECT c FROM Competition c ORDER BY c.registrationCount DESC")
    List<Competition> findPopularCompetitions(Pageable pageable);
    
    // 按类别统计竞赛数量
    @Query("SELECT c.category, COUNT(c) FROM Competition c GROUP BY c.category")
    List<Object[]> countCompetitionsByCategory();
    
    // 按状态统计竞赛数量
    @Query("SELECT c.status, COUNT(c) FROM Competition c GROUP BY c.status")
    List<Object[]> countCompetitionsByStatus();
    
    // 统计创建者的竞赛数量
    @Query("SELECT COUNT(c) FROM Competition c WHERE c.creator = :creator")
    Long countByCreator(@Param("creator") User creator);
    
    // 根据日期范围查找竞赛
    Page<Competition> findByCompetitionStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // 检查竞赛名称是否存在
    boolean existsByName(String name);
    
    // 根据状态查找竞赛（分页）
    Page<Competition> findByStatus(Competition.CompetitionStatus status, Pageable pageable);
    
    // 根据分类和状态查找竞赛
    Page<Competition> findByCategoryAndStatus(Competition.CompetitionCategory category, 
                                            Competition.CompetitionStatus status, 
                                            Pageable pageable);
    
    // 根据级别和状态查找竞赛
    Page<Competition> findByLevelAndStatus(Competition.CompetitionLevel level, 
                                         Competition.CompetitionStatus status, 
                                         Pageable pageable);
    
    // 模糊搜索竞赛（名称、描述）
    @Query("SELECT c FROM Competition c WHERE c.name LIKE %:keyword% OR c.description LIKE %:keyword%")
    Page<Competition> searchCompetitions(@Param("keyword") String keyword, Pageable pageable);
    
    // 查找正在报名的竞赛
    @Query("SELECT c FROM Competition c WHERE c.status = 'REGISTRATION_OPEN' AND c.registrationStartTime <= :now AND c.registrationEndTime >= :now")
    List<Competition> findOpenForRegistration(@Param("now") LocalDateTime now);
    
    // 查找即将开始的竞赛
    @Query("SELECT c FROM Competition c WHERE c.status = 'UPCOMING' AND c.competitionStartTime > :now ORDER BY c.competitionStartTime ASC")
    List<Competition> findUpcomingCompetitions(@Param("now") LocalDateTime now, Pageable pageable);
    
    // 查找正在进行的竞赛
    @Query("SELECT c FROM Competition c WHERE c.status = 'ONGOING' AND c.competitionStartTime <= :now AND c.competitionEndTime >= :now")
    List<Competition> findOngoingCompetitions(@Param("now") LocalDateTime now);
    
    // 查找已结束的竞赛
    @Query("SELECT c FROM Competition c WHERE c.status = 'COMPLETED' AND c.competitionEndTime < :now ORDER BY c.competitionEndTime DESC")
    List<Competition> findCompletedCompetitions(@Param("now") LocalDateTime now, Pageable pageable);
    
    // 查找待审核的竞赛
    List<Competition> findByStatusOrderByCreatedAtDesc(Competition.CompetitionStatus status);
    
    // 根据时间范围查找竞赛
    @Query("SELECT c FROM Competition c WHERE c.competitionStartTime BETWEEN :startDate AND :endDate")
    List<Competition> findCompetitionsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    // 查找热门竞赛（根据报名团队数量）
    @Query("SELECT c FROM Competition c LEFT JOIN c.registrations r GROUP BY c ORDER BY COUNT(r) DESC")
    List<Competition> findPopularCompetitionsByRegistrations(Pageable pageable);
    
    // 查找用户创建的竞赛
    @Query("SELECT c FROM Competition c WHERE c.creator.id = :userId ORDER BY c.createdAt DESC")
    Page<Competition> findByCreatedBy(@Param("userId") Long userId, Pageable pageable);
    
    // 查找用户可以报名的竞赛（排除已报名的）
    @Query("SELECT c FROM Competition c WHERE c.status = 'REGISTRATION_OPEN' AND c.registrationStartTime <= :now AND c.registrationEndTime >= :now AND c.id NOT IN (SELECT r.competition.id FROM Registration r WHERE r.submittedBy.id = :userId)")
    List<Competition> findAvailableForUserExcludingRegistered(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    // 根据多个状态查询竞赛
    @Query("SELECT c FROM Competition c WHERE c.status IN :statuses ORDER BY c.createdAt DESC")
    Page<Competition> findByStatusInOrderByCreatedAtDesc(
            @Param("statuses") List<Competition.CompetitionStatus> statuses, 
            Pageable pageable);
    
    // 综合筛选竞赛
    @Query("SELECT c FROM Competition c WHERE " +
           "(:keyword IS NULL OR c.name LIKE %:keyword% OR c.description LIKE %:keyword%) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:startDate IS NULL OR c.competitionStartTime >= :startDate) AND " +
           "(:endDate IS NULL OR c.competitionEndTime <= :endDate)")
    Page<Competition> filterCompetitions(
            @Param("keyword") String keyword,
            @Param("category") Competition.CompetitionCategory category,
            @Param("status") Competition.CompetitionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}