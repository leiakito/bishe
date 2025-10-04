package com.example.demo.repository;

import com.example.demo.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    
    // 根据报名号查找
    Optional<Registration> findByRegistrationNumber(String registrationNumber);
    
    // 根据竞赛查找报名
    List<Registration> findByCompetitionId(Long competitionId);
    
    // 根据团队查找报名
    Optional<Registration> findByTeamId(Long teamId);
    
    // 根据提交用户查找报名
    List<Registration> findBySubmittedById(Long userId);
    
    // 根据状态查找报名
    List<Registration> findByStatus(Registration.RegistrationStatus status);
    
    // 根据状态查找报名（分页）
    Page<Registration> findByStatus(Registration.RegistrationStatus status, Pageable pageable);
    
    // 根据竞赛和状态查找报名
    List<Registration> findByCompetitionIdAndStatus(Long competitionId, Registration.RegistrationStatus status);
    
    // 根据竞赛和状态查找报名（分页）
    Page<Registration> findByCompetitionIdAndStatus(Long competitionId, Registration.RegistrationStatus status, Pageable pageable);
    
    // 根据竞赛实体和状态查找报名
    List<Registration> findByCompetitionAndStatus(com.example.demo.entity.Competition competition, Registration.RegistrationStatus status);
    
    // 根据竞赛实体和状态查找报名（分页）
    Page<Registration> findByCompetitionAndStatus(com.example.demo.entity.Competition competition, Registration.RegistrationStatus status, Pageable pageable);
    
    // 根据支付状态查找报名
    List<Registration> findByPaymentStatus(Registration.PaymentStatus paymentStatus);
    
    // 根据支付状态查找报名（分页）
    Page<Registration> findByPaymentStatus(Registration.PaymentStatus paymentStatus, Pageable pageable);
    
    // 根据竞赛和支付状态查找报名
    List<Registration> findByCompetitionIdAndPaymentStatus(Long competitionId, Registration.PaymentStatus paymentStatus);
    
    // 查找待审核的报名
    List<Registration> findByStatusOrderByCreatedAtAsc(Registration.RegistrationStatus status);
    
    // 查找用户在特定竞赛中的报名
    @Query("SELECT r FROM Registration r WHERE r.competition.id = :competitionId AND r.submittedBy.id = :userId")
    Optional<Registration> findByCompetitionAndUser(@Param("competitionId") Long competitionId, @Param("userId") Long userId);
    
    // 检查团队是否已报名
    boolean existsByTeamId(Long teamId);
    
    // 检查用户是否已在竞赛中报名
    @Query("SELECT COUNT(r) > 0 FROM Registration r WHERE r.competition.id = :competitionId AND r.submittedBy.id = :userId")
    boolean existsByCompetitionAndUser(@Param("competitionId") Long competitionId, @Param("userId") Long userId);
    
    // 统计竞赛报名数量
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.competition.id = :competitionId")
    Long countByCompetitionId(@Param("competitionId") Long competitionId);
    
    // 统计竞赛特定状态的报名数量
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.competition.id = :competitionId AND r.status = :status")
    Long countByCompetitionIdAndStatus(@Param("competitionId") Long competitionId, @Param("status") Registration.RegistrationStatus status);
    
    // 统计竞赛已审核通过的报名数量
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.competition.id = :competitionId AND r.status = 'APPROVED'")
    Long countApprovedByCompetitionId(@Param("competitionId") Long competitionId);
    
    // 统计各状态报名数量
    @Query("SELECT r.status, COUNT(r) FROM Registration r GROUP BY r.status")
    List<Object[]> countRegistrationsByStatus();
    
    // 统计各支付状态报名数量
    @Query("SELECT r.paymentStatus, COUNT(r) FROM Registration r GROUP BY r.paymentStatus")
    List<Object[]> countRegistrationsByPaymentStatus();
    
    // 查找最近的报名
    @Query("SELECT r FROM Registration r ORDER BY r.createdAt DESC")
    List<Registration> findRecentRegistrations(Pageable pageable);
    
    // 根据时间范围查找报名
    @Query("SELECT r FROM Registration r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    List<Registration> findRegistrationsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    // 根据报名时间范围查找报名
    List<Registration> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // 根据提交用户和竞赛查找报名
    List<Registration> findBySubmittedByIdAndCompetitionId(Long userId, Long competitionId);
    
    // 查找需要支付的报名
    @Query("SELECT r FROM Registration r WHERE r.status = 'APPROVED' AND r.paymentStatus = 'UNPAID' AND r.paymentAmount > 0")
    List<Registration> findUnpaidRegistrations();
    
    // 查找用户的所有报名（分页）
    Page<Registration> findBySubmittedByIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 查找竞赛的所有报名（分页）
    Page<Registration> findByCompetitionIdOrderByCreatedAtDesc(Long competitionId, Pageable pageable);
    
    // 查找审核人的审核记录
    List<Registration> findByReviewedByOrderByReviewedAtDesc(Long reviewerId);
    
    // 查找特定时间段内的支付记录
    @Query("SELECT r FROM Registration r WHERE r.paymentStatus = 'PAID' AND r.paymentTime BETWEEN :startDate AND :endDate")
    List<Registration> findPaidRegistrationsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    // 统计竞赛收入
    @Query("SELECT SUM(r.paymentAmount) FROM Registration r WHERE r.competition.id = :competitionId AND r.paymentStatus = 'PAID'")
    Double calculateCompetitionRevenue(@Param("competitionId") Long competitionId);
    
    // 统计竞赛实体特定状态的报名数量
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.competition = :competition AND r.status = :status")
    Long countByCompetitionAndStatus(@Param("competition") com.example.demo.entity.Competition competition, @Param("status") Registration.RegistrationStatus status);
}