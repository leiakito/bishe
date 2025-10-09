package com.example.demo.service;

import com.example.demo.entity.Team;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 团队数据修复服务
 * 用于修复 currentMembers 字段的数据不一致问题
 */
@Service
public class TeamDataFixService {
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    /**
     * 修复所有团队的 currentMembers 字段
     * 从数据库重新计算活跃成员数量
     */
    @Transactional
    public void fixAllTeamsCurrentMembers() {
        List<Team> allTeams = teamRepository.findAll();
        int fixedCount = 0;
        int errorCount = 0;
        
        for (Team team : allTeams) {
            try {
                // 从数据库重新计算活跃成员数量
                Long activeCount = teamMemberRepository.countActiveByTeamId(team.getId());
                int oldValue = team.getCurrentMembers() != null ? team.getCurrentMembers() : 0;
                int newValue = activeCount.intValue();
                
                if (oldValue != newValue) {
                    System.out.println(String.format(
                        "团队 [%s] (ID: %d) currentMembers 修复: %d -> %d",
                        team.getName(), team.getId(), oldValue, newValue
                    ));
                    
                    team.setCurrentMembers(newValue);
                    
                    // 同时修正团队状态
                    if (newValue >= team.getMaxMembers() && team.getStatus() == Team.TeamStatus.ACTIVE) {
                        team.setStatus(Team.TeamStatus.FULL);
                        System.out.println(String.format(
                            "  -> 团队状态更新为 FULL (成员数: %d/%d)",
                            newValue, team.getMaxMembers()
                        ));
                    } else if (newValue < team.getMaxMembers() && team.getStatus() == Team.TeamStatus.FULL) {
                        team.setStatus(Team.TeamStatus.ACTIVE);
                        System.out.println(String.format(
                            "  -> 团队状态更新为 ACTIVE (成员数: %d/%d)",
                            newValue, team.getMaxMembers()
                        ));
                    }
                    
                    teamRepository.save(team);
                    fixedCount++;
                }
            } catch (Exception e) {
                errorCount++;
                System.err.println(String.format(
                    "修复团队 [%s] (ID: %d) 时出错: %s",
                    team.getName(), team.getId(), e.getMessage()
                ));
            }
        }
        
        System.out.println(String.format(
            "\n=== 数据修复完成 ===\n总计: %d 个团队\n修复: %d 个\n错误: %d 个\n",
            allTeams.size(), fixedCount, errorCount
        ));
    }
    
    /**
     * 修复指定团队的 currentMembers 字段
     */
    @Transactional
    public void fixTeamCurrentMembers(Long teamId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("团队不存在"));
        
        Long activeCount = teamMemberRepository.countActiveByTeamId(teamId);
        int oldValue = team.getCurrentMembers() != null ? team.getCurrentMembers() : 0;
        int newValue = activeCount.intValue();
        
        System.out.println(String.format(
            "团队 [%s] (ID: %d) currentMembers: %d -> %d",
            team.getName(), teamId, oldValue, newValue
        ));
        
        team.setCurrentMembers(newValue);
        
        // 同时修正团队状态
        if (newValue >= team.getMaxMembers() && team.getStatus() == Team.TeamStatus.ACTIVE) {
            team.setStatus(Team.TeamStatus.FULL);
        } else if (newValue < team.getMaxMembers() && team.getStatus() == Team.TeamStatus.FULL) {
            team.setStatus(Team.TeamStatus.ACTIVE);
        }
        
        teamRepository.save(team);
    }
    
    /**
     * 检查所有团队的数据一致性
     */
    public void checkAllTeamsDataConsistency() {
        List<Team> allTeams = teamRepository.findAll();
        int inconsistentCount = 0;
        
        System.out.println("=== 开始检查团队数据一致性 ===\n");
        
        for (Team team : allTeams) {
            Long activeCount = teamMemberRepository.countActiveByTeamId(team.getId());
            int currentMembers = team.getCurrentMembers() != null ? team.getCurrentMembers() : 0;
            
            if (!activeCount.equals((long) currentMembers)) {
                inconsistentCount++;
                System.out.println(String.format(
                    "❌ 团队 [%s] (ID: %d) 数据不一致: currentMembers=%d, 实际活跃成员=%d (差值: %d)",
                    team.getName(), team.getId(), currentMembers, activeCount, currentMembers - activeCount
                ));
            }
        }
        
        if (inconsistentCount == 0) {
            System.out.println("✅ 所有团队数据一致！");
        } else {
            System.out.println(String.format(
                "\n发现 %d 个团队数据不一致，建议运行 fixAllTeamsCurrentMembers() 修复",
                inconsistentCount
            ));
        }
    }
}

