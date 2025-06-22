<script>
export default {
  name: 'DemandeServiceDetailView',
  data() {
    return {
      demande: null,
      prestataireSelectionne: null,
      serviceProgression: null,
      isLoading: true,
      error: null,
      showAnnulationModal: false,
      isProcessing: false,
      annulationForm: {
        raison: ''
      },
      categories: [
        { code: 'TRANSPORT_LIVRAISON', libelle: 'TRANSPORT & LIVRAISON' },
        { code: 'SERVICES_DOMICILE', libelle: 'SERVICES À DOMICILE' },
        { code: 'TRAVAUX_REPARATIONS', libelle: 'TRAVAUX & RÉPARATIONS' },
        { code: 'COURSES_ACHATS', libelle: 'COURSES & ACHATS' },
        { code: 'SERVICES_PERSONNELS', libelle: 'SERVICES PERSONNELS' },
        { code: 'EDUCATION_FORMATION', libelle: 'ÉDUCATION & FORMATION' }
      ]
    }
  },
  computed: {
    peutEtreModifiee() {
      return this.demande && ['PUBLIEE', 'CANDIDATURES_RECUES'].includes(this.demande.statut);
    },

    peutEtreAnnulee() {
      return this.demande && !['TERMINEE', 'ANNULEE'].includes(this.demande.statut);
    },

    actionsDisponibles() {
      const actions = [];

      if (this.demande?.nombreCandidatures > 0 && ['CANDIDATURES_RECUES', 'PRESTATAIRE_SELECTIONNE'].includes(this.demande.statut)) {
        actions.push({
          key: 'voir-candidatures',
          title: 'Voir les candidatures',
          description: `${this.demande.nombreCandidatures} candidature${this.demande.nombreCandidatures > 1 ? 's' : ''} reçue${this.demande.nombreCandidatures > 1 ? 's' : ''}`,
          icon: 'fas fa-users',
          class: 'action-primary'
        });
      }

      if (this.peutEtreModifiee) {
        actions.push({
          key: 'modifier',
          title: 'Modifier la demande',
          description: 'Ajuster les détails de votre demande',
          icon: 'fas fa-edit',
          class: 'action-warning'
        });
      }

      if (this.demande?.statut === 'EN_COURS') {
        actions.push({
          key: 'suivre-service',
          title: 'Suivre le service',
          description: 'Voir la progression en temps réel',
          icon: 'fas fa-chart-line',
          class: 'action-info'
        });
      }

      return actions;
    }
  },
  async mounted() {
    const demandeId = this.$route.params.id;
    if (demandeId) {
      await this.loadDemande(demandeId);
    } else {
      this.error = 'ID de demande manquant';
      this.isLoading = false;
    }
  },
  methods: {
    async loadDemande(demandeId) {
      this.isLoading = true;
      this.error = null;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/demandes-services/${demandeId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          this.demande = await response.json();

          if (this.demande.statut === 'PRESTATAIRE_SELECTIONNE' || this.demande.statut === 'EN_COURS') {
            await this.loadPrestataireSelectionne();
          }

          if (this.demande.statut === 'EN_COURS') {
            await this.loadServiceProgression();
          }
        } else {
          const error = await response.json();
          this.error = error.error || 'Erreur lors du chargement de la demande';
        }
      } catch (error) {
        console.error('Erreur:', error);
        this.error = 'Erreur de connexion';
      } finally {
        this.isLoading = false;
      }
    },

    async loadPrestataireSelectionne() {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/candidatures/demande/${this.demande.idDemande}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const candidatures = await response.json();
          const candidatureAcceptee = candidatures.find(c => c.statut === 'ACCEPTEE');
          if (candidatureAcceptee) {
            this.prestataireSelectionne = candidatureAcceptee.prestataire;
          }
        }
      } catch (error) {
        console.error('Erreur lors du chargement du prestataire:', error);
      }
    },

    async loadServiceProgression() {
      try {
        const token = localStorage.getItem('token');
        this.serviceProgression = 45; // exemple de progression fixe, à remplacer
      } catch (error) {
        console.error('Erreur lors du chargement de la progression:', error);
      }
    },

    getCategoryLabel(code) {
      const category = this.categories.find(c => c.code === code);
      return category ? category.libelle : code;
    },

    getStatutLabel(statut) {
      const labels = {
        'PUBLIEE': 'Publiée',
        'CANDIDATURES_RECUES': 'Candidatures reçues',
        'PRESTATAIRE_SELECTIONNE': 'Prestataire sélectionné',
        'EN_COURS': 'En cours',
        'TERMINEE': 'Terminée',
        'ANNULEE': 'Annulée'
      };
      return labels[statut] || statut;
    },

    getStatutClass(statut) {
      const classes = {
        'PUBLIEE': 'statut-publiee',
        'CANDIDATURES_RECUES': 'statut-candidatures',
        'PRESTATAIRE_SELECTIONNE': 'statut-selectionne',
        'EN_COURS': 'statut-en-cours',
        'TERMINEE': 'statut-terminee',
        'ANNULEE': 'statut-annulee'
      };
      return classes[statut] || '';
    },

    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    isStepActive(statutStep) {
      return this.demande?.statut === statutStep;
    },

    isStepCompleted(statutStep) {
      if (!this.demande?.statut) return false;

      const steps = ['PUBLIEE', 'CANDIDATURES_RECUES', 'PRESTATAIRE_SELECTIONNE', 'EN_COURS', 'TERMINEE'];
      const currentIndex = steps.indexOf(this.demande.statut);
      const stepIndex = steps.indexOf(statutStep);

      return currentIndex > stepIndex;
    },

    goBack() {
      this.$router.push('/client/demandes-services');
    },

    modifierDemande() {
      this.$router.push(`/client/demandes-services/${this.demande.idDemande}/edit`);
    },

    confirmerAnnulation() {
      this.annulationForm.raison = '';
      this.showAnnulationModal = true;
    },

    fermerModalAnnulation() {
      this.showAnnulationModal = false;
    },

    async annulerDemande() {
      this.isProcessing = true;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/demandes-services/${this.demande.idDemande}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            raison: this.annulationForm.raison
          })
        });

        if (response.ok) {
          this.fermerModalAnnulation();
          alert('Demande annulée avec succès');
          this.$router.push('/client/demandes-services');
        } else {
          const error = await response.json();
          alert('Erreur: ' + error.error);
        }
      } catch (error) {
        console.error('Erreur lors de l\'annulation:', error);
        alert('Erreur lors de l\'annulation');
      } finally {
        this.isProcessing = false;
      }
    },

    executerAction(action) {
      switch (action.key) {
        case 'voir-candidatures':
          this.$router.push(`/client/demandes-services/${this.demande.idDemande}/candidatures`);
          break;
        case 'modifier':
          this.modifierDemande();
          break;
        case 'suivre-service':
          alert('Fonctionnalité de suivi en développement');
          break;
      }
    },

    contacterPrestataire() {
      if (this.prestataireSelectionne?.telephone) {
        window.open(`tel:${this.prestataireSelectionne.telephone}`);
      } else if (this.prestataireSelectionne?.email) {
        window.open(`mailto:${this.prestataireSelectionne.email}`);
      }
    },

    voirProfilPrestataire() {
      if (this.prestataireSelectionne) {
        this.$router.push(`/prestataires/${this.prestataireSelectionne.idUtilisateur}`);
      }
    }
  }
}
</script>

<template>
  <div class="demande-detail-container">
    <div class="page-header">
      <button @click="goBack" class="btn btn-secondary">
        <i class="fas fa-arrow-left"></i> Retour
      </button>
      <h1>Ma demande de service</h1>
      <div class="header-actions">
        <button
          v-if="peutEtreModifiee"
          @click="modifierDemande"
          class="btn btn-warning"
        >
          <i class="fas fa-edit"></i> Modifier
        </button>
        <button
          v-if="peutEtreAnnulee"
          @click="confirmerAnnulation"
          class="btn btn-danger"
        >
          <i class="fas fa-times"></i> Annuler
        </button>
      </div>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i> Chargement...
    </div>

    <div v-else-if="error" class="error-message">
      <i class="fas fa-exclamation-triangle"></i> {{ error }}
    </div>

    <div v-else-if="demande" class="demande-content">

      <div class="demande-header-card">
        <div class="header-main">
          <h2>{{ demande.titre }}</h2>
          <div class="badges">
            <span class="category-badge">{{ getCategoryLabel(demande.categorieService) }}</span>
            <span class="statut-badge" :class="getStatutClass(demande.statut)">
              {{ getStatutLabel(demande.statut) }}
            </span>
            <span v-if="demande.servicePersonnalise" class="custom-badge">Personnalisé</span>
          </div>
        </div>
        <div class="header-budget" v-if="demande.budgetMin || demande.budgetMax">
          <div class="budget-label">Votre budget</div>
          <div class="budget-amount">
            <span v-if="demande.budgetMin && demande.budgetMax">
              {{ demande.budgetMin }}€ - {{ demande.budgetMax }}€
            </span>
            <span v-else-if="demande.budgetMin">
              À partir de {{ demande.budgetMin }}€
            </span>
            <span v-else>
              Maximum {{ demande.budgetMax }}€
            </span>
          </div>
        </div>
      </div>

      <div class="progress-section">
        <h3><i class="fas fa-tasks"></i> Progression de votre demande</h3>
        <div class="progress-timeline">
          <div class="timeline-step" :class="{ active: isStepActive('PUBLIEE'), completed: isStepCompleted('PUBLIEE') }">
            <div class="step-icon">
              <i class="fas fa-upload"></i>
            </div>
            <div class="step-content">
              <div class="step-title">Demande publiée</div>
              <div class="step-date">{{ formatDate(demande.dateCreation) }}</div>
            </div>
          </div>

          <div class="timeline-step" :class="{ active: isStepActive('CANDIDATURES_RECUES'), completed: isStepCompleted('CANDIDATURES_RECUES') }">
            <div class="step-icon">
              <i class="fas fa-users"></i>
            </div>
            <div class="step-content">
              <div class="step-title">Candidatures reçues</div>
              <div class="step-info">{{ demande.nombreCandidatures || 0 }} candidature{{ (demande.nombreCandidatures || 0) > 1 ? 's' : '' }}</div>
            </div>
          </div>

          <div class="timeline-step" :class="{ active: isStepActive('PRESTATAIRE_SELECTIONNE'), completed: isStepCompleted('PRESTATAIRE_SELECTIONNE') }">
            <div class="step-icon">
              <i class="fas fa-user-check"></i>
            </div>
            <div class="step-content">
              <div class="step-title">Prestataire sélectionné</div>
              <div class="step-info" v-if="prestataireSelectionne">{{ prestataireSelectionne.prenom }} {{ prestataireSelectionne.nom }}</div>
            </div>
          </div>

          <div class="timeline-step" :class="{ active: isStepActive('EN_COURS'), completed: isStepCompleted('EN_COURS') }">
            <div class="step-icon">
              <i class="fas fa-tools"></i>
            </div>
            <div class="step-content">
              <div class="step-title">Service en cours</div>
              <div class="step-info" v-if="serviceProgression !== null">{{ serviceProgression }}% terminé</div>
            </div>
          </div>

          <div class="timeline-step" :class="{ active: isStepActive('TERMINEE'), completed: isStepCompleted('TERMINEE') }">
            <div class="step-icon">
              <i class="fas fa-check-circle"></i>
            </div>
            <div class="step-content">
              <div class="step-title">Service terminé</div>
            </div>
          </div>
        </div>
      </div>

      <div class="actions-section" v-if="actionsDisponibles.length > 0">
        <h3><i class="fas fa-lightning-bolt"></i> Actions disponibles</h3>
        <div class="actions-grid">
          <button
            v-for="action in actionsDisponibles"
            :key="action.key"
            @click="executerAction(action)"
            class="action-card"
            :class="action.class"
          >
            <i :class="action.icon"></i>
            <div class="action-content">
              <div class="action-title">{{ action.title }}</div>
              <div class="action-description">{{ action.description }}</div>
            </div>
          </button>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-card">
          <h3><i class="fas fa-info-circle"></i> Description</h3>
          <p class="description">{{ demande.description }}</p>
        </div>

        <div class="info-card">
          <h3><i class="fas fa-calendar-alt"></i> Dates</h3>
          <div class="date-info">
            <div class="date-item">
              <span class="date-label">Créée le :</span>
              <span class="date-value">{{ formatDate(demande.dateCreation) }}</span>
            </div>
            <div v-if="demande.dateSouhaitee" class="date-item">
              <span class="date-label">Date souhaitée :</span>
              <span class="date-value">{{ formatDate(demande.dateSouhaitee) }}</span>
            </div>
            <div v-if="demande.dateModification" class="date-item">
              <span class="date-label">Dernière modification :</span>
              <span class="date-value">{{ formatDate(demande.dateModification) }}</span>
            </div>
          </div>
        </div>

        <div class="info-card" v-if="demande.adresseDepart || demande.adresseArrivee">
          <h3><i class="fas fa-map-marker-alt"></i> Localisation</h3>
          <div class="address-info">
            <div v-if="demande.adresseDepart" class="address-item">
              <span class="address-label">Départ :</span>
              <span class="address-value">{{ demande.adresseDepart }}</span>
            </div>
            <div v-if="demande.adresseArrivee" class="address-item">
              <span class="address-label">Arrivée :</span>
              <span class="address-value">{{ demande.adresseArrivee }}</span>
            </div>
          </div>
        </div>

        <div class="info-card">
          <h3><i class="fas fa-chart-bar"></i> Statistiques</h3>
          <div class="stats-info">
            <div class="stat-item">
              <span class="stat-number">{{ demande.nombreCandidatures || 0 }}</span>
              <span class="stat-label">candidature{{ (demande.nombreCandidatures || 0) > 1 ? 's' : '' }}</span>
            </div>
            <div class="stat-item" v-if="demande.nombreVues">
              <span class="stat-number">{{ demande.nombreVues }}</span>
              <span class="stat-label">vue{{ demande.nombreVues > 1 ? 's' : '' }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="prestataireSelectionne" class="prestataire-section">
        <h3><i class="fas fa-user-tie"></i> Prestataire sélectionné</h3>
        <div class="prestataire-card">
          <div class="prestataire-avatar">
            <i class="fas fa-user"></i>
          </div>
          <div class="prestataire-info">
            <div class="prestataire-name">{{ prestataireSelectionne.prenom }} {{ prestataireSelectionne.nom }}</div>
            <div class="prestataire-details">
              <div v-if="prestataireSelectionne.specialite" class="prestataire-specialite">
                <i class="fas fa-tools"></i>
                {{ prestataireSelectionne.specialite }}
              </div>
              <div v-if="prestataireSelectionne.ville" class="prestataire-location">
                <i class="fas fa-map-marker-alt"></i>
                {{ prestataireSelectionne.ville }}
              </div>
              <div v-if="prestataireSelectionne.noteMoyenne" class="prestataire-rating">
                <i class="fas fa-star"></i>
                {{ prestataireSelectionne.noteMoyenne }}/5
              </div>
            </div>
          </div>
          <div class="prestataire-actions">
            <button @click="contacterPrestataire" class="btn btn-primary btn-sm">
              <i class="fas fa-phone"></i> Contacter
            </button>
            <button @click="voirProfilPrestataire" class="btn btn-secondary btn-sm">
              <i class="fas fa-eye"></i> Voir profil
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showAnnulationModal" class="modal-overlay" @click="fermerModalAnnulation">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Confirmer l'annulation</h3>
          <button @click="fermerModalAnnulation" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="modal-body">
          <div class="warning-message">
            <i class="fas fa-exclamation-triangle"></i>
            <p>Êtes-vous sûr de vouloir annuler cette demande de service ?</p>
            <p><strong>Cette action est irréversible.</strong></p>
          </div>

          <div class="form-group">
            <label for="raisonAnnulation">Motif d'annulation (optionnel) :</label>
            <textarea
              id="raisonAnnulation"
              v-model="annulationForm.raison"
              placeholder="Expliquez pourquoi vous annulez cette demande..."
              class="form-textarea"
              rows="3"
            ></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="fermerModalAnnulation" class="btn btn-secondary">
            Non, conserver
          </button>
          <button
            @click="annulerDemande"
            class="btn btn-danger"
            :disabled="isProcessing"
          >
            {{ isProcessing ? 'Annulation...' : 'Oui, annuler' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.demande-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  gap: 1rem;
}

.page-header h1 {
  color: var(--text-primary);
  margin: 0;
  flex: 1;
  text-align: center;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.loading, .error-message {
  text-align: center;
  padding: 3rem;
  color: var(--text-primary);
}

.error-message {
  color: var(--error-color);
  background-color: var(--error-bg);
  border-radius: 8px;
}

.demande-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.demande-header-card {
  background-color: var(--card-bg);
  padding: 2rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
}

.header-main h2 {
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  font-size: 1.8rem;
}

.badges {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.category-badge, .custom-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.category-badge {
  background-color: var(--primary-color-light);
  color: var(--primary-color);
}

.custom-badge {
  background-color: var(--warning-color-light);
  color: var(--warning-color);
}

.statut-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.statut-publiee { background-color: #e3f2fd; color: #1976d2; }
.statut-candidatures { background-color: #fff3e0; color: #f57c00; }
.statut-selectionne { background-color: #f3e5f5; color: #7b1fa2; }
.statut-en-cours { background-color: #e8f5e8; color: #388e3c; }
.statut-terminee { background-color: #e0f2f1; color: #00695c; }
.statut-annulee { background-color: #ffebee; color: #d32f2f; }

.header-budget {
  text-align: right;
  min-width: 150px;
}

.budget-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}

.budget-amount {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--success-color);
}

.progress-section {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.progress-section h3 {
  color: var(--text-primary);
  margin: 0 0 1.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.progress-section h3 i {
  color: var(--primary-color);
}

.progress-timeline {
  display: flex;
  justify-content: space-between;
  position: relative;
}

.progress-timeline::before {
  content: '';
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  height: 2px;
  background-color: var(--border-color);
  z-index: 1;
}

.timeline-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  flex: 1;
  position: relative;
  z-index: 2;
}

.step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.timeline-step.active .step-icon {
  background-color: var(--primary-color);
  animation: pulse 2s infinite;
}

.timeline-step.completed .step-icon {
  background-color: var(--success-color);
}

.step-title {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.step-date, .step-info {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.actions-section {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.actions-section h3 {
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.actions-section h3 i {
  color: var(--primary-color);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: none;
  border-radius: 8px;
  background-color: var(--background-color);
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: left;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.action-card.action-primary {
  border-left: 4px solid var(--primary-color);
}

.action-card.action-warning {
  border-left: 4px solid var(--warning-color);
}

.action-card.action-info {
  border-left: 4px solid #17a2b8;
}

.action-card i {
  font-size: 1.5rem;
  color: var(--primary-color);
}

.action-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.action-description {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.info-card {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.info-card h3 {
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.info-card h3 i {
  color: var(--primary-color);
}

.description {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

.date-info, .address-info {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.date-item, .address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-label, .address-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.date-value, .address-value {
  color: var(--text-primary);
}

.stats-info {
  display: flex;
  gap: 2rem;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--primary-color);
  display: block;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.prestataire-section {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.prestataire-section h3 {
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.prestataire-section h3 i {
  color: var(--primary-color);
}

.prestataire-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background-color: var(--background-color);
  border-radius: 8px;
}

.prestataire-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: var(--primary-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  font-size: 1.5rem;
}

.prestataire-info {
  flex: 1;
}

.prestataire-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.prestataire-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.prestataire-specialite, .prestataire-location, .prestataire-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.prestataire-specialite i, .prestataire-location i, .prestataire-rating i {
  color: var(--primary-color);
}

.prestataire-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 1rem;
  transition: all 0.3s ease;
  font-weight: 500;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--primary-color-dark);
}

.btn-secondary {
  background-color: var(--border-color);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background-color: var(--text-secondary);
}

.btn-warning {
  background-color: var(--warning-color);
  color: white;
}

.btn-warning:hover {
  background-color: var(--warning-color-dark);
}

.btn-danger {
  background-color: var(--error-color);
  color: white;
}

.btn-danger:hover {
  background-color: var(--error-color-dark);
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background-color: var(--card-bg);
  border-radius: 8px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  color: var(--text-primary);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.5rem;
}

.modal-body {
  padding: 1.5rem;
}

.warning-message {
  background-color: var(--warning-color-light);
  color: var(--warning-color);
  padding: 1rem;
  border-radius: 6px;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.warning-message p {
  margin: 0;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  font-weight: 500;
}

.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 1rem;
  resize: vertical;
}

.form-textarea:focus {
  outline: none;
  border-color: var(--primary-color);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid var(--border-color);
}

@media (max-width: 768px) {
  .demande-detail-container {
    padding: 1rem;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .page-header h1 {
    text-align: left;
  }
  
  .demande-header-card {
    flex-direction: column;
    gap: 1rem;
  }
  
  .header-budget {
    text-align: left;
  }
  
  .progress-timeline {
    flex-direction: column;
    gap: 1rem;
  }
  
  .progress-timeline::before {
    display: none;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .date-item, .address-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
  
  .stats-info {
    justify-content: space-around;
  }
  
  .prestataire-card {
    flex-direction: column;
    text-align: center;
  }
  
  .prestataire-actions {
    flex-direction: row;
  }
}
</style>
