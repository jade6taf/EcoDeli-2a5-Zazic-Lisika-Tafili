# Guide de Développement EcoDeli

Ce guide fournit toutes les informations nécessaires pour contribuer au développement de la plateforme EcoDeli.

## Table des Matières

1. [Configuration de l'Environnement](#configuration-de-lenvironnement)
2. [Standards de Code](#standards-de-code)
3. [Architecture du Code](#architecture-du-code)
4. [Workflow de Développement](#workflow-de-développement)
5. [Debugging et Diagnostics](#debugging-et-diagnostics)
6. [Performance et Optimisation](#performance-et-optimisation)
7. [Sécurité](#sécurité)
8. [Contribution](#contribution)

---

## Configuration de l'Environnement

### IDE Recommandés

#### Backend (Java/Spring Boot)
- **IntelliJ IDEA Ultimate** (recommandé)
- **Eclipse IDE** avec Spring Tools
- **Visual Studio Code** avec extensions Java

**Extensions IntelliJ recommandées :**
- Spring Boot
- Lombok
- JPA Buddy
- SonarLint
- GitToolBox

#### Frontend (Vue.js)
- **Visual Studio Code** (recommandé)
- **WebStorm**

**Extensions VS Code recommandées :**
```json
{
  "recommendations": [
    "Vue.volar",
    "Vue.vscode-typescript-vue-plugin",
    "bradlc.vscode-tailwindcss",
    "esbenp.prettier-vscode",
    "dbaeumer.vscode-eslint",
    "ms-vscode.vscode-json",
    "humao.rest-client"
  ]
}
```

### Configuration Git

```bash
# Configuration globale
git config --global user.name "Votre Nom"
git config --global user.email "votre.email@example.com"
git config --global init.defaultBranch main

# Configuration des hooks
cp scripts/git-hooks/* .git/hooks/
chmod +x .git/hooks/*

# Alias utiles
git config --global alias.st status
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.cm commit
git config --global alias.lg "log --oneline --graph --all"
```

### Variables d'Environnement de Développement

Créer un fichier `.env.development` :

```env
# Base de données locale
DATABASE_URL=jdbc:postgresql://localhost:5432/ecodeli_dev
DATABASE_USERNAME=ecodeli_dev
DATABASE_PASSWORD=dev_password

# JWT avec clé de développement
JWT_SECRET=development_secret_key_64_characters_minimum_for_security_2024

# API Keys de développement
GOOGLE_MAPS_API_KEY=your_development_google_maps_key
STRIPE_PUBLISHABLE_KEY=pk_test_your_development_stripe_key
STRIPE_SECRET_KEY=sk_test_your_development_stripe_secret

# Email de test
MAIL_HOST=smtp.mailtrap.io
MAIL_PORT=587
MAIL_USERNAME=your_mailtrap_username
MAIL_PASSWORD=your_mailtrap_password

# Logs en mode debug
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_ECODELI=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG

# Profil Spring actif
SPRING_PROFILES_ACTIVE=development
```

---

## Standards de Code

### Conventions de Nommage

#### Backend Java

```java
// Classes : PascalCase
public class UtilisateurService { }
public class AnnonceController { }

// Méthodes et variables : camelCase
private String nomUtilisateur;
public void creerNouvelleAnnonce() { }

// Constantes : UPPER_SNAKE_CASE
public static final String STATUS_ACTIVE = "ACTIVE";
public static final int MAX_RETRY_ATTEMPTS = 3;

// Packages : lowercase avec underscores
package com.ecodeli.ecodeli_backend.services;

// Enums : PascalCase avec valeurs UPPER_SNAKE_CASE
public enum StatutLivraison {
    EN_ATTENTE,
    EN_COURS,
    LIVREE,
    ANNULEE
}
```

#### Frontend Vue.js

```javascript
// Composants : PascalCase
export default {
  name: 'UtilisateurProfile'
}

// Props et données : camelCase
props: {
  utilisateurId: Number,
  showDetails: Boolean
}

// Events : kebab-case
this.$emit('user-updated', userData)
this.$emit('form-submitted')

// Fichiers : kebab-case
user-profile.vue
user-service.js
api-client.js

// Constantes : UPPER_SNAKE_CASE
const API_BASE_URL = 'http://localhost:8080'
const MAX_FILE_SIZE = 5242880 // 5MB
```

### Structure des Fichiers

#### Backend Service

```java
@Service
@Transactional
@Slf4j
public class UtilisateurService {
    
    // 1. Constantes
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    
    // 2. Dépendances injectées (final)
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    // 3. Constructeur
    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                             PasswordEncoder passwordEncoder,
                             EmailService emailService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    
    // 4. Méthodes publiques (interface du service)
    @Transactional(readOnly = true)
    public Optional<Utilisateur> findById(Integer id) {
        log.debug("Recherche utilisateur avec ID: {}", id);
        return utilisateurRepository.findById(id);
    }
    
    public Utilisateur createUtilisateur(CreateUtilisateurRequest request) {
        log.info("Création nouvel utilisateur: {}", request.getEmail());
        
        // Validation
        validateCreateRequest(request);
        
        // Logique métier
        Utilisateur utilisateur = buildUtilisateurFromRequest(request);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        
        // Actions post-création
        emailService.sendWelcomeEmail(saved);
        
        log.info("Utilisateur créé avec succès: ID {}", saved.getIdUtilisateur());
        return saved;
    }
    
    // 5. Méthodes privées (utilitaires)
    private void validateCreateRequest(CreateUtilisateurRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email déjà utilisé: " + request.getEmail());
        }
        // Autres validations...
    }
    
    private Utilisateur buildUtilisateurFromRequest(CreateUtilisateurRequest request) {
        // Logique de construction
        return new Client(); // Exemple
    }
}
```

#### Frontend Composant Vue

```vue
<template>
  <div class="user-form">
    <!-- 1. Structure principale -->
    <div class="form-header">
      <h2>{{ isEditing ? 'Modifier' : 'Créer' }} un utilisateur</h2>
    </div>
    
    <!-- 2. Formulaire -->
    <form @submit.prevent="handleSubmit" class="form-content">
      <!-- Champs du formulaire -->
      <div class="field-group">
        <label for="nom">Nom *</label>
        <input
          id="nom"
          v-model="form.nom"
          type="text"
          required
          :class="{ 'error': errors.nom }"
          data-test="nom-input"
        />
        <span v-if="errors.nom" class="error-message">{{ errors.nom }}</span>
      </div>
      
      <!-- Autres champs... -->
      
      <!-- Actions -->
      <div class="form-actions">
        <button type="button" @click="$emit('cancel')" class="btn-secondary">
          Annuler
        </button>
        <button type="submit" :disabled="isSubmitting" class="btn-primary">
          {{ isSubmitting ? 'Enregistrement...' : 'Enregistrer' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { validateForm } from '@/utils/validation'

export default {
  name: 'UserForm',
  
  // 1. Props
  props: {
    userId: {
      type: Number,
      default: null
    },
    initialData: {
      type: Object,
      default: () => ({})
    }
  },
  
  // 2. Events
  emits: ['user-saved', 'cancel'],
  
  // 3. Setup
  setup(props, { emit }) {
    // 3.1 Stores
    const userStore = useUserStore()
    
    // 3.2 Reactive data
    const form = ref({
      nom: '',
      prenom: '',
      email: '',
      telephone: ''
    })
    const errors = ref({})
    const isSubmitting = ref(false)
    
    // 3.3 Computed
    const isEditing = computed(() => props.userId !== null)
    const formIsValid = computed(() => Object.keys(errors.value).length === 0)
    
    // 3.4 Methods
    const validateField = (field, value) => {
      const fieldErrors = validateForm.validateField(field, value)
      if (fieldErrors.length > 0) {
        errors.value[field] = fieldErrors[0]
      } else {
        delete errors.value[field]
      }
    }
    
    const handleSubmit = async () => {
      if (!formIsValid.value) return
      
      isSubmitting.value = true
      try {
        const result = isEditing.value
          ? await userStore.updateUser(props.userId, form.value)
          : await userStore.createUser(form.value)
        
        emit('user-saved', result)
      } catch (error) {
        console.error('Erreur sauvegarde utilisateur:', error)
        // Gestion d'erreur...
      } finally {
        isSubmitting.value = false
      }
    }
    
    const loadUserData = async () => {
      if (props.userId) {
        try {
          const user = await userStore.fetchUser(props.userId)
          Object.assign(form.value, user)
        } catch (error) {
          console.error('Erreur chargement utilisateur:', error)
        }
      }
    }
    
    // 3.5 Watchers
    watch(() => props.userId, loadUserData, { immediate: true })
    
    watch(() => props.initialData, (newData) => {
      Object.assign(form.value, newData)
    }, { immediate: true, deep: true })
    
    // 3.6 Lifecycle
    onMounted(() => {
      // Initialisation si nécessaire
    })
    
    // 3.7 Return
    return {
      form,
      errors,
      isSubmitting,
      isEditing,
      formIsValid,
      handleSubmit,
      validateField
    }
  }
}
</script>

<style scoped>
/* 1. Layout */
.user-form {
  max-width: 600px;
  margin: 0 auto;
  padding: 2rem;
}

/* 2. Form elements */
.field-group {
  margin-bottom: 1.5rem;
}

.field-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
}

/* 3. Error states */
.error {
  border-color: var(--color-error);
}

.error-message {
  color: var(--color-error);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

/* 4. Actions */
.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}
</style>
```

### Documentation du Code

#### Commentaires Java

```java
/**
 * Service de gestion des utilisateurs.
 * 
 * Ce service centralise toute la logique métier liée aux utilisateurs,
 * incluant la création, modification, authentification et gestion des profils.
 * 
 * @author Équipe EcoDeli
 * @since 1.0.0
 */
@Service
public class UtilisateurService {
    
    /**
     * Crée un nouvel utilisateur dans le système.
     * 
     * Cette méthode valide les données, vérifie l'unicité de l'email,
     * encode le mot de passe et envoie un email de bienvenue.
     * 
     * @param request Les données du nouvel utilisateur
     * @return L'utilisateur créé avec son ID généré
     * @throws EmailAlreadyExistsException Si l'email est déjà utilisé
     * @throws ValidationException Si les données sont invalides
     */
    @Transactional
    public Utilisateur createUtilisateur(CreateUtilisateurRequest request) {
        // Validation préalable
        validateCreateRequest(request);
        
        // Construction de l'entité
        Utilisateur utilisateur = switch (request.getType()) {
            case CLIENT -> new Client();
            case LIVREUR -> new Livreur();
            case COMMERCANT -> new Commercant();
            case PRESTATAIRE -> new Prestataire();
            default -> throw new IllegalArgumentException("Type utilisateur invalide");
        };
        
        // Configuration des propriétés communes
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        
        // Sauvegarde
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        
        // Actions post-création
        emailService.sendWelcomeEmail(saved);
        auditService.logUserCreation(saved);
        
        return saved;
    }
}
```

#### Commentaires JavaScript/Vue

```javascript
/**
 * Store Pinia pour la gestion des utilisateurs
 * 
 * Centralise l'état et les actions liées aux utilisateurs,
 * incluant l'authentification, les profils et les permissions.
 * 
 * @example
 * const userStore = useUserStore()
 * await userStore.login({ email, password })
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    /** @type {User|null} Utilisateur connecté */
    currentUser: null,
    
    /** @type {string|null} Token JWT d'authentification */
    authToken: null,
    
    /** @type {boolean} Indicateur de chargement */
    isLoading: false,
    
    /** @type {string|null} Dernier message d'erreur */
    lastError: null
  }),
  
  getters: {
    /**
     * Vérifie si l'utilisateur est authentifié
     * @returns {boolean} True si connecté
     */
    isAuthenticated: (state) => !!state.authToken && !!state.currentUser,
    
    /**
     * Retourne le rôle de l'utilisateur connecté
     * @returns {string|null} Le rôle ou null
     */
    userRole: (state) => state.currentUser?.type || null
  },
  
  actions: {
    /**
     * Authentifie un utilisateur
     * 
     * @param {Object} credentials - Identifiants de connexion
     * @param {string} credentials.email - Email utilisateur
     * @param {string} credentials.password - Mot de passe
     * @returns {Promise<User>} L'utilisateur authentifié
     * @throws {Error} Si l'authentification échoue
     */
    async login(credentials) {
      this.isLoading = true
      this.lastError = null
      
      try {
        const response = await authService.login(credentials)
        
        // Mise à jour de l'état
        this.authToken = response.token
        this.currentUser = response.user
        
        // Persistance locale
        localStorage.setItem('authToken', this.authToken)
        
        return this.currentUser
      } catch (error) {
        this.lastError = error.message
        throw error
      } finally {
        this.isLoading = false
      }
    }
  }
})
```

---

## Architecture du Code

### Structure des Packages Backend

```
com.ecodeli.ecodeli_backend/
├── config/                    # Configuration Spring
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   ├── DatabaseConfig.java
│   └── SwaggerConfig.java
├── controllers/               # Couche présentation
│   ├── auth/
│   │   └── AuthController.java
│   ├── user/
│   │   ├── AnnonceController.java
│   │   ├── LivraisonController.java
│   │   └── UserController.java
│   └── admin/
│       ├── AdminUserController.java
│       └── AdminStatsController.java
├── services/                  # Logique métier
│   ├── interfaces/           # Interfaces de services
│   │   ├── UserService.java
│   │   └── AnnonceService.java
│   └── impl/                 # Implémentations
│       ├── UserServiceImpl.java
│       └── AnnonceServiceImpl.java
├── repositories/              # Accès aux données
│   ├── UtilisateurRepository.java
│   ├── AnnonceRepository.java
│   └── specifications/       # JPA Specifications
│       └── AnnonceSpecs.java
├── models/                    # Entités JPA
│   ├── base/                 # Classes abstraites
│   │   └── BaseEntity.java
│   ├── Utilisateur.java
│   ├── Client.java
│   ├── Livreur.java
│   └── Annonce.java
├── dto/                       # Data Transfer Objects
│   ├── requests/             # DTOs de requête
│   │   ├── auth/
│   │   ├── user/
│   │   └── admin/
│   └── responses/            # DTOs de réponse
│       ├── auth/
│       ├── user/
│       └── admin/
├── security/                  # Sécurité
│   ├── JwtTokenUtil.java
│   ├── UserDetailsImpl.java
│   └── filters/
│       └── JwtRequestFilter.java
├── exceptions/                # Gestion des erreurs
│   ├── GlobalExceptionHandler.java
│   ├── custom/               # Exceptions métier
│   │   ├── UserNotFoundException.java
│   │   └── EmailAlreadyExistsException.java
│   └── ErrorResponse.java
├── validation/                # Validation métier
│   ├── validators/           # Validators personnalisés
│   │   ├── EmailValidator.java
│   │   └── PhoneValidator.java
│   └── constraints/          # Annotations de validation
│       ├── ValidEmail.java
│       └── ValidPhone.java
└── utils/                     # Utilitaires
    ├── DateUtils.java
    ├── StringUtils.java
    └── Constants.java
```

### Pattern Repository avec Specifications

```java
// Interface de base
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.createdAt >= :date")
    long countCreatedSince(@Param("date") LocalDateTime date);
}

// Repository spécialisé
@Repository
public interface AnnonceRepository extends BaseRepository<Annonce, Integer> {
    
    @Query("SELECT a FROM Annonce a WHERE a.client.id = :clientId AND a.estActive = true")
    List<Annonce> findActiveAnnoncesByClient(@Param("clientId") Integer clientId);
    
    @Modifying
    @Query("UPDATE Annonce a SET a.estActive = false WHERE a.dateExpiration < CURRENT_TIMESTAMP")
    int deactivateExpiredAnnonces();
}

// Specifications pour requêtes dynamiques
public class AnnonceSpecifications {
    
    public static Specification<Annonce> hasClient(Integer clientId) {
        return (root, query, criteriaBuilder) -> 
            clientId == null ? null : criteriaBuilder.equal(root.get("client").get("id"), clientId);
    }
    
    public static Specification<Annonce> isActive() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.isTrue(root.get("estActive"));
    }
    
    public static Specification<Annonce> hasVilleInAddress(String ville) {
        return (root, query, criteriaBuilder) -> {
            if (ville == null || ville.trim().isEmpty()) {
                return null;
            }
            
            String likePattern = "%" + ville.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("adresseDepart")), 
                    likePattern
                ),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("adresseArrivee")), 
                    likePattern
                )
            );
        };
    }
}

// Utilisation dans le service
@Service
public class AnnonceService {
    
    public Page<Annonce> searchAnnonces(String ville, Integer clientId, boolean activeOnly, Pageable pageable) {
        Specification<Annonce> spec = Specification.where(null);
        
        if (activeOnly) {
            spec = spec.and(AnnonceSpecifications.isActive());
        }
        
        if (clientId != null) {
            spec = spec.and(AnnonceSpecifications.hasClient(clientId));
        }
        
        if (ville != null) {
            spec = spec.and(AnnonceSpecifications.hasVilleInAddress(ville));
        }
        
        return annonceRepository.findAll(spec, pageable);
    }
}
```

### Architecture des Stores Frontend

```javascript
// store/base.js - Store de base
export function createBaseStore(name, initialState = {}) {
  return defineStore(name, {
    state: () => ({
      items: [],
      currentItem: null,
      isLoading: false,
      error: null,
      filters: {},
      pagination: {
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0
      },
      ...initialState
    }),
    
    getters: {
      hasItems: (state) => state.items.length > 0,
      hasError: (state) => !!state.error,
      hasNextPage: (state) => state.pagination.page < state.pagination.totalPages - 1,
      hasPreviousPage: (state) => state.pagination.page > 0
    },
    
    actions: {
      setLoading(loading) {
        this.isLoading = loading
      },
      
      setError(error) {
        this.error = error
      },
      
      clearError() {
        this.error = null
      },
      
      updatePagination(paginationData) {
        this.pagination = { ...this.pagination, ...paginationData }
      }
    }
  })
}

// store/annonces.js - Store spécialisé
export const useAnnoncesStore = defineStore('annonces', {
  ...createBaseStore('annonces'),
  
  state: () => ({
    ...createBaseStore('annonces').state(),
    categories: [],
    selectedCategorie: null,
    searchQuery: ''
  }),
  
  getters: {
    ...createBaseStore('annonces').getters,
    
    filteredAnnonces: (state) => {
      let filtered = [...state.items]
      
      if (state.searchQuery) {
        const query = state.searchQuery.toLowerCase()
        filtered = filtered.filter(annonce => 
          annonce.titre.toLowerCase().includes(query) ||
          annonce.description.toLowerCase().includes(query)
        )
      }
      
      if (state.selectedCategorie) {
        filtered = filtered.filter(annonce => 
          annonce.categorie.id === state.selectedCategorie
        )
      }
      
      return filtered
    }
  },
  
  actions: {
    async fetchAnnonces(params = {}) {
      this.setLoading(true)
      this.clearError()
      
      try {
        const response = await annonceService.getAnnonces({
          page: this.pagination.page,
          size: this.pagination.size,
          ...this.filters,
          ...params
        })
        
        this.items = response.content
        this.updatePagination({
          page: response.number,
          size: response.size,
          totalElements: response.totalElements,
          totalPages: response.totalPages
        })
      } catch (error) {
        this.setError(error.message)
        throw error
      } finally {
        this.setLoading(false)
      }
    },
    
    async createAnnonce(annonceData) {
      this.setLoading(true)
      try {
        const newAnnonce = await annonceService.createAnnonce(annonceData)
        this.items.unshift(newAnnonce)
        return newAnnonce
      } catch (error) {
        this.setError(error.message)
        throw error
      } finally {
        this.setLoading(false)
      }
    },
    
    setSearchQuery(query) {
      this.searchQuery = query
    },
    
    setSelectedCategorie(categorieId) {
      this.selectedCategorie = categorieId
    }
  }
})
```

---

## Workflow de Développement

### Git Flow

#### Branches Principales

```bash
main                 # Code en production
develop             # Code de développement intégré
feature/*           # Nouvelles fonctionnalités
bugfix/*           # Corrections de bugs
hotfix/*           # Corrections urgentes en production
release/*          # Préparation des releases
```

#### Workflow Complet

```bash
# 1. Créer une branche feature
git checkout develop
git pull origin develop
git checkout -b feature/nouvelle-fonctionnalite

# 2. Développer et commiter
git add .
git commit -m "feat: ajouter nouvelle fonctionnalité"

# 3. Rebaser sur develop (si nécessaire)
git fetch origin
git rebase origin/develop

# 4. Pousser la branche
git push origin feature/nouvelle-fonctionnalite

# 5. Créer une Pull Request
# Via l'interface GitHub

# 6. Après review et merge
git checkout develop
git pull origin develop
git branch -d feature/nouvelle-fonctionnalite
```

#### Messages de Commit Conventionnels

```bash
# Format : type(scope): description

# Types principaux
feat: nouvelle fonctionnalité
fix: correction de bug
docs: documentation
style: formatage, indentation
refactor: refactoring sans changement fonctionnel
test: ajout ou modification de tests
chore: tâches de maintenance

# Exemples
feat(auth): ajouter authentification OAuth
fix(api): corriger validation email
docs(readme): mettre à jour guide installation
style(frontend): formatter code selon ESLint
refactor(service): extraire logique validation
test(user): ajouter tests unitaires UserService
chore(deps): mettre à jour dépendances Spring
```

### Code Review

#### Checklist Reviewer

**Fonctionnel :**
- [ ] La fonctionnalité répond au besoin
- [ ] Les cas d'erreur sont gérés
- [ ] L'UX/UI est cohérente
- [ ] Les performances sont acceptables

**Technique :**
- [ ] Code respecte les conventions
- [ ] Architecture cohérente
- [ ] Pas de code dupliqué
- [ ] Gestion d'erreurs appropriée
- [ ] Tests unitaires présents
- [ ] Documentation à jour

**Sécurité :**
- [ ] Validation des entrées
- [ ] Pas de failles de sécurité
- [ ] Permissions vérifiées
- [ ] Données sensibles protégées

#### Template Pull Request

```markdown
## Description
Brève description des changements apportés.

## Type de changement
- [ ] Bug fix
- [ ] Nouvelle fonctionnalité
- [ ] Breaking change
- [ ] Documentation

## Tests
- [ ] Tests unitaires ajoutés/mis à jour
- [ ] Tests d'intégration passent
- [ ] Tests manuels effectués

## Checklist
- [ ] Code suit les conventions du projet
- [ ] Auto-review effectuée
- [ ] Documentation mise à jour
- [ ] Pas de console.log oubliés
- [ ] Variables d'environnement documentées

## Screenshots
Si applicable, ajouter des captures d'écran.

## Remarques
Informations supplémentaires pour les reviewers.
```

---

## Debugging et Diagnostics

### Backend Debugging

#### Configuration Logging

```yaml
# application-development.yml
logging:
  level:
    root: INFO
    com.ecodeli: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/ecodeli-backend.log
```

#### Debugging IntelliJ

```java
// Breakpoints conditionnels
@PostMapping("/annonces")
public ResponseEntity<AnnonceResponse> createAnnonce(@RequestBody CreateAnnonceRequest request) {
    // Breakpoint conditionnel : request.getPrix().compareTo(BigDecimal.valueOf(100)) > 0
    log.debug("Création annonce: {}", request);
    
    // Remote debugging sur port 5005
    // VM options: -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
    
    AnnonceResponse response = annonceService.createAnnonce(request);
    return ResponseEntity.ok(response);
}
```

#### Outils de Debug

```java
// Service de debug pour développement
@Service
@Profile("development")
public class DebugService {
    
    public void logRequestDetails(HttpServletRequest request) {
        log.debug("=== REQUEST DEBUG ===");
        log.debug("Method: {}", request.getMethod());
        log.debug("URL: {}", request.getRequestURL());
        log.debug("Headers: {}", getHeaders(request));
        log.debug("Parameters: {}", request.getParameterMap());
    }
    
    public void logDatabaseQueries() {
        // Utiliser p6spy pour logger toutes les requêtes SQL
    }
    
    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator()
            .forEachRemaining(name -> headers.put(name, request.getHeader(name)));
        return headers;
    }
}
```

### Frontend Debugging

#### Vue DevTools

```javascript
// main.js - Configuration pour développement
if (process.env.NODE_ENV === 'development') {
  // Activer Vue DevTools
  app.config.devtools = true
  
  // Logger global pour debug
  app.config.globalProperties.$log = console.log
  
  // Intercepteur Axios pour debug
  axios.interceptors.request.use(request => {
    console.log('🚀 Request:', request)
    return request
  })
  
  axios.interceptors.response.use(
    response => {
      console.log('✅ Response:', response)
      return response
    },
    error => {
      console.error('❌ Error:', error)
      return Promise.reject(error)
    }
  )
}
```

#### Debugging Console

```javascript
// Composable pour debugging
export function useDebug() {
  const debug = (label, data) => {
    if (process.env.NODE_ENV === 'development') {
      console.group(`🔍 DEBUG: ${label}`)
      console.log('Data:', data)
      console.log('Type:', typeof data)
      console.log('Timestamp:', new Date().toISOString())
      console.groupEnd()
    }
  }
  
  const debugStore = (storeName) => {
    if (process.env.NODE_ENV === 'development') {
      const store = useStore(storeName)
      console.log(`📦 Store ${storeName}:`, store.$state)
    }
  }
  
  const debugRoute = () => {
    if (process.env.NODE_ENV === 'development') {
      const route = useRoute()
      console.log('🛣️ Route:', {
        path: route.path,
        params: route.params,
        query: route.query,
        meta: route.meta
      })
    }
  }
  
  return {
    debug,
    debugStore,
    debugRoute
  }
}

// Utilisation dans un composant
export default {
  setup() {
    const { debug, debugStore } = useDebug()
    
    const handleSubmit = (formData) => {
      debug('Form Submit', formData)
      debugStore('user')
      
      // Logique de soumission...
    }
    
    return { handleSubmit }
  }
}
```

### Monitoring en Développement

#### Health Checks

```java
// Custom Health Indicator
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("validationQuery", "SELECT 1")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
        
        return Health.down().build();
    }
}

// Endpoints de monitoring
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {
    
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", Instant.now());
        status.put("application", "ecodeli-backend");
        status.put("version", getClass().getPackage().getImplementationVersion());
        status.put("environment", environment.getActiveProfiles());
        return status;
    }
    
    @GetMapping("/metrics/custom")
    public Map<String, Object> getCustomMetrics() {
        // Métriques personnalisées pour le développement
        return Map.of(
            "activeUsers", userService.countActiveUsers(),
            "todayAnnonces", annonceService.countTodayAnnonces(),
            "pendingLivraisons", livraisonService.countPendingLivraisons()
        );
    }
}
```

---

*Guide de Développement EcoDeli - Version 1.0 - Décembre 2024*

Ce guide sera complété avec les sections Performance, Sécurité et Contribution dans la suite...