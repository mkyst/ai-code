import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/auth'

export const useUserStore = defineStore('user', () => {
    const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
    const token = ref(localStorage.getItem('token') || '')

    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => user.value?.role === 'ADMIN')

    function setUser(userData, tokenValue) {
        user.value = userData
        token.value = tokenValue
        localStorage.setItem('user', JSON.stringify(userData))
        localStorage.setItem('token', tokenValue)
    }

    function logout() {
        user.value = null
        token.value = ''
        localStorage.removeItem('user')
        localStorage.removeItem('token')
    }

    async function login(email, password) {
        const res = await authApi.login({ email, password })
        setUser(res.user, res.token)
        return res
    }

    async function register(username, email, password) {
        const res = await authApi.register({ username, email, password })
        setUser(res.user, res.token)
        return res
    }

    async function fetchMe() {
        const res = await authApi.me()
        user.value = res
        localStorage.setItem('user', JSON.stringify(res))
        return res
    }

    return { user, token, isLoggedIn, isAdmin, login, register, logout, fetchMe }
})
