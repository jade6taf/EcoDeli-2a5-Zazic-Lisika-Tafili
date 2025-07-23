# Architecture et Conception EcoDeli

## Vue d'ensemble de l'Architecture

EcoDeli implémente une architecture moderne basée sur les principes de séparation des responsabilités, de modularité et de scalabilité. Cette documentation détaille les choix architecturaux, les patterns utilisés et les justifications techniques.

## Table des Matières

1. [Architecture Générale](#architecture-générale)
2. [Architecture Backend](#architecture-backend)
3. [Architecture Frontend](#architecture-frontend)
4. [Base de Données](#base-de-données)
5. [Sécurité](#sécurité)
6. [Performance et Scalabilité](#performance-et-scalabilité)
7. [Monitoring et Observabilité](#monitoring-et-observabilité)

---

## Architecture Générale

### Diagramme d'Architecture Système

```
┌─────────────────────────────────────────────────────────────────┐
│                        COUCHE PRÉSENTATION                     │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Frontend      │  │   Frontend      │  │   Mobile App    │ │
│  │   Utilisateur   │  │   Admin         │  │   (Future)      │ │
│  │   (Vue.js)      │  │   (Vue.js)      │  │   (React N.)    │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                 │
                    ┌─────────────────┐
                    │     Nginx       │
                    │  Load Balancer  │
                    │  Reverse Proxy  │
                    │   SSL Termination│
                    └─────────────────┘
                                 │
┌─────────────────────────────────────────────────────────────────┐
│                        COUCHE API                               │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │    Backend      │  │   Analytics     │  │   Notification  │ │
│  │  (Spring Boot)  │  │   Service       │  │   Service       │ │
│  │                 │  │                 │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                 │
┌─────────────────────────────────────────────────────────────────┐
│                      COUCHE SERVICES                            │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   PostgreSQL    │  │     Redis       │  │    File Storage │ │
│  │   Database      │  │    Cache        │  │    (S3/Local)   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                 │
┌─────────────────────────────────────────────────────────────────┐
│                    SERVICES EXTERNES                            │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Google Maps   │  │     Stripe      │  │    SendGrid     │ │
│  │      API        │  │    Payment      │  │    Email        │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Principes Architecturaux

#### 1. Séparation des Responsabilités (SoC)
- **Frontend** : Interface utilisateur et expérience utilisateur
- **Backend** : Logique métier et gestion des données
- **Base de données** : Persistance des données
- **Services externes** : Fonctionnalités spécialisées

#### 2. Architecture en Couches
```
┌─────────────────┐
│  Présentation   │ ← Controllers, Views
├─────────────────┤
│    Service      │ ← Business Logic
├─────────────────┤
│   Repository    │ ← Data Access
├─────────────────┤
│    Database     │ ← Data Storage
└─────────────────┘
```

#### 3. Patterns de Conception Utilisés
- **MVC** (Model-View-Controller)
- **Repository Pattern**
- **Dependency Injection**
- **Observer Pattern** (WebSocket)
- **Strategy Pattern** (Paiements)
- **Factory Pattern** (Utilisateurs)

---

## Architecture Backend

### Structure du Projet Spring Boot

```
ecodeli-backend/
├── src/main/java/com/ecodeli/ecodeli_backend/
│   ├── config/                     # Configuration
│   │   ├── SecurityConfig.java     # Sécurité Spring Security
│   │   ├── WebConfig.java          # Configuration Web
│   │   ├── DatabaseConfig.java     # Configuration JPA
│   │   └── WebSocketConfig.java    # Configuration WebSocket
│   ├── controllers/                # Couche Présentation
│   │   ├── auth/                   # Authentification
│   │   ├── user/                   # Endpoints utilisateurs
│   │   └── admin/                  # Endpoints administrateur
│   ├── services/                   # Couche Business Logic
│   │   ├── UtilisateurService.java
│   │   ├── AnnonceService.java
│   │   ├── LivraisonService.java
│   │   └── PaiementService.java
│   ├── repositories/               # Couche Data Access
│   │   ├── UtilisateurRepository.java
│   │   ├── AnnonceRepository.java
│   │   └── LivraisonRepository.java
│   ├── models/                     # Entités JPA
│   │   ├── Utilisateur.java
│   │   ├── Client.java
│   │   ├── Livreur.java
│   │   └── Annonce.java
│   ├── dto/                        # Data Transfer Objects
│   │   ├── requests/
│   │   └── responses/
│   ├── security/                   # Sécurité
│   │   ├── JwtUtils.java
│   │   ├── UserDetailsImpl.java
│   │   └── AuthTokenFilter.java
│   ├── exceptions/                 # Gestion des erreurs
│   │   ├── GlobalExceptionHandler.java
│   │   └── CustomExceptions.java
│   └── validation/                 # Validation métier
│       ├── EmailValidator.java
│       └── PhoneValidator.java
└── src/main/resources/
    ├── application.yml             # Configuration Spring
    ├── data.sql                    # Données initiales
    └── static/                     # Ressources statiques
```

### Couche Controllers

#### Structure des Controllers

```java
@RestController
@RequestMapping("/api/annonces")
@Validated
@SecurityRequirement(name = "bearer-jwt")
public class AnnonceController {
    
    private final AnnonceService annonceService;
    
    // Constructor injection
    public AnnonceController(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }
    
    @GetMapping
    @Operation(summary = "Récupérer la liste des annonces")
    public ResponseEntity<Page<AnnonceResponse>> getAnnonces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) Integer categorieId) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AnnonceResponse> annonces = annonceService.getAnnonces(
            pageable, ville, categorieId);
        
        return ResponseEntity.ok(annonces);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Créer une nouvelle annonce")
    public ResponseEntity<AnnonceResponse> createAnnonce(
            @Valid @RequestBody CreateAnnonceRequest request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        AnnonceResponse annonce = annonceService.createAnnonce(request, userEmail);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(annonce);
    }
}
```

### Couche Services

#### Pattern de Service avec Transaction

```java
@Service
@Transactional
public class AnnonceService {
    
    private final AnnonceRepository annonceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final GoogleMapsService googleMapsService;
    private final NotificationService notificationService;
    
    public AnnonceService(AnnonceRepository annonceRepository,
                         UtilisateurRepository utilisateurRepository,
                         GoogleMapsService googleMapsService,
                         NotificationService notificationService) {
        this.annonceRepository = annonceRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.googleMapsService = googleMapsService;
        this.notificationService = notificationService;
    }
    
    @Transactional(readOnly = true)
    public Page<AnnonceResponse> getAnnonces(Pageable pageable, 
                                           String ville, 
                                           Integer categorieId) {
        Specification<Annonce> spec = buildSpecification(ville, categorieId);
        Page<Annonce> annonces = annonceRepository.findAll(spec, pageable);
        return annonces.map(this::toResponse);
    }
    
    public AnnonceResponse createAnnonce(CreateAnnonceRequest request, 
                                       String userEmail) {
        // 1. Validation métier
        validateAnnonceRequest(request);
        
        // 2. Récupération de l'utilisateur
        Client client = (Client) utilisateurRepository
            .findByEmail(userEmail)
            .orElseThrow(() -> new UserNotFoundException("Client non trouvé"));
        
        // 3. Géocodage des adresses
        GeocodingResult departCoords = googleMapsService
            .geocode(request.getAdresseDepart());
        GeocodingResult arriveeCoords = googleMapsService
            .geocode(request.getAdresseArrivee());
        
        // 4. Création de l'annonce
        Annonce annonce = buildAnnonceFromRequest(request, client, 
                                                 departCoords, arriveeCoords);
        
        // 5. Sauvegarde
        Annonce savedAnnonce = annonceRepository.save(annonce);
        
        // 6. Notification asynchrone aux livreurs
        notificationService.notifyLivreursNouvelleAnnonce(savedAnnonce);
        
        return toResponse(savedAnnonce);
    }
    
    private void validateAnnonceRequest(CreateAnnonceRequest request) {
        if (request.getDateExpiration().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("La date d'expiration doit être au moins 1h dans le futur");
        }
        // Autres validations métier...
    }
}
```

### Couche Repository

#### Repository avec Specifications

```java
@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Integer>, 
                                         JpaSpecificationExecutor<Annonce> {
    
    @Query("SELECT a FROM Annonce a WHERE a.client.id = :clientId AND a.estActive = true")
    List<Annonce> findActiveAnnoncesByClientId(@Param("clientId") Integer clientId);
    
    @Query("SELECT a FROM Annonce a JOIN a.candidatures c WHERE c.livreur.id = :livreurId")
    List<Annonce> findAnnoncesByCandidatureLivreurId(@Param("livreurId") Integer livreurId);
    
    @Modifying
    @Query("UPDATE Annonce a SET a.estActive = false WHERE a.dateExpiration < :date")
    int deactivateExpiredAnnonces(@Param("date") LocalDateTime date);
}

// Classe pour les Specifications JPA
public class AnnonceSpecifications {
    
    public static Specification<Annonce> hasVille(String ville) {
        return (root, query, criteriaBuilder) -> {
            if (ville == null || ville.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.or(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("adresseDepart")), 
                    "%" + ville.toLowerCase() + "%"
                ),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("adresseArrivee")), 
                    "%" + ville.toLowerCase() + "%"
                )
            );
        };
    }
    
    public static Specification<Annonce> hasCategorie(Integer categorieId) {
        return (root, query, criteriaBuilder) -> {
            if (categorieId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("categorie").get("id"), categorieId);
        };
    }
    
    public static Specification<Annonce> isActive() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.isTrue(root.get("estActive"));
    }
}
```

### Gestion des WebSocket

#### Configuration WebSocket

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final TrackingWebSocketHandler trackingHandler;
    private final ChatWebSocketHandler chatHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(trackingHandler, "/ws/tracking/{livraisonId}")
                .setAllowedOrigins("*")
                .withSockJS();
                
        registry.addHandler(chatHandler, "/ws/chat/{conversationId}")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}

@Component
public class TrackingWebSocketHandler extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final LivraisonService livraisonService;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String livraisonId = extractLivraisonId(session);
        sessions.put(livraisonId, session);
        
        // Envoyer la position actuelle
        Livraison livraison = livraisonService.findById(Integer.valueOf(livraisonId));
        if (livraison != null && livraison.getPositionActuelle() != null) {
            sendPositionUpdate(session, livraison.getPositionActuelle());
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) 
            throws Exception {
        // Traitement des messages de mise à jour de position
        PositionUpdate update = objectMapper.readValue(
            message.getPayload(), PositionUpdate.class);
        
        String livraisonId = extractLivraisonId(session);
        livraisonService.updatePosition(Integer.valueOf(livraisonId), update);
        
        // Diffuser à tous les clients connectés pour cette livraison
        broadcastPositionUpdate(livraisonId, update);
    }
    
    public void broadcastPositionUpdate(String livraisonId, PositionUpdate update) {
        sessions.values().forEach(session -> {
            try {
                sendPositionUpdate(session, update);
            } catch (IOException e) {
                logger.error("Erreur envoi position", e);
            }
        });
    }
}
```

---

## Architecture Frontend

### Structure Vue.js

```
src/
├── main.js                    # Point d'entrée
├── App.vue                    # Composant racine
├── router/
│   └── index.js              # Configuration Vue Router
├── stores/                   # Pinia stores
│   ├── auth.js              # Authentification
│   ├── annonces.js          # Gestion des annonces
│   ├── livraisons.js        # Gestion des livraisons
│   └── notifications.js     # Notifications
├── views/                   # Pages principales
│   ├── HomeView.vue
│   ├── auth/
│   │   ├── LoginView.vue
│   │   └── RegisterView.vue
│   ├── client/
│   │   ├── DashboardView.vue
│   │   ├── AnnoncesView.vue
│   │   └── LivraisonsView.vue
│   └── admin/
│       ├── AdminDashboard.vue
│       └── UserManagement.vue
├── components/              # Composants réutilisables
│   ├── common/
│   │   ├── AppHeader.vue
│   │   ├── AppNavigation.vue
│   │   └── LoadingSpinner.vue
│   ├── forms/
│   │   ├── AnnonceForm.vue
│   │   └── ProfileForm.vue
│   └── ui/
│       ├── DataTable.vue
│       ├── Modal.vue
│       └── Toast.vue
├── composables/             # Composition API
│   ├── useApi.js
│   ├── useWebSocket.js
│   └── useGeolocation.js
├── services/                # Services API
│   ├── api.js               # Client HTTP
│   ├── authService.js
│   ├── annonceService.js
│   └── livraisonService.js
├── utils/                   # Utilitaires
│   ├── validation.js
│   ├── formatting.js
│   └── constants.js
└── assets/                  # Ressources statiques
    ├── styles/
    │   ├── main.css
    │   └── components.css
    └── images/
```

### Architecture des Stores Pinia

#### Store d'Authentification

```javascript
// stores/auth.js
import { defineStore } from 'pinia'
import { authService } from '@/services/authService'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token'),
    isAuthenticated: false,
    isLoading: false
  }),
  
  getters: {
    userRole: (state) => state.user?.type,
    isClient: (state) => state.user?.type === 'CLIENT',
    isLivreur: (state) => state.user?.type === 'LIVREUR',
    isAdmin: (state) => state.user?.type === 'ADMIN'
  },
  
  actions: {
    async login(credentials) {
      this.isLoading = true
      try {
        const response = await authService.login(credentials)
        this.token = response.token
        this.user = response.utilisateur
        this.isAuthenticated = true
        
        localStorage.setItem('token', this.token)
        this.setupTokenRefresh()
        
        return response
      } catch (error) {
        this.logout()
        throw error
      } finally {
        this.isLoading = false
      }
    },
    
    async logout() {
      try {
        if (this.token) {
          await authService.logout(this.token)
        }
      } catch (error) {
        console.error('Erreur lors de la déconnexion:', error)
      } finally {
        this.user = null
        this.token = null
        this.isAuthenticated = false
        localStorage.removeItem('token')
        clearTimeout(this.refreshTimer)
      }
    },
    
    async refreshToken() {
      try {
        const response = await authService.refresh(this.token)
        this.token = response.token
        localStorage.setItem('token', this.token)
        this.setupTokenRefresh()
      } catch (error) {
        console.error('Erreur refresh token:', error)
        this.logout()
      }
    },
    
    setupTokenRefresh() {
      // Renouveler le token 5 minutes avant expiration
      const expirationTime = this.getTokenExpiration()
      const refreshTime = expirationTime - Date.now() - 5 * 60 * 1000
      
      if (refreshTime > 0) {
        this.refreshTimer = setTimeout(() => {
          this.refreshToken()
        }, refreshTime)
      }
    },
    
    getTokenExpiration() {
      if (!this.token) return 0
      try {
        const payload = JSON.parse(atob(this.token.split('.')[1]))
        return payload.exp * 1000
      } catch {
        return 0
      }
    }
  }
})
```

#### Store des Annonces

```javascript
// stores/annonces.js
import { defineStore } from 'pinia'
import { annonceService } from '@/services/annonceService'

export const useAnnoncesStore = defineStore('annonces', {
  state: () => ({
    annonces: [],
    currentAnnonce: null,
    filters: {
      ville: '',
      categorie: null,
      prixMin: null,
      prixMax: null
    },
    pagination: {
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0
    },
    isLoading: false,
    error: null
  }),
  
  getters: {
    filteredAnnonces: (state) => {
      return state.annonces.filter(annonce => {
        if (state.filters.ville && 
            !annonce.adresseDepart.toLowerCase().includes(state.filters.ville.toLowerCase()) &&
            !annonce.adresseArrivee.toLowerCase().includes(state.filters.ville.toLowerCase())) {
          return false
        }
        if (state.filters.categorie && annonce.categorie.id !== state.filters.categorie) {
          return false
        }
        if (state.filters.prixMin && annonce.prix < state.filters.prixMin) {
          return false
        }
        if (state.filters.prixMax && annonce.prix > state.filters.prixMax) {
          return false
        }
        return true
      })
    },
    
    hasNextPage: (state) => state.pagination.page < state.pagination.totalPages - 1,
    hasPreviousPage: (state) => state.pagination.page > 0
  },
  
  actions: {
    async fetchAnnonces(params = {}) {
      this.isLoading = true
      this.error = null
      
      try {
        const queryParams = {
          page: this.pagination.page,
          size: this.pagination.size,
          ...this.filters,
          ...params
        }
        
        const response = await annonceService.getAnnonces(queryParams)
        
        this.annonces = response.content
        this.pagination = {
          page: response.number,
          size: response.size,
          totalElements: response.totalElements,
          totalPages: response.totalPages
        }
      } catch (error) {
        this.error = error.message
        throw error
      } finally {
        this.isLoading = false
      }
    },
    
    async createAnnonce(annonceData) {
      try {
        const newAnnonce = await annonceService.createAnnonce(annonceData)
        this.annonces.unshift(newAnnonce)
        return newAnnonce
      } catch (error) {
        this.error = error.message
        throw error
      }
    },
    
    async updateAnnonce(id, annonceData) {
      try {
        const updatedAnnonce = await annonceService.updateAnnonce(id, annonceData)
        const index = this.annonces.findIndex(a => a.idAnnonce === id)
        if (index !== -1) {
          this.annonces[index] = updatedAnnonce
        }
        return updatedAnnonce
      } catch (error) {
        this.error = error.message
        throw error
      }
    },
    
    setFilters(newFilters) {
      this.filters = { ...this.filters, ...newFilters }
      this.pagination.page = 0 // Reset à la première page
      this.fetchAnnonces()
    },
    
    nextPage() {
      if (this.hasNextPage) {
        this.pagination.page++
        this.fetchAnnonces()
      }
    },
    
    previousPage() {
      if (this.hasPreviousPage) {
        this.pagination.page--
        this.fetchAnnonces()
      }
    }
  }
})
```

### Composables Vue.js

#### Composable WebSocket

```javascript
// composables/useWebSocket.js
import { ref, onMounted, onUnmounted } from 'vue'

export function useWebSocket(url) {
  const socket = ref(null)
  const isConnected = ref(false)
  const lastMessage = ref(null)
  const error = ref(null)
  
  const connect = () => {
    try {
      socket.value = new WebSocket(url)
      
      socket.value.onopen = () => {
        isConnected.value = true
        error.value = null
        console.log('WebSocket connecté:', url)
      }
      
      socket.value.onmessage = (event) => {
        try {
          lastMessage.value = JSON.parse(event.data)
        } catch (e) {
          lastMessage.value = event.data
        }
      }
      
      socket.value.onclose = () => {
        isConnected.value = false
        console.log('WebSocket fermé:', url)
        
        // Reconnexion automatique après 3 secondes
        setTimeout(() => {
          if (!isConnected.value) {
            connect()
          }
        }, 3000)
      }
      
      socket.value.onerror = (event) => {
        error.value = event
        console.error('Erreur WebSocket:', event)
      }
    } catch (e) {
      error.value = e
      console.error('Erreur création WebSocket:', e)
    }
  }
  
  const disconnect = () => {
    if (socket.value) {
      socket.value.close()
      socket.value = null
      isConnected.value = false
    }
  }
  
  const sendMessage = (message) => {
    if (socket.value && isConnected.value) {
      const data = typeof message === 'string' ? message : JSON.stringify(message)
      socket.value.send(data)
    } else {
      console.warn('WebSocket non connecté, impossible d\'envoyer le message')
    }
  }
  
  onMounted(() => {
    connect()
  })
  
  onUnmounted(() => {
    disconnect()
  })
  
  return {
    isConnected,
    lastMessage,
    error,
    sendMessage,
    connect,
    disconnect
  }
}
```

#### Composable pour Géolocalisation

```javascript
// composables/useGeolocation.js
import { ref, onMounted } from 'vue'

export function useGeolocation() {
  const coordinates = ref({ latitude: null, longitude: null })
  const error = ref(null)
  const isLoading = ref(false)
  const isSupported = ref(false)
  
  const getCurrentPosition = () => {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Géolocalisation non supportée'))
        return
      }
      
      isLoading.value = true
      
      navigator.geolocation.getCurrentPosition(
        (position) => {
          coordinates.value = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude
          }
          isLoading.value = false
          resolve(coordinates.value)
        },
        (err) => {
          error.value = err.message
          isLoading.value = false
          reject(err)
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 600000 // 10 minutes
        }
      )
    })
  }
  
  const watchPosition = (callback) => {
    if (!navigator.geolocation) {
      error.value = 'Géolocalisation non supportée'
      return null
    }
    
    return navigator.geolocation.watchPosition(
      (position) => {
        coordinates.value = {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        }
        callback(coordinates.value)
      },
      (err) => {
        error.value = err.message
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 60000 // 1 minute
      }
    )
  }
  
  onMounted(() => {
    isSupported.value = 'geolocation' in navigator
  })
  
  return {
    coordinates,
    error,
    isLoading,
    isSupported,
    getCurrentPosition,
    watchPosition
  }
}
```

---

## Base de Données

### Modèle de Données

#### Diagramme Entité-Relation

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   UTILISATEUR   │     │    ANNONCE      │     │   CANDIDATURE   │
├─────────────────┤     ├─────────────────┤     ├─────────────────┤
│ id_utilisateur  │◄────┤ id_client       │     │ id_candidature  │
│ nom            │     │ id_annonce      │────►│ id_annonce      │
│ prenom         │     │ titre           │     │ id_livreur      │
│ email          │     │ description     │     │ prix            │
│ type           │     │ prix            │     │ statut          │
│ statut         │     │ adresse_depart  │     │ date_creation   │
└─────────────────┘     │ adresse_arrivee │     └─────────────────┘
                        │ date_creation   │
                        │ date_expiration │
                        │ est_active      │
                        └─────────────────┘
                                │
                                ▼
                        ┌─────────────────┐
                        │   LIVRAISON     │
                        ├─────────────────┤
                        │ id_livraison    │
                        │ id_annonce      │
                        │ id_livreur      │
                        │ statut          │
                        │ date_debut      │
                        │ date_fin        │
                        │ code_validation │
                        └─────────────────┘
```

#### Héritage des Utilisateurs

```sql
-- Table parent UTILISATEUR
CREATE TABLE UTILISATEUR (
    id_utilisateur SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    telephone VARCHAR(20),
    adresse TEXT,
    date_de_naissance DATE,
    genre BOOLEAN,
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    derniere_connexion TIMESTAMP,
    statut VARCHAR(20) DEFAULT 'ACTIVE',
    note_globale DECIMAL(3,2) DEFAULT 0,
    nombre_evaluations INTEGER DEFAULT 0,
    type_utilisateur VARCHAR(20) NOT NULL
);

-- Table CLIENT (héritage)
CREATE TABLE CLIENT (
    id_utilisateur INTEGER PRIMARY KEY REFERENCES UTILISATEUR(id_utilisateur),
    preferences_livraison TEXT,
    adresse_facturation TEXT
);

-- Table LIVREUR (héritage)
CREATE TABLE LIVREUR (
    id_utilisateur INTEGER PRIMARY KEY REFERENCES UTILISATEUR(id_utilisateur),
    type_vehicule VARCHAR(50),
    numero_permis VARCHAR(50),
    zone_activite VARCHAR(255),
    capacite_transport DECIMAL(5,2),
    est_disponible BOOLEAN DEFAULT true,
    position_latitude DECIMAL(10,8),
    position_longitude DECIMAL(11,8),
    derniere_position_maj TIMESTAMP
);

-- Table COMMERCANT (héritage)
CREATE TABLE COMMERCANT (
    id_utilisateur INTEGER PRIMARY KEY REFERENCES UTILISATEUR(id_utilisateur),
    nom_commerce VARCHAR(255) NOT NULL,
    siret VARCHAR(14),
    description_commerce TEXT,
    horaires_ouverture TEXT
);
```

#### Index et Optimisations

```sql
-- Index pour les recherches fréquentes
CREATE INDEX idx_utilisateur_email ON UTILISATEUR(email);
CREATE INDEX idx_utilisateur_type ON UTILISATEUR(type_utilisateur);
CREATE INDEX idx_annonce_date_creation ON ANNONCE(date_creation DESC);
CREATE INDEX idx_annonce_ville ON ANNONCE USING gin(to_tsvector('french', adresse_depart || ' ' || adresse_arrivee));
CREATE INDEX idx_livraison_statut ON LIVRAISON(statut);
CREATE INDEX idx_livreur_position ON LIVREUR(position_latitude, position_longitude);

-- Index composites
CREATE INDEX idx_annonce_active_categorie ON ANNONCE(est_active, id_categorie) WHERE est_active = true;
CREATE INDEX idx_candidature_livreur_statut ON CANDIDATURE(id_livreur, statut);

-- Index partiels pour les performances
CREATE INDEX idx_annonce_active_recent ON ANNONCE(date_creation DESC) 
    WHERE est_active = true AND date_creation > (CURRENT_TIMESTAMP - INTERVAL '30 days');
```

#### Contraintes et Triggers

```sql
-- Contraintes métier
ALTER TABLE ANNONCE ADD CONSTRAINT chk_prix_positif 
    CHECK (prix > 0);

ALTER TABLE CANDIDATURE ADD CONSTRAINT chk_prix_candidature_positif 
    CHECK (prix > 0);

ALTER TABLE UTILISATEUR ADD CONSTRAINT chk_email_format 
    CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

-- Trigger pour mettre à jour la note globale
CREATE OR REPLACE FUNCTION update_note_globale()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE UTILISATEUR 
    SET note_globale = (
        SELECT AVG(note) 
        FROM EVALUATION 
        WHERE id_utilisateur_evalue = NEW.id_utilisateur_evalue
    ),
    nombre_evaluations = (
        SELECT COUNT(*) 
        FROM EVALUATION 
        WHERE id_utilisateur_evalue = NEW.id_utilisateur_evalue
    )
    WHERE id_utilisateur = NEW.id_utilisateur_evalue;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_note_globale
    AFTER INSERT OR UPDATE ON EVALUATION
    FOR EACH ROW
    EXECUTE FUNCTION update_note_globale();
```

---

## Sécurité

### Authentification JWT

#### Configuration Spring Security

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                
                // Endpoints par rôle
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/annonces").hasRole("CLIENT")
                .requestMatchers(HttpMethod.POST, "/api/candidatures").hasRole("LIVREUR")
                
                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> 
                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "https://*.railway.app",
            "https://ecodeli.fr"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

#### Filtre JWT

```java
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain chain) throws ServletException, IOException {
        
        String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;
        
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                logger.error("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                logger.error("JWT Token has expired");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                return;
            } catch (MalformedJwtException e) {
                logger.error("JWT Token is malformed");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }
        }
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        chain.doFilter(request, response);
    }
}
```

### Validation et Sanitisation

#### Validation des Données

```java
// DTO avec validation
public class CreateAnnonceRequest {
    
    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 100, message = "Le titre doit contenir entre 5 et 100 caractères")
    private String titre;
    
    @NotBlank(message = "La description est obligatoire")
    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;
    
    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    @Digits(integer = 6, fraction = 2, message = "Format de prix invalide")
    private BigDecimal prix;
    
    @NotBlank(message = "L'adresse de départ est obligatoire")
    @Pattern(regexp = "^[\\p{L}0-9\\s,.-]+$", message = "Adresse de départ invalide")
    private String adresseDepart;
    
    @NotBlank(message = "L'adresse d'arrivée est obligatoire")
    @Pattern(regexp = "^[\\p{L}0-9\\s,.-]+$", message = "Adresse d'arrivée invalide")
    private String adresseArrivee;
    
    @Future(message = "La date d'expiration doit être dans le futur")
    private LocalDateTime dateExpiration;
    
    // Validation personnalisée
    @AssertTrue(message = "La date d'expiration doit être au moins 1 heure dans le futur")
    public boolean isDateExpirationValid() {
        return dateExpiration == null || 
               dateExpiration.isAfter(LocalDateTime.now().plusHours(1));
    }
}

// Validator personnalisé
@Component
public class AnnonceValidator {
    
    public void validateCreateRequest(CreateAnnonceRequest request) {
        List<String> errors = new ArrayList<>();
        
        // Validation métier spécifique
        if (request.getPrix().compareTo(new BigDecimal("1000")) > 0) {
            errors.add("Le prix ne peut pas dépasser 1000€");
        }
        
        if (isAddressInvalid(request.getAdresseDepart())) {
            errors.add("Adresse de départ invalide");
        }
        
        if (isAddressInvalid(request.getAdresseArrivee())) {
            errors.add("Adresse d'arrivée invalide");
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(", ", errors));
        }
    }
    
    private boolean isAddressInvalid(String address) {
        // Logique de validation d'adresse
        return address == null || 
               address.length() < 10 || 
               !address.matches(".*\\d.*"); // Doit contenir au moins un chiffre
    }
}
```

### Protection CSRF et XSS

#### Configuration CSRF

```java
// Désactivation CSRF pour les API REST (stateless)
.csrf(csrf -> csrf.disable())

// Si CSRF activé (pour les applications web traditionnelles)
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .ignoringRequestMatchers("/api/auth/login", "/api/auth/register")
)
```

#### Protection XSS

```java
@Component
public class XSSFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        XSSRequestWrapper wrappedRequest = new XSSRequestWrapper((HttpServletRequest) request);
        chain.doFilter(wrappedRequest, response);
    }
}

public class XSSRequestWrapper extends HttpServletRequestWrapper {
    
    private static final Pattern[] PATTERNS = {
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };
    
    public XSSRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
    }
    
    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }
        
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = stripXSS(values[i]);
        }
        
        return encodedValues;
    }
    
    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return stripXSS(value);
    }
    
    private String stripXSS(String value) {
        if (value != null) {
            value = StringEscapeUtils.escapeHtml4(value);
            
            for (Pattern scriptPattern : PATTERNS) {
                value = scriptPattern.matcher(value).replaceAll("");
            }
        }
        return value;
    }
}
```

---

## Performance et Scalabilité

### Mise en Cache

#### Configuration Redis

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(
            new RedisStandaloneConfiguration("localhost", 6379));
    }
    
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
            .transactionAware()
            .build();
    }
}

// Utilisation du cache dans les services
@Service
public class AnnonceService {
    
    @Cacheable(value = "annonces", key = "#page + '_' + #size + '_' + #ville")
    public Page<AnnonceResponse> getAnnonces(int page, int size, String ville) {
        // Logique de récupération
    }
    
    @CacheEvict(value = "annonces", allEntries = true)
    public AnnonceResponse createAnnonce(CreateAnnonceRequest request) {
        // Logique de création - invalide tout le cache
    }
    
    @CachePut(value = "annonce", key = "#result.idAnnonce")
    public AnnonceResponse updateAnnonce(Integer id, UpdateAnnonceRequest request) {
        // Logique de mise à jour - met à jour le cache
    }
}
```

### Optimisation Base de Données

#### Pool de Connexions

```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          batch_size: 25
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
        generate_statistics: false
        format_sql: false
    show-sql: false
```

#### Requêtes Optimisées

```java
@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Integer> {
    
    // Utilisation de @Query pour optimiser
    @Query("SELECT a FROM Annonce a " +
           "JOIN FETCH a.client c " +
           "JOIN FETCH a.categorie cat " +
           "WHERE a.estActive = true " +
           "AND (:ville IS NULL OR LOWER(a.adresseDepart) LIKE LOWER(CONCAT('%', :ville, '%')) " +
           "   OR LOWER(a.adresseArrivee) LIKE LOWER(CONCAT('%', :ville, '%'))) " +
           "ORDER BY a.dateCreation DESC")
    Page<Annonce> findActiveAnnoncesWithDetails(@Param("ville") String ville, 
                                               Pageable pageable);
    
    // Projection pour réduire les données transférées
    @Query("SELECT new com.ecodeli.dto.AnnonceSummary(" +
           "a.idAnnonce, a.titre, a.prix, a.dateCreation, " +
           "c.nom, c.prenom, cat.nom) " +
           "FROM Annonce a " +
           "JOIN a.client c " +
           "JOIN a.categorie cat " +
           "WHERE a.estActive = true")
    List<AnnonceSummary> findActiveAnnoncesSummary();
    
    // Requête native pour performance maximale
    @Query(value = "SELECT * FROM annonce a " +
                   "WHERE a.est_active = true " +
                   "AND ST_DWithin(ST_Point(a.longitude_depart, a.latitude_depart)::geography, " +
                   "                ST_Point(:longitude, :latitude)::geography, :radius)", 
           nativeQuery = true)
    List<Annonce> findAnnoncesNearLocation(@Param("latitude") Double latitude,
                                          @Param("longitude") Double longitude,
                                          @Param("radius") Integer radius);
}
```

### Monitoring avec Micrometer

```java
@Component
public class CustomMetrics {
    
    private final Counter annonceCreatedCounter;
    private final Timer annonceCreationTimer;
    private final Gauge activeUsersGauge;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.annonceCreatedCounter = Counter.builder("ecodeli.annonces.created")
            .description("Nombre d'annonces créées")
            .tag("type", "creation")
            .register(meterRegistry);
            
        this.annonceCreationTimer = Timer.builder("ecodeli.annonces.creation.time")
            .description("Temps de création d'une annonce")
            .register(meterRegistry);
            
        this.activeUsersGauge = Gauge.builder("ecodeli.users.active")
            .description("Nombre d'utilisateurs actifs")
            .register(meterRegistry, this, CustomMetrics::getActiveUsersCount);
    }
    
    public void incrementAnnonceCreated() {
        annonceCreatedCounter.increment();
    }
    
    public Timer.Sample startAnnonceCreationTimer() {
        return Timer.start();
    }
    
    public void recordAnnonceCreationTime(Timer.Sample sample) {
        sample.stop(annonceCreationTimer);
    }
    
    private Double getActiveUsersCount() {
        // Logique pour compter les utilisateurs actifs
        return 1234.0; // Exemple
    }
}

// Utilisation dans le service
@Service
public class AnnonceService {
    
    private final CustomMetrics metrics;
    
    public AnnonceResponse createAnnonce(CreateAnnonceRequest request) {
        Timer.Sample sample = metrics.startAnnonceCreationTimer();
        
        try {
            // Logique de création
            AnnonceResponse response = doCreateAnnonce(request);
            
            metrics.incrementAnnonceCreated();
            return response;
        } finally {
            metrics.recordAnnonceCreationTime(sample);
        }
    }
}
```

---

*Documentation Architecture EcoDeli - Version 1.0 - Décembre 2024*