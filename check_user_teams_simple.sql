-- 检查用户64的团队数据（简化版，不连接competition表）
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
    t.competition_id
FROM teams t
WHERE t.leader_id = 64
ORDER BY t.created_at DESC;

-- 2. 查看用户64的所有团队成员记录
SELECT
    tm.id as member_id,
    tm.team_id,
    tm.user_id,
    tm.role,
    tm.status,
    tm.joined_at
FROM team_members tm
WHERE tm.user_id = 64
ORDER BY tm.joined_at DESC;

-- 3. 查看最近创建的所有团队
SELECT
    t.id,
    t.name,
    t.leader_id,
    t.status,
    t.current_members,
    t.created_at
FROM teams t
ORDER BY t.created_at DESC
LIMIT 10;

-- 4. 查看所有team_members记录
SELECT
    tm.id,
    tm.team_id,
    tm.user_id,
    tm.role,
    tm.status,
    tm.joined_at
FROM team_members tm
ORDER BY tm.joined_at DESC
LIMIT 10;
