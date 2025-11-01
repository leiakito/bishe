package com.example.demo.service;

import com.example.demo.dto.DistributionResultDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 考卷下发服务
 */
@Service
public class ExamDistributionService {

    private static final Logger logger = LoggerFactory.getLogger(ExamDistributionService.class);

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private CompetitionQuestionRepository competitionQuestionRepository;

    @Autowired
    private ExamPaperRepository examPaperRepository;

    @Autowired
    private ExamAnswerRepository examAnswerRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    /**
     * 为竞赛下发考卷
     * 当竞赛状态变更为"进行中"时调用
     *
     * @param competitionId 竞赛ID
     * @return 下发结果
     */
    @Transactional
    public DistributionResultDTO distributeExamPapers(Long competitionId) {
        DistributionResultDTO result = new DistributionResultDTO();

        // 1. 验证竞赛是否存在
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new RuntimeException("竞赛不存在"));

        logger.info("开始为竞赛下发考卷: competitionId={}, name={}", competitionId, competition.getName());

        // 2. 获取该竞赛的所有已批准的报名记录
        List<Registration> approvedRegistrations = registrationRepository
                .findByCompetitionAndStatus(competition, Registration.RegistrationStatus.APPROVED);

        if (approvedRegistrations.isEmpty()) {
            logger.warn("竞赛没有已批准的参赛者: competitionId={}", competitionId);
            throw new RuntimeException("该竞赛没有已批准的参赛者");
        }

        logger.info("找到{}条已批准的报名记录", approvedRegistrations.size());

        // 3. 获取该竞赛的所有启用的题目
        List<CompetitionQuestion> competitionQuestions = competitionQuestionRepository
                .findByCompetitionIdAndIsActiveTrueOrderByQuestionOrder(competitionId);

        if (competitionQuestions.isEmpty()) {
            logger.warn("竞赛尚未配置题目: competitionId={}", competitionId);
            throw new RuntimeException("该竞赛尚未配置题目");
        }

        logger.info("找到{}道题目", competitionQuestions.size());

        int individualCount = 0;
        int teamCount = 0;

        // 4. 为每个参赛者创建考卷
        for (Registration registration : approvedRegistrations) {
            Team team = registration.getTeam();

            if (team == null) {
                logger.error("报名记录的团队为空: registrationId={}", registration.getId());
                continue;
            }

            // 判断是个人赛还是团队赛
            boolean isTeamCompetition = team.getMaxMembers() != null && team.getMaxMembers() > 1;

            ExamPaper.ParticipantType participantType = isTeamCompetition ?
                    ExamPaper.ParticipantType.TEAM : ExamPaper.ParticipantType.INDIVIDUAL;
            Long participantId = isTeamCompetition ? team.getId() : team.getLeader().getId();

            // 检查是否已存在考卷(避免重复下发)
            boolean paperExists = examPaperRepository.existsByCompetitionIdAndParticipantTypeAndParticipantId(
                    competitionId, participantType, participantId
            );

            if (paperExists) {
                logger.info("考卷已存在,跳过: competitionId={}, participantType={}, participantId={}",
                        competitionId, participantType, participantId);
                continue;
            }

            // 创建考卷
            ExamPaper paper = new ExamPaper();
            paper.setCompetitionId(competitionId);
            paper.setParticipantType(participantType);
            paper.setParticipantId(participantId);
            paper.setPaperStatus(ExamPaper.PaperStatus.NOT_STARTED);
            paper.setTotalQuestionCount(competitionQuestions.size());

            // 保存考卷
            ExamPaper savedPaper = examPaperRepository.save(paper);

            logger.info("创建考卷成功: paperId={}, participantType={}, participantId={}",
                    savedPaper.getId(), participantType, participantId);

            // 5. 为考卷创建答题记录(预生成,初始状态为空)
            for (CompetitionQuestion cq : competitionQuestions) {
                ExamAnswer answer = new ExamAnswer();
                answer.setExamPaperId(savedPaper.getId());
                answer.setQuestionId(cq.getQuestionId());
                answer.setMaxScore(cq.getQuestionScore());
                answer.setGradingStatus(ExamAnswer.GradingStatus.PENDING);
                examAnswerRepository.save(answer);
            }

            logger.info("为考卷创建了{}条答题记录", competitionQuestions.size());

            if (isTeamCompetition) {
                teamCount++;
            } else {
                individualCount++;
            }
        }

        result.setIndividualPapers(individualCount);
        result.setTeamPapers(teamCount);
        result.setTotalParticipants(approvedRegistrations.size());

        logger.info("考卷下发完成: 个人赛{}份, 团队赛{}份, 总参赛者{}人",
                individualCount, teamCount, approvedRegistrations.size());

        return result;
    }

    /**
     * 检查某个竞赛是否已下发考卷
     */
    public boolean isExamDistributed(Long competitionId) {
        long count = examPaperRepository.countByCompetitionId(competitionId);
        return count > 0;
    }
}
