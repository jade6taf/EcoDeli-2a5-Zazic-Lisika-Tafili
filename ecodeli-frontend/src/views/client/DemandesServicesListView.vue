<script>
export default {
  name: 'DemandesServicesListView',
  data() {
    return {
      demandes: [],
      filteredDemandes: [],
      isLoading: true,
      error: null,
      selectedStatut: '',
      selectedCategorie: '',
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
    statistiques() {
      const total = this.demandes.length;
      const actives = this.demandes.filter(d =>
        ['PUBLIEE', 'CANDIDATURES_RECUES', 'PRESTATAIRE_SELECTIONNE', 'EN_COURS'].includes(d.statut)
      ).length;
      const terminees = this.demandes.filter(d => d.statut === 'TERMINEE').length;

      return { total, actives, terminees };
    }
  },
  async mounted() {
    await this.loadDemandes();
  },
  methods: {
    async loadDemandes() {
      this.isLoading = true;
      this.error = null;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/demandes-services/mes-demandes', {
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
        alert('Erreur !');
      } finally {
        this.isLoading = false;
      }
    },

    applyFilters() {
      this.filteredDemandes = this.demandes.filter(demande => {
        const matchStatut = !this.selectedStatut || demande.statut === this.selectedStatut;
        const matchCategorie = !this.selectedCategorie || demande.categorieService === this.selectedCategorie;
        return matchStatut && matchCategorie;
      });
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
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },
    peutEtreModifiee(demande) {
      return ['PUBLIEE', 'CANDIDATURES_RECUES'].includes(demande.statut);
    },
    peutEtreAnnulee(demande) {
      return !['TERMINEE', 'ANNULEE'].includes(demande.statut);
    },
    goToDetail(demandeId) {
      this.$router.push(`/client/demandes-services/${demandeId}`);
    },
    modifierDemande(demandeId) {
      this.$router.push(`/client/demandes-services/${demandeId}/edit`);
    },
    voirCandidatures(demandeId) {
      this.$router.push(`/client/demandes-services/${demandeId}/candidatures`);
    },
    async annulerDemande(demande) {
      if (!confirm(`Êtes-vous sûr de vouloir annuler la demande "${demande.titre}" ?`)) {
        return;
      }
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/demandes-services/${demande.idDemande}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (response.ok) {
          await this.loadDemandes();
        } else {
          const error = await response.json();
          alert('Erreur: ' + error.error);
        }
      } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur lors de l\'annulation');
      }
    }
  }
}
</script>

<template>
  <div class="demandes-services-container">
    <div class="page-header">
      <h1>Mes demandes de service</h1>
      <router-link to="/client/demandes-services/new" class="btn btn-primary">
        <i class="fas fa-plus"></i> Nouvelle demande
      </router-link>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i> Chargement...
    </div>

    <div v-else-if="error" class="error-message">
      <i class="fas fa-exclamation-triangle"></i> {{ error }}
    </div>

    <div v-else>
      <div class="filters-section">
        <div class="filter-group">
          <label for="statutFilter">Filtrer par statut:</label>
          <select id="statutFilter" v-model="selectedStatut" @change="applyFilters" class="filter-select">
            <option value="">Tous les statuts</option>
            <option value="PUBLIEE">Publiées</option>
            <option value="CANDIDATURES_RECUES">Candidatures reçues</option>
            <option value="PRESTATAIRE_SELECTIONNE">Prestataire sélectionné</option>
            <option value="EN_COURS">En cours</option>
            <option value="TERMINEE">Terminées</option>
            <option value="ANNULEE">Annulées</option>
          </select>
        </div>

        <div class="filter-group">
          <label for="categorieFilter">Filtrer par catégorie:</label>
          <select id="categorieFilter" v-model="selectedCategorie" @change="applyFilters" class="filter-select">
            <option value="">Toutes les catégories</option>
            <option v-for="category in categories" :key="category.code" :value="category.code">
              {{ category.libelle }}
            </option>
          </select>
        </div>
      </div>

      <div class="stats-section" v-if="demandes.length > 0">
        <div class="stat-card">
          <div class="stat-number">{{ statistiques.total }}</div>
          <div class="stat-label">Total</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ statistiques.actives }}</div>
          <div class="stat-label">Actives</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ statistiques.terminees }}</div>
          <div class="stat-label">Terminées</div>
        </div>
      </div>

      <div v-if="filteredDemandes.length === 0" class="empty-state">
        <i class="fas fa-clipboard-list"></i>
        <h3>{{ selectedStatut || selectedCategorie ? 'Aucune demande correspondante' : 'Aucune demande de service' }}</h3>
        <p>{{ selectedStatut || selectedCategorie ? 'Essayez de modifier vos filtres' : 'Commencez par créer votre première demande de service' }}</p>
        <router-link to="/client/demandes-services/new" class="btn btn-primary" v-if="!selectedStatut && !selectedCategorie">
          Créer ma première demande
        </router-link>
      </div>

      <div v-else class="demandes-grid">
        <div
          v-for="demande in filteredDemandes"
          :key="demande.idDemande"
          class="demande-card"
          @click="goToDetail(demande.idDemande)"
        >
          <div class="demande-header">
            <h3>{{ demande.titre }}</h3>
            <span class="statut-badge" :class="getStatutClass(demande.statut)">
              {{ getStatutLabel(demande.statut) }}
            </span>
          </div>

          <div class="demande-meta">
            <div class="meta-item">
              <i class="fas fa-tag"></i>
              <span>{{ getCategoryLabel(demande.categorieService) }}</span>
            </div>
            <div class="meta-item">
              <i class="fas fa-calendar"></i>
              <span>{{ formatDate(demande.dateCreation) }}</span>
            </div>
            <div v-if="demande.dateSouhaitee" class="meta-item">
              <i class="fas fa-clock"></i>
              <span>{{ formatDate(demande.dateSouhaitee) }}</span>
            </div>
          </div>

          <div class="demande-description">
            {{ truncateText(demande.description, 120) }}
          </div>

          <div class="demande-footer">
            <div class="candidatures-info" v-if="demande.nombreCandidatures > 0">
              <i class="fas fa-users"></i>
              <span>{{ demande.nombreCandidatures }} candidature{{ demande.nombreCandidatures > 1 ? 's' : '' }}</span>
            </div>

            <div class="budget-info" v-if="demande.budgetMin || demande.budgetMax">
              <i class="fas fa-euro-sign"></i>
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

            <div class="actions">
              <button
                v-if="demande.nombreCandidatures > 0"
                @click.stop="voirCandidatures(demande.idDemande)"
                class="btn btn-sm btn-info"
              >
                <i class="fas fa-users"></i>
              </button>
              <button
                v-if="peutEtreModifiee(demande)"
                @click.stop="modifierDemande(demande.idDemande)"
                class="btn btn-sm btn-secondary"
              >
                <i class="fas fa-edit"></i>
              </button>
              <button
                v-if="peutEtreAnnulee(demande)"
                @click.stop="annulerDemande(demande)"
                class="btn btn-sm btn-danger"
              >
                <i class="fas fa-times"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.demandes-services-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  color: var(--text-primary);
  margin: 0;
}

.filters-section {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: var(--shadow);
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
}

.stats-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  flex: 1;
  text-align: center;
  padding: 1.5rem;
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: var(--primary-color);
  margin-bottom: 0.5rem;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
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
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.demande-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.demande-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.demande-header h3 {
  color: var(--text-primary);
  margin: 0;
  flex: 1;
  margin-right: 1rem;
}

.statut-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  white-space: nowrap;
}

.statut-publiee { background-color: #e3f2fd; color: #1976d2; }
.statut-candidatures { background-color: #fff3e0; color: #f57c00; }
.statut-selectionne { background-color: #f3e5f5; color: #7b1fa2; }
.statut-en-cours { background-color: #e8f5e8; color: #388e3c; }
.statut-terminee { background-color: #e0f2f1; color: #00695c; }
.statut-annulee { background-color: #ffebee; color: #d32f2f; }

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
  font-size: 0.9rem;
}

.meta-item i {
  color: var(--primary-color);
}

.demande-description {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 1rem;
}

.demande-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.candidatures-info, .budget-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.candidatures-info i, .budget-info i {
  color: var(--primary-color);
}

.actions {
  display: flex;
  gap: 0.5rem;
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
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.8rem;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}

.btn-primary:hover {
  background-color: var(--primary-color-dark);
}

.btn-secondary {
  background-color: var(--border-color);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background-color: var(--text-secondary);
}

.btn-danger {
  background-color: var(--error-color);
  color: white;
}

.btn-danger:hover {
  background-color: var(--error-color-dark);
}

.btn-info {
  background-color: #17a2b8;
  color: white;
}

.btn-info:hover {
  background-color: #138496;
}

@media (max-width: 768px) {
  .demandes-services-container {
    padding: 1rem;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .filters-section {
    flex-direction: column;
    gap: 1rem;
  }
  
  .stats-section {
    flex-direction: column;
  }
  
  .demandes-grid {
    grid-template-columns: 1fr;
  }
  
  .demande-footer {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .actions {
    justify-content: flex-end;
  }
}
</style>
