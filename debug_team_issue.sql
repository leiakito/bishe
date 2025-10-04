-- 调试团队显示问题
USE competition_system;

-- 1. 检查表名（是team还是teams）
SHOW TABLES LIKE '%team%';

-- 2. 查看最近创建的团队（尝试两种表名）
-- 如果表名是 team：
SELECT
    t.id as team_id,
    t.name as team_name,
    t.status as team_status,
    t.current_members,
    t.max_members,
    t.created_at,
    t.leader_id
FROM team t
ORDER BY t.created_at DESC
LIMIT 5;

-- 如果上面的查询失败，尝试 teams：
-- SELECT
--     t.id as team_id,
--     t.name as team_name,
--     t.status as team_status,
--     t.current_members,
--     t.max_members,
--     t.created_at,
--     t.leader_id
-- FROM teams t
-- ORDER BY t.created_at DESC
-- LIMIT 5;

-- 3. 查看团队成员关系
SELECT
    tm.id as member_id,
    tm.team_id,
    tm.user_id,
    tm.role,
    tm.status,
    tm.joined_at
FROM team_member tm
ORDER BY tm.joined_at DESC
LIMIT 10;

-- 4. 检查是否有ACTIVE状态的团队成员
SELECT COUNT(*) as active_members_count
FROM team_member tm
WHERE tm.status = 'ACTIVE';

-- 5. 查看特定用户的团队成员记录（请替换1为你的实际用户ID）
SELECT
    tm.id,
    tm.team_id,
    tm.user_id,
    tm.role,
    tm.status,
    tm.joined_at
FROM team_member tm
WHERE tm.user_id = 1;  -- 替换为你的用户ID
