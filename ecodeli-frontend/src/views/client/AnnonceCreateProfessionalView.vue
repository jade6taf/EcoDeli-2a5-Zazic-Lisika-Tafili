<script>
import AddressAutocomplete from '@/components/AddressAutocomplete.vue'
import MapComponent from '@/components/MapComponent.vue'
import { mapsServices } from '@/services/mapsServices'

export default {
  name: 'AnnonceCreateProfessionalView',
  components: {
    AddressAutocomplete,
    MapComponent
  },
  data() {
    return {
      currentStep: 1,
      totalSteps: 5,
      annonce: {
        titre: '',
        description: '',
        prixUnitaire: null,
        adresseDepart: '',
        adresseFin: '',
        dateDebut: '',
        dateFin: '',
        typeAnnonce: 'unique',
        colis: {
          poids: null,
          longueur: null,
          largeur: null,
          hauteur: null,
          fragile: false,
          description: ''
        },
        livraisonPartielleAutorisee: false,
        entrepotIntermediaire: '',
        expediteur: {
          nom: '',
          prenom: '',
          telephone: '',
          email: ''
        },
        destinataire: {
          nom: '',
          prenom: '',
          telephone: '',
          email: ''
        }
      },
      departCoordinates: null,
      destinationCoordinates: null,

      calculatedDistance: 0,
      calculatedPrice: 0,
      isCalculatingPrice: false,

      mapMarkers: [],
      showMap: false,

      availableWarehouses: [],

      minDate: '',
      user: null,
      isSubmitting: false,
      error: null,
      success: false,

      stepValidation: {
        1: false,
        2: false,
        3: false,
        4: false,
        5: false
      },

      fieldStates: {}
    }
  },
  computed: {
    isStepValid() {
      switch(this.currentStep) {
        case 1:
          return this.annonce.titre && this.annonce.description && this.annonce.typeAnnonce;
        case 2:
          return this.annonce.colis.poids && this.annonce.colis.longueur &&
                 this.annonce.colis.largeur && this.annonce.colis.hauteur;
        case 3:
          return this.annonce.adresseDepart && this.annonce.adresseFin &&
                 this.annonce.expediteur.nom && this.annonce.expediteur.prenom &&
                 this.annonce.expediteur.telephone && this.annonce.destinataire.nom &&
                 this.annonce.destinataire.prenom && this.annonce.destinataire.telephone;
        case 4:
          return this.annonce.dateDebut && this.annonce.dateFin;
        case 5:
          return this.annonce.prixUnitaire && this.annonce.prixUnitaire > 0;
        default:
          return false;
      }
    },
    progressPercentage() {
      return ((this.currentStep - 1) / (this.totalSteps - 1)) * 100;
    },
    canCalculateDistance() {
      return this.annonce.adresseDepart && this.annonce.adresseFin;
    },
    formattedPrice() {
      if (this.calculatedPrice > 0) {
        return {
          totalPrice: this.calculatedPrice.toFixed(2),
          distance: this.calculatedDistance > 0 ? `${this.calculatedDistance.toFixed(1)} km` : '',
          pricePerKm: '0.80',
          segments: this.annonce.livraisonPartielleAutorisee ? [
            {
              number: 1,
              price: (this.calculatedPrice / 2).toFixed(2),
              description: 'Premier segment'
            },
            {
              number: 2,
              price: (this.calculatedPrice / 2).toFixed(2),
              description: 'Second segment'
            }
          ] : []
        };
      }
      return null;
    },
    stepMeta() {
      return {
        1: {
          title: 'Informations générales',
          icon: '📝',
          description: 'Titre et description de votre annonce',
          estimatedTime: '2 min'
        },
        2: {
          title: 'Détails du colis',
          icon: '📦',
          description: 'Dimensions, poids et spécificités',
          estimatedTime: '1 min'
        },
        3: {
          title: 'Adresses & contacts',
          icon: '📍',
          description: 'Points de départ et d\'arrivée',
          estimatedTime: '3 min'
        },
        4: {
          title: 'Planning',
          icon: '⏰',
          description: 'Dates et horaires de livraison',
          estimatedTime: '1 min'
        },
        5: {
          title: 'Validation & prix',
          icon: '💰',
          description: 'Vérification et confirmation',
          estimatedTime: '2 min'
        }
      };
    }
  },
  watch: {
    'annonce.livraisonPartielleAutorisee'() {
      if (this.calculatedDistance > 0) {
        this.calculateSimplePrice();
      }
    }
  },
  async mounted() {
    await this.initializeComponent();
  },
  methods: {
    async initializeComponent() {
      const now = new Date();
      this.minDate = this.formatDateForInput(now);

      const userStr = localStorage.getItem('user');
      if (userStr) {
        this.user = JSON.parse(userStr);
        this.annonce.expediteur.nom = this.user.nom || '';
        this.annonce.expediteur.prenom = this.user.prenom || '';
        this.annonce.expediteur.email = this.user.email || '';
      } else {
        this.$router.push('/login');
        return;
      }

      try {
        this.availableWarehouses = await mapsServices.getAvailableWarehouses();
      } catch (error) {
        console.error('Erreur lors du chargement des entrepôts:', error);
      }
    },

    formatDateForInput(date) {
      return date.toISOString().slice(0, 16);
    },

    async nextStep() {
      if (this.currentStep === 3 && this.canCalculateDistance && this.calculatedDistance === 0) {
        await this.calculateDistanceAndPrice();
      }

      if (this.currentStep < this.totalSteps) {
        this.stepValidation[this.currentStep] = true;
        this.currentStep++;
      }
    },

    prevStep() {
      if (this.currentStep > 1) {
        this.currentStep--;
      }
    },

    goToStep(step) {
      for (let i = 1; i < step; i++) {
        if (!this.stepValidation[i] && i !== this.currentStep) {
          return;
        }
      }
      this.currentStep = step;
    },

    onDepartAddressValidated(addressData) {
      this.departCoordinates = addressData.coordinates;
      this.updateMapMarkers();
      this.calculateDistanceAndPrice();
    },

    onDestinationAddressValidated(addressData) {
      this.destinationCoordinates = addressData.coordinates;
      this.updateMapMarkers();
      this.calculateDistanceAndPrice();
    },

    updateMapMarkers() {
      this.mapMarkers = [];

      if (this.departCoordinates) {
        this.mapMarkers.push({
          lat: this.departCoordinates.lat,
          lng: this.departCoordinates.lng,
          type: 'origin',
          title: 'Adresse de départ',
          infoWindow: `<div><strong>Départ</strong><br>${this.annonce.adresseDepart}</div>`
        });
      }

      if (this.destinationCoordinates) {
        this.mapMarkers.push({
          lat: this.destinationCoordinates.lat,
          lng: this.destinationCoordinates.lng,
          type: 'destination',
          title: 'Adresse de destination',
          infoWindow: `<div><strong>Destination</strong><br>${this.annonce.adresseFin}</div>`
        });
      }

      this.showMap = this.mapMarkers.length > 0;
    },

    async calculateDistanceAndPrice() {
      if (!this.canCalculateDistance) return;

      this.isCalculatingPrice = true;

      try {
        const result = await mapsServices.calculateDistance(
          this.departCoordinates,
          this.destinationCoordinates
        );

        if (result && result.distance) {
          this.calculatedDistance = result.distance;
          this.calculateSimplePrice();
        }
      } catch (error) {
        console.error('Erreur lors du calcul de distance:', error);
        if (this.departCoordinates && this.destinationCoordinates) {
          this.calculatedDistance = this.calculateStraightLineDistance();
          this.calculateSimplePrice();
        }
      } finally {
        this.isCalculatingPrice = false;
      }
    },

    calculateStraightLineDistance() {
      const R = 6371;
      const dLat = this.toRadians(this.destinationCoordinates.lat - this.departCoordinates.lat);
      const dLon = this.toRadians(this.destinationCoordinates.lng - this.departCoordinates.lng);
      const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(this.toRadians(this.departCoordinates.lat)) *
                Math.cos(this.toRadians(this.destinationCoordinates.lat)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
      return R * c;
    },

    toRadians(degrees) {
      return degrees * (Math.PI / 180);
    },

    calculateSimplePrice() {
      const pricePerKm = 0.80;
      this.calculatedPrice = this.calculatedDistance * pricePerKm;
      this.annonce.prixUnitaire = this.calculatedPrice;
    },

    async handleSubmit() {
      this.isSubmitting = true;
      this.error = null;
      this.success = false;

      try {
        if (!this.annonce.titre || !this.annonce.description || !this.annonce.prixUnitaire || 
            !this.annonce.adresseDepart || !this.annonce.adresseFin || !this.annonce.dateDebut || 
            !this.annonce.dateFin) {
          throw new Error('Veuillez remplir tous les champs obligatoires');
        }

        const dateDebut = new Date(this.annonce.dateDebut);
        const dateFin = new Date(this.annonce.dateFin);
        if (dateDebut >= dateFin) {
          throw new Error('La date de début doit être antérieure à la date de fin');
        }
        if (dateDebut < new Date()) {
          throw new Error('La date de début ne peut pas être dans le passé');
        }

        this.annonce.prixUnitaire = parseFloat(this.annonce.prixUnitaire);
        const token = localStorage.getItem('token');
        if (!token) {
          this.$router.push('/login');
          return;
        }

        const annonceData = {
          ...this.annonce,
          idExpediteur: this.user.idUtilisateur,
          expediteur: {
            ...this.annonce.expediteur,
            idUtilisateur: this.user.idUtilisateur,
            type: "CLIENT"
          }
        };

        if (annonceData.destinataire) {
          annonceData.destinataire = {
            ...annonceData.destinataire,
            type: "CLIENT"
          };
        }

        const response = await fetch(`/api/annonces?idExpediteur=${this.user.idUtilisateur}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(annonceData)
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData || 'Erreur lors de la création de l\'annonce');
        }

        const result = await response.json();
        this.success = true;
        setTimeout(() => {
          this.$router.push('/client/annonces');
        }, 2000);

      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
      } finally {
        this.isSubmitting = false;
      }
    },

    onRouteCalculated(routeData) {
      if (this.calculatedPrice > 0) {
        this.$refs.mapComponent.setRoutePrice(this.calculatedPrice);
      }
    }
  }
}
</script>

<template>
  <div class="professional-form-container">

    <header class="form-header">
      <h1 class="form-title">Créer une nouvelle annonce de livraison</h1>
      <p class="form-subtitle">Remplissez les informations ci-dessous pour publier votre annonce de livraison</p>
    </header>

    <div class="main-layout">
      <aside class="steps-sidebar">
        <div class="sidebar-content">
          <div class="sidebar-header">
            <h3>Progression</h3>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
            </div>
          </div>

          <nav class="steps-nav">
            <div
              v-for="step in totalSteps"
              :key="step"
              class="step-item"
              :class="{
                'step-current': currentStep === step,
                'step-completed': stepValidation[step],
                'step-available': step <= currentStep
              }"
              @click="goToStep(step)"
            >
              <div class="step-icon">
                <span v-if="stepValidation[step]" class="step-check">✓</span>
                <span v-else-if="currentStep === step" class="step-number pulse">{{ step }}</span>
                <span v-else class="step-number">{{ step }}</span>
              </div>
              <div class="step-content">
                <div class="step-title">{{ stepMeta[step]?.title }}</div>
                <div class="step-description">{{ stepMeta[step]?.description }}</div>
                <div class="step-time">{{ stepMeta[step]?.estimatedTime }}</div>
              </div>
            </div>
          </nav>
        </div>
      </aside>

      <main class="form-content">
        <div v-if="success" class="success-notification animate-fade-in">
          <div class="success-icon">✨</div>
          <div class="success-content">
            <h3>Annonce créée avec succès !</h3>
            <p>Redirection vers vos annonces...</p>
          </div>
        </div>

        <div v-else class="form-card">
          <div v-if="error" class="error-notification">
            <div class="error-icon">⚠️</div>
            <div class="error-content">
              <h4>Une erreur est survenue</h4>
              <p>{{ error }}</p>
            </div>
          </div>

          <form @submit.prevent="currentStep === totalSteps ? handleSubmit() : nextStep()" class="form">
            <!-- Étape 1: Informations générales -->
            <div v-if="currentStep === 1" class="form-step animate-fade-in">
              <div class="step-header">
                <div class="step-emoji">{{ stepMeta[1].icon }}</div>
                <div>
                  <h2>{{ stepMeta[1].title }}</h2>
                  <p>{{ stepMeta[1].description }}</p>
                </div>
              </div>

              <div class="form-grid">
                <div class="form-field">
                  <label class="label required">Titre de l'annonce</label>
                  <input
                    v-model="annonce.titre"
                    type="text"
                    class="input"
                    placeholder="Ex: Livraison de produits bio urgente"
                    required
                  >
                  <div class="field-hint">Soyez précis et attractif pour attirer les livreurs</div>
                </div>

                <div class="form-field">
                  <label class="label required">Description détaillée</label>
                  <textarea
                    v-model="annonce.description"
                    class="input textarea"
                    placeholder="Décrivez votre besoin de livraison en détail..."
                    rows="4"
                    required
                  ></textarea>
                  <div class="field-hint">Mentionnez toute information importante (fragilité, urgence, etc.)</div>
                </div>
              </div>
            </div>

            <!-- Étape 2: Détails du colis -->
            <div v-if="currentStep === 2" class="form-step animate-fade-in">
              <div class="step-header">
                <div class="step-emoji">{{ stepMeta[2].icon }}</div>
                <div>
                  <h2>{{ stepMeta[2].title }}</h2>
                  <p>{{ stepMeta[2].description }}</p>
                </div>
              </div>

              <div class="colis-card">
                <div class="form-row">
                  <div class="form-field">
                    <label class="label required">Poids (kg)</label>
                    <input
                      v-model="annonce.colis.poids"
                      type="number"
                      class="input"
                      min="0.1"
                      step="0.1"
                      placeholder="5.0"
                      required
                    >
                  </div>
                </div>

                <div class="dimensions-section">
                  <h4>Dimensions du colis (cm)</h4>
                  <div class="dimensions-grid">
                    <div class="form-field">
                      <label class="label required">Longueur</label>
                      <input
                        v-model="annonce.colis.longueur"
                        type="number"
                        class="input"
                        min="1"
                        placeholder="30"
                        required
                      >
                    </div>
                    <div class="form-field">
                      <label class="label required">Largeur</label>
                      <input
                        v-model="annonce.colis.largeur"
                        type="number"
                        class="input"
                        min="1"
                        placeholder="20"
                        required
                      >
                    </div>
                    <div class="form-field">
                      <label class="label required">Hauteur</label>
                      <input
                        v-model="annonce.colis.hauteur"
                        type="number"
                        class="input"
                        min="1"
                        placeholder="15"
                        required
                      >
                    </div>
                  </div>
                </div>

                <div class="form-field">
                  <label class="checkbox-label">
                    <input type="checkbox" v-model="annonce.colis.fragile" class="checkbox">
                    <span class="checkbox-text">Colis fragile (manipulation délicate requise)</span>
                  </label>
                </div>

                <div class="form-field">
                  <label class="label">Description du contenu</label>
                  <textarea
                    v-model="annonce.colis.description"
                    class="input textarea-sm"
                    placeholder="Décrivez brièvement le contenu du colis..."
                    rows="2"
                  ></textarea>
                </div>
              </div>
            </div>

            <!-- Étape 3: Adresses et contacts -->
            <div v-if="currentStep === 3" class="form-step animate-fade-in">
              <div class="step-header">
                <div class="step-emoji">{{ stepMeta[3].icon }}</div>
                <div>
                  <h2>{{ stepMeta[3].title }}</h2>
                  <p>{{ stepMeta[3].description }}</p>
                </div>
              </div>

              <div class="addresses-container">
                <!-- Adresse de départ -->
                <div class="address-card">
                  <div class="address-header">
                    <div class="address-icon departure">📍</div>
                    <h3>Point de départ</h3>
                  </div>

                  <div class="form-field">
                    <label class="label required">Adresse complète</label>
                    <AddressAutocomplete
                      v-model="annonce.adresseDepart"
                      placeholder="Numéro, rue, code postal, ville"
                      :required="true"
                      @address-validated="onDepartAddressValidated"
                    />
                  </div>

                  <div class="contact-section">
                    <h4>Expéditeur</h4>
                    <div class="contact-grid">
                      <div class="form-field">
                        <label class="label required">Nom</label>
                        <input v-model="annonce.expediteur.nom" type="text" class="input" required>
                      </div>
                      <div class="form-field">
                        <label class="label required">Prénom</label>
                        <input v-model="annonce.expediteur.prenom" type="text" class="input" required>
                      </div>
                      <div class="form-field">
                        <label class="label required">Téléphone</label>
                        <input v-model="annonce.expediteur.telephone" type="tel" class="input" required>
                      </div>
                      <div class="form-field">
                        <label class="label">Email</label>
                        <input v-model="annonce.expediteur.email" type="email" class="input">
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Flèche de direction -->
                <div class="direction-arrow">
                  <div class="arrow-line"></div>
                  <div class="arrow-head">→</div>
                </div>

                <!-- Adresse de destination -->
                <div class="address-card">
                  <div class="address-header">
                    <div class="address-icon destination">🎯</div>
                    <h3>Point d'arrivée</h3>
                  </div>

                  <div class="form-field">
                    <label class="label required">Adresse complète</label>
                    <AddressAutocomplete
                      v-model="annonce.adresseFin"
                      placeholder="Numéro, rue, code postal, ville"
                      :required="true"
                      @address-validated="onDestinationAddressValidated"
                    />
                  </div>

                  <div class="contact-section">
                    <h4>Destinataire</h4>
                    <div class="contact-grid">
                      <div class="form-field">
                        <label class="label required">Nom</label>
                        <input v-model="annonce.destinataire.nom" type="text" class="input" required>
                      </div>
                      <div class="form-field">
                        <label class="label required">Prénom</label>
                        <input v-model="annonce.destinataire.prenom" type="text" class="input" required>
                      </div>
                      <div class="form-field">
                        <label class="label required">Téléphone</label>
                        <input v-model="annonce.destinataire.telephone" type="tel" class="input" required>
                      </div>
                      <div class="form-field">
                        <label class="label">Email</label>
                        <input v-model="annonce.destinataire.email" type="email" class="input">
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Étape 4: Planning -->
            <div v-if="currentStep === 4" class="form-step animate-fade-in">
              <div class="step-header">
                <div class="step-emoji">{{ stepMeta[4].icon }}</div>
                <div>
                  <h2>{{ stepMeta[4].title }}</h2>
                  <p>{{ stepMeta[4].description }}</p>
                </div>
              </div>

              <div class="planning-card">
                <div class="planning-grid">
                  <div class="form-field">
                    <label class="label required">Date et heure de prise en charge</label>
                    <input
                      v-model="annonce.dateDebut"
                      type="datetime-local"
                      class="input"
                      :min="minDate"
                      required
                    >
                    <div class="field-hint">À partir de quand le colis peut être récupéré</div>
                  </div>

                  <div class="form-field">
                    <label class="label required">Date et heure limite de livraison</label>
                    <input
                      v-model="annonce.dateFin"
                      type="datetime-local"
                      class="input"
                      :min="annonce.dateDebut"
                      required
                    >
                    <div class="field-hint">Date limite avant laquelle le colis doit être livré</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Étape 5: Validation et prix -->
            <div v-if="currentStep === 5" class="form-step animate-fade-in">
              <div class="step-header">
                <div class="step-emoji">{{ stepMeta[5].icon }}</div>
                <div>
                  <h2>{{ stepMeta[5].title }}</h2>
                  <p>{{ stepMeta[5].description }}</p>
                </div>
              </div>

              <!-- Options de livraison -->
              <div class="delivery-options">
                <div class="option-card">
                  <label class="option-label">
                    <input
                      type="checkbox"
                      v-model="annonce.livraisonPartielleAutorisee"
                      class="option-checkbox"
                    >
                    <div class="option-content">
                      <div class="option-header">
                        <span class="option-title">Autoriser la livraison partielle</span>
                        <span class="option-badge">Recommandé</span>
                      </div>
                      <div class="option-description">
                        Permet à deux livreurs différents de prendre en charge votre colis via un entrepôt intermédiaire.
                        Prix identique mais plus de flexibilité.
                      </div>
                    </div>
                  </label>
                </div>
              </div>

              <!-- Récapitulatif final -->
              <div class="summary-section">
                <h3>Récapitulatif de votre annonce</h3>
                <div class="summary-grid">
                  <div class="summary-item">
                    <span class="summary-label">Titre</span>
                    <span class="summary-value">{{ annonce.titre }}</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">Départ</span>
                    <span class="summary-value">{{ annonce.adresseDepart }}</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">Arrivée</span>
                    <span class="summary-value">{{ annonce.adresseFin }}</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">Dimensions</span>
                    <span class="summary-value">{{ annonce.colis.longueur }}×{{ annonce.colis.largeur }}×{{ annonce.colis.hauteur }} cm</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">Poids</span>
                    <span class="summary-value">{{ annonce.colis.poids }} kg</span>
                  </div>
                  <div v-if="annonce.colis.fragile" class="summary-item">
                    <span class="summary-label">Spécial</span>
                    <span class="summary-value badge badge-warning">Fragile</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Navigation du formulaire -->
            <div class="form-navigation">
              <button
                v-if="currentStep > 1"
                type="button"
                @click="prevStep"
                class="btn btn-secondary"
              >
                ← Précédent
              </button>
              <div class="nav-spacer"></div>
              <button
                type="submit"
                class="btn btn-primary btn-lg"
                :disabled="!isStepValid || (currentStep === totalSteps && isSubmitting)"
              >
                <span v-if="isSubmitting" class="spinner"></span>
                <span v-else-if="currentStep < totalSteps">Suivant →</span>
                <span v-else>Créer l'annonce ✨</span>
              </button>
            </div>
          </form>
        </div>
      </main>

      <!-- Colonne droite : Récapitulatif et carte -->
      <aside class="summary-sidebar">
        <div class="sidebar-content">
          <!-- Carte interactive -->
          <div v-if="showMap" class="map-section">
            <h3>Itinéraire</h3>
            <div class="map-container">
              <MapComponent
                ref="mapComponent"
                :markers="mapMarkers"
                :show-route="mapMarkers.length > 1"
                :map-height="'250px'"
                @route-calculated="onRouteCalculated"
              />
            </div>
          </div>

          <!-- Calculateur de prix -->
          <div v-if="currentStep >= 3" class="price-section">
            <h3>Prix de livraison</h3>

            <div v-if="isCalculatingPrice" class="calculating-state">
              <div class="spinner"></div>
              <span>Calcul en cours...</span>
            </div>

            <div v-else-if="formattedPrice" class="price-result">
              <div class="price-main">
                <div class="price-amount">{{ formattedPrice.totalPrice }} €</div>
                <div class="price-details">
                  {{ formattedPrice.distance }} • {{ formattedPrice.pricePerKm }}€/km
                </div>
              </div>

              <div v-if="annonce.livraisonPartielleAutorisee && formattedPrice.segments.length" class="segments-preview">
                <div class="segment-item" v-for="segment in formattedPrice.segments" :key="segment.number">
                  <span>Segment {{ segment.number }}</span>
                  <span>{{ segment.price }} €</span>
                </div>
              </div>
            </div>

            <div v-else class="price-placeholder">
              <div class="placeholder-icon">📊</div>
              <span>Prix calculé automatiquement</span>
            </div>
          </div>

          <!-- Aide contextuelle -->
          <div class="help-section">
            <h3>💡 Aide</h3>
            <div class="help-content">
              <div v-if="currentStep === 1" class="help-tip">
                <strong>Conseil :</strong> Un titre clair et une description détaillée attirent plus de livreurs qualifiés.
              </div>
              <div v-if="currentStep === 2" class="help-tip">
                <strong>Précision :</strong> Les dimensions exactes permettent au livreur de mieux s'organiser.
              </div>
              <div v-if="currentStep === 3" class="help-tip">
                <strong>Important :</strong> Vérifiez que les adresses sont complètes et correctes.
              </div>
              <div v-if="currentStep === 4" class="help-tip">
                <strong>Flexibilité :</strong> Plus votre créneau est large, plus vous avez de chance de trouver un livreur.
              </div>
              <div v-if="currentStep === 5" class="help-tip">
                <strong>Final :</strong> Vérifiez tous les détails avant de publier votre annonce.
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
@import '@/assets/css/design-system.css';

.professional-form-container {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--color-primary-50) 0%, var(--color-secondary-50) 100%);
  font-family: var(--font-family-primary);
}

.form-header {
  background: white;
  border-bottom: 1px solid var(--color-primary-200);
  padding: var(--space-6) var(--space-8);
  box-shadow: var(--shadow-sm);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
  font-size: var(--text-sm);
}

.breadcrumb-item {
  color: var(--color-primary-500);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.breadcrumb-item:hover {
  color: var(--color-secondary-600);
}

.breadcrumb-separator {
  color: var(--color-primary-400);
}

.breadcrumb-current {
  color: var(--color-primary-800);
  font-weight: var(--font-medium);
}

.form-title {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--color-primary-900);
  margin-bottom: var(--space-2);
}

.form-subtitle {
  font-size: var(--text-lg);
  color: var(--color-primary-600);
  font-weight: var(--font-normal);
}

/* ================================
   LAYOUT 3 COLONNES
   ================================ */

.main-layout {
  display: grid;
  grid-template-columns: 300px 1fr 350px;
  gap: var(--space-6);
  max-width: 1600px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-8);
  min-height: calc(100vh - 200px);
}

/* ================================
   SIDEBAR GAUCHE - NAVIGATION
   ================================ */

.steps-sidebar {
  background: white;
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-lg);
  height: fit-content;
  position: sticky;
  top: var(--space-6);
}

.sidebar-content {
  padding: var(--space-6);
}

.sidebar-header {
  margin-bottom: var(--space-6);
}

.sidebar-header h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin-bottom: var(--space-3);
}

.progress-bar {
  width: 100%;
  height: 6px;
  background-color: var(--color-primary-200);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-secondary-500), var(--color-accent-500));
  border-radius: var(--radius-full);
  transition: width var(--transition-slow);
}

.steps-nav {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 2px solid transparent;
}

.step-item:hover {
  background-color: var(--color-primary-50);
}

.step-item.step-current {
  background-color: var(--color-secondary-50);
  border-color: var(--color-secondary-200);
}

.step-item.step-completed {
  background-color: var(--color-success-50);
  border-color: var(--color-success-200);
}

.step-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: var(--font-bold);
  font-size: var(--text-sm);
  flex-shrink: 0;
  transition: all var(--transition-fast);
}

.step-item.step-current .step-icon {
  background-color: var(--color-secondary-500);
  color: white;
}

.step-item.step-completed .step-icon {
  background-color: var(--color-success-500);
  color: white;
}

.step-item:not(.step-current):not(.step-completed) .step-icon {
  background-color: var(--color-primary-200);
  color: var(--color-primary-600);
}

.step-check {
  font-size: var(--text-sm);
}

.step-number.pulse {
  animation: pulse 2s infinite;
}

.step-content {
  flex: 1;
  min-width: 0;
}

.step-title {
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin-bottom: var(--space-1);
  font-size: var(--text-sm);
}

.step-description {
  font-size: var(--text-xs);
  color: var(--color-primary-500);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-1);
}

.step-time {
  font-size: var(--text-xs);
  color: var(--color-accent-600);
  font-weight: var(--font-medium);
}

/* ================================
   CONTENU CENTRAL - FORMULAIRE
   ================================ */

.form-content {
  display: flex;
  flex-direction: column;
}

.form-card {
  background: white;
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  flex: 1;
}

.form {
  padding: var(--space-8);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.form-step {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.step-header {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  margin-bottom: var(--space-8);
  padding-bottom: var(--space-6);
  border-bottom: 1px solid var(--color-primary-100);
}

.step-emoji {
  font-size: var(--text-4xl);
  line-height: 1;
}

.step-header h2 {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-primary-900);
  margin-bottom: var(--space-2);
}

.step-header p {
  font-size: var(--text-base);
  color: var(--color-primary-600);
}

/* ================================
   CHAMPS DE FORMULAIRE
   ================================ */

.form-grid {
  display: grid;
  gap: var(--space-6);
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-4);
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.field-hint {
  font-size: var(--text-xs);
  color: var(--color-primary-500);
  font-style: italic;
}

.textarea {
  resize: vertical;
  min-height: 120px;
}

.textarea-sm {
  min-height: 80px;
}

/* ================================
   CARTES SPÉCIALISÉES
   ================================ */

.colis-card {
  background: var(--color-primary-50);
  border: 2px solid var(--color-primary-100);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.dimensions-section h4 {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin-bottom: var(--space-4);
}

.dimensions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  cursor: pointer;
  padding: var(--space-3);
  border-radius: var(--radius-lg);
  transition: background-color var(--transition-fast);
}

.checkbox-label:hover {
  background-color: var(--color-primary-100);
}

.checkbox {
  width: 18px;
  height: 18px;
  accent-color: var(--color-secondary-500);
}

.checkbox-text {
  font-size: var(--text-sm);
  color: var(--color-primary-700);
  font-weight: var(--font-medium);
}

/* ================================
   ADRESSES
   ================================ */

.addresses-container {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: var(--space-6);
  align-items: start;
}

.address-card {
  background: var(--color-primary-50);
  border: 2px solid var(--color-primary-100);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  transition: all var(--transition-fast);
}

.address-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.address-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-6);
}

.address-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-lg);
}

.address-icon.departure {
  background-color: var(--color-secondary-100);
}

.address-icon.destination {
  background-color: var(--color-accent-100);
}

.address-header h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
}

.direction-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-4) 0;
}

.arrow-line {
  width: 2px;
  height: 60px;
  background: linear-gradient(to bottom, var(--color-secondary-300), var(--color-accent-300));
  margin-bottom: var(--space-2);
}

.arrow-head {
  font-size: var(--text-2xl);
  color: var(--color-primary-400);
  transform: rotate(90deg);
}

.contact-section h4 {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin: var(--space-6) 0 var(--space-4) 0;
}

.contact-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

/* ================================
   PLANNING
   ================================ */

.planning-card {
  background: var(--color-primary-50);
  border: 2px solid var(--color-primary-100);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
}

.planning-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-6);
}

/* ================================
   OPTIONS DE LIVRAISON
   ================================ */

.delivery-options {
  margin-bottom: var(--space-8);
}

.option-card {
  border: 2px solid var(--color-primary-200);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all var(--transition-fast);
}

.option-card:hover {
  border-color: var(--color-secondary-300);
  box-shadow: var(--shadow-md);
}

.option-label {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-6);
  cursor: pointer;
  width: 100%;
}

.option-checkbox {
  width: 20px;
  height: 20px;
  margin-top: var(--space-1);
  accent-color: var(--color-secondary-500);
}

.option-content {
  flex: 1;
}

.option-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.option-title {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
}

.option-badge {
  padding: var(--space-1) var(--space-3);
  background-color: var(--color-accent-100);
  color: var(--color-accent-800);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
}

.option-description {
  font-size: var(--text-sm);
  color: var(--color-primary-600);
  line-height: var(--leading-relaxed);
}

/* ================================
   RÉCAPITULATIF
   ================================ */

.summary-section {
  background: var(--color-primary-50);
  border: 2px solid var(--color-primary-100);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
}

.summary-section h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin-bottom: var(--space-4);
}

.summary-grid {
  display: grid;
  gap: var(--space-3);
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3);
  background: white;
  border-radius: var(--radius-lg);
}

.summary-label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-primary-600);
}

.summary-value {
  font-size: var(--text-sm);
  color: var(--color-primary-800);
  text-align: right;
  max-width: 60%;
  word-break: break-word;
}

/* ================================
   NAVIGATION DU FORMULAIRE
   ================================ */

.form-navigation {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-top: auto;
  padding-top: var(--space-8);
  border-top: 1px solid var(--color-primary-100);
}

.nav-spacer {
  flex: 1;
}

/* ================================
   SIDEBAR DROITE - RÉCAPITULATIF
   ================================ */

.summary-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  position: sticky;
  top: var(--space-6);
  height: fit-content;
}

.summary-sidebar .sidebar-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.map-section,
.price-section,
.help-section {
  background: white;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: var(--space-6);
}

.map-section h3,
.price-section h3,
.help-section h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin-bottom: var(--space-4);
}

.map-container {
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

/* ================================
   PRIX
   ================================ */

.calculating-state {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--color-primary-50);
  border-radius: var(--radius-lg);
  color: var(--color-primary-600);
}

.price-result {
  background: linear-gradient(135deg, var(--color-success-50), var(--color-accent-50));
  border: 2px solid var(--color-success-200);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.price-main {
  text-align: center;
  margin-bottom: var(--space-4);
}

.price-amount {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--color-success-700);
  margin-bottom: var(--space-1);
}

.price-details {
  font-size: var(--text-sm);
  color: var(--color-primary-600);
}

.segments-preview {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-success-200);
}

.segment-item {
  display: flex;
  justify-content: space-between;
  font-size: var(--text-sm);
  color: var(--color-primary-700);
}

.price-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-8);
  color: var(--color-primary-500);
  text-align: center;
}

.placeholder-icon {
  font-size: var(--text-3xl);
  opacity: 0.6;
}

/* ================================
   AIDE
   ================================ */

.help-content {
  font-size: var(--text-sm);
  line-height: var(--leading-relaxed);
}

.help-tip {
  padding: var(--space-4);
  background: var(--color-accent-50);
  border-left: 4px solid var(--color-accent-400);
  border-radius: var(--radius-lg);
  color: var(--color-primary-700);
}

.help-tip strong {
  color: var(--color-accent-700);
}

/* ================================
   NOTIFICATIONS
   ================================ */

.success-notification {
  background: linear-gradient(135deg, var(--color-success-50), var(--color-success-100));
  border: 2px solid var(--color-success-200);
  border-radius: var(--radius-2xl);
  padding: var(--space-8);
  display: flex;
  align-items: center;
  gap: var(--space-6);
  margin-bottom: var(--space-6);
}

.success-icon {
  font-size: var(--text-4xl);
}

.success-content h3 {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-success-800);
  margin-bottom: var(--space-2);
}

.success-content p {
  color: var(--color-success-700);
}

.error-notification {
  background: var(--color-error-50);
  border: 2px solid var(--color-error-200);
  border-radius: var(--radius-xl);
  padding: var(--space-4);
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  margin-bottom: var(--space-6);
}

.error-icon {
  font-size: var(--text-xl);
}

.error-content h4 {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--color-error-800);
  margin-bottom: var(--space-1);
}

.error-content p {
  font-size: var(--text-sm);
  color: var(--color-error-700);
}

/* ================================
   RESPONSIVE DESIGN
   ================================ */

@media (max-width: 1400px) {
  .main-layout {
    grid-template-columns: 280px 1fr 320px;
    gap: var(--space-4);
    padding: var(--space-4) var(--space-6);
  }
}

@media (max-width: 1200px) {
  .main-layout {
    grid-template-columns: 1fr;
    gap: var(--space-4);
  }
  
  .steps-sidebar,
  .summary-sidebar {
    position: static;
  }
  
  .addresses-container {
    grid-template-columns: 1fr;
    gap: var(--space-4);
  }
  
  .direction-arrow {
    display: none;
  }
  
  .contact-grid,
  .planning-grid,
  .dimensions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
