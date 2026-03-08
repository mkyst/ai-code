import request from './request'

export const editApi = {
    // AI edit via SSE (returns URL for EventSource)
    buildEditStreamUrl: (appId, message, selectedElement) => {
        const token = localStorage.getItem('token')
        const params = new URLSearchParams({
            appId,
            message,
            token,
            ...(selectedElement ? { selectedElement } : {})
        })
        return `http://localhost:8080/api/edit/stream?${params.toString()}`
    },

    // Version history
    getVersions: (appId) => request.get(`/api/edit/versions/${appId}`),
    restoreVersion: (versionId) => request.post(`/api/edit/versions/${versionId}/restore`),

    // Chat history
    getChatHistory: (appId) => request.get(`/api/edit/chat/${appId}`),
    clearChatHistory: (appId) => request.delete(`/api/edit/chat/${appId}`)
}
