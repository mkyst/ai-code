<template>
  <div class="workspace">
    <!-- Header -->
    <header class="ws-header">
      <div class="ws-header-left">
        <router-link to="/" class="ws-logo">
          <span>⚡</span><span class="gradient-text">AI Code</span>
        </router-link>
        <div class="app-title-edit" v-if="currentApp">
          <a-input v-model="appTitle" class="title-input" @blur="saveTitle" size="small" />
          <span :class="['badge', 'badge-' + (currentApp.status || 'done').toLowerCase()]">
            {{ statusText }}
          </span>
        </div>
      </div>
      <div class="ws-header-center">
        <div class="mode-switcher" v-if="currentApp?.status === 'DONE'">
          <button :class="['mode-btn', { active: mode === 'generate' }]" @click="switchMode('generate')">
            🤖 生成模式
          </button>
          <button :class="['mode-btn', { active: mode === 'edit' }]" @click="switchMode('edit')">
            ✏️ 编辑模式
          </button>
        </div>
        <div v-else class="view-tabs">
          <button :class="['view-tab', { active: rightView === 'preview' }]" @click="rightView = 'preview'">👁 预览</button>
          <button :class="['view-tab', { active: rightView === 'code' }]" @click="rightView = 'code'">💻 代码</button>
        </div>
      </div>
      <div class="ws-header-right">
        <button class="icon-btn" title="版本历史" @click="showVersions = !showVersions" v-if="currentApp">
          🕐 历史
        </button>
        <router-link to="/apps">
          <a-button size="small">我的应用</a-button>
        </router-link>
        <a-avatar :size="30">
          <img :src="userStore.user?.avatar" />
        </a-avatar>
      </div>
    </header>

    <!-- Main Layout -->
    <div class="ws-main">
      <!-- Left: Chat Panel -->
      <div class="ws-left">
        <!-- Edit mode indicator -->
        <div class="edit-mode-bar" v-if="mode === 'edit'">
          <span class="edit-mode-icon">✏️</span>
          <span>编辑模式 - 点击右侧页面元素或直接描述修改</span>
          <button class="clear-btn" @click="clearSelectedElement" v-if="selectedElement">✕ 取消选中</button>
        </div>

        <!-- Selected element preview -->
        <div class="selected-element-hint" v-if="selectedElement && mode === 'edit'">
          <span class="element-tag">📌 已选中元素</span>
          <code class="element-preview">{{ selectedElement.substring(0, 80) }}{{ selectedElement.length > 80 ? '...' : '' }}</code>
        </div>

        <!-- AI Stream Panel -->
        <div class="ai-stream-panel" ref="streamPanel">
          <div v-if="!isGenerating && messages.length === 0" class="stream-empty">
            <div class="empty-icon">{{ mode === 'edit' ? '✏️' : '🤖' }}</div>
            <h3>{{ mode === 'edit' ? 'AI 编辑助手' : 'AI 代码助手' }}</h3>
            <p>{{ mode === 'edit' ? '点击右侧元素选中，或直接描述你想要的修改' : '描述你想要的应用，AI 将为你生成完整的网页代码' }}</p>
            <div class="suggestions" v-if="mode === 'generate'">
              <div v-for="s in suggestions" :key="s" class="suggestion-chip" @click="usePrompt(s)">{{ s }}</div>
            </div>
            <div class="suggestions" v-else>
              <div v-for="s in editSuggestions" :key="s" class="suggestion-chip" @click="usePrompt(s)">{{ s }}</div>
            </div>
          </div>

          <div v-for="(msg, i) in messages" :key="i" :class="['message', msg.type]">
            <div v-if="msg.type === 'user'" class="user-message"><span>{{ msg.content }}</span></div>
            <div v-else-if="msg.type === 'thinking'" class="thinking-message">
              <span class="think-icon">🤔</span><span>{{ msg.content }}</span>
            </div>
            <div v-else-if="msg.type === 'tool'" class="tool-message">
              <span>🛠️</span><span>{{ msg.content }}</span>
            </div>
            <div v-else-if="msg.type === 'done'" class="done-message">
              <span>🎉</span><span>{{ msg.content }}</span>
            </div>
            <div v-else-if="msg.type === 'error'" class="error-message">
              <span>❌</span><span>{{ msg.content }}</span>
            </div>
          </div>

          <div v-if="isGenerating" class="generating-dots">
            <span></span><span></span><span></span>
          </div>
        </div>

        <!-- Input -->
        <div class="ws-input-area">
          <div class="input-wrapper">
            <a-textarea
              v-model="prompt"
              :placeholder="inputPlaceholder"
              :disabled="isGenerating"
              :auto-size="{ minRows: 2, maxRows: 5 }"
              class="prompt-input"
              @keydown.enter.ctrl="handleSend"
            />
            <div class="input-actions">
              <span class="input-hint">Ctrl+Enter 发送</span>
              <button class="glow-btn send-btn" :disabled="isGenerating || !prompt.trim()" @click="handleSend">
                {{ isGenerating ? '处理中...' : (mode === 'edit' ? '✏️ 修改' : '🚀 生成') }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Preview + Code -->
      <div class="ws-right" :class="{ 'edit-mode': mode === 'edit' }">
        <!-- View tabs (only in generate mode) -->
        <div class="preview-toolbar" v-if="mode === 'generate'">
          <button :class="['view-tab', { active: rightView === 'preview' }]" @click="rightView = 'preview'">👁 预览</button>
          <button :class="['view-tab', { active: rightView === 'code' }]" @click="rightView = 'code'">💻 代码</button>
        </div>

        <!-- Edit mode toolbar -->
        <div class="preview-toolbar" v-if="mode === 'edit'">
          <div class="edit-tip">
            <span class="tip-dot"></span>
            点击页面元素进行选中，然后在左侧描述修改
          </div>
          <div class="toolbar-right">
            <button :class="['view-tab', { active: rightView === 'preview' }]" @click="rightView = 'preview'">👁 预览</button>
            <button :class="['view-tab', { active: rightView === 'code' }]" @click="rightView = 'code'">💻 代码</button>
          </div>
        </div>

        <!-- Preview iframe -->
        <div v-if="rightView === 'preview'" class="preview-panel">
          <div v-if="!hasCode" class="preview-empty">
            <div style="font-size: 64px">📱</div>
            <p>代码生成后将在此处实时预览</p>
          </div>
          <!-- Wrapper for element selection -->
          <div class="iframe-wrapper" :class="{ 'element-select-mode': mode === 'edit' }" @click.capture="handleIframeClick">
            <iframe
              v-if="hasCode"
              ref="previewFrame"
              class="preview-frame"
              sandbox="allow-scripts allow-same-origin"
              :srcdoc="previewHtml"
            ></iframe>
          </div>
          <!-- Selection overlay (injected message) -->
          <div class="selection-overlay" v-if="mode === 'edit' && hasCode">
            <span>点击元素选中 · 已选中 {{ selectedElement ? '1' : '0' }} 个元素</span>
          </div>
        </div>

        <!-- Code view -->
        <div v-else class="code-panel">
          <div class="code-tabs">
            <button v-for="tab in codeTabs" :key="tab.key" :class="['code-tab', { active: activeCodeTab === tab.key }]" @click="activeCodeTab = tab.key">
              {{ tab.label }}
            </button>
          </div>
          <div class="code-editor-wrapper">
            <pre class="code-content"><code>{{ currentCode }}</code></pre>
          </div>
        </div>
      </div>

      <!-- Version History Sidebar -->
      <transition name="slide-right">
        <div class="version-sidebar" v-if="showVersions">
          <div class="sidebar-header">
            <h4>🕐 版本历史</h4>
            <button class="close-btn" @click="showVersions = false">✕</button>
          </div>
          <div class="version-list">
            <div v-if="versions.length === 0" class="no-versions">暂无历史版本</div>
            <div v-for="v in versions" :key="v.id" class="version-item" @click="previewVersion(v)">
              <div class="version-header-row">
                <span class="version-num">v{{ v.versionNum }}</span>
                <span class="version-date">{{ formatDate(v.createdAt) }}</span>
              </div>
              <div class="version-desc">{{ v.changeDesc }}</div>
              <button class="restore-btn" @click.stop="restoreVersion(v)">↩ 恢复此版本</button>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { appApi, aiApi } from '../api/app'
import { editApi } from '../api/edit'
import { Message } from '@arco-design/web-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// State
const prompt = ref('')
const isGenerating = ref(false)
const messages = ref([])
const currentApp = ref(null)
const appTitle = ref('新建应用')
const mode = ref('generate') // 'generate' | 'edit'
const rightView = ref('preview')
const activeCodeTab = ref('html')
const streamPanel = ref(null)
const previewFrame = ref(null)
const showVersions = ref(false)
const versions = ref([])
const selectedElement = ref('')

const htmlCode = ref('')
const cssCode = ref('')
const jsCode = ref('')

// Computed
const hasCode = computed(() => !!(htmlCode.value || cssCode.value))

const previewHtml = computed(() => {
  if (!htmlCode.value) return ''
  let html = htmlCode.value

  // Inject element selection script in edit mode
  const selectionScript = mode.value === 'edit' ? `
    <script>
    (function() {
      let highlighted = null;
      document.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        if (highlighted) {
          highlighted.style.outline = highlighted._origOutline || '';
          highlighted.style.boxShadow = highlighted._origShadow || '';
        }
        highlighted = e.target;
        highlighted._origOutline = highlighted.style.outline;
        highlighted._origShadow = highlighted.style.boxShadow;
        highlighted.style.outline = '2px solid #6c5ce7';
        highlighted.style.boxShadow = '0 0 12px rgba(108,92,231,0.5)';
        window.parent.postMessage({
          type: 'ELEMENT_SELECTED',
          html: e.target.outerHTML,
          tag: e.target.tagName,
          text: e.target.textContent?.substring(0, 100)
        }, '*');
      }, true);
    })();
    <\/script>` : '';

  if (cssCode.value) {
    html = html.replace('</head>', `<style>${cssCode.value}</style></head>`)
  }
  if (jsCode.value) {
    html = html.replace('</body>', `<script>${jsCode.value}<\/script>${selectionScript}</body>`)
  } else if (selectionScript) {
    html = html.replace('</body>', `${selectionScript}</body>`)
  }
  return html
})

const currentCode = computed(() => {
  if (activeCodeTab.value === 'html') return htmlCode.value || '// HTML 代码将在生成后显示'
  if (activeCodeTab.value === 'css') return cssCode.value || '// CSS 代码将在生成后显示'
  return jsCode.value || '// JavaScript 代码将在生成后显示'
})

const codeTabs = [
  { key: 'html', label: '🌐 HTML' },
  { key: 'css', label: '🎨 CSS' },
  { key: 'js', label: '⚡ JavaScript' }
]

const statusText = computed(() => {
  const s = currentApp.value?.status
  if (s === 'GENERATING') return '生成中'
  if (s === 'DONE') return '已完成'
  if (s === 'PUBLISHED') return '已发布'
  return ''
})

const inputPlaceholder = computed(() => {
  if (isGenerating.value) return mode.value === 'edit' ? 'AI 正在修改...' : 'AI 正在生成中...'
  if (mode.value === 'edit') {
    return selectedElement.value
      ? `已选中元素，描述如何修改它...`
      : '描述你想修改的内容，例如：把标题改成蓝色，加大字号'
  }
  return '描述你想要的应用，例如：一个带动画效果的星空主题待办事项应用'
})

const suggestions = [
  '🌙 星空主题的待办事项应用',
  '📊 数据可视化仪表盘',
  '🎮 经典贪吃蛇游戏',
  '🎵 音乐播放器 UI',
  '💼 个人简历网页',
  '🛒 电商商品展示页'
]

const editSuggestions = [
  '把背景色改成深蓝色渐变',
  '给按钮添加悬浮动画效果',
  '把字体改成更现代的风格',
  '给页面添加粒子背景特效',
  '修改配色方案为紫色系',
  '给卡片添加毛玻璃效果'
]

// Methods
function usePrompt(s) { prompt.value = s }

function switchMode(newMode) {
  mode.value = newMode
  messages.value = []
  if (newMode === 'edit' && currentApp.value) {
    loadVersions()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (streamPanel.value) streamPanel.value.scrollTop = streamPanel.value.scrollHeight
  })
}

// Listen for element selection from iframe
function setupIframeListener() {
  window.addEventListener('message', (event) => {
    if (event.data?.type === 'ELEMENT_SELECTED' && mode.value === 'edit') {
      selectedElement.value = event.data.html
      const tag = event.data.tag
      const text = event.data.text
      Message.info(`已选中 <${tag.toLowerCase()}> 元素：${text?.substring(0, 30) || ''}`)
    }
  })
}

function handleIframeClick(e) {
  // Only intercept in edit mode for click overlay
  if (mode.value !== 'edit') return
}

function clearSelectedElement() {
  selectedElement.value = ''
}

async function handleSend() {
  if (!prompt.value.trim() || isGenerating.value) return
  const userPrompt = prompt.value.trim()
  prompt.value = ''
  isGenerating.value = true
  messages.value.push({ type: 'user', content: userPrompt })
  scrollToBottom()

  if (mode.value === 'generate') {
    await doGenerate(userPrompt)
  } else {
    await doEdit(userPrompt)
  }
}

async function doGenerate(userPrompt) {
  try {
    const app = await appApi.create({ title: '生成中...', description: userPrompt, prompt: userPrompt })
    currentApp.value = app
    appTitle.value = app.title || '新建应用'

    const streamUrl = aiApi.buildStreamUrl(app.id, userPrompt)
    const es = new EventSource(streamUrl)

    es.addEventListener('thinking', (e) => {
      const d = JSON.parse(e.data)
      if (d.message) { messages.value.push({ type: 'thinking', content: d.message }); scrollToBottom() }
    })
    es.addEventListener('tool_call', (e) => {
      const d = JSON.parse(e.data)
      messages.value.push({ type: 'tool', content: d.message })
      if (d.tool === 'setAppTitle') appTitle.value = d.message?.replace('📝 应用标题：', '') || appTitle.value
      scrollToBottom()
    })
    es.addEventListener('code', (e) => {
      const d = JSON.parse(e.data)
      if (d.type === 'html') htmlCode.value = d.content
      if (d.type === 'css') cssCode.value = d.content
      if (d.type === 'js') jsCode.value = d.content
      rightView.value = 'preview'
    })
    es.addEventListener('done', (e) => {
      const d = JSON.parse(e.data)
      messages.value.push({ type: 'done', content: d.message })
      if (currentApp.value) { currentApp.value.status = 'DONE'; currentApp.value.title = d.title || appTitle.value }
      isGenerating.value = false; es.close(); scrollToBottom()
      Message.success('生成完成！')
    })
    es.addEventListener('error', (e) => {
      try { const d = JSON.parse(e.data || '{}'); messages.value.push({ type: 'error', content: d.message }) } catch { }
      isGenerating.value = false; es.close(); scrollToBottom()
    })
    es.onerror = () => { isGenerating.value = false; es.close() }
  } catch (err) {
    isGenerating.value = false
    messages.value.push({ type: 'error', content: '启动失败，请重试' })
  }
}

async function doEdit(userPrompt) {
  if (!currentApp.value) { Message.warning('请先生成一个应用再进行编辑'); isGenerating.value = false; return }
  try {
    const streamUrl = editApi.buildEditStreamUrl(currentApp.value.id, userPrompt, selectedElement.value)
    const es = new EventSource(streamUrl)

    es.addEventListener('thinking', (e) => {
      const d = JSON.parse(e.data)
      if (d.message) { messages.value.push({ type: 'thinking', content: d.message }); scrollToBottom() }
    })
    es.addEventListener('tool_call', (e) => {
      const d = JSON.parse(e.data); messages.value.push({ type: 'tool', content: d.message }); scrollToBottom()
    })
    es.addEventListener('code', (e) => {
      const d = JSON.parse(e.data)
      if (d.type === 'html') htmlCode.value = d.content
      if (d.type === 'css') cssCode.value = d.content
      if (d.type === 'js') jsCode.value = d.content
      rightView.value = 'preview'
    })
    es.addEventListener('done', (e) => {
      const d = JSON.parse(e.data)
      messages.value.push({ type: 'done', content: d.message || '✅ 修改完成' })
      selectedElement.value = ''
      isGenerating.value = false; es.close(); scrollToBottom()
      loadVersions()
      Message.success('修改完成！')
    })
    es.addEventListener('error', (e) => {
      try { const d = JSON.parse(e.data || '{}'); messages.value.push({ type: 'error', content: d.message }) } catch { }
      isGenerating.value = false; es.close(); scrollToBottom()
    })
    es.onerror = () => { isGenerating.value = false; es.close() }
  } catch (err) {
    isGenerating.value = false
    messages.value.push({ type: 'error', content: '编辑失败，请重试' })
  }
}

async function loadVersions() {
  if (!currentApp.value) return
  try {
    versions.value = await editApi.getVersions(currentApp.value.id)
  } catch (e) {}
}

function previewVersion(v) {
  htmlCode.value = v.htmlContent || htmlCode.value
  cssCode.value = v.cssContent || cssCode.value
  jsCode.value = v.jsContent || jsCode.value
  Message.info(`预览版本 v${v.versionNum}`)
}

async function restoreVersion(v) {
  const vData = await editApi.restoreVersion(v.id)
  htmlCode.value = vData.htmlContent || htmlCode.value
  cssCode.value = vData.cssContent || cssCode.value
  jsCode.value = vData.jsContent || jsCode.value
  // Save to app
  if (currentApp.value) {
    await appApi.update(currentApp.value.id, {
      htmlContent: htmlCode.value,
      cssContent: cssCode.value,
      jsContent: jsCode.value
    })
  }
  Message.success(`已恢复到版本 v${v.versionNum}`)
  showVersions.value = false
}

async function saveTitle() {
  if (currentApp.value && appTitle.value) {
    await appApi.update(currentApp.value.id, { title: appTitle.value })
  }
}

const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : ''

// Load existing app if appId is passed in query
onMounted(async () => {
  setupIframeListener()
  const appId = route.query.appId
  if (appId) {
    try {
      const app = await appApi.getById(appId)
      currentApp.value = app
      appTitle.value = app.title
      htmlCode.value = app.htmlContent || ''
      cssCode.value = app.cssContent || ''
      jsCode.value = app.jsContent || ''
      if (app.status === 'DONE') mode.value = 'edit'
    } catch (e) {}
  }
})
</script>

<style scoped>
.workspace { height: 100vh; display: flex; flex-direction: column; position: relative; z-index: 1; }

/* Header */
.ws-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 20px; height: 52px;
  background: var(--bg-secondary); border-bottom: 1px solid var(--border); flex-shrink: 0;
}
.ws-header-left, .ws-header-right { display: flex; align-items: center; gap: 12px; flex: 1; }
.ws-header-right { justify-content: flex-end; }
.ws-header-center { display: flex; justify-content: center; }
.ws-logo { display: flex; align-items: center; gap: 6px; text-decoration: none; font-weight: 700; font-size: 17px; }
.title-input { width: 180px; }
.icon-btn {
  background: var(--bg-card); border: 1px solid var(--border); color: var(--text-secondary);
  padding: 5px 12px; border-radius: 8px; cursor: pointer; font-size: 13px;
  transition: var(--transition); font-family: 'Inter', sans-serif;
}
.icon-btn:hover { border-color: var(--accent-primary); color: var(--accent-secondary); }

/* Mode Switcher */
.mode-switcher { display: flex; gap: 0; background: var(--bg-card); border-radius: 10px; padding: 3px; }
.mode-btn {
  padding: 6px 18px; border: none; background: transparent; color: var(--text-secondary);
  cursor: pointer; border-radius: 8px; font-size: 13px; font-weight: 500;
  transition: var(--transition); font-family: 'Inter', sans-serif;
}
.mode-btn.active { background: var(--gradient-hero); color: white; box-shadow: 0 2px 10px var(--accent-glow); }

/* View Tabs */
.view-tabs, .view-tab { display: flex; }
.view-tab {
  padding: 5px 16px; border: none; background: transparent; color: var(--text-secondary);
  cursor: pointer; border-radius: 6px; font-size: 13px; transition: var(--transition); font-family: 'Inter', sans-serif;
}
.view-tab.active { background: var(--accent-primary); color: white; }

/* Main */
.ws-main { flex: 1; display: grid; grid-template-columns: 380px 1fr; overflow: hidden; position: relative; }

/* Left Panel */
.ws-left { display: flex; flex-direction: column; border-right: 1px solid var(--border); background: var(--bg-secondary); }
.edit-mode-bar {
  display: flex; align-items: center; gap: 8px; padding: 10px 16px;
  background: rgba(108,92,231,0.1); border-bottom: 1px solid var(--border-active);
  font-size: 12px; color: var(--accent-secondary);
}
.edit-mode-icon { font-size: 16px; }
.clear-btn {
  margin-left: auto; background: transparent; border: 1px solid var(--border);
  color: var(--text-muted); padding: 2px 8px; border-radius: 4px; cursor: pointer; font-size: 11px;
  font-family: 'Inter', sans-serif;
}
.selected-element-hint {
  display: flex; align-items: center; gap: 8px; padding: 8px 16px;
  background: rgba(108,92,231,0.06); border-bottom: 1px solid var(--border);
}
.element-tag { font-size: 11px; color: var(--accent-secondary); white-space: nowrap; }
.element-preview { font-size: 11px; color: var(--text-muted); font-family: monospace; overflow: hidden; text-overflow: ellipsis; }
.ai-stream-panel { flex: 1; overflow-y: auto; padding: 16px; }
.stream-empty { text-align: center; padding: 32px 16px; color: var(--text-secondary); }
.empty-icon { font-size: 40px; margin-bottom: 12px; }
.stream-empty h3 { font-size: 16px; color: var(--text-primary); margin-bottom: 6px; }
.stream-empty p { font-size: 13px; line-height: 1.5; margin-bottom: 18px; }
.suggestions { display: flex; flex-direction: column; gap: 6px; }
.suggestion-chip {
  background: var(--bg-card); border: 1px solid var(--border); border-radius: 8px;
  padding: 9px 12px; font-size: 13px; cursor: pointer; text-align: left;
  transition: var(--transition); color: var(--text-secondary);
}
.suggestion-chip:hover { border-color: var(--accent-primary); color: var(--text-primary); }

/* Messages */
.message { margin-bottom: 10px; }
.user-message { background: var(--accent-primary); border-radius: 12px 12px 2px 12px; padding: 10px 14px; max-width: 90%; margin-left: auto; font-size: 14px; }
.thinking-message, .tool-message { display: flex; align-items: flex-start; gap: 8px; padding: 8px 10px; background: var(--bg-card); border-radius: 8px; font-size: 13px; color: var(--text-secondary); }
.done-message { display: flex; align-items: center; gap: 8px; padding: 10px; background: rgba(0,206,201,0.1); border: 1px solid rgba(0,206,201,0.3); border-radius: 8px; font-size: 13px; color: var(--accent-teal); }
.error-message { display: flex; align-items: center; gap: 8px; padding: 10px; background: rgba(255,100,100,0.1); border-radius: 8px; color: #ff6b6b; font-size: 13px; }
.generating-dots { display: flex; gap: 4px; padding: 12px; }
.generating-dots span { width: 8px; height: 8px; border-radius: 50%; background: var(--accent-primary); animation: pulse 1.4s infinite; }
.generating-dots span:nth-child(2) { animation-delay: 0.2s; }
.generating-dots span:nth-child(3) { animation-delay: 0.4s; }

/* Input */
.ws-input-area { border-top: 1px solid var(--border); padding: 14px; }
.input-wrapper { display: flex; flex-direction: column; gap: 8px; }
.input-actions { display: flex; align-items: center; justify-content: space-between; }
.input-hint { font-size: 11px; color: var(--text-muted); }
.send-btn { padding: 8px 18px; font-size: 13px; }
.send-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none !important; }

/* Right Panel */
.ws-right { display: flex; flex-direction: column; overflow: hidden; transition: var(--transition); }
.ws-right.edit-mode { outline: 2px solid var(--accent-primary); }
.preview-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 16px; background: var(--bg-card); border-bottom: 1px solid var(--border);
  flex-shrink: 0; gap: 8px;
}
.edit-tip { display: flex; align-items: center; gap: 8px; font-size: 12px; color: var(--accent-secondary); }
.tip-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--accent-secondary); animation: pulse 2s infinite; }
.toolbar-right { display: flex; gap: 4px; background: var(--bg-secondary); border-radius: 6px; padding: 2px; }
.preview-panel { flex: 1; position: relative; display: flex; flex-direction: column; }
.preview-empty { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: var(--text-secondary); gap: 12px; }
.iframe-wrapper { flex: 1; position: relative; }
.iframe-wrapper.element-select-mode { cursor: crosshair; }
.preview-frame { width: 100%; height: 100%; border: none; background: white; }
.selection-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: rgba(108,92,231,0.8); color: white;
  font-size: 12px; padding: 6px 12px; text-align: center;
}
.code-panel { flex: 1; display: flex; flex-direction: column; }
.code-tabs { display: flex; gap: 6px; background: var(--bg-card); padding: 8px 16px; }
.code-tab { padding: 5px 14px; border: 1px solid var(--border); background: transparent; color: var(--text-secondary); cursor: pointer; border-radius: 6px; font-size: 13px; transition: var(--transition); font-family: 'Inter', sans-serif; }
.code-tab.active { background: var(--accent-primary); color: white; border-color: var(--accent-primary); }
.code-editor-wrapper { flex: 1; overflow: auto; padding: 16px; }
.code-content { font-family: 'JetBrains Mono', 'Fira Code', monospace; font-size: 13px; line-height: 1.6; color: #abb2bf; white-space: pre-wrap; word-break: break-all; }

/* Version Sidebar */
.version-sidebar {
  position: absolute; right: 0; top: 0; bottom: 0; width: 300px;
  background: var(--bg-secondary); border-left: 1px solid var(--border);
  display: flex; flex-direction: column; z-index: 50;
  box-shadow: -8px 0 24px rgba(0,0,0,0.4);
}
.sidebar-header { display: flex; align-items: center; justify-content: space-between; padding: 16px; border-bottom: 1px solid var(--border); }
.sidebar-header h4 { font-size: 15px; font-weight: 600; }
.close-btn { background: transparent; border: none; color: var(--text-muted); cursor: pointer; font-size: 18px; font-family: 'Inter', sans-serif; }
.version-list { flex: 1; overflow-y: auto; padding: 12px; }
.no-versions { text-align: center; color: var(--text-muted); padding: 24px; font-size: 13px; }
.version-item {
  background: var(--bg-card); border: 1px solid var(--border); border-radius: 10px;
  padding: 12px; margin-bottom: 10px; cursor: pointer; transition: var(--transition);
}
.version-item:hover { border-color: var(--accent-primary); }
.version-header-row { display: flex; justify-content: space-between; margin-bottom: 6px; }
.version-num { font-size: 13px; font-weight: 600; color: var(--accent-secondary); }
.version-date { font-size: 11px; color: var(--text-muted); }
.version-desc { font-size: 12px; color: var(--text-secondary); margin-bottom: 10px; }
.restore-btn {
  width: 100%; background: rgba(108,92,231,0.1); border: 1px solid var(--border-active);
  color: var(--accent-secondary); padding: 6px; border-radius: 6px; cursor: pointer;
  font-size: 12px; transition: var(--transition); font-family: 'Inter', sans-serif;
}
.restore-btn:hover { background: rgba(108,92,231,0.25); }

/* Slide animation */
.slide-right-enter-active, .slide-right-leave-active { transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.slide-right-enter-from, .slide-right-leave-to { transform: translateX(100%); }
</style>
