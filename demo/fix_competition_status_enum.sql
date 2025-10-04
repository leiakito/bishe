-- 修复竞赛状态枚举不匹配问题
-- 将数据库中的 status 字段枚举值与 Java 实体类中的 CompetitionStatus 枚举完全匹配

USE competition_system;

-- 首先查看当前表结构
SELECT COLUMN_NAME, COLUMN_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'competition_system' 
  AND TABLE_NAME = 'competitions' 
  AND COLUMN_NAME = 'status';

-- 修改 status 字段的枚举值，确保包含所有 Java 实体类中定义的状态
ALTER TABLE competitions 
MODIFY COLUMN status ENUM(
    'DRAFT', 
    'PUBLISHED', 
    'REGISTRATION_OPEN', 
    'REGISTRATION_CLOSED', 
    'IN_PROGRESS', 
    'ONGOING', 
    'COMPLETED', 
    'CANCELLED', 
    'PENDING_APPROVAL'
) NOT NULL DEFAULT 'DRAFT';

-- 验证修改结果
SELECT COLUMN_NAME, COLUMN_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'competition_system' 
  AND TABLE_NAME = 'competitions' 
  AND COLUMN_NAME = 'status';

-- 查看当前数据库中的状态分布
SELECT status, COUNT(*) as count 
FROM competitions 
GROUP BY status 
ORDER BY count DESC;