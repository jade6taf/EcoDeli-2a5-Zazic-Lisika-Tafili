# Documentation Complète EcoDeli

## Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture](#architecture)
3. [Installation et Configuration](#installation-et-configuration)
4. [Documentation Technique](#documentation-technique)
5. [Guide d'Utilisation](#guide-dutilisation)
6. [Guide de Développement](#guide-de-développement)
7. [Maintenance et Opérations](#maintenance-et-opérations)

---

## Vue d'ensemble

EcoDeli est une plateforme de livraison durable qui connecte commerçants, prestataires de services, livreurs et clients dans un écosystème respectueux de l'environnement. Cette solution logicielle modulaire, sécurisée et maintenable vise à promouvoir une logistique responsable, réduire l'impact environnemental et soutenir le commerce local.

### Objectifs Principaux

- **Écologie** : Optimisation des trajets de livraison pour réduire l'empreinte carbone
- **Commerce Local** : Soutien aux commerçants et prestataires de proximité
- **Innovation** : Plateforme technologique moderne et évolutive
- **Communauté** : Création d'un écosystème collaboratif

### Fonctionnalités Clés

- Gestion multi-acteurs (clients, commerçants, livreurs, prestataires, administrateurs)
- Système de commandes et livraisons optimisées
- Gestion des paiements intégrée (Stripe)
- Interface d'administration complète
- Notifications temps réel via WebSocket
- Analytics et reporting

---

## Architecture

### Architecture Générale

EcoDeli suit une architecture microservices avec séparation des responsabilités :

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Frontend      │    │   Analytics     │
│   Utilisateur   │    │   Admin         │    │   Module        │
│   (Vue.js)      │    │   (Vue.js)      │    │   (Spring)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │     Nginx       │
                    │  Reverse Proxy  │
                    └─────────────────┘
                                 │
                    ┌─────────────────┐
                    │    Backend      │
                    │  (Spring Boot)  │
                    └─────────────────┘
                                 │
                    ┌─────────────────┐
                    │   PostgreSQL    │
                    │   Database      │
                    └─────────────────┘
```

### Technologies Utilisées

#### Backend
- **Framework** : Spring Boot 3.2.3
- **Langage** : Java 21
- **Base de données** : PostgreSQL
- **Sécurité** : Spring Security + JWT
- **Documentation API** : SpringDoc OpenAPI (Swagger)
- **Paiements** : Stripe SDK
- **Géolocalisation** : Google Maps API
- **Communication temps réel** : WebSocket
- **Email** : Spring Mail

#### Frontend Utilisateur
- **Framework** : Vue.js 3.x
- **UI Framework** : PrimeVue 4.x
- **State Management** : Pinia
- **Routing** : Vue Router 4.x
- **HTTP Client** : Axios
- **Build Tool** : Vite
- **Linting** : ESLint

#### Frontend Admin
- **Framework** : Vue.js 3.x avec template Sakai
- **UI Framework** : PrimeVue 4.x
- **State Management** : Pinia
- **Routing** : Vue Router 4.x
- **HTTP Client** : Axios

#### Infrastructure
- **Conteneurisation** : Docker & Docker Compose
- **Reverse Proxy** : Nginx
- **Déploiement** : Railway
- **Monitoring** : Spring Actuator

---

## Installation et Configuration

### Prérequis Système

#### Développement Local
- **Java** : JDK 21 ou supérieur
- **Node.js** : Version 16.x ou supérieure
- **npm** : Version 8.x ou supérieure
- **PostgreSQL** : Version 12 ou supérieure
- **Git** : Pour le contrôle de version

#### Production
- **Docker** : Version 20.x ou supérieure
- **Docker Compose** : Version 2.x ou supérieure

### Installation Locale

#### 1. Clonage du Repository

```bash
git clone https://github.com/jade6taf/EcoDeli-2a5-Zazic-Lisika-Tafili.git
cd EcoDeli-2a5-Zazic-Lisika-Tafili
```

#### 2. Configuration de la Base de Données

```sql
-- Créer la base de données PostgreSQL
CREATE DATABASE ecodeli;
CREATE USER ecodeli_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE ecodeli TO ecodeli_user;
```

#### 3. Configuration du Backend

```bash
cd ecodeli-backend

# Copier le fichier de configuration
cp application.yml.example application.yml

# Éditer les paramètres de connexion
# - Base de données PostgreSQL
# - Clés API (Google Maps, Stripe)
# - Configuration email

# Installer les dépendances et compiler
./mvnw clean install

# Démarrer le serveur (port 8080)
./mvnw spring-boot:run
```

#### 4. Configuration du Frontend Utilisateur

```bash
cd ecodeli-frontend-user

# Installer les dépendances
npm install

# Démarrer en mode développement (port 5173)
npm run dev
```

#### 5. Configuration du Frontend Admin

```bash
cd ecodeli-frontend-admin

# Installer les dépendances
npm install

# Démarrer en mode développement (port 5174)
npm run dev
```

### Déploiement avec Docker

#### Développement

```bash
# Démarrer tous les services
docker-compose up -d

# Arrêter les services
docker-compose down
```

#### Production

```bash
# Démarrer en mode production
docker-compose -f docker-compose.production.yml up -d
```

### Variables d'Environnement

#### Backend (application.yml)

```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/ecodeli}
    username: ${DATABASE_USERNAME:ecodeli_user}
    password: ${DATABASE_PASSWORD:password}

jwt:
  secret: ${JWT_SECRET:your-secret-key}
  
google:
  maps:
    api-key: ${GOOGLE_MAPS_API_KEY:your-api-key}
    
stripe:
  public-key: ${STRIPE_PUBLISHABLE_KEY:pk_test_...}
  secret-key: ${STRIPE_SECRET_KEY:sk_test_...}
  
mail:
  username: ${MAIL_USERNAME:your-email@gmail.com}
  password: ${MAIL_PASSWORD:your-app-password}
```

#### Frontend (config/api.js)

```javascript
export const API_CONFIG = {
  BASE_URL: process.env.NODE_ENV === 'production' 
    ? 'https://your-backend.railway.app'
    : 'http://localhost:8080',
  TIMEOUT: 10000
}
```

---

## Documentation Technique

### Modèle de Données

#### Entités Principales

##### Utilisateur (Héritage)
```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utilisateur {
    private Integer idUtilisateur;
    private String nom;
    private String prenom;
    private Boolean genre;
    private LocalDate dateDeNaissance;
    private String email;
    private String telephone;
    private String adresse;
    // ...
}
```

**Spécialisations :**
- `Client` : Utilisateur final qui passe des commandes
- `Commercant` : Propriétaire d'un commerce avec SIRET
- `Livreur` : Effectue les livraisons avec véhicule et licence
- `Prestataire` : Fournit des services avec expertise
- `Admin` : Administrateur système avec privilèges étendus

##### Annonce
```java
@Entity
public class Annonce {
    private Integer idAnnonce;
    private String titre;
    private String description;
    private BigDecimal prix;
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;
    private String adresseDepart;
    private String adresseArrivee;
    private Boolean estActive;
    // Relations
    private Client client;
    private CategorieAnnonce categorie;
}
```

##### Livraison
```java
@Entity
public class Livraison {
    private Integer idLivraison;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String statut;
    private String commentaires;
    // Relations
    private Annonce annonce;
    private Livreur livreur;
    private List<Colis> colis;
}
```

### Architecture API REST

#### Endpoints Principaux

##### Authentification (`/api/auth`)
- `POST /login` : Connexion utilisateur
- `POST /register` : Inscription utilisateur
- `POST /refresh` : Renouvellement du token JWT
- `POST /logout` : Déconnexion

##### Utilisateurs (`/api/users`)
- `GET /profile` : Profil utilisateur connecté
- `PUT /profile` : Mise à jour du profil
- `GET /users/{id}` : Détails d'un utilisateur
- `DELETE /users/{id}` : Suppression d'un compte

##### Annonces (`/api/annonces`)
- `GET /` : Liste des annonces (avec pagination)
- `POST /` : Création d'une annonce
- `GET /{id}` : Détails d'une annonce
- `PUT /{id}` : Modification d'une annonce
- `DELETE /{id}` : Suppression d'une annonce

##### Livraisons (`/api/livraisons`)
- `GET /` : Liste des livraisons
- `POST /` : Création d'une livraison
- `PUT /{id}/statut` : Mise à jour du statut
- `GET /{id}/tracking` : Suivi en temps réel

##### Paiements (`/api/paiements`)
- `POST /create-payment-intent` : Intention de paiement Stripe
- `POST /confirm-payment` : Confirmation du paiement
- `GET /history` : Historique des paiements

#### Sécurité API

##### Authentification JWT
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
            .build();
    }
}
```

##### Autorisation par Rôles
- **CLIENT** : Gestion de ses annonces et commandes
- **COMMERCANT** : Gestion de son commerce et contrats
- **LIVREUR** : Accès aux livraisons qui lui sont assignées
- **PRESTATAIRE** : Gestion de ses services et candidatures
- **ADMIN** : Accès complet à toutes les fonctionnalités

### Architecture Frontend

#### Structure des Projets Vue.js

```
src/
├── components/         # Composants réutilisables
│   ├── common/        # Composants génériques
│   ├── forms/         # Composants de formulaires
│   └── layout/        # Composants de mise en page
├── views/             # Pages de l'application
│   ├── auth/         # Pages d'authentification
│   ├── client/       # Pages client
│   ├── commercant/   # Pages commerçant
│   └── admin/        # Pages administrateur
├── stores/           # Stores Pinia
├── router/           # Configuration Vue Router
├── config/           # Configuration (API, constantes)
├── assets/           # Ressources statiques
└── main.js          # Point d'entrée
```

#### Gestion d'État avec Pinia

```javascript
// stores/auth.js
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token'),
    isAuthenticated: false
  }),
  
  actions: {
    async login(credentials) {
      const response = await authService.login(credentials)
      this.token = response.token
      this.user = response.user
      this.isAuthenticated = true
      localStorage.setItem('token', this.token)
    },
    
    logout() {
      this.user = null
      this.token = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
    }
  }
})
```

#### Routing et Navigation

```javascript
// router/index.js
const routes = [
  {
    path: '/',
    component: HomeView,
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    component: DashboardView,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'ADMIN' }
  }
]

// Navigation guards
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.role && authStore.user?.role !== to.meta.role) {
    next('/unauthorized')
  } else {
    next()
  }
})
```

---

## Guide d'Utilisation

### Interface Client

#### Inscription et Connexion

1. **Inscription**
   - Accéder à la page d'inscription
   - Remplir le formulaire (nom, prénom, email, mot de passe)
   - Confirmer l'email envoyé
   - Connexion automatique

2. **Connexion**
   - Saisir email et mot de passe
   - Option "Se souvenir de moi"
   - Redirection vers le tableau de bord

#### Création d'Annonce

1. **Nouvelle Annonce**
   - Cliquer sur "Créer une annonce"
   - Remplir les informations :
     - Titre et description
     - Catégorie
     - Prix
     - Adresses de départ et d'arrivée
     - Date limite
   - Publier l'annonce

2. **Gestion des Annonces**
   - Voir toutes ses annonces
   - Modifier ou supprimer
   - Suivre les candidatures

#### Suivi des Livraisons

1. **Dashboard Livraisons**
   - Vue d'ensemble des livraisons en cours
   - Statuts temps réel
   - Chat avec le livreur

2. **Tracking GPS**
   - Position du livreur sur carte
   - Estimation du temps d'arrivée
   - Notifications push

### Interface Livreur

#### Candidature aux Livraisons

1. **Recherche d'Annonces**
   - Filtres par localisation
   - Tri par prix ou distance
   - Vue liste ou carte

2. **Candidature**
   - Sélectionner une annonce
   - Proposer un prix
   - Ajouter un message
   - Envoyer la candidature

#### Gestion des Livraisons

1. **Livraisons Acceptées**
   - Planning des livraisons
   - Détails des colis
   - Itinéraire optimisé

2. **Suivi en Temps Réel**
   - Mise à jour de position
   - Communication avec le client
   - Confirmation de livraison

### Interface Administrateur

#### Tableau de Bord

1. **Métriques Principales**
   - Nombre d'utilisateurs actifs
   - Livraisons en cours
   - Revenus du jour/mois
   - Taux de satisfaction

2. **Graphiques et Analytics**
   - Évolution des inscriptions
   - Performance des livreurs
   - Zones de livraison populaires

#### Gestion des Utilisateurs

1. **Liste des Utilisateurs**
   - Recherche et filtres
   - Export des données
   - Actions en masse

2. **Modération**
   - Validation des documents
   - Suspension de comptes
   - Gestion des signalements

#### Gestion des Contenus

1. **Validation des Annonces**
   - Modération des contenus
   - Approbation/rejet
   - Catégorisation

2. **Gestion des Tarifs**
   - Configuration des commissions
   - Tarifs de livraison
   - Promotions et codes promo

---

## Guide de Développement

### Standards de Code

#### Backend Java

##### Conventions de Nommage
```java
// Classes : PascalCase
public class UtilisateurService { }

// Méthodes et variables : camelCase
private String nomUtilisateur;
public void creerUtilisateur() { }

// Constantes : UPPER_SNAKE_CASE
public static final String STATUS_ACTIVE = "ACTIVE";

// Packages : kebab-case
package com.ecodeli.ecodeli_backend.services;
```

##### Structure des Services
```java
@Service
@Transactional
public class UtilisateurService {
    
    private final UtilisateurRepository utilisateurRepository;
    
    // Constructor injection
    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }
    
    public Utilisateur save(Utilisateur utilisateur) {
        // Validation métier
        validateUtilisateur(utilisateur);
        return utilisateurRepository.save(utilisateur);
    }
    
    private void validateUtilisateur(Utilisateur utilisateur) {
        // Logique de validation
    }
}
```

#### Frontend Vue.js

##### Conventions de Nommage
```javascript
// Composants : PascalCase
export default {
  name: 'UtilisateurForm'
}

// Props et variables : camelCase
props: {
  utilisateurId: Number,
  showModal: Boolean
}

// Events : kebab-case
this.$emit('user-created', userData)

// Fichiers : kebab-case
user-form.vue
user-service.js
```

##### Structure des Composants
```vue
<template>
  <div class="user-form">
    <!-- Template -->
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

export default {
  name: 'UserForm',
  
  props: {
    userId: {
      type: Number,
      default: null
    }
  },
  
  emits: ['user-saved', 'cancel'],
  
  setup(props, { emit }) {
    const userStore = useUserStore()
    
    // Reactive data
    const form = ref({
      nom: '',
      prenom: '',
      email: ''
    })
    
    // Computed
    const isEditing = computed(() => props.userId !== null)
    
    // Methods
    const saveUser = async () => {
      try {
        await userStore.saveUser(form.value)
        emit('user-saved')
      } catch (error) {
        console.error('Error saving user:', error)
      }
    }
    
    // Lifecycle
    onMounted(() => {
      if (props.userId) {
        loadUser()
      }
    })
    
    return {
      form,
      isEditing,
      saveUser
    }
  }
}
</script>

<style scoped>
.user-form {
  /* Styles */
}
</style>
```

### Tests

#### Tests Backend avec JUnit

```java
@SpringBootTest
@Transactional
class UtilisateurServiceTest {
    
    @Autowired
    private UtilisateurService utilisateurService;
    
    @Test
    void shouldCreateUtilisateur() {
        // Given
        Utilisateur utilisateur = new Client();
        utilisateur.setNom("Dupont");
        utilisateur.setPrenom("Jean");
        utilisateur.setEmail("jean.dupont@example.com");
        
        // When
        Utilisateur result = utilisateurService.save(utilisateur);
        
        // Then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getNom()).isEqualTo("Dupont");
    }
    
    @Test
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        String email = "existing@example.com";
        // Create user with this email first
        
        // When & Then
        assertThatThrownBy(() -> {
            Utilisateur newUser = new Client();
            newUser.setEmail(email);
            utilisateurService.save(newUser);
        }).isInstanceOf(EmailAlreadyExistsException.class);
    }
}
```

#### Tests Frontend avec Jest

```javascript
// UserForm.test.js
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import UserForm from '@/components/UserForm.vue'

describe('UserForm', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })
  
  it('should render form fields', () => {
    const wrapper = mount(UserForm)
    
    expect(wrapper.find('[data-test="nom-input"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="prenom-input"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="email-input"]').exists()).toBe(true)
  })
  
  it('should emit user-saved event on valid submit', async () => {
    const wrapper = mount(UserForm)
    
    await wrapper.find('[data-test="nom-input"]').setValue('Dupont')
    await wrapper.find('[data-test="prenom-input"]').setValue('Jean')
    await wrapper.find('[data-test="email-input"]').setValue('jean@example.com')
    
    await wrapper.find('form').trigger('submit')
    
    expect(wrapper.emitted('user-saved')).toBeTruthy()
  })
})
```

### Gestion des Erreurs

#### Backend
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UtilisateurNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUtilisateurNotFound(
            UtilisateurNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "USER_NOT_FOUND",
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex) {
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

#### Frontend
```javascript
// utils/errorHandler.js
export const handleApiError = (error) => {
  if (error.response) {
    const { status, data } = error.response
    
    switch (status) {
      case 401:
        // Rediriger vers login
        router.push('/login')
        break
      case 403:
        // Afficher erreur permission
        toast.error('Accès non autorisé')
        break
      case 404:
        // Afficher erreur ressource non trouvée
        toast.error('Ressource non trouvée')
        break
      default:
        // Erreur générique
        toast.error(data.message || 'Une erreur est survenue')
    }
  } else {
    // Erreur réseau
    toast.error('Erreur de connexion')
  }
}
```

### Processus de Contribution

#### Workflow Git

1. **Créer une branche feature**
```bash
git checkout -b feature/nouvelle-fonctionnalite
```

2. **Développer et tester**
```bash
# Backend
./mvnw test

# Frontend
npm test
npm run lint
```

3. **Commit avec message conventionnel**
```bash
git commit -m "feat: ajouter authentification OAuth"
git commit -m "fix: corriger validation email"
git commit -m "docs: mettre à jour guide installation"
```

4. **Push et Pull Request**
```bash
git push origin feature/nouvelle-fonctionnalite
# Créer une PR sur GitHub
```

#### Code Review

1. **Checklist Reviewer**
   - Code respecte les conventions
   - Tests unitaires présents
   - Documentation mise à jour
   - Pas de vulnérabilités de sécurité
   - Performance acceptable

2. **Checklist Auteur**
   - Code auto-reviewé
   - Tests passent
   - Documentation à jour
   - Branch à jour avec main

---

## Maintenance et Opérations

### Monitoring et Observabilité

#### Métriques Spring Actuator

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

**Endpoints disponibles :**
- `/actuator/health` : État de santé de l'application
- `/actuator/metrics` : Métriques système et applicatives
- `/actuator/info` : Informations sur l'application
- `/actuator/prometheus` : Métriques au format Prometheus

#### Logs Structurés

```java
@RestController
public class UtilisateurController {
    
    private static final Logger logger = LoggerFactory.getLogger(UtilisateurController.class);
    
    @PostMapping("/users")
    public ResponseEntity<Utilisateur> createUser(@RequestBody Utilisateur user) {
        logger.info("Creating user with email: {}", user.getEmail());
        
        try {
            Utilisateur created = userService.save(user);
            logger.info("User created successfully with ID: {}", created.getId());
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

### Sauvegarde et Récupération

#### Sauvegarde Base de Données

```bash
# Sauvegarde quotidienne
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/var/backups/ecodeli"
DB_NAME="ecodeli"

# Créer le répertoire si nécessaire
mkdir -p $BACKUP_DIR

# Sauvegarde
pg_dump -h localhost -U ecodeli_user $DB_NAME | gzip > $BACKUP_DIR/ecodeli_$DATE.sql.gz

# Nettoyer les sauvegardes anciennes (> 30 jours)
find $BACKUP_DIR -name "ecodeli_*.sql.gz" -mtime +30 -delete

echo "Backup completed: ecodeli_$DATE.sql.gz"
```

#### Restauration

```bash
# Restauration depuis sauvegarde
gunzip -c /var/backups/ecodeli/ecodeli_20241201_120000.sql.gz | psql -h localhost -U ecodeli_user ecodeli
```

### Déploiement

#### Pipeline CI/CD

```yaml
# .github/workflows/deploy.yml
name: Deploy to Railway

on:
  push:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          
      - name: Run backend tests
        run: |
          cd ecodeli-backend
          ./mvnw test
          
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          
      - name: Run frontend tests
        run: |
          cd ecodeli-frontend-user
          npm install
          npm test
          
  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Deploy to Railway
        uses: bervProject/railway-deploy@v1.2.0
        with:
          railway_token: ${{ secrets.RAILWAY_TOKEN }}
          service: 'ecodeli-backend'
```

#### Configuration Railway

```toml
# railway.toml
[build]
builder = "DOCKERFILE"
dockerfilePath = "Dockerfile"

[deploy]
healthcheckPath = "/actuator/health"
healthcheckTimeout = 300
restartPolicyType = "ON_FAILURE"
restartPolicyMaxRetries = 10

[[services]]
name = "ecodeli-backend"

[services.variables]
SPRING_PROFILES_ACTIVE = "production"
```

### Sécurité

#### Checklist Sécurité

1. **Authentification et Autorisation**
   - [ ] Mots de passe hashés avec BCrypt
   - [ ] Tokens JWT avec expiration
   - [ ] Validation des rôles sur chaque endpoint
   - [ ] Protection contre les attaques par force brute

2. **Protection des Données**
   - [ ] Chiffrement des données sensibles
   - [ ] Validation et sanitisation des entrées
   - [ ] Protection contre l'injection SQL
   - [ ] HTTPS obligatoire en production

3. **Configuration**
   - [ ] Variables d'environnement pour les secrets
   - [ ] CORS configuré correctement
   - [ ] Headers de sécurité (CSP, HSTS, etc.)
   - [ ] Logs de sécurité activés

#### Mise à Jour de Sécurité

```bash
# Vérifier les vulnérabilités
# Backend
./mvnw org.owasp:dependency-check-maven:check

# Frontend
npm audit
npm audit fix
```

### Performance

#### Optimisation Base de Données

```sql
-- Index sur les colonnes fréquemment utilisées
CREATE INDEX idx_utilisateur_email ON utilisateur(email);
CREATE INDEX idx_annonce_date_creation ON annonce(date_creation);
CREATE INDEX idx_livraison_statut ON livraison(statut);

-- Analyse des performances
EXPLAIN ANALYZE SELECT * FROM annonce WHERE date_creation > NOW() - INTERVAL '7 days';
```

#### Cache Redis (optionnel)

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}

@Service
public class UtilisateurService {
    
    @Cacheable(value = "users", key = "#id")
    public Utilisateur findById(Integer id) {
        return utilisateurRepository.findById(id).orElse(null);
    }
    
    @CacheEvict(value = "users", key = "#utilisateur.id")
    public Utilisateur save(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }
}
```

---

## Support et Contact

### Équipe de Développement

- **Zazic Ivan** - Développeur Full Stack
- **Lisika Clara** - Développeur Frontend
- **Tafili Jade** - Développeur Backend

### Ressources Utiles

- **Repository GitHub** : [EcoDeli-2a5-Zazic-Lisika-Tafili](https://github.com/jade6taf/EcoDeli-2a5-Zazic-Lisika-Tafili)
- **Documentation API** : `/swagger-ui.html` (en développement)
- **Guide de déploiement** : `RAILWAY-DEPLOYMENT.md`

### Signalement de Bugs

Pour signaler un bug ou proposer une amélioration :

1. Vérifier que le bug n'existe pas déjà dans les issues GitHub
2. Créer une nouvelle issue avec :
   - Description détaillée du problème
   - Étapes pour reproduire
   - Environnement (navigateur, OS)
   - Screenshots si pertinents

---

*Cette documentation est maintenue par l'équipe EcoDeli et mise à jour régulièrement. Dernière mise à jour : Décembre 2024*