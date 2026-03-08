<template>
  <div class="admin-view">
    <!-- Sidebar -->
    <aside class="admin-sidebar">
      <div class="sidebar-brand">
        <span>⚡</span>
        <span class="gradient-text">Admin</span>
      </div>
      <nav class="sidebar-nav">
        <button v-for="item in navItems" :key="item.key"
          :class="['nav-item', { active: activeTab === item.key }]"
          @click="activeTab = item.key">
          <span class="nav-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </button>
      </nav>
      <div class="sidebar-footer">
        <router-link to="/" class="back-link">← 返回前台</router-link>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="admin-main">
      <!-- Overview -->
      <div v-if="activeTab === 'overview'" class="tab-content">
        <h2 class="tab-title">📊 数据概览</h2>
        <div class="stats-grid">
          <div v-for="card in statsCards" :key="card.key" class="stat-card glass-card">
            <div class="stat-icon">{{ card.icon }}</div>
            <div class="stat-info">
              <div class="stat-value">{{ overview[card.key] ?? '...' }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
            <div class="stat-today" v-if="card.todayKey">
              <span class="today-badge">今日 +{{ overview[card.todayKey] ?? 0 }}</span>
            </div>
          </div>
        </div>

        <!-- Quick charts -->
        <div class="charts-row">
          <div class="chart-card glass-card">
            <h4>📈 最近 AI 调用记录</h4>
            <div class="ai-log-mini">
              <div v-for="log in aiLogs.slice(0, 8)" :key="log.id" class="log-row">
                <span class="log-user">用户 #{{ log.userId }}</span>
                <span class="log-app">App #{{ log.appId }}</span>
                <span class="log-tokens">{{ log.tokens }} tokens</span>
                <span class="log-time">{{ formatDateTime(log.createdAt) }}</span>
              </div>
              <div v-if="aiLogs.length === 0" class="no-data">暂无记录</div>
            </div>
          </div>
          <div class="chart-card glass-card">
            <h4>🌟 平台状态</h4>
            <div class="status-items">
              <div class="status-item">
                <span class="status-dot green"></span>
                <span>MySQL 数据库</span>
                <span class="status-ok">正常</span>
              </div>
              <div class="status-item">
                <span class="status-dot green"></span>
                <span>Redis 缓存</span>
                <span class="status-ok">正常</span>
              </div>
              <div class="status-item">
                <span class="status-dot green"></span>
                <span>通义千问 AI</span>
                <span class="status-ok">正常</span>
              </div>
              <div class="status-item">
                <span class="status-dot green"></span>
                <span>后端 API</span>
                <span class="status-ok">运行中</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- User Management -->
      <div v-if="activeTab === 'users'" class="tab-content">
        <div class="tab-header">
          <h2 class="tab-title">👥 用户管理</h2>
          <div class="tab-actions">
            <a-input v-model="userKeyword" placeholder="搜索用户名/邮箱" allow-clear @change="searchUsers" style="width: 220px;" />
          </div>
        </div>
        <div class="data-table glass-card">
          <table class="table">
            <thead>
              <tr>
                <th>ID</th><th>头像</th><th>用户名</th><th>邮箱</th>
                <th>角色</th><th>状态</th><th>注册时间</th><th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td class="td-id">#{{ user.id }}</td>
                <td><a-avatar :size="32"><img :src="user.avatar || '/avatar.png'" /></a-avatar></td>
                <td class="td-name">{{ user.username }}</td>
                <td class="td-email">{{ user.email }}</td>
                <td>
                  <span :class="['role-badge', user.role === 'admin' ? 'admin' : 'user']">
                    {{ user.role === 'admin' ? '管理员' : '用户' }}
                  </span>
                </td>
                <td>
                  <span :class="['status-pill', user.status === 'banned' ? 'banned' : 'active']">
                    {{ user.status === 'banned' ? '已禁用' : '正常' }}
                  </span>
                </td>
                <td class="td-date">{{ formatDate(user.createdAt) }}</td>
                <td>
                  <div class="action-btns">
                    <button class="tbl-btn"
                      @click="toggleUserStatus(user)"
                      :disabled="user.role === 'admin'">
                      {{ user.status === 'banned' ? '解禁' : '禁用' }}
                    </button>
                    <button class="tbl-btn danger"
                      @click="deleteUser(user)"
                      :disabled="user.role === 'admin'">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="table-footer">
            <a-pagination v-model:current="userPage" :total="userTotal" :page-size="16" @change="loadUsers" size="small" />
          </div>
        </div>
      </div>

      <!-- App Management -->
      <div v-if="activeTab === 'apps'" class="tab-content">
        <div class="tab-header">
          <h2 class="tab-title">📱 应用管理</h2>
          <div class="tab-actions">
            <select v-model="appStatusFilter" class="filter-select" @change="loadApps(1)">
              <option value="">全部状态</option>
              <option value="DONE">已完成</option>
              <option value="PUBLISHED">已发布</option>
              <option value="GENERATING">生成中</option>
            </select>
          </div>
        </div>
        <div class="data-table glass-card">
          <table class="table">
            <thead>
              <tr><th>ID</th><th>应用名</th><th>用户ID</th><th>状态</th><th>精选</th><th>创建时间</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="app in apps" :key="app.id">
                <td class="td-id">#{{ app.id }}</td>
                <td class="td-name">{{ app.title }}</td>
                <td>#{{ app.userId }}</td>
                <td><span :class="['badge', 'badge-' + (app.status || 'done').toLowerCase()]">{{ statusLabel(app.status) }}</span></td>
                <td>
                  <button :class="['featured-btn', { active: app.isFeatured }]" @click="toggleFeatured(app)">
                    {{ app.isFeatured ? '⭐ 精选' : '☆ 设精选' }}
                  </button>
                </td>
                <td class="td-date">{{ formatDate(app.createdAt) }}</td>
                <td>
                  <div class="action-btns">
                    <a :href="`/app/${app.id}`" target="_blank" class="tbl-btn">查看</a>
                    <button class="tbl-btn danger" @click="deleteApp(app)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="table-footer">
            <a-pagination v-model:current="appPage" :total="appTotal" :page-size="16" @change="loadApps" size="small" />
          </div>
        </div>
      </div>

      <!-- AI Logs -->
      <div v-if="activeTab === 'ai-logs'" class="tab-content">
        <h2 class="tab-title">🤖 AI 调用日志</h2>
        <div class="data-table glass-card">
          <table class="table">
            <thead>
              <tr><th>ID</th><th>用户ID</th><th>应用ID</th><th>Token消耗</th><th>调用时间</th></tr>
            </thead>
            <tbody>
              <tr v-for="log in aiLogs" :key="log.id">
                <td class="td-id">#{{ log.id }}</td>
                <td>#{{ log.userId }}</td>
                <td>#{{ log.appId }}</td>
                <td><span class="token-badge">{{ log.tokens ?? 'N/A' }} tokens</span></td>
                <td>{{ formatDateTime(log.createdAt) }}</td>
              </tr>
              <tr v-if="aiLogs.length === 0">
                <td colspan="5" class="no-data-td">暂无 AI 调用记录</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../api/admin'
import { Message, Modal } from '@arco-design/web-vue'

const activeTab = ref('overview')
const overview = ref({})
const aiLogs = ref([])
const users = ref([])
const userPage = ref(1)
const userTotal = ref(0)
const userKeyword = ref('')
const apps = ref([])
const appPage = ref(1)
const appTotal = ref(0)
const appStatusFilter = ref('')

const navItems = [
  { key: 'overview', icon: '📊', label: '数据概览' },
  { key: 'users', icon: '👥', label: '用户管理' },
  { key: 'apps', icon: '📱', label: '应用管理' },
  { key: 'ai-logs', icon: '🤖', label: 'AI 日志' }
]

const statsCards = [
  { key: 'totalUsers', todayKey: 'todayUsers', icon: '👥', label: '注册用户' },
  { key: 'totalApps', todayKey: 'todayApps', icon: '📱', label: '创建应用' },
  { key: 'publishedApps', todayKey: null, icon: '🌐', label: '已发布应用' },
  { key: 'totalAiCalls', todayKey: 'todayAiCalls', icon: '🤖', label: 'AI 调用次数' }
]

const statusLabel = (s) => {
  if (s === 'GENERATING') return '生成中'
  if (s === 'DONE') return '已完成'
  if (s === 'PUBLISHED') return '已发布'
  return s || ''
}
const formatDate = (d) => d ? new Date(d).toLocaleDateString('zh-CN') : ''
const formatDateTime = (d) => d ? new Date(d).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : ''

async function loadOverview() {
  try {
    overview.value = await adminApi.getOverview()
    const logRes = await adminApi.getAiLogs()
    aiLogs.value = logRes.records || []
  } catch (e) {
    Message.error('加载失败，请确认您是管理员账号')
  }
}

async function loadUsers(page = 1) {
  const res = await adminApi.getUsers(page, 16, userKeyword.value)
  users.value = res.records || []
  userTotal.value = res.total || 0
}

async function loadApps(page = 1) {
  const res = await adminApi.getApps(page, 16, appStatusFilter.value)
  apps.value = res.records || []
  appTotal.value = res.total || 0
}

function searchUsers() {
  loadUsers(1)
}

async function toggleUserStatus(user) {
  const newStatus = user.status === 'banned' ? 'active' : 'banned'
  await adminApi.toggleUserStatus(user.id, newStatus)
  user.status = newStatus
  Message.success(newStatus === 'banned' ? '用户已禁用' : '用户已解禁')
}

function deleteUser(user) {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除用户「${user.username}」？此操作不可恢复。`,
    okButtonProps: { status: 'danger' },
    onOk: async () => {
      await adminApi.deleteUser(user.id)
      Message.success('已删除')
      loadUsers(userPage.value)
    }
  })
}

async function toggleFeatured(app) {
  await adminApi.setFeatured(app.id, !app.isFeatured)
  app.isFeatured = !app.isFeatured
  Message.success(app.isFeatured ? '已设为精选' : '已取消精选')
}

function deleteApp(app) {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除应用「${app.title}」？`,
    okButtonProps: { status: 'danger' },
    onOk: async () => {
      await adminApi.deleteApp(app.id)
      Message.success('已删除')
      loadApps(appPage.value)
    }
  })
}

onMounted(async () => {
  await loadOverview()
  await Promise.all([loadUsers(), loadApps()])
})
</script>

<style scoped>
.admin-view { display: flex; height: 100vh; position: relative; z-index: 1; }

/* Sidebar */
.admin-sidebar {
  width: 220px; flex-shrink: 0;
  background: var(--bg-secondary); border-right: 1px solid var(--border);
  display: flex; flex-direction: column;
}
.sidebar-brand {
  padding: 20px; font-size: 20px; font-weight: 700;
  display: flex; align-items: center; gap: 8px;
  border-bottom: 1px solid var(--border);
}
.sidebar-nav { flex: 1; padding: 12px 10px; display: flex; flex-direction: column; gap: 4px; }
.nav-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 14px; border: none; background: transparent;
  color: var(--text-secondary); border-radius: 10px; cursor: pointer;
  font-size: 14px; transition: var(--transition); font-family: 'Inter', sans-serif;
  text-align: left;
}
.nav-item:hover { background: var(--bg-card); color: var(--text-primary); }
.nav-item.active { background: var(--gradient-hero); color: white; }
.nav-icon { font-size: 16px; }
.sidebar-footer { padding: 16px; border-top: 1px solid var(--border); }
.back-link { color: var(--text-muted); font-size: 13px; text-decoration: none; transition: var(--transition); }
.back-link:hover { color: var(--text-primary); }

/* Main */
.admin-main { flex: 1; overflow-y: auto; padding: 28px; }
.tab-title { font-size: 22px; font-weight: 700; margin-bottom: 24px; }
.tab-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.tab-header .tab-title { margin-bottom: 0; }
.tab-actions { display: flex; gap: 10px; align-items: center; }

/* Stats Grid */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { display: flex; align-items: center; gap: 16px; padding: 20px; }
.stat-icon { font-size: 36px; line-height: 1; }
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: 700; line-height: 1; }
.stat-label { font-size: 13px; color: var(--text-muted); margin-top: 4px; }
.today-badge { font-size: 11px; background: rgba(108,92,231,0.2); color: var(--accent-secondary); padding: 2px 8px; border-radius: 20px; }

/* Charts */
.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.chart-card { padding: 20px; }
.chart-card h4 { font-size: 14px; color: var(--text-secondary); margin-bottom: 16px; }
.ai-log-mini { display: flex; flex-direction: column; gap: 8px; }
.log-row { display: flex; align-items: center; gap: 12px; font-size: 12px; padding: 6px 8px; background: var(--bg-card); border-radius: 6px; }
.log-user, .log-app { color: var(--accent-secondary); font-family: monospace; }
.log-tokens { margin-left: auto; color: var(--text-muted); }
.log-time { color: var(--text-muted); font-size: 11px; }
.status-items { display: flex; flex-direction: column; gap: 12px; }
.status-item { display: flex; align-items: center; gap: 10px; font-size: 14px; }
.status-dot { width: 10px; height: 10px; border-radius: 50%; animation: pulse 2s infinite; }
.status-dot.green { background: #28c840; }
.status-ok { margin-left: auto; color: #28c840; font-size: 12px; }

/* Table */
.data-table { padding: 0; overflow: hidden; }
.table { width: 100%; border-collapse: collapse; }
.table th { padding: 12px 16px; text-align: left; font-size: 12px; color: var(--text-muted); font-weight: 600; background: var(--bg-card); border-bottom: 1px solid var(--border); white-space: nowrap; }
.table td { padding: 12px 16px; font-size: 13px; border-bottom: 1px solid rgba(255,255,255,0.04); }
.table tbody tr:hover { background: rgba(255,255,255,0.02); }
.table tbody tr:last-child td { border-bottom: none; }
.td-id { color: var(--text-muted); font-family: monospace; font-size: 12px; }
.td-name { font-weight: 500; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.td-email { color: var(--text-secondary); font-size: 12px; }
.td-date { color: var(--text-muted); font-size: 12px; }
.role-badge { font-size: 11px; padding: 2px 8px; border-radius: 4px; }
.role-badge.admin { background: rgba(253,203,110,0.2); color: #fdcb6e; }
.role-badge.user { background: rgba(108,92,231,0.2); color: var(--accent-secondary); }
.status-pill { font-size: 11px; padding: 3px 10px; border-radius: 20px; }
.status-pill.active { background: rgba(40,200,64,0.1); color: #28c840; }
.status-pill.banned { background: rgba(255,107,107,0.1); color: #ff6b6b; }
.action-btns { display: flex; gap: 6px; }
.tbl-btn {
  padding: 4px 10px; border: 1px solid var(--border); background: transparent;
  color: var(--text-secondary); border-radius: 5px; cursor: pointer;
  font-size: 11px; transition: var(--transition); font-family: 'Inter', sans-serif;
  text-decoration: none; display: inline-flex; align-items: center;
}
.tbl-btn:hover { border-color: var(--accent-primary); color: var(--accent-secondary); }
.tbl-btn.danger:hover { border-color: #ff6b6b; color: #ff6b6b; }
.tbl-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.featured-btn {
  padding: 4px 10px; border: 1px solid var(--border); background: transparent;
  color: var(--text-muted); border-radius: 5px; cursor: pointer;
  font-size: 11px; transition: var(--transition); font-family: 'Inter', sans-serif;
}
.featured-btn.active { background: rgba(253,203,110,0.15); border-color: #fdcb6e; color: #fdcb6e; }
.filter-select {
  background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);
  padding: 6px 12px; border-radius: 8px; font-size: 13px; font-family: 'Inter', sans-serif;
}
.table-footer { padding: 12px 16px; display: flex; justify-content: flex-end; border-top: 1px solid var(--border); }
.token-badge { font-size: 11px; background: rgba(0,204,152,0.1); color: var(--accent-teal); padding: 2px 8px; border-radius: 4px; }
.no-data { color: var(--text-muted); font-size: 13px; text-align: center; padding: 16px; }
.no-data-td { text-align: center; color: var(--text-muted); padding: 32px !important; }
</style>
