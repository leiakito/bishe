package com.example.demo.service;

import com.example.demo.entity.Team;
import com.example.demo.entity.TeamMember;
import com.example.demo.entity.User;
import com.example.demo.entity.Competition;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.CompetitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TeamService {
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompetitionRepository competitionRepository;
    
    // 创建团队
    public Team createTeam(Team team, Long competitionId, Long creatorId) {
        // 验证创建者
        Optional<User> creatorOpt = userRepository.findById(creatorId);
        if (creatorOpt.isEmpty()) {
            throw new RuntimeException("创建者不存在");
        }
        
        // 验证竞赛
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        
        Competition competition = competitionOpt.get();
        if (competition.getStatus() != Competition.CompetitionStatus.REGISTRATION_OPEN) {
            throw new RuntimeException("竞赛未开放报名");
        }
        
        // 检查用户是否已经在该竞赛中有团队
        Optional<Team> existingTeam = teamRepository.findUserTeamInCompetition(creatorId, competition.getId());
        if (existingTeam.isPresent()) {
            throw new RuntimeException("您已经在该竞赛中创建了团队");
        }
        
        // 设置团队属性
        team.setCompetition(competition);
        team.setLeader(creatorOpt.get()); // 设置队长
        team.setMaxMembers(competition.getMaxTeamSize());
        team.setInviteCode(generateInviteCode());
        
        // 生成团队编号（如果需要的话，可以在这里添加逻辑）
        team.setStatus(Team.TeamStatus.ACTIVE);
        team.setCurrentMembers(1); // 创建者自动成为成员
        
        Team savedTeam = teamRepository.save(team);
        
        // 创建者自动成为队长
        TeamMember captain = new TeamMember();
        captain.setTeam(savedTeam);
        captain.setUser(creatorOpt.get());
        captain.setRole(TeamMember.MemberRole.LEADER);
        captain.setStatus(TeamMember.MemberStatus.ACTIVE);
        captain.setJoinedAt(LocalDateTime.now());
        teamMemberRepository.save(captain);
        
        return savedTeam;
    }
    
    // 生成团队编号
    private String generateTeamNumber() {
        return "TEAM" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
    
    // 根据ID查找团队
    public Optional<Team> findById(Long id) {
        return teamRepository.findById(id);
    }

    // 获取所有团队（分页）
    public Page<Team> getAllTeams(Pageable pageable) {
        // 使用自定义查询来加载关联的 competition 和 leader 数据
        return teamRepository.findAllWithDetails(pageable);
    }
    
    // 根据竞赛获取团队（旧方法，仅查询直接关联的团队）
    public Page<Team> getTeamsByCompetition(Long competitionId, Pageable pageable) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        return teamRepository.findByCompetitionId(competitionOpt.get().getId(), pageable);
    }

    // 根据竞赛获取所有相关团队（包括通过报名记录关联的团队）
    public Page<Team> getAllTeamsByCompetition(Long competitionId, Pageable pageable) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        // 使用新方法：查询直接关联的团队 + 通过报名记录关联的团队
        return teamRepository.findTeamsByCompetitionIncludingRegistrations(competitionId, pageable);
    }

    // 获取竞赛的团队列表（带leader信息，用于教师管理）
    public List<Team> getTeamsByCompetitionWithLeader(Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        return teamRepository.findTeamsByCompetitionWithLeader(competitionId);
    }

    // 获取竞赛的报名团队（仅通过报名记录关联）
    public Page<Team> getRegisteredTeamsByCompetition(Long competitionId, Pageable pageable) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        return teamRepository.findRegisteredTeamsByCompetition(competitionId, pageable);
    }
    
    // 根据状态获取团队
    public Page<Team> getTeamsByStatus(Team.TeamStatus status, Pageable pageable) {
        return teamRepository.findByStatus(status, pageable);
    }
    
    // 搜索团队
    public Page<Team> searchTeams(String keyword, Pageable pageable) {
        return teamRepository.searchTeams(keyword, pageable);
    }
    
    // 获取用户创建的团队
    public List<Team> getTeamsByCreator(Long creatorId) {
        return teamRepository.findTeamsCreatedByUser(creatorId);
    }
    
    // 获取用户参与的团队
    public List<Team> getTeamsByMember(Long userId) {
        return teamRepository.findTeamsByUser(userId);
    }
    
    // 获取正在招募的团队
    public List<Team> getRecruitingTeams(Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new RuntimeException("竞赛不存在");
        }
        return teamRepository.findByCompetitionIdAndStatus(competitionOpt.get().getId(), Team.TeamStatus.ACTIVE);
    }
    
    // 获取未满员的团队
    public List<Team> getAvailableTeams(Long competitionId) {
        if (competitionId != null) {
            Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
            if (competitionOpt.isEmpty()) {
                throw new RuntimeException("竞赛不存在");
            }
            // 使用带关联数据的查询方法
            return teamRepository.findAvailableTeamsInCompetitionWithDetails(competitionId);
        } else {
            // 如果没有指定竞赛ID，返回所有可用团队
            return teamRepository.findAllAvailableTeamsWithDetails();
        }
    }
    
    // 申请加入团队
    public TeamMember joinTeam(Long teamId, Long userId) {
        // 验证团队
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        if (team.getStatus() != Team.TeamStatus.ACTIVE) {
            throw new RuntimeException("团队未开放招募");
        }
        
        // 验证用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查用户是否已经在该团队中
        Optional<TeamMember> existingMember = teamMemberRepository.findByTeamIdAndUserId(team.getId(), userOpt.get().getId());
        if (existingMember.isPresent()) {
            throw new RuntimeException("您已经是该团队成员");
        }
        
        // 检查用户是否已经在该竞赛的其他团队中（排除当前团队）
        Optional<Team> userTeamInCompetition = teamRepository.findUserTeamInCompetition(userId, team.getCompetition().getId());
        if (userTeamInCompetition.isPresent() && !userTeamInCompetition.get().getId().equals(team.getId())) {
            throw new RuntimeException("您已经参加了该竞赛的其他团队");
        }
        
        // 检查团队是否已满
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            throw new RuntimeException("团队已满员");
        }
        
        // 创建团队成员记录
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(userOpt.get());
        member.setRole(TeamMember.MemberRole.MEMBER);
        member.setStatus(TeamMember.MemberStatus.INACTIVE); // 待队长审核
        member.setJoinedAt(LocalDateTime.now());
        
        return teamMemberRepository.save(member);
    }
    
    // 审核团队成员申请
    public TeamMember approveMember(Long teamId, Long memberId, Long captainId, boolean approve) {
        // 验证团队
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        
        // 验证队长权限
        Optional<TeamMember> captainOpt = teamMemberRepository.findByTeamIdAndUserId(
            team.getId(), captainId);
        if (captainOpt.isEmpty() || captainOpt.get().getRole() != TeamMember.MemberRole.LEADER) {
            throw new RuntimeException("您不是该团队的队长");
        }

        
        // 验证成员申请
        Optional<TeamMember> memberOpt = teamMemberRepository.findById(memberId);
        if (memberOpt.isEmpty() || !memberOpt.get().getTeam().getId().equals(teamId)) {
            throw new RuntimeException("成员申请不存在");
        }
        
        TeamMember member = memberOpt.get();
        if (member.getStatus() != TeamMember.MemberStatus.INACTIVE) {
            throw new RuntimeException("申请已处理");
        }
        
        if (approve) {
            // 检查团队是否还有空位
            if (team.getCurrentMembers() >= team.getMaxMembers()) {
                throw new RuntimeException("团队已满员");
            }
            
            member.setStatus(TeamMember.MemberStatus.ACTIVE);
            
            // 更新团队成员数量
            team.setCurrentMembers(team.getCurrentMembers() + 1);
            
            // 如果团队满员，更新状态
            if (team.getCurrentMembers() >= team.getMaxMembers()) {
                team.setStatus(Team.TeamStatus.FULL);
            }
            
            teamRepository.save(team);
        } else {
            member.setStatus(TeamMember.MemberStatus.REMOVED);
        }
        
        return teamMemberRepository.save(member);
    }
    
    // 移除团队成员
    public void removeMember(Long teamId, Long memberId, Long operatorId) {
        // 验证团队
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        
        // 验证操作者权限（队长或管理员）
        Optional<User> operatorOpt = userRepository.findById(operatorId);
        if (operatorOpt.isEmpty()) {
            throw new RuntimeException("操作者不存在");
        }
        
        User operator = operatorOpt.get();
        boolean isCaptain = teamMemberRepository.isTeamLeader(team.getId(), operatorId);
        boolean isAdmin = operator.getRole() == User.UserRole.ADMIN;
        // 检查是否是竞赛创建者（教师）
        boolean isCompetitionCreator = team.getCompetition() != null &&
                                        team.getCompetition().getCreatedBy() != null &&
                                        team.getCompetition().getCreatedBy().getId().equals(operatorId);

        if (!isCaptain && !isAdmin && !isCompetitionCreator) {
            throw new RuntimeException("没有权限移除成员");
        }
        
        // 验证成员
        Optional<TeamMember> memberOpt = teamMemberRepository.findById(memberId);
        if (memberOpt.isEmpty() || !memberOpt.get().getTeam().getId().equals(teamId)) {
            throw new RuntimeException("成员不存在");
        }
        
        TeamMember member = memberOpt.get();
        
        // 不能移除队长
        if (member.getRole() == TeamMember.MemberRole.LEADER) {
            throw new RuntimeException("不能移除队长");
        }
        
        // 移除成员
        teamMemberRepository.delete(member);
        
        // 更新团队成员数量
        if (member.getStatus() == TeamMember.MemberStatus.ACTIVE) {
            team.setCurrentMembers(team.getCurrentMembers() - 1);
            
            // 如果团队不再满员，更新状态
            if (team.getStatus() == Team.TeamStatus.FULL) {
                team.setStatus(Team.TeamStatus.ACTIVE);
            }
            
            teamRepository.save(team);
        }
    }
    
    // 退出团队
    public void leaveTeam(Long teamId, Long userId) {
        // 验证团队
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        
        // 验证用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        // 查找成员记录
        Optional<TeamMember> memberOpt = teamMemberRepository.findByTeamIdAndUserId(team.getId(), userOpt.get().getId());
        if (memberOpt.isEmpty()) {
            throw new RuntimeException("您不是该团队成员");
        }
        
        TeamMember member = memberOpt.get();
        
        // 队长不能直接退出，需要先转让队长
        if (member.getRole() == TeamMember.MemberRole.LEADER) {
            throw new RuntimeException("队长不能直接退出团队，请先转让队长职位");
        }
        
        // 移除成员
        teamMemberRepository.delete(member);
        
        // 更新团队成员数量
        if (member.getStatus() == TeamMember.MemberStatus.ACTIVE) {
            team.setCurrentMembers(team.getCurrentMembers() - 1);
            
            // 如果团队不再满员，更新状态
            if (team.getStatus() == Team.TeamStatus.FULL) {
                team.setStatus(Team.TeamStatus.ACTIVE);
            }
            
            teamRepository.save(team);
        }
    }
    
    // 转让队长
    public void transferCaptain(Long teamId, Long currentCaptainId, Long newCaptainId) {
        // 验证团队
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        
        // 验证当前队长
        if (!teamMemberRepository.isTeamLeader(team.getId(), currentCaptainId)) {
            throw new RuntimeException("您不是该团队的队长");
        }
        
        // 验证新队长
        Optional<User> newCaptainUserOpt = userRepository.findById(newCaptainId);
        if (newCaptainUserOpt.isEmpty()) {
            throw new RuntimeException("新队长用户不存在");
        }
        
        Optional<TeamMember> newCaptainOpt = teamMemberRepository.findByTeamIdAndUserId(team.getId(), newCaptainUserOpt.get().getId());
        if (newCaptainOpt.isEmpty()) {
            throw new RuntimeException("新队长不是团队成员");
        }
        
        TeamMember newCaptain = newCaptainOpt.get();
        if (newCaptain.getStatus() != TeamMember.MemberStatus.ACTIVE) {
            throw new RuntimeException("新队长不是活跃成员");
        }
        
        // 获取当前队长
        Optional<TeamMember> currentCaptainOpt = teamMemberRepository.findByTeamIdAndUserId(team.getId(), currentCaptainId);
        if (currentCaptainOpt.isEmpty()) {
            throw new RuntimeException("当前队长记录不存在");
        }
        
        // 转让队长
        TeamMember currentCaptain = currentCaptainOpt.get();
        currentCaptain.setRole(TeamMember.MemberRole.MEMBER);
        newCaptain.setRole(TeamMember.MemberRole.LEADER);
        
        teamMemberRepository.save(currentCaptain);
        teamMemberRepository.save(newCaptain);
    }
    
    // 更新团队信息
    public Team updateTeam(Long teamId, Team updatedTeam, Long userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        
        // 检查权限（队长或管理员）
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        boolean isCaptain = teamMemberRepository.isTeamLeader(team.getId(), userId);
        boolean isAdmin = user.getRole() == User.UserRole.ADMIN;
        
        if (!isCaptain && !isAdmin) {
            throw new RuntimeException("没有权限修改团队信息");
        }
        
        // 更新允许修改的字段
        if (updatedTeam.getName() != null) {
            // 检查团队名称是否重复
            Optional<Team> existingTeam = teamRepository.findByNameAndCompetitionId(
                updatedTeam.getName(), team.getCompetition().getId());
            if (existingTeam.isPresent() && !existingTeam.get().getId().equals(teamId)) {
                throw new RuntimeException("团队名称已存在");
            }
            team.setName(updatedTeam.getName());
        }
        
        if (updatedTeam.getDescription() != null) {
            team.setDescription(updatedTeam.getDescription());
        }
        
        if (updatedTeam.getMaxMembers() != null) {
            // 不能设置小于当前成员数的最大成员数
            if (updatedTeam.getMaxMembers() < team.getCurrentMembers()) {
                throw new RuntimeException("最大成员数不能小于当前成员数");
            }
            team.setMaxMembers(updatedTeam.getMaxMembers());
            
            // 更新团队状态
            if (team.getCurrentMembers() >= team.getMaxMembers()) {
                team.setStatus(Team.TeamStatus.FULL);
            } else if (team.getStatus() == Team.TeamStatus.FULL) {
                team.setStatus(Team.TeamStatus.ACTIVE);
            }
        }
        
        return teamRepository.save(team);
    }
    
    // 解散团队
    public void disbandTeam(Long teamId, Long userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        
        // 检查权限（队长或管理员）
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        boolean isCaptain = teamMemberRepository.isTeamLeader(team.getId(), userId);
        boolean isAdmin = user.getRole() == User.UserRole.ADMIN;
        // 检查是否是竞赛创建者（教师）
        boolean isCompetitionCreator = team.getCompetition() != null &&
                                        team.getCompetition().getCreatedBy() != null &&
                                        team.getCompetition().getCreatedBy().getId().equals(userId);

        if (!isCaptain && !isAdmin && !isCompetitionCreator) {
            throw new RuntimeException("没有权限解散团队");
        }
        
        // 检查是否可以解散
        Competition competition = team.getCompetition();
        if (competition.getStatus() == Competition.CompetitionStatus.IN_PROGRESS || 
            competition.getStatus() == Competition.CompetitionStatus.COMPLETED) {
            throw new RuntimeException("竞赛进行中或已完成的团队不能解散");
        }
        
        // 删除所有团队成员
        List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());
        teamMemberRepository.deleteAll(members);
        
        // 删除团队
        teamRepository.delete(team);
    }
    
    // 获取团队成员
    public List<TeamMember> getTeamMembers(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }

        // 使用带用户信息急加载的查询方法，避免懒加载异常
        return teamMemberRepository.findByTeamIdWithUser(teamOpt.get().getId());
    }
    
    // 获取待审核的成员申请
    public List<TeamMember> getPendingMembers(Long teamId, Long captainId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        
        // 验证队长权限
        if (!teamMemberRepository.isTeamLeader(team.getId(), captainId)) {
            throw new RuntimeException("您不是该团队的队长");
        }
        
        return teamMemberRepository.findByTeamId(team.getId());
    }
    
    // 获取团队统计信息
    public List<Object[]> getTeamStatsByCompetition() {
        return teamRepository.countTeamsByCompetition();
    }
    
    public List<Object[]> getTeamStatsByStatus() {
        return teamRepository.countTeamsByStatus();
    }
    
    // 获取特定竞赛的团队数量
    public long getTeamCountByCompetition(Long competitionId) {
        return teamRepository.countByCompetitionId(competitionId);
    }
    
    // 私有辅助方法
    private boolean isTeamLeader(Long teamId, Long userId) {
        Optional<TeamMember> memberOpt = teamMemberRepository.findByTeamIdAndUserId(teamId, userId);
        return memberOpt.isPresent() && memberOpt.get().getRole() == TeamMember.MemberRole.LEADER;
    }
    
    private String generateInviteCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    public void deleteTeam(Long teamId, Long userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        
        // 验证权限（队长或管理员）
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        boolean isCaptain = teamMemberRepository.isTeamLeader(team.getId(), userId);
        boolean isAdmin = user.getRole() == User.UserRole.ADMIN;
        
        if (!isCaptain && !isAdmin) {
            throw new RuntimeException("没有权限删除团队");
        }
        
        // 删除团队（级联删除成员）
        teamRepository.delete(team);
    }

    // 获取用户加入的团队
    public Page<Team> getTeamsJoinedByUser(Long userId, Pageable pageable) {
        System.out.println("=== TeamService.getTeamsJoinedByUser 调用 ===");
        System.out.println("userId: " + userId);
        System.out.println("pageable: " + pageable);
        Page<Team> result = teamRepository.findTeamsJoinedByUser(userId, pageable);
        System.out.println("查询结果数量: " + result.getTotalElements());
        System.out.println("查询结果内容: " + result.getContent());
        return result;
    }
    
    public Page<Team> getTeamsCreatedByUser(Long userId, Pageable pageable) {
        return teamRepository.findTeamsCreatedByUser(userId, pageable);
    }
    
    // 通过邀请码加入团队
    public TeamMember joinTeamByInviteCode(String inviteCode, Long userId, String reason) {
        // 严格的邀请码验证和过滤
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            throw new RuntimeException("邀请码不能为空");
        }

        // 转换为大写并去除空格
        inviteCode = inviteCode.trim().toUpperCase();

        // 验证邀请码长度
        if (inviteCode.length() < 4 || inviteCode.length() > 20) {
            throw new RuntimeException("邀请码格式错误");
        }

        // 验证邀请码只包含字母数字和连字符
        if (!inviteCode.matches("^[A-Z0-9-]+$")) {
            throw new RuntimeException("邀请码格式错误，只能包含字母、数字和连字符");
        }

        // 根据邀请码查找团队
        Optional<Team> teamOpt = teamRepository.findByInviteCode(inviteCode);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("无效的邀请码");
        }

        Team team = teamOpt.get();

        // 验证团队状态
        if (team.getStatus() != Team.TeamStatus.ACTIVE) {
            throw new RuntimeException("团队未开放招募");
        }

        // 验证用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOpt.get();

        // 检查用户是否已经在该团队中
        Optional<TeamMember> existingMember = teamMemberRepository.findByTeamIdAndUserId(team.getId(), userId);
        if (existingMember.isPresent()) {
            throw new RuntimeException("您已经是该团队成员");
        }
        
        // 检查用户是否已经在该竞赛的其他团队中（排除当前团队）
        Optional<Team> userTeamInCompetition = teamRepository.findUserTeamInCompetition(userId, team.getCompetition().getId());
        if (userTeamInCompetition.isPresent() && !userTeamInCompetition.get().getId().equals(team.getId())) {
            throw new RuntimeException("您已经参加了该竞赛的其他团队");
        }
        
        // 检查团队是否已满
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            throw new RuntimeException("团队已满员");
        }
        
        // 检查竞赛状态
        Competition competition = team.getCompetition();
        if (competition.getStatus() != Competition.CompetitionStatus.REGISTRATION_OPEN) {
            throw new RuntimeException("竞赛未开放报名");
        }
        
        // 创建团队成员记录
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(user);
        member.setRole(TeamMember.MemberRole.MEMBER);
        member.setStatus(TeamMember.MemberStatus.ACTIVE); // 通过邀请码直接加入，无需审核
        member.setJoinedAt(LocalDateTime.now());
        member.setJoinReason(reason);
        
        TeamMember savedMember = teamMemberRepository.save(member);
        
        // 更新团队成员数量
        team.setCurrentMembers(team.getCurrentMembers() + 1);
        
        // 如果团队满员，更新状态
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            team.setStatus(Team.TeamStatus.FULL);
        }
        
        teamRepository.save(team);
        
        return savedMember;
    }
    
    // 获取团队邀请码
    public String getTeamInviteCode(Long teamId, Long userId) {
        // 验证团队
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        
        // 验证用户权限（必须是团队成员）
        Optional<TeamMember> memberOpt = teamMemberRepository.findByTeamIdAndUserId(teamId, userId);
        if (memberOpt.isEmpty()) {
            throw new RuntimeException("您不是该团队成员");
        }
        
        TeamMember member = memberOpt.get();
        if (member.getStatus() != TeamMember.MemberStatus.ACTIVE) {
            throw new RuntimeException("您不是活跃的团队成员");
        }
        
        // 检查团队状态
        if (team.getStatus() != Team.TeamStatus.ACTIVE) {
            throw new RuntimeException("团队未开放招募");
        }
        
        // 如果邀请码为空，生成新的邀请码
        if (team.getInviteCode() == null || team.getInviteCode().trim().isEmpty()) {
            team.setInviteCode(generateInviteCode());
            teamRepository.save(team);
        }
        
        return team.getInviteCode();
    }
    
    // 通过学号邀请成员
    public void inviteMemberByStudentId(Long teamId, String studentId, String message, Long inviterId) {
        // 验证团队
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new RuntimeException("团队不存在");
        }
        
        Team team = teamOpt.get();
        
        // 验证邀请人权限（必须是团队队长）
        Optional<TeamMember> inviterMemberOpt = teamMemberRepository.findByTeamIdAndUserId(teamId, inviterId);
        if (inviterMemberOpt.isEmpty()) {
            throw new RuntimeException("您不是该团队成员");
        }
        
        TeamMember inviterMember = inviterMemberOpt.get();
        if (inviterMember.getRole() != TeamMember.MemberRole.LEADER) {
            throw new RuntimeException("只有队长可以邀请成员");
        }
        
        // 验证团队状态
        if (team.getStatus() != Team.TeamStatus.ACTIVE) {
            throw new RuntimeException("团队未开放招募");
        }
        
        // 检查团队是否已满
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            throw new RuntimeException("团队已满员");
        }
        
        // 根据学号查找用户
        Optional<User> targetUserOpt = userRepository.findByStudentId(studentId);
        if (targetUserOpt.isEmpty()) {
            throw new RuntimeException("学号为 " + studentId + " 的用户不存在");
        }
        
        User targetUser = targetUserOpt.get();
        
        // 检查用户是否已经在该团队中
        Optional<TeamMember> existingMember = teamMemberRepository.findByTeamIdAndUserId(team.getId(), targetUser.getId());
        if (existingMember.isPresent()) {
            throw new RuntimeException("该用户已经是团队成员");
        }
        
        // 检查用户是否已经在该竞赛的其他团队中（排除当前团队）
        Optional<Team> userTeamInCompetition = teamRepository.findUserTeamInCompetition(targetUser.getId(), team.getCompetition().getId());
        if (userTeamInCompetition.isPresent() && !userTeamInCompetition.get().getId().equals(team.getId())) {
            throw new RuntimeException("该用户已经参加了该竞赛的其他团队");
        }
        
        // 检查竞赛状态
        Competition competition = team.getCompetition();
        if (competition.getStatus() != Competition.CompetitionStatus.REGISTRATION_OPEN) {
            throw new RuntimeException("竞赛未开放报名");
        }
        
        // 创建邀请记录（这里可以扩展为邀请表，目前直接加入团队）
        // 为了简化，这里直接将用户加入团队，状态为待审核
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(targetUser);
        member.setRole(TeamMember.MemberRole.MEMBER);
        member.setStatus(TeamMember.MemberStatus.PENDING); // 邀请状态，需要用户确认
        member.setJoinedAt(LocalDateTime.now());
        member.setJoinReason(message != null ? message : "队长邀请加入");
        
        teamMemberRepository.save(member);
        
        // 这里可以添加发送邀请通知的逻辑
        // 例如：notificationService.sendInviteNotification(targetUser, team, message);
    }

    // 为个人报名创建单人团队
    public Team createIndividualTeam(Competition competition, User user) {
        // 检查是否已有个人团队
        Optional<Team> existingTeam = teamRepository.findUserTeamInCompetition(user.getId(), competition.getId());
        if (existingTeam.isPresent()) {
            return existingTeam.get();
        }

        // 创建个人团队
        Team team = new Team();
        team.setName(user.getUsername() + "的个人团队");
        team.setDescription("个人报名自动创建");
        team.setCompetition(competition);
        team.setLeader(user);
        team.setStatus(Team.TeamStatus.ACTIVE);
        team.setInviteCode(generateInviteCode());
        team.setMaxMembers(1); // 个人团队只有1人
        team.setCurrentMembers(1);

        team = teamRepository.save(team);

        // 创建团队成员记录（队长）
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(user);
        member.setRole(TeamMember.MemberRole.LEADER);
        member.setStatus(TeamMember.MemberStatus.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());
        member.setJoinReason("个人报名");

        teamMemberRepository.save(member);

        return team;
    }
}