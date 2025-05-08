# Documentation Technique Frontend EcoDeli

## 1. Vue d'ensemble technique

### Architecture Vue.js
- Framework : Vue.js 3
- Build tool : Vite
- Structure modulaire avec séparation claire des responsabilités
- Organisation en composants réutilisables

### Structure des composants
```
src/
├── components/         # Composants réutilisables
│   ├── layout/        # Composants de mise en page
│   └── DeliveryMap    # Composant de carte
├── views/             # Pages de l'application
│   ├── admin/         # Interface administrateur
│   ├── client/        # Interface client
│   ├── livreur/       # Interface livreur
│   └── prestataire/   # Interface prestataire
├── services/          # Services API et Auth
├── store/             # Gestion d'état
└── assets/            # Ressources statiques
```

### Gestion d'état
- Utilisation du store pour la gestion globale de l'état
- Module auth.js pour la gestion de l'authentification
- État local des composants pour les données spécifiques

### Routing et navigation
- Configuration centralisée dans `router.js`
- Routes protégées par rôle utilisateur
- Gestion des layouts par type d'utilisateur
- Navigation dynamique selon les permissions

### Intégration backend
- Services API centralisés dans `apiServices.js`
- Intercepteurs pour gestion des tokens
- Gestion des erreurs HTTP
- Communication REST avec le backend Spring Boot

## 2. Sécurité côté client

### Gestion des tokens JWT
- Stockage sécurisé des tokens dans le localStorage
- Refresh automatique des tokens
- Déconnexion automatique à expiration
- Intercepteurs axios pour injection automatique

### Gestion des sessions
- Timeout automatique
- Refresh token workflow
- Stockage sécurisé des données de session
- Nettoyage à la déconnexion


### 3. Process de build

```bash
# Installation
npm install

# Développement
npm run dev

```