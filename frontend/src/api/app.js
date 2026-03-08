import request from './request'

export const appApi = {
    create: (data) => request.post('/api/apps', data).then(r => r.data),
    myApps: (page = 1, size = 20) => request.get('/api/apps/my', { params: { page, size } }).then(r => r.data),
    publicApps: (page = 1, size = 20) => request.get('/api/apps/public', { params: { page, size } }).then(r => r.data),
    getById: (id) => request.get(`/api/apps/${id}`).then(r => r.data),
    update: (id, data) => request.put(`/api/apps/${id}`, data).then(r => r.data),
    delete: (id) => request.delete(`/api/apps/${id}`),
    // Phase 3: deploy & share
    publish: (id) => request.post(`/api/apps/${id}/publish`).then(r => r.data),
    fork: (id) => request.post(`/api/apps/${id}/fork`).then(r => r.data),
    downloadUrl: (id) => `http://localhost:8080/api/apps/${id}/download?token=${localStorage.getItem('token')}`
}

export const aiApi = {
    buildStreamUrl: (appId, prompt) => {
        const token = localStorage.getItem('token')
        return `http://localhost:8080/api/ai/stream?appId=${appId}&prompt=${encodeURIComponent(prompt)}&token=${token}`
    }
}
