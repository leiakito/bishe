package com.example.demo.repository;

import com.example.demo.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    
    // 根据团队查找成员
    List<TeamMember> findByTeamId(Long teamId);

    // 根据团队查找成员（主动加载用户信息，避免懒加载异常）
    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user WHERE tm.team.id = :teamId")
    List<TeamMember> findByTeamIdWithUser(@Param("teamId") Long teamId);
    
    // 根据用户查找团队成员记录
    List<TeamMember> findByUserId(Long userId);
    
    // 根据团队和用户查找成员记录
    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);
    
    // 根据角色查找成员
    List<TeamMember> findByRole(TeamMember.MemberRole role);
    
    // 根据状态查找成员
    List<TeamMember> findByStatus(TeamMember.MemberStatus status);
    
    // 根据团队和角色查找成员
    List<TeamMember> findByTeamIdAndRole(Long teamId, TeamMember.MemberRole role);
    
    // 根据团队和状态查找成员
    List<TeamMember> findByTeamIdAndStatus(Long teamId, TeamMember.MemberStatus status);
    
    // 查找团队的队长
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.role = 'LEADER'")
    Optional<TeamMember> findTeamLeader(@Param("teamId") Long teamId);
    
    // 查找团队的普通成员
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.role = 'MEMBER'")
    List<TeamMember> findTeamMembers(@Param("teamId") Long teamId);
    
    // 查找团队的活跃成员
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.status = 'ACTIVE'")
    List<TeamMember> findActiveMembers(@Param("teamId") Long teamId);
    
    // 统计团队成员数量
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team.id = :teamId")
    Long countByTeamId(@Param("teamId") Long teamId);
    
    // 统计团队活跃成员数量
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.status = 'ACTIVE'")
    Long countActiveByTeamId(@Param("teamId") Long teamId);
    
    // 统计团队指定状态的成员数量
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.status = :status")
    Long countByTeamAndStatus(@Param("teamId") Long teamId, @Param("status") TeamMember.MemberStatus status);
    
    // 检查用户是否在团队中
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
    
    // 检查用户是否是团队队长
    @Query("SELECT COUNT(tm) > 0 FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.user.id = :userId AND tm.role = 'LEADER'")
    boolean isTeamLeader(@Param("teamId") Long teamId, @Param("userId") Long userId);
    
    // 检查用户是否是团队活跃成员
    @Query("SELECT COUNT(tm) > 0 FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.user.id = :userId AND tm.status = 'ACTIVE'")
    boolean isActiveMember(@Param("teamId") Long teamId, @Param("userId") Long userId);
    
    // 查找用户创建的团队（作为队长）
    @Query("SELECT tm FROM TeamMember tm WHERE tm.user.id = :userId AND tm.role = 'LEADER'")
    List<TeamMember> findTeamsLeadByUser(@Param("userId") Long userId);
    
    // 查找用户参与的团队（所有角色）
    @Query("SELECT tm FROM TeamMember tm WHERE tm.user.id = :userId AND tm.status = 'ACTIVE'")
    List<TeamMember> findActiveTeamsByUser(@Param("userId") Long userId);
    
    // 查找用户在特定竞赛中的团队成员记录
    @Query("SELECT tm FROM TeamMember tm WHERE tm.user.id = :userId AND tm.team.competition.id = :competitionId")
    Optional<TeamMember> findUserMembershipInCompetition(@Param("userId") Long userId, @Param("competitionId") Long competitionId);
    
    // 统计各角色成员数量
    @Query("SELECT tm.role, COUNT(tm) FROM TeamMember tm GROUP BY tm.role")
    List<Object[]> countMembersByRole();
    
    // 统计各状态成员数量
    @Query("SELECT tm.status, COUNT(tm) FROM TeamMember tm GROUP BY tm.status")
    List<Object[]> countMembersByStatus();
    
    // 查找最近加入的成员
    @Query("SELECT tm FROM TeamMember tm ORDER BY tm.joinedAt DESC")
    List<TeamMember> findRecentMembers(org.springframework.data.domain.Pageable pageable);
    
    // 根据团队查找成员（按加入时间排序）
    List<TeamMember> findByTeamIdOrderByJoinedAtAsc(Long teamId);
    
    // 查找待审核的成员申请
    @Query("SELECT tm FROM TeamMember tm WHERE tm.status = 'PENDING' ORDER BY tm.joinedAt ASC")
    List<TeamMember> findPendingMembers();
    
    // 查找团队的待审核成员申请
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.status = 'PENDING' ORDER BY tm.joinedAt ASC")
    List<TeamMember> findPendingMembersByTeam(@Param("teamId") Long teamId);
}