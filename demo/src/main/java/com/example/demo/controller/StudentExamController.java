package com.example.demo.controller;

import com.example.demo.entity.Competition;
import com.example.demo.entity.Registration;
import com.example.demo.entity.TeamMember;
import com.example.demo.entity.User;
import com.example.demo.repository.CompetitionRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.StudentExamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生答题Controller
 */
@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentExamController {

    private static final Logger logger = LoggerFactory.getLogger(StudentExamController.class);

    @Autowired
    private StudentExamService studentExamService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    /**
     * 获取学生的竞赛列表（已报名的）
     */
    @GetMapping("/competitions")
    public ResponseEntity<Map<String, Object>> getStudentCompetitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "false") boolean onlyRegistered,
            Authentication authentication) {

        logger.info("获取学生竞赛列表: page={}, size={}, onlyRegistered={}", page, size, onlyRegistered);

        try {
            User currentUser = getCurrentUser(authentication);

            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            if (onlyRegistered) {
                // 方案1: 获取用户直接提交的报名记录
                List<Registration> directRegistrations = registrationRepository
                        .findBySubmittedById(currentUser.getId());
                
                // 方案2: 获取用户作为团队成员的报名记录
                List<TeamMember> userTeamMemberships = teamMemberRepository
                        .findActiveTeamsByUser(currentUser.getId());
                
                logger.info("用户 {} 直接提交的报名数: {}", currentUser.getId(), directRegistrations.size());
                logger.info("用户 {} 的团队成员身份数: {}", currentUser.getId(), userTeamMemberships.size());

                // 使用 Map 来去重（避免同一个竞赛多次添加）
                Map<Long, Map<String, Object>> competitionMap = new HashMap<>();
                
                // 处理直接提交的报名
                for (Registration reg : directRegistrations) {
                    if (reg.getStatus() == Registration.RegistrationStatus.APPROVED || 
                        reg.getStatus() == Registration.RegistrationStatus.PENDING) {
                        
                        logger.info("直接报名 - 报名ID: {}, 竞赛ID: {}, 状态: {}", 
                            reg.getId(), 
                            reg.getCompetition().getId(), 
                            reg.getStatus());
                        
                        addCompetitionToMap(competitionMap, reg);
                    }
                }
                
                // 处理团队成员身份的报名
                for (TeamMember membership : userTeamMemberships) {
                    Long teamId = membership.getTeam().getId();
                    
                    // 查找该团队的报名记录
                    registrationRepository.findByTeamId(teamId).ifPresent(reg -> {
                        if (reg.getStatus() == Registration.RegistrationStatus.APPROVED || 
                            reg.getStatus() == Registration.RegistrationStatus.PENDING) {
                            
                            logger.info("团队报名 - 团队ID: {}, 报名ID: {}, 竞赛ID: {}, 状态: {}", 
                                teamId,
                                reg.getId(), 
                                reg.getCompetition().getId(), 
                                reg.getStatus());
                            
                            addCompetitionToMap(competitionMap, reg);
                        }
                    });
                }

                List<Map<String, Object>> competitions = new ArrayList<>(competitionMap.values());
                logger.info("用户 {} 的竞赛总数（去重后）: {}", currentUser.getId(), competitions.size());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", Map.of(
                        "content", competitions,
                        "totalElements", competitions.size(),
                        "totalPages", 1,
                        "currentPage", 0,
                        "size", competitions.size()
                ));

                return ResponseEntity.ok(response);
            } else {
                // 获取所有已发布的竞赛
                Page<Competition> competitionPage = competitionRepository.findAll(pageable);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", Map.of(
                        "content", competitionPage.getContent(),
                        "totalElements", competitionPage.getTotalElements(),
                        "totalPages", competitionPage.getTotalPages(),
                        "currentPage", page,
                        "size", size
                ));

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            logger.error("获取竞赛列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 开始答题
     */
    @PostMapping("/exam/start/{competitionId}")
    public ResponseEntity<Map<String, Object>> startExam(
            @PathVariable Long competitionId,
            Authentication authentication) {

        logger.info("开始答题请求: competitionId={}", competitionId);

        try {
            User currentUser = getCurrentUser(authentication);

            Map<String, Object> result = studentExamService.startExam(competitionId, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "开始答题成功");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("开始答题失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 保存答案（自动保存）
     */
    @PostMapping("/exam/save-answer")
    public ResponseEntity<Map<String, Object>> saveAnswer(
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {

        try {
            User currentUser = getCurrentUser(authentication);

            Long examPaperId = getLongValue(requestBody, "examPaperId");
            Long questionId = getLongValue(requestBody, "questionId");
            String answerContent = (String) requestBody.get("answerContent");

            studentExamService.saveAnswer(examPaperId, questionId, answerContent, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "答案保存成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("保存答案失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 提交答卷
     */
    @PostMapping("/exam/submit/{examPaperId}")
    public ResponseEntity<Map<String, Object>> submitExam(
            @PathVariable Long examPaperId,
            Authentication authentication) {

        logger.info("提交答卷请求: examPaperId={}", examPaperId);

        try {
            User currentUser = getCurrentUser(authentication);

            Map<String, Object> result = studentExamService.submitExam(examPaperId, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "答卷提交成功");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("提交答卷失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取答题进度
     */
    @GetMapping("/exam/progress/{examPaperId}")
    public ResponseEntity<Map<String, Object>> getExamProgress(
            @PathVariable Long examPaperId,
            Authentication authentication) {

        try {
            User currentUser = getCurrentUser(authentication);

            Map<String, Object> result = studentExamService.getExamProgress(examPaperId, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取答题进度失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 将竞赛添加到Map中（用于去重）
     */
    private void addCompetitionToMap(Map<Long, Map<String, Object>> competitionMap, Registration reg) {
        Long competitionId = reg.getCompetition().getId();
        
        // 如果已经添加过这个竞赛，跳过
        if (competitionMap.containsKey(competitionId)) {
            return;
        }
        
        competitionRepository.findById(competitionId).ifPresent(competition -> {
            Map<String, Object> competitionData = new HashMap<>();
            competitionData.put("id", competition.getId());
            competitionData.put("name", competition.getName());
            competitionData.put("description", competition.getDescription());
            competitionData.put("status", competition.getStatus());
            competitionData.put("category", competition.getCategory());
            competitionData.put("level", competition.getLevel());
            competitionData.put("startTime", competition.getStartTime());
            competitionData.put("endTime", competition.getEndTime());
            competitionData.put("registrationStartTime", competition.getRegistrationStartTime());
            competitionData.put("registrationEndTime", competition.getRegistrationEndTime());
            competitionData.put("isRegistered", true);
            competitionData.put("registrationStatus", reg.getStatus());
            competitionMap.put(competitionId, competitionData);
        });
    }

    /**
     * 获取考卷信息（根据竞赛ID和团队ID）
     */
    @GetMapping("/paper-info")
    public ResponseEntity<Map<String, Object>> getPaperInfo(
            @RequestParam Long competitionId,
            @RequestParam Long teamId) {
        
        logger.info("获取考卷信息: competitionId={}, teamId={}", competitionId, teamId);
        
        try {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> data = studentExamService.getPaperIdByCompetitionAndTeam(competitionId, teamId);
            
            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取考卷信息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取答题详情（根据考卷ID）
     */
    @GetMapping("/answer-details/{paperId}")
    public ResponseEntity<Map<String, Object>> getAnswerDetails(@PathVariable Long paperId) {
        
        logger.info("获取答题详情: paperId={}", paperId);
        
        try {
            Map<String, Object> response = new HashMap<>();
            List<Map<String, Object>> data = studentExamService.getAnswerDetailsByPaperId(paperId);
            
            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取答题详情失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 从Map中获取Long类型值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        throw new RuntimeException("无效的参数: " + key);
    }
}
