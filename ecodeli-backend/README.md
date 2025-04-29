# EcoDeli Backend

EcoDeli est un backend modulaire Java Spring Boot pour une plateforme de livraison durable. Il fournit des API RESTful pour la gestion des utilisateurs, des livraisons, des produits et plus encore, en respectant les bonnes pratiques de structuration du code, de validation et de sécurité.

---

## Table des matières

- [Structure du projet](#structure-du-projet)
- [Démarrage rapide](#démarrage-rapide)
- [Documentation de l’API](#documentation-de-lapi)
- [Sécurité & Authentification](#sécurité--authentification)

---

## Structure du projet

```
ecodeli-backend/
├── src/
│   └── main/
│       └── java/com/ecodeli/ecodeli_backend/
│           ├── config/         # Configuration de la sécurité et de l’application
│           ├── controllers/    # Contrôleurs REST API
│           ├── models/         # Modèles de domaine et DTOs
│           ├── repositories/   # Repositories Spring Data JPA
│           ├── security/       # JWT et filtres de sécurité
│           └── services/       # Logique métier et services
├── resources/
│   └── application.properties  # Configuration de l’application
├── pom.xml                     # Fichier de build Maven
└── README.md                   # Documentation du projet
```

---

## Démarrage rapide

### Prérequis

- Java 17+
- Maven 3.6+
- (Optionnel) Docker

### Installation & Lancement

```bash
# Cloner le dépôt
git clone https://github.com/your-org/ecodeli-backend.git
cd ecodeli-backend

# Compiler le projet
./mvnw clean install

# Lancer l’application
./mvnw spring-boot:run
```

Le backend démarre sur [http://localhost:8080](http://localhost:8080).

---

## Documentation de l’API

La documentation complète de l’API est disponible via Swagger UI :

- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Cette documentation interactive détaille tous les endpoints, les formats de requête/réponse et les exigences d’authentification.

---

## Sécurité & Authentification

EcoDeli utilise JWT (JSON Web Token) pour une authentification et une autorisation sans état.

### Fonctionnement

- Les utilisateurs s’authentifient via `/auth/login` et reçoivent un token JWT.
- Le token doit être inclus dans l’en-tête `Authorization` sous la forme `Bearer <token>` pour accéder aux endpoints protégés.
- Les rôles et permissions sont gérés via Spring Security.

### Configuration

- Les paramètres de sécurité sont définis dans `SecurityConfig.java` et `application.properties`.
- Le secret JWT et la durée d’expiration se configurent dans `application.properties` :

```properties
jwt.secret=your_jwt_secret
jwt.expiration=3600000
```

- Le filtre `JwtRequestFilter` valide les tokens à chaque requête.

### Exemple d’utilisation

```http
POST /auth/login
{
  "username": "user",
  "password": "pass"
}
# Réponse : { "token": "<jwt_token>" }

GET /api/protected/resource
Authorization: Bearer <jwt_token>
```
