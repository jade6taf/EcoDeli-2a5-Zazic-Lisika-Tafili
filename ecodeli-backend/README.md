# EcoDeli Backend

EcoDeli est un backend modulaire Java Spring Boot pour une plateforme de livraison durable. Il fournit des API RESTful pour la gestion des utilisateurs, des livraisons, des produits et plus encore, en respectant les bonnes pratiques de structuration du code, de validation et de sécurité.

---

## Table des matières

- [Structure du projet](#structure-du-projet)
- [Documentation technique](#documentation-technique)
  - [Configuration Spring Boot](#configuration-spring-boot)
  - [Structure de la base de données](#structure-de-la-base-de-données)
  - [Système d'authentification](#système-dauthentification)
  - [Gestion des rôles](#gestion-des-rôles)
- [Démarrage rapide](#démarrage-rapide)

---

## Structure du projet

```
ecodeli-backend/
├── src/
│   └── main/
│       └── java/com/ecodeli/ecodeli_backend/
│           ├── config/         # Configuration de la sécurité et de l'application
│           ├── controllers/    # Contrôleurs REST API
│           ├── models/         # Modèles de domaine et DTOs
│           ├── repositories/   # Repositories Spring Data JPA
│           ├── security/       # JWT et filtres de sécurité
│           └── services/       # Logique métier et services
├── resources/
│   └── application.properties  # Configuration de l'application
├── pom.xml                     # Fichier de build Maven
└── README.md                   # Documentation du projet
```

---

## Documentation technique

### Configuration Spring Boot

Le projet utilise Spring Boot 3.x avec les dépendances principales suivantes :
- `spring-boot-starter-web` : Support REST API
- `spring-boot-starter-data-jpa` : Persistence des données
- `spring-boot-starter-security` : Sécurité et authentification
- `spring-boot-starter-validation` : Validation des données
- `jsonwebtoken` : Gestion des JWT

### Structure de la base de données

La base de données est structurée autour des entités principales suivantes :
- `Utilisateur` : Entité de base pour tous les types d'utilisateurs
- `Client`, `Livreur`, `Commercant` : Héritent d'Utilisateur
- `Annonce` : Représente une demande de livraison
- `Livraison` : Suivi d'une livraison en cours
- `Colis` : Détails du colis à livrer
- `Entrepot` : Points de stockage et transit

Les relations sont gérées via JPA avec lazy loading pour optimiser les performances.

### Système d'authentification

#### Flux d'authentification JWT

1. L'utilisateur s'authentifie via `/auth/login`
2. Le serveur valide les credentials et génère un JWT
3. Le token est retourné dans la réponse
4. Le client inclut le token dans le header `Authorization`
5. `JwtRequestFilter` valide le token à chaque requête

#### Configuration JWT

```properties
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000  # 24 heures
jwt.refresh.expiration=604800000  # 7 jours
```

### Gestion des rôles

Les rôles sont définis dans l'entité `Utilisateur` :
- ROLE_ADMIN
- ROLE_CLIENT
- ROLE_LIVREUR
- ROLE_COMMERCANT

La sécurité est configurée dans `SecurityConfig.java` avec des règles spécifiques par rôle.

---

## Démarrage rapide

### Prérequis

- Java 17+
- Maven 3.6+
- Docker (optionnel)

### Installation & Lancement

```bash
# Cloner le dépôt
git clone https://github.com/jade6taf/EcoDeli-2a5-Zazic-Lisika-Tafili.git
cd ecodeli-backend

# Compiler le projet
./mvnw clean install

# Lancer l'application
./mvnw spring-boot:run
```

Le backend démarre sur [http://localhost:8080](http://localhost:8080).
