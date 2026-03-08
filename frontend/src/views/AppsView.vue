<template>
  <div class="apps-view">
    <!-- Header -->
    <header class="apps-header">
      <div class="header-inner">
        <router-link to="/" class="ws-logo">
          <span>⚡</span><span class="gradient-text">AI Code</span>
        </router-link>
        <h2 class="page-title">我的应用</h2>
        <div class="header-actions">
          <router-link to="/workspace">
            <button class="glow-btn" style="font-size: 14px; padding: 8px 20px">+ 新建应用</button>
          </router-link>
          <a-avatar :size="32">
            <img :src="userStore.user?.avatar" />
          </a-avatar>
        </div>
      </div>
    </header>

    <div class="apps-content">
      <!-- Filter Tabs -->
      <div class="filter-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="['filter-tab', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card glass-card"></div>
      </div>

      <!-- Apps Grid -->
      <div v-else-if="apps.length > 0" class="apps-grid">
        <div
          v-for="app in apps"
          :key="app.id"
          class="app-card glass-card"
          @click="openApp(app)"
        >
          <div class="app-preview" :style="{ background: getGradient(app.id) }">
            <span class="app-emoji">{{ getEmoji(app.title) }}</span>
            <span :class="['badge', 'badge-' + app.status?.toLowerCase()]" class="app-status">
              {{ statusLabel(app.status) }}
            </span>
          </div>
          <div class="app-info">
            <h3 class="app-name">{{ app.title }}</h3>
            <p class="app-desc">{{ app.prompt?.substring(0, 60) }}{{ app.prompt?.length > 60 ? '...' : '' }}</p>
            <div class="app-meta">
              <span class="app-date">{{ formatDate(app.createdAt) }}</span>
              <div class="app-actions-row">
                <button class="action-btn" @click.stop="editApp(app)">编辑</button>
                <button class="action-btn danger" @click.stop="deleteApp(app)">删除</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty -->
      <div v-else class="apps-empty">
        <div style="font-size: 64px">🚀</div>
        <h3>还没有应用</h3>
        <p>用 AI 生成你的第一个应用吧！</p>
        <router-link to="/workspace">
          <button class="glow-btn" style="margin-top: 20px">开始创作</button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { appApi } from '../api/app'
import { Message, Modal } from '@arco-design/web-vue'

const router = useRouter()
const userStore = useUserStore()
const apps = ref([])
const loading = ref(false)
const activeTab = ref('my')

const tabs = [
  { key: 'my', label: '我的应用' },
  { key: 'public', label: '公开作品' }
]

const gradients = [
  'linear-gradient(135deg, #6c5ce7, #a29bfe)',
  'linear-gradient(135deg, #fd79a8, #e84393)',
  'linear-gradient(135deg, #00cec9, #00b894)',
  'linear-gradient(135deg, #fdcb6e, #e17055)',
  'linear-gradient(135deg, #74b9ff, #0984e3)',
  'linear-gradient(135deg, #a29bfe, #6c5ce7)',
]

const emojis = ['🌙', '🚀', '🎮', '📊', '🎨', '💡', '⚡', '🌟', '🔥', '💎']

const getGradient = (id) => gradients[id % gradients.length]
const getEmoji = (title) => {
  if (title?.includes('游戏')) return '🎮'
  if (title?.includes('数据')) return '📊'
  if (title?.includes('音乐')) return '🎵'
  if (title?.includes('天气')) return '🌤'
  if (title?.includes('日历')) return '📅'
  return emojis[Math.floor(Math.random() * emojis.length)]
}

const statusLabel = (s) => {
  if (s === 'GENERATING') return '生成中'
  if (s === 'DONE') return '已完成'
  if (s === 'PUBLISHED') return '已发布'
  return s
}

const formatDate = (d) => {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN')
}

async function loadApps() {
  loading.value = true
  try {
    const res = activeTab.value === 'my'
      ? await appApi.myApps()
      : await appApi.publicApps()
    apps.value = res.records || []
  } finally {
    loading.value = false
  }
}

function openApp(app) {
  router.push(`/app/${app.id}`)
}

function editApp(app) {
  router.push({ path: '/workspace', query: { appId: app.id } })
}

async function deleteApp(app) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用「${app.title}」吗？`,
    okButtonProps: { status: 'danger' },
    onOk: async () => {
      await appApi.delete(app.id)
      Message.success('已删除')
      loadApps()
    }
  })
}

watch(activeTab, loadApps)
onMounted(loadApps)
</script>

<style scoped>
.apps-view { min-height: 100vh; position: relative; z-index: 1; }
.apps-header { background: var(--bg-secondary); border-bottom: 1px solid var(--border); }
.header-inner {
  max-width: 1200px; margin: 0 auto; padding: 14px 24px;
  display: flex; align-items: center; gap: 24px;
}
.ws-logo { display: flex; align-items: center; gap: 6px; text-decoration: none; font-weight: 700; font-size: 18px; }
.page-title { font-size: 18px; font-weight: 600; flex: 1; }
.header-actions { display: flex; align-items: center; gap: 12px; }
.apps-content { max-width: 1200px; margin: 0 auto; padding: 24px; }
.filter-tabs { display: flex; gap: 8px; margin-bottom: 24px; }
.filter-tab {
  padding: 8px 20px; border: 1px solid var(--border); background: transparent;
  color: var(--text-secondary); border-radius: 50px; cursor: pointer;
  font-size: 14px; transition: var(--transition); font-family: 'Inter', sans-serif;
}
.filter-tab.active { background: var(--accent-primary); color: white; border-color: var(--accent-primary); }
.apps-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.loading-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.skeleton-card { height: 260px; animation: pulse 1.5s infinite; }
.app-card { cursor: pointer; overflow: hidden; padding: 0; }
.app-preview {
  height: 150px; display: flex; align-items: center; justify-content: center;
  position: relative;
}
.app-emoji { font-size: 52px; }
.app-status { position: absolute; top: 12px; right: 12px; }
.app-info { padding: 16px; }
.app-name { font-size: 15px; font-weight: 600; margin-bottom: 6px; }
.app-desc { font-size: 13px; color: var(--text-secondary); margin-bottom: 12px; line-height: 1.5; min-height: 38px; }
.app-meta { display: flex; align-items: center; justify-content: space-between; }
.app-date { font-size: 12px; color: var(--text-muted); }
.app-actions-row { display: flex; gap: 8px; }
.action-btn {
  padding: 4px 12px; border: 1px solid var(--border); background: transparent;
  color: var(--text-secondary); border-radius: 6px; cursor: pointer; font-size: 12px;
  transition: var(--transition); font-family: 'Inter', sans-serif;
}
.action-btn:hover { border-color: var(--accent-primary); color: var(--accent-secondary); }
.action-btn.danger:hover { border-color: #ff6b6b; color: #ff6b6b; }
.apps-empty { text-align: center; padding: 80px 20px; color: var(--text-secondary); }
.apps-empty h3 { font-size: 22px; color: var(--text-primary); margin: 16px 0 8px; }
</style>
