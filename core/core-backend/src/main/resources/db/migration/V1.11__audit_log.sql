-- 审计日志表
CREATE TABLE IF NOT EXISTS core_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- 操作信息
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型',
    resource_id VARCHAR(100) COMMENT '资源ID',
    resource_name VARCHAR(200) COMMENT '资源名称',
    
    -- 操作详情
    operation_desc VARCHAR(500) COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_code INT COMMENT '响应码',
    response_msg VARCHAR(500) COMMENT '响应消息',
    
    -- 操作人信息
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人姓名',
    operator_account VARCHAR(100) COMMENT '操作人账号',
    operator_ip VARCHAR(50) COMMENT '操作人IP',
    
    -- 时间信息
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    duration BIGINT COMMENT '执行时长(ms)',
    
    -- 索引
    INDEX idx_operation_time (operation_time),
    INDEX idx_operator_id (operator_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_resource_type (resource_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
