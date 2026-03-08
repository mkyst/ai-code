-- 创建数据库
CREATE DATABASE IF NOT EXISTS ai_code DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_code;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    email       VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password    VARCHAR(255) NOT NULL COMMENT '密码（bcrypt）',
    avatar      VARCHAR(500) COMMENT '头像URL',
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    credits     INT          NOT NULL DEFAULT 100 COMMENT '积分',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 应用表
CREATE TABLE IF NOT EXISTS apps (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL COMMENT '创建者ID',
    title        VARCHAR(100) NOT NULL DEFAULT '新建应用' COMMENT '应用名称',
    description  TEXT COMMENT '描述',
    prompt       TEXT COMMENT '用户提示词',
    html_content LONGTEXT COMMENT 'HTML代码',
    css_content  LONGTEXT COMMENT 'CSS代码',
    js_content   LONGTEXT COMMENT 'JS代码',
    status       VARCHAR(20)  NOT NULL DEFAULT 'GENERATING' COMMENT '状态: GENERATING/DONE/PUBLISHED',
    cover_url    VARCHAR(500) COMMENT '封面图URL',
    share_url    VARCHAR(500) COMMENT '分享链接',
    is_featured  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否精选',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_is_featured (is_featured)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用表';

-- AI调用日志表
CREATE TABLE IF NOT EXISTS ai_logs (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT  COMMENT '用户ID',
    app_id            BIGINT  COMMENT '应用ID',
    model             VARCHAR(100) COMMENT '模型名称',
    prompt_tokens     INT     COMMENT '输入tokens',
    completion_tokens INT     COMMENT '输出tokens',
    cost              DECIMAL(10, 6) COMMENT '估算费用（元）',
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI调用日志';

-- 默认管理员账号（密码: admin123456，bcrypt加密）
INSERT IGNORE INTO users (username, email, password, role, credits, avatar) VALUES
('admin', 'admin@aicode.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBagf.Q3wpkHZq', 'ADMIN', 9999, 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin');
