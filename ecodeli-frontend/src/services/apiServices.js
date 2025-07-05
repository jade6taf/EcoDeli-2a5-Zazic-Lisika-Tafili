import authService from './authServices';

export default {

    async fetch(endpoint, options = {}) {
        const token = authService.getToken();
        const headers = options.headers || {};

        if (!(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        }

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        try {
            const response = await fetch(`/api/${endpoint}`, {
                ...options,
                headers
            });
            if (response.status === 401) {
                authService.logout();
                throw new Error('Session expirée, veuillez vous reconnecter');
            }
            return response;
        } catch (error) {
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

    async postFormData(endpoint, formData) {
        return this.fetch(endpoint, {
            method: 'POST',
            body: formData
        });
    },

    async put(endpoint, data) {
        return this.fetch(endpoint, {
        method: 'PUT',
        body: JSON.stringify(data)
        });
    },

    async patch(endpoint, data) {
        return this.fetch(endpoint, {
            method: 'PATCH',
            body: JSON.stringify(data)
        });
    },

    async delete(endpoint) {
        return this.fetch(endpoint, {
        method: 'DELETE'
        });
    },

    async getPrestatairesByServiceType(serviceType) {
        try {
            const response = await this.get(`prestataires/by-service/${serviceType}`);
            if (!response.ok) {
                throw new Error(`Erreur lors de la récupération des prestataires: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            throw error;
        }
    },

    async calculateDeliveryPrice(data) {
        try {
            const response = await this.post('maps/calculate-price', data);
            if (!response.ok) {
                throw new Error(`Erreur lors du calcul du prix: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            throw error;
        }
    },

    async getAvailableWarehouses() {
        try {
            const response = await this.get('maps/warehouses');
            if (!response.ok) {
                throw new Error(`Erreur lors de la récupération des entrepôts: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            throw error;
        }
    },
};
