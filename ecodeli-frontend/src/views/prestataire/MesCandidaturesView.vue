<template>
  <div class="mes-candidatures-container">
    <div class="page-header">
      <h1>Mes candidatures</h1>
      <router-link to="/prestataire/demandes-disponibles" class="btn btn-primary">
        <i class="fas fa-plus"></i> Nouvelle candidature
      </router-link>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i> Chargement...
    </div>

    <div v-else-if="error" class="error-message">
      <i class="fas fa-exclamation-triangle"></i> {{ error }}
    </div>

    <div v-else>
      <div class="stats-section">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-paper-plane"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ candidatures.length }}</div>
            <div class="stat-label">Total candidatures</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon pending">
            <i class="fas fa-clock"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ candidaturesEnAttente.length }}</div>
            <div class="stat-label">En attente</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon success">
            <i class="fas fa-check-circle"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ candidaturesAcceptees.length }}</div>
            <div class="stat-label">Acceptées</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon warning">
            <i class="fas fa-percentage"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ tauxReussite }}%</div>
            <div class="stat-label">Taux de réussite</div>
          </div>
        </div>
      </div>

      <div class="tabs-section">
        <div class="tabs">
          <button
            class="tab"
            :class="{ 'active': activeTab === 'all' }"
            @click="activeTab = 'all'"
          >
            Toutes ({{ candidatures.length }})
          </button>
          <button
            class="tab"
            :class="{ 'active': activeTab === 'EN_ATTENTE' }"
            @click="activeTab = 'EN_ATTENTE'"
          >
            En attente ({{ candidaturesEnAttente.length }})
          </button>
          <button
            class="tab"
            :class="{ 'active': activeTab === 'ACCEPTEE' }"
            @click="activeTab = 'ACCEPTEE'"
          >
            Acceptées ({{ candidaturesAcceptees.length }})
          </button>
          <button
            class="tab"
            :class="{ 'active': activeTab === 'REFUSEE' }"
            @click="activeTab = 'REFUSEE'"
          >
            Refusées ({{ candidaturesRefusees.length }})
          </button>
          <button
            class="tab"
            :class="{ 'active': activeTab === 'TERMINEES' }"
            @click="activeTab = 'TERMINEES'"
          >
            Terminées ({{ candidaturesTerminees.length }})
          </button>
        </div>
      </div>

      <div v-if="filteredCandidatures.length === 0" class="empty-state">
        <i class="fas fa-inbox"></i>
        <h3>{{ getEmptyStateTitle() }}</h3>
        <p>{{ getEmptyStateMessage() }}</p>
        <router-link to="/prestataire/demandes-disponibles" class="btn btn-primary" v-if="activeTab === 'all'">
          Découvrir des opportunités
        </router-link>
      </div>

      <div v-else class="candidatures-list">
        <div
          v-for="candidature in filteredCandidatures"
          :key="candidature.idCandidature"
          class="candidature-card"
          :class="getCardClass(candidature.statut)"
        >
          <div class="candidature-header">
            <div class="candidature-title">
              <h3>{{ candidature.demandeService?.titre || 'Demande sans titre' }}</h3>
              <div class="candidature-meta">
                <span class="category-badge">{{ getCategoryLabel(candidature.demandeService?.categorieService) }}</span>
                <span class="date-info">Candidaté {{ formatDateRelative(candidature.dateCandidature) }}</span>
              </div>
            </div>
            <div class="candidature-status">
              <span class="status-badge" :class="getStatutClass(candidature.statut)">
                <i :class="getStatutIcon(candidature.statut)"></i>
                {{ getStatutLabel(candidature.statut) }}
              </span>
              <div class="prix-propose" v-if="candidature.prixPropose">
                {{ candidature.prixPropose }}€
              </div>
            </div>
          </div>

          <div class="candidature-description" v-if="candidature.demandeService?.description">
            {{ truncateText(candidature.demandeService.description, 120) }}
          </div>

          <div class="candidature-details">
            <div class="detail-row" v-if="candidature.message">
              <div class="detail-label">Mon message :</div>
              <div class="detail-value">{{ truncateText(candidature.message, 100) }}</div>
            </div>

            <div class="detail-row" v-if="candidature.delaiPropose">
              <div class="detail-label">Délai proposé :</div>
              <div class="detail-value">{{ candidature.delaiPropose }} jour{{ candidature.delaiPropose > 1 ? 's' : '' }}</div>
            </div>

            <div class="detail-row" v-if="candidature.demandeService?.dateSouhaitee">
              <div class="detail-label">Date souhaitée :</div>
              <div class="detail-value">{{ formatDate(candidature.demandeService.dateSouhaitee) }}</div>
            </div>

            <div class="detail-row" v-if="candidature.commentaireClient">
              <div class="detail-label">Commentaire client :</div>
              <div class="detail-value client-comment">{{ candidature.commentaireClient }}</div>
            </div>
          </div>

          <div class="candidature-client">
            <div class="client-info">
              <div class="client-avatar">
                <i class="fas fa-user"></i>
              </div>
              <div class="client-details">
                <div class="client-name">{{ candidature.demandeService?.client?.prenom }} {{ candidature.demandeService?.client?.nom }}</div>
                <div class="client-location">{{ candidature.demandeService?.client?.ville || 'Localisation non précisée' }}</div>
              </div>
            </div>
            <div class="candidature-competition" v-if="candidature.demandeService?.nombreCandidatures">
              <i class="fas fa-users"></i>
              <span>{{ candidature.demandeService.nombreCandidatures }} candidature{{ candidature.demandeService.nombreCandidatures > 1 ? 's' : '' }}</span>
            </div>
          </div>

          <div class="candidature-actions">
            <button
              @click="voirDemande(candidature.demandeService)"
              class="btn btn-secondary btn-sm"
            >
              <i class="fas fa-eye"></i> Voir la demande
            </button>

            <button
              v-if="candidature.statut === 'ACCEPTEE'"
              @click="commencerService(candidature)"
              class="btn btn-success btn-sm"
              :disabled="isStartingService === candidature.idCandidature"
            >
              <i v-if="isStartingService === candidature.idCandidature" class="fas fa-spinner fa-spin"></i>
              <i v-else class="fas fa-play"></i>
              {{ isStartingService === candidature.idCandidature ? 'Démarrage...' : 'Commencer le service' }}
            </button>

            <button
              v-if="candidature.statut === 'EN_ATTENTE'"
              @click="retirerCandidature(candidature)"
              class="btn btn-danger btn-sm"
            >
              <i class="fas fa-times"></i> Retirer
            </button>

            <button
              v-if="candidature.statut === 'REFUSEE'"
              @click="voirRaison(candidature)"
              class="btn btn-info btn-sm"
            >
              <i class="fas fa-info-circle"></i> Voir raison
            </button>
          </div>

          <div v-if="candidature.statut === 'ACCEPTEE'" class="candidature-timeline">
            <div class="timeline-item completed">
              <div class="timeline-icon">
                <i class="fas fa-paper-plane"></i>
              </div>
              <div class="timeline-content">
                <div class="timeline-title">Candidature envoyée</div>
                <div class="timeline-date">{{ formatDate(candidature.dateCandidature) }}</div>
              </div>
            </div>
            <div class="timeline-item completed">
              <div class="timeline-icon">
                <i class="fas fa-check"></i>
              </div>
              <div class="timeline-content">
                <div class="timeline-title">Candidature acceptée</div>
                <div class="timeline-date">{{ formatDate(candidature.dateReponse) }}</div>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-icon">
                <i class="fas fa-tools"></i>
              </div>
              <div class="timeline-content">
                <div class="timeline-title">Service en cours</div>
                <div class="timeline-date">À commencer</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="filteredCandidatures.length > itemsPerPage" class="pagination">
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
  </div>
</template>

<script>
export default {
  name: 'MesCandidaturesView',
  data() {
    return {
      candidatures: [],
      candidaturesTerminees: [],
      isLoading: true,
      error: null,
      activeTab: 'all',
      currentPage: 1,
      itemsPerPage: 10,
      isStartingService: null,
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
    candidaturesEnAttente() {
      return this.candidatures.filter(c => c.statut === 'EN_ATTENTE');
    },
    candidaturesAcceptees() {
      return this.candidatures.filter(c => c.statut === 'ACCEPTEE');
    },
    candidaturesRefusees() {
      return this.candidatures.filter(c => c.statut === 'REFUSEE');
    },
    tauxReussite() {
      if (this.candidatures.length === 0) return 0;
      return Math.round((this.candidaturesAcceptees.length / this.candidatures.length) * 100);
    },
    filteredCandidatures() {
      let filtered;

      if (this.activeTab === 'TERMINEES') {
        filtered = this.candidaturesTerminees;
      } else if (this.activeTab === 'all') {
        filtered = this.candidatures;
      } else {
        filtered = this.candidatures.filter(c => c.statut === this.activeTab);
      }

      filtered = filtered.sort((a, b) => new Date(b.dateCandidature) - new Date(a.dateCandidature));

      return filtered;
    },
    totalPages() {
      return Math.ceil(this.filteredCandidatures.length / this.itemsPerPage);
    }
  },
  async mounted() {
    await this.loadCandidatures();
  },
  methods: {
    async loadCandidatures() {
      this.isLoading = true;
      this.error = null;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/candidatures/mes-candidatures', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          this.candidatures = await response.json();
        } else {
          const error = await response.json();
          this.error = error.error || 'Erreur lors du chargement des candidatures';
        }
      } catch (error) {
        console.error('Erreur:', error);
        this.error = 'Erreur de connexion';
      } finally {
        this.isLoading = false;
      }
    },

    async loadCandidaturesTerminees() {
      if (this.candidaturesTerminees.length > 0) {
        return;
      }

      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/candidatures/mes-candidatures/toutes', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const toutesLesCandidatures = await response.json();

          this.candidaturesTerminees = toutesLesCandidatures.filter(candidature => {
            return candidature.statut === 'ACCEPTEE';
          }).filter(candidature => {
            // Cette logique sera affinée côté backend,
            // mais pour l'instant on prend les candidatures acceptées qui ne sont pas dans la liste "active"
            return !this.candidatures.some(c => c.idCandidature === candidature.idCandidature);
          });
        } else {
          console.error('Erreur lors du chargement de l\'historique complet');
        }
      } catch (error) {
        console.error('Erreur:', error);
      }
    },

    getCategoryLabel(code) {
      const category = this.categories.find(c => c.code === code);
      return category ? category.libelle : code;
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
        'EN_ATTENTE': 'status-pending',
        'ACCEPTEE': 'status-success',
        'REFUSEE': 'status-error'
      };
      return classes[statut] || '';
    },

    getStatutIcon(statut) {
      const icons = {
        'EN_ATTENTE': 'fas fa-clock',
        'ACCEPTEE': 'fas fa-check-circle',
        'REFUSEE': 'fas fa-times-circle'
      };
      return icons[statut] || 'fas fa-question-circle';
    },

    getCardClass(statut) {
      return {
        'card-pending': statut === 'EN_ATTENTE',
        'card-success': statut === 'ACCEPTEE',
        'card-error': statut === 'REFUSEE'
      };
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

    getEmptyStateTitle() {
      const titles = {
        'all': 'Aucune candidature',
        'EN_ATTENTE': 'Aucune candidature en attente',
        'ACCEPTEE': 'Aucune candidature acceptée',
        'REFUSEE': 'Aucune candidature refusée'
      };
      return titles[this.activeTab] || 'Aucune candidature';
    },

    getEmptyStateMessage() {
      const messages = {
        'all': 'Vous n\'avez pas encore envoyé de candidature. Découvrez les opportunités disponibles !',
        'EN_ATTENTE': 'Toutes vos candidatures ont reçu une réponse.',
        'ACCEPTEE': 'Aucune de vos candidatures n\'a encore été acceptée.',
        'REFUSEE': 'Aucune de vos candidatures n\'a été refusée.'
      };
      return messages[this.activeTab] || '';
    },

    voirDemande(demandeService) {
      if (demandeService?.idDemande) {
        this.$router.push(`/prestataire/demandes/${demandeService.idDemande}`);
      }
    },

    async retirerCandidature(candidature) {
      if (!confirm('Êtes-vous sûr de vouloir retirer cette candidature ?')) {
        return;
      }

      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/candidatures/${candidature.idCandidature}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          await this.loadCandidatures();
        } else {
          const error = await response.json();
          alert('Erreur: ' + error.error);
        }
      } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur lors du retrait de la candidature');
      }
    },

    async commencerService(candidature) {
      this.isStartingService = candidature.idCandidature;

      try {
        const token = localStorage.getItem('token');

        const servicesResponse = await fetch('/api/services/mes-services', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!servicesResponse.ok) {
          throw new Error('Erreur lors de la récupération des services');
        }
        const services = await servicesResponse.json();

        const service = services.find(s =>
          s.candidature && s.candidature.idCandidature === candidature.idCandidature
        );

        if (!service) {
          alert('Service non trouvé. Le service devrait être créé automatiquement après l\'acceptation de la candidature.');
          return;
        }

        const commencerResponse = await fetch(`/api/services/${service.idService}/commencer`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });
        if (commencerResponse.ok) {
          alert('Service commencé avec succès !');
          this.$router.push('/prestataire/services-en-cours');
        } else {
          const error = await commencerResponse.json();
          alert('Erreur: ' + error.error);
        }

      } catch (error) {
        console.error('Erreur lors du démarrage du service:', error);
        alert('Erreur lors du démarrage du service');
      } finally {
        this.isStartingService = null;
      }
    },

    voirRaison(candidature) {
      if (candidature.commentaireClient) {
        alert('Raison du refus:\n' + candidature.commentaireClient);
      } else {
        alert('Aucune raison spécifique fournie par le client.');
      }
    }
  },
  watch: {
    async activeTab(newTab) {
      this.currentPage = 1;

      if (newTab === 'TERMINEES') {
        await this.loadCandidaturesTerminees();
      }
    }
  }
}
</script>

<style scoped>
.mes-candidatures-container {
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

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  font-size: 1.2rem;
}

.stat-icon.pending {
  background-color: var(--warning-color);
}

.stat-icon.success {
  background-color: var(--success-color);
}

.stat-icon.warning {
  background-color: #ffc107;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-top: 0.25rem;
}

.tabs-section {
  margin-bottom: 2rem;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  background-color: var(--card-bg);
  padding: 0.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.tab {
  background: none;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-secondary);
  font-weight: 500;
  transition: all 0.3s ease;
}

.tab:hover {
  background-color: var(--primary-color-light);
  color: var(--primary-color);
}

.tab.active {
  background-color: var(--primary-color);
  color: white;
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

.candidature-card.card-pending {
  border-left-color: var(--warning-color);
}

.candidature-card.card-success {
  border-left-color: var(--success-color);
}

.candidature-card.card-error {
  border-left-color: var(--error-color);
}

.candidature-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.candidature-title h3 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
}

.candidature-meta {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.category-badge {
  background-color: var(--primary-color-light);
  color: var(--primary-color);
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.date-info {
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.candidature-status {
  text-align: right;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.status-badge.status-pending {
  background-color: var(--warning-color-light);
  color: var(--warning-color);
}

.status-badge.status-success {
  background-color: var(--success-color-light);
  color: var(--success-color);
}

.status-badge.status-error {
  background-color: var(--error-color-light);
  color: var(--error-color);
}

.prix-propose {
  font-weight: 600;
  color: var(--success-color);
  font-size: 1.1rem;
}

.candidature-description {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.candidature-details {
  margin-bottom: 1rem;
}

.detail-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.detail-label {
  color: var(--text-secondary);
  font-weight: 500;
  min-width: 120px;
}

.detail-value {
  color: var(--text-primary);
  flex: 1;
}

.detail-value.client-comment {
  font-style: italic;
  color: var(--warning-color);
}

.candidature-client {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding: 1rem;
  background-color: var(--background-color);
  border-radius: 6px;
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

.candidature-competition {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.candidature-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
  flex-wrap: wrap;
}

.candidature-timeline {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.timeline-item {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  position: relative;
}

.timeline-item:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 20px;
  top: 40px;
  bottom: -16px;
  width: 2px;
  background-color: var(--border-color);
}

.timeline-item.completed::after {
  background-color: var(--success-color);
}

.timeline-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.9rem;
}

.timeline-item.completed .timeline-icon {
  background-color: var(--success-color);
}

.timeline-content {
  flex: 1;
  padding-top: 0.5rem;
}

.timeline-title {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.timeline-date {
  color: var(--text-secondary);
  font-size: 0.8rem;
  margin-top: 0.25rem;
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

.btn-success {
  background-color: var(--success-color);
  color: white;
}

.btn-success:hover {
  background-color: var(--success-color-dark);
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

@media (max-width: 768px) {
  .mes-candidatures-container {
    padding: 1rem;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .stats-section {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }
  
  .tabs {
    flex-direction: column;
  }
  
  .candidature-header {
    flex-direction: column;
    gap: 1rem;
  }
  
  .candidature-status {
    text-align: left;
  }
  
  .candidature-client {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .candidature-actions {
    flex-direction: column;
  }
}
</style>
