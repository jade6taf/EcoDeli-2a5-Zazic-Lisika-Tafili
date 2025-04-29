# EcoDeli Frontend

EcoDeli Frontend est une application web moderne développée avec Vue 3 et Vite, offrant une interface intuitive pour la plateforme de livraison durable EcoDeli. Elle permet aux clients, livreurs et commerçants d’interagir efficacement avec le système : gestion des annonces, suivi des livraisons, authentification sécurisée, et bien plus.

---

## Table des matières

- [Fonctionnalités principales](#fonctionnalités-principales)
- [Structure du projet](#structure-du-projet)
- [Démarrage rapide](#démarrage-rapide)
- [Informations utiles](#informations-utiles)

---

## Fonctionnalités principales

- Authentification et gestion des profils utilisateurs (clients, livreurs, commerçants)
- Création, édition et consultation d’annonces de livraison
- Tableau de bord personnalisé selon le rôle
- Suivi des livraisons en temps réel
- Consultation des services et informations sur la plateforme
- Interface responsive et ergonomique

---

## Structure du projet

```
ecodeli-frontend/
├── public/                     # Fichiers statiques (favicon, etc.)
├── src/
│   ├── assets/                 # Images et ressources statiques
│   ├── components/             # Composants Vue réutilisables (ex : Header)
│   │   └── layout/
│   ├── services/               # Services d’accès à l’API (auth, données)
│   ├── views/                  # Pages principales (Home, Login, Dashboard, etc.)
│   │   ├── client/             # Vues spécifiques au client
│   │   └── livreur/            # Vues spécifiques au livreur
│   ├── App.vue                 # Composant racine
│   ├── main.js                 # Point d’entrée de l’application
│   └── router.js               # Configuration du routage
├── package.json                # Dépendances et scripts npm
├── vite.config.js              # Configuration Vite
└── README.md                   # Documentation du projet
```

---

## Démarrage rapide

### Prérequis

- Node.js 18+ recommandé
- npm 9+ ou yarn

### Installation & Lancement

```bash
# Cloner le dépôt
git clone https://github.com/your-org/ecodeli-frontend.git
cd ecodeli-frontend

# Installer les dépendances
npm install

# Lancer l’application en développement
npm run dev
```

L’application sera accessible sur [http://localhost:5173](http://localhost:5173) (ou le port affiché en console).

---

## Informations utiles

- **Documentation Vue** : [https://vuejs.org/](https://vuejs.org/)
- **Documentation Vite** : [https://vitejs.dev/](https://vitejs.dev/)
- **Charte graphique et maquettes** : voir `/docs/UI:UX/`

Pour toute question, contactez l’équipe projet !
