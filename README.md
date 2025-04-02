# EcoDeli

EcoDeli est une plateforme de livraison écologique qui connecte commerçants, prestataires de services, livreurs et clients dans un écosystème durable.

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

## 🏗️ Architecture du projet

```
EcoDeli/
├── ecodeli-backend/         # API REST Spring Boot
│   ├── src/                 # Code source du backend
│   ├── pom.xml              # Configuration Maven
│   └── README.md            # Documentation spécifique au backend
├── ecodeli-frontend/        # Application Vue.js
│   ├── src/                 # Code source du frontend
│   ├── public/              # Ressources statiques
│   └── package.json         # Dépendances npm
└── docs/                    # Documentation du projet
    └── Gestion de projet/   # Documents relatifs à la gestion de projet
```

## 🚀 Installation

### Prérequis
- JDK 17 ou supérieur
- Node.js 16 ou supérieur
- MariaDB 10.x
- Maven 3.6 ou supérieur

### Backend

```bash
cd ecodeli-backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd ecodeli-frontend
npm install
npm run dev
```

## 👥 Équipe
- Zazic Ivan
- Lisika Clara
- Tafili Jade

## 📝 Licence
Ce projet est développé dans le cadre d'un projet d'études.

## 🔗 Liens utiles
- [Documentation API](http://localhost:8080/swagger-ui/index.html) (disponible après lancement du backend)
- [Application web](http://localhost:5173) (disponible après lancement du frontend)
