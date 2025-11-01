package com.example.demo.service;

import com.example.demo.entity.TeamMember;
import com.example.demo.entity.Team;
import com.example.demo.entity.User;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TeamMemberService {
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // 申请加入团队
    public TeamMember applyToJoinTeam(Long teamId, Long userId) {
        // 验证团队
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        if (team.getStatus() != Team.TeamStatus.ACTIVE) {
            throw new RuntimeException("团队当前不接受新成员");
        }
        
        // 验证用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        if (user.getStatus() != User.UserStatus.APPROVED) {
            throw new RuntimeException("用户状态异常，无法加入团队");
        }
        
        // 检查是否已经是成员
        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), user.getId())) {
            throw new RuntimeException("用户已经是该团队成员或已申请加入");
        }
        
        // 检查团队是否已满
        // 统计团队活跃成员数量
        long currentMemberCount = teamMemberRepository.countActiveByTeamId(team.getId());
        if (currentMemberCount >= team.getMaxMembers()) {
            throw new RuntimeException("团队已满员");
        }
        
        // 检查用户是否已经在同一竞赛的其他团队中
        List<TeamMember> activeMemberships = teamMemberRepository.findActiveUserMembershipsInCompetition(userId, team.getCompetition().getId());
        for (TeamMember member : activeMemberships) {
            // 如果用户在该竞赛的其他团队中（不是当前团队）且状态为活跃，则拒绝
            if (!member.getTeam().getId().equals(teamId)) {
                throw new RuntimeException("用户已经在该竞赛的其他团队中");
            }
        }
        
        // 创建申请记录
        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setUser(user);
        teamMember.setRole(TeamMember.MemberRole.MEMBER);
        teamMember.setStatus(TeamMember.MemberStatus.PENDING);
        teamMember.setJoinedAt(LocalDateTime.now());
        
        return teamMemberRepository.save(teamMember);
    }
    
    // 审核加入申请
    public TeamMember approveJoinApplication(Long teamMemberId, Long approverId, boolean approved) {
        Optional<TeamMember> memberOpt = teamMemberRepository.findById(teamMemberId);
        if (memberOpt.isEmpty()) {
            throw new RuntimeException("申请记录不存在");
        }
        
        TeamMember teamMember = memberOpt.get();
        
        if (teamMember.getStatus() != TeamMember.MemberStatus.PENDING) {
            throw new RuntimeException("申请已经被处理");
        }
        
        // 验证审核者权限（队长或管理员）
        Optional<User> approverOpt = userRepository.findById(approverId);
        if (approverOpt.isEmpty()) {
            throw new RuntimeException("审核者不存在");
        }
        
        User approver = approverOpt.get();
        Team team = teamMember.getTeam();
        
        // 查找团队领导需要调整查询逻辑
        List<TeamMember> teamLeaders = teamMemberRepository.findByTeamId(team.getId());
        boolean isTeamLeader = teamLeaders.stream().anyMatch(leader -> 
            leader.getRole() == TeamMember.MemberRole.LEADER && 
            leader.getUser().getId().equals(approverId));
        boolean isAdmin = approver.getRole() == User.UserRole.ADMIN;
        
        if (!isTeamLeader && !isAdmin) {
            throw new RuntimeException("只有队长或管理员可以审核加入申请");
        }
      if (approved) {
            // 再次检查团队是否已满
            // 统计活跃成员
            long currentMemberCount = teamMemberRepository.countActiveByTeamId(team.getId());
            if (currentMemberCount >= team.getMaxMembers()) {
                throw new RuntimeException("团队已满员，无法批准申请");
            }
            
            teamMember.setStatus(TeamMember.MemberStatus.ACTIVE);
            teamMember.setJoinedAt(LocalDateTime.now());
            
            TeamMember savedMember = teamMemberRepository.save(teamMember);
            
            // 同步更新团队的 currentMembers 字段
            team.setCurrentMembers(teamMemberRepository.countActiveByTeamId(team.getId()).intValue());
            
            // 如果团队满员，更新状态
            if (team.getCurrentMembers() >= team.getMaxMembers()) {
                team.setStatus(Team.TeamStatus.FULL);
            }
            
            teamRepository.save(team);
            
            return savedMember;
        } else {
            // 拒绝申请：直接删除记录，将成员踢出队伍
            // PENDING 状态的成员不应该影响 currentMembers，所以不需要更新
            teamMemberRepository.delete(teamMember);
            
            return teamMember; // 返回被删除的记录信息
        }
        
    }
    
    // 直接添加成员（队长或管理员）
    public TeamMember addMemberDirectly(Long teamId, Long userId, Long operatorId, TeamMember.MemberRole role) {
        // 验证操作者权限
        Optional<User> operatorOpt = userRepository.findById(operatorId);
        if (operatorOpt.isEmpty()) {
            throw new RuntimeException("操作者不存在");
        }
        
        User operator = operatorOpt.get();
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        
        // 查找团队领导需要调整查询逻辑
        List<TeamMember> teamLeaders = teamMemberRepository.findByTeamId(team.getId());
        boolean isTeamLeader = teamLeaders.stream().anyMatch(leader -> 
            leader.getRole() == TeamMember.MemberRole.LEADER && 
            leader.getUser().getId().equals(operatorId));
        boolean isAdmin = operator.getRole() == User.UserRole.ADMIN;
        
        if (!isTeamLeader && !isAdmin) {
            throw new RuntimeException("只有队长或管理员可以直接添加成员");
        }
        
        // 验证用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        if (user.getStatus() != User.UserStatus.APPROVED) {
            throw new RuntimeException("用户状态异常，无法加入团队");
        }
        
        // 检查是否已经是成员
        // 检查是否已经是成员需要调整查询逻辑
        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), user.getId())) {
            throw new RuntimeException("用户已经是该团队成员");
        }
        
        // 检查团队是否已满
        // 统计活跃成员
        long currentMemberCount = teamMemberRepository.countActiveByTeamId(team.getId());
        if (currentMemberCount >= team.getMaxMembers()) {
            throw new RuntimeException("团队已满员");
        }
        
        // 如果要添加队长，需要先检查是否已有队长
        if (role == TeamMember.MemberRole.LEADER) {
            // 查找现有队长需要调整查询逻辑
            List<TeamMember> existingLeaders = teamMemberRepository.findByTeamId(team.getId()).stream()
                .filter(member -> member.getRole() == TeamMember.MemberRole.LEADER)
                .collect(java.util.stream.Collectors.toList());
            if (!existingLeaders.isEmpty()) {
                throw new RuntimeException("团队已有队长，请先转让队长职务");
            }
        }
        
        // 创建成员记录
        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setUser(user);
        teamMember.setRole(role);
        teamMember.setStatus(TeamMember.MemberStatus.ACTIVE);
        teamMember.setJoinedAt(LocalDateTime.now());
        
        TeamMember savedMember = teamMemberRepository.save(teamMember);
        
        // 同步更新团队的 currentMembers 字段
        team.setCurrentMembers(teamMemberRepository.countActiveByTeamId(team.getId()).intValue());
        
        // 如果团队满员，更新状态
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            team.setStatus(Team.TeamStatus.FULL);
        }
        
        teamRepository.save(team);
        
        return savedMember;
    }
    
    // 移除成员
    public void removeMember(Long teamMemberId, Long operatorId) {
        Optional<TeamMember> memberOpt = teamMemberRepository.findById(teamMemberId);
        if (memberOpt.isEmpty()) {
            throw new RuntimeException("成员记录不存在");
        }
        
        TeamMember teamMember = memberOpt.get();
        Team team = teamMember.getTeam();
        
        // 验证操作者权限
        Optional<User> operatorOpt = userRepository.findById(operatorId);
        if (operatorOpt.isEmpty()) {
            throw new RuntimeException("操作者不存在");
        }
        
        User operator = operatorOpt.get();
        
        // 查找团队领导需要调整查询逻辑
        List<TeamMember> teamLeaders = teamMemberRepository.findByTeamId(team.getId());
        boolean isTeamLeader = teamLeaders.stream().anyMatch(leader -> 
            leader.getRole() == TeamMember.MemberRole.LEADER && 
            leader.getUser().getId().equals(operatorId));
        boolean isAdmin = operator.getRole() == User.UserRole.ADMIN;
        boolean isSelf = teamMember.getUser().getId().equals(operatorId);
        
        if (!isTeamLeader && !isAdmin && !isSelf) {
            throw new RuntimeException("只有队长、管理员或本人可以移除成员");
        }
        
        // 队长不能被移除，只能转让
        if (teamMember.getRole() == TeamMember.MemberRole.LEADER && !isAdmin) {
            throw new RuntimeException("队长不能被移除，请先转让队长职务");
        }
        
        // 记录移除前的状态
        boolean wasActive = teamMember.getStatus() == TeamMember.MemberStatus.ACTIVE;
        
        teamMember.setStatus(TeamMember.MemberStatus.REMOVED);
        teamMember.setLeftAt(LocalDateTime.now());
        
        teamMemberRepository.save(teamMember);
        
        // 如果移除的是活跃成员，需要同步更新 currentMembers
        if (wasActive) {
            team.setCurrentMembers(teamMemberRepository.countActiveByTeamId(team.getId()).intValue());
            
            // 如果团队不再满员，更新状态
            if (team.getStatus() == Team.TeamStatus.FULL && team.getCurrentMembers() < team.getMaxMembers()) {
                team.setStatus(Team.TeamStatus.ACTIVE);
            }
            
            teamRepository.save(team);
        }
    }
    
    // 主动退出团队
    public void leaveTeam(Long teamId, Long userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        // 查找团队成员需要调整查询逻辑
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        Optional<TeamMember> memberOpt = teamMemberRepository.findByTeamIdAndUserId(team.getId(), user.getId());
        
        if (memberOpt.isEmpty()) {
            throw new RuntimeException("用户不是该团队的活跃成员");
        }
        
        TeamMember teamMember = memberOpt.get();
        
        // 队长退出需要先转让队长职务
        if (teamMember.getRole() == TeamMember.MemberRole.LEADER) {
            // 统计活跃成员数量
            long activeMemberCount = teamMemberRepository.countActiveByTeamId(team.getId());
            if (activeMemberCount > 1) {
                throw new RuntimeException("队长退出前需要先转让队长职务");
            }
        }
        
        // 记录退出前的状态
        boolean wasActive = teamMember.getStatus() == TeamMember.MemberStatus.ACTIVE;
        
        teamMember.setStatus(TeamMember.MemberStatus.INACTIVE);
        teamMember.setLeftAt(LocalDateTime.now());
        
        teamMemberRepository.save(teamMember);
        
        // 如果退出的是活跃成员，需要同步更新 currentMembers
        if (wasActive) {
            team.setCurrentMembers(teamMemberRepository.countActiveByTeamId(team.getId()).intValue());
            
            // 如果团队不再满员，更新状态
            if (team.getStatus() == Team.TeamStatus.FULL && team.getCurrentMembers() < team.getMaxMembers()) {
                team.setStatus(Team.TeamStatus.ACTIVE);
            }
            
            teamRepository.save(team);
        }
    }
    
    // 转让队长职务
    public void transferLeadership(Long teamId, Long currentLeaderId, Long newLeaderId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        
        // 验证当前队长
        Optional<User> currentLeaderOpt = userRepository.findById(currentLeaderId);
        if (currentLeaderOpt.isEmpty()) {
            throw new RuntimeException("当前队长不存在");
        }
        
        // 查找当前队长成员需要调整查询逻辑
        Optional<TeamMember> currentLeaderMemberOpt = teamMemberRepository.findByTeamIdAndUserId(team.getId(), 
            currentLeaderOpt.get().getId());
        
        if (currentLeaderMemberOpt.isEmpty() || 
            currentLeaderMemberOpt.get().getRole() != TeamMember.MemberRole.LEADER) {
            throw new RuntimeException("用户不是该团队的队长");
        }
        
        // 验证新队长
        Optional<User> newLeaderOpt = userRepository.findById(newLeaderId);
        if (newLeaderOpt.isEmpty()) {
            throw new RuntimeException("新队长不存在");
        }
        
        // 查找新队长成员需要调整查询逻辑
        Optional<TeamMember> newLeaderMemberOpt = teamMemberRepository.findByTeamIdAndUserId(team.getId(), 
            newLeaderOpt.get().getId());
        
        if (newLeaderMemberOpt.isEmpty()) {
            throw new RuntimeException("新队长不是该团队的活跃成员");
        }
        
        // 执行转让
        TeamMember currentLeaderMember = currentLeaderMemberOpt.get();
        TeamMember newLeaderMember = newLeaderMemberOpt.get();
        
        currentLeaderMember.setRole(TeamMember.MemberRole.MEMBER);
        newLeaderMember.setRole(TeamMember.MemberRole.LEADER);
        
        teamMemberRepository.save(currentLeaderMember);
        teamMemberRepository.save(newLeaderMember);
    }
    
    // 根据ID查找成员
    public Optional<TeamMember> findById(Long id) {
        return teamMemberRepository.findById(id);
    }
    
    // 获取团队所有成员
    public List<TeamMember> getTeamMembers(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        // 查找团队成员需要调整查询逻辑
        return teamMemberRepository.findByTeamId(teamOpt.get().getId());
    }
    
    // 获取团队活跃成员
    public List<TeamMember> getActiveTeamMembers(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        // 查找活跃团队成员需要调整查询逻辑
        return teamMemberRepository.findByTeamId(teamOpt.get().getId()).stream()
            .filter(member -> member.getStatus() == TeamMember.MemberStatus.ACTIVE)
            .collect(java.util.stream.Collectors.toList());
    }
    
    // 获取团队队长
    public Optional<TeamMember> getTeamLeader(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        // 查找团队领导需要调整查询逻辑
        List<TeamMember> leaders = teamMemberRepository.findByTeamId(teamOpt.get().getId());
        return leaders.isEmpty() ? Optional.empty() : Optional.of(leaders.get(0));
    }
    
    // 获取用户参与的团队
    public List<TeamMember> getUserTeams(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        // 查找用户团队需要调整查询逻辑
        return teamMemberRepository.findByUserId(userOpt.get().getId());
    }
    
    // 获取用户当前活跃的团队
    public List<TeamMember> getUserActiveTeams(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        // 查找用户活跃团队需要调整查询逻辑
        return teamMemberRepository.findByUserId(userOpt.get().getId()).stream()
            .filter(member -> member.getStatus() == TeamMember.MemberStatus.ACTIVE)
            .collect(java.util.stream.Collectors.toList());
    }
    
    // 获取用户在特定竞赛中的团队
    public List<TeamMember> getUserTeamsInCompetition(Long userId, Long competitionId) {
        // 查找用户在竞赛中的团队需要调整查询逻辑
        return teamMemberRepository.findByUserId(userId);
    }
    
    // 检查用户是否是团队成员
    public boolean isTeamMember(Long teamId, Long userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (teamOpt.isEmpty() || userOpt.isEmpty()) {
            return false;
        }
        
        // 检查团队成员需要调整查询逻辑
        return teamMemberRepository.existsByTeamIdAndUserId(teamOpt.get().getId(), userOpt.get().getId());
    }
    
    // 检查用户是否是团队队长
    public boolean isTeamLeader(Long teamId, Long userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (teamOpt.isEmpty() || userOpt.isEmpty()) {
            return false;
        }
        
        // 查找团队成员需要调整查询逻辑
        Optional<TeamMember> memberOpt = teamMemberRepository.findByTeamIdAndUserId(
            teamOpt.get().getId(), userOpt.get().getId());
        
        return memberOpt.isPresent() && memberOpt.get().getRole() == TeamMember.MemberRole.LEADER;
    }
    
    // 获取团队成员数量
    public long getTeamMemberCount(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        // 统计团队活跃成员数量
        return teamMemberRepository.countActiveByTeamId(teamOpt.get().getId());
    }
    
    // 获取待审核的申请
    public List<TeamMember> getPendingApplications(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        // 查找待审核申请
        return teamMemberRepository.findPendingMembersByTeam(teamOpt.get().getId());
    }
    
    // 获取用户的待审核申请
    public List<TeamMember> getUserPendingApplications(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        // 查找用户待审核申请需要调整查询逻辑
        return teamMemberRepository.findByUserId(userOpt.get().getId()).stream()
            .filter(member -> member.getStatus() == TeamMember.MemberStatus.PENDING)
            .collect(java.util.stream.Collectors.toList());
    }
    
    // 批量审核申请
    public List<TeamMember> batchApproveApplications(List<Long> teamMemberIds, Long approverId, boolean approved) {
        List<TeamMember> results = new java.util.ArrayList<>();
        
        for (Long teamMemberId : teamMemberIds) {
            try {
                TeamMember result = approveJoinApplication(teamMemberId, approverId, approved);
                results.add(result);
            } catch (RuntimeException e) {
                // 记录错误但继续处理其他申请
                System.err.println("Failed to process application " + teamMemberId + ": " + e.getMessage());
            }
        }
        
        return results;
    }
    
    // 根据状态获取团队成员
    public Page<TeamMember> getTeamMembersByStatus(Long teamId, TeamMember.MemberStatus status, Pageable pageable) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (!teamOpt.isPresent()) {
            return Page.empty();
        }
        
        // 注意：需要调整查询逻辑，因为原方法不存在
        List<TeamMember> allMembers = teamMemberRepository.findByTeamId(teamOpt.get().getId());
        List<TeamMember> filteredMembers = allMembers.stream()
            .filter(member -> member.getStatus() == status)
            .collect(java.util.stream.Collectors.toList());
            
        // 手动实现分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredMembers.size());
        List<TeamMember> pageContent = filteredMembers.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, filteredMembers.size());
    }
    
    // 根据角色获取团队成员
    public Page<TeamMember> getTeamMembersByRole(Long teamId, TeamMember.MemberRole role, Pageable pageable) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (!teamOpt.isPresent()) {
            return Page.empty();
        }
        
        // 注意：需要调整查询逻辑，因为原方法不存在
        List<TeamMember> allMembers = teamMemberRepository.findByTeamId(teamOpt.get().getId());
        List<TeamMember> filteredMembers = allMembers.stream()
            .filter(member -> member.getRole() == role)
            .collect(java.util.stream.Collectors.toList());
            
        // 手动实现分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredMembers.size());
        List<TeamMember> pageContent = filteredMembers.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, filteredMembers.size());
    }
    
    // 搜索团队成员
    public Page<TeamMember> searchTeamMembers(Long teamId, String keyword, Pageable pageable) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        if (keyword == null || keyword.trim().isEmpty()) {
             // 分页查找团队成员需要调整查询逻辑
         List<TeamMember> allMembers = teamMemberRepository.findByTeamId(teamOpt.get().getId());
         int start = (int) pageable.getOffset();
         int end = Math.min((start + pageable.getPageSize()), allMembers.size());
         List<TeamMember> pageContent = allMembers.subList(start, end);
         return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, allMembers.size());
        }
        
        // 搜索团队成员需要调整查询逻辑
        List<TeamMember> allMembers = teamMemberRepository.findByTeamId(teamOpt.get().getId());
        List<TeamMember> filteredMembers = allMembers.stream()
            .filter(member -> member.getUser().getRealName().contains(keyword) || 
                             member.getUser().getEmail().contains(keyword))
            .collect(java.util.stream.Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredMembers.size());
        List<TeamMember> pageContent = filteredMembers.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filteredMembers.size());
    }
    
    // 获取团队成员统计信息
    public java.util.Map<String, Object> getTeamMemberStats(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 统计团队成员总数（包括所有状态）
        List<TeamMember> allMembers = teamMemberRepository.findByTeamId(team.getId());
        stats.put("totalMembers", allMembers.size());
        // 统计活跃成员
        stats.put("activeMembers", teamMemberRepository.countActiveByTeamId(team.getId()));
        // 统计待审核申请
        stats.put("pendingApplications", teamMemberRepository.findPendingMembersByTeam(team.getId()).size());
        stats.put("maxMembers", team.getMaxMembers());
        // 计算可用名额
        stats.put("availableSlots", team.getMaxMembers() - teamMemberRepository.countActiveByTeamId(team.getId()));
        
        return stats;
    }
}