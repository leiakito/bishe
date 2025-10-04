-- 检查用户64的团队数据
USE competition_system;

-- 1. 查看用户64创建的所有团队
SELECT
    t.id as team_id,
    t.name as team_name,
    t.status as team_status,
    t.current_members,
    t.max_members,
    t.created_at,
    t.leader_id,
    c.name as competition_name
FROM teams t
LEFT JOIN competition c ON t.competition_id = c.id
WHERE t.leader_id = 64
ORDER BY t.created_at DESC;

-- 2. 查看用户64的所有团队成员记录
SELECT
    tm.id as member_id,
    tm.team_id,
    t.name as team_name,
    tm.user_id,
    tm.role,
    tm.status,
    tm.joined_at
FROM team_members tm
LEFT JOIN teams t ON tm.team_id = t.id
WHERE tm.user_id = 64
ORDER BY tm.joined_at DESC;

-- 3. 查看所有团队及其成员数量
SELECT
    t.id,
    t.name,
    t.leader_id,
    COUNT(tm.id) as member_count,
    t.current_members
FROM teams t
LEFT JOIN team_members tm ON t.id = tm.team_id AND tm.status = 'ACTIVE'
GROUP BY t.id, t.name, t.leader_id, t.current_members
ORDER BY t.created_at DESC
LIMIT 10;
