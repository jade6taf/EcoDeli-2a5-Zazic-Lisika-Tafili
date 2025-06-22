<script>
export default {
  name: 'CandidaturesRecuesView',
  data() {
    return {
      demandeService: null,
      candidatures: [],
      filteredCandidatures: [],
      isLoading: true,
      error: null,
      selectedStatut: '',
      sortOrder: 'date',
      showAcceptModal: false,
      showRefuseModal: false,
      selectedCandidature: null,
      isProcessing: false,
      acceptForm: {
        commentaire: ''
      },
      refuseForm: {
        commentaire: ''
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
  async mounted() {
    const demandeId = this.$route.params.id;
    if (demandeId) {
      await this.loadDemandeAndCandidatures(demandeId);
    } else {
      this.error = 'ID de demande manquant';
      this.isLoading = false;
    }
  },
  methods: {
    async loadDemandeAndCandidatures(demandeId) {
      this.isLoading = true;
      this.error = null;

      try {
        const token = localStorage.getItem('token');

        const demandeResponse = await fetch(`/api/demandes-services/${demandeId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (demandeResponse.ok) {
          this.demandeService = await demandeResponse.json();
        } else {
          throw new Error('Erreur lors du chargement de la demande');
        }

        const candidaturesResponse = await fetch(`/api/candidatures/demande/${demandeId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (candidaturesResponse.ok) {
          this.candidatures = await candidaturesResponse.json();
          this.applyFilters();
        } else {
          throw new Error('Erreur lors du chargement des candidatures');
        }

      } catch (error) {
        console.error('Erreur:', error);
        this.error = error.message || 'Erreur lors du chargement';
      } finally {
        this.isLoading = false;
      }
    },

    applyFilters() {
      this.filteredCandidatures = this.candidatures.filter(candidature => {
        if (this.selectedStatut && candidature.statut !== this.selectedStatut) {
          return false;
        }
        return true;
      });

      this.filteredCandidatures.sort((a, b) => {
        switch (this.sortOrder) {
          case 'price_asc':
            return a.prixPropose - b.prixPropose;
          case 'price_desc':
            return b.prixPropose - a.prixPropose;
          case 'date':
          default:
            return new Date(b.dateCandidature) - new Date(a.dateCandidature);
        }
      });
    },

    getCategoryLabel(code) {
      const category = this.categories.find(c => c.code === code);
      return category ? category.libelle : code;
    },

    getBudgetRange(demande) {
      if (demande.budgetMin && demande.budgetMax) {
        return `${demande.budgetMin}€ - ${demande.budgetMax}€`;
      } else if (demande.budgetMin) {
        return `À partir de ${demande.budgetMin}€`;
      } else if (demande.budgetMax) {
        return `Maximum ${demande.budgetMax}€`;
      }
      return '';
    },

    getStatutLabel(statut) {
      const labels = {
        'EN_ATTENTE': 'En attente',
        'ACCEPTEE': 'Acceptée',
        'REFUSEE': 'Refusée'
      };
      return labels[statut] || statut;
    },

    getStatutClass(statut) {
      const classes = {
        'EN_ATTENTE': 'statut-attente',
        'ACCEPTEE': 'statut-acceptee',
        'REFUSEE': 'statut-refusee'
      };
      return classes[statut] || '';
    },

    getPriceComparison(prixPropose) {
      if (!this.demandeService?.budgetMax) return '';

      const budget = this.demandeService.budgetMax;
      const difference = ((prixPropose - budget) / budget) * 100;

      if (difference > 20) return 'Prix élevé';
      if (difference > 0) return 'Légèrement au-dessus';
      if (difference > -20) return 'Dans le budget';
      return 'Prix avantageux';
    },

    getPriceComparisonClass(prixPropose) {
      if (!this.demandeService?.budgetMax) return '';

      const budget = this.demandeService.budgetMax;
      const difference = ((prixPropose - budget) / budget) * 100;

      if (difference > 20) return 'price-high';
      if (difference > 0) return 'price-above';
      if (difference > -20) return 'price-good';
      return 'price-low';
    },

    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    },

    formatDateRelative(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const maintenant = new Date();
      const diffJours = Math.floor((maintenant - date) / (1000 * 60 * 60 * 24));

      if (diffJours === 0) return 'aujourd\'hui';
      if (diffJours === 1) return 'hier';
      if (diffJours < 7) return `il y a ${diffJours} jours`;

      return date.toLocaleDateString('fr-FR');
    },

    getEmptyMessage() {
      if (this.selectedStatut) {
        return `Aucune candidature ${this.getStatutLabel(this.selectedStatut).toLowerCase()}`;
      }
      return 'Aucune candidature reçue';
    },

    getEmptyDescription() {
      if (this.selectedStatut) {
        return 'Modifiez les filtres pour voir d\'autres candidatures.';
      }
      return 'Votre demande n\'a pas encore reçu de candidatures. Soyez patient, les prestataires vont bientôt répondre !';
    },

    accepterCandidature(candidature) {
      this.selectedCandidature = candidature;
      this.acceptForm.commentaire = '';
      this.showAcceptModal = true;
    },

    refuserCandidature(candidature) {
      this.selectedCandidature = candidature;
      this.refuseForm.commentaire = '';
      this.showRefuseModal = true;
    },

    async confirmerAcceptation() {
      if (!this.selectedCandidature) return;

      this.isProcessing = true;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/candidatures/${this.selectedCandidature.idCandidature}/accepter`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            commentaireClient: this.acceptForm.commentaire
          })
        });

        if (response.ok) {
          await this.loadDemandeAndCandidatures(this.$route.params.id);
          this.closeAcceptModal();
          alert('Candidature acceptée avec succès ! Un service a été créé automatiquement.');
        } else {
          const error = await response.json();
          alert('Erreur: ' + error.error);
        }

      } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur lors de l\'acceptation de la candidature');
      } finally {
        this.isProcessing = false;
      }
    },

    async confirmerRefus() {
      if (!this.selectedCandidature) return;

      this.isProcessing = true;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/candidatures/${this.selectedCandidature.idCandidature}/refuser`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            commentaireClient: this.refuseForm.commentaire
          })
        });

        if (response.ok) {
          await this.loadDemandeAndCandidatures(this.$route.params.id);
          this.closeRefuseModal();
          alert('Candidature refusée.');
        } else {
          const error = await response.json();
          alert('Erreur: ' + error.error);
        }

      } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur lors du refus de la candidature');
      } finally {
        this.isProcessing = false;
      }
    },

    closeAcceptModal() {
      this.showAcceptModal = false;
      this.selectedCandidature = null;
    },

    closeRefuseModal() {
      this.showRefuseModal = false;
      this.selectedCandidature = null;
    },

    voirProfilPrestataire(prestataire) {
      this.$router.push(`/prestataires/${prestataire.idUtilisateur}`);
    },

    contacterPrestataire(prestataire) {
      if (prestataire.telephone) {
        window.open(`tel:${prestataire.telephone}`);
      } else if (prestataire.email) {
        window.open(`mailto:${prestataire.email}`);
      }
    }
  },
  watch: {
    selectedStatut() {
      this.applyFilters();
    },
    sortOrder() {
      this.applyFilters();
    }
  }
}
</script>

<template>
  <div class="candidatures-recues-container">
    <div class="page-header">
      <h1>Candidatures reçues</h1>
      <div class="header-info">
        <span class="demande-title">{{ demandeService?.titre }}</span>
      </div>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i> Chargement...
    </div>

    <div v-else-if="error" class="error-message">
      <i class="fas fa-exclamation-triangle"></i> {{ error }}
    </div>

    <div v-else>
      <div v-if="demandeService" class="demande-summary">
        <div class="summary-content">
          <h3>{{ demandeService.titre }}</h3>
          <p class="description">{{ demandeService.description }}</p>
          <div class="demande-meta">
            <div class="meta-item">
              <i class="fas fa-tag"></i>
              <span>{{ getCategoryLabel(demandeService.categorieService) }}</span>
            </div>
            <div class="meta-item" v-if="demandeService.budgetMin || demandeService.budgetMax">
              <i class="fas fa-euro-sign"></i>
              <span>{{ getBudgetRange(demandeService) }}</span>
            </div>
            <div class="meta-item" v-if="demandeService.dateSouhaitee">
              <i class="fas fa-calendar"></i>
              <span>{{ formatDate(demandeService.dateSouhaitee) }}</span>
            </div>
          </div>
        </div>
        <div class="summary-stats">
          <div class="stat">
            <div class="stat-number">{{ candidatures.length }}</div>
            <div class="stat-label">Candidatures reçues</div>
          </div>
        </div>
      </div>

      <div class="filters-section">
        <div class="filter-group">
          <label for="statutFilter">Statut:</label>
          <select id="statutFilter" v-model="selectedStatut" @change="applyFilters" class="filter-select">
            <option value="">Toutes</option>
            <option value="EN_ATTENTE">En attente</option>
            <option value="ACCEPTEE">Acceptées</option>
            <option value="REFUSEE">Refusées</option>
          </select>
        </div>

        <div class="filter-group">
          <label for="sortOrder">Trier par:</label>
          <select id="sortOrder" v-model="sortOrder" @change="applyFilters" class="filter-select">
            <option value="date">Date de candidature</option>
            <option value="price_asc">Prix croissant</option>
            <option value="price_desc">Prix décroissant</option>
          </select>
        </div>
      </div>

      <div v-if="filteredCandidatures.length === 0" class="empty-state">
        <i class="fas fa-inbox"></i>
        <h3>{{ getEmptyMessage() }}</h3>
        <p>{{ getEmptyDescription() }}</p>
      </div>

      <div v-else class="candidatures-list">
        <div
          v-for="candidature in filteredCandidatures"
          :key="candidature.idCandidature"
          class="candidature-card"
          :class="getStatutClass(candidature.statut)"
        >
          <div class="candidature-header">
            <div class="prestataire-info">
              <div class="prestataire-avatar">
                <i class="fas fa-user"></i>
              </div>
              <div class="prestataire-details">
                <h3>{{ candidature.prestataire?.prenom }} {{ candidature.prestataire?.nom }}</h3>
                <div class="prestataire-meta">
                  <span class="specialite">{{ candidature.prestataire?.specialite || 'Prestataire' }}</span>
                  <span class="note" v-if="candidature.prestataire?.noteMoyenne">
                    <i class="fas fa-star"></i> {{ candidature.prestataire.noteMoyenne }}/5
                  </span>
                </div>
              </div>
            </div>
            <div class="candidature-status">
              <span class="status-badge" :class="getStatutClass(candidature.statut)">
                {{ getStatutLabel(candidature.statut) }}
              </span>
            </div>
          </div>

          <div class="candidature-content">
            <div class="price-section">
              <div class="price">
                <span class="price-amount">{{ candidature.prixPropose }}€</span>
                <span class="price-label">Prix proposé</span>
              </div>
              <div class="price-comparison" v-if="demandeService?.budgetMax">
                <span class="comparison" :class="getPriceComparisonClass(candidature.prixPropose)">
                  {{ getPriceComparison(candidature.prixPropose) }}
                </span>
              </div>
            </div>

            <div class="candidature-message" v-if="candidature.message">
              <h4>Message du prestataire :</h4>
              <p>{{ candidature.message }}</p>
            </div>

            <div class="candidature-meta">
              <div class="meta-item">
                <i class="fas fa-calendar"></i>
                <span>Candidature envoyée {{ formatDateRelative(candidature.dateCandidature) }}</span>
              </div>
              <div class="meta-item" v-if="candidature.delaiPropose">
                <i class="fas fa-clock"></i>
                <span>Délai proposé : {{ candidature.delaiPropose }} jours</span>
              </div>
              <div class="meta-item" v-if="candidature.prestataire?.nombreServices">
                <i class="fas fa-briefcase"></i>
                <span>{{ candidature.prestataire.nombreServices }} services réalisés</span>
              </div>
            </div>

            <div class="candidature-actions" v-if="candidature.statut === 'EN_ATTENTE'">
              <button @click="voirProfilPrestataire(candidature.prestataire)" class="btn btn-secondary">
                <i class="fas fa-user"></i> Voir profil
              </button>
              <button @click="contacterPrestataire(candidature.prestataire)" class="btn btn-info">
                <i class="fas fa-phone"></i> Contacter
              </button>
              <button @click="refuserCandidature(candidature)" class="btn btn-danger">
                <i class="fas fa-times"></i> Refuser
              </button>
              <button @click="accepterCandidature(candidature)" class="btn btn-success">
                <i class="fas fa-check"></i> Accepter
              </button>
            </div>

            <div v-if="candidature.commentaireClient" class="commentaire-section">
              <h4>Votre commentaire :</h4>
              <p>{{ candidature.commentaireClient }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showAcceptModal" class="modal-overlay" @click="closeAcceptModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Accepter la candidature</h3>
          <button @click="closeAcceptModal" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="modal-body">
          <p>Vous êtes sur le point d'accepter la candidature de :</p>
          <div class="candidature-recap" v-if="selectedCandidature">
            <strong>{{ selectedCandidature.prestataire?.prenom }} {{ selectedCandidature.prestataire?.nom }}</strong>
            <div>Prix : {{ selectedCandidature.prixPropose }}€</div>
          </div>

          <div class="form-group">
            <label for="commentaireAcceptation">Commentaire (optionnel) :</label>
            <textarea
              id="commentaireAcceptation"
              v-model="acceptForm.commentaire"
              placeholder="Votre message au prestataire..."
              class="form-textarea"
              rows="3"
            ></textarea>
          </div>

          <div class="warning-notice">
            <i class="fas fa-info-circle"></i>
            <span>En acceptant cette candidature, toutes les autres candidatures seront automatiquement refusées.</span>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeAcceptModal" class="btn btn-secondary">
            Annuler
          </button>
          <button
            @click="confirmerAcceptation"
            class="btn btn-success"
            :disabled="isProcessing"
          >
            {{ isProcessing ? 'Traitement...' : 'Confirmer l\'acceptation' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showRefuseModal" class="modal-overlay" @click="closeRefuseModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Refuser la candidature</h3>
          <button @click="closeRefuseModal" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="modal-body">
          <p>Vous êtes sur le point de refuser la candidature de :</p>
          <div class="candidature-recap" v-if="selectedCandidature">
            <strong>{{ selectedCandidature.prestataire?.prenom }} {{ selectedCandidature.prestataire?.nom }}</strong>
          </div>

          <div class="form-group">
            <label for="commentaireRefus">Motif du refus (optionnel) :</label>
            <textarea
              id="commentaireRefus"
              v-model="refuseForm.commentaire"
              placeholder="Expliquez pourquoi vous refusez cette candidature..."
              class="form-textarea"
              rows="3"
            ></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeRefuseModal" class="btn btn-secondary">
            Annuler
          </button>
          <button
            @click="confirmerRefus"
            class="btn btn-danger"
            :disabled="isProcessing"
          >
            {{ isProcessing ? 'Traitement...' : 'Confirmer le refus' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.candidatures-recues-container {
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

.demande-title {
  color: var(--text-secondary);
  font-style: italic;
}

.demande-summary {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.summary-content {
  flex: 1;
}

.summary-content h3 {
  color: var(--text-primary);
  margin: 0 0 1rem 0;
}

.description {
  color: var(--text-secondary);
  margin-bottom: 1rem;
  line-height: 1.5;
}

.demande-meta {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
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

.summary-stats {
  text-align: center;
}

.stat {
  padding: 1rem;
  background-color: var(--primary-color-light);
  border-radius: 8px;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: var(--primary-color);
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
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

.candidatures-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.candidature-card {
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: var(--shadow);
  padding: 1.5rem;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border-left: 4px solid var(--border-color);
}

.candidature-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.candidature-card.statut-attente {
  border-left-color: var(--warning-color);
}

.candidature-card.statut-acceptee {
  border-left-color: var(--success-color);
}

.candidature-card.statut-refusee {
  border-left-color: var(--error-color);
}

.candidature-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.prestataire-info {
  display: flex;
  align-items: center;
  gap: 1rem;
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

.prestataire-details h3 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
}

.prestataire-meta {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.specialite {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.note {
  color: var(--warning-color);
  font-size: 0.9rem;
  font-weight: 500;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.statut-attente {
  background-color: var(--warning-color-light);
  color: var(--warning-color);
}

.status-badge.statut-acceptee {
  background-color: var(--success-color-light);
  color: var(--success-color);
}

.status-badge.statut-refusee {
  background-color: var(--error-color-light);
  color: var(--error-color);
}

.candidature-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.price {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.price-amount {
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--success-color);
}

.price-label {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.price-comparison {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.comparison.price-low {
  background-color: var(--success-color-light);
  color: var(--success-color);
}

.comparison.price-good {
  background-color: var(--info-color-light);
  color: var(--info-color);
}

.comparison.price-above {
  background-color: var(--warning-color-light);
  color: var(--warning-color);
}

.comparison.price-high {
  background-color: var(--error-color-light);
  color: var(--error-color);
}

.candidature-message {
  padding: 1rem;
  background-color: var(--background-color);
  border-radius: 6px;
  border-left: 3px solid var(--primary-color);
}

.candidature-message h4 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
}

.candidature-message p {
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.5;
}

.candidature-meta {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.candidature-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.commentaire-section {
  padding: 1rem;
  background-color: var(--success-color-light);
  border-radius: 6px;
  border-left: 3px solid var(--success-color);
}

.commentaire-section h4 {
  color: var(--success-color);
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
}

.commentaire-section p {
  color: var(--text-primary);
  margin: 0;
  line-height: 1.5;
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

.btn-secondary {
  background-color: var(--border-color);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background-color: var(--text-secondary);
}

.btn-info {
  background-color: #17a2b8;
  color: white;
}

.btn-info:hover {
  background-color: #138496;
}

.btn-danger {
  background-color: var(--error-color);
  color: white;
}

.btn-danger:hover {
  background-color: var(--error-color-dark);
}

.btn-success {
  background-color: var(--success-color);
  color: white;
}

.btn-success:hover {
  background-color: var(--success-color-dark);
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

.candidature-recap {
  padding: 1rem;
  background-color: var(--background-color);
  border-radius: 6px;
  margin: 1rem 0;
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

.warning-notice {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  background-color: var(--warning-color-light);
  color: var(--warning-color);
  border-radius: 4px;
  font-size: 0.9rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid var(--border-color);
}
</style>
