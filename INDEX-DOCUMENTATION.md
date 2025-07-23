# Index de la Documentation EcoDeli

Bienvenue dans la documentation complète de la plateforme EcoDeli. Cette documentation couvre tous les aspects techniques et fonctionnels de l'application.

## 📚 Navigation Rapide

### Documentation Principale

| Document | Description | Audience |
|----------|-------------|----------|
| **[DOCUMENTATION.md](DOCUMENTATION.md)** | Documentation complète de l'application | Tous |
| **[README.md](README.md)** | Présentation générale du projet | Tous |

### Guides Pratiques

| Document | Description | Audience |
|----------|-------------|----------|
| **[GUIDE-INSTALLATION.md](GUIDE-INSTALLATION.md)** | Guide d'installation détaillé | Développeurs, DevOps |
| **[GUIDE-UTILISATION.md](GUIDE-UTILISATION.md)** | Guide d'utilisation des interfaces | Utilisateurs finaux |
| **[GUIDE-DEVELOPPEMENT.md](GUIDE-DEVELOPPEMENT.md)** | Guide pour les développeurs | Développeurs |

### Documentation Technique

| Document | Description | Audience |
|----------|-------------|----------|
| **[ARCHITECTURE.md](ARCHITECTURE.md)** | Architecture et conception technique | Architectes, Développeurs |
| **[API-DOCUMENTATION.md](API-DOCUMENTATION.md)** | Documentation de l'API REST | Développeurs frontend/mobile |
| **[TESTS.md](TESTS.md)** | Documentation des tests | Développeurs, QA |

### Guides de Déploiement

| Document | Description | Audience |
|----------|-------------|----------|
| **[RAILWAY-DEPLOYMENT.md](RAILWAY-DEPLOYMENT.md)** | Déploiement sur Railway | DevOps |
| **[RAILWAY-FRONTEND-DEPLOYMENT.md](RAILWAY-FRONTEND-DEPLOYMENT.md)** | Déploiement frontend | DevOps |
| **[FRONTEND-API-MIGRATION.md](FRONTEND-API-MIGRATION.md)** | Migration des API frontend | Développeurs |

---

## 🎯 Accès Rapide par Profil

### 👨‍💻 Développeur Backend
- [Architecture Backend](ARCHITECTURE.md#architecture-backend)
- [Modèle de Données](ARCHITECTURE.md#base-de-données)
- [Documentation API](API-DOCUMENTATION.md)
- [Guide d'Installation](GUIDE-INSTALLATION.md#configuration-du-backend)
- [Tests Backend](TESTS.md#tests-backend)

### 🎨 Développeur Frontend  
- [Architecture Frontend](ARCHITECTURE.md#architecture-frontend)
- [Guide d'Installation](GUIDE-INSTALLATION.md#configuration-frontend-utilisateur)
- [Documentation API](API-DOCUMENTATION.md)
- [Tests Frontend](TESTS.md#tests-frontend)

### 🔧 DevOps / SysAdmin
- [Guide d'Installation](GUIDE-INSTALLATION.md)
- [Déploiement Railway](RAILWAY-DEPLOYMENT.md)
- [Configuration Docker](GUIDE-INSTALLATION.md#configuration-avec-docker)
- [Monitoring](ARCHITECTURE.md#monitoring-et-observabilité)

### 👥 Product Owner / Manager
- [Vue d'ensemble](DOCUMENTATION.md#vue-densemble)
- [Fonctionnalités](DOCUMENTATION.md#fonctionnalités-clés)
- [Guide d'Utilisation](GUIDE-UTILISATION.md)

### 🧪 Testeur / QA
- [Guide d'Installation](GUIDE-INSTALLATION.md)
- [Documentation Tests](TESTS.md)
- [Guide d'Utilisation](GUIDE-UTILISATION.md)

### 👤 Utilisateur Final
- [Guide d'Utilisation](GUIDE-UTILISATION.md)
- [FAQ](FAQ.md)

---

## 📖 Structure de l'Application

### Composants Principaux

```
EcoDeli/
├── ecodeli-backend/          # API REST Spring Boot
├── ecodeli-frontend-user/    # Interface utilisateur Vue.js
├── ecodeli-frontend-admin/   # Interface admin Vue.js
├── ecodeli-analytics/        # Module d'analytics
├── database/                 # Scripts de base de données
├── nginx/                    # Configuration reverse proxy
└── scripts/                  # Scripts utilitaires
```

### Technologies Principales

- **Backend** : Spring Boot 3.2.3, Java 21, PostgreSQL
- **Frontend** : Vue.js 3.x, PrimeVue, Pinia
- **Infrastructure** : Docker, Nginx, Railway
- **Paiements** : Stripe
- **Cartographie** : Google Maps API
- **Communication** : WebSocket

---

## 🚀 Démarrage Rapide

### Pour les Développeurs

1. **Cloner le repository**
   ```bash
   git clone https://github.com/jade6taf/EcoDeli-2a5-Zazic-Lisika-Tafili.git
   cd EcoDeli-2a5-Zazic-Lisika-Tafili
   ```

2. **Suivre le guide d'installation**
   - [Installation complète](GUIDE-INSTALLATION.md)
   - [Installation avec Docker](GUIDE-INSTALLATION.md#configuration-avec-docker)

3. **Configuration de développement**
   - [Configuration Backend](GUIDE-INSTALLATION.md#configuration-du-backend)
   - [Configuration Frontend](GUIDE-INSTALLATION.md#configuration-frontend-utilisateur)

### Pour les Utilisateurs

1. **Accéder à l'application**
   - Interface utilisateur : [URL production]
   - Interface admin : [URL admin]

2. **Créer un compte**
   - Suivre le [guide d'inscription](GUIDE-UTILISATION.md#inscription-et-authentification)

3. **Utiliser les fonctionnalités**
   - [Guide client](GUIDE-UTILISATION.md#interface-client)
   - [Guide livreur](GUIDE-UTILISATION.md#interface-livreur)
   - [Guide commerçant](GUIDE-UTILISATION.md#interface-commerçant)

---

## 🔍 Recherche dans la Documentation

### Par Mot-clé

| Terme | Documents Pertinents |
|-------|---------------------|
| **Installation** | [GUIDE-INSTALLATION.md](GUIDE-INSTALLATION.md) |
| **API** | [API-DOCUMENTATION.md](API-DOCUMENTATION.md) |
| **Architecture** | [ARCHITECTURE.md](ARCHITECTURE.md) |
| **Tests** | [TESTS.md](TESTS.md) |
| **Authentification** | [ARCHITECTURE.md#sécurité](ARCHITECTURE.md), [API-DOCUMENTATION.md#authentification](API-DOCUMENTATION.md) |
| **Base de données** | [ARCHITECTURE.md#base-de-données](ARCHITECTURE.md) |
| **Déploiement** | [RAILWAY-DEPLOYMENT.md](RAILWAY-DEPLOYMENT.md) |
| **WebSocket** | [ARCHITECTURE.md#gestion-des-websocket](ARCHITECTURE.md) |
| **Paiements** | [API-DOCUMENTATION.md#gestion-des-paiements](API-DOCUMENTATION.md) |

### Par Problème Courant

| Problème | Solution |
|----------|----------|
| **L'application ne démarre pas** | [Dépannage](GUIDE-INSTALLATION.md#dépannage) |
| **Erreur de connexion base de données** | [Configuration DB](GUIDE-INSTALLATION.md#configuration-de-la-base-de-données) |
| **Problème d'authentification** | [Sécurité](ARCHITECTURE.md#sécurité) |
| **API ne répond pas** | [Endpoints](API-DOCUMENTATION.md) |
| **Tests échouent** | [Documentation Tests](TESTS.md) |

---

## 📝 Contribution à la Documentation

### Comment Contribuer

1. **Identifier les améliorations**
   - Informations manquantes
   - Erreurs ou imprécisions
   - Exemples supplémentaires

2. **Proposer des modifications**
   - Fork du repository
   - Branche feature pour les modifications
   - Pull request avec description détaillée

3. **Standards de documentation**
   - Français professionnel
   - Structure claire avec titres
   - Exemples pratiques
   - Captures d'écran si pertinentes

### Structure des Documents

- **En-tête** : Titre et vue d'ensemble
- **Table des matières** : Navigation interne
- **Sections thématiques** : Contenu structuré
- **Exemples** : Code et captures d'écran
- **Liens** : Références croisées
- **Pied de page** : Version et date

---

## 📞 Support et Assistance

### Équipe de Développement

- **Zazic Ivan** - Développeur Full Stack
- **Lisika Clara** - Développeur Frontend  
- **Tafili Jade** - Développeur Backend

### Ressources

- **Repository GitHub** : [EcoDeli-2a5-Zazic-Lisika-Tafili](https://github.com/jade6taf/EcoDeli-2a5-Zazic-Lisika-Tafili)
- **Issues GitHub** : Pour signaler des bugs ou proposer des améliorations
- **Wiki** : Documentation communautaire (à venir)

### Contact

Pour toute question concernant la documentation :

1. **Consulter la FAQ** : [FAQ.md](FAQ.md)
2. **Rechercher dans les issues** : GitHub Issues
3. **Créer une nouvelle issue** : Si problème non résolu

---

## 📊 Métriques de Documentation

### Couverture

| Composant | Couverture | Statut |
|-----------|------------|--------|
| **Vue d'ensemble** | 100% | ✅ Complet |
| **Installation** | 100% | ✅ Complet |
| **Architecture** | 100% | ✅ Complet |
| **API REST** | 100% | ✅ Complet |
| **Guide utilisateur** | 100% | ✅ Complet |
| **Tests** | 90% | 🟡 En cours |
| **Déploiement** | 100% | ✅ Complet |

### Dernières Mises à Jour

| Document | Dernière MàJ | Version |
|----------|--------------|---------|
| DOCUMENTATION.md | Décembre 2024 | 1.0 |
| GUIDE-INSTALLATION.md | Décembre 2024 | 1.0 |
| ARCHITECTURE.md | Décembre 2024 | 1.0 |
| API-DOCUMENTATION.md | Décembre 2024 | 1.0 |
| GUIDE-UTILISATION.md | Décembre 2024 | 1.0 |

---

## 🔄 Roadmap Documentation

### Prochaines Améliorations

- [ ] **Diagrammes interactifs** : Architecture système
- [ ] **Tutoriels vidéo** : Installation et utilisation
- [ ] **Documentation API OpenAPI** : Swagger UI intégré
- [ ] **Guides spécifiques** : Par cas d'usage métier
- [ ] **Documentation mobile** : Application mobile future
- [ ] **Monitoring avancé** : Métriques et alertes

### Feedback

Votre feedback est précieux pour améliorer cette documentation. N'hésitez pas à :

- Signaler les informations manquantes
- Proposer des améliorations
- Partager vos cas d'usage
- Suggérer de nouveaux guides

---

*Index de Documentation EcoDeli - Version 1.0 - Décembre 2024*

**Navigation :** [🏠 Accueil](README.md) | [📖 Documentation Complète](DOCUMENTATION.md) | [⚙️ Installation](GUIDE-INSTALLATION.md) | [🎯 Utilisation](GUIDE-UTILISATION.md) | [🏗️ Architecture](ARCHITECTURE.md) | [🔌 API](API-DOCUMENTATION.md)