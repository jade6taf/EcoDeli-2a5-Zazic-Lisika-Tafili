# EcoDeli

EcoDeli est une plateforme de livraison durable qui connecte commerçants, prestataires de services, livreurs et clients dans un écosystème respectueux de l’environnement. Ce projet vise à promouvoir une logistique responsable, à réduire l’impact environnemental et à soutenir le commerce local grâce à une solution logicielle modulaire, sécurisée et maintenable.

---

## 🌱 Objectifs du projet

- Faciliter la livraison et la logistique écologiques pour les entreprises locales et les particuliers.
- Offrir une plateforme sécurisée et intuitive pour la gestion des colis, des annonces et des profils utilisateurs.
- Garantir la modularité et la maintenabilité grâce aux bonnes pratiques de développement backend et frontend.
- Promouvoir la transparence et la collaboration via une documentation claire et des règles de contribution ouvertes.

---

## ✨ Fonctionnalités principales

- **Rôles utilisateurs :** Prise en charge des commerçants, livreurs, prestataires de services et clients.
- **Authentification sécurisée :** Authentification basée sur JWT et contrôle d’accès par rôle.
- **Gestion des annonces :** Création, modification et suivi des annonces de livraison.
- **Suivi des commandes et livraisons :** Mises à jour en temps réel du statut des colis et des livraisons.
- **Gestion des entrepôts et des stocks :** Administration des lieux de stockage et des inventaires produits.
- **Messagerie :** Communication entre utilisateurs pour la coordination.
- **API-First :** API RESTful avec documentation Swagger intégrée.
- **Application web responsive :** Frontend moderne sous Vue.js pour tous les rôles utilisateurs.

---

## 🗂️ Structure du projet & navigation

```
EcoDeli/
├── ecodeli-backend/         # API REST Java Spring Boot
│   ├── src/                 # Code source backend
│   ├── pom.xml              # Configuration Maven
│   └── README.md            # Documentation backend
├── ecodeli-frontend/        # Application Vue.js 3
│   ├── src/                 # Code source frontend
│   ├── public/              # Fichiers statiques
│   └── package.json         # Dépendances npm
└── docs/                    # Documentation du projet
    └── Gestion de projet/   # Documents de gestion de projet
```

- **[Documentation Backend](./ecodeli-backend/README.md) :** Installation, structure de l’API et détails backend.
- **[Documentation Frontend](./ecodeli-frontend/README.md) :** Utilisation, architecture et personnalisation de l’application Vue.js.

---

## 🛠️ Technologies

### Backend

- Java 17/21
- Spring Boot 3.4.x
- Spring Data JPA
- MariaDB
- Maven

### Frontend

- Vue.js 3
- Vue Router
- HTML/CSS/JavaScript
- Vite
- NuxtUI

---

## 🚀 Démarrage rapide

### Prérequis

- JDK 17 ou supérieur
- Node.js 16 ou supérieur
- MariaDB 10.x
- Maven 3.6 ou supérieur

### Installation du backend

```bash
cd ecodeli-backend
./mvnw spring-boot:run
```

### Installation du frontend

```bash
cd ecodeli-frontend
npm install
npm run dev
```

---

## 📚 Documentation API

- **Swagger UI :** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) (disponible après le lancement du backend)
- L’API suit les conventions RESTful et utilise JWT pour l’authentification. Voir le README backend pour les endpoints et l’utilisation.

---

## 👥 Équipe

- Zazic Ivan
- Lisika Clara
- Tafili Jade

---

## 📝 Licence

Ce projet est développé dans le cadre d’un projet académique.

---

## 🔗 Liens utiles

- [Documentation API Backend (Swagger)](http://localhost:8080/swagger-ui/index.html)
- [Application Frontend](http://localhost:5173)
- [Documentation Backend](./ecodeli-backend/README.md)
- [Documentation Frontend](./ecodeli-frontend/README.md)
