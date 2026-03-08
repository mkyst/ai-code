import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/HomeView.vue')
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/LoginView.vue'),
        meta: { guest: true }
    },
    {
        path: '/workspace',
        name: 'Workspace',
        component: () => import('../views/WorkspaceView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/apps',
        name: 'Apps',
        component: () => import('../views/AppsView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/app/:id',
        name: 'AppDetail',
        component: () => import('../views/AppDetailView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/admin',
        name: 'Admin',
        component: () => import('../views/AdminView.vue'),
        meta: { requiresAuth: true }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
        next('/login')
    } else if (to.meta.guest && userStore.isLoggedIn) {
        next('/workspace')
    } else {
        next()
    }
})

export default router
