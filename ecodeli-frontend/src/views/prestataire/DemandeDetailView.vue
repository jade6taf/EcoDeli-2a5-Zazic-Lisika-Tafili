
<script>
export default {
  name: 'DemandeDetailView',
  data() {
    return {
      demande: null,
      isLoading: true,
      error: null,
      peutCandidater: false,
      showCandidatureModal: false,
      isSubmittingCandidature: false,
      candidatureForm: {
        prixPropose: null,
        message: '',
        delaiPropose: null
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
    isUrgent() {
      if (!this.demande?.dateSouhaitee) return false;
      const dateSouhaitee = new Date(this.demande.dateSouhaitee);
      const maintenant = new Date();
      const diffJours = Math.ceil((dateSouhaitee - maintenant) / (1000 * 60 * 60 * 24));
      return diffJours <= 3;
    }
  },
  async mounted() {
    const demandeId = this.$route.params.id;
    if (demandeId) {
      await this.loadDemande(demandeId);
      await this.verifierCandidature(demandeId);
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

    async verifierCandidature(demandeId) {
      try {
        const token = localStorage.getItem('token');
        const user = JSON.parse(localStorage.getItem('user'));

        const response = await fetch(`/api/candidatures/prestataire/${user.idUtilisateur}/peut-candidater/${demandeId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const result = await response.json();
          this.peutCandidater = result.peutCandidater;
        }
      } catch (error) {
        console.error('Erreur lors de la vérification:', error);
        this.peutCandidater = true; // Par défaut, on autorise
      }
    },

    getCategoryLabel(code) {
      const category = this.categories.find(c => c.code === code);
      return category ? category.libelle : code;
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

    masquerTelephone(telephone) {
      if (!telephone) return '';
      return telephone.substring(0, 2) + 'XX.XX.XX.XX';
    },

    goBack() {
      this.$router.go(-1);
    },

    ouvrirModalCandidature() {
      this.candidatureForm = {
        prixPropose: this.demande?.budgetMax || null,
        message: '',
        delaiPropose: null
      };
      this.showCandidatureModal = true;
    },

    fermerModalCandidature() {
      this.showCandidatureModal = false;
    },

    async envoyerCandidature() {
      if (!this.candidatureForm.prixPropose || !this.candidatureForm.message) return;

      this.isSubmittingCandidature = true;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/candidatures', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify({
            demandeService: { idDemande: this.demande.idDemande },
            prixPropose: parseFloat(this.candidatureForm.prixPropose),
            message: this.candidatureForm.message,
            delaiPropose: this.candidatureForm.delaiPropose ? parseInt(this.candidatureForm.delaiPropose) : null
          })
        });

        if (response.ok) {
          this.peutCandidater = false;
          this.fermerModalCandidature();
          alert('Candidature envoyée avec succès !');
        } else {
          const error = await response.json();
          alert('Erreur: ' + error.error);
        }
      } catch (error) {
        console.error('Erreur lors de l\'envoi de la candidature:', error);
        alert('Erreur lors de l\'envoi de la candidature');
      } finally {
        this.isSubmittingCandidature = false;
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
      <h1>Détail de la demande</h1>
      <div class="header-actions">
        <button
          v-if="peutCandidater"
          @click="ouvrirModalCandidature"
          class="btn btn-primary"
        >
          <i class="fas fa-paper-plane"></i> Candidater
        </button>
        <span v-else class="deja-candidate">
          <i class="fas fa-check"></i> Déjà candidaté
        </span>
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
            <span v-if="isUrgent" class="urgent-badge">Urgent</span>
            <span v-if="demande.servicePersonnalise" class="custom-badge">Personnalisé</span>
          </div>
        </div>
        <div class="header-budget" v-if="demande.budgetMin || demande.budgetMax">
          <div class="budget-label">Budget proposé</div>
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

      <div class="info-grid">
        <div class="info-card">
          <h3><i class="fas fa-info-circle"></i> Description</h3>
          <p class="description">{{ demande.description }}</p>
        </div>

        <div class="info-card">
          <h3><i class="fas fa-calendar-alt"></i> Dates</h3>
          <div class="date-info">
            <div class="date-item">
              <span class="date-label">Publié le :</span>
              <span class="date-value">{{ formatDate(demande.dateCreation) }}</span>
            </div>
            <div v-if="demande.dateSouhaitee" class="date-item">
              <span class="date-label">Date souhaitée :</span>
              <span class="date-value">{{ formatDate(demande.dateSouhaitee) }}</span>
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
          <h3><i class="fas fa-users"></i> Concurrence</h3>
          <div class="competition-info">
            <div class="competition-stat">
              <span class="stat-number">{{ demande.nombreCandidatures || 0 }}</span>
              <span class="stat-label">candidature{{ (demande.nombreCandidatures || 0) > 1 ? 's' : '' }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="client-section">
        <h3><i class="fas fa-user"></i> Informations client</h3>
        <div class="client-card">
          <div class="client-avatar">
            <i class="fas fa-user"></i>
          </div>
          <div class="client-info">
            <div class="client-name">{{ demande.client?.prenom }} {{ demande.client?.nom }}</div>
            <div class="client-details">
              <div v-if="demande.client?.ville" class="client-location">
                <i class="fas fa-map-marker-alt"></i>
                {{ demande.client.ville }}
              </div>
              <div v-if="demande.client?.telephone" class="client-contact">
                <i class="fas fa-phone"></i>
                <span class="masked-phone">{{ masquerTelephone(demande.client.telephone) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="client-note">
          <i class="fas fa-info-circle"></i>
          Les coordonnées complètes seront révélées si votre candidature est acceptée.
        </div>
      </div>

      <div v-if="demande.servicePersonnalise" class="technical-section">
        <h3><i class="fas fa-cogs"></i> Détails techniques</h3>
        <div class="technical-details">
          <p>Service personnalisé selon les besoins spécifiés dans la description.</p>
        </div>
      </div>
    </div>

    <div v-if="showCandidatureModal" class="modal-overlay" @click="fermerModalCandidature">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Candidater pour "{{ demande?.titre }}"</h3>
          <button @click="fermerModalCandidature" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="modal-body">
          <div class="demande-recap">
            <h4>{{ demande?.titre }}</h4>
            <p>{{ getCategoryLabel(demande?.categorieService) }}</p>
            <div v-if="demande?.budgetMax" class="budget-reference">
              Budget client : {{ demande.budgetMax }}€ maximum
            </div>
          </div>

          <div class="form-group">
            <label for="prixPropose">Prix proposé (€) *</label>
            <input
              type="number"
              id="prixPropose"
              v-model="candidatureForm.prixPropose"
              min="0"
              step="5"
              class="form-input"
              required
              :placeholder="demande?.budgetMax ? `Max ${demande.budgetMax}€` : 'Votre prix'"
            >
          </div>

          <div class="form-group">
            <label for="message">Message de motivation *</label>
            <textarea
              id="message"
              v-model="candidatureForm.message"
              placeholder="Présentez-vous et expliquez pourquoi vous êtes le bon prestataire pour cette mission..."
              class="form-textarea"
              rows="4"
              required
            ></textarea>
          </div>

          <div class="form-group">
            <label for="delaiPropose">Délai proposé (jours)</label>
            <input
              type="number"
              id="delaiPropose"
              v-model="candidatureForm.delaiPropose"
              min="1"
              class="form-input"
              placeholder="Nombre de jours nécessaires"
            >
          </div>
        </div>

        <div class="modal-footer">
          <button @click="fermerModalCandidature" class="btn btn-secondary">
            Annuler
          </button>
          <button
            @click="envoyerCandidature"
            class="btn btn-primary"
            :disabled="isSubmittingCandidature || !candidatureForm.prixPropose || !candidatureForm.message"
          >
            {{ isSubmittingCandidature ? 'Envoi...' : 'Envoyer ma candidature' }}
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

.header-actions .deja-candidate {
  color: var(--success-color);
  font-weight: 500;
  display: flex;
  align-items: center;
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

.category-badge, .urgent-badge, .custom-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.category-badge {
  background-color: var(--primary-color-light);
  color: var(--primary-color);
}

.urgent-badge {
  background-color: var(--error-color-light);
  color: var(--error-color);
}

.custom-badge {
  background-color: var(--warning-color-light);
  color: var(--warning-color);
}

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

.competition-info {
  text-align: center;
}

.competition-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: var(--primary-color);
  line-height: 1;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.client-section, .technical-section {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.client-section h3, .technical-section h3 {
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.client-section h3 i, .technical-section h3 i {
  color: var(--primary-color);
}

.client-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.client-avatar {
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

.client-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.client-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.client-location, .client-contact {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.client-location i, .client-contact i {
  color: var(--primary-color);
}

.masked-phone {
  font-family: monospace;
}

.client-note {
  background-color: var(--info-color-light);
  color: var(--info-color);
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.technical-details {
  color: var(--text-secondary);
  line-height: 1.5;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  transition: all 0.3s ease;
  font-weight: 500;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  max-width: 600px;
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

.demande-recap {
  background-color: var(--background-color);
  padding: 1rem;
  border-radius: 6px;
  margin-bottom: 1.5rem;
}

.demande-recap h4 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
}

.demande-recap p {
  color: var(--text-secondary);
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
}

.budget-reference {
  color: var(--success-color);
  font-weight: 500;
  font-size: 0.9rem;
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

.form-input, .form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 1rem;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: var(--primary-color);
}

.form-textarea {
  resize: vertical;
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
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .date-item, .address-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}
</style>
