# EcoDeli Backend

EcoDeli est un backend modulaire Java Spring Boot pour une plateforme de livraison durable. Il fournit des API RESTful pour la gestion des utilisateurs, des livraisons, des produits et plus encore, en respectant les bonnes pratiques de structuration du code, de validation et de sécurité.

---

## 📁 Structure du projet

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

## 🔧 Documentation technique

### Configuration Spring Boot

Le projet utilise Spring Boot 3.x avec les dépendances principales suivantes :
- `spring-boot-starter-web` : Support REST API
- `spring-boot-starter-data-jpa` : Persistence des données
- `spring-boot-starter-security` : Sécurité et authentification
- `spring-boot-starter-validation` : Validation des données
- `jsonwebtoken` : Gestion des JWT
- `springdoc-openapi` : Documentation API Swagger
- `lombok` : Réduction du boilerplate
- `mapstruct` : Mapping DTO

### Structure de la base de données

#### Entités principales
- `Utilisateur` : Entité de base pour tous les types d'utilisateurs
- `Client`, `Livreur`, `Commercant` : Héritent d'Utilisateur
- `Annonce` : Représente une demande de livraison
- `Livraison` : Suivi d'une livraison en cours
- `Colis` : Détails du colis à livrer
- `Entrepot` : Points de stockage et transit

#### Relations
- `Utilisateur <-> Roles` : ManyToMany
- `Client <-> Annonce` : OneToMany
- `Livreur <-> Livraison` : OneToMany
- `Annonce <-> Colis` : OneToOne

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

## 🚀 Déploiement

### Configuration des environnements

#### Production
```
Soon to be available
```

#### Développement
```properties
# application-dev.properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
logging.level.root=INFO
```

### Variables d'environnement requises

```bash
# Base de données
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ecodeli
DB_USER=your_user
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email
MAIL_PASSWORD=your-app-password

# Serveur
SERVER_PORT=8080
```

---

## ⚠️ Gestion des erreurs

### Structure des erreurs

Toutes les erreurs retournent un objet JSON standardisé :
```json
{
  "timestamp": "2025-05-11T15:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input",
  "path": "/api/users",
  "details": ["Le champ email est requis"]
}
```

### Codes d'erreur

- 400 : Requête invalide
- 401 : Non authentifié
- 403 : Non autorisé
- 404 : Ressource non trouvée
- 409 : Conflit
- 422 : Entité non traitable
- 500 : Erreur serveur

### Exceptions personnalisées

- `UserNotFoundException`
- `DeliveryNotFoundException`
- `InvalidTokenException`
- `ResourceNotFoundException`
- `DuplicateResourceException`

Toutes les exceptions sont interceptées par `GlobalExceptionHandler`.