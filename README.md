# EcoDeli

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Version](https://img.shields.io/badge/version-1.0.0-blue)]()
[![License](https://img.shields.io/badge/license-Academic-yellow)]()
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-42b883)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-6db33f)]()

EcoDeli est une plateforme de livraison durable qui connecte commerçants, prestataires de services, livreurs et clients dans un écosystème respectueux de l'environnement. Ce projet vise à promouvoir une logistique responsable, à réduire l'impact environnemental et à soutenir le commerce local grâce à une solution logicielle modulaire, sécurisée et maintenable.

---

## 📋 Prérequis système

### Backend
- JDK 17 ou supérieur
- Maven 3.6 ou supérieur
- MariaDB 10.6 ou supérieur

### Frontend
- Node.js 16.x ou supérieur
- npm 8.x ou supérieur
- Vue.js 3.x
- Navigateurs supportés :
  - Chrome
  - Firefox
  - Safari
  - Edge

### Outils recommandés
- Git 2.30+
- VS Code avec extensions recommandées
- Postman pour les tests API

---

## 🌱 Objectifs du projet

- Faciliter la livraison et la logistique écologiques pour les entreprises locales et les particuliers
- Offrir une plateforme sécurisée et intuitive pour la gestion des colis, des annonces et des profils utilisateurs
- Garantir la modularité et la maintenabilité grâce aux bonnes pratiques de développement backend et frontend
- Promouvoir la transparence et la collaboration via une documentation claire et des règles de contribution ouvertes

---

## ✨ Fonctionnalités principales

### 🔐 Système d'authentification et sécurité
- Authentification basée sur JWT (JSON Web Tokens)
- Validation en deux étapes (2FA) disponible
- Gestion fine des permissions par rôle
- Chiffrement des données sensibles
- Protection contre les attaques CSRF et XSS

### 👥 Gestion des utilisateurs et rôles
- Administration complète des profils utilisateurs
- Rôles distincts : Administrateur, Client, Commerçant, Livreur, Prestataire
- Tableau de bord personnalisé par rôle
- Système de notation et d'évaluation

### 📦 Système de livraison
- Suivi en temps réel des livraisons
- Géolocalisation des colis et livreurs
- Gestion des retours et incidents

### 🛡️ Protection des données
- Conformité RGPD
- Chiffrement des données personnelles
- Politique de conservation des données
- Journalisation des actions sensibles
- Sauvegarde régulière des données

---

## 🚀 Installation et démarrage

### 1. Configuration de l'environnement

```bash
# Créer et configurer le fichier .env à la racine
cp .env.example .env

# Variables d'environnement requises
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ecodeli
DB_USER=your_user
DB_PASSWORD=your_password
JWT_SECRET=your-secret-key
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email
MAIL_PASSWORD=your-app-password
```

### 2. Installation de la base de données

```bash
# Installer MariaDB
sudo apt update
sudo apt install mariadb-server

# Sécuriser l'installation
sudo mysql_secure_installation

# Créer la base de données
mysql -u root -p
CREATE DATABASE ecodeli;
```

### 3. Backend

```bash
cd ecodeli-backend

# Installer les dépendances
./mvnw clean install

# Démarrer le serveur
./mvnw spring-boot:run
```

### 4. Frontend

```bash
cd ecodeli-frontend

# Installer les dépendances
npm install

# Lancer en mode développement
npm run dev

```

---

## 📚 Documentation API

### Points d'entrée principaux

#### Authentification
- POST /api/auth/login : Connexion utilisateur
- POST /api/auth/register : Inscription nouvel utilisateur
- POST /api/auth/logout : Déconnexion
- GET /api/auth/me : Informations utilisateur connecté

#### Livraisons
- GET /api/deliveries : Liste des livraisons
- POST /api/deliveries : Créer une livraison
- GET /api/deliveries/{id} : Détails d'une livraison
- PUT /api/deliveries/{id}/status : Mettre à jour le statut

#### Utilisateurs
- GET /api/users : Liste des utilisateurs
- POST /api/users : Créer un utilisateur
- PUT /api/users/{id} : Modifier un utilisateur
- DELETE /api/users/{id} : Supprimer un utilisateur

Documentation complète disponible sur Swagger UI : http://localhost:8080/swagger-ui.html

---

## 👥 Équipe

- Zazic Ivan
- Lisika Clara
- Tafili Jade

---

## 📄 Licence

Ce projet est développé dans le cadre d'un projet académique. Tous droits réservés.

---

## 🔗 Liens utiles

- [Documentation API (Swagger)](http://localhost:8080/swagger-ui/index.html)
- [Application Frontend](http://localhost:5173)
- [Documentation Backend](./ecodeli-backend/README.md)
- [Documentation Frontend](./ecodeli-frontend/README.md)
