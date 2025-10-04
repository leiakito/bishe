package com.example.demo.service;

import com.example.demo.entity.Registration;
import com.example.demo.entity.Competition;
import com.example.demo.entity.Team;
import com.example.demo.entity.User;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.CompetitionRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RegistrationService {
    
    @Autowired
    private RegistrationRepository registrationRepository;
    
    @Autowired
    private CompetitionRepository competitionRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamService teamService;

    // 单人报名竞赛
    public Registration registerIndividual(Long competitionId, Long userId) {
        // 验证竞赛
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Competition competition = competitionOpt.get();
        if (competition.getStatus() != Competition.CompetitionStatus.REGISTRATION_OPEN) {
            throw new RuntimeException("竞赛未开放报名");
        }
        
        // 检查报名时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(competition.getRegistrationStartTime()) || 
            now.isAfter(competition.getRegistrationEndTime())) {
            throw new RuntimeException("不在报名时间范围内");
        }
        
        // 验证用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        if (user.getRole() != User.UserRole.STUDENT) {
            throw new RuntimeException("只有学生可以报名竞赛");
        }
        
        // 检查是否已经报名
        if (isUserRegistered(userId, competitionId)) {
            throw new RuntimeException("您已经报名该竞赛");
        }
        
        // 检查竞赛是否已满
        if (competition.getMaxTeams() != null) {
            long registeredCount = registrationRepository.countByCompetitionIdAndStatus(
                competition.getId(), Registration.RegistrationStatus.APPROVED);
            if (registeredCount >= competition.getMaxTeams()) {
                throw new RuntimeException("竞赛报名已满");
            }
        }

        // 为个人报名创建一个单人团队
        Team individualTeam = teamService.createIndividualTeam(competition, user);

        // 创建报名记录
        Registration registration = new Registration();
        registration.setRegistrationNumber(generateRegistrationNumber());
        registration.setCompetition(competition);
        registration.setTeam(individualTeam); // 关联个人团队
        registration.setSubmittedBy(user);
        registration.setStatus(Registration.RegistrationStatus.PENDING);
        if (competition.getRegistrationFee() != null) {
            registration.setPaymentAmount(competition.getRegistrationFee().doubleValue());
        } else {
            registration.setPaymentAmount(0.0);
        }
        
        // 如果免费，直接确认
        if (competition.getRegistrationFee() == null || 
            competition.getRegistrationFee().equals(0.0)) {
            registration.setStatus(Registration.RegistrationStatus.APPROVED);
            registration.setPaymentStatus(Registration.PaymentStatus.NOT_REQUIRED);
        } else {
            registration.setPaymentStatus(Registration.PaymentStatus.UNPAID);
        }
        
        return registrationRepository.save(registration);
    }
    
    // 团队报名竞赛
    public Registration registerTeam(Long competitionId, Long teamId, Long submittedBy) {
        // 验证竞赛
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Competition competition = competitionOpt.get();
        if (competition.getStatus() != Competition.CompetitionStatus.REGISTRATION_OPEN) {
            throw new RuntimeException("竞赛未开放报名");
        }
        
        // 检查报名时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(competition.getRegistrationStartTime()) || 
            now.isAfter(competition.getRegistrationEndTime())) {
            throw new RuntimeException("不在报名时间范围内");
        }
        
        // 验证团队
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        if (!team.getCompetition().getId().equals(competitionId)) {
            throw new RuntimeException("团队不属于该竞赛");
        }
        
        if (team.getStatus() != Team.TeamStatus.ACTIVE) {
            throw new RuntimeException("团队状态不允许报名");
        }
        
        // 验证提交者
        Optional<User> submitterOpt = userRepository.findById(submittedBy);
        if (submitterOpt.isEmpty()) {
            throw new RuntimeException("提交者不存在");
        }
        
        // 检查是否已经报名
        Optional<Registration> existingRegistration = registrationRepository.findByTeamId(teamId);
        if (existingRegistration.isPresent()) {
            throw new RuntimeException("团队已经报名该竞赛");
        }
        
        // 检查竞赛是否已满
        if (competition.getMaxTeams() != null) {
            long registeredTeams = registrationRepository.countByCompetitionIdAndStatus(
                competition.getId(), Registration.RegistrationStatus.APPROVED);
            if (registeredTeams >= competition.getMaxTeams()) {
                throw new RuntimeException("竞赛报名已满");
            }
        }
        
        // 创建报名记录
        Registration registration = new Registration();
        registration.setRegistrationNumber(generateRegistrationNumber());
        registration.setCompetition(competition);
        registration.setTeam(team);
        Optional<User> userOpt = userRepository.findById(submittedBy);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        registration.setSubmittedBy(userOpt.get());
        registration.setStatus(Registration.RegistrationStatus.PENDING);
        if (competition.getRegistrationFee() != null) {
            registration.setPaymentAmount(competition.getRegistrationFee().doubleValue());
        } else {
            registration.setPaymentAmount(0.0);
        }
        
        // 如果免费，直接确认
        if (competition.getRegistrationFee() == null || 
            competition.getRegistrationFee().equals(0.0)) {
            registration.setStatus(Registration.RegistrationStatus.APPROVED);
            registration.setPaymentStatus(Registration.PaymentStatus.NOT_REQUIRED);
        } else {
            registration.setPaymentStatus(Registration.PaymentStatus.PAID);
        }
        
        return registrationRepository.save(registration);
    }
    
    // 生成报名编号
    private String generateRegistrationNumber() {
        return "REG" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
    
    // 根据ID查找报名
    public Optional<Registration> findById(Long id) {
        return registrationRepository.findById(id);
    }
    
    // 根据报名编号查找
    public Optional<Registration> findByRegistrationNumber(String registrationNumber) {
        return registrationRepository.findByRegistrationNumber(registrationNumber);
    }
    
    // 获取所有报名（分页）
    public Page<Registration> getAllRegistrations(Pageable pageable) {
        return registrationRepository.findAll(pageable);
    }
    
    // 根据竞赛获取报名
    public List<Registration> getRegistrationsByCompetition(Long competitionId) {
        return registrationRepository.findByCompetitionId(competitionId);
    }

    // 根据竞赛获取报名（分页）
    public Page<Registration> getRegistrationsByCompetition(Long competitionId, Pageable pageable) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        return registrationRepository.findByCompetitionIdOrderByCreatedAtDesc(competitionId, pageable);
    }
    
    // 根据团队获取报名
    public List<Registration> getRegistrationsByTeam(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            return List.of();
        }
        Optional<Registration> registration = registrationRepository.findByTeamId(teamId);
        if (registration.isPresent()) {
            return List.of(registration.get());
        } else {
            return List.of();
        }
    }
    
    // 根据团队获取报名（分页）
    public Page<Registration> getRegistrationsByTeam(Long teamId, Pageable pageable) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            return Page.empty(pageable);
        }
        Optional<Registration> registration = registrationRepository.findByTeamId(teamId);
        if (registration.isPresent()) {
            return new PageImpl<>(List.of(registration.get()), pageable, 1);
        } else {
            return Page.empty(pageable);
        }
    }
    
    // 根据用户获取报名
    public List<Registration> getRegistrationsByUser(Long userId) {
        return registrationRepository.findBySubmittedById(userId);
    }

    // 根据用户获取报名（分页）
    public Page<Registration> getRegistrationsByUser(Long userId, Pageable pageable) {
        return registrationRepository.findBySubmittedByIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    // 根据状态获取报名
    public List<Registration> getRegistrationsByStatus(Registration.RegistrationStatus status) {
        return registrationRepository.findByStatus(status);
    }

    // 根据状态获取报名（分页）
    public Page<Registration> getRegistrationsByStatus(Registration.RegistrationStatus status, Pageable pageable) {
        return registrationRepository.findByStatus(status, pageable);
    }
    
    // 根据支付状态获取报名
    public List<Registration> getRegistrationsByPaymentStatus(Registration.PaymentStatus paymentStatus) {
        return registrationRepository.findByPaymentStatus(paymentStatus);
    }

    // 根据支付状态获取报名（分页）
    public Page<Registration> getRegistrationsByPaymentStatus(Registration.PaymentStatus paymentStatus, Pageable pageable) {
        return registrationRepository.findByPaymentStatus(paymentStatus, pageable);
    }
    

    
    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }
    
    public List<Registration> getUserRegistrationsInCompetition(Long userId, Long competitionId) {
        return registrationRepository.findBySubmittedById(userId);
    }
    
    public boolean existsByTeamAndCompetition(Long teamId, Long competitionId) {
        Optional<Registration> registration = registrationRepository.findByTeamId(teamId);
        return registration.isPresent();
    }
    
    // 获取待审核的报名
    public List<Registration> getPendingRegistrations() {
        return registrationRepository.findByStatusOrderByCreatedAtAsc(Registration.RegistrationStatus.PENDING);
    }
    
    // 获取待支付的报名
    public List<Registration> getPendingPayments() {
        return registrationRepository.findByPaymentStatus(Registration.PaymentStatus.UNPAID);
    }
    
    // 审核报名
    public Registration approveRegistration(Long registrationId, Boolean approved, Long reviewedBy, String remarks) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("报名记录不存在"));
        
        if (approved) {
            registration.setStatus(Registration.RegistrationStatus.APPROVED);
        } else {
            registration.setStatus(Registration.RegistrationStatus.REJECTED);
        }
        
        // 验证审核人存在
        userRepository.findById(reviewedBy)
                .orElseThrow(() -> new RuntimeException("审核人不存在"));
        registration.setReviewedBy(reviewedBy);
        registration.setReviewedAt(LocalDateTime.now());
        registration.setRemarks(remarks);
        
        return registrationRepository.save(registration);
    }
    
    // 重载方法，只接收registrationId和reviewedBy参数
    public Registration approveRegistration(Long registrationId, Long reviewedBy) {
        return approveRegistration(registrationId, true, reviewedBy, null);
    }
    
    // 拒绝报名（重载方法）
    public Registration rejectRegistration(Long registrationId, Long reviewerId) {
        return rejectRegistration(registrationId, reviewerId, null);
    }
    
    // 拒绝报名
    public Registration rejectRegistration(Long registrationId, Long reviewerId, String reason) {
        Optional<Registration> registrationOpt = registrationRepository.findById(registrationId);
        if (registrationOpt.isEmpty()) {
            throw new RuntimeException("报名记录不存在");
        }
        
        Registration registration = registrationOpt.get();
        if (registration.getStatus() != Registration.RegistrationStatus.PENDING) {
            throw new RuntimeException("报名不在待审核状态");
        }
        
        registration.setStatus(Registration.RegistrationStatus.REJECTED);
        registration.setReviewedBy(reviewerId);
        registration.setReviewedAt(LocalDateTime.now());
        registration.setRemarks(reason);
        
        return registrationRepository.save(registration);
    }
    
    // 确认支付
    public Registration confirmPayment(Long registrationId, String paymentMethod, String transactionId) {
        Optional<Registration> registrationOpt = registrationRepository.findById(registrationId);
        if (registrationOpt.isEmpty()) {
            throw new RuntimeException("报名记录不存在");
        }
        
        Registration registration = registrationOpt.get();
        if (registration.getPaymentStatus() != Registration.PaymentStatus.UNPAID) {
            throw new RuntimeException("支付状态不正确");
        }
        
        registration.setPaymentStatus(Registration.PaymentStatus.PAID);
        registration.setPaymentTime(LocalDateTime.now());
        
        // 如果报名状态还是待审核，自动确认
        if (registration.getStatus() == Registration.RegistrationStatus.PENDING) {
            registration.setStatus(Registration.RegistrationStatus.APPROVED);
        }
        
        return registrationRepository.save(registration);
    }
    
    // 取消报名
    public Registration cancelRegistration(Long registrationId, Long userId, String reason) {
        Optional<Registration> registrationOpt = registrationRepository.findById(registrationId);
        if (registrationOpt.isEmpty()) {
            throw new RuntimeException("报名记录不存在");
        }
        
        Registration registration = registrationOpt.get();
        
        // 检查权限
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        if (user.getRole() != User.UserRole.ADMIN && 
            !registration.getSubmittedBy().equals(userId)) {
            throw new RuntimeException("没有权限取消此报名");
        }
        
        // 检查是否可以取消
        Competition competition = registration.getCompetition();
        if (competition.getStatus() == Competition.CompetitionStatus.IN_PROGRESS || 
            competition.getStatus() == Competition.CompetitionStatus.COMPLETED) {
            throw new RuntimeException("竞赛已开始或已结束，不能取消报名");
        }
        
        registration.setStatus(Registration.RegistrationStatus.CANCELLED);
        registration.setRemarks(reason);
        
        // 如果已支付，标记为需要退款
        if (registration.getPaymentStatus() == Registration.PaymentStatus.PAID) {
            registration.setPaymentStatus(Registration.PaymentStatus.REFUNDED);
        }
        
        return registrationRepository.save(registration);
    }
    
    // 处理退款
    public Registration processRefund(Long registrationId, Long processedBy, String refundMethod, String refundTransactionId) {
        Optional<Registration> registrationOpt = registrationRepository.findById(registrationId);
        if (registrationOpt.isEmpty()) {
            throw new RuntimeException("报名记录不存在");
        }
        
        Registration registration = registrationOpt.get();
        if (registration.getPaymentStatus() != Registration.PaymentStatus.PAID) {
            throw new RuntimeException("不需要退款或退款状态不正确");
        }
        
        registration.setPaymentStatus(Registration.PaymentStatus.REFUNDED);
        
        // 设置审核人ID
        registration.setReviewedBy(processedBy);
        
        // 退款处理逻辑
         // 注意：Registration实体中没有退款相关字段，这里只更新支付状态
         registration.setReviewedAt(LocalDateTime.now());
        
        return registrationRepository.save(registration);
    }
    
    // 检查用户是否已报名竞赛
    public boolean isUserRegistered(Long userId, Long competitionId) {
        return registrationRepository.existsByCompetitionAndUser(competitionId, userId);
    }
    
    // 检查团队是否已报名竞赛
    public boolean isTeamRegistered(Long teamId, Long competitionId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        
        if (teamOpt.isEmpty() || competitionOpt.isEmpty()) {
            return false;
        }
        
        return registrationRepository.findByTeamId(teamId).isPresent();
    }
    
    // 获取竞赛报名统计
    public long getRegistrationCount(Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        return registrationRepository.countByCompetitionIdAndStatus(
            competitionId, Registration.RegistrationStatus.APPROVED);
    }
    
    // 获取竞赛收入统计
    public BigDecimal getCompetitionRevenue(Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        // 计算收入
        List<Registration> paidRegistrations = registrationRepository.findByCompetitionIdAndPaymentStatus(competitionId, Registration.PaymentStatus.PAID);
        return paidRegistrations.stream()
            .map(Registration::getPaymentAmount)
            .map(amount -> BigDecimal.valueOf(amount))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // 获取时间范围内的报名
    public List<Registration> getRegistrationsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return registrationRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    // 获取用户在特定竞赛中的报名
    public Optional<Registration> getUserRegistrationInCompetition(Long userId, Long competitionId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (userOpt.isEmpty() || competitionOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        Competition competition = competitionOpt.get();
        return registrationRepository.findByCompetitionAndUser(competition.getId(), user.getId());
    }

    // 获取待审核的报名（分页）
    public Page<Registration> getPendingApprovalRegistrations(Pageable pageable) {
        return registrationRepository.findByStatus(Registration.RegistrationStatus.PENDING, pageable);
    }
    
    // 获取教师创建的竞赛的待审核报名
    public List<Registration> getPendingRegistrationsByTeacher(Long teacherId) {
        // 首先获取教师创建的所有竞赛
        Page<Competition> competitionsPage = competitionRepository.findByCreatorId(teacherId, Pageable.unpaged());
        List<Competition> teacherCompetitions = competitionsPage.getContent();
        
        List<Registration> pendingRegistrations = new ArrayList<>();
        for (Competition competition : teacherCompetitions) {
            List<Registration> competitionPendingRegistrations = registrationRepository
                .findByCompetitionAndStatus(competition, Registration.RegistrationStatus.PENDING);
            pendingRegistrations.addAll(competitionPendingRegistrations);
        }
        
        return pendingRegistrations;
    }
    
    // 获取教师创建的竞赛的待审核报名（分页版本）
    public Page<Registration> getPendingRegistrationsByTeacher(Long teacherId, Pageable pageable) {
        // 首先获取教师创建的所有竞赛
        List<Competition> teacherCompetitions = competitionRepository.findByCreatorOrderByCreatedAtDesc(teacherId, Pageable.unpaged()).getContent();
        
        if (teacherCompetitions.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // 获取第一个竞赛的待审核报名作为示例，实际应该合并所有竞赛的结果
        Competition firstCompetition = teacherCompetitions.get(0);
        return registrationRepository.findByCompetitionAndStatus(firstCompetition, Registration.RegistrationStatus.PENDING, pageable);
    }
    
    // 获取特定竞赛的待审核报名（分页）
    public Page<Registration> getPendingRegistrationsByCompetition(Long competitionId, Pageable pageable) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Competition competition = competitionOpt.get();
        return registrationRepository.findByCompetitionAndStatus(competition, Registration.RegistrationStatus.PENDING, pageable);
    }
    
    // 获取报名统计信息
    public List<Object[]> getRegistrationStatsByStatus() {
        return registrationRepository.countRegistrationsByStatus();
    }
    
    // 统计教师创建的竞赛的待审核报名数量
    public Long countPendingByTeacher(Long teacherId) {
        // 首先获取教师创建的所有竞赛
        Page<Competition> competitionsPage = competitionRepository.findByCreatorId(teacherId, Pageable.unpaged());
        List<Competition> teacherCompetitions = competitionsPage.getContent();
        
        long totalPending = 0;
        for (Competition competition : teacherCompetitions) {
            long pendingCount = registrationRepository.countByCompetitionAndStatus(competition, Registration.RegistrationStatus.PENDING);
            totalPending += pendingCount;
        }
        
        return totalPending;
    }
    
    public List<Object[]> getRegistrationStatsByPaymentStatus() {
        return registrationRepository.countRegistrationsByPaymentStatus();
    }
    
    public List<Object[]> getRegistrationStatsByCompetition() {
        // 统计各竞赛的报名数量
        return List.of();
    }
    
    // 批量确认报名
    public List<Registration> batchApproveRegistrations(List<Long> registrationIds, Long reviewerId) {
        List<Registration> approvedRegistrations = new java.util.ArrayList<>();
        
        for (Long registrationId : registrationIds) {
            try {
                Registration approved = approveRegistration(registrationId, reviewerId);
                approvedRegistrations.add(approved);
            } catch (RuntimeException e) {
                // 记录错误但继续处理其他报名
                System.err.println("Failed to approve registration " + registrationId + ": " + e.getMessage());
            }
        }
        
        return approvedRegistrations;
    }
    
    // 批量拒绝报名
    public List<Registration> batchRejectRegistrations(List<Long> registrationIds, Long reviewerId, String reason) {
        List<Registration> rejectedRegistrations = new java.util.ArrayList<>();
        
        for (Long registrationId : registrationIds) {
            try {
                Registration rejected = rejectRegistration(registrationId, reviewerId, reason);
                rejectedRegistrations.add(rejected);
            } catch (RuntimeException e) {
                // 记录错误但继续处理其他报名
                System.err.println("Failed to reject registration " + registrationId + ": " + e.getMessage());
            }
        }
        
        return rejectedRegistrations;
    }
    
    // 更新报名备注
    public Registration updateRegistrationRemarks(Long registrationId, String remarks, Long userId) {
        Optional<Registration> registrationOpt = registrationRepository.findById(registrationId);
        if (registrationOpt.isEmpty()) {
            throw new RuntimeException("报名记录不存在");
        }
        
        Registration registration = registrationOpt.get();
        
        // 检查权限
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        if (user.getRole() != User.UserRole.ADMIN && 
            user.getRole() != User.UserRole.TEACHER && 
            !registration.getSubmittedBy().equals(userId)) {
            throw new RuntimeException("没有权限修改备注");
        }
        
        registration.setRemarks(remarks);
        return registrationRepository.save(registration);
    }
    
    // 批量审核报名
    public List<Registration> batchReviewRegistrations(List<Long> registrationIds, Boolean approved, Long reviewerId, String remarks) {
        List<Registration> reviewedRegistrations = new ArrayList<>();
        
        for (Long registrationId : registrationIds) {
            try {
                Registration reviewed;
                if (approved) {
                    reviewed = approveRegistration(registrationId, reviewerId);
                } else {
                    reviewed = rejectRegistration(registrationId, reviewerId, remarks);
                }
                reviewedRegistrations.add(reviewed);
            } catch (RuntimeException e) {
                // 记录错误但继续处理其他报名
                System.err.println("Failed to review registration " + registrationId + ": " + e.getMessage());
            }
        }
        
        return reviewedRegistrations;
    }
    
    // 审核单个报名
    public Registration reviewRegistration(Long registrationId, Boolean approved, Long reviewerId, String remarks) {
        if (approved) {
            return approveRegistration(registrationId, reviewerId);
        } else {
            return rejectRegistration(registrationId, reviewerId, remarks);
        }
    }
    
    // 根据竞赛和状态获取报名（分页）
    public Page<Registration> getRegistrationsByCompetitionAndStatus(Long competitionId, Registration.RegistrationStatus status, Pageable pageable) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Competition competition = competitionOpt.get();
        return registrationRepository.findByCompetitionAndStatus(competition, status, pageable);
    }
    
    // 保存报名记录
    public Registration save(Registration registration) {
        return registrationRepository.save(registration);
    }
}