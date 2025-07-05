<script>
import apiServices from '@/services/apiServices';
import { authStore } from '@/store/auth';

export default {
  name: 'InformationsPersonnelles',
  data() {
    return {
      userData: {
        email: '',
        nom: '',
        prenom: '',
        telephone: '',
        nomEntreprise: '',
        siret: ''
      },
      loading: false,
      message: '',
      messageType: ''
    }
  },
  methods: {
    async loadUserData() {
      this.loading = true;
      try {
        if (!authStore.isAuthenticated) {
          this.$router.push('/login');
          return;
        }

        const response = await apiServices.get('prestataires/profile');
        if (response.ok) {
          const data = await response.json();
          this.userData = {
            email: data.email || '',
            nom: data.nom || '',
            prenom: data.prenom || '',
            telephone: data.telephone || '',
            nomEntreprise: data.nomEntreprise || '',
            siret: data.siret || ''
          };
        } else {
          throw new Error('Erreur lors du chargement des données');
        }
      } catch (error) {
        console.error('Erreur lors du chargement:', error);
        this.showMessage('Erreur lors du chargement des informations', 'error');

        if (error.message.includes('session') || error.message.includes('401')) {
          authStore.clearAuth();
          this.$router.push('/login');
        }
      } finally {
        this.loading = false;
      }
    },

    showMessage(text, type) {
      this.message = text;
      this.messageType = type;
      setTimeout(() => {
        this.message = '';
      }, 5000);
    }
  },

  created() {
    if (!authStore.isAuthenticated) {
      this.$router.push('/login');
      return;
    }
    this.loadUserData();
  }
}
</script>

<template>
  <div class="informations-container">
    <!-- Messages de feedback -->
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

    <!-- Header de la page -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">
            <i class="fas fa-user-circle"></i>
            Informations personnelles
          </h1>
          <p class="page-subtitle">Consultez vos informations personnelles et professionnelles</p>
        </div>

        <div class="header-badge">
          <div class="status-badge verified">
            <i class="fas fa-shield-check"></i>
            <span>Compte vérifié</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
      <div class="loading-content">
        <div class="spinner"></div>
        <p>Chargement de vos informations...</p>
      </div>
    </div>

    <!-- Contenu principal -->
    <div v-else class="content-sections">
      <!-- Section Informations personnelles -->
      <div class="info-section card">
        <div class="section-header">
          <h2 class="section-title">
            <i class="fas fa-user"></i>
            Informations personnelles
          </h2>
          <p class="section-description">Vos données personnelles de contact</p>
        </div>

        <div class="fields-grid">
          <!-- Prénom -->
          <div class="field-group">
            <label class="label">Prénom</label>
            <div class="field-display">
              <i class="fas fa-user-tag field-icon"></i>
              <span class="field-value">{{ userData.prenom || 'Non renseigné' }}</span>
            </div>
          </div>

          <!-- Nom -->
          <div class="field-group">
            <label class="label">Nom</label>
            <div class="field-display">
              <i class="fas fa-user field-icon"></i>
              <span class="field-value">{{ userData.nom || 'Non renseigné' }}</span>
            </div>
          </div>

          <!-- Email -->
          <div class="field-group full-width">
            <label class="label">Adresse email</label>
            <div class="field-display">
              <i class="fas fa-envelope field-icon"></i>
              <span class="field-value">{{ userData.email || 'Non renseigné' }}</span>
              <div class="field-badge verified">
                <i class="fas fa-check-circle"></i>
                <span>Vérifié</span>
              </div>
            </div>
          </div>

          <!-- Téléphone -->
          <div class="field-group full-width">
            <label class="label">Téléphone</label>
            <div class="field-display">
              <i class="fas fa-phone field-icon"></i>
              <span class="field-value">{{ userData.telephone || 'Non renseigné' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Section Informations professionnelles -->
      <div class="info-section card">
        <div class="section-header">
          <h2 class="section-title">
            <i class="fas fa-building"></i>
            Informations professionnelles
          </h2>
          <p class="section-description">Informations relatives à votre entreprise</p>
        </div>

        <div class="fields-grid">
          <!-- Nom de l'entreprise -->
          <div class="field-group full-width">
            <label class="label">Nom de l'entreprise</label>
            <div class="field-display">
              <i class="fas fa-building field-icon"></i>
              <span class="field-value">{{ userData.nomEntreprise || 'Non renseigné' }}</span>
            </div>
          </div>

          <!-- SIRET -->
          <div class="field-group full-width">
            <label class="label">Numéro SIRET</label>
            <div class="field-display">
              <i class="fas fa-id-card field-icon"></i>
              <span class="field-value">{{ userData.siret || 'Non renseigné' }}</span>
              <div v-if="userData.siret" class="field-badge verified">
                <i class="fas fa-shield-alt"></i>
                <span>Entreprise</span>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.informations-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: var(--space-6);
  min-height: 100vh;
}

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

/* Header de la page */
.page-header {
  margin-bottom: var(--space-8);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-6);
  padding: var(--space-8);
  background: linear-gradient(135deg, var(--color-primary-50) 0%, white 100%);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-primary-100);
}

.header-info {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--color-primary-800);
  margin: 0 0 var(--space-2) 0;
}

.page-subtitle {
  color: var(--color-primary-600);
  margin: 0;
  font-size: var(--text-lg);
}

.header-badge {
  display: flex;
  align-items: center;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-full);
  font-weight: var(--font-medium);
  font-size: var(--text-sm);
}

.status-badge.verified {
  background-color: var(--color-success-100);
  color: var(--color-success-800);
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

/* Sections de contenu */
.content-sections {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.info-section {
  background-color: white;
  border-radius: var(--radius-xl);
  padding: var(--space-8);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-primary-100);
}

.section-header {
  margin-bottom: var(--space-8);
  padding-bottom: var(--space-4);
  border-bottom: 2px solid var(--color-primary-100);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin: 0 0 var(--space-2) 0;
}

.section-description {
  color: var(--color-primary-600);
  margin: 0;
  font-size: var(--text-base);
}

.fields-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--space-6);
}

.field-group.full-width {
  grid-column: 1 / -1;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-primary-700);
}

/* Affichage des champs */
.field-display {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background-color: var(--color-primary-50);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-primary-200);
  min-height: 48px;
}

.field-icon {
  color: var(--color-primary-500);
  font-size: var(--text-lg);
  flex-shrink: 0;
}

.field-value {
  color: var(--color-primary-800);
  font-weight: var(--font-medium);
  flex: 1;
}

.field-badge {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
}

.field-badge.verified {
  background-color: var(--color-success-100);
  color: var(--color-success-800);
}

/* Section sécurité */
.security-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: var(--space-6);
}

.security-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-6);
  background-color: var(--color-primary-50);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-primary-200);
}

.security-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-secondary-100);
  color: var(--color-secondary-600);
  border-radius: var(--radius-lg);
  flex-shrink: 0;
  font-size: var(--text-xl);
}

.security-details {
  flex: 1;
}

.security-details h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--color-primary-800);
  margin: 0 0 var(--space-1) 0;
}

.security-details p {
  color: var(--color-primary-600);
  margin: 0 0 var(--space-3) 0;
  font-size: var(--text-sm);
}

.security-status {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
}

.security-status.warning {
  color: var(--color-warning-700);
}

.security-status.inactive {
  color: var(--color-error-600);
}

.security-status.active {
  color: var(--color-success-700);
}

/* Section statistiques */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-6);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6);
  background-color: var(--color-primary-50);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-primary-200);
  transition: all var(--transition-fast);
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-secondary-100);
  color: var(--color-secondary-600);
  border-radius: var(--radius-lg);
  flex-shrink: 0;
  font-size: var(--text-xl);
}

.stat-content h3 {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-primary-600);
  margin: 0 0 var(--space-1) 0;
}

.stat-value {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-primary-800);
  margin: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .informations-container {
    padding: var(--space-4);
  }

  .header-content {
    flex-direction: column;
    text-align: center;
    gap: var(--space-4);
  }

  .fields-grid {
    grid-template-columns: 1fr;
  }

  .field-group.full-width {
    grid-column: 1;
  }

  .security-info-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: var(--text-2xl);
  }

  .info-section {
    padding: var(--space-4);
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .security-item {
    flex-direction: column;
    text-align: center;
    gap: var(--space-3);
  }

  .stat-item {
    flex-direction: column;
    text-align: center;
    gap: var(--space-3);
  }
}
</style>
