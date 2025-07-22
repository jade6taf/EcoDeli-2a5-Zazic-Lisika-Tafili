// Configuration de l'API pour EcoDeli Admin - avec variables d'environnement Railway
export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app',
  ENDPOINTS: {
    AUTH: '/api/auth',
    ADMIN: '/api/admin',
    USERS: '/api/admin/users',
    CONTRATS: '/api/admin/contrats',
    LIVRAISONS: '/api/admin/livraisons',
    PRESTATAIRES: '/api/admin/prestataires'
  }
}

// URL complète pour un endpoint donné
export const getApiUrl = (endpoint) => `${API_CONFIG.BASE_URL}${endpoint}`

// URLs courantes pour l'admin
export const API_URLS = {
  AUTH_LOGIN: getApiUrl(API_CONFIG.ENDPOINTS.AUTH + '/login'),
  AUTH_REGISTER: getApiUrl(API_CONFIG.ENDPOINTS.AUTH + '/register'),
  ADMIN_USERS: getApiUrl(API_CONFIG.ENDPOINTS.USERS),
  ADMIN_CONTRATS: getApiUrl(API_CONFIG.ENDPOINTS.CONTRATS),
  ADMIN_LIVRAISONS: getApiUrl(API_CONFIG.ENDPOINTS.LIVRAISONS),
  ADMIN_PRESTATAIRES: getApiUrl(API_CONFIG.ENDPOINTS.PRESTATAIRES)
}
