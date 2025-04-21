<script>
export default {
  name: 'AnnoncesDisponiblesView',
  data() {
    return {
      annonces: [],
      user: null,
      isLoading: true,
      error: null,
      searchVilleDepart: '',
      searchVilleArrivee: '',
      searchPrixMin: '',
      searchPrixMax: '',
      currentPage: 1,
      itemsPerPage: 6,
      sortBy: 'dateDebut',
      sortOrder: 'asc'
    }
  },
  computed: {
    statutLabels() {
      return {
        'PUBLIEE': { text: 'Publiée', class: 'status-published' }
      }
    },
    filteredAnnonces() {
      let result = [...this.annonces];
      if (this.searchVilleDepart) {
        const search = this.searchVilleDepart.toLowerCase();
        result = result.filter(annonce => annonce.adresseDepart && annonce.adresseDepart.toLowerCase().includes(search)
        );
      }
      if (this.searchVilleArrivee) {
        const search = this.searchVilleArrivee.toLowerCase();
        result = result.filter(annonce => annonce.adresseFin && annonce.adresseFin.toLowerCase().includes(search)
        );
      }

      if (this.searchPrixMin) {
        const min = parseFloat(this.searchPrixMin);
        result = result.filter(annonce => annonce.prixUnitaire >= min);
      }
      if (this.searchPrixMax) {
        const max = parseFloat(this.searchPrixMax);
        result = result.filter(annonce => annonce.prixUnitaire <= max);
      }

      result.sort((a, b) => {
        let comparison = 0;
        if (this.sortBy === 'dateDebut') {
          comparison = new Date(a.dateDebut) - new Date(b.dateDebut);
        } else if (this.sortBy === 'prixUnitaire') {
          comparison = a.prixUnitaire - b.prixUnitaire;
        }
        return this.sortOrder === 'asc' ? comparison : -comparison;
      });
      return result;
    },
    paginatedAnnonces() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.filteredAnnonces.slice(start, end);
    },
    totalPages() {
      return Math.ceil(this.filteredAnnonces.length / this.itemsPerPage);
    }
  },
  methods: {
    async fetchAnnonces() {
      this.isLoading = true;
      this.error = null;

      try {
        const userStr = localStorage.getItem('user');
        if (!userStr) {
          this.$router.push('/login');
          return;
        }
        this.user = JSON.parse(userStr);
        const token = localStorage.getItem('token');
        if (!token) {
          this.$router.push('/login');
          return;
        }
        const response = await fetch(`/api/annonces/statut/PUBLIEE`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (!response.ok) {
          throw new Error('Erreur lors de la récupération des annonces');
        }
        this.annonces = await response.json();
        try {
          const responsePubliee = await fetch(`/api/annonces/statut/PUBLIÉE`, {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          if (responsePubliee.ok) {
            const annoncesPubliees = await responsePubliee.json();
            this.annonces = [...this.annonces, ...annoncesPubliees];
          }
        } catch (err) {
          console.warn('Impossible de récupérer les annonces avec statut PUBLIÉE:', err);
        }
      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
        console.error('Erreur:', err);
      } finally {
        this.isLoading = false;
      }
    },
    async postuleAnnonce(idAnnonce) {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/annonces/${idAnnonce}/demande-validation?idLivreur=${this.user.idUtilisateur}`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData || 'Erreur lors de la prise en charge de l\'annonce');
        }
        this.fetchAnnonces();
        alert('Vous avez été assigné à cette annonce avec succès! Vous pouvez maintenant la voir dans "Mes livraisons".');
        this.$router.push('/livreur/mes-livraisons');
      } catch (err) {
        alert(err.message || 'Une erreur est survenue');
        console.error('Erreur:', err);
      }
    },
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }).format(date);
    },

    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    changeSort(field) {
      if (this.sortBy === field) {
        this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
      } else {
        this.sortBy = field;
        this.sortOrder = 'asc';
      }
    },
    resetFilters() {
      this.searchVilleDepart = '';
      this.searchVilleArrivee = '';
      this.searchPrixMin = '';
      this.searchPrixMax = '';
      this.currentPage = 1;
    }
  },
  mounted() {
    this.fetchAnnonces();
  }
}
</script>

<template>
  <div class="annonces-container">
    <div class="annonces-header">
      <h1>Annonces disponibles</h1>
      <router-link to="/livreur" class="btn-back">
        <i class="fas fa-arrow-left"></i> Retour au dashboard
      </router-link>
    </div>

    <div class="filters-container">
      <div class="filter-group">
        <label for="villeDepart">Ville de départ</label>
        <input type="text" id="villeDepart" v-model="searchVilleDepart" placeholder="Ex: Paris">
      </div>
      <div class="filter-group">
        <label for="villeArrivee">Ville d'arrivée</label>
        <input type="text" id="villeArrivee" v-model="searchVilleArrivee" placeholder="Ex: Lyon">
      </div>
      <div class="filter-group">
        <label for="prixMin">Prix min.</label>
        <input type="number" id="prixMin" v-model="searchPrixMin" placeholder="Min €">
      </div>
      <div class="filter-group">
        <label for="prixMax">Prix max.</label>
        <input type="number" id="prixMax" v-model="searchPrixMax" placeholder="Max €">
      </div>
      <button @click="resetFilters" class="btn-reset">
        <i class="fas fa-times"></i> Réinitialiser
      </button>
    </div>

    <div class="sort-container">
      <span>Trier par:</span>
      <button
        @click="changeSort('dateDebut')"
        class="btn-sort"
        :class="{ active: sortBy === 'dateDebut' }">
        Date
        <i v-if="sortBy === 'dateDebut'" :class="sortOrder === 'asc' ? 'fas fa-sort-up' : 'fas fa-sort-down'"></i>
      </button>
      <button
        @click="changeSort('prixUnitaire')"
        class="btn-sort"
        :class="{ active: sortBy === 'prixUnitaire' }">
        Prix
        <i v-if="sortBy === 'prixUnitaire'" :class="sortOrder === 'asc' ? 'fas fa-sort-up' : 'fas fa-sort-down'"></i>
      </button>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i>
      <p>Chargement des annonces...</p>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else-if="filteredAnnonces.length === 0" class="empty-state">
      <i class="fas fa-search empty-icon"></i>
      <h3>Aucune annonce disponible</h3>
      <p v-if="annonces.length === 0">
        Il n'y a actuellement aucune annonce active dans le système.
      </p>
      <p v-else>
        Aucune annonce ne correspond à vos critères de recherche.
      </p>
      <button @click="resetFilters" class="btn-primary">
        Réinitialiser les filtres
      </button>
    </div>

    <div v-else class="annonces-list">
      <div v-for="annonce in paginatedAnnonces" :key="annonce.idAnnonce" class="annonce-card">
        <div class="annonce-header">
          <h3>{{ annonce.titre }}</h3>
          <span class="status-badge" :class="statutLabels[annonce.statut]?.class">
            {{ statutLabels[annonce.statut]?.text || annonce.statut }}
          </span>
        </div>

        <div class="annonce-details">
          <p>{{ annonce.description }}</p>
          <div class="annonce-info">
            <div class="info-item">
              <i class="fas fa-map-marker-alt"></i>
              <span>De: {{ annonce.adresseDepart }}</span>
            </div>
            <div class="info-item">
              <i class="fas fa-map-marker"></i>
              <span>À: {{ annonce.adresseFin }}</span>
            </div>
            <div class="info-item">
              <i class="fas fa-calendar"></i>
              <span>Du {{ formatDate(annonce.dateDebut) }} au {{ formatDate(annonce.dateFin) }}</span>
            </div>
            <div class="info-item">
              <i class="fas fa-euro-sign"></i>
              <span>{{ annonce.prixUnitaire }} €</span>
            </div>
            <div class="info-item">
              <i class="fas fa-tag"></i>
              <span>{{ annonce.typeAnnonce }}</span>
            </div>
          </div>
        </div>

        <div class="annonce-actions">
          <button @click="postuleAnnonce(annonce.idAnnonce)" class="btn-postule">
            <i class="fas fa-check-circle"></i> Postuler
          </button>
          <button class="btn-details">
            <i class="fas fa-info-circle"></i> Détails
          </button>
        </div>
      </div>
    </div>

    <div v-if="totalPages > 1" class="pagination">
      <button
        @click="changePage(currentPage - 1)"
        :disabled="currentPage === 1"
        class="pagination-btn"
      >
        <i class="fas fa-chevron-left"></i>
      </button>

      <button
        v-for="page in totalPages"
        :key="page"
        @click="changePage(page)"
        class="pagination-btn"
        :class="{ active: currentPage === page }"
      >
        {{ page }}
      </button>

      <button
        @click="changePage(currentPage + 1)"
        :disabled="currentPage === totalPages"
        class="pagination-btn"
      >
        <i class="fas fa-chevron-right"></i>
      </button>
    </div>
  </div>
</template>

<style scoped>
.annonces-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.annonces-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  background-color: #f5f5f5;
  color: #333;
  padding: 0.6rem 1.2rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background-color: #e0e0e0;
}

.btn-back i {
  margin-right: 0.5rem;
}

/* Filtres */
.filters-container {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 150px;
}

.filter-group label {
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  font-weight: 500;
  color: #666;
}

.filter-group input {
  padding: 0.7rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
}

.btn-reset {
  align-self: flex-end;
  display: inline-flex;
  align-items: center;
  background-color: #f1f1f1;
  color: #666;
  border: none;
  padding: 0.7rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.2s;
}

.btn-reset:hover {
  background-color: #e0e0e0;
}

.btn-reset i {
  margin-right: 0.5rem;
}

/* Tri */
.sort-container {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.sort-container span {
  font-size: 0.9rem;
  color: #666;
}

.btn-sort {
  background: none;
  border: 1px solid #ddd;
  padding: 0.5rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-sort:hover {
  background-color: #f5f5f5;
}

.btn-sort.active {
  background-color: #3F51B5;
  color: white;
  border-color: #3F51B5;
}

/* Liste des annonces */
.annonces-list {
  display: grid;
  gap: 1.5rem;
}

.annonce-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  overflow: hidden;
}

.annonce-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}

.status-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-active, .status-published {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.annonce-details {
  padding: 1.5rem;
}

.annonce-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 6px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item i {
  margin-right: 0.5rem;
  color: #666;
  width: 18px;
  text-align: center;
}

.annonce-actions {
  display: flex;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background-color: #f8f8f8;
  border-top: 1px solid #eee;
}

.btn-postule, .btn-details {
  padding: 0.7rem 1.2rem;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.btn-postule {
  background-color: #3F51B5;
  color: white;
  flex: 1;
  justify-content: center;
}

.btn-postule:hover {
  background-color: #303f9f;
}

.btn-details {
  background-color: #f0f0f0;
  color: #333;
}

.btn-details:hover {
  background-color: #e0e0e0;
}

.btn-postule i, .btn-details i {
  margin-right: 0.5rem;
}

/* États vides et chargement */
.loading, .error-message, .empty-state {
  text-align: center;
  padding: 3rem;
}

.loading i {
  font-size: 2rem;
  color: #3F51B5;
  margin-bottom: 1rem;
}

.error-message {
  color: #e53935;
  background-color: #ffebee;
  border-radius: 8px;
}

.empty-state {
  background-color: #f5f5f5;
  border-radius: 8px;
}

.empty-icon {
  font-size: 3rem;
  color: #bdbdbd;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin-bottom: 1rem;
  color: #616161;
}

.empty-state p {
  margin-bottom: 1.5rem;
  color: #757575;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.btn-primary {
  display: inline-block;
  background-color: #3F51B5;
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.3s;
  border: none;
  cursor: pointer;
}

.btn-primary:hover {
  background-color: #303f9f;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 2rem;
}

.pagination-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #f5f5f5;
}

.pagination-btn.active {
  background-color: #3F51B5;
  color: white;
  border-color: #3F51B5;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .annonces-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .filters-container {
    flex-direction: column;
  }
  
  .sort-container {
    flex-wrap: wrap;
  }
}
</style>