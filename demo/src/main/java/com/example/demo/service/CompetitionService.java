package com.example.demo.service;

import com.example.demo.entity.Competition;
import com.example.demo.entity.CompetitionAuditLog;
import com.example.demo.entity.User;
import com.example.demo.repository.CompetitionRepository;
import com.example.demo.repository.CompetitionAuditLogRepository;
import com.example.demo.repository.CompetitionQuestionRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.ExamPaperRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import static com.example.demo.entity.Question.QuestionCategory.*;
import static com.example.demo.entity.Question.QuestionType.PROGRAMMING;

@Service
@Transactional
public class CompetitionService {
    
    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompetitionAuditLogRepository auditLogRepository;
    
    @Autowired
    private CompetitionQuestionRepository competitionQuestionRepository;
    
    @Autowired
    private RegistrationRepository registrationRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private ExamPaperRepository examPaperRepository;

    private String normalizeCategoryValue(String category) {
        if (category == null || category.trim().isEmpty()) {
            return null;
        }
        return category.trim().toUpperCase();
    }
    
    // 创建竞赛
    public Competition createCompetition(Competition competition, Long creatorId) {
        // 验证创建者
        Optional<User> creatorOpt = userRepository.findById(creatorId);
        if (creatorOpt.isEmpty()) {
            throw new RuntimeException("创建者不存在");
        }
        
        User creator = creatorOpt.get();
        if (creator.getRole() != User.UserRole.TEACHER && creator.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("只有教师和管理员可以创建竞赛");
        }
        
        // 生成竞赛编号
        competition.setCompetitionNumber(generateCompetitionNumber());
        competition.setCreator(creator);
        String normalizedCategory = normalizeCategoryValue(competition.getCategory());
        if (normalizedCategory == null) {
            throw new RuntimeException("竞赛分类不能为空");
        }
        competition.setCategory(normalizedCategory);
        
        // 设置初始状态
        if (creator.getRole() == User.UserRole.ADMIN) {
            competition.setStatus(Competition.CompetitionStatus.REGISTRATION_OPEN);
        } else {
            competition.setStatus(Competition.CompetitionStatus.DRAFT); // 教师创建需要审核
        }
        
        return competitionRepository.save(competition);
    }
    

    
    // 检查竞赛名称是否存在
    public boolean existsByName(String name) {
        return competitionRepository.existsByName(name);
    }
    
    // 批量更新竞赛状态
    public void batchUpdateStatus(List<Long> competitionIds, Competition.CompetitionStatus status) {
        List<Competition> competitions = competitionRepository.findAllById(competitionIds);
        competitions.forEach(competition -> {
            competition.setStatus(status);
            competition.setUpdatedAt(LocalDateTime.now());
        });
        competitionRepository.saveAll(competitions);
    }
    
    // 根据ID列表查找竞赛
    public List<Competition> findByIds(List<Long> competitionIds) {
        return competitionRepository.findAllById(competitionIds);
    }
    
    // 保存竞赛
    public Competition save(Competition competition) {
        System.out.println("=== CompetitionService.save() ===");
        System.out.println("保存前竞赛状态: " + competition.getStatus());
        System.out.println("竞赛ID: " + competition.getId());
        System.out.println("竞赛名称: " + competition.getName());
        
        Competition savedCompetition = competitionRepository.save(competition);
        
        System.out.println("保存后竞赛状态: " + savedCompetition.getStatus());
        System.out.println("保存后竞赛ID: " + savedCompetition.getId());
        System.out.println("=== CompetitionService.save() 完成 ===");
        
        return savedCompetition;
    }
    

    
    // 获取竞赛详情
    public Competition getCompetitionDetails(Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Competition competition = competitionOpt.get();
        // 增加浏览次数
        competition.setViewCount(competition.getViewCount() + 1);
        competitionRepository.save(competition);
        
        return competition;
    }
    

    
    // 生成竞赛编号
    private String generateCompetitionNumber() {
        return "COMP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
    
    // 根据ID查找竞赛
    public Optional<Competition> findById(Long id) {
        return competitionRepository.findById(id);
    }
    
    // 获取所有竞赛（分页）
    public Page<Competition> getAllCompetitions(Pageable pageable) {
        return competitionRepository.findAll(pageable);
    }
    
    // 根据状态获取竞赛（分页）
    public Page<Competition> getCompetitionsByStatus(Competition.CompetitionStatus status, Pageable pageable) {
        return competitionRepository.findByStatus(status, pageable);
    }
    
    // 根据分类获取竞赛
    public Page<Competition> getCompetitionsByCategory(String category, 
                                                     Competition.CompetitionStatus status, 
                                                     Pageable pageable) {
        String normalizedCategory = normalizeCategoryValue(category);
        return competitionRepository.findByCategoryAndStatus(normalizedCategory, status, pageable);
    }
    
    // 根据级别获取竞赛
    public Page<Competition> getCompetitionsByLevel(Competition.CompetitionLevel level, 
                                                   Competition.CompetitionStatus status, 
                                                   Pageable pageable) {
        return competitionRepository.findByLevelAndStatus(level, status, pageable);
    }
    
    // 搜索竞赛
    public Page<Competition> searchCompetitions(String keyword, Pageable pageable) {
        return competitionRepository.searchCompetitions(keyword, pageable);
    }
    
    // 综合筛选竞赛
    public Page<Competition> filterCompetitions(String keyword, 
                                              String category,
                                              Competition.CompetitionStatus status,
                                              LocalDateTime startDate,
                                              LocalDateTime endDate,
                                              Pageable pageable) {
        String normalizedCategory = normalizeCategoryValue(category);
        return competitionRepository.filterCompetitions(keyword, normalizedCategory, status, startDate, endDate, pageable);
    }
    
    // 获取正在报名的竞赛
    public List<Competition> getOpenForRegistration() {
        return competitionRepository.findOpenForRegistration(LocalDateTime.now());
    }
    
    // 获取即将开始的竞赛
    public List<Competition> getUpcomingCompetitions(int limit) {
        return competitionRepository.findUpcomingCompetitions(LocalDateTime.now(), Pageable.ofSize(limit));
    }
    
    // 获取正在进行的竞赛
    public List<Competition> getOngoingCompetitions() {
        return competitionRepository.findOngoingCompetitions(LocalDateTime.now());
    }
    
    // 获取已结束的竞赛
    public List<Competition> getCompletedCompetitions(int limit) {
        return competitionRepository.findCompletedCompetitions(LocalDateTime.now(), Pageable.ofSize(limit));
    }
    
    // 获取待审核的竞赛
    public List<Competition> getPendingCompetitions() {
        return competitionRepository.findByStatusOrderByCreatedAtDesc(Competition.CompetitionStatus.DRAFT);
    }
    
    // 审核竞赛
    public Competition approveCompetition(Long competitionId, Long reviewerId, String remarks) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Competition competition = competitionOpt.get();
        Competition.CompetitionStatus oldStatus = competition.getStatus();
        
        if (oldStatus != Competition.CompetitionStatus.DRAFT) {
            throw new RuntimeException("竞赛不在待审核状态");
        }
        
        // 检查时间设置是否合理
        LocalDateTime now = LocalDateTime.now();
        if (competition.getRegistrationStartTime().isBefore(now)) {
            throw new RuntimeException("报名开始时间不能早于当前时间");
        }
        
        // 获取审核员信息
        Optional<User> reviewerOpt = userRepository.findById(reviewerId);
        if (reviewerOpt.isEmpty()) {
            throw new RuntimeException("审核员不存在");
        }
        
        // 更新竞赛状态
        competition.setStatus(Competition.CompetitionStatus.REGISTRATION_OPEN);
        Competition savedCompetition = competitionRepository.save(competition);
        
        // 记录审核日志
        CompetitionAuditLog auditLog = new CompetitionAuditLog(
            competition, 
            reviewerOpt.get(), 
            CompetitionAuditLog.AuditAction.APPROVE,
            oldStatus,
            Competition.CompetitionStatus.REGISTRATION_OPEN,
            remarks
        );
        auditLogRepository.save(auditLog);
        
        return savedCompetition;
    }
    
    // 拒绝竞赛
    public Competition rejectCompetition(Long competitionId, Long reviewerId, String reason) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Competition competition = competitionOpt.get();
        Competition.CompetitionStatus oldStatus = competition.getStatus();
        
        if (oldStatus != Competition.CompetitionStatus.DRAFT) {
            throw new RuntimeException("竞赛不在待审核状态");
        }
        
        // 获取审核员信息
        Optional<User> reviewerOpt = userRepository.findById(reviewerId);
        if (reviewerOpt.isEmpty()) {
            throw new RuntimeException("审核员不存在");
        }
        
        // 更新竞赛状态
        competition.setStatus(Competition.CompetitionStatus.CANCELLED);
        Competition savedCompetition = competitionRepository.save(competition);
        
        // 记录审核日志
        CompetitionAuditLog auditLog = new CompetitionAuditLog(
            competition, 
            reviewerOpt.get(), 
            CompetitionAuditLog.AuditAction.REJECT,
            oldStatus,
            Competition.CompetitionStatus.CANCELLED,
            reason
        );
        auditLogRepository.save(auditLog);
        
        return savedCompetition;
    }
    
    // 更新竞赛信息
    public Competition updateCompetition(Long competitionId, Competition updatedCompetition, Long userId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Competition competition = competitionOpt.get();
        
        // 检查权限
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        if (user.getRole() != User.UserRole.ADMIN && !competition.getCreator().getId().equals(userId)) {
            throw new RuntimeException("没有权限修改此竞赛");
        }
        
        // 更新允许修改的字段
        if (updatedCompetition.getName() != null) {
            competition.setName(updatedCompetition.getName());
        }
        if (updatedCompetition.getDescription() != null) {
            competition.setDescription(updatedCompetition.getDescription());
        }
        if (updatedCompetition.getCategory() != null) {
            String normalizedCategory = normalizeCategoryValue(updatedCompetition.getCategory());
            if (normalizedCategory == null) {
                throw new RuntimeException("竞赛分类不能为空");
            }
            competition.setCategory(normalizedCategory);
        }
        if (updatedCompetition.getLevel() != null) {
            competition.setLevel(updatedCompetition.getLevel());
        }
        if (updatedCompetition.getMaxTeams() != null) {
            competition.setMaxTeams(updatedCompetition.getMaxTeams());
        }
        if (updatedCompetition.getMaxTeamSize() != null) {
            competition.setMaxTeamSize(updatedCompetition.getMaxTeamSize());
        }
        if (updatedCompetition.getRegistrationFee() != null) {
            competition.setRegistrationFee(updatedCompetition.getRegistrationFee());
        }
        if (updatedCompetition.getRegistrationStartTime() != null) {
            competition.setRegistrationStartTime(updatedCompetition.getRegistrationStartTime());
        }
        if (updatedCompetition.getRegistrationEndTime() != null) {
            competition.setRegistrationEndTime(updatedCompetition.getRegistrationEndTime());
        }
        if (updatedCompetition.getCompetitionStartTime() != null) {
            competition.setCompetitionStartTime(updatedCompetition.getCompetitionStartTime());
        }
        if (updatedCompetition.getCompetitionEndTime() != null) {
            competition.setCompetitionEndTime(updatedCompetition.getCompetitionEndTime());
        }
        if (updatedCompetition.getRules() != null) {
            competition.setRules(updatedCompetition.getRules());
        }
        if (updatedCompetition.getPrizeInfo() != null) {
            competition.setPrizeInfo(updatedCompetition.getPrizeInfo());
        }
        if (updatedCompetition.getLocation() != null) {
            competition.setLocation(updatedCompetition.getLocation());
        }
        if (updatedCompetition.getOrganizer() != null) {
            competition.setOrganizer(updatedCompetition.getOrganizer());
        }
        if (updatedCompetition.getContactInfo() != null) {
            competition.setContactInfo(updatedCompetition.getContactInfo());
        }
        if (updatedCompetition.getMinTeamSize() != null) {
            competition.setMinTeamSize(updatedCompetition.getMinTeamSize());
        }
        if (updatedCompetition.getStatus() != null) {
            competition.setStatus(updatedCompetition.getStatus());
        }
        
        return competitionRepository.save(competition);
    }
    
    // 删除竞赛
    public void deleteCompetition(Long competitionId, Long userId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Competition competition = competitionOpt.get();
        
        // 检查权限
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        if (user.getRole() != User.UserRole.ADMIN && !competition.getCreator().getId().equals(userId)) {
            throw new RuntimeException("没有权限删除此竞赛");
        }
        
        // 检查是否可以删除
        if (competition.getStatus() == Competition.CompetitionStatus.IN_PROGRESS || 
            competition.getStatus() == Competition.CompetitionStatus.ONGOING ||
            competition.getStatus() == Competition.CompetitionStatus.COMPLETED) {
            throw new RuntimeException("正在进行或已完成的竞赛不能删除");
        }
        
        // 删除所有关联数据（按照外键依赖顺序）
        // 1. 删除成绩记录
        gradeRepository.deleteByCompetitionId(competitionId);
        
        // 2. 删除考试试卷
        examPaperRepository.deleteByCompetitionId(competitionId);
        
        // 3. 删除团队（会级联删除团队成员）
        teamRepository.deleteByCompetitionId(competitionId);
        
        // 4. 删除报名记录
        registrationRepository.deleteByCompetitionId(competitionId);
        
        // 5. 删除竞赛题目关联
        competitionQuestionRepository.deleteByCompetitionId(competitionId);
        
        // 6. 删除审核日志
        auditLogRepository.deleteByCompetitionId(competitionId);
        
        // 7. 最后删除竞赛本身
        competitionRepository.deleteById(competitionId);
    }
    

    
    // 获取用户可以报名的竞赛
    public List<Competition> getAvailableCompetitionsForUser(Long userId) {
        return competitionRepository.findAvailableForUser(userId, LocalDateTime.now());
    }
    
    // 获取热门竞赛
    public List<Competition> getPopularCompetitions(int limit) {
        return competitionRepository.findPopularCompetitions(Pageable.ofSize(limit));
    }
    
    // 获取竞赛统计信息
    public List<Object[]> getCompetitionStatsByCategory() {
        return competitionRepository.countCompetitionsByCategory();
    }
    
    public List<Object[]> getCompetitionStatsByStatus() {
        return competitionRepository.countCompetitionsByStatus();
    }
    
    // 获取竞赛统计信息（合并方法）
    public Map<String, Object> getCompetitionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取总竞赛数
        long totalCompetitions = competitionRepository.count();
        stats.put("totalCompetitions", totalCompetitions);
        
        // 获取各状态的竞赛数量
        long pendingApproval = competitionRepository.countByStatus(Competition.CompetitionStatus.DRAFT);
        long inProgress = competitionRepository.countByStatus(Competition.CompetitionStatus.REGISTRATION_OPEN) +
                         competitionRepository.countByStatus(Competition.CompetitionStatus.ONGOING);
        long completed = competitionRepository.countByStatus(Competition.CompetitionStatus.COMPLETED);
        long published = competitionRepository.countByStatus(Competition.CompetitionStatus.PUBLISHED);
        long cancelled = competitionRepository.countByStatus(Competition.CompetitionStatus.CANCELLED);
        
        stats.put("pendingApproval", pendingApproval);
        stats.put("inProgress", inProgress);
        stats.put("completed", completed);
        stats.put("published", published);
        stats.put("draft", pendingApproval); // draft 和 pendingApproval 是同一个状态
        stats.put("cancelled", cancelled);
        
        // 保留原有的统计信息
        stats.put("categoryStats", getCompetitionStatsByCategory());
        stats.put("statusStats", getCompetitionStatsByStatus());
        
        // 添加空的统计信息以保持兼容性
        stats.put("levelStats", new HashMap<>());
        stats.put("monthlyCreated", new ArrayList<>());
        stats.put("topCreators", new ArrayList<>());
        
        return stats;
    }
    
    // 获取待审核竞赛（分页）
    public Page<Competition> getPendingApprovalCompetitions(Pageable pageable) {
        return competitionRepository.findByStatus(Competition.CompetitionStatus.DRAFT, pageable);
    }
    
    // 获取竞赛审核日志
    public List<CompetitionAuditLog> getCompetitionAuditLogs(Long competitionId) {
        return auditLogRepository.findByCompetitionIdOrderByCreatedAtDesc(competitionId);
    }
    
    // 获取竞赛审核日志（分页）
    public Page<CompetitionAuditLog> getCompetitionAuditLogs(Long competitionId, Pageable pageable) {
        return auditLogRepository.findByCompetitionIdOrderByCreatedAtDesc(competitionId, pageable);
    }
    
    // 获取所有审核日志（分页）
    public Page<CompetitionAuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    

    
    // 获取即将开始的竞赛
    public Page<Competition> getUpcomingCompetitions(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        List<Competition> competitions = competitionRepository.findUpcomingCompetitions(now, pageable);
        return new PageImpl<>(competitions, pageable, competitions.size());
    }
    

    
    // 获取用户创建的竞赛
    public Page<Competition> getCompetitionsByCreator(Long creatorId, Pageable pageable) {
        Optional<User> creatorOpt = userRepository.findById(creatorId);
        if (creatorOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        return competitionRepository.findByCreatorOrderByCreatedAtDesc(creatorOpt.get(), pageable);
    }
    
    // 根据时间范围获取竞赛
    public Page<Competition> getCompetitionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return competitionRepository.findByCompetitionStartTimeBetween(startDate, endDate, pageable);
    }
    
    // 统计教师创建的竞赛数量
    public long countByCreator(Long creatorId) {
        Optional<User> creatorOpt = userRepository.findById(creatorId);
        if (creatorOpt.isEmpty()) {
            return 0;
        }
        return competitionRepository.countByCreator(creatorOpt.get());
    }
    
    // 获取教师最近创建的竞赛
    public List<Competition> findRecentByCreator(Long creatorId, int limit) {
        Optional<User> creatorOpt = userRepository.findById(creatorId);
        if (creatorOpt.isEmpty()) {
            return new ArrayList<>();
        }
        return competitionRepository.findByCreatorOrderByCreatedAtDesc(creatorOpt.get(), Pageable.ofSize(limit)).getContent();
    }
    
    // 自动更新竞赛状态
    public void updateCompetitionStatusAutomatically() {
        updateCompetitionStatuses();
    }
    
    public void updateCompetitionStatuses() {
        LocalDateTime now = LocalDateTime.now();
        
        // 更新报名开始的竞赛
        List<Competition> toOpenRegistration = competitionRepository.findAll().stream()
            .filter(c -> c.getStatus() == Competition.CompetitionStatus.PUBLISHED && 
                        c.getRegistrationStartTime().isBefore(now) && 
                        c.getRegistrationEndTime().isAfter(now))
            .toList();
        
        for (Competition competition : toOpenRegistration) {
            competition.setStatus(Competition.CompetitionStatus.REGISTRATION_OPEN);
            competitionRepository.save(competition);
        }
        
        // 更新报名结束的竞赛
        List<Competition> toCloseRegistration = competitionRepository.findAll().stream()
            .filter(c -> c.getStatus() == Competition.CompetitionStatus.REGISTRATION_OPEN && 
                        c.getRegistrationEndTime().isBefore(now))
            .toList();
        
        for (Competition competition : toCloseRegistration) {
            competition.setStatus(Competition.CompetitionStatus.REGISTRATION_CLOSED);
            competitionRepository.save(competition);
        }
        
        // 更新开始的竞赛
        List<Competition> toStart = competitionRepository.findAll().stream()
            .filter(c -> c.getStatus() == Competition.CompetitionStatus.REGISTRATION_CLOSED && 
                        c.getCompetitionStartTime().isBefore(now) && 
                        c.getCompetitionEndTime().isAfter(now))
            .toList();
        
        for (Competition competition : toStart) {
            competition.setStatus(Competition.CompetitionStatus.IN_PROGRESS);
            competitionRepository.save(competition);
        }
        
        // 更新结束的竞赛
        List<Competition> toComplete = competitionRepository.findAll().stream()
            .filter(c -> c.getStatus() == Competition.CompetitionStatus.IN_PROGRESS && 
                        c.getCompetitionEndTime().isBefore(now))
            .toList();
        
        for (Competition competition : toComplete) {
            competition.setStatus(Competition.CompetitionStatus.COMPLETED);
            competitionRepository.save(competition);
        }
    }
    
    // 导出相关方法
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 获取用于导出的竞赛数据
    public List<Competition> getCompetitionsForExport(String keyword, 
                                                     String category,
                                                     Competition.CompetitionStatus status,
                                                     LocalDateTime startDate,
                                                     LocalDateTime endDate) {
        // 使用现有的筛选方法，但不分页
        Page<Competition> page = filterCompetitions(keyword, normalizeCategoryValue(category), status, startDate, endDate, 
                                                   Pageable.unpaged());
        return page.getContent();
    }
    
    // 导出竞赛数据到Excel
    public byte[] exportCompetitionsToExcel(List<Competition> competitions) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("竞赛数据");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            createCompetitionHeaderCells(headerRow);

            // 创建数据行
            int rowNum = 1;
            for (Competition competition : competitions) {
                Row row = sheet.createRow(rowNum++);
                fillCompetitionData(row, competition);
            }

            // 自动调整列宽
            for (int i = 0; i < 13; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小列宽
                if (sheet.getColumnWidth(i) < 2000) {
                    sheet.setColumnWidth(i, 2000);
                }
            }

            // 将工作簿写入字节数组
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
                return outputStream.toByteArray();
            }
        }
    }
    
    private void createCompetitionHeaderCells(Row headerRow) {
        String[] headers = {
            "竞赛编号", "竞赛名称", "分类", "级别", "状态", 
            "主办方", "创建者", "报名数", "浏览数", "报名费用",
            "报名开始时间", "报名结束时间", "创建时间"
        };

        CellStyle headerStyle = headerRow.getSheet().getWorkbook().createCellStyle();
        Font headerFont = headerRow.getSheet().getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }
    
    private void fillCompetitionData(Row row, Competition competition) {
        CellStyle dataStyle = row.getSheet().getWorkbook().createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);

        // 竞赛编号
        Cell cell0 = row.createCell(0);
        cell0.setCellValue(competition.getCompetitionNumber() != null ? competition.getCompetitionNumber() : "");
        cell0.setCellStyle(dataStyle);

        // 竞赛名称
        Cell cell1 = row.createCell(1);
        cell1.setCellValue(competition.getName() != null ? competition.getName() : "");
        cell1.setCellStyle(dataStyle);

        // 分类
        Cell cell2 = row.createCell(2);
        String categoryText = "";
        if (competition.getCategory() != null) {
            String cat = competition.getCategory().toUpperCase();
            switch (cat) {
                case "PROGRAMMING":
                    categoryText = "程序设计";
                    break;
                case "MATHEMATICS":
                    categoryText = "数学竞赛";
                    break;
                case "PHYSICS":
                    categoryText = "物理竞赛";
                    break;
                case "CHEMISTRY":
                    categoryText = "化学竞赛";
                    break;
                case "BIOLOGY":
                    categoryText = "生物竞赛";
                    break;
                case "ENGLISH":
                    categoryText = "英语竞赛";
                    break;
                case "DESIGN":
                    categoryText = "设计竞赛";
                    break;
                case "INNOVATION":
                    categoryText = "创新创业";
                    break;
                case "OTHER":
                    categoryText = "其他";
                    break;
                default:
                    categoryText = competition.getCategory();
            }
        }
        cell2.setCellValue(categoryText);
        cell2.setCellStyle(dataStyle);

        // 级别
        Cell cell3 = row.createCell(3);
        String levelText = "";
        if (competition.getLevel() != null) {
            switch (competition.getLevel()) {
                case SCHOOL:
                    levelText = "校级";
                    break;
                case CITY:
                    levelText = "市级";
                    break;
                case PROVINCE:
                    levelText = "省级";
                    break;
                case NATIONAL:
                    levelText = "国家级";
                    break;
                case INTERNATIONAL:
                    levelText = "国际级";
                    break;
                default:
                    levelText = competition.getLevel().toString();
            }
        }
        cell3.setCellValue(levelText);
        cell3.setCellStyle(dataStyle);

        // 状态
        Cell cell4 = row.createCell(4);
        String statusText = "";
        if (competition.getStatus() != null) {
            switch (competition.getStatus()) {
                case DRAFT:
                    statusText = "草稿";
                    break;
                case PUBLISHED:
                    statusText = "已发布";
                    break;
                case REGISTRATION_OPEN:
                    statusText = "报名中";
                    break;
                case REGISTRATION_CLOSED:
                    statusText = "报名结束";
                    break;
                case IN_PROGRESS:
                case ONGOING:
                    statusText = "进行中";
                    break;
                case COMPLETED:
                    statusText = "已结束";
                    break;
                case CANCELLED:
                    statusText = "已取消";
                    break;
                case PENDING_APPROVAL:
                    statusText = "待审核";
                    break;
                default:
                    statusText = competition.getStatus().toString();
            }
        }
        cell4.setCellValue(statusText);
        cell4.setCellStyle(dataStyle);

        // 主办方
        Cell cell5 = row.createCell(5);
        cell5.setCellValue(competition.getOrganizer() != null ? competition.getOrganizer() : "");
        cell5.setCellStyle(dataStyle);

        // 创建者
        Cell cell6 = row.createCell(6);
        String creatorName = "";
        if (competition.getCreator() != null) {
            creatorName = competition.getCreator().getRealName() != null ? 
                         competition.getCreator().getRealName() : 
                         competition.getCreator().getUsername();
        }
        cell6.setCellValue(creatorName);
        cell6.setCellStyle(dataStyle);

        // 报名数
        Cell cell7 = row.createCell(7);
        cell7.setCellValue(competition.getRegistrationCount() != null ? competition.getRegistrationCount() : 0);
        cell7.setCellStyle(dataStyle);

        // 浏览数
        Cell cell8 = row.createCell(8);
        cell8.setCellValue(competition.getViewCount() != null ? competition.getViewCount() : 0);
        cell8.setCellStyle(dataStyle);

        // 报名费用
        Cell cell9 = row.createCell(9);
        cell9.setCellValue(competition.getRegistrationFee() != null ? competition.getRegistrationFee() : 0.0);
        cell9.setCellStyle(dataStyle);

        // 报名开始时间
        Cell cell10 = row.createCell(10);
        cell10.setCellValue(competition.getRegistrationStartTime() != null ? 
                           competition.getRegistrationStartTime().format(DATE_FORMATTER) : "");
        cell10.setCellStyle(dataStyle);

        // 报名结束时间
        Cell cell11 = row.createCell(11);
        cell11.setCellValue(competition.getRegistrationEndTime() != null ? 
                           competition.getRegistrationEndTime().format(DATE_FORMATTER) : "");
        cell11.setCellStyle(dataStyle);

        // 创建时间
        Cell cell12 = row.createCell(12);
        cell12.setCellValue(competition.getCreatedAt() != null ? 
                           competition.getCreatedAt().format(DATE_FORMATTER) : "");
        cell12.setCellStyle(dataStyle);
    }
}
