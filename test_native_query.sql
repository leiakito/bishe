-- 测试原生SQL查询（与后端完全一致）
USE competition_system;

-- 这是后端使用的完全相同的查询
SELECT DISTINCT t.*
FROM teams t
INNER JOIN team_members tm ON t.id = tm.team_id
WHERE tm.user_id = 64 AND tm.status = 'ACTIVE'
ORDER BY t.created_at DESC;

-- 查看查询返回的列
SELECT
    t.id,
    t.name,
    t.description,
    t.status,
    t.invite_code,
    t.max_members,
    t.current_members,
    t.created_at,
    t.updated_at,
    t.competition_id,
    t.leader_id
FROM teams t
INNER JOIN team_members tm ON t.id = tm.team_id
WHERE tm.user_id = 64 AND tm.status = 'ACTIVE';
