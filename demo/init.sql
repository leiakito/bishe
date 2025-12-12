-- ============================================
-- 北城竞赛管理系统数据库初始化脚本
-- ============================================
-- 作者: 系统自动生成
-- 数据库: competition_system
-- 字符集: UTF-8
-- 时区: Asia/Shanghai
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS competition_system 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE competition_system;

-- ============================================
-- 1. 用户表 (users)
-- 存储系统所有用户信息（管理员、教师、学生）
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    real_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
    phone_number VARCHAR(20) COMMENT '手机号码',
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL COMMENT '用户角色',
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'DISABLED') NOT NULL DEFAULT 'PENDING' COMMENT '用户状态',
    school_name VARCHAR(200) COMMENT '学校名称',
    student_id VARCHAR(50) UNIQUE COMMENT '学号',
    teacher_id VARCHAR(50) COMMENT '教师工号',
    department VARCHAR(100) COMMENT '院系/部门',
    employee_id VARCHAR(50) COMMENT '员工ID',
    title VARCHAR(100) COMMENT '职称',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 竞赛表 (competitions)
-- 存储竞赛基本信息
-- ============================================
CREATE TABLE IF NOT EXISTS competitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '竞赛ID',
    name VARCHAR(200) NOT NULL COMMENT '竞赛名称',
    description TEXT COMMENT '竞赛描述',
    category VARCHAR(100) NOT NULL COMMENT '竞赛类别',
    level ENUM('SCHOOL', 'CITY', 'PROVINCE', 'NATIONAL', 'INTERNATIONAL') NOT NULL COMMENT '竞赛级别',
    status ENUM('DRAFT', 'PUBLISHED', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'IN_PROGRESS', 'ONGOING', 'COMPLETED', 'CANCELLED', 'PENDING_APPROVAL') NOT NULL DEFAULT 'DRAFT' COMMENT '竞赛状态',
    registration_start_time DATETIME NOT NULL COMMENT '报名开始时间',
    registration_end_time DATETIME NOT NULL COMMENT '报名结束时间',
    competition_start_time DATETIME NOT NULL COMMENT '竞赛开始时间',
    competition_end_time DATETIME NOT NULL COMMENT '竞赛结束时间',
    max_team_size INT DEFAULT 1 COMMENT '团队最大人数',
    min_team_size INT DEFAULT 1 COMMENT '团队最小人数',
    max_teams INT COMMENT '最大参赛队伍数',
    registration_fee DOUBLE DEFAULT 0.0 COMMENT '报名费用',
    prize_info TEXT COMMENT '奖项信息',
    rules TEXT COMMENT '竞赛规则',
    contact_info VARCHAR(255) COMMENT '联系方式',
    poster_url VARCHAR(500) COMMENT '竞赛海报URL',
    attachment_urls TEXT COMMENT '附件URLs（JSON格式）',
    location VARCHAR(255) COMMENT '竞赛地点',
    organizer VARCHAR(200) COMMENT '主办方',
    competition_number VARCHAR(50) UNIQUE COMMENT '竞赛编号',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    registration_count INT DEFAULT 0 COMMENT '报名人数',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_level (level),
    INDEX idx_status (status),
    INDEX idx_creator_id (creator_id),
    INDEX idx_competition_number (competition_number),
    INDEX idx_registration_time (registration_start_time, registration_end_time),
    INDEX idx_competition_time (competition_start_time, competition_end_time),
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛表';

-- ============================================
-- 2.1 竞赛分类表 (competition_categories)
-- 维护可配置的竞赛分类
-- ============================================
CREATE TABLE IF NOT EXISTS competition_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '分类编码（大写）',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '描述',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_competition_category_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛分类表';

INSERT IGNORE INTO competition_categories (code, name, description, status)
VALUES 
('PROGRAMMING', '程序设计', '内置分类', 'ACTIVE'),
('MATHEMATICS', '数学竞赛', '内置分类', 'ACTIVE'),
('PHYSICS', '物理竞赛', '内置分类', 'ACTIVE'),
('CHEMISTRY', '化学竞赛', '内置分类', 'ACTIVE'),
('BIOLOGY', '生物竞赛', '内置分类', 'ACTIVE'),
('ENGLISH', '英语竞赛', '内置分类', 'ACTIVE'),
('DESIGN', '设计竞赛', '内置分类', 'ACTIVE'),
('INNOVATION', '创新创业', '内置分类', 'ACTIVE'),
('OTHER', '其他', '内置分类', 'ACTIVE');

-- ============================================
-- 3. 团队表 (teams)
-- 存储参赛团队信息
-- ============================================
CREATE TABLE IF NOT EXISTS teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '团队ID',
    name VARCHAR(100) NOT NULL COMMENT '团队名称',
    description TEXT COMMENT '团队描述',
    status ENUM('ACTIVE', 'DISBANDED', 'COMPLETED', 'FULL') NOT NULL DEFAULT 'ACTIVE' COMMENT '团队状态',
    invite_code VARCHAR(50) UNIQUE COMMENT '邀请码',
    max_members INT COMMENT '最大成员数',
    current_members INT DEFAULT 1 COMMENT '当前成员数',
    competition_id BIGINT NOT NULL COMMENT '所属竞赛ID',
    leader_id BIGINT NOT NULL COMMENT '队长ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_competition_id (competition_id),
    INDEX idx_leader_id (leader_id),
    INDEX idx_invite_code (invite_code),
    INDEX idx_status (status),
    FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE,
    FOREIGN KEY (leader_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队表';

-- ============================================
-- 4. 团队成员表 (team_members)
-- 存储团队成员关系
-- ============================================
CREATE TABLE IF NOT EXISTS team_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成员ID',
    team_id BIGINT NOT NULL COMMENT '团队ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('LEADER', 'MEMBER') NOT NULL DEFAULT 'MEMBER' COMMENT '成员角色',
    status ENUM('PENDING', 'ACTIVE', 'INACTIVE', 'REMOVED') NOT NULL DEFAULT 'ACTIVE' COMMENT '成员状态',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    left_at DATETIME COMMENT '离开时间',
    join_reason VARCHAR(500) COMMENT '加入原因',
    INDEX idx_team_id (team_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    UNIQUE KEY unique_team_user (team_id, user_id),
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员表';

-- ============================================
-- 5. 报名表 (registrations)
-- 存储竞赛报名信息
-- ============================================
CREATE TABLE IF NOT EXISTS registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报名ID',
    competition_id BIGINT NOT NULL COMMENT '竞赛ID',
    team_id BIGINT NOT NULL COMMENT '团队ID',
    submitted_by BIGINT NOT NULL COMMENT '提交人ID',
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '报名状态',
    registration_number VARCHAR(50) UNIQUE COMMENT '报名编号',
    payment_status ENUM('UNPAID', 'PAID', 'REFUNDED', 'NOT_REQUIRED') DEFAULT 'UNPAID' COMMENT '支付状态',
    payment_amount DOUBLE COMMENT '支付金额',
    payment_time DATETIME COMMENT '支付时间',
    remarks TEXT COMMENT '备注',
    review_remarks TEXT COMMENT '审核备注',
    reviewed_by BIGINT COMMENT '审核人ID',
    reviewed_at DATETIME COMMENT '审核时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_competition_id (competition_id),
    INDEX idx_team_id (team_id),
    INDEX idx_submitted_by (submitted_by),
    INDEX idx_status (status),
    INDEX idx_registration_number (registration_number),
    FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (submitted_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名表';

-- ============================================
-- 6. 题库表 (questions)
-- 存储竞赛题目信息
-- ============================================
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    title VARCHAR(500) NOT NULL COMMENT '题目标题',
    content TEXT NOT NULL COMMENT '题目内容',
    type ENUM('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE', 'FILL_BLANK', 'SHORT_ANSWER', 'ESSAY', 'PROGRAMMING') NOT NULL COMMENT '题目类型',
    difficulty ENUM('EASY', 'MEDIUM', 'HARD', 'EXPERT') NOT NULL COMMENT '题目难度',
    category ENUM('PROGRAMMING', 'MATHEMATICS', 'PHYSICS', 'CHEMISTRY', 'BIOLOGY', 'ENGLISH', 'COMPUTER_SCIENCE', 'DATA_STRUCTURE', 'ALGORITHM', 'DATABASE', 'NETWORK', 'SOFTWARE_ENGINEERING', 'OPERATING_SYSTEM', 'COMPUTER_ARCHITECTURE', 'COMPILER_PRINCIPLE', 'ARTIFICIAL_INTELLIGENCE', 'MACHINE_LEARNING', 'WEB_DEVELOPMENT', 'MOBILE_DEVELOPMENT', 'CLOUD_COMPUTING', 'CYBERSECURITY', 'PYTHON', 'JAVA', 'JAVASCRIPT', 'CPP', 'C', 'CSHARP', 'GO', 'RUST', 'PHP', 'RUBY', 'OTHER') NOT NULL COMMENT '题目分类',
    options TEXT COMMENT '选项（JSON格式）',
    correct_answer TEXT COMMENT '正确答案',
    explanation TEXT COMMENT '答案解析',
    score INT NOT NULL DEFAULT 10 COMMENT '题目分值',
    tags VARCHAR(500) COMMENT '题目标签（逗号分隔）',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT' COMMENT '题目状态',
    usage_count INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_type (type),
    INDEX idx_difficulty (difficulty),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by),
    FULLTEXT INDEX idx_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库表';

-- ============================================
-- 7. 竞赛题目关联表 (competition_questions)
-- 存储竞赛与题目的多对多关系
-- ============================================
CREATE TABLE IF NOT EXISTS competition_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    competition_id BIGINT NOT NULL COMMENT '竞赛ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    question_order INT NOT NULL COMMENT '题目顺序',
    question_score DECIMAL(10, 2) NOT NULL COMMENT '题目分值',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_competition_id (competition_id),
    INDEX idx_question_id (question_id),
    INDEX idx_order (question_order),
    UNIQUE KEY unique_competition_question (competition_id, question_id),
    FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛题目关联表';

-- ============================================
-- 8. 考卷表 (exam_papers)
-- 存储学生或团队的考试卷
-- ============================================
CREATE TABLE IF NOT EXISTS exam_papers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '考卷ID',
    competition_id BIGINT NOT NULL COMMENT '竞赛ID',
    participant_type ENUM('INDIVIDUAL', 'TEAM') NOT NULL COMMENT '参赛类型',
    participant_id BIGINT NOT NULL COMMENT '参赛者ID（学生ID或团队ID）',
    paper_status ENUM('NOT_STARTED', 'IN_PROGRESS', 'SUBMITTED', 'GRADING', 'GRADED') NOT NULL DEFAULT 'NOT_STARTED' COMMENT '考卷状态',
    start_time DATETIME COMMENT '开始时间',
    submit_time DATETIME COMMENT '提交时间',
    total_score DECIMAL(10, 2) DEFAULT 0.00 COMMENT '总分',
    objective_score DECIMAL(10, 2) DEFAULT 0.00 COMMENT '客观题得分',
    subjective_score DECIMAL(10, 2) DEFAULT 0.00 COMMENT '主观题得分',
    correct_count INT DEFAULT 0 COMMENT '正确题数',
    total_question_count INT DEFAULT 0 COMMENT '总题数',
    graded_by BIGINT COMMENT '评卷人ID',
    graded_at DATETIME COMMENT '评卷时间',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_competition_id (competition_id),
    INDEX idx_participant (participant_type, participant_id),
    INDEX idx_status (paper_status),
    UNIQUE KEY unique_competition_participant (competition_id, participant_type, participant_id),
    FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考卷表';

-- ============================================
-- 9. 答题记录表 (exam_answers)
-- 存储学生对每道题的答案
-- ============================================
CREATE TABLE IF NOT EXISTS exam_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '答题记录ID',
    exam_paper_id BIGINT NOT NULL COMMENT '考卷ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    answer_content TEXT COMMENT '答题内容',
    is_correct BOOLEAN COMMENT '是否正确',
    score DECIMAL(10, 2) DEFAULT 0.00 COMMENT '得分',
    max_score DECIMAL(10, 2) COMMENT '满分',
    grading_status ENUM('PENDING', 'AUTO_GRADED', 'MANUAL_GRADING', 'MANUAL_GRADED', 'COMPLETED') NOT NULL DEFAULT 'PENDING' COMMENT '评分状态',
    grading_remarks TEXT COMMENT '评分备注',
    graded_by BIGINT COMMENT '评分人ID',
    graded_at DATETIME COMMENT '评分时间',
    time_spent INT COMMENT '答题耗时（秒）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_exam_paper_id (exam_paper_id),
    INDEX idx_question_id (question_id),
    INDEX idx_grading_status (grading_status),
    UNIQUE KEY unique_paper_question (exam_paper_id, question_id),
    FOREIGN KEY (exam_paper_id) REFERENCES exam_papers(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题记录表';

-- ============================================
-- 10. 成绩表 (grades)
-- 存储团队或个人的最终成绩
-- ============================================
CREATE TABLE IF NOT EXISTS grades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成绩ID',
    team_id BIGINT NOT NULL COMMENT '团队ID',
    competition_id BIGINT NOT NULL COMMENT '竞赛ID',
    score DECIMAL(10, 2) COMMENT '分数',
    ranking INT COMMENT '排名',
    award_level ENUM('FIRST_PRIZE', 'SECOND_PRIZE', 'THIRD_PRIZE', 'EXCELLENCE', 'PARTICIPATION', 'NONE') COMMENT '奖项等级',
    certificate_url VARCHAR(500) COMMENT '证书URL',
    remarks TEXT COMMENT '备注',
    is_final BOOLEAN DEFAULT FALSE COMMENT '是否为最终成绩',
    graded_by BIGINT COMMENT '评分人ID',
    graded_at DATETIME COMMENT '评分时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_team_id (team_id),
    INDEX idx_competition_id (competition_id),
    INDEX idx_ranking (ranking),
    INDEX idx_award_level (award_level),
    UNIQUE KEY unique_team_competition (team_id, competition_id),
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩表';

-- ============================================
-- 11. 竞赛审核日志表 (competition_audit_logs)
-- 存储竞赛审核记录
-- ============================================
CREATE TABLE IF NOT EXISTS competition_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    competition_id BIGINT NOT NULL COMMENT '竞赛ID',
    reviewer_id BIGINT NOT NULL COMMENT '审核人ID',
    action ENUM('APPROVE', 'REJECT', 'MODIFY', 'SUBMIT') NOT NULL COMMENT '审核操作',
    old_status ENUM('DRAFT', 'PUBLISHED', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'IN_PROGRESS', 'ONGOING', 'COMPLETED', 'CANCELLED', 'PENDING_APPROVAL') COMMENT '旧状态',
    new_status ENUM('DRAFT', 'PUBLISHED', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'IN_PROGRESS', 'ONGOING', 'COMPLETED', 'CANCELLED', 'PENDING_APPROVAL') COMMENT '新状态',
    remarks TEXT COMMENT '审核备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_competition_id (competition_id),
    INDEX idx_reviewer_id (reviewer_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛审核日志表';

-- ============================================
-- 12. 系统通知表 (systeminform)
-- 存储系统通知信息
-- ============================================
CREATE TABLE IF NOT EXISTS systeminform (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    content VARCHAR(1000) NOT NULL COMMENT '通知内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入默认管理员账户
-- 密码: admin123 (需要使用BCrypt加密后的值替换)
INSERT INTO users (username, password, email, real_name, role, status, created_at, updated_at) 
VALUES 
('admin', '$2a$10$3J5QJ5QJ5QJ5QJ5QJ5QJ5u', 'admin@example.com', '系统管理员', 'ADMIN', 'APPROVED', NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;

-- 插入示例系统通知
INSERT INTO systeminform (content, created_at, updated_at) 
VALUES 
('欢迎使用北城竞赛管理系统！系统已成功初始化。', NOW(), NOW()),
('请及时关注竞赛报名时间，不要错过报名机会。', NOW(), NOW())
ON DUPLICATE KEY UPDATE content=content;

-- ============================================
-- 视图定义
-- ============================================

-- 创建竞赛统计视图
CREATE OR REPLACE VIEW view_competition_statistics AS
SELECT 
    c.id AS competition_id,
    c.name AS competition_name,
    c.status AS competition_status,
    c.category,
    c.level,
    COUNT(DISTINCT t.id) AS team_count,
    COUNT(DISTINCT tm.user_id) AS participant_count,
    COUNT(DISTINCT r.id) AS registration_count,
    COUNT(DISTINCT CASE WHEN r.status = 'APPROVED' THEN r.id END) AS approved_registration_count,
    c.created_at,
    u.real_name AS creator_name
FROM competitions c
LEFT JOIN teams t ON c.id = t.competition_id
LEFT JOIN team_members tm ON t.id = tm.team_id AND tm.status = 'ACTIVE'
LEFT JOIN registrations r ON c.id = r.competition_id
LEFT JOIN users u ON c.creator_id = u.id
GROUP BY c.id;

-- 创建用户竞赛参与视图
CREATE OR REPLACE VIEW view_user_competition_participation AS
SELECT 
    u.id AS user_id,
    u.username,
    u.real_name,
    u.role,
    COUNT(DISTINCT tm.team_id) AS joined_team_count,
    COUNT(DISTINCT t.competition_id) AS participated_competition_count,
    COUNT(DISTINCT CASE WHEN c.status = 'COMPLETED' THEN t.competition_id END) AS completed_competition_count
FROM users u
LEFT JOIN team_members tm ON u.id = tm.user_id AND tm.status = 'ACTIVE'
LEFT JOIN teams t ON tm.team_id = t.id
LEFT JOIN competitions c ON t.competition_id = c.id
WHERE u.role = 'STUDENT'
GROUP BY u.id;

-- 创建成绩排行视图
CREATE OR REPLACE VIEW view_grade_rankings AS
SELECT 
    g.id AS grade_id,
    g.competition_id,
    c.name AS competition_name,
    g.team_id,
    t.name AS team_name,
    u.real_name AS leader_name,
    g.score,
    g.ranking,
    g.award_level,
    g.is_final
FROM grades g
INNER JOIN competitions c ON g.competition_id = c.id
INNER JOIN teams t ON g.team_id = t.id
INNER JOIN users u ON t.leader_id = u.id
WHERE g.is_final = TRUE
ORDER BY g.competition_id, g.ranking;

-- ============================================
-- 存储过程
-- ============================================

-- 创建自动更新团队成员数的存储过程
DELIMITER //

CREATE PROCEDURE update_team_member_count(IN p_team_id BIGINT)
BEGIN
    UPDATE teams 
    SET current_members = (
        SELECT COUNT(*) 
        FROM team_members 
        WHERE team_id = p_team_id AND status = 'ACTIVE'
    )
    WHERE id = p_team_id;
END //

-- 创建自动更新竞赛报名数的存储过程
CREATE PROCEDURE update_competition_registration_count(IN p_competition_id BIGINT)
BEGIN
    UPDATE competitions 
    SET registration_count = (
        SELECT COUNT(*) 
        FROM registrations 
        WHERE competition_id = p_competition_id AND status = 'APPROVED'
    )
    WHERE id = p_competition_id;
END //

DELIMITER ;

-- ============================================
-- 触发器
-- ============================================

-- 团队成员变更时自动更新团队成员数
DELIMITER //

CREATE TRIGGER after_team_member_insert
AFTER INSERT ON team_members
FOR EACH ROW
BEGIN
    CALL update_team_member_count(NEW.team_id);
END //

CREATE TRIGGER after_team_member_update
AFTER UPDATE ON team_members
FOR EACH ROW
BEGIN
    CALL update_team_member_count(NEW.team_id);
END //

CREATE TRIGGER after_team_member_delete
AFTER DELETE ON team_members
FOR EACH ROW
BEGIN
    CALL update_team_member_count(OLD.team_id);
END //

-- 报名状态变更时自动更新竞赛报名数
CREATE TRIGGER after_registration_insert
AFTER INSERT ON registrations
FOR EACH ROW
BEGIN
    IF NEW.status = 'APPROVED' THEN
        CALL update_competition_registration_count(NEW.competition_id);
    END IF;
END //

CREATE TRIGGER after_registration_update
AFTER UPDATE ON registrations
FOR EACH ROW
BEGIN
    IF NEW.status != OLD.status THEN
        CALL update_competition_registration_count(NEW.competition_id);
    END IF;
END //

DELIMITER ;

-- ============================================
-- 权限配置（可选）
-- ============================================
-- 如果需要创建独立的数据库用户，取消以下注释并修改密码

-- CREATE USER IF NOT EXISTS 'competition_user'@'localhost' IDENTIFIED BY 'your_secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON competition_system.* TO 'competition_user'@'localhost';
-- FLUSH PRIVILEGES;

-- ============================================
-- 完成
-- ============================================
SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('共创建 ', COUNT(*), ' 张表') AS table_count 
FROM information_schema.tables 
WHERE table_schema = 'competition_system' AND table_type = 'BASE TABLE';
