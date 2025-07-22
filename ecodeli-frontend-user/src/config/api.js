// Configuration de l'API pour EcoDeli
export const API_CONFIG = {
  // Utilise la variable d'environnement Vite en production, sinon fallback sur Railway
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app',
  ENDPOINTS: {
    AUTH: '/api/auth',
    USERS: '/api/users',
    ANNONCES: '/api/annonces',
    SERVICES: '/api/services',
    CONTRATS: '/api/contrats',
    LIVRAISONS: '/api/livraisons',
    CANDIDATURES: '/api/candidatures',
    PRESTATAIRES: '/api/prestataires',
    PAIEMENTS: '/api/paiements'
  }
}

// URL complète pour un endpoint donné
export const getApiUrl = (endpoint) => `${API_CONFIG.BASE_URL}${endpoint}`

// URLs courantes
export const API_URLS = {
  AUTH_LOGIN: getApiUrl(API_CONFIG.ENDPOINTS.AUTH + '/login'),
  AUTH_REGISTER: getApiUrl(API_CONFIG.ENDPOINTS.AUTH + '/register'),
  // Ajouter d'autres URLs selon les besoins
}
