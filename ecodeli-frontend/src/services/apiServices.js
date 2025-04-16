import authService from './authService';

const API_BASE_URL = 'http://localhost:8080/api/';

export default {

    async fetch(endpoint, options = {}) {
        const token = authService.getToken();
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers
        });
        if (response.status === 401) {
            authService.logout();
            throw new Error('Session expirée, veuillez vous reconnecter');
        }
        return response;
    },

    async get(endpoint) {
        return this.fetch(endpoint);
    },

    async post(endpoint, data) {
        return this.fetch(endpoint, {
        method: 'POST',
        body: JSON.stringify(data)
        });
    },

    async put(endpoint, data) {
        return this.fetch(endpoint, {
        method: 'PUT',
        body: JSON.stringify(data)
        });
    },

    async delete(endpoint) {
        return this.fetch(endpoint, {
        method: 'DELETE'
        });
    }
};
