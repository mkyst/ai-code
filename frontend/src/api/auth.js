import request from './request'

export const authApi = {
    register: (data) => request.post('/api/auth/register', data),
    login: (data) => request.post('/api/auth/login', data),
    me: () => request.get('/api/auth/me')
}
