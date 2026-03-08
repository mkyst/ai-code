import axios from 'axios'
import { Message } from '@arco-design/web-vue'

const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 30000
})

// Request interceptor - add JWT token
request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// Response interceptor
request.interceptors.response.use(
    response => {
        const data = response.data
        if (data.code !== 200) {
            Message.error(data.message || '请求失败')
            return Promise.reject(new Error(data.message))
        }
        return data.data
    },
    error => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            window.location.href = '/login'
        }
        Message.error(error.response?.data?.message || '网络错误')
        return Promise.reject(error)
    }
)

export default request
