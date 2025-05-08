# EcoDeli

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Version](https://img.shields.io/badge/version-1.0.0-blue)]()
[![License](https://img.shields.io/badge/license-Academic-yellow)]()
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-42b883)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-6db33f)]()

EcoDeli est une plateforme de livraison durable qui connecte commerçants, prestataires de services, livreurs et clients dans un écosystème respectueux de l'environnement. Ce projet vise à promouvoir une logistique responsable, à réduire l'impact environnemental et à soutenir le commerce local grâce à une solution logicielle modulaire, sécurisée et maintenable.

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

## 🏗️ Architecture technique

### Backend (Spring Boot)
- API REST sécurisée
- Architecture en couches (MVC)
- Persistence avec JPA/Hibernate

### Frontend (Vue.js)
- Architecture modulaire
- State management centralisé
- Composants réutilisables

---

## 🚀 Installation et configuration

### Prérequis
- JDK 17 ou supérieur
- Node.js 16 ou supérieur
- MariaDB 10.x
- Maven 3.6 ou supérieur

### Configuration de l'environnement
```bash
# Variables d'environnement requises
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=ecodeli
export JWT_SECRET=your-secret-key
```

### Installation

1. Backend
```bash
cd ecodeli-backend
./mvnw spring-boot:run
```

2. Frontend
```bash
cd ecodeli-frontend
npm install
npm run dev
```

---

### Conventions
- Commit conventionnels (feat:, fix:, docs:, etc.)
- Documentation des changements
- Tests requis pour les nouvelles fonctionnalités
- Code review obligatoire

---

## 👥 Équipe

- Zazic Ivan
- Lisika Clara
- Tafili Jade

---

## 📝 Licence

Ce projet est développé dans le cadre d'un projet académique.

---

## 🔗 Liens utiles

- [Documentation API (Swagger)](http://localhost:8080/swagger-ui/index.html)
- [Application Frontend](http://localhost:5173)
- [Documentation Backend](./ecodeli-backend/README.md)
- [Documentation Frontend](./ecodeli-frontend/README.md)
