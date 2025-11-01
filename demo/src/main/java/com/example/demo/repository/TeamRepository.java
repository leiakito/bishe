package com.example.demo.repository;

import com.example.demo.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    // 获取所有团队（带关联数据）
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.competition LEFT JOIN FETCH t.leader")
    List<Team> findAllWithDetails();

    // 获取所有团队（分页，带关联数据）
    @Query(value = "SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.competition LEFT JOIN FETCH t.leader",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t")
    Page<Team> findAllWithDetails(Pageable pageable);

    // 根据团队名称查找
    Optional<Team> findByName(String name);
    
    // 根据邀请码查找团队
    Optional<Team> findByInviteCode(String inviteCode);
    
    // 根据团队名称和竞赛查找
    Optional<Team> findByNameAndCompetitionId(String name, Long competitionId);
    
    // 检查团队名称是否存在
    boolean existsByName(String name);
    
    // 检查团队名称在特定竞赛中是否存在
    boolean existsByNameAndCompetitionId(String name, Long competitionId);
    
    // 根据竞赛查找团队
    List<Team> findByCompetitionId(Long competitionId);
    
    // 根据竞赛查找团队（分页）
    Page<Team> findByCompetitionId(Long competitionId, Pageable pageable);
    
    // 根据状态查找团队
    List<Team> findByStatus(Team.TeamStatus status);
    
    // 根据状态查找团队（分页）
    Page<Team> findByStatus(Team.TeamStatus status, Pageable pageable);
    
    // 根据竞赛和状态查找团队
    List<Team> findByCompetitionIdAndStatus(Long competitionId, Team.TeamStatus status);
    
    // 模糊搜索团队（团队名称、描述）
    @Query("SELECT t FROM Team t WHERE t.name LIKE %:keyword% OR t.description LIKE %:keyword%")
    Page<Team> searchTeams(@Param("keyword") String keyword, Pageable pageable);
    
    // 查找用户创建的团队
    @Query("SELECT t FROM Team t WHERE t.id IN (SELECT tm.team.id FROM TeamMember tm WHERE tm.user.id = :userId AND tm.role = 'LEADER')")
    List<Team> findTeamsCreatedByUser(@Param("userId") Long userId);
    
    // 查找用户参与的团队
    @Query("SELECT t FROM Team t WHERE t.id IN (SELECT tm.team.id FROM TeamMember tm WHERE tm.user.id = :userId)")
    List<Team> findTeamsByUser(@Param("userId") Long userId);
    
    // 查找用户在特定竞赛中的团队（只查找活跃成员）
    @Query("SELECT t FROM Team t WHERE t.competition.id = :competitionId AND t.id IN (SELECT tm.team.id FROM TeamMember tm WHERE tm.user.id = :userId AND tm.status = 'ACTIVE')")
    Optional<Team> findUserTeamInCompetition(@Param("userId") Long userId, @Param("competitionId") Long competitionId);
    
    // 查找用户在特定竞赛中的所有团队（只查找活跃成员）
    @Query("SELECT t FROM Team t WHERE t.competition.id = :competitionId AND t.id IN (SELECT tm.team.id FROM TeamMember tm WHERE tm.user.id = :userId AND tm.status = 'ACTIVE')")
    List<Team> findAllUserTeamsInCompetition(@Param("userId") Long userId, @Param("competitionId") Long competitionId);
    
    // 查找团队成员数量
    @Query("SELECT t, COUNT(tm) FROM Team t LEFT JOIN t.members tm WHERE t.id = :teamId GROUP BY t")
    Object[] findTeamWithMemberCount(@Param("teamId") Long teamId);
    
    // 查找未满员的团队
    @Query("SELECT t FROM Team t LEFT JOIN t.members tm GROUP BY t HAVING COUNT(tm) < t.maxMembers")
    List<Team> findTeamsWithAvailableSlots();
    
    // 查找特定竞赛中未满员的团队
    @Query("SELECT t FROM Team t LEFT JOIN t.members tm WHERE t.competition.id = :competitionId GROUP BY t HAVING COUNT(tm) < t.maxMembers")
    List<Team> findAvailableTeamsInCompetition(@Param("competitionId") Long competitionId);

    // 查找未满员的团队（带关联数据）
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.competition LEFT JOIN FETCH t.leader LEFT JOIN t.members tm WHERE t.competition.id = :competitionId GROUP BY t.id HAVING COUNT(tm) < t.maxMembers OR t.currentMembers < t.maxMembers")
    List<Team> findAvailableTeamsInCompetitionWithDetails(@Param("competitionId") Long competitionId);

    // 查找所有未满员的团队（带关联数据）
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.competition LEFT JOIN FETCH t.leader WHERE t.currentMembers < t.maxMembers AND t.status = 'ACTIVE'")
    List<Team> findAllAvailableTeamsWithDetails();
    
    // 统计各状态团队数量
    @Query("SELECT t.status, COUNT(t) FROM Team t GROUP BY t.status")
    List<Object[]> countTeamsByStatus();
    
    // 统计各竞赛的团队数量
    @Query("SELECT t.competition.id, t.competition.name, COUNT(t) FROM Team t GROUP BY t.competition.id, t.competition.name")
    List<Object[]> countTeamsByCompetition();
    
    // 统计特定竞赛的团队数量
    @Query("SELECT COUNT(t) FROM Team t WHERE t.competition.id = :competitionId")
    Long countByCompetitionId(@Param("competitionId") Long competitionId);
    
    // 查找最近创建的团队
    @Query("SELECT t FROM Team t ORDER BY t.createdAt DESC")
    List<Team> findRecentTeams(Pageable pageable);
    
    // 根据竞赛查找活跃团队（已报名且状态为ACTIVE）
    @Query("SELECT t FROM Team t WHERE t.competition.id = :competitionId AND t.status = 'ACTIVE' AND EXISTS (SELECT r FROM Registration r WHERE r.team = t AND r.status = 'APPROVED')")
    List<Team> findActiveTeamsInCompetition(@Param("competitionId") Long competitionId);
    
    // 查找有成绩的团队
    @Query("SELECT DISTINCT t FROM Team t WHERE EXISTS (SELECT g FROM Grade g WHERE g.team = t)")
    List<Team> findTeamsWithGrades();
    
    // 根据竞赛查找有成绩的团队
    @Query("SELECT DISTINCT t FROM Team t WHERE t.competition.id = :competitionId AND EXISTS (SELECT g FROM Grade g WHERE g.team = t AND g.competition.id = :competitionId)")
    List<Team> findTeamsWithGradesInCompetition(@Param("competitionId") Long competitionId);
    
    // 查找用户加入的团队（分页）
    @Query(value = "SELECT DISTINCT t.* FROM teams t " +
           "INNER JOIN team_members tm ON t.id = tm.team_id " +
           "WHERE tm.user_id = :userId AND tm.status = 'ACTIVE'",
           countQuery = "SELECT COUNT(DISTINCT t.id) FROM teams t " +
           "INNER JOIN team_members tm ON t.id = tm.team_id " +
           "WHERE tm.user_id = :userId AND tm.status = 'ACTIVE'",
           nativeQuery = true)
    Page<Team> findTeamsJoinedByUser(@Param("userId") Long userId, Pageable pageable);
    
    // 查找用户创建的团队
    @Query("SELECT t FROM Team t WHERE t.leader.id = :userId")
    Page<Team> findTeamsCreatedByUser(@Param("userId") Long userId, Pageable pageable);

    // 通过报名记录查找竞赛的所有团队（包括直接关联和通过报名关联的）
    // 使用JPQL主动加载leader，用于教师管理团队列表
    @Query("SELECT DISTINCT t FROM Team t " +
           "LEFT JOIN FETCH t.leader " +
           "LEFT JOIN FETCH t.competition " +
           "WHERE t.competition.id = :competitionId " +
           "ORDER BY t.createdAt DESC")
    List<Team> findTeamsByCompetitionWithLeader(@Param("competitionId") Long competitionId);

    @Query(value = "SELECT DISTINCT t.* FROM teams t " +
           "LEFT JOIN registrations r ON r.team_id = t.id " +
           "WHERE t.competition_id = :competitionId OR r.competition_id = :competitionId " +
           "ORDER BY t.created_at DESC",
           countQuery = "SELECT COUNT(DISTINCT t.id) FROM teams t " +
           "LEFT JOIN registrations r ON r.team_id = t.id " +
           "WHERE t.competition_id = :competitionId OR r.competition_id = :competitionId",
           nativeQuery = true)
    Page<Team> findTeamsByCompetitionIncludingRegistrations(@Param("competitionId") Long competitionId, Pageable pageable);

    // 通过报名记录查找竞赛的所有团队（仅通过报名记录关联）
    @Query(value = "SELECT DISTINCT t.* FROM teams t " +
           "INNER JOIN registrations r ON r.team_id = t.id " +
           "WHERE r.competition_id = :competitionId " +
           "ORDER BY t.created_at DESC",
           countQuery = "SELECT COUNT(DISTINCT t.id) FROM teams t " +
           "INNER JOIN registrations r ON r.team_id = t.id " +
           "WHERE r.competition_id = :competitionId",
           nativeQuery = true)
    Page<Team> findRegisteredTeamsByCompetition(@Param("competitionId") Long competitionId, Pageable pageable);
    
    // 删除竞赛的所有团队
    void deleteByCompetitionId(Long competitionId);
}