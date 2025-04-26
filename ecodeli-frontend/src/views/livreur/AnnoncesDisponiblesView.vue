<script>
export default {
  name: 'AnnoncesDisponiblesView',
  data() {
    return {
      annonces: [],
      user: null,
      isLoading: true,
      error: null,
      filtres: {
        adresse: '',
        minPrix: '',
        maxPrix: ''
      }
    }
  },
  computed: {
    annoncesFiltrees() {
      return this.annonces;
    },
    statutLabels() {
      return {
        'PUBLIEE': { text: 'Disponible', class: 'status-published' }
      }
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
        const response = await fetch('/api/annonces/statut/PUBLIEE', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (!response.ok) {
          throw new Error('Erreur lors de la récupération des annonces');
        }
        this.annonces = await response.json();
      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
        console.error('Erreur:', err);
      } finally {
        this.isLoading = false;
      }
    },

    async demanderValidation(idAnnonce) {
      if (!confirm('Êtes-vous sûr de vouloir prendre en charge cette livraison?')) {
        return;
      }
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
        alert('Vous avez pris en charge cette livraison avec succès!');
        this.fetchAnnonces();
      } catch (err) {
        alert(err.message || 'Une erreur est survenue');
        console.error('Erreur:', err);
      }
    },
    appliquerFiltres() {
      this.fetchAnnonces();
    },
    resetFiltres() {
      this.filtres = {
        adresse: '',
        minPrix: '',
        maxPrix: ''
      };
      this.fetchAnnonces();
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
      <div class="filters-form">
        <div class="form-group">
          <label for="adresse">Adresse</label>
          <input
            id="adresse"
            v-model="filtres.adresse"
            type="text"
            placeholder="Ville ou code postal"
          >
        </div>
        <div class="form-group price-range">
          <label>Prix (€)</label>
          <div class="price-inputs">
            <input
              v-model="filtres.minPrix"
              type="number"
              min="0"
              step="0.01"
              placeholder="Min"
            >
            <span class="separator">-</span>
            <input
              v-model="filtres.maxPrix"
              type="number"
              min="0"
              step="0.01"
              placeholder="Max"
            >
          </div>
        </div>
        <div class="filters-actions">
          <button @click="appliquerFiltres" class="btn-filter">
            <i class="fas fa-search"></i> Filtrer
          </button>
          <button @click="resetFiltres" class="btn-reset">
            <i class="fas fa-times"></i> Réinitialiser
          </button>
        </div>
      </div>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i>
      <p>Chargement des annonces...</p>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else-if="annoncesFiltrees.length === 0" class="empty-state">
      <i class="fas fa-search empty-icon"></i>
      <h3>Aucune annonce disponible</h3>
      <p>Il n'y a actuellement aucune annonce correspondant à vos critères.</p>
    </div>

    <div v-else class="annonces-list">
      <div v-for="annonce in annoncesFiltrees" :key="annonce.idAnnonce" class="annonce-card">
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
            <div class="info-item" v-if="annonce.colis">
              <i class="fas fa-box"></i>
              <span>{{ annonce.colis.poids }} kg - {{ annonce.colis.longueur }}×{{ annonce.colis.largeur }}×{{ annonce.colis.hauteur }} cm</span>
            </div>
            <div class="info-item" v-if="annonce.colis && annonce.colis.fragile">
              <i class="fas fa-glass-whiskey"></i>
              <span>Colis fragile</span>
            </div>
          </div>

          <div class="contact-info">
            <div class="contact-item">
              <i class="fas fa-user"></i>
              <div>
                <h4>Expéditeur</h4>
                <p>{{ annonce.expediteur.prenom }} {{ annonce.expediteur.nom }}</p>
              </div>
            </div>
            <div class="contact-item">
              <i class="fas fa-user-friends"></i>
              <div>
                <h4>Destinataire</h4>
                <p>{{ annonce.destinataire.prenom }} {{ annonce.destinataire.nom }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="annonce-actions">
          <button
            @click="demanderValidation(annonce.idAnnonce)"
            class="btn-action btn-primary">
            <i class="fas fa-check"></i> Prendre en charge
          </button>
        </div>
      </div>
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

.filters-container {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.filters-form {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  align-items: flex-end;
}

.form-group {
  flex: 1;
  min-width: 200px;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.price-range {
  flex: 1;
  min-width: 200px;
}

.price-inputs {
  display: flex;
  align-items: center;
}

.price-inputs input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.separator {
  margin: 0 0.5rem;
  color: #666;
}

.filters-actions {
  display: flex;
  gap: 1rem;
}

.btn-filter, .btn-reset {
  padding: 0.8rem 1.2rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-filter {
  background-color: #4CAF50;
  color: white;
}

.btn-filter:hover {
  background-color: #45a049;
}

.btn-reset {
  background-color: #f1f1f1;
  color: #666;
}

.btn-reset:hover {
  background-color: #e0e0e0;
}

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

.status-published {
  background-color: #e3f2fd;
  color: #1976d2;
}

.annonce-details {
  padding: 1.5rem;
}

.annonce-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
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

.contact-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.contact-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
}

.contact-item i {
  font-size: 1.2rem;
  color: #3F51B5;
}

.contact-item h4 {
  margin-bottom: 0.5rem;
  color: #666;
  font-weight: 500;
}

.annonce-actions {
  display: flex;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background-color: #f8f8f8;
  border-top: 1px solid #eee;
}

.btn-action {
  padding: 0.8rem 1.5rem;
  border-radius: 4px;
  border: none;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #3F51B5;
  color: white;
}

.btn-primary:hover {
  background-color: #303f9f;
}

.btn-primary i {
  margin-right: 0.5rem;
}

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
}

@media (max-width: 768px) {
  .annonces-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .filters-form {
    flex-direction: column;
  }

  .contact-info {
    grid-template-columns: 1fr;
  }
}
</style>