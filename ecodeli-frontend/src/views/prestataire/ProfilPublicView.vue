<script>
import apiServices from '@/services/apiServices';

export default {
  name: 'ProfilPublicView',
  data() {
    return {
      servicesTypes: [
        {
          code: 'TRANSPORT_LIVRAISON',
          libelle: '🚚 Transport & Livraison',
          description: 'Transport de personnes, livraison de colis, déménagement, courses urgentes',
          icon: '🚚'
        },
        {
          code: 'SERVICES_DOMICILE',
          libelle: '🏠 Services à domicile',
          description: 'Ménage, garde d\'enfants/animaux, jardinage, assistance aux personnes âgées',
          icon: '🏠'
        },
        {
          code: 'TRAVAUX_REPARATIONS',
          libelle: '🔧 Travaux & Réparations',
          description: 'Bricolage, plomberie, électricité, rénovation, assemblage de meubles',
          icon: '🔧'
        },
        {
          code: 'COURSES_ACHATS',
          libelle: '🛒 Courses & Achats',
          description: 'Courses alimentaires, achats divers, recherche de produits spécifiques',
          icon: '🛒'
        },
        {
          code: 'SERVICES_PERSONNELS',
          libelle: '👥 Services personnels',
          description: 'Assistance administrative, organisation d\'événements, secrétariat, conciergerie',
          icon: '👥'
        },
        {
          code: 'EDUCATION_FORMATION',
          libelle: '🎓 Éducation & Formation',
          description: 'Cours particuliers, formation professionnelle, coaching, soutien scolaire',
          icon: '🎓'
        }
      ],
      prestataire: {
        nomEntreprise: '',
        domaineExpertise: '',
        zoneIntervention: '',
        tarifHoraire: 0,
        description: '',
        disponible: false,
        imageUrl: ''
      },
      saving: false,
      loading: false,
      uploadingPhoto: false,
      updatingDisponibilite: false,
      message: '',
      messageType: '',
      editing: false,
      selectedFile: null,
      previewImage: null
    }
  },
  computed: {
    selectedService() {
      return this.servicesTypes.find(s => s.code === this.prestataire.domaineExpertise);
    },
    displayImageUrl() {
      return this.prestataire.imageUrl;
    }
  },
  methods: {
    async loadServicesTypes() {
      try {
        const response = await apiServices.get('prestataires/services-types');
        const data = await response.json();
        this.servicesTypes = data.map(serviceBackend => {
          const serviceFrontend = this.servicesTypes.find(s => s.code === serviceBackend.code);
          return {
            ...serviceBackend,
            icon: serviceFrontend?.icon || '📋',
            description: serviceFrontend?.description || serviceBackend.libelle
          };
        });
      } catch (error) {
        console.error('Erreur lors du chargement des types de services:', error);
      }
    },
    async loadPrestataireData() {
      this.loading = true;
      try {
        const response = await apiServices.get('prestataires/profile');
        const data = await response.json();
        this.prestataire = {
          ...data,
          disponible: data.disponible || false,
          tarifHoraire: data.tarifHoraire || 0
        };
      } catch (error) {
        console.error('Erreur lors du chargement du profil prestataire:', error);
        this.showMessage('Erreur lors du chargement du profil', 'error');
      } finally {
        this.loading = false;
      }
    },
    selectService(serviceCode) {
      this.prestataire.domaineExpertise = serviceCode;
    },
    toggleEdit() {
      this.editing = !this.editing;
      this.message = '';
    },
    async saveProfile() {
      this.saving = true;
      this.message = '';
      try {
        const dataToSend = {
          ...this.prestataire,
          type: "PRESTATAIRE"
        };

        const response = await apiServices.put('prestataires/profile', dataToSend);
        if (response.ok) {
          const updatedPrestataire = await response.json();
          this.prestataire = updatedPrestataire;
          this.showMessage('Profil mis à jour avec succès', 'success');
          this.editing = false;
        } else {
          const error = await response.json();
          this.showMessage(error.message || 'Une erreur est survenue lors de la mise à jour', 'error');
        }
      } catch (error) {
        this.showMessage('Une erreur est survenue lors de la communication avec le serveur', 'error');
      } finally {
        this.saving = false;
      }
    },
    cancelEdit() {
      this.loadPrestataireData();
      this.editing = false;
      this.message = '';
    },
    showMessage(text, type) {
      this.message = text;
      this.messageType = type;
      setTimeout(() => {
        this.message = '';
      }, 5000);
    },
    async handleFileSelect(event) {
      const file = event.target.files[0];
      if (!file) return;

      if (!file.type.startsWith('image/')) {
        this.showMessage('Veuillez sélectionner une image', 'error');
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        this.showMessage('L\'image ne doit pas dépasser 5MB', 'error');
        return;
      }

      this.selectedFile = file;

      const reader = new FileReader();
      reader.onload = (e) => {
        this.previewImage = e.target.result;
      };
      reader.readAsDataURL(file);
    },
    async uploadPhoto() {
      if (!this.selectedFile) return;

      this.uploadingPhoto = true;
      try {
        const formData = new FormData();
        formData.append('photo', this.selectedFile);

        const response = await apiServices.postFormData('prestataires/upload-photo', formData);

        if (response.ok) {
          const data = await response.json();
          this.prestataire.imageUrl = data.imageUrl;
          this.showMessage(data.message, 'success');
          this.selectedFile = null;
          this.previewImage = null;
        } else {
          const error = await response.json();
          this.showMessage(error.message || 'Erreur lors de l\'upload', 'error');
        }
      } catch (error) {
        this.showMessage('Erreur lors de l\'upload de la photo', 'error');
      } finally {
        this.uploadingPhoto = false;
      }
    },
    async toggleDisponibilite() {
      const newValue = !this.prestataire.disponible;
      this.updatingDisponibilite = true;

      try {
        const response = await apiServices.patch('prestataires/disponibilite', {
          disponible: newValue
        });

        if (response.ok) {
          const data = await response.json();
          this.prestataire.disponible = data.disponible;
          this.showMessage(data.message, 'success');
        } else {
          const error = await response.json();
          this.showMessage(error.message || 'Erreur lors de la mise à jour', 'error');
        }
      } catch (error) {
        this.showMessage('Erreur lors de la mise à jour de la disponibilité', 'error');
      } finally {
        this.updatingDisponibilite = false;
      }
    },
    clearFileSelection() {
      this.selectedFile = null;
      this.previewImage = null;
      const fileInput = this.$refs.fileInput;
      if (fileInput) {
        fileInput.value = '';
      }
    }
  },
  async created() {
    await Promise.all([
      this.loadPrestataireData(),
      this.loadServicesTypes()
    ]);
  }
}
</script>

<template>
  <div class="profil-container">
    <div v-if="message"
         :class="['message-banner', `message-${messageType}`]"
         class="animate-fade-in">
      <div class="message-content">
        <i :class="messageType === 'success' ? 'fas fa-check-circle' : 'fas fa-exclamation-circle'"></i>
        <span>{{ message }}</span>
      </div>
      <button @click="message = ''" class="message-close">
        <i class="fas fa-times"></i>
      </button>
    </div>

    <!-- Header du profil -->
    <div class="profil-header card">
      <div class="header-content">

        <div class="profil-info">
          <h1 class="entreprise-name">{{ prestataire.nomEntreprise || 'Nom de l\'entreprise' }}</h1>
          <p class="profil-type">Prestataire de services</p>
          <div class="profil-meta">
            <div v-if="selectedService" class="service-badge">
              <!-- <span class="service-icon">{{ selectedService.icon }}</span> -->
              <span class="service-label">{{ selectedService.libelle }}</span>
            </div>
            <div v-if="prestataire.tarifHoraire" class="tarif-info">
              <i class="fas fa-euro-sign"></i>
              <span>{{ prestataire.tarifHoraire }}€/h</span>
            </div>
          </div>
        </div>

        <div class="header-actions">
          <div v-if="!editing" class="disponibilite-display">
            <div 
              :class="['disponibilite-status', 'clickable', prestataire.disponible ? 'available' : 'unavailable']"
              @click="toggleDisponibilite"
              :disabled="updatingDisponibilite">
              <i v-if="updatingDisponibilite" class="fas fa-spinner fa-spin"></i>
              <i v-else :class="prestataire.disponible ? 'fas fa-circle' : 'fas fa-circle'"></i>
              <span>{{ prestataire.disponible ? 'Disponible' : 'Non disponible' }}</span>
            </div>
            <small class="disponibilite-help">Cliquez pour changer</small>
          </div>

          <button v-if="!editing" @click="toggleEdit" class="btn btn-primary">
            <i class="fas fa-edit"></i>
            Modifier le profil
          </button>
          <div v-else class="edit-actions">
            <button @click="saveProfile" :disabled="saving" class="btn btn-success">
              <i v-if="saving" class="fas fa-spinner fa-spin"></i>
              <i v-else class="fas fa-save"></i>
              {{ saving ? 'Enregistrement...' : 'Enregistrer' }}
            </button>
            <button @click="cancelEdit" class="btn btn-secondary">
              <i class="fas fa-times"></i>
              Annuler
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
      <div class="loading-content">
        <div class="spinner"></div>
        <p>Chargement du profil...</p>
      </div>
    </div>

    <!-- Contenu principal -->
    <div v-else class="profil-content">
      <!-- Mode édition -->
      <div v-if="editing" class="edit-section">
        <!-- Informations de base -->
        <div class="form-section card">
          <h3 class="section-title">
            <i class="fas fa-building"></i>
            Informations de base
          </h3>
          <div class="form-group">
            <label for="nomEntreprise" class="label required">Nom de l'entreprise</label>
            <input
              type="text"
              id="nomEntreprise"
              v-model="prestataire.nomEntreprise"
              class="input"
              placeholder="Nom de votre entreprise">
          </div>
        </div>

        <!-- Sélection de catégorie -->
        <div class="form-section card">
          <h3 class="section-title">
            <i class="fas fa-tags"></i>
            Catégorie de service
          </h3>
          <p class="section-description">Choisissez la catégorie qui correspond le mieux à vos services</p>

          <div class="services-grid">
            <div
              v-for="service in servicesTypes"
              :key="service.code"
              :class="['service-card', { 'selected': prestataire.domaineExpertise === service.code }]"
              @click="selectService(service.code)">
              <div class="service-icon-large">{{ service.icon }}</div>
              <div class="service-content">
                <h4 class="service-name">{{ service.libelle.replace(/^🔧|🚚|🏠|🛒|👥|🎓\s/, '') }}</h4>
                <p class="service-description">{{ service.description }}</p>
              </div>
              <div v-if="prestataire.domaineExpertise === service.code" class="service-selected">
                <i class="fas fa-check-circle"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- Détails du service -->
        <div class="form-section card">
          <h3 class="section-title">
            <i class="fas fa-info-circle"></i>
            Détails de vos services
          </h3>

          <div class="form-row">
            <div class="form-group">
              <label for="zoneIntervention" class="label">Zone d'intervention</label>
              <input
                type="text"
                id="zoneIntervention"
                v-model="prestataire.zoneIntervention"
                class="input"
                placeholder="Ex: Paris, Île-de-France, France entière">
            </div>

            <div class="form-group">
              <label for="tarifHoraire" class="label">Tarif horaire (€)</label>
              <input
                type="number"
                id="tarifHoraire"
                v-model="prestataire.tarifHoraire"
                class="input"
                min="0"
                step="0.01"
                placeholder="25.00">
            </div>
          </div>

          <div class="form-group">
            <label for="description" class="label">Description de vos services</label>
            <textarea
              id="description"
              v-model="prestataire.description"
              class="input textarea"
              rows="5"
              placeholder="Décrivez vos services, votre expérience, vos qualifications..."></textarea>
          </div>
        </div>

      </div>

      <!-- Mode visualisation -->
      <div v-else class="preview-section">
        <!-- Aperçu du profil -->
        <div class="preview-card card">
          <div class="preview-header">
            <h3 class="preview-title">
              <i class="fas fa-eye"></i>
              Aperçu de votre profil public
            </h3>
            <p class="preview-subtitle">Voici comment les clients voient votre profil</p>
          </div>

          <div class="preview-content">
            <!-- Catégorie sélectionnée -->
            <div v-if="selectedService" class="service-preview">
              <div class="service-header">
                <div class="service-info">
                  <h4 class="service-title">{{ selectedService.libelle }}</h4>
                  <p class="service-desc">{{ selectedService.description }}</p>
                </div>
              </div>
            </div>

            <!-- Informations principales -->
            <div class="info-grid">
              <div v-if="prestataire.zoneIntervention" class="info-item">
                <i class="fas fa-map-marker-alt"></i>
                <div>
                  <span class="info-label">Zone d'intervention</span>
                  <span class="info-value">{{ prestataire.zoneIntervention }}</span>
                </div>
              </div>
              <div v-if="prestataire.tarifHoraire" class="info-item">
                <i class="fas fa-euro-sign"></i>
                <div>
                  <span class="info-label">Tarif horaire</span>
                  <span class="info-value">{{ prestataire.tarifHoraire }}€/h</span>
                </div>
              </div>
            </div>

            <!-- Description -->
            <div v-if="prestataire.description" class="description-preview">
              <h4 class="description-title">À propos de mes services</h4>
              <p class="description-text">{{ prestataire.description }}</p>
            </div>

            <!-- Message si profil incomplet -->
            <div v-if="!selectedService || !prestataire.description" class="incomplete-notice">
              <i class="fas fa-info-circle"></i>
              <div>
                <h4>Profil incomplet</h4>
                <p>Complétez votre profil pour attirer plus de clients :</p>
                <ul>
                  <li v-if="!selectedService">Sélectionnez une catégorie de service</li>
                  <li v-if="!prestataire.description">Ajoutez une description de vos services</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profil-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: var(--space-6);
  min-height: 100vh;
}

/* Messages */
.message-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-6);
  box-shadow: var(--shadow-md);
}

.message-success {
  background-color: var(--color-success-50);
  border: 1px solid var(--color-success-200);
  color: var(--color-success-800);
}

.message-error {
  background-color: var(--color-error-50);
  border: 1px solid var(--color-error-200);
  color: var(--color-error-800);
}

.message-content {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.message-close {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-base);
  opacity: 0.7;
  transition: opacity var(--transition-fast);
}

.message-close:hover {
  opacity: 1;
}

/* Header du profil */
.profil-header {
  margin-bottom: var(--space-8);
}

.header-content {
  display: flex;
  align-items: center;
  gap: var(--space-6);
  padding: var(--space-8);
}

.profil-avatar {
  position: relative;
  flex-shrink: 0;
}

.avatar-image {
  width: 120px;
  height: 120px;
  border-radius: var(--radius-2xl);
  object-fit: cover;
  border: 4px solid var(--color-primary-100);
  box-shadow: var(--shadow-md);
}

.status-indicator {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: var(--text-sm);
  border: 3px solid white;
  box-shadow: var(--shadow-md);
}

.status-indicator.available {
  background-color: var(--color-success-500);
}

.status-indicator.unavailable {
  background-color: var(--color-error-500);
}

.profil-info {
  flex: 1;
}

.entreprise-name {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--color-primary-800);
  margin: 0 0 var(--space-2) 0;
}

.profil-type {
  color: var(--color-primary-600);
  font-size: var(--text-lg);
  margin: 0 0 var(--space-4) 0;
}

.profil-meta {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.service-badge {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  background-color: var(--color-secondary-100);
  color: var(--color-secondary-800);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  font-weight: var(--font-medium);
}

.service-icon {
  font-size: var(--text-lg);
}

.tarif-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--color-primary-600);
  font-weight: var(--font-medium);
}

.header-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--space-4);
}

.disponibilite-display {
  text-align: right;
}

.disponibilite-status {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: var(--font-medium);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
}

.disponibilite-status.available {
  background-color: var(--color-success-100);
  color: var(--color-success-800);
}

.disponibilite-status.unavailable {
  background-color: var(--color-error-100);
  color: var(--color-error-800);
}

.disponibilite-status.clickable {
  cursor: pointer;
  transition: all var(--transition-fast);
  user-select: none;
}

.disponibilite-status.clickable:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.disponibilite-status.clickable:active {
  transform: translateY(0);
}

.disponibilite-status.clickable[disabled] {
  cursor: not-allowed;
  opacity: 0.7;
}

.disponibilite-help {
  color: var(--color-primary-500);
  font-size: var(--text-xs);
  margin-top: var(--space-1);
  font-style: italic;
}

.edit-actions {
  display: flex;
  gap: var(--space-3);
}

/* Loading */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-content {
  text-align: center;
  color: var(--color-primary-600);
}

.loading-content p {
  margin-top: var(--space-4);
  font-size: var(--text-lg);
}

/* Sections de formulaire */
.form-section {
  margin-bottom: var(--space-8);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin: 0 0 var(--space-4) 0;
  padding-bottom: var(--space-3);
  border-bottom: 2px solid var(--color-primary-100);
}

.section-description {
  color: var(--color-primary-600);
  margin-bottom: var(--space-6);
}

.form-group {
  margin-bottom: var(--space-6);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-6);
}

.label.required::after {
  content: " *";
  color: var(--color-error-500);
}

.form-help {
  display: block;
  margin-top: var(--space-2);
  color: var(--color-primary-500);
  font-size: var(--text-sm);
}

.textarea {
  resize: vertical;
  min-height: 120px;
}

/* Grille des services */
.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--space-4);
}

.service-card {
  position: relative;
  padding: var(--space-6);
  border: 2px solid var(--color-primary-200);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-fast);
  background-color: white;
}

.service-card:hover {
  border-color: var(--color-secondary-300);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.service-card.selected {
  border-color: var(--color-secondary-500);
  background-color: var(--color-secondary-50);
  box-shadow: var(--shadow-lg);
}

.service-icon-large {
  font-size: 3rem;
  margin-bottom: var(--space-4);
  text-align: center;
}

.service-content {
  text-align: center;
}

.service-name {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin: 0 0 var(--space-2) 0;
}

.service-description {
  color: var(--color-primary-600);
  font-size: var(--text-sm);
  line-height: var(--leading-relaxed);
  margin: 0;
}

.service-selected {
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
  color: var(--color-secondary-500);
  font-size: var(--text-xl);
}

/* Toggle de disponibilité */
.disponibilite-toggle {
  background-color: var(--color-primary-50);
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-primary-200);
}

.toggle-container {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  cursor: pointer;
}

.toggle-container input {
  display: none;
}

.toggle-switch {
  position: relative;
  width: 60px;
  height: 32px;
  background-color: var(--color-primary-300);
  border-radius: var(--radius-full);
  transition: background-color var(--transition-fast);
  flex-shrink: 0;
}

.toggle-slider {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 28px;
  height: 28px;
  background-color: white;
  border-radius: 50%;
  transition: transform var(--transition-fast);
  box-shadow: var(--shadow-sm);
}

.toggle-container input:checked + .toggle-switch {
  background-color: var(--color-success-500);
}

.toggle-container input:checked + .toggle-switch .toggle-slider {
  transform: translateX(28px);
}

.toggle-content {
  flex: 1;
}

.toggle-label {
  display: block;
  font-weight: var(--font-medium);
  color: var(--color-primary-800);
  margin-bottom: var(--space-1);
}

.toggle-description {
  display: block;
  font-size: var(--text-sm);
  color: var(--color-primary-600);
}

/* Aperçu du profil */
.preview-card {
  background: linear-gradient(135deg, var(--color-primary-50) 0%, white 100%);
  border: 1px solid var(--color-primary-200);
}

.preview-header {
  padding: var(--space-6) var(--space-6) var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--color-primary-100);
}

.preview-title {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin: 0 0 var(--space-2) 0;
}

.preview-subtitle {
  color: var(--color-primary-600);
  margin: 0;
}

.preview-content {
  padding: var(--space-6);
}

.service-preview {
  margin-bottom: var(--space-8);
}

.service-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6);
  background-color: white;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-primary-100);
}

.service-icon-display {
  font-size: 3rem;
  flex-shrink: 0;
}

.service-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin: 0 0 var(--space-2) 0;
}

.service-desc {
  color: var(--color-primary-600);
  margin: 0;
  line-height: var(--leading-relaxed);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--space-4);
  margin-bottom: var(--space-8);
}

.info-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background-color: white;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-primary-100);
}

.info-item i {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-secondary-100);
  color: var(--color-secondary-600);
  border-radius: 50%;
  flex-shrink: 0;
}

.info-label {
  display: block;
  font-size: var(--text-sm);
  color: var(--color-primary-500);
  margin-bottom: var(--space-1);
}

.info-value {
  display: block;
  font-weight: var(--font-medium);
  color: var(--color-primary-800);
}

.description-preview {
  background-color: white;
  padding: var(--space-6);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-primary-100);
}

.description-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin: 0 0 var(--space-4) 0;
}

.description-text {
  color: var(--color-primary-700);
  line-height: var(--leading-relaxed);
  margin: 0;
}

.incomplete-notice {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-6);
  background-color: var(--color-warning-50);
  border: 1px solid var(--color-warning-200);
  border-radius: var(--radius-xl);
  color: var(--color-warning-800);
}

.incomplete-notice i {
  font-size: var(--text-xl);
  color: var(--color-warning-600);
  flex-shrink: 0;
  margin-top: var(--space-1);
}

.incomplete-notice h4 {
  margin: 0 0 var(--space-2) 0;
  color: var(--color-warning-800);
}

.incomplete-notice p {
  margin: 0 0 var(--space-3) 0;
}

.incomplete-notice ul {
  margin: 0;
  padding-left: var(--space-5);
}

.incomplete-notice li {
  margin-bottom: var(--space-1);
}

/* Upload de photo */
.current-photo-preview {
  display: flex;
  justify-content: center;
  margin-bottom: var(--space-6);
}

.current-photo {
  width: 150px;
  height: 150px;
  border-radius: var(--radius-2xl);
  object-fit: cover;
  border: 4px solid var(--color-primary-100);
  box-shadow: var(--shadow-md);
}

.upload-section {
  margin-top: var(--space-4);
}

.upload-area {
  border: 2px dashed var(--color-primary-300);
  border-radius: var(--radius-lg);
  padding: var(--space-8);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  background-color: var(--color-primary-50);
  margin-bottom: var(--space-4);
}

.upload-area:hover {
  border-color: var(--color-secondary-400);
  background-color: var(--color-secondary-50);
}

.upload-area.dragover {
  border-color: var(--color-secondary-500);
  background-color: var(--color-secondary-100);
  transform: scale(1.02);
}

.upload-icon {
  font-size: 3rem;
  color: var(--color-primary-400);
  margin-bottom: var(--space-4);
}

.upload-text {
  margin: 0 0 var(--space-2) 0;
  color: var(--color-primary-700);
  font-size: var(--text-base);
}

.upload-text strong {
  color: var(--color-secondary-600);
}

.upload-help {
  color: var(--color-primary-500);
  font-size: var(--text-sm);
}

.file-actions {
  background-color: var(--color-primary-50);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-primary-200);
  margin-bottom: var(--space-4);
}

.selected-file {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  padding: var(--space-3);
  background-color: white;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-primary-200);
}

.selected-file i {
  color: var(--color-secondary-500);
  font-size: var(--text-lg);
}

.selected-file span {
  flex: 1;
  color: var(--color-primary-700);
  font-weight: var(--font-medium);
}

.btn-clear {
  background: none;
  border: none;
  color: var(--color-error-500);
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-base);
  transition: background-color var(--transition-fast);
}

.btn-clear:hover {
  background-color: var(--color-error-100);
}

.upload-btn {
  width: 100%;
  justify-content: center;
}

.url-alternative {
  margin-top: var(--space-6);
}

.or-separator {
  text-align: center;
  margin: var(--space-4) 0;
  position: relative;
  color: var(--color-primary-500);
}

.or-separator::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background-color: var(--color-primary-200);
  z-index: 1;
}

.or-separator span {
  background-color: white;
  padding: 0 var(--space-4);
  position: relative;
  z-index: 2;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
}

/* Responsive */
@media (max-width: 768px) {
  .profil-container {
    padding: var(--space-4);
  }

  .header-content {
    flex-direction: column;
    text-align: center;
    gap: var(--space-4);
  }

  .header-actions {
    align-items: center;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: var(--space-4);
  }

  .services-grid {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .service-header {
    flex-direction: column;
    text-align: center;
  }

  .profil-meta {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .edit-actions {
    flex-direction: column;
    width: 100%;
  }

  .toggle-container {
    flex-direction: column;
    text-align: center;
    gap: var(--space-3);
  }

  .message-banner {
    flex-direction: column;
    gap: var(--space-3);
  }
}
</style>
