-- Phase 2: Add version history and chat message tables
USE ai_code;

CREATE TABLE IF NOT EXISTS app_versions (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id       BIGINT       NOT NULL COMMENT '应用ID',
    version_num  INT          NOT NULL COMMENT '版本号',
    html_content LONGTEXT COMMENT 'HTML快照',
    css_content  LONGTEXT COMMENT 'CSS快照',
    js_content   LONGTEXT COMMENT 'JS快照',
    title        VARCHAR(100) COMMENT '标题',
    change_desc  VARCHAR(255) COMMENT '修改说明',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_app_id (app_id),
    INDEX idx_version_num (app_id, version_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用版本历史';

CREATE TABLE IF NOT EXISTS chat_messages (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id     BIGINT       NOT NULL COMMENT '应用ID',
    user_id    BIGINT       COMMENT '用户ID',
    role       VARCHAR(20)  NOT NULL COMMENT 'user/assistant',
    content    TEXT         NOT NULL COMMENT '消息内容',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_app_id (app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话历史';
