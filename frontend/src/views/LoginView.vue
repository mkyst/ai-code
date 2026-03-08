<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <div class="brand">
          <span style="font-size: 40px">⚡</span>
          <h1 class="gradient-text">AI Code</h1>
          <p>企业级 AI 代码生成平台</p>
        </div>
        <div class="features-list">
          <div v-for="f in loginFeatures" :key="f.text" class="feature-item">
            <span class="feature-check">{{ f.icon }}</span>
            <span>{{ f.text }}</span>
          </div>
        </div>
      </div>
      <div class="login-right glass-card">
        <!-- Tabs -->
        <div class="form-tabs">
          <button :class="['tab', { active: tab === 'login' }]" @click="tab = 'login'">登录</button>
          <button :class="['tab', { active: tab === 'register' }]" @click="tab = 'register'">注册</button>
        </div>

        <!-- Login Form -->
        <form v-if="tab === 'login'" @submit.prevent="handleLogin" class="auth-form">
          <h2>欢迎回来</h2>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="loginForm.email" class="form-input" placeholder="请输入邮箱" type="email" required />
          </div>
          <div class="form-group">
            <label>密码</label>
            <div class="input-password-wrap">
              <input v-model="loginForm.password" class="form-input" :type="showPwd ? 'text' : 'password'" placeholder="请输入密码" required />
              <button type="button" class="eye-btn" @click="showPwd = !showPwd">{{ showPwd ? '🙈' : '👁️' }}</button>
            </div>
          </div>
          <button type="submit" class="glow-btn submit-btn" :disabled="loading">
            {{ loading ? '登录中...' : '🚀 立即登录' }}
          </button>
          <p class="demo-hint">演示账号：admin@aicode.com / admin123456</p>
        </form>

        <!-- Register Form -->
        <form v-else @submit.prevent="handleRegister" class="auth-form">
          <h2>创建账号</h2>
          <div class="form-group">
            <label>用户名</label>
            <input v-model="registerForm.username" class="form-input" placeholder="2-20 位字符" required />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="registerForm.email" class="form-input" placeholder="请输入邮箱" type="email" required />
          </div>
          <div class="form-group">
            <label>密码</label>
            <div class="input-password-wrap">
              <input v-model="registerForm.password" class="form-input" :type="showPwd2 ? 'text' : 'password'" placeholder="至少 6 位" required />
              <button type="button" class="eye-btn" @click="showPwd2 = !showPwd2">{{ showPwd2 ? '🙈' : '👁️' }}</button>
            </div>
          </div>
          <button type="submit" class="glow-btn submit-btn" :disabled="loading">
            {{ loading ? '注册中...' : '✨ 免费注册' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { Message } from '@arco-design/web-vue'

const router = useRouter()
const userStore = useUserStore()
const tab = ref('login')
const loading = ref(false)
const showPwd = ref(false)
const showPwd2 = ref(false)

const loginForm = ref({ email: '', password: '' })
const registerForm = ref({ username: '', email: '', password: '' })

const loginFeatures = [
  { icon: '🤖', text: '智能 AI 代码生成' },
  { icon: '⚡', text: '实时流式输出' },
  { icon: '🎨', text: '可视化编辑修改' },
  { icon: '🚀', text: '一键云端部署' }
]

async function handleLogin() {
  loading.value = true
  try {
    await userStore.login(loginForm.value.email, loginForm.value.password)
    Message.success('登录成功，开始创作吧！')
    router.push('/workspace')
  } catch (e) { /* handled in axios interceptor */ }
  finally { loading.value = false }
}

async function handleRegister() {
  loading.value = true
  try {
    await userStore.register(registerForm.value.username, registerForm.value.email, registerForm.value.password)
    Message.success('注册成功！')
    router.push('/workspace')
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  position: relative; z-index: 1; padding: 24px;
}
.login-container {
  display: grid; grid-template-columns: 1fr 420px; gap: 60px;
  max-width: 960px; width: 100%; align-items: center;
}

/* Left branding */
.login-left .brand { margin-bottom: 40px; }
.login-left .brand h1 { font-size: 42px; font-weight: 900; margin: 8px 0 12px; }
.login-left .brand p { color: var(--text-secondary); font-size: 16px; }
.features-list { display: flex; flex-direction: column; gap: 16px; }
.feature-item { display: flex; align-items: center; gap: 12px; color: var(--text-secondary); font-size: 15px; }
.feature-check { font-size: 22px; }

/* Card */
.login-right { padding: 36px; }

/* Tabs */
.form-tabs {
  display: flex; margin-bottom: 28px;
  background: rgba(255,255,255,0.05); border-radius: 50px; padding: 4px;
}
.tab {
  flex: 1; padding: 9px; border: none; background: transparent;
  color: var(--text-secondary); cursor: pointer; border-radius: 50px;
  font-size: 14px; font-weight: 500; transition: var(--transition); font-family: 'Inter', sans-serif;
}
.tab.active { background: var(--gradient-hero); color: #fff; box-shadow: 0 4px 12px var(--accent-glow); }

/* Form */
.auth-form h2 { font-size: 22px; font-weight: 700; margin-bottom: 22px; }
.form-group { margin-bottom: 16px; }
.form-group label {
  display: block; font-size: 12px; color: var(--text-muted);
  margin-bottom: 6px; letter-spacing: 0.04em; font-weight: 500; text-transform: uppercase;
}

/* ── Native inputs — no more white arco box ── */
.form-input {
  width: 100%; box-sizing: border-box;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 11px 14px;
  color: #e8eaf0;
  font-size: 14px;
  font-family: 'Inter', sans-serif;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}
.form-input::placeholder { color: rgba(255, 255, 255, 0.22); }
.form-input:hover:not(:focus) {
  border-color: rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.08);
}
.form-input:focus {
  border-color: rgba(126, 87, 255, 0.65);
  background: rgba(126, 87, 255, 0.05);
  box-shadow: 0 0 0 3px rgba(126, 87, 255, 0.15);
}

/* Password eye toggle */
.input-password-wrap { position: relative; }
.input-password-wrap .form-input { padding-right: 42px; }
.eye-btn {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  background: none; border: none; cursor: pointer; font-size: 15px;
  color: rgba(255,255,255,0.3); padding: 0; line-height: 1; transition: color 0.2s;
}
.eye-btn:hover { color: rgba(255,255,255,0.6); }

/* Submit */
.submit-btn { width: 100%; font-size: 15px; padding: 13px; margin-top: 8px; }
.submit-btn:disabled { opacity: 0.55; cursor: not-allowed; }
.demo-hint { text-align: center; font-size: 12px; color: var(--text-muted); margin-top: 14px; }
</style>
