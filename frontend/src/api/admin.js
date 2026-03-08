import request from './request'

export const adminApi = {
    // Stats
    getOverview: () => request.get('/api/admin/stats/overview').then(r => r.data),
    getAiLogs: (page = 1, size = 20) => request.get('/api/admin/stats/ai-logs', { params: { page, size } }).then(r => r.data),

    // Users
    getUsers: (page = 1, size = 16, keyword = '') => request.get('/api/admin/users', { params: { page, size, keyword } }).then(r => r.data),
    toggleUserStatus: (id, status) => request.put(`/api/admin/users/${id}/status`, { status }),
    deleteUser: (id) => request.delete(`/api/admin/users/${id}`),

    // Apps
    getApps: (page = 1, size = 16, status = '') => request.get('/api/admin/apps', { params: { page, size, status } }).then(r => r.data),
    setFeatured: (id, featured) => request.put(`/api/admin/apps/${id}/featured`, { featured }),
    deleteApp: (id) => request.delete(`/api/admin/apps/${id}`)
}
