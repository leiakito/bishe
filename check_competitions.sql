-- 检查竞赛数据
USE competition_system;

-- 查看竞赛表结构
DESCRIBE competition;

-- 查看竞赛总数
SELECT COUNT(*) as total_competitions FROM competition;

-- 查看所有竞赛数据
SELECT
    id,
    name,
    category,
    status,
    start_date,
    end_date,
    created_at
FROM competition
ORDER BY created_at DESC
LIMIT 20;

-- 按状态分组统计
SELECT
    status,
    COUNT(*) as count
FROM competition
GROUP BY status;

-- 按类别分组统计
SELECT
    category,
    COUNT(*) as count
FROM competition
GROUP BY category;
