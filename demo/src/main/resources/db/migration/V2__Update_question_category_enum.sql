-- 修改 questions 表的 category 列以支持新的编程语言分类
-- 将 ENUM 类型改为 VARCHAR 以支持更多值

ALTER TABLE questions MODIFY COLUMN category VARCHAR(50) NOT NULL;

-- 如果需要，也可以更新现有数据
-- UPDATE questions SET category = 'PYTHON' WHERE category = 'PROGRAMMING' AND tags LIKE '%Python%';
