<script>
export default {
  name: 'ServicesTerminesView',
  data() {
    return {
      servicesTermines: [],
      filteredServices: [],
      isLoading: true,
      error: null,
      selectedPeriod: 'all',
      selectedCategory: '',
      selectedRating: '',
      searchTerm: '',
      currentPage: 1,
      itemsPerPage: 8,
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
    chiffresAffaires() {
      return this.filteredServices.reduce((total, service) =>
        total + (service.montantFinal || service.prixAccorde || 0), 0
      );
    },
    notemoyenne() {
      const servicesAvecNote = this.filteredServices.filter(s => s.evaluation?.note);
      if (servicesAvecNote.length === 0) return '-';
      const total = servicesAvecNote.reduce((sum, s) => sum + s.evaluation.note, 0);
      return (total / servicesAvecNote.length).toFixed(1);
    },
    dureeeMoyenne() {
      if (this.filteredServices.length === 0) return 0;
      const totalDuree = this.filteredServices.reduce((total, service) => {
        return total + this.getDurationInDays(service.dateDebut, service.dateFin);
      }, 0);
      return Math.round(totalDuree / this.filteredServices.length);
    },
    paginatedServices() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.filteredServices.slice(start, end);
    },
    totalPages() {
      return Math.ceil(this.filteredServices.length / this.itemsPerPage);
    },
    last6Months() {
      // Simuler des données pour les 6 derniers mois
      return [
        { name: 'Jan', revenue: 450 },
        { name: 'Fév', revenue: 620 },
        { name: 'Mar', revenue: 380 },
        { name: 'Avr', revenue: 750 },
        { name: 'Mai', revenue: 900 },
        { name: 'Juin', revenue: 680 }
      ];
    },
    maxRevenue() {
      return Math.max(...this.last6Months.map(m => m.revenue));
    },
    servicesThisYear() {
      const thisYear = new Date().getFullYear();
      return this.servicesTermines.filter(s =>
        new Date(s.dateFin).getFullYear() === thisYear
      ).length;
    },
    clientsFideles() {
      // Simuler le nombre de clients fidèles
      return 12;
    },
    tauxSatisfaction() {
      const servicesAvecNote = this.servicesTermines.filter(s => s.evaluation?.note);
      if (servicesAvecNote.length === 0) return 0;
      const satisfaits = servicesAvecNote.filter(s => s.evaluation.note >= 4).length;
      return Math.round((satisfaits / servicesAvecNote.length) * 100);
    },
    tempsMoyen() {
      return this.dureeeMoyenne;
    }
  },
  async mounted() {
    await this.loadServices();
  },
  methods: {
    async loadServices() {
      this.isLoading = true;
      this.error = null;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/services/termines', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const services = await response.json();
          this.servicesTermines = services.map(service => ({
            id: service.idService,
            titre: service.demandeService?.titre || 'Service sans titre',
            description: service.demandeService?.description || '',
            categorieService: service.demandeService?.categorieService || '',
            prixAccorde: service.prixFinal,
            montantFinal: service.prixFinal,
            bonusQualite: 0, // Sera calculé plus tard si nécessaire
            dateDebut: service.dateDebut,
            dateFin: service.dateFin,
            adresse: service.adresseService || service.demandeService?.adresseDepart,
            respectDelai: service.dateFin && service.dateRealisationPrevue ?
              new Date(service.dateFin) <= new Date(service.dateRealisationPrevue) : true,
            tauxProgression: 100,
            isQuickService: this.isQuickService(service),
            client: {
              nom: service.client?.nom || '',
              prenom: service.client?.prenom || '',
              type: this.getClientType(service.client),
              telephone: service.client?.telephone || ''
            },
            evaluation: {
              note: null,
              commentaire: null
            }
          }));
        } else {
          const error = await response.json();
          this.error = error.error || 'Erreur lors du chargement des services';
        }

        this.applyFilters();

      } catch (error) {
        console.error('Erreur:', error);
        this.error = 'Erreur lors du chargement des services';
      } finally {
        this.isLoading = false;
      }
    },

    applyFilters() {
      this.filteredServices = this.servicesTermines.filter(service => {

        if (this.selectedPeriod !== 'all') {
          const dateFin = new Date(service.dateFin);
          const now = new Date();
          let dateLimit;

          switch (this.selectedPeriod) {
            case 'month':
              dateLimit = new Date(now.getFullYear(), now.getMonth(), 1);
              break;
            case 'quarter':
              dateLimit = new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3, 1);
              break;
            case 'year':
              dateLimit = new Date(now.getFullYear(), 0, 1);
              break;
          }

          if (dateFin < dateLimit) return false;
        }

        if (this.selectedCategory && service.categorieService !== this.selectedCategory) {
          return false;
        }

        if (this.selectedRating) {
          const minRating = parseInt(this.selectedRating);
          if (!service.evaluation?.note || service.evaluation.note < minRating) {
            return false;
          }
        }

        if (this.searchTerm) {
          const searchLower = this.searchTerm.toLowerCase();
          return service.titre.toLowerCase().includes(searchLower) ||
                 service.description.toLowerCase().includes(searchLower) ||
                 (service.client?.nom && service.client.nom.toLowerCase().includes(searchLower));
        }

        return true;
      });

      this.currentPage = 1;
    },

    getCategoryLabel(code) {
      const category = this.categories.find(c => c.code === code);
      return category ? category.libelle : code;
    },

    getClientTypeLabel(type) {
      const labels = {
        'particulier': 'Particulier',
        'entreprise': 'Entreprise',
        'association': 'Association'
      };
      return labels[type] || 'Client';
    },

    getRatingClass(note) {
      if (!note) return '';
      if (note >= 5) return 'rating-excellent';
      if (note >= 4) return 'rating-good';
      if (note >= 3) return 'rating-average';
      return 'rating-poor';
    },

    formatDateRange(dateDebut, dateFin) {
      const debut = new Date(dateDebut);
      const fin = new Date(dateFin);

      if (debut.toDateString() === fin.toDateString()) {
        return debut.toLocaleDateString('fr-FR');
      }

      return `${debut.toLocaleDateString('fr-FR')} - ${fin.toLocaleDateString('fr-FR')}`;
    },

    getDuration(dateDebut, dateFin) {
      const days = this.getDurationInDays(dateDebut, dateFin);
      if (days === 0) return 'Même jour';
      if (days === 1) return '1 jour';
      return `${days} jours`;
    },

    getDurationInDays(dateDebut, dateFin) {
      const debut = new Date(dateDebut);
      const fin = new Date(dateFin);
      return Math.ceil((fin - debut) / (1000 * 60 * 60 * 24));
    },

    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },

    getEmptyMessage() {
      if (this.selectedPeriod !== 'all' || this.selectedCategory || this.selectedRating || this.searchTerm) {
        return 'Aucun service ne correspond à vos critères de recherche.';
      }
      return 'Vous n\'avez pas encore terminé de service.';
    },

    getPageNumbers() {
      const pages = [];
      const maxVisible = 5;
      let start = Math.max(1, this.currentPage - Math.floor(maxVisible / 2));
      let end = Math.min(this.totalPages, start + maxVisible - 1);

      if (end - start + 1 < maxVisible) {
        start = Math.max(1, end - maxVisible + 1);
      }

      for (let i = start; i <= end; i++) {
        pages.push(i);
      }

      return pages;
    },

    voirDetails(service) {
      // TODO: Implémenter la vue détail
      this.$router.push(`/prestataire/services/${service.id}/details`);
    },

    contacterClient(service) {
      if (service.client?.telephone) {
        window.open(`tel:${service.client.telephone}`);
      }
    },

    proposerNouveauService(service) {
      // TODO: Implémenter la proposition de nouveau service
      alert(`Proposer un nouveau service à ${service.client?.nom} ${service.client?.prenom}`);
    },

    exportData() {
      // TODO: Implémenter l'export des données
      alert('Export des données en cours de développement');
    },

    isQuickService(service) {
      if (!service.dateDebut || !service.dateFin) return false;
      const debut = new Date(service.dateDebut);
      const fin = new Date(service.dateFin);
      const dureeHeures = (fin - debut) / (1000 * 60 * 60);
      return dureeHeures <= 24; // Service express si durée <= 24h
    },

    getClientType(client) {
      // Déduire le type de client basé sur les informations disponibles
      if (!client) return 'particulier';
      if (client.nom && !client.prenom) return 'entreprise';
      return 'particulier';
    }
  },
  watch: {
    selectedPeriod() {
      this.applyFilters();
    },
    searchTerm() {
      this.applyFilters();
    }
  }
}
</script>

<template>
  <div class="services-termines-container">
    <div class="page-header">
      <h1>Historique des services</h1>
      <div class="header-actions">
        <select v-model="selectedPeriod" @change="applyFilters" class="period-select">
          <option value="all">Toute la période</option>
          <option value="month">Ce mois</option>
          <option value="quarter">Ce trimestre</option>
          <option value="year">Cette année</option>
        </select>
        <button @click="exportData" class="btn btn-secondary">
          <i class="fas fa-download"></i> Exporter
        </button>
      </div>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i> Chargement...
    </div>

    <div v-else-if="error" class="error-message">
      <i class="fas fa-exclamation-triangle"></i> {{ error }}
    </div>

    <div v-else>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-check-circle"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ filteredServices.length }}</div>
            <div class="stat-label">Services terminés</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon revenue">
            <i class="fas fa-euro-sign"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ chiffresAffaires }}€</div>
            <div class="stat-label">Chiffre d'affaires</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon rating">
            <i class="fas fa-star"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ notemoyenne }}</div>
            <div class="stat-label">Note moyenne</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon time">
            <i class="fas fa-clock"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ dureeeMoyenne }}j</div>
            <div class="stat-label">Durée moyenne</div>
          </div>
        </div>
      </div>

      <!-- Graphique de performance (simulé) -->
      <div class="performance-chart">
        <h3>Évolution des revenus</h3>
        <div class="chart-placeholder">
          <div class="chart-bars">
            <div v-for="(month, index) in last6Months" :key="index" class="chart-bar">
              <div
                class="bar-fill"
                :style="{ height: `${(month.revenue / maxRevenue) * 100}%` }"
              ></div>
              <div class="bar-label">{{ month.name }}</div>
              <div class="bar-value">{{ month.revenue }}€</div>
            </div>
          </div>
        </div>
      </div>

      <div class="filters-section">
        <div class="filter-group">
          <label for="categoryFilter">Catégorie:</label>
          <select id="categoryFilter" v-model="selectedCategory" @change="applyFilters" class="filter-select">
            <option value="">Toutes les catégories</option>
            <option v-for="category in categories" :key="category.code" :value="category.code">
              {{ category.libelle }}
            </option>
          </select>
        </div>

        <div class="filter-group">
          <label for="ratingFilter">Note minimum:</label>
          <select id="ratingFilter" v-model="selectedRating" @change="applyFilters" class="filter-select">
            <option value="">Toutes les notes</option>
            <option value="5">5 étoiles</option>
            <option value="4">4+ étoiles</option>
            <option value="3">3+ étoiles</option>
          </select>
        </div>

        <div class="search-group">
          <input
            type="text"
            v-model="searchTerm"
            placeholder="Rechercher un service..."
            class="search-input"
          >
        </div>
      </div>

      <div v-if="filteredServices.length === 0" class="empty-state">
        <i class="fas fa-history"></i>
        <h3>Aucun service terminé</h3>
        <p>{{ getEmptyMessage() }}</p>
      </div>

      <div v-else class="services-list">
        <div
          v-for="service in paginatedServices"
          :key="service.id"
          class="service-card"
          :class="getRatingClass(service.evaluation?.note)"
        >
          <div class="service-header">
            <div class="service-title">
              <h3>{{ service.titre }}</h3>
              <div class="service-badges">
                <span class="category-badge">{{ getCategoryLabel(service.categorieService) }}</span>
                <span class="completed-badge">Terminé</span>
                <span v-if="service.isQuickService" class="quick-badge">Express</span>
              </div>
            </div>
            <div class="service-amount">
              {{ service.montantFinal || service.prixAccorde }}€
            </div>
          </div>

          <div class="service-meta">
            <div class="meta-item">
              <i class="fas fa-calendar-alt"></i>
              <span>{{ formatDateRange(service.dateDebut, service.dateFin) }}</span>
            </div>
            <div class="meta-item">
              <i class="fas fa-clock"></i>
              <span>Durée: {{ getDuration(service.dateDebut, service.dateFin) }}</span>
            </div>
            <div class="meta-item">
              <i class="fas fa-map-marker-alt"></i>
              <span>{{ service.adresse || 'Service à distance' }}</span>
            </div>
          </div>

          <div class="service-description">
            {{ truncateText(service.description, 100) }}
          </div>

          <div v-if="service.evaluation" class="service-evaluation">
            <div class="evaluation-header">
              <div class="stars">
                <i
                  v-for="n in 5"
                  :key="n"
                  class="fas fa-star"
                  :class="{ 'filled': n <= service.evaluation.note }"
                ></i>
              </div>
              <span class="rating-text">{{ service.evaluation.note }}/5</span>
            </div>
            <div v-if="service.evaluation.commentaire" class="evaluation-comment">
              "{{ service.evaluation.commentaire }}"
            </div>
          </div>

          <div class="service-client">
            <div class="client-info">
              <div class="client-avatar">
                <i class="fas fa-user"></i>
              </div>
              <div class="client-details">
                <div class="client-name">{{ service.client?.nom }} {{ service.client?.prenom }}</div>
                <div class="client-type">{{ getClientTypeLabel(service.client?.type) }}</div>
              </div>
            </div>
            <div class="service-status">
              <span class="status-badge success">
                <i class="fas fa-check"></i>
                Service terminé
              </span>
            </div>
          </div>

          <div class="service-actions">
            <button @click="voirDetails(service)" class="btn btn-secondary btn-sm">
              <i class="fas fa-eye"></i> Détails
            </button>
            <button @click="contacterClient(service)" class="btn btn-info btn-sm">
              <i class="fas fa-phone"></i> Contacter
            </button>
            <button @click="proposerNouveauService(service)" class="btn btn-primary btn-sm">
              <i class="fas fa-plus"></i> Nouveau service
            </button>
          </div>

          <div class="service-stats">
            <div class="stats-row">
              <div class="stat-item">
                <span class="stat-label">Taux de progression</span>
                <span class="stat-value">{{ service.tauxProgression || 100 }}%</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Respect délai</span>
                <span class="stat-value" :class="{ 'success': service.respectDelai, 'warning': !service.respectDelai }">
                  {{ service.respectDelai ? 'Oui' : 'Retard' }}
                </span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Bonus qualité</span>
                <span class="stat-value success" v-if="service.bonusQualite">
                  +{{ service.bonusQualite }}€
                </span>
                <span class="stat-value" v-else>-</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button
          @click="currentPage--"
          :disabled="currentPage <= 1"
          class="btn btn-secondary"
        >
          <i class="fas fa-chevron-left"></i> Précédent
        </button>

        <div class="page-numbers">
          <button
            v-for="page in getPageNumbers()"
            :key="page"
            @click="currentPage = page"
            class="page-btn"
            :class="{ 'active': page === currentPage }"
          >
            {{ page }}
          </button>
        </div>

        <button
          @click="currentPage++"
          :disabled="currentPage >= totalPages"
          class="btn btn-secondary"
        >
          Suivant <i class="fas fa-chevron-right"></i>
        </button>
      </div>

      <div class="performance-summary">
        <h3>Résumé de performance</h3>
        <div class="summary-grid">
          <div class="summary-item">
            <div class="summary-label">Services cette année</div>
            <div class="summary-value">{{ servicesThisYear }} <span class="trend up">+15%</span></div>
          </div>
          <div class="summary-item">
            <div class="summary-label">Clients fidèles</div>
            <div class="summary-value">{{ clientsFideles }} <span class="trend up">+8%</span></div>
          </div>
          <div class="summary-item">
            <div class="summary-label">Taux de satisfaction</div>
            <div class="summary-value">{{ tauxSatisfaction }}% <span class="trend up">+3%</span></div>
          </div>
          <div class="summary-item">
            <div class="summary-label">Temps moyen</div>
            <div class="summary-value">{{ tempsMoyen }}j <span class="trend down">-2j</span></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.services-termines-container {
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

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.period-select {
  padding: 0.5rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--background-color);
  color: var(--text-primary);
}

.stats-grid {
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
  background-color: var(--success-color);
  color: white;
  font-size: 1.2rem;
}

.stat-icon.revenue {
  background-color: var(--primary-color);
}

.stat-icon.rating {
  background-color: #ffc107;
}

.stat-icon.time {
  background-color: var(--info-color);
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

.performance-chart {
  background-color: var(--card-bg);
  padding: 2rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
  margin-bottom: 2rem;
}

.performance-chart h3 {
  color: var(--text-primary);
  margin: 0 0 1.5rem 0;
}

.chart-placeholder {
  height: 200px;
  position: relative;
}

.chart-bars {
  display: flex;
  height: 100%;
  align-items: flex-end;
  justify-content: space-around;
  gap: 1rem;
}

.chart-bar {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.bar-fill {
  width: 100%;
  max-width: 40px;
  background-color: var(--primary-color);
  border-radius: 4px 4px 0 0;
  min-height: 10px;
  transition: height 0.3s ease;
}

.bar-label {
  margin-top: 0.5rem;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.bar-value {
  font-size: 0.75rem;
  color: var(--text-primary);
  font-weight: 500;
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

.filter-group, .search-group {
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

.search-input {
  padding: 0.5rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  width: 200px;
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

.services-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.service-card {
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: var(--shadow);
  padding: 1.5rem;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border-left: 4px solid var(--success-color);
}

.service-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.service-card.rating-excellent {
  border-left-color: #28a745;
}

.service-card.rating-good {
  border-left-color: #17a2b8;
}

.service-card.rating-average {
  border-left-color: #ffc107;
}

.service-card.rating-poor {
  border-left-color: #dc3545;
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.service-title h3 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
}

.service-badges {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.category-badge, .completed-badge, .quick-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.category-badge {
  background-color: var(--primary-color-light);
  color: var(--primary-color);
}

.completed-badge {
  background-color: var(--success-color-light);
  color: var(--success-color);
}

.quick-badge {
  background-color: var(--warning-color-light);
  color: var(--warning-color);
}

.service-amount {
  font-size: 1.3rem;
  font-weight: bold;
  color: var(--success-color);
}

.service-meta {
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

.service-description {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.service-evaluation {
  background-color: var(--background-color);
  padding: 1rem;
  border-radius: 6px;
  margin-bottom: 1rem;
}

.evaluation-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.stars {
  color: #ffc107;
}

.stars i.filled {
  color: #ffc107;
}

.stars i:not(.filled) {
  color: var(--text-disabled);
}

.rating-text {
  font-weight: 600;
  color: var(--text-primary);
}

.evaluation-comment {
  font-style: italic;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.service-client {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: var(--background-color);
  border-radius: 6px;
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

.client-type {
  color: var(--text-secondary);
  font-size: 0.8rem;
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

.status-badge.success {
  background-color: var(--success-color-light);
  color: var(--success-color);
}

.service-actions {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.service-stats {
  border-top: 1px solid var(--border-color);
  padding-top: 1rem;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  text-align: center;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-bottom: 0.25rem;
}

.stat-value {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.stat-value.success {
  color: var(--success-color);
}

.stat-value.warning {
  color: var(--warning-color);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin: 2rem 0;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
}

.page-btn {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--border-color);
  background-color: var(--background-color);
  color: var(--text-primary);
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.page-btn:hover {
  background-color: var(--primary-color-light);
  color: var(--primary-color);
}

.page-btn.active {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.performance-summary {
  background-color: var(--card-bg);
  padding: 2rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.performance-summary h3 {
  color: var(--text-primary);
  margin: 0 0 1.5rem 0;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.summary-item {
  text-align: center;
}

.summary-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.summary-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.trend {
  font-size: 0.8rem;
  font-weight: 500;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
}

.trend.up {
  background-color: var(--success-color-light);
  color: var(--success-color);
}

.trend.down {
  background-color: var(--info-color-light);
  color: var(--info-color);
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

.btn-info {
  background-color: #17a2b8;
  color: white;
}

.btn-info:hover {
  background-color: #138496;
}
</style>
