import { createApp } from 'vue'
import ArcoDesign from '@arco-design/web-vue'
import ArcoDesignIconVue from '@arco-design/web-vue/es/icon'
import '@arco-design/web-vue/dist/arco.css'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './style.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ArcoDesign)
app.use(ArcoDesignIconVue)
app.mount('#app')
