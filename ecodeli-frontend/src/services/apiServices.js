import authService from './authServices';

const API_BASE_URL = 'http://localhost:8080/api/';

// Assurons-nous que l'URL est correcte
console.log('URL de base API:', API_BASE_URL);

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

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                ...options,
                headers
            });

            if (response.status === 401 || response.status === 403) {
                authService.logout();
                throw new Error('Session expirée, veuillez vous reconnecter');
            }

            return response;
        } catch (error) {
            console.error('Erreur lors de la requête:', error);
            throw error;
        }
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
