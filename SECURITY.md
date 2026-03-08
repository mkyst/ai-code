# AI Code Platform

## 安全配置说明

### 环境变量设置

本项目使用环境变量管理敏感信息。请按以下步骤配置：

1. 复制 `backend/.env.example` 为 `backend/.env`
2. 填入你的实际配置信息：
   - `DB_PASSWORD`: 数据库密码
   - `JWT_SECRET`: JWT密钥（至少256位）
   - `QWEN_API_KEY`: 阿里云通义千问API密钥

### 重要提醒

⚠️ **切勿将 `.env` 文件提交到 Git 仓库！**

`.env` 文件已添加到 `.gitignore` 中。
