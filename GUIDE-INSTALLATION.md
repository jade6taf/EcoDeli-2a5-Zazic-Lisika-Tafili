# Guide d'Installation EcoDeli

## Vue d'ensemble

Ce guide détaille l'installation et la configuration complète de la plateforme EcoDeli en environnement de développement et de production.

## Prérequis Système

### Environnement de Développement

| Composant | Version Minimum | Version Recommandée |
|-----------|-----------------|-------------------|
| Java JDK | 21 | 21+ |
| Node.js | 16.x | 18.x ou 20.x |
| npm | 8.x | 9.x+ |
| PostgreSQL | 12 | 15+ |
| Git | 2.x | 2.40+ |
| Docker | 20.x | 24.x+ |
| Docker Compose | 2.x | 2.20+ |

### Environnement de Production

- **Serveur** : 2 CPU, 4GB RAM minimum
- **Base de données** : PostgreSQL avec 20GB d'espace
- **Réseau** : Connexion HTTPS avec certificat SSL

## Installation Pas à Pas

### 1. Clonage du Repository

```bash
# Cloner le repository
git clone https://github.com/jade6taf/EcoDeli-2a5-Zazic-Lisika-Tafili.git

# Accéder au dossier
cd EcoDeli-2a5-Zazic-Lisika-Tafili

# Vérifier la structure
ls -la
```

### 2. Configuration de PostgreSQL

#### Installation PostgreSQL (Ubuntu/Debian)

```bash
# Mettre à jour les paquets
sudo apt update

# Installer PostgreSQL
sudo apt install postgresql postgresql-contrib

# Démarrer le service
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

#### Configuration de la Base de Données

```bash
# Se connecter en tant qu'utilisateur postgres
sudo -u postgres psql

# Créer la base de données et l'utilisateur
CREATE DATABASE ecodeli;
CREATE USER ecodeli_user WITH PASSWORD 'ecodeli_password';
GRANT ALL PRIVILEGES ON DATABASE ecodeli TO ecodeli_user;

# Quitter PostgreSQL
\q
```

#### Test de Connexion

```bash
# Tester la connexion
psql -h localhost -U ecodeli_user -d ecodeli
```

### 3. Configuration du Backend

#### Variables d'Environnement

Créer le fichier `ecodeli-backend/.env` :

```env
# Base de données
DATABASE_URL=jdbc:postgresql://localhost:5432/ecodeli
DATABASE_USERNAME=ecodeli_user
DATABASE_PASSWORD=ecodeli_password

# JWT
JWT_SECRET=VotreCleSecrete64CaracteresMinimumPourLaSecuriteMaximale2024

# Google Maps API
GOOGLE_MAPS_API_KEY=votre_cle_google_maps_api

# Stripe
STRIPE_PUBLISHABLE_KEY=pk_test_votre_cle_publique_stripe
STRIPE_SECRET_KEY=sk_test_votre_cle_secrete_stripe
STRIPE_WEBHOOK_SECRET=whsec_votre_webhook_secret

# Email (Gmail)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre.email@gmail.com
MAIL_PASSWORD=votre_mot_de_passe_application
```

#### Configuration application.yml

Modifier `ecodeli-backend/src/main/resources/application.yml` :

```yaml
spring:
  application:
    name: ecodeli-backend
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/ecodeli}
    username: ${DATABASE_USERNAME:ecodeli_user}
    password: ${DATABASE_PASSWORD:ecodeli_password}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

server:
  port: 8080
  servlet:
    context-path: /

jwt:
  secret: ${JWT_SECRET:default-secret-key-for-development-only}
  expiration: 86400000 # 24 heures

google:
  maps:
    api-key: ${GOOGLE_MAPS_API_KEY:}

stripe:
  public-key: ${STRIPE_PUBLISHABLE_KEY:}
  secret-key: ${STRIPE_SECRET_KEY:}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET:}

logging:
  level:
    com.ecodeli: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

#### Installation et Démarrage Backend

```bash
# Accéder au dossier backend
cd ecodeli-backend

# Donner les permissions d'exécution au wrapper Maven
chmod +x mvnw

# Installer les dépendances et compiler
./mvnw clean install

# Démarrer l'application
./mvnw spring-boot:run
```

#### Vérification Backend

```bash
# Tester l'API
curl http://localhost:8080/actuator/health

# Réponse attendue
{"status":"UP"}
```

### 4. Configuration Frontend Utilisateur

#### Configuration API

Créer `ecodeli-frontend-user/src/config/api.js` :

```javascript
// Configuration de l'API
export const API_CONFIG = {
  BASE_URL: process.env.NODE_ENV === 'production' 
    ? 'https://votre-backend-production.railway.app'
    : 'http://localhost:8080',
  TIMEOUT: 10000,
  RETRY_ATTEMPTS: 3
}

// URLs des endpoints
export const API_ENDPOINTS = {
  // Authentification
  AUTH: {
    LOGIN: '/api/auth/login',
    REGISTER: '/api/auth/register',
    REFRESH: '/api/auth/refresh',
    LOGOUT: '/api/auth/logout'
  },
  
  // Utilisateurs
  USERS: {
    PROFILE: '/api/users/profile',
    UPDATE_PROFILE: '/api/users/profile',
    DELETE_ACCOUNT: '/api/users/account'
  },
  
  // Annonces
  ANNONCES: {
    LIST: '/api/annonces',
    CREATE: '/api/annonces',
    DETAIL: '/api/annonces',
    UPDATE: '/api/annonces',
    DELETE: '/api/annonces'
  },
  
  // Livraisons
  LIVRAISONS: {
    LIST: '/api/livraisons',
    CREATE: '/api/livraisons',
    TRACKING: '/api/livraisons',
    UPDATE_STATUS: '/api/livraisons'
  },
  
  // Paiements
  PAIEMENTS: {
    CREATE_INTENT: '/api/paiements/create-payment-intent',
    CONFIRM: '/api/paiements/confirm-payment',
    HISTORY: '/api/paiements/history'
  }
}
```

#### Installation Frontend Utilisateur

```bash
# Accéder au dossier frontend utilisateur
cd ../ecodeli-frontend-user

# Installer les dépendances
npm install

# Démarrer en mode développement
npm run dev
```

#### Vérification Frontend Utilisateur

L'application sera accessible sur `http://localhost:5173`

### 5. Configuration Frontend Admin

#### Installation Frontend Admin

```bash
# Accéder au dossier frontend admin
cd ../ecodeli-frontend-admin

# Copier la configuration API depuis le frontend utilisateur
cp ../ecodeli-frontend-user/src/config/api.js src/config/

# Installer les dépendances
npm install

# Démarrer en mode développement
npm run dev
```

#### Vérification Frontend Admin

L'application sera accessible sur `http://localhost:5174`

### 6. Configuration du Module Analytics

```bash
# Accéder au dossier analytics
cd ../ecodeli-analytics

# Vérifier la configuration
cat pom.xml

# Installer et démarrer
./mvnw clean install
./mvnw spring-boot:run
```

## Configuration avec Docker

### Docker Compose pour Développement

Créer `docker-compose.dev.yml` :

```yaml
version: '3.8'

services:
  postgresql:
    image: postgres:15
    environment:
      POSTGRES_DB: ecodeli
      POSTGRES_USER: ecodeli_user
      POSTGRES_PASSWORD: ecodeli_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ecodeli_user -d ecodeli"]
      interval: 30s
      timeout: 10s
      retries: 3

  backend:
    build:
      context: ./ecodeli-backend
      dockerfile: Dockerfile
    environment:
      DATABASE_URL: jdbc:postgresql://postgresql:5432/ecodeli
      DATABASE_USERNAME: ecodeli_user
      DATABASE_PASSWORD: ecodeli_password
      SPRING_PROFILES_ACTIVE: development
    ports:
      - "8080:8080"
    depends_on:
      postgresql:
        condition: service_healthy
    volumes:
      - ./ecodeli-backend:/app
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  frontend-user:
    build:
      context: ./ecodeli-frontend-user
      dockerfile: Dockerfile
    ports:
      - "5173:5173"
    environment:
      VITE_API_BASE_URL: http://localhost:8080
    volumes:
      - ./ecodeli-frontend-user:/app
      - /app/node_modules
    depends_on:
      - backend

  frontend-admin:
    build:
      context: ./ecodeli-frontend-admin
      dockerfile: Dockerfile
    ports:
      - "5174:5174"
    environment:
      VITE_API_BASE_URL: http://localhost:8080
    volumes:
      - ./ecodeli-frontend-admin:/app
      - /app/node_modules
    depends_on:
      - backend

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/conf.d:/etc/nginx/conf.d
    depends_on:
      - backend
      - frontend-user
      - frontend-admin

volumes:
  postgres_data:
```

### Démarrage avec Docker

```bash
# Démarrer tous les services
docker-compose -f docker-compose.dev.yml up -d

# Voir les logs
docker-compose -f docker-compose.dev.yml logs -f

# Arrêter les services
docker-compose -f docker-compose.dev.yml down
```

## Configuration de Production

### Variables d'Environnement Production

```env
# Base de données (Railway PostgreSQL)
DATABASE_URL=postgresql://user:password@host:port/database

# JWT avec clé forte
JWT_SECRET=cle_secrete_ultra_secure_production_64_caracteres_minimum

# API Keys production
GOOGLE_MAPS_API_KEY=cle_production_google_maps
STRIPE_PUBLISHABLE_KEY=pk_live_...
STRIPE_SECRET_KEY=sk_live_...

# Email production
MAIL_USERNAME=noreply@ecodeli.fr
MAIL_PASSWORD=mot_de_passe_application_gmail

# Configuration production
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
```

### Déploiement sur Railway

#### 1. Configuration railway.toml

```toml
[build]
builder = "DOCKERFILE"
dockerfilePath = "Dockerfile"

[deploy]
healthcheckPath = "/actuator/health"
healthcheckTimeout = 300
restartPolicyType = "ON_FAILURE"
restartPolicyMaxRetries = 10

[environments.production]
[environments.production.services.backend]
source = "ecodeli-backend"
build.watchPatterns = ["ecodeli-backend/**"]

[environments.production.services.frontend-user]
source = "ecodeli-frontend-user"
build.watchPatterns = ["ecodeli-frontend-user/**"]

[environments.production.services.frontend-admin]
source = "ecodeli-frontend-admin"
build.watchPatterns = ["ecodeli-frontend-admin/**"]
```

#### 2. Dockerfile Multi-stage

```dockerfile
# Multi-stage Dockerfile pour production
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY ecodeli-backend/pom.xml .
COPY ecodeli-backend/src ./src
RUN mvn clean package -DskipTests

FROM node:18-alpine AS frontend-user-build
WORKDIR /app
COPY ecodeli-frontend-user/package*.json ./
RUN npm ci --only=production
COPY ecodeli-frontend-user/ .
RUN npm run build

FROM node:18-alpine AS frontend-admin-build
WORKDIR /app
COPY ecodeli-frontend-admin/package*.json ./
RUN npm ci --only=production
COPY ecodeli-frontend-admin/ .
RUN npm run build

FROM eclipse-temurin:21-jre-jammy AS production
WORKDIR /app

# Installer nginx
RUN apt-get update && apt-get install -y nginx && rm -rf /var/lib/apt/lists/*

# Copier les artefacts
COPY --from=backend-build /app/target/*.jar app.jar
COPY --from=frontend-user-build /app/dist /var/www/user
COPY --from=frontend-admin-build /app/dist /var/www/admin

# Configuration nginx
COPY nginx/nginx.conf /etc/nginx/nginx.conf

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

#### 3. Déploiement

```bash
# Connecter Railway
npm install -g @railway/cli
railway login

# Créer le projet
railway init
railway link

# Ajouter PostgreSQL
railway add postgresql

# Déployer
git add .
git commit -m "Deploy to production"
git push origin main
```

## Vérification de l'Installation

### Checklist Backend

- [ ] Application démarre sans erreur
- [ ] Base de données connectée
- [ ] Endpoint `/actuator/health` retourne `{"status":"UP"}`
- [ ] API Swagger accessible sur `/swagger-ui.html`
- [ ] Logs n'affichent pas d'erreurs critiques

### Checklist Frontend Utilisateur

- [ ] Application charge sans erreur
- [ ] Page d'accueil s'affiche correctement
- [ ] Formulaire d'inscription fonctionne
- [ ] Connexion/déconnexion fonctionnelle
- [ ] Requêtes API vers le backend réussies

### Checklist Frontend Admin

- [ ] Interface d'administration accessible
- [ ] Tableau de bord charge les données
- [ ] Gestion des utilisateurs fonctionnelle
- [ ] Métriques et graphiques s'affichent

### Tests de Bout en Bout

```bash
# Test d'inscription
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Test",
    "prenom": "User",
    "email": "test@example.com",
    "motDePasse": "motdepasse123",
    "type": "CLIENT"
  }'

# Test de connexion
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "motDePasse": "motdepasse123"
  }'
```

## Dépannage

### Problèmes Courants

#### Backend ne démarre pas

```bash
# Vérifier les logs
./mvnw spring-boot:run --debug

# Vérifier la base de données
psql -h localhost -U ecodeli_user -d ecodeli -c "SELECT version();"

# Vérifier les variables d'environnement
env | grep DATABASE
```

#### Frontend ne se connecte pas au backend

```bash
# Vérifier la configuration API
cat src/config/api.js

# Vérifier le CORS
curl -H "Origin: http://localhost:5173" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: X-Requested-With" \
     -X OPTIONS \
     http://localhost:8080/api/auth/login
```

#### Erreurs de base de données

```sql
-- Vérifier les tables
\dt

-- Vérifier les données
SELECT COUNT(*) FROM utilisateur;

-- Recréer les tables si nécessaire
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

### Logs et Diagnostics

```bash
# Logs backend
tail -f ecodeli-backend/logs/application.log

# Logs frontend (navigateur)
# Ouvrir F12 > Console

# Logs base de données
sudo tail -f /var/log/postgresql/postgresql-15-main.log

# Ressources système
top -p $(pgrep -f "ecodeli")
```

## Support

Pour toute question ou problème d'installation :

1. Consulter la [documentation complète](DOCUMENTATION.md)
2. Vérifier les [issues GitHub](https://github.com/jade6taf/EcoDeli-2a5-Zazic-Lisika-Tafili/issues)
3. Contacter l'équipe de développement

---

*Guide d'installation EcoDeli - Version 1.0 - Décembre 2024*