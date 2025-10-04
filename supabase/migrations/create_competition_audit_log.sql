-- 创建竞赛审核日志表
CREATE TABLE IF NOT EXISTS competition_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_competition_id (competition_id),
    INDEX idx_reviewer_id (reviewer_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (competition_id) REFERENCES competition(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES user(id) ON DELETE CASCADE
);

-- 添加注释
ALTER TABLE competition_audit_log COMMENT = '竞赛审核日志表';
ALTER TABLE competition_audit_log MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE competition_audit_log MODIFY COLUMN competition_id BIGINT NOT NULL COMMENT '竞赛ID';
ALTER TABLE competition_audit_log MODIFY COLUMN reviewer_id BIGINT NOT NULL COMMENT '审核员ID';
ALTER TABLE competition_audit_log MODIFY COLUMN action VARCHAR(20) NOT NULL COMMENT '审核操作：APPROVE-通过，REJECT-拒绝';
ALTER TABLE competition_audit_log MODIFY COLUMN remarks TEXT COMMENT '审核备注';
ALTER TABLE competition_audit_log MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';