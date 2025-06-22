<script>
export default {
  name: 'DemandesDisponiblesView',
  data() {
    return {
      demandes: [],
      filteredDemandes: [],
      isLoading: true,
      error: null,
      searchTerm: '',
      selectedCategorie: '',
      selectedBudget: '',
      selectedUrgence: '',
      currentPage: 1,
      itemsPerPage: 12,
      showCandidatureModal: false,
      selectedDemande: null,
      isSubmittingCandidature: false,
      candidatureForm: {
        prixPropose: null,
        message: '',
        delaiPropose: null
      },
      candidaturesEnvoyees: [],
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
    totalPages() {
      return Math.ceil(this.filteredDemandes.length / this.itemsPerPage);
    }
  },
  async mounted() {
    await this.loadDemandes();
    await this.loadMesCandidatures();
  },
  methods: {
    async loadDemandes() {
      this.isLoading = true;
      this.error = null;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/demandes-services/disponibles-prestataire', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          this.demandes = await response.json();
          this.applyFilters();
        } else {
          const error = await response.json();
          this.error = error.error || 'Erreur lors du chargement des demandes';
        }
      } catch (error) {
        console.error('Erreur:', error);
        this.error = 'Erreur de connexion';
      } finally {
        this.isLoading = false;
      }
    },

    async loadMesCandidatures() {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/candidatures/mes-candidatures', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const candidatures = await response.json();
          this.candidaturesEnvoyees = candidatures.map(c => c.demandeService?.idDemande).filter(Boolean);
        }
      } catch (error) {
        console.error('Erreur lors du chargement des candidatures:', error);
      }
    },

    applyFilters() {
      this.filteredDemandes = this.demandes.filter(demande => {

        const matchSearch = !this.searchTerm ||
          demande.titre.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          demande.description.toLowerCase().includes(this.searchTerm.toLowerCase());

        const matchCategorie = !this.selectedCategorie || demande.categorieService === this.selectedCategorie;

        const matchBudget = !this.selectedBudget || 
          (demande.budgetMax && demande.budgetMax <= parseInt(this.selectedBudget));

        const matchUrgence = !this.selectedUrgence || this.matchUrgence(demande);

        return matchSearch && matchCategorie && matchBudget && matchUrgence;
      });

      this.currentPage = 1;
    },

    matchUrgence(demande) {
      if (!this.selectedUrgence || !demande.dateSouhaitee) return true;

      const dateSouhaitee = new Date(demande.dateSouhaitee);
      const maintenant = new Date();
      const diffJours = Math.ceil((dateSouhaitee - maintenant) / (1000 * 60 * 60 * 24));

      switch (this.selectedUrgence) {
        case 'today':
          return diffJours <= 1;
        case 'week':
          return diffJours <= 7;
        case 'month':
          return diffJours <= 30;
        default:
          return true;
      }
    },

    clearFilters() {
      this.searchTerm = '';
      this.selectedCategorie = '';
      this.selectedBudget = '';
      this.selectedUrgence = '';
      this.applyFilters();
    },

    getCategoryLabel(code) {
      const category = this.categories.find(c => c.code === code);
      return category ? category.libelle : code;
    },

    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('fr-FR', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      });
    },

    formatDateRelative(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const maintenant = new Date();
      const diffHeures = Math.floor((maintenant - date) / (1000 * 60 * 60));

      if (diffHeures < 1) return 'à l\'instant';
      if (diffHeures < 24) return `il y a ${diffHeures}h`;

      const diffJours = Math.floor(diffHeures / 24);
      if (diffJours < 7) return `il y a ${diffJours} jour${diffJours > 1 ? 's' : ''}`;

      return date.toLocaleDateString('fr-FR');
    },

    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },

    isUrgent(demande) {
      if (!demande.dateSouhaitee) return false;
      const dateSouhaitee = new Date(demande.dateSouhaitee);
      const maintenant = new Date();
      const diffJours = Math.ceil((dateSouhaitee - maintenant) / (1000 * 60 * 60 * 24));
      return diffJours <= 3;
    },

    peutCandidater(demande) {
      return !this.candidaturesEnvoyees.includes(demande.idDemande);
    },

    dejaCandidateLabel(demande) {
      return this.peutCandidater(demande) ? 'Candidater' : 'Déjà candidaté';
    },

    voirDetails(demande) {
      this.$router.push(`/prestataire/demandes/${demande.idDemande}`);
    },

    candidater(demande) {
      if (!this.peutCandidater(demande)) return;

      this.selectedDemande = demande;
      this.candidatureForm = {
        prixPropose: null,
        message: '',
        delaiPropose: null
      };
      this.showCandidatureModal = true;
    },

    closeCandidatureModal() {
      this.showCandidatureModal = false;
      this.selectedDemande = null;
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
            demandeService: { idDemande: this.selectedDemande.idDemande },
            prixPropose: parseFloat(this.candidatureForm.prixPropose),
            messagePrestataire: this.candidatureForm.message,
            delaiPropose: this.candidatureForm.delaiPropose ? parseInt(this.candidatureForm.delaiPropose) : null
          })
        });

        if (response.ok) {
          this.candidaturesEnvoyees.push(this.selectedDemande.idDemande);
          this.closeCandidatureModal();
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
  },
  watch: {
    searchTerm() {
      this.applyFilters();
    }
  }
}
</script>

<template>
  <div class="demandes-disponibles-container">
    <div class="page-header">
      <h1>Demandes de services disponibles</h1>
      <div class="header-actions">
        <div class="search-box">
          <i class="fas fa-search"></i>
          <input
            type="text"
            v-model="searchTerm"
            placeholder="Rechercher dans les demandes..."
            class="search-input"
          >
        </div>
        <button @click="loadDemandes" class="btn btn-primary">
          <i class="fas fa-sync-alt"></i> Actualiser
        </button>
      </div>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i> Chargement des demandes...
    </div>

    <div v-else-if="error" class="error-message">
      <i class="fas fa-exclamation-triangle"></i> {{ error }}
    </div>

    <div v-else>
      <div class="filters-section">
        <div class="filter-group">
          <label for="categorieFilter">Catégorie:</label>
          <select id="categorieFilter" v-model="selectedCategorie" @change="applyFilters" class="filter-select">
            <option value="">Toutes les catégories</option>
            <option v-for="category in categories" :key="category.code" :value="category.code">
              {{ category.libelle }}
            </option>
          </select>
        </div>

        <div class="filter-group">
          <label for="budgetFilter">Budget max:</label>
          <select id="budgetFilter" v-model="selectedBudget" @change="applyFilters" class="filter-select">
            <option value="">Tous budgets</option>
            <option value="50">Jusqu'à 50€</option>
            <option value="100">Jusqu'à 100€</option>
            <option value="200">Jusqu'à 200€</option>
            <option value="500">Jusqu'à 500€</option>
          </select>
        </div>

        <div class="filter-group">
          <label for="urgenceFilter">Urgence:</label>
          <select id="urgenceFilter" v-model="selectedUrgence" @change="applyFilters" class="filter-select">
            <option value="">Toutes</option>
            <option value="today">Aujourd'hui</option>
            <option value="week">Cette semaine</option>
            <option value="month">Ce mois</option>
          </select>
        </div>

        <div class="filter-results">
          {{ filteredDemandes.length }} demande{{ filteredDemandes.length > 1 ? 's' : '' }} trouvée{{ filteredDemandes.length > 1 ? 's' : '' }}
        </div>
      </div>

      <div v-if="filteredDemandes.length === 0" class="empty-state">
        <i class="fas fa-search"></i>
        <h3>Aucune demande disponible</h3>
        <p>{{ searchTerm || selectedCategorie || selectedBudget ? 'Essayez de modifier vos filtres' : 'Il n\'y a pas de nouvelles demandes pour le moment' }}</p>
        <button @click="clearFilters" class="btn btn-secondary" v-if="searchTerm || selectedCategorie || selectedBudget">
          Effacer les filtres
        </button>
      </div>

      <div v-else class="demandes-grid">
        <div
          v-for="demande in filteredDemandes"
          :key="demande.idDemande"
          class="demande-card"
          :class="{ 'urgent': isUrgent(demande) }"
        >
          <div class="demande-header">
            <div class="demande-title">
              <h3>{{ demande.titre }}</h3>
              <div class="demande-badges">
                <span class="category-badge">{{ getCategoryLabel(demande.categorieService) }}</span>
                <span v-if="isUrgent(demande)" class="urgent-badge">Urgent</span>
                <span v-if="demande.servicePersonnalise" class="custom-badge">Personnalisé</span>
              </div>
            </div>
            <div class="demande-budget" v-if="demande.budgetMin || demande.budgetMax">
              <div class="budget-label">Budget</div>
              <div class="budget-amount">
                <span v-if="demande.budgetMin && demande.budgetMax">
                  {{ demande.budgetMin }}€ - {{ demande.budgetMax }}€
                </span>
                <span v-else-if="demande.budgetMin">
                  À partir de {{ demande.budgetMin }}€
                </span>
                <span v-else>
                  Max {{ demande.budgetMax }}€
                </span>
              </div>
            </div>
          </div>

          <div class="demande-meta">
            <div class="meta-item">
              <i class="fas fa-calendar"></i>
              <span>Publié {{ formatDateRelative(demande.dateCreation) }}</span>
            </div>
            <div v-if="demande.dateSouhaitee" class="meta-item">
              <i class="fas fa-clock"></i>
              <span>Pour le {{ formatDate(demande.dateSouhaitee) }}</span>
            </div>
            <div v-if="demande.adresseDepart" class="meta-item">
              <i class="fas fa-map-marker-alt"></i>
              <span>{{ demande.adresseDepart }}</span>
            </div>
            <div class="meta-item">
              <i class="fas fa-users"></i>
              <span>{{ demande.nombreCandidatures || 0 }} candidature{{ (demande.nombreCandidatures || 0) > 1 ? 's' : '' }}</span>
            </div>
          </div>

          <div class="demande-description">
            {{ truncateText(demande.description, 150) }}
          </div>

          <div class="demande-client">
            <div class="client-info">
              <div class="client-avatar">
                <i class="fas fa-user"></i>
              </div>
              <div class="client-details">
                <div class="client-name">{{ demande.client?.prenom }} {{ demande.client?.nom }}</div>
                <div class="client-location">{{ demande.client?.ville || 'Localisation non précisée' }}</div>
              </div>
            </div>
          </div>

          <div class="demande-actions">
            <button @click="voirDetails(demande)" class="btn btn-secondary btn-sm">
              <i class="fas fa-eye"></i> Voir détails
            </button>
            <button
              @click="candidater(demande)"
              class="btn btn-primary btn-sm"
              :disabled="!peutCandidater(demande)"
            >
              <i class="fas fa-paper-plane"></i>
              {{ dejaCandidateLabel(demande) }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="filteredDemandes.length > 0" class="pagination">
        <button
          @click="currentPage--"
          :disabled="currentPage <= 1"
          class="btn btn-secondary"
        >
          <i class="fas fa-chevron-left"></i> Précédent
        </button>

        <span class="page-info">
          Page {{ currentPage }} sur {{ totalPages }}
        </span>

        <button
          @click="currentPage++"
          :disabled="currentPage >= totalPages"
          class="btn btn-secondary"
        >
          Suivant <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>

    <div v-if="showCandidatureModal" class="modal-overlay" @click="closeCandidatureModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Candidater pour "{{ selectedDemande?.titre }}"</h3>
          <button @click="closeCandidatureModal" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="modal-body">
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
            >
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeCandidatureModal" class="btn btn-secondary">
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
.demandes-disponibles-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.page-header h1 {
  color: var(--text-primary);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-box i {
  position: absolute;
  left: 0.75rem;
  color: var(--text-secondary);
}

.search-input {
  padding: 0.5rem 0.75rem 0.5rem 2.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  width: 250px;
  font-size: 0.9rem;
}

.filters-section {
  display: flex;
  gap: 1.5rem;
  align-items: center;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: var(--shadow);
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 0.9rem;
}

.filter-select {
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--background-color);
  color: var(--text-primary);
  min-width: 150px;
}

.filter-results {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-left: auto;
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

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: var(--text-secondary);
}

.empty-state i {
  font-size: 4rem;
  margin-bottom: 1rem;
  color: var(--border-color);
}

.empty-state h3 {
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.demandes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
}

.demande-card {
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: var(--shadow);
  padding: 1.5rem;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
}

.demande-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.demande-card.urgent {
  border-left: 4px solid var(--error-color);
}

.demande-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.demande-title h3 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
}

.demande-badges {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.category-badge, .urgent-badge, .custom-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
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

.demande-budget {
  text-align: right;
  min-width: 100px;
}

.budget-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-bottom: 0.25rem;
}

.budget-amount {
  font-weight: 600;
  color: var(--success-color);
  font-size: 0.9rem;
}

.demande-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.meta-item i {
  color: var(--primary-color);
  width: 12px;
}

.demande-description {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.demande-client {
  margin-bottom: 1rem;
}

.client-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.client-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--primary-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}

.client-name {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.client-location {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.demande-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  font-weight: 500;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.8rem;
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

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  padding: 1rem;
}

.page-info {
  color: var(--text-secondary);
  font-size: 0.9rem;
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

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid var(--border-color);
}

@media (max-width: 768px) {
  .demandes-disponibles-container {
    padding: 1rem;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    flex-direction: column;
  }
  
  .search-input {
    width: 100%;
  }
  
  .filters-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-results {
    margin-left: 0;
    text-align: center;
  }
  
  .demandes-grid {
    grid-template-columns: 1fr;
  }
  
  .demande-header {
    flex-direction: column;
    gap: 1rem;
  }
  
  .demande-budget {
    text-align: left;
  }
}
</style>
