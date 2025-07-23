# Documentation des Tests EcoDeli

Ce document détaille la stratégie de tests, les outils utilisés et les bonnes pratiques pour assurer la qualité du code de la plateforme EcoDeli.

## Table des Matières

1. [Stratégie de Tests](#stratégie-de-tests)
2. [Tests Backend](#tests-backend)
3. [Tests Frontend](#tests-frontend)
4. [Tests d'Intégration](#tests-dintégration)
5. [Tests End-to-End](#tests-end-to-end)
6. [Couverture de Code](#couverture-de-code)
7. [CI/CD et Tests](#cicd-et-tests)

---

## Stratégie de Tests

### Pyramide des Tests

```
      /\
     /  \
    / E2E \ ←── Tests End-to-End (10%)
   /______\
  /        \
 / Integration \ ←── Tests d'Intégration (20%)
/______________\
/              \
/   Unit Tests   \ ←── Tests Unitaires (70%)
/________________\
```

### Types de Tests par Couche

| Type | Portée | Vitesse | Coût | Fiabilité |
|------|--------|---------|------|-----------|
| **Unitaires** | Fonction/Méthode | Rapide | Faible | Élevée |
| **Intégration** | Composant/Service | Moyen | Moyen | Élevée |
| **End-to-End** | Application complète | Lent | Élevé | Variable |

### Objectifs de Couverture

- **Tests Unitaires** : ≥ 80%
- **Tests d'Intégration** : ≥ 60%
- **Tests E2E** : Scénarios critiques uniquement

---

## Tests Backend

### Configuration JUnit 5

#### Structure des Tests

```
src/test/java/
├── com/ecodeli/ecodeli_backend/
│   ├── unit/                    # Tests unitaires
│   │   ├── services/
│   │   │   ├── UtilisateurServiceTest.java
│   │   │   └── AnnonceServiceTest.java
│   │   ├── controllers/
│   │   │   └── AnnonceControllerTest.java
│   │   └── utils/
│   │       └── ValidationUtilsTest.java
│   ├── integration/             # Tests d'intégration
│   │   ├── repositories/
│   │   │   ├── UtilisateurRepositoryTest.java
│   │   │   └── AnnonceRepositoryTest.java
│   │   ├── services/
│   │   │   └── AnnonceServiceIntegrationTest.java
│   │   └── controllers/
│   │       └── AnnonceControllerIntegrationTest.java
│   └── config/                  # Configuration des tests
│       ├── TestConfig.java
│       ├── TestDataBuilder.java
│       └── TestContainersConfig.java
└── resources/
    ├── application-test.yml
    ├── data-test.sql
    └── test-data/
```

#### Configuration Test

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    
  mail:
    host: localhost
    port: 3025  # MailHog pour tests

jwt:
  secret: test-secret-key-for-testing-purposes-only
  expiration: 3600000

logging:
  level:
    com.ecodeli: DEBUG
    org.springframework.test: DEBUG
```

### Tests Unitaires

#### Test de Service

```java
@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {
    
    @Mock
    private UtilisateurRepository utilisateurRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;
    
    @Test
    @DisplayName("Devrait créer un utilisateur client avec succès")
    void shouldCreateClientSuccessfully() {
        // Given
        CreateUtilisateurRequest request = CreateUtilisateurRequest.builder()
            .nom("Dupont")
            .prenom("Jean")
            .email("jean.dupont@example.com")
            .motDePasse("password123")
            .type(TypeUtilisateur.CLIENT)
            .build();
        
        Client expectedClient = new Client();
        expectedClient.setIdUtilisateur(1);
        expectedClient.setNom("Dupont");
        expectedClient.setEmail("jean.dupont@example.com");
        
        when(utilisateurRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getMotDePasse())).thenReturn("encoded-password");
        when(utilisateurRepository.save(any(Client.class))).thenReturn(expectedClient);
        
        // When
        Utilisateur result = utilisateurService.createUtilisateur(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("Dupont");
        assertThat(result.getEmail()).isEqualTo("jean.dupont@example.com");
        assertThat(result).isInstanceOf(Client.class);
        
        verify(utilisateurRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getMotDePasse());
        verify(utilisateurRepository).save(any(Client.class));
        verify(emailService).sendWelcomeEmail(expectedClient);
    }
    
    @Test
    @DisplayName("Devrait lever une exception si l'email existe déjà")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        CreateUtilisateurRequest request = CreateUtilisateurRequest.builder()
            .email("existing@example.com")
            .build();
        
        when(utilisateurRepository.existsByEmail(request.getEmail())).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> utilisateurService.createUtilisateur(request))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessage("Email déjà utilisé: existing@example.com");
        
        verify(utilisateurRepository).existsByEmail(request.getEmail());
        verifyNoMoreInteractions(utilisateurRepository, passwordEncoder, emailService);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email", "test@", "@example.com"})
    @DisplayName("Devrait valider le format de l'email")
    void shouldValidateEmailFormat(String invalidEmail) {
        // Given
        CreateUtilisateurRequest request = CreateUtilisateurRequest.builder()
            .email(invalidEmail)
            .build();
        
        // When & Then
        assertThatThrownBy(() -> utilisateurService.createUtilisateur(request))
            .isInstanceOf(ValidationException.class);
    }
    
    @Test
    @DisplayName("Devrait gérer les erreurs de base de données")
    void shouldHandleDatabaseErrors() {
        // Given
        CreateUtilisateurRequest request = CreateUtilisateurRequest.builder()
            .nom("Test")
            .prenom("User")
            .email("test@example.com")
            .motDePasse("password123")
            .type(TypeUtilisateur.CLIENT)
            .build();
        
        when(utilisateurRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(utilisateurRepository.save(any())).thenThrow(new DataAccessException("DB Error") {});
        
        // When & Then
        assertThatThrownBy(() -> utilisateurService.createUtilisateur(request))
            .isInstanceOf(DataAccessException.class)
            .hasMessage("DB Error");
    }
}
```

#### Test de Controller

```java
@WebMvcTest(AnnonceController.class)
class AnnonceControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AnnonceService annonceService;
    
    @MockBean
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("GET /api/annonces devrait retourner la liste des annonces")
    void shouldReturnAnnoncesList() throws Exception {
        // Given
        List<AnnonceResponse> annonces = Arrays.asList(
            AnnonceResponse.builder()
                .idAnnonce(1)
                .titre("Livraison urgente")
                .prix(BigDecimal.valueOf(25.50))
                .build(),
            AnnonceResponse.builder()
                .idAnnonce(2)
                .titre("Transport meubles")
                .prix(BigDecimal.valueOf(75.00))
                .build()
        );
        
        Page<AnnonceResponse> page = new PageImpl<>(annonces);
        when(annonceService.getAnnonces(any(Pageable.class), anyString(), any()))
            .thenReturn(page);
        
        // When & Then
        mockMvc.perform(get("/api/annonces")
                .param("page", "0")
                .param("size", "10")
                .param("ville", "Paris"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].idAnnonce").value(1))
            .andExpect(jsonPath("$.content[0].titre").value("Livraison urgente"))
            .andExpect(jsonPath("$.content[0].prix").value(25.50))
            .andExpect(jsonPath("$.totalElements").value(2));
        
        verify(annonceService).getAnnonces(any(Pageable.class), eq("Paris"), any());
    }
    
    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("POST /api/annonces devrait créer une nouvelle annonce")
    void shouldCreateAnnonce() throws Exception {
        // Given
        CreateAnnonceRequest request = CreateAnnonceRequest.builder()
            .titre("Nouvelle annonce")
            .description("Description test")
            .prix(BigDecimal.valueOf(30.00))
            .adresseDepart("Paris 15ème")
            .adresseArrivee("Paris 16ème")
            .idCategorie(1)
            .build();
        
        AnnonceResponse response = AnnonceResponse.builder()
            .idAnnonce(123)
            .titre("Nouvelle annonce")
            .prix(BigDecimal.valueOf(30.00))
            .build();
        
        when(annonceService.createAnnonce(any(CreateAnnonceRequest.class), anyString()))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/annonces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.idAnnonce").value(123))
            .andExpected(jsonPath("$.titre").value("Nouvelle annonce"))
            .andExpect(jsonPath("$.prix").value(30.00));
        
        verify(annonceService).createAnnonce(eq(request), anyString());
    }
    
    @Test
    @DisplayName("POST /api/annonces sans authentification devrait retourner 401")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        // Given
        CreateAnnonceRequest request = CreateAnnonceRequest.builder()
            .titre("Test")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/annonces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
        
        verifyNoInteractions(annonceService);
    }
    
    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("POST /api/annonces avec données invalides devrait retourner 400")
    void shouldReturn400WhenInvalidData() throws Exception {
        // Given
        CreateAnnonceRequest request = CreateAnnonceRequest.builder()
            .titre("") // Titre vide - invalide
            .prix(BigDecimal.valueOf(-10)) // Prix négatif - invalide
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/annonces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details").isArray())
            .andExpect(jsonPath("$.details[*].field", hasItems("titre", "prix")));
    }
}
```

### Tests d'Intégration

#### Test Repository avec TestContainers

```java
@DataJpaTest
@Testcontainers
class AnnonceRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("ecodeli_test")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private AnnonceRepository annonceRepository;
    
    @Test
    @DisplayName("Devrait trouver les annonces actives par client")
    void shouldFindActiveAnnoncesByClient() {
        // Given
        Client client = TestDataBuilder.createClient("test@example.com");
        entityManager.persistAndFlush(client);
        
        Annonce annonceActive = TestDataBuilder.createAnnonce(client, true);
        Annonce annonceInactive = TestDataBuilder.createAnnonce(client, false);
        
        entityManager.persistAndFlush(annonceActive);
        entityManager.persistAndFlush(annonceInactive);
        
        // When
        List<Annonce> result = annonceRepository.findActiveAnnoncesByClientId(client.getIdUtilisateur());
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstActive()).isTrue();
        assertThat(result.get(0).getClient().getIdUtilisateur()).isEqualTo(client.getIdUtilisateur());
    }
    
    @Test
    @DisplayName("Devrait désactiver les annonces expirées")
    void shouldDeactivateExpiredAnnonces() {
        // Given
        Client client = TestDataBuilder.createClient("test@example.com");
        entityManager.persistAndFlush(client);
        
        Annonce annonceExpiree = TestDataBuilder.createAnnonce(client, true);
        annonceExpiree.setDateExpiration(LocalDateTime.now().minusHours(1)); // Expirée
        
        Annonce annonceActive = TestDataBuilder.createAnnonce(client, true);
        annonceActive.setDateExpiration(LocalDateTime.now().plusHours(1)); // Future
        
        entityManager.persistAndFlush(annonceExpiree);
        entityManager.persistAndFlush(annonceActive);
        
        // When
        int updatedCount = annonceRepository.deactivateExpiredAnnonces();
        
        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        entityManager.refresh(annonceExpiree);
        entityManager.refresh(annonceActive);
        
        assertThat(annonceExpiree.getEstActive()).isFalse();
        assertThat(annonceActive.getEstActive()).isTrue();
    }
    
    @Test
    @DisplayName("Devrait chercher les annonces par ville avec Specification")
    void shouldSearchAnnoncesByVilleWithSpecification() {
        // Given
        Client client = TestDataBuilder.createClient("test@example.com");
        entityManager.persistAndFlush(client);
        
        Annonce annonceParis = TestDataBuilder.createAnnonce(client, true);
        annonceParis.setAdresseDepart("123 Rue de la Paix, 75001 Paris");
        annonceParis.setAdresseArrivee("456 Avenue des Champs, 75008 Paris");
        
        Annonce annonceLyon = TestDataBuilder.createAnnonce(client, true);
        annonceLyon.setAdresseDepart("789 Place Bellecour, 69002 Lyon");
        annonceLyon.setAdresseArrivee("321 Rue de la République, 69001 Lyon");
        
        entityManager.persistAndFlush(annonceParis);
        entityManager.persistAndFlush(annonceLyon);
        
        // When
        Specification<Annonce> spec = Specification
            .where(AnnonceSpecifications.isActive())
            .and(AnnonceSpecifications.hasVilleInAddress("Paris"));
        
        List<Annonce> result = annonceRepository.findAll(spec);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAdresseDepart()).contains("Paris");
    }
}
```

#### Test Service d'Intégration

```java
@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.yml")
class AnnonceServiceIntegrationTest {
    
    @Autowired
    private AnnonceService annonceService;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @MockBean
    private GoogleMapsService googleMapsService;
    
    @MockBean
    private NotificationService notificationService;
    
    private Client testClient;
    
    @BeforeEach
    void setUp() {
        testClient = TestDataBuilder.createClient("client@example.com");
        testClient = utilisateurRepository.save(testClient);
    }
    
    @Test
    @DisplayName("Devrait créer une annonce avec géocodage")
    void shouldCreateAnnonceWithGeocoding() {
        // Given
        CreateAnnonceRequest request = CreateAnnonceRequest.builder()
            .titre("Livraison test")
            .description("Description test")
            .prix(BigDecimal.valueOf(25.00))
            .adresseDepart("Paris 15ème")
            .adresseArrivee("Paris 16ème")
            .dateExpiration(LocalDateTime.now().plusHours(24))
            .idCategorie(1)
            .build();
        
        GeocodingResult departCoords = new GeocodingResult(48.8534, 2.2945);
        GeocodingResult arriveeCoords = new GeocodingResult(48.8698, 2.2885);
        
        when(googleMapsService.geocode("Paris 15ème")).thenReturn(departCoords);
        when(googleMapsService.geocode("Paris 16ème")).thenReturn(arriveeCoords);
        
        // When
        AnnonceResponse result = annonceService.createAnnonce(request, testClient.getEmail());
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitre()).isEqualTo("Livraison test");
        assertThat(result.getPrix()).isEqualByComparingTo(BigDecimal.valueOf(25.00));
        assertThat(result.getClient().getIdUtilisateur()).isEqualTo(testClient.getIdUtilisateur());
        
        verify(googleMapsService).geocode("Paris 15ème");
        verify(googleMapsService).geocode("Paris 16ème");
        verify(notificationService).notifyLivreursNouvelleAnnonce(any(Annonce.class));
    }
    
    @Test
    @DisplayName("Devrait valider les données métier")
    void shouldValidateBusinessRules() {
        // Given
        CreateAnnonceRequest request = CreateAnnonceRequest.builder()
            .titre("Test")
            .description("Test")
            .prix(BigDecimal.valueOf(25.00))
            .adresseDepart("Paris")
            .adresseArrivee("Lyon")
            .dateExpiration(LocalDateTime.now().minusHours(1)) // Date passée
            .idCategorie(1)
            .build();
        
        // When & Then
        assertThatThrownBy(() -> annonceService.createAnnonce(request, testClient.getEmail()))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("date d'expiration");
        
        verifyNoInteractions(googleMapsService, notificationService);
    }
}
```

### Builders de Données de Test

```java
// TestDataBuilder.java
public class TestDataBuilder {
    
    public static Client createClient(String email) {
        Client client = new Client();
        client.setNom("Test");
        client.setPrenom("User");
        client.setEmail(email);
        client.setMotDePasse("encoded-password");
        client.setTelephone("0123456789");
        client.setAdresse("123 Test Street");
        client.setDateInscription(LocalDateTime.now());
        client.setStatut(StatutUtilisateur.ACTIVE);
        return client;
    }
    
    public static Livreur createLivreur(String email) {
        Livreur livreur = new Livreur();
        livreur.setNom("Test");
        livreur.setPrenom("Livreur");
        livreur.setEmail(email);
        livreur.setMotDePasse("encoded-password");
        livreur.setTypeVehicule(TypeVehicule.VELO);
        livreur.setCapaciteTransport(BigDecimal.valueOf(15.0));
        livreur.setEstDisponible(true);
        return livreur;
    }
    
    public static Annonce createAnnonce(Client client, boolean active) {
        Annonce annonce = new Annonce();
        annonce.setTitre("Annonce test");
        annonce.setDescription("Description test");
        annonce.setPrix(BigDecimal.valueOf(25.00));
        annonce.setAdresseDepart("Paris 15ème");
        annonce.setAdresseArrivee("Paris 16ème");
        annonce.setDateCreation(LocalDateTime.now());
        annonce.setDateExpiration(LocalDateTime.now().plusHours(24));
        annonce.setEstActive(active);
        annonce.setClient(client);
        annonce.setCategorie(createCategorie());
        return annonce;
    }
    
    public static CategorieAnnonce createCategorie() {
        CategorieAnnonce categorie = new CategorieAnnonce();
        categorie.setIdCategorie(1);
        categorie.setNom("Général");
        categorie.setDescription("Catégorie générale");
        return categorie;
    }
    
    // Builder pattern pour plus de flexibilité
    public static class AnnonceBuilder {
        private Annonce annonce = new Annonce();
        
        public static AnnonceBuilder builder() {
            return new AnnonceBuilder();
        }
        
        public AnnonceBuilder titre(String titre) {
            annonce.setTitre(titre);
            return this;
        }
        
        public AnnonceBuilder prix(BigDecimal prix) {
            annonce.setPrix(prix);
            return this;
        }
        
        public AnnonceBuilder client(Client client) {
            annonce.setClient(client);
            return this;
        }
        
        public AnnonceBuilder active(boolean active) {
            annonce.setEstActive(active);
            return this;
        }
        
        public AnnonceBuilder expiration(LocalDateTime expiration) {
            annonce.setDateExpiration(expiration);
            return this;
        }
        
        public Annonce build() {
            // Valeurs par défaut
            if (annonce.getTitre() == null) annonce.setTitre("Annonce test");
            if (annonce.getDescription() == null) annonce.setDescription("Description test");
            if (annonce.getPrix() == null) annonce.setPrix(BigDecimal.valueOf(25.00));
            if (annonce.getDateCreation() == null) annonce.setDateCreation(LocalDateTime.now());
            if (annonce.getDateExpiration() == null) annonce.setDateExpiration(LocalDateTime.now().plusHours(24));
            if (annonce.getCategorie() == null) annonce.setCategorie(createCategorie());
            
            return annonce;
        }
    }
}
```

---

## Tests Frontend

### Configuration Jest et Vue Test Utils

#### Configuration

```javascript
// jest.config.js
module.exports = {
  preset: '@vue/cli-plugin-unit-jest',
  
  testEnvironment: 'jsdom',
  
  moduleFileExtensions: ['js', 'json', 'vue'],
  
  transform: {
    '^.+\\.vue$': '@vue/vue3-jest',
    '^.+\\.js$': 'babel-jest'
  },
  
  testMatch: [
    '**/tests/unit/**/*.spec.(js|jsx|ts|tsx)|**/__tests__/*.(js|jsx|ts|tsx)'
  ],
  
  collectCoverageFrom: [
    'src/**/*.{js,vue}',
    '!src/main.js',
    '!src/router/index.js',
    '!**/node_modules/**'
  ],
  
  coverageThreshold: {
    global: {
      branches: 70,
      functions: 70,
      lines: 70,
      statements: 70
    }
  },
  
  setupFilesAfterEnv: ['<rootDir>/tests/setup.js']
}

// tests/setup.js
import { config } from '@vue/test-utils'
import { createPinia } from 'pinia'

// Configuration globale pour les tests
config.global.plugins = [createPinia()]

// Mocks globaux
global.fetch = jest.fn()

// Mock console pour éviter les logs en test
global.console = {
  ...console,
  log: jest.fn(),
  debug: jest.fn(),
  info: jest.fn(),
  warn: jest.fn(),
  error: jest.fn()
}
```

### Tests de Composants

#### Test de Composant Simple

```javascript
// tests/unit/components/UserForm.spec.js
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import UserForm from '@/components/UserForm.vue'
import { useUserStore } from '@/stores/user'

describe('UserForm', () => {
  let wrapper
  let userStore
  
  beforeEach(() => {
    setActivePinia(createPinia())
    userStore = useUserStore()
    
    wrapper = mount(UserForm, {
      props: {
        userId: null
      }
    })
  })
  
  afterEach(() => {
    wrapper.unmount()
  })
  
  describe('Rendu initial', () => {
    it('devrait afficher le formulaire', () => {
      expect(wrapper.find('[data-test="user-form"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="nom-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="prenom-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="email-input"]').exists()).toBe(true)
    })
    
    it('devrait afficher le bon titre pour création', () => {
      expect(wrapper.find('h2').text()).toBe('Créer un utilisateur')
    })
    
    it('devrait avoir des champs vides initialement', () => {
      expect(wrapper.find('[data-test="nom-input"]').element.value).toBe('')
      expect(wrapper.find('[data-test="prenom-input"]').element.value).toBe('')
      expect(wrapper.find('[data-test="email-input"]').element.value).toBe('')
    })
  })
  
  describe('Mode édition', () => {
    beforeEach(async () => {
      await wrapper.setProps({ userId: 123 })
    })
    
    it('devrait afficher le bon titre pour édition', () => {
      expect(wrapper.find('h2').text()).toBe('Modifier un utilisateur')
    })
    
    it('devrait charger les données utilisateur', async () => {
      const userData = {
        nom: 'Dupont',
        prenom: 'Jean',
        email: 'jean.dupont@example.com'
      }
      
      userStore.fetchUser = jest.fn().mockResolvedValue(userData)
      
      await wrapper.setProps({ userId: 123 })
      await wrapper.vm.$nextTick()
      
      expect(userStore.fetchUser).toHaveBeenCalledWith(123)
    })
  })
  
  describe('Validation', () => {
    it('devrait valider les champs requis', async () => {
      const submitButton = wrapper.find('[data-test="submit-button"]')
      await submitButton.trigger('click')
      
      expect(wrapper.find('.error-message').exists()).toBe(true)
      expect(wrapper.emitted('user-saved')).toBeFalsy()
    })
    
    it('devrait valider le format email', async () => {
      await wrapper.find('[data-test="email-input"]').setValue('email-invalide')
      await wrapper.find('[data-test="email-input"]').trigger('blur')
      
      expect(wrapper.find('.error-message').text()).toContain('email invalide')
    })
    
    it('devrait enlever les erreurs quand les champs sont corrigés', async () => {
      // Déclencher une erreur
      await wrapper.find('[data-test="submit-button"]').trigger('click')
      expect(wrapper.find('.error-message').exists()).toBe(true)
      
      // Corriger le champ
      await wrapper.find('[data-test="nom-input"]').setValue('Dupont')
      await wrapper.find('[data-test="nom-input"]').trigger('input')
      
      expect(wrapper.find('.error-message').exists()).toBe(false)
    })
  })
  
  describe('Soumission', () => {
    beforeEach(async () => {
      await wrapper.find('[data-test="nom-input"]').setValue('Dupont')
      await wrapper.find('[data-test="prenom-input"]').setValue('Jean')
      await wrapper.find('[data-test="email-input"]').setValue('jean@example.com')
    })
    
    it('devrait créer un utilisateur', async () => {
      const newUser = { id: 123, nom: 'Dupont', prenom: 'Jean' }
      userStore.createUser = jest.fn().mockResolvedValue(newUser)
      
      await wrapper.find('form').trigger('submit')
      
      expect(userStore.createUser).toHaveBeenCalledWith({
        nom: 'Dupont',
        prenom: 'Jean',
        email: 'jean@example.com'
      })
      
      expect(wrapper.emitted('user-saved')).toBeTruthy()
      expect(wrapper.emitted('user-saved')[0][0]).toEqual(newUser)
    })
    
    it('devrait afficher un indicateur de chargement', async () => {
      userStore.createUser = jest.fn().mockImplementation(() => new Promise(() => {}))
      
      wrapper.find('form').trigger('submit')
      await wrapper.vm.$nextTick()
      
      expect(wrapper.find('[data-test="submit-button"]').text()).toBe('Enregistrement...')
      expect(wrapper.find('[data-test="submit-button"]').attributes('disabled')).toBe('')
    })
    
    it('devrait gérer les erreurs de création', async () => {
      const error = new Error('Erreur serveur')
      userStore.createUser = jest.fn().mockRejectedValue(error)
      
      await wrapper.find('form').trigger('submit')
      
      expect(wrapper.find('.error-message').text()).toContain('Erreur serveur')
      expect(wrapper.emitted('user-saved')).toBeFalsy()
    })
  })
  
  describe('Annulation', () => {
    it('devrait émettre un événement cancel', async () => {
      await wrapper.find('[data-test="cancel-button"]').trigger('click')
      
      expect(wrapper.emitted('cancel')).toBeTruthy()
    })
  })
})
```

#### Test de Composant avec Routing

```javascript
// tests/unit/views/Dashboard.spec.js
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '@/views/Dashboard.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: Dashboard },
    { path: '/annonces', component: { template: '<div>Annonces</div>' } }
  ]
})

describe('Dashboard', () => {
  let wrapper
  
  beforeEach(async () => {
    router.push('/')
    await router.isReady()
    
    wrapper = mount(Dashboard, {
      global: {
        plugins: [router]
      }
    })
  })
  
  it('devrait naviguer vers les annonces', async () => {
    const annonceLink = wrapper.find('[data-test="annonces-link"]')
    await annonceLink.trigger('click')
    
    expect(wrapper.vm.$route.path).toBe('/annonces')
  })
})
```

### Tests de Stores Pinia

```javascript
// tests/unit/stores/user.spec.js
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'
import { authService } from '@/services/authService'

jest.mock('@/services/authService')

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })
  
  describe('État initial', () => {
    it('devrait avoir un état initial correct', () => {
      const store = useUserStore()
      
      expect(store.currentUser).toBeNull()
      expect(store.authToken).toBeNull()
      expect(store.isLoading).toBe(false)
      expect(store.lastError).toBeNull()
    })
  })
  
  describe('Getters', () => {
    it('isAuthenticated devrait retourner false si pas de token', () => {
      const store = useUserStore()
      expect(store.isAuthenticated).toBe(false)
    })
    
    it('isAuthenticated devrait retourner true si token et user', () => {
      const store = useUserStore()
      store.authToken = 'token'
      store.currentUser = { id: 1, nom: 'Test' }
      
      expect(store.isAuthenticated).toBe(true)
    })
    
    it('userRole devrait retourner le type utilisateur', () => {
      const store = useUserStore()
      store.currentUser = { id: 1, type: 'CLIENT' }
      
      expect(store.userRole).toBe('CLIENT')
    })
  })
  
  describe('Actions', () => {
    describe('login', () => {
      it('devrait connecter un utilisateur avec succès', async () => {
        const store = useUserStore()
        const credentials = { email: 'test@example.com', password: 'password' }
        const response = {
          token: 'jwt-token',
          user: { id: 1, nom: 'Test', type: 'CLIENT' }
        }
        
        authService.login.mockResolvedValue(response)
        
        const result = await store.login(credentials)
        
        expect(authService.login).toHaveBeenCalledWith(credentials)
        expect(store.authToken).toBe('jwt-token')
        expect(store.currentUser).toEqual(response.user)
        expect(store.isAuthenticated).toBe(true)
        expect(localStorage.getItem('authToken')).toBe('jwt-token')
        expect(result).toEqual(response.user)
      })
      
      it('devrait gérer les erreurs de connexion', async () => {
        const store = useUserStore()
        const credentials = { email: 'test@example.com', password: 'wrong' }
        const error = new Error('Identifiants incorrects')
        
        authService.login.mockRejectedValue(error)
        
        await expect(store.login(credentials)).rejects.toThrow('Identifiants incorrects')
        
        expect(store.authToken).toBeNull()
        expect(store.currentUser).toBeNull()
        expect(store.isAuthenticated).toBe(false)
        expect(store.lastError).toBe('Identifiants incorrects')
      })
      
      it('devrait gérer l\'état de chargement', async () => {
        const store = useUserStore()
        const credentials = { email: 'test@example.com', password: 'password' }
        
        let resolvePromise
        const promise = new Promise(resolve => { resolvePromise = resolve })
        authService.login.mockReturnValue(promise)
        
        const loginPromise = store.login(credentials)
        
        expect(store.isLoading).toBe(true)
        
        resolvePromise({ token: 'token', user: { id: 1 } })
        await loginPromise
        
        expect(store.isLoading).toBe(false)
      })
    })
    
    describe('logout', () => {
      it('devrait déconnecter un utilisateur', async () => {
        const store = useUserStore()
        store.authToken = 'token'
        store.currentUser = { id: 1 }
        localStorage.setItem('authToken', 'token')
        
        authService.logout.mockResolvedValue()
        
        await store.logout()
        
        expect(authService.logout).toHaveBeenCalledWith('token')
        expect(store.authToken).toBeNull()
        expect(store.currentUser).toBeNull()
        expect(store.isAuthenticated).toBe(false)
        expect(localStorage.getItem('authToken')).toBeNull()
      })
    })
  })
})
```

### Tests de Services

```javascript
// tests/unit/services/annonceService.spec.js
import { annonceService } from '@/services/annonceService'
import apiClient from '@/services/apiClient'

jest.mock('@/services/apiClient')

describe('AnnonceService', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })
  
  describe('getAnnonces', () => {
    it('devrait récupérer la liste des annonces', async () => {
      const mockResponse = {
        data: {
          content: [
            { id: 1, titre: 'Annonce 1' },
            { id: 2, titre: 'Annonce 2' }
          ],
          totalElements: 2
        }
      }
      
      apiClient.get.mockResolvedValue(mockResponse)
      
      const params = { page: 0, size: 10, ville: 'Paris' }
      const result = await annonceService.getAnnonces(params)
      
      expect(apiClient.get).toHaveBeenCalledWith('/api/annonces', { params })
      expect(result).toEqual(mockResponse.data)
    })
    
    it('devrait gérer les erreurs API', async () => {
      const error = {
        response: {
          status: 500,
          data: { message: 'Erreur serveur' }
        }
      }
      
      apiClient.get.mockRejectedValue(error)
      
      await expect(annonceService.getAnnonces({})).rejects.toThrow('Erreur serveur')
    })
  })
  
  describe('createAnnonce', () => {
    it('devrait créer une nouvelle annonce', async () => {
      const annonceData = {
        titre: 'Nouvelle annonce',
        description: 'Description',
        prix: 25.00
      }
      
      const mockResponse = {
        data: { id: 123, ...annonceData }
      }
      
      apiClient.post.mockResolvedValue(mockResponse)
      
      const result = await annonceService.createAnnonce(annonceData)
      
      expect(apiClient.post).toHaveBeenCalledWith('/api/annonces', annonceData)
      expect(result).toEqual(mockResponse.data)
    })
  })
})
```

---

## Tests End-to-End

### Configuration Cypress

```javascript
// cypress.config.js
import { defineConfig } from 'cypress'

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:5173',
    supportFile: 'cypress/support/e2e.js',
    specPattern: 'cypress/e2e/**/*.cy.{js,jsx,ts,tsx}',
    videosFolder: 'cypress/videos',
    screenshotsFolder: 'cypress/screenshots',
    
    env: {
      apiUrl: 'http://localhost:8080',
      testUser: {
        email: 'test@example.com',
        password: 'password123'
      }
    },
    
    setupNodeEvents(on, config) {
      // Plugin configuration
    }
  }
})

// cypress/support/e2e.js
import './commands'

// cypress/support/commands.js
Cypress.Commands.add('login', (email, password) => {
  cy.visit('/login')
  cy.get('[data-cy="email-input"]').type(email)
  cy.get('[data-cy="password-input"]').type(password)
  cy.get('[data-cy="login-button"]').click()
  cy.url().should('not.include', '/login')
})

Cypress.Commands.add('createAnnonce', (annonceData) => {
  cy.visit('/annonces/new')
  cy.get('[data-cy="titre-input"]').type(annonceData.titre)
  cy.get('[data-cy="description-textarea"]').type(annonceData.description)
  cy.get('[data-cy="prix-input"]').type(annonceData.prix.toString())
  cy.get('[data-cy="adresse-depart-input"]').type(annonceData.adresseDepart)
  cy.get('[data-cy="adresse-arrivee-input"]').type(annonceData.adresseArrivee)
  cy.get('[data-cy="submit-button"]').click()
})
```

### Tests E2E

```javascript
// cypress/e2e/auth/login.cy.js
describe('Authentification', () => {
  beforeEach(() => {
    cy.visit('/login')
  })
  
  it('devrait permettre à un utilisateur de se connecter', () => {
    cy.get('[data-cy="email-input"]').type(Cypress.env('testUser.email'))
    cy.get('[data-cy="password-input"]').type(Cypress.env('testUser.password'))
    cy.get('[data-cy="login-button"]').click()
    
    cy.url().should('include', '/dashboard')
    cy.get('[data-cy="user-menu"]').should('be.visible')
    cy.get('[data-cy="welcome-message"]').should('contain', 'Bienvenue')
  })
  
  it('devrait afficher une erreur avec des identifiants incorrects', () => {
    cy.get('[data-cy="email-input"]').type('wrong@example.com')
    cy.get('[data-cy="password-input"]').type('wrongpassword')
    cy.get('[data-cy="login-button"]').click()
    
    cy.get('[data-cy="error-message"]').should('be.visible')
    cy.get('[data-cy="error-message"]').should('contain', 'Identifiants incorrects')
    cy.url().should('include', '/login')
  })
  
  it('devrait valider les champs requis', () => {
    cy.get('[data-cy="login-button"]').click()
    
    cy.get('[data-cy="email-error"]').should('contain', 'Email requis')
    cy.get('[data-cy="password-error"]').should('contain', 'Mot de passe requis')
  })
})

// cypress/e2e/annonces/creation.cy.js
describe('Création d\'annonce', () => {
  beforeEach(() => {
    cy.login(Cypress.env('testUser.email'), Cypress.env('testUser.password'))
  })
  
  it('devrait créer une nouvelle annonce', () => {
    const annonceData = {
      titre: 'Livraison test E2E',
      description: 'Description de test pour E2E',
      prix: 35.50,
      adresseDepart: 'Paris 15ème',
      adresseArrivee: 'Paris 16ème'
    }
    
    cy.createAnnonce(annonceData)
    
    cy.url().should('include', '/annonces')
    cy.get('[data-cy="success-message"]').should('contain', 'Annonce créée avec succès')
    cy.get('[data-cy="annonce-list"]').should('contain', annonceData.titre)
  })
  
  it('devrait calculer automatiquement la distance', () => {
    cy.visit('/annonces/new')
    
    cy.get('[data-cy="adresse-depart-input"]').type('Paris 15ème')
    cy.get('[data-cy="adresse-arrivee-input"]').type('Paris 16ème')
    cy.get('[data-cy="calculate-distance-button"]').click()
    
    cy.get('[data-cy="distance-display"]').should('be.visible')
    cy.get('[data-cy="distance-display"]').should('contain', 'km')
    cy.get('[data-cy="temps-estime"]').should('be.visible')
  })
})

// cypress/e2e/workflow/complete-delivery.cy.js
describe('Workflow complet de livraison', () => {
  it('devrait gérer un processus complet de livraison', () => {
    // 1. Client crée une annonce
    cy.login('client@example.com', 'password123')
    cy.createAnnonce({
      titre: 'Livraison E2E complète',
      description: 'Test workflow complet',
      prix: 25.00,
      adresseDepart: 'Paris Gare du Nord',
      adresseArrivee: 'Paris Bastille'
    })
    
    // 2. Déconnexion client
    cy.get('[data-cy="user-menu"]').click()
    cy.get('[data-cy="logout-button"]').click()
    
    // 3. Livreur se connecte et candidate
    cy.login('livreur@example.com', 'password123')
    cy.visit('/annonces')
    cy.get('[data-cy="annonce-item"]').first().click()
    cy.get('[data-cy="candidater-button"]').click()
    
    cy.get('[data-cy="prix-candidature"]').type('22.00')
    cy.get('[data-cy="message-candidature"]').type('Je peux m\'en occuper rapidement')
    cy.get('[data-cy="submit-candidature"]').click()
    
    cy.get('[data-cy="success-message"]').should('contain', 'Candidature envoyée')
    
    // 4. Retour client pour accepter candidature
    cy.get('[data-cy="user-menu"]').click()
    cy.get('[data-cy="logout-button"]').click()
    
    cy.login('client@example.com', 'password123')
    cy.visit('/mes-annonces')
    cy.get('[data-cy="annonce-item"]').first().click()
    cy.get('[data-cy="candidature-item"]').first().within(() => {
      cy.get('[data-cy="accepter-candidature"]').click()
    })
    
    cy.get('[data-cy="confirm-acceptation"]').click()
    cy.get('[data-cy="success-message"]').should('contain', 'Candidature acceptée')
    
    // 5. Vérification création livraison
    cy.visit('/mes-livraisons')
    cy.get('[data-cy="livraison-item"]').should('contain', 'Livraison E2E complète')
    cy.get('[data-cy="statut-livraison"]').should('contain', 'Confirmée')
  })
})
```

---

## Couverture de Code

### Configuration Coverage

```javascript
// jest.config.js - Backend
module.exports = {
  collectCoverageFrom: [
    'src/main/java/**/*.java',
    '!src/main/java/**/config/**',
    '!src/main/java/**/dto/**',
    '!src/main/java/**/*Application.java'
  ],
  
  coverageReporters: ['text', 'lcov', 'html'],
  
  coverageThreshold: {
    global: {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80
    },
    'src/main/java/**/services/**/*.java': {
      branches: 90,
      functions: 90,
      lines: 90,
      statements: 90
    }
  }
}

// package.json - Frontend
{
  "scripts": {
    "test:coverage": "vue-cli-service test:unit --coverage",
    "test:watch": "vue-cli-service test:unit --watch"
  },
  
  "jest": {
    "coverageThreshold": {
      "global": {
        "branches": 70,
        "functions": 70,
        "lines": 70,
        "statements": 70
      },
      "src/stores/**/*.js": {
        "branches": 85,
        "functions": 85,
        "lines": 85,
        "statements": 85
      }
    }
  }
}
```

### Scripts de Test

```bash
#!/bin/bash
# scripts/run-tests.sh

echo "🧪 Exécution des tests EcoDeli"

# Backend Tests
echo "📋 Tests Backend..."
cd ecodeli-backend
./mvnw clean test
BACKEND_EXIT_CODE=$?

if [ $BACKEND_EXIT_CODE -ne 0 ]; then
    echo "❌ Tests backend échoués"
    exit 1
fi

# Frontend User Tests
echo "🎨 Tests Frontend Utilisateur..."
cd ../ecodeli-frontend-user
npm test
FRONTEND_USER_EXIT_CODE=$?

if [ $FRONTEND_USER_EXIT_CODE -ne 0 ]; then
    echo "❌ Tests frontend utilisateur échoués"
    exit 1
fi

# Frontend Admin Tests
echo "👨‍💼 Tests Frontend Admin..."
cd ../ecodeli-frontend-admin
npm test
FRONTEND_ADMIN_EXIT_CODE=$?

if [ $FRONTEND_ADMIN_EXIT_CODE -ne 0 ]; then
    echo "❌ Tests frontend admin échoués"
    exit 1
fi

# E2E Tests
echo "🎭 Tests End-to-End..."
cd ../
npm run test:e2e
E2E_EXIT_CODE=$?

if [ $E2E_EXIT_CODE -ne 0 ]; then
    echo "❌ Tests E2E échoués"
    exit 1
fi

echo "✅ Tous les tests sont passés avec succès!"
```

---

## CI/CD et Tests

### GitHub Actions

```yaml
# .github/workflows/tests.yml
name: Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  backend-tests:
    name: Tests Backend
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: ecodeli_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2
      
      - name: Run backend tests
        run: |
          cd ecodeli-backend
          ./mvnw clean test
        env:
          DATABASE_URL: jdbc:postgresql://localhost:5432/ecodeli_test
          DATABASE_USERNAME: postgres
          DATABASE_PASSWORD: postgres
      
      - name: Generate test report
        uses: dorny/test-reporter@v1
        if: success() || failure()
        with:
          name: Backend Tests
          path: ecodeli-backend/target/surefire-reports/*.xml
          reporter: java-junit
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          file: ecodeli-backend/target/site/jacoco/jacoco.xml
          flags: backend

  frontend-tests:
    name: Tests Frontend
    runs-on: ubuntu-latest
    
    strategy:
      matrix:
        node-version: [18.x, 20.x]
        frontend: [ecodeli-frontend-user, ecodeli-frontend-admin]
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Use Node.js ${{ matrix.node-version }}
        uses: actions/setup-node@v3
        with:
          node-version: ${{ matrix.node-version }}
          cache: 'npm'
          cache-dependency-path: ${{ matrix.frontend }}/package-lock.json
      
      - name: Install dependencies
        run: |
          cd ${{ matrix.frontend }}
          npm ci
      
      - name: Run tests
        run: |
          cd ${{ matrix.frontend }}
          npm run test:unit
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          file: ${{ matrix.frontend }}/coverage/lcov.info
          flags: ${{ matrix.frontend }}

  e2e-tests:
    name: Tests E2E
    runs-on: ubuntu-latest
    needs: [backend-tests, frontend-tests]
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Use Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18.x'
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Start application
        run: |
          docker-compose -f docker-compose.test.yml up -d
          ./scripts/wait-for-services.sh
      
      - name: Run Cypress tests
        uses: cypress-io/github-action@v5
        with:
          wait-on: 'http://localhost:5173'
          wait-on-timeout: 120
          record: true
        env:
          CYPRESS_RECORD_KEY: ${{ secrets.CYPRESS_RECORD_KEY }}
      
      - name: Upload screenshots
        uses: actions/upload-artifact@v3
        if: failure()
        with:
          name: cypress-screenshots
          path: cypress/screenshots
      
      - name: Stop application
        run: docker-compose -f docker-compose.test.yml down
```

### SonarQube Integration

```yaml
# .github/workflows/sonar.yml
name: SonarQube Analysis

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  sonarqube:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Cache SonarQube packages
        uses: actions/cache@v3
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
          restore-keys: ${{ runner.os }}-sonar
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2
      
      - name: Build and analyze
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: |
          cd ecodeli-backend
          ./mvnw clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
            -Dsonar.projectKey=ecodeli-backend \
            -Dsonar.host.url=https://sonarcloud.io \
            -Dsonar.organization=ecodeli
```

---

*Documentation Tests EcoDeli - Version 1.0 - Décembre 2024*