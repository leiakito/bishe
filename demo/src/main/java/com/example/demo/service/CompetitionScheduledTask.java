package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 竞赛定时任务服务
 * 用于自动更新竞赛状态
 */
@Service
public class CompetitionScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(CompetitionScheduledTask.class);

    @Autowired
    private CompetitionService competitionService;

    /**
     * 每分钟检查并更新竞赛状态
     *
     * 状态转换规则：
     * 1. PUBLISHED -> REGISTRATION_OPEN (当报名开始时间到达时)
     * 2. REGISTRATION_OPEN -> REGISTRATION_CLOSED (当报名结束时间到达时)
     * 3. REGISTRATION_CLOSED -> IN_PROGRESS (当竞赛开始时间到达时)
     * 4. IN_PROGRESS -> COMPLETED (当竞赛结束时间到达时)
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    public void updateCompetitionStatuses() {
        try {
            logger.info("开始执行竞赛状态自动更新任务");
            competitionService.updateCompetitionStatuses();
            logger.info("竞赛状态自动更新任务完成");
        } catch (Exception e) {
            logger.error("竞赛状态自动更新任务执行失败", e);
        }
    }

    /**
     * 每小时执行一次状态更新（备用任务，确保即使分钟任务失败也能更新）
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时整点执行
    public void updateCompetitionStatusesHourly() {
        try {
            logger.info("开始执行竞赛状态每小时更新任务");
            competitionService.updateCompetitionStatuses();
            logger.info("竞赛状态每小时更新任务完成");
        } catch (Exception e) {
            logger.error("竞赛状态每小时更新任务执行失败", e);
        }
    }
}
