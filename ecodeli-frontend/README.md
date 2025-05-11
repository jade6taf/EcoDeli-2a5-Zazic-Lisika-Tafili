# Documentation Technique Frontend EcoDeli

## 🔧 Vue d'ensemble technique

### Stack technique
- Framework : Vue.js 3
- Build tool : Vite

### Structure du projet
```
src/
├── assets/            # Ressources statiques
├── components/        # Composants réutilisables
│   ├── layout/       # Composants de mise en page
│   └── ui/           # Composants d'interface
├── views/            # Pages de l'application
│   ├── admin/       # Interface administrateur
│   ├── client/      # Interface client
│   ├── livreur/     # Interface livreur
│   └── prestataire/ # Interface prestataire
├── router/          # Configuration des routes
├── services/        # Services API
├── stores/          # Stores Pinia
└── utils/           # Utilitaires
```

## 🚀 Installation et configuration

### Prérequis
- Node.js 16.x ou supérieur
- npm 8.x ou supérieur

### Installation
```bash
# Installation des dépendances
npm install

# Lancement en développement
npm run dev

```