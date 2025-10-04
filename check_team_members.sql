-- 检查团队和成员数据
USE competition_system;

-- 查看最近创建的团队
SELECT
    t.id as team_id,
    t.name as team_name,
    t.status as team_status,
    t.current_members,
    t.max_members,
    t.created_at,
    u.id as leader_id,
    u.username as leader_name
FROM team t
LEFT JOIN user u ON t.leader_id = u.id
ORDER BY t.created_at DESC
LIMIT 10;

-- 查看团队成员关系
SELECT
    tm.id as member_id,
    tm.team_id,
    t.name as team_name,
    tm.user_id,
    u.username,
    tm.role,
    tm.status,
    tm.joined_at
FROM team_member tm
LEFT JOIN team t ON tm.team_id = t.id
LEFT JOIN user u ON tm.user_id = u.id
ORDER BY tm.joined_at DESC
LIMIT 10;

-- 查看特定用户的团队（替换YOUR_USER_ID为实际用户ID）
-- SELECT t.*
-- FROM team t
-- JOIN team_member tm ON t.id = tm.team_id
-- WHERE tm.user_id = YOUR_USER_ID
-- AND tm.status = 'ACTIVE';
