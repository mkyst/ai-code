<template>
  <div class="home">
    <!-- Header -->
    <header class="header">
      <div class="header-inner">
        <div class="logo">
          <span class="logo-icon">⚡</span>
          <span class="logo-text gradient-text">AI Code</span>
        </div>
        <nav class="nav">
          <a href="#features" class="nav-link">功能特性</a>
          <a href="#showcase" class="nav-link">应用展示</a>
          <router-link v-if="!userStore.isLoggedIn" to="/login" class="nav-btn">登录 / 注册</router-link>
          <template v-else>
            <router-link to="/workspace" class="nav-btn">开始创作</router-link>
            <a-avatar :size="36" class="avatar-btn" @click="goToApps">
              <img :src="userStore.user?.avatar" />
            </a-avatar>
          </template>
        </nav>
      </div>
    </header>

    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-content fade-in">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          基于 AI 的下一代代码生成平台
        </div>
        <h1 class="hero-title">
          用
          <span class="gradient-text">自然语言</span>
          <br />构建你的应用
        </h1>
        <p class="hero-desc">
          描述你的想法，AI 自动生成美观的网页应用。<br />
          支持实时预览、可视化编辑、一键云端部署。
        </p>
        <div class="hero-actions">
          <router-link to="/workspace">
            <button class="glow-btn hero-btn">
              🚀 立即开始创作
            </button>
          </router-link>
          <router-link to="/apps">
            <button class="outline-btn">
              👁 浏览作品广场
            </button>
          </router-link>
        </div>
        <div class="hero-stats">
          <div class="stat">
            <span class="stat-num gradient-text">10K+</span>
            <span class="stat-label">已生成应用</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat">
            <span class="stat-num gradient-text">500+</span>
            <span class="stat-label">活跃用户</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat">
            <span class="stat-num gradient-text">99%</span>
            <span class="stat-label">用户满意度</span>
          </div>
        </div>
      </div>
      <!-- Code Preview Box -->
      <div class="hero-preview fade-in" style="animation-delay: 0.3s">
        <div class="preview-window">
          <div class="window-header">
            <span class="dot red"></span>
            <span class="dot yellow"></span>
            <span class="dot green"></span>
            <span class="window-title">AI 正在生成...</span>
          </div>
          <div class="preview-content">
            <div class="code-stream">
              <div v-for="(line, i) in codeLines" :key="i" class="code-line" :style="{ animationDelay: i * 0.1 + 's' }">
                <span class="line-num">{{ i + 1 }}</span>
                <span class="line-text" v-html="line"></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section id="features" class="features">
      <h2 class="section-title">核心能力</h2>
      <p class="section-desc">专为开发者和创意人设计的 AI 代码生成平台</p>
      <div class="features-grid">
        <div v-for="feature in features" :key="feature.title" class="feature-card glass-card">
          <div class="feature-icon">{{ feature.icon }}</div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.desc }}</p>
        </div>
      </div>
    </section>

    <!-- Showcase Section -->
    <section id="showcase" class="showcase">
      <h2 class="section-title">精选作品</h2>
      <p class="section-desc">用户使用 AI Code 创作的优秀应用</p>
      <div class="showcase-grid">
        <div v-for="app in showcaseApps" :key="app.id" class="showcase-card glass-card">
          <div class="showcase-preview" :style="{ background: app.bg }">
            <span class="showcase-emoji">{{ app.emoji }}</span>
          </div>
          <div class="showcase-info">
            <h4>{{ app.title }}</h4>
            <p>{{ app.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta">
      <div class="cta-content glass-card">
        <h2>准备好开始了吗？</h2>
        <p>只需用文字描述，AI 即刻为你生成专业应用</p>
        <router-link to="/workspace">
          <button class="glow-btn" style="font-size: 17px; padding: 14px 40px">
            🎯 免费开始创作
          </button>
        </router-link>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <span>© 2026 AI Code Platform · 企业级 AI 代码生成平台</span>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const goToApps = () => router.push('/apps')

const codeLines = [
  '<span class="kw">const</span> <span class="fn">createApp</span> = <span class="kw">async</span> (prompt) => {',
  '  <span class="cm">// 🤔 AI 分析需求...</span>',
  '  <span class="kw">const</span> strategy = <span class="kw">await</span> ai.analyze(prompt)',
  '  <span class="cm">// 🛠 调用代码生成工具</span>',
  '  <span class="kw">await</span> tools.createHtml(strategy.html)',
  '  <span class="kw">await</span> tools.createCss(strategy.css)',
  '  <span class="kw">await</span> tools.createJs(strategy.js)',
  '  <span class="cm">// ✅ 生成完成！</span>',
  '  <span class="kw">return</span> { success: <span class="bool">true</span> }',
  '}'
]

const features = [
  { icon: '🤖', title: '智能代码生成', desc: 'AI 分析需求，自动选择最优生成策略，通过工具调用生成 HTML/CSS/JS 三件套' },
  { icon: '⚡', title: '实时流式输出', desc: '采用 SSE 流式传输，实时看到 AI 的思考过程和代码生成过程' },
  { icon: '🎨', title: '可视化编辑', desc: '点击页面元素，与 AI 对话即时修改，所见即所得的编辑体验' },
  { icon: '🚀', title: '一键云端部署', desc: '生成完成后一键部署到云端，获得可访问链接，轻松分享你的作品' },
  { icon: '📦', title: '源码下载', desc: '支持下载完整项目源码，随时迁移到自己的服务器或代码仓库' },
  { icon: '🏢', title: '企业级管理', desc: '提供完整后台管理系统，监控 AI 调用、系统性能和业务指标' }
]

const showcaseApps = [
  { id: 1, emoji: '🌙', title: '星空日历', desc: '美观的日历应用，带动画效果', bg: 'linear-gradient(135deg, #0f3460, #16213e)' },
  { id: 2, emoji: '📊', title: '数据看板', desc: '实时数据可视化仪表盘', bg: 'linear-gradient(135deg, #1a1a2e, #16213e)' },
  { id: 3, emoji: '🎮', title: '贪吃蛇游戏', desc: '经典游戏的现代化重制', bg: 'linear-gradient(135deg, #0d1117, #161b22)' },
  { id: 4, emoji: '🎵', title: '音乐播放器', desc: '带可视化效果的音乐播放器', bg: 'linear-gradient(135deg, #1e0338, #3d0066)' },
  { id: 5, emoji: '📝', title: 'Todo 应用', desc: '简洁美观的任务管理工具', bg: 'linear-gradient(135deg, #003049, #005f73)' },
  { id: 6, emoji: '🌤', title: '天气助手', desc: '实时天气查询与预报', bg: 'linear-gradient(135deg, #184e77, #1e6091)' }
]
</script>

<style scoped>
.home { min-height: 100vh; position: relative; z-index: 1; }

/* Header */
.header {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  backdrop-filter: blur(20px);
  background: rgba(10, 10, 15, 0.8);
  border-bottom: 1px solid var(--border);
}
.header-inner {
  max-width: 1200px; margin: 0 auto; padding: 16px 24px;
  display: flex; align-items: center; justify-content: space-between;
}
.logo { display: flex; align-items: center; gap: 8px; text-decoration: none; }
.logo-icon { font-size: 24px; }
.logo-text { font-size: 22px; font-weight: 800; }
.nav { display: flex; align-items: center; gap: 24px; }
.nav-link {
  color: var(--text-secondary); text-decoration: none; font-size: 14px; font-weight: 500;
  transition: var(--transition);
}
.nav-link:hover { color: var(--text-primary); }
.nav-btn {
  background: rgba(108,92,231,0.15); border: 1px solid var(--border-active);
  color: var(--accent-secondary); padding: 8px 20px; border-radius: 50px;
  font-size: 14px; font-weight: 500; text-decoration: none; transition: var(--transition);
}
.nav-btn:hover { background: rgba(108,92,231,0.3); }
.avatar-btn { cursor: pointer; border: 2px solid var(--accent-primary); }

/* Hero */
.hero {
  max-width: 1200px; margin: 0 auto;
  padding: 120px 24px 80px;
  display: grid; grid-template-columns: 1fr 1fr; gap: 60px; align-items: center;
}
.hero-badge {
  display: inline-flex; align-items: center; gap: 8px;
  background: rgba(108,92,231,0.1); border: 1px solid var(--border-active);
  padding: 6px 16px; border-radius: 50px; font-size: 13px; color: var(--accent-secondary);
  margin-bottom: 24px;
}
.badge-dot {
  width: 8px; height: 8px; border-radius: 50%; background: var(--accent-secondary);
  animation: pulse 2s infinite;
}
.hero-title {
  font-size: 62px; font-weight: 900; line-height: 1.1; margin-bottom: 20px;
  letter-spacing: -1px;
}
.hero-desc {
  font-size: 18px; color: var(--text-secondary); line-height: 1.7; margin-bottom: 36px;
}
.hero-actions { display: flex; gap: 16px; margin-bottom: 48px; }
.hero-btn { font-size: 16px; padding: 14px 36px; }
.outline-btn {
  background: transparent; border: 1px solid var(--border); color: var(--text-primary);
  padding: 14px 36px; border-radius: 50px; font-size: 15px; font-weight: 500;
  cursor: pointer; transition: var(--transition); font-family: 'Inter', sans-serif;
}
.outline-btn:hover { border-color: var(--accent-primary); color: var(--accent-secondary); }
.hero-stats { display: flex; align-items: center; gap: 32px; }
.stat { text-align: center; }
.stat-num { font-size: 28px; font-weight: 800; display: block; }
.stat-label { font-size: 12px; color: var(--text-muted); }
.stat-divider { width: 1px; height: 40px; background: var(--border); }

/* Code Preview */
.preview-window {
  background: var(--bg-secondary); border: 1px solid var(--border);
  border-radius: var(--radius); overflow: hidden;
  box-shadow: var(--shadow-glow);
}
.window-header {
  display: flex; align-items: center; gap: 8px; padding: 12px 16px;
  background: var(--bg-card); border-bottom: 1px solid var(--border);
}
.dot { width: 12px; height: 12px; border-radius: 50%; }
.dot.red { background: #ff5f57; }
.dot.yellow { background: #ffbd2e; }
.dot.green { background: #28c840; }
.window-title { font-size: 13px; color: var(--text-muted); margin-left: 8px; }
.preview-content { padding: 20px; }
.code-line {
  display: flex; gap: 16px; margin-bottom: 6px;
  animation: fadeInUp 0.4s ease both;
}
.line-num { color: var(--text-muted); font-size: 13px; min-width: 20px; font-family: monospace; }
.line-text { font-family: 'JetBrains Mono', 'Fira Code', monospace; font-size: 13px; color: #abb2bf; }
:deep(.kw) { color: #c678dd; }
:deep(.fn) { color: #61afef; }
:deep(.cm) { color: #5c6370; font-style: italic; }
:deep(.bool) { color: #d19a66; }

/* Sections */
.features, .showcase { max-width: 1200px; margin: 0 auto; padding: 80px 24px; }
.section-title { font-size: 40px; font-weight: 800; text-align: center; margin-bottom: 12px; }
.section-desc { text-align: center; color: var(--text-secondary); margin-bottom: 48px; font-size: 16px; }

.features-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.feature-card { padding: 28px; }
.feature-icon { font-size: 36px; margin-bottom: 16px; }
.feature-title { font-size: 18px; font-weight: 700; margin-bottom: 10px; }
.feature-desc { font-size: 14px; color: var(--text-secondary); line-height: 1.6; }

.showcase-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.showcase-card { overflow: hidden; padding: 0; }
.showcase-preview {
  height: 160px; display: flex; align-items: center; justify-content: center;
}
.showcase-emoji { font-size: 56px; }
.showcase-info { padding: 16px 20px; }
.showcase-info h4 { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.showcase-info p { font-size: 13px; color: var(--text-secondary); }

/* CTA */
.cta { max-width: 800px; margin: 0 auto 80px; padding: 0 24px; }
.cta-content { text-align: center; padding: 60px 40px; }
.cta-content h2 { font-size: 36px; font-weight: 800; margin-bottom: 12px; }
.cta-content p { color: var(--text-secondary); margin-bottom: 32px; font-size: 16px; }

/* Footer */
.footer {
  text-align: center; padding: 24px;
  color: var(--text-muted); font-size: 13px;
  border-top: 1px solid var(--border);
}
</style>
