<template>
  <div class="app-detail">
    <!-- Header -->
    <header class="detail-header">
      <div class="header-inner">
        <router-link to="/apps" class="back-btn">← 返回</router-link>
        <div class="app-title-section">
          <h2>{{ app?.title }}</h2>
          <span :class="['badge', 'badge-' + (app?.status || 'done').toLowerCase()]">
            {{ statusLabel(app?.status) }}
          </span>
        </div>
        <div class="header-actions" v-if="app">
          <a-button @click="editInWorkspace" size="small">✏️ 继续编辑</a-button>
          <a-button @click="handleFork" size="small" :loading="forking">🍴 Fork</a-button>
          <a-button @click="handleDownload" size="small">💾 下载源码</a-button>
          <a-button type="primary" @click="handlePublish" size="small" :loading="publishing">
            {{ app.status === 'PUBLISHED' ? '🌐 已发布' : '🚀 一键发布' }}
          </a-button>
        </div>
      </div>
    </header>

    <!-- Share Dialog -->
    <a-modal v-model:visible="showShareDialog" title="🎉 发布成功！" :footer="false" width="480px">
      <div class="share-dialog">
        <div class="share-icon">🌐</div>
        <p class="share-desc">你的应用已成功发布，任何人都可以通过以下链接访问：</p>
        <div class="share-url-box">
          <span class="share-url-text">{{ shareUrl }}</span>
          <button class="copy-btn" @click="copyShareUrl">{{ copied ? '✅ 已复制' : '复制' }}</button>
        </div>
        <div class="share-actions">
          <a :href="shareUrl" target="_blank" class="open-btn glow-btn">在新窗口打开 →</a>
        </div>
        <div class="embed-section">
          <p class="embed-title">嵌入代码：</p>
          <div class="embed-code">{{ embedCode }}</div>
        </div>
      </div>
    </a-modal>

    <div class="detail-content" v-if="app">
      <!-- Left: Info + Code -->
      <div class="detail-left">
        <div class="info-card glass-card">
          <div class="info-header">
            <h4>📋 应用信息</h4>
            <span class="app-id">ID: {{ app.id }}</span>
          </div>
          <p class="info-prompt">{{ app.prompt }}</p>
          <div class="info-meta-grid">
            <div class="meta-item"><span class="meta-label">创建时间</span><span>{{ formatDate(app.createdAt) }}</span></div>
            <div class="meta-item"><span class="meta-label">最后更新</span><span>{{ formatDate(app.updatedAt) }}</span></div>
            <div class="meta-item" v-if="app.shareUrl">
              <span class="meta-label">分享链接</span>
              <a :href="app.shareUrl" target="_blank" class="share-link">查看 →</a>
            </div>
          </div>
        </div>

        <!-- Code View -->
        <div class="code-card glass-card">
          <div class="code-header">
            <h4>💻 源代码</h4>
            <div class="code-tabs-mini">
              <button v-for="tab in codeTabs" :key="tab.key"
                :class="['code-tab-mini', { active: activeTab === tab.key }]"
                @click="activeTab = tab.key">{{ tab.label }}</button>
            </div>
          </div>
          <pre class="code-preview"><code>{{ currentCode }}</code></pre>
        </div>
      </div>

      <!-- Right: Live Preview -->
      <div class="detail-right">
        <div class="preview-container glass-card">
          <!-- Browser chrome -->
          <div class="browser-chrome">
            <div class="browser-dots">
              <span class="dot red"></span>
              <span class="dot yellow"></span>
              <span class="dot green"></span>
            </div>
            <div class="browser-url">
              <span v-if="app.shareUrl">🌐 {{ app.shareUrl }}</span>
              <span v-else>📱 应用预览</span>
            </div>
            <div class="browser-actions">
              <button class="chrome-btn" @click="refreshPreview">↺</button>
            </div>
          </div>
          <iframe
            ref="previewFrame"
            class="app-preview-frame"
            sandbox="allow-scripts allow-same-origin"
            :srcdoc="previewHtml"
          ></iframe>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-else class="loading-state">
      <a-spin size="36" /><p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { appApi } from '../api/app'
import { Message } from '@arco-design/web-vue'

const route = useRoute()
const router = useRouter()
const app = ref(null)
const activeTab = ref('html')
const publishing = ref(false)
const forking = ref(false)
const showShareDialog = ref(false)
const shareUrl = ref('')
const copied = ref(false)
const previewFrame = ref(null)

const codeTabs = [
  { key: 'html', label: 'HTML' },
  { key: 'css', label: 'CSS' },
  { key: 'js', label: 'JS' }
]

const statusLabel = (s) => {
  if (s === 'GENERATING') return '生成中'
  if (s === 'DONE') return '已完成'
  if (s === 'PUBLISHED') return '已发布'
  return s || ''
}
const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN') : ''

const currentCode = computed(() => {
  if (!app.value) return ''
  if (activeTab.value === 'html') return app.value.htmlContent || '// 无内容'
  if (activeTab.value === 'css') return app.value.cssContent || '// 无内容'
  return app.value.jsContent || '// 无内容'
})

const previewHtml = computed(() => {
  if (!app.value?.htmlContent) return '<p style="text-align:center;color:#888;padding:60px;font-family:sans-serif">暂无预览内容</p>'
  let html = app.value.htmlContent
  if (app.value.cssContent) html = html.replace('</head>', `<style>${app.value.cssContent}</style></head>`)
  if (app.value.jsContent) html = html.replace('</body>', `<script>${app.value.jsContent}<\/script></body>`)
  return html
})

const embedCode = computed(() =>
  app.value ? `<iframe src="http://localhost:8080/embed/${app.value.id}" width="100%" height="600" frameborder="0"></iframe>` : ''
)

function refreshPreview() {
  if (previewFrame.value) {
    const src = previewFrame.value.srcdoc
    previewFrame.value.srcdoc = ''
    setTimeout(() => { previewFrame.value.srcdoc = src }, 50)
  }
}

function editInWorkspace() {
  router.push({ path: '/workspace', query: { appId: app.value.id } })
}

async function handlePublish() {
  publishing.value = true
  try {
    const updated = await appApi.publish(app.value.id)
    app.value = updated
    shareUrl.value = updated.shareUrl
    showShareDialog.value = true
    Message.success('发布成功！')
  } catch (e) {
    Message.error('发布失败：' + (e.message || '请重试'))
  } finally {
    publishing.value = false
  }
}

async function handleFork() {
  forking.value = true
  try {
    const forked = await appApi.fork(app.value.id)
    Message.success(`Fork 成功！已添加到你的应用列表`)
    router.push(`/app/${forked.id}`)
  } catch (e) {
    Message.error('Fork 失败')
  } finally {
    forking.value = false
  }
}

function handleDownload() {
  const url = appApi.downloadUrl(app.value.id)
  window.open(url, '_blank')
}

function copyShareUrl() {
  navigator.clipboard.writeText(shareUrl.value).then(() => {
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  })
}

onMounted(async () => {
  const id = route.params.id
  app.value = await appApi.getById(id)
  if (app.value?.shareUrl) shareUrl.value = app.value.shareUrl
})
</script>

<style scoped>
.app-detail { min-height: 100vh; position: relative; z-index: 1; display: flex; flex-direction: column; }
.detail-header { background: var(--bg-secondary); border-bottom: 1px solid var(--border); }
.header-inner {
  max-width: 1400px; margin: 0 auto; padding: 12px 24px;
  display: flex; align-items: center; gap: 16px; flex-wrap: wrap;
}
.back-btn { color: var(--text-secondary); text-decoration: none; font-size: 14px; white-space: nowrap; transition: var(--transition); }
.back-btn:hover { color: var(--text-primary); }
.app-title-section { display: flex; align-items: center; gap: 10px; flex: 1; }
.app-title-section h2 { font-size: 17px; font-weight: 600; }
.header-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.detail-content {
  flex: 1; display: grid; grid-template-columns: 420px 1fr;
  max-height: calc(100vh - 53px); overflow: hidden;
}
.detail-left { padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; }
.info-card, .code-card { padding: 16px; }
.info-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.info-header h4 { font-size: 14px; color: var(--text-secondary); }
.app-id { font-size: 11px; color: var(--text-muted); font-family: monospace; }
.info-prompt { font-size: 13px; color: var(--text-primary); line-height: 1.6; margin-bottom: 12px; }
.info-meta-grid { display: flex; flex-direction: column; gap: 6px; }
.meta-item { display: flex; justify-content: space-between; font-size: 12px; }
.meta-label { color: var(--text-muted); }
.share-link { color: var(--accent-secondary); text-decoration: none; }
.code-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.code-header h4 { font-size: 14px; color: var(--text-secondary); }
.code-tabs-mini { display: flex; gap: 4px; }
.code-tab-mini {
  padding: 3px 10px; border: 1px solid var(--border); background: transparent;
  color: var(--text-secondary); border-radius: 4px; cursor: pointer; font-size: 11px;
  transition: var(--transition); font-family: 'Inter', sans-serif;
}
.code-tab-mini.active { background: var(--accent-primary); color: white; border-color: var(--accent-primary); }
.code-preview {
  font-family: 'JetBrains Mono', monospace; font-size: 11px; color: #abb2bf;
  line-height: 1.5; white-space: pre-wrap; word-break: break-all; max-height: 320px; overflow-y: auto;
}
.detail-right { padding: 20px; overflow: hidden; }
.preview-container { height: 100%; display: flex; flex-direction: column; overflow: hidden; padding: 0; }
.browser-chrome {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px;
  background: var(--bg-card); border-bottom: 1px solid var(--border); flex-shrink: 0;
}
.browser-dots { display: flex; gap: 6px; }
.dot { width: 12px; height: 12px; border-radius: 50%; }
.dot.red { background: #ff5f57; }
.dot.yellow { background: #ffbd2e; }
.dot.green { background: #28c840; }
.browser-url { flex: 1; font-size: 12px; color: var(--text-muted); }
.browser-actions .chrome-btn {
  background: transparent; border: none; color: var(--text-muted);
  cursor: pointer; font-size: 16px; font-family: 'Inter', sans-serif;
}
.app-preview-frame { flex: 1; border: none; background: white; width: 100%; }
.loading-state { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; color: var(--text-secondary); }

/* Share dialog */
.share-dialog { text-align: center; padding: 8px 0; }
.share-icon { font-size: 48px; margin-bottom: 12px; }
.share-desc { font-size: 14px; color: var(--text-secondary); margin-bottom: 16px; }
.share-url-box {
  display: flex; align-items: center; background: var(--bg-card);
  border: 1px solid var(--border); border-radius: 8px; padding: 10px 14px; gap: 10px; margin-bottom: 16px;
}
.share-url-text { flex: 1; font-size: 13px; color: var(--accent-secondary); word-break: break-all; text-align: left; }
.copy-btn {
  background: var(--accent-primary); border: none; color: white;
  padding: 5px 12px; border-radius: 6px; cursor: pointer; font-size: 12px; white-space: nowrap;
  font-family: 'Inter', sans-serif; transition: var(--transition);
}
.copy-btn:hover { opacity: 0.85; }
.share-actions { margin-bottom: 20px; }
.open-btn { font-size: 14px; padding: 10px 28px; text-decoration: none; }
.embed-section { text-align: left; }
.embed-title { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; }
.embed-code {
  background: var(--bg-card); border: 1px solid var(--border); border-radius: 6px;
  padding: 10px; font-family: monospace; font-size: 11px; color: var(--text-secondary);
  word-break: break-all;
}
</style>
