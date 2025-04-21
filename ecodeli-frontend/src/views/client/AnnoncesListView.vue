<script>
export default {
  name: 'ClientAnnoncesList',
  data() {
    return {
      annonces: [],
      user: null,
      isLoading: true,
      error: null
    }
  },
  computed: {
    statutLabels() {
      return {
        'PUBLIEE': { text: 'Publiée', class: 'status-published' },
        'EN_ATTENTE_VALIDATION': { text: 'En attente', class: 'status-pending' },
        'VALIDEE': { text: 'Validée', class: 'status-validated' },
        'EN_COURS': { text: 'En cours', class: 'status-in-progress' },
        'TERMINEE': { text: 'Terminée', class: 'status-completed' },
        'ANNULEE': { text: 'Annulée', class: 'status-cancelled' }
      }
    },
    canModifyAnnonce() {
      return (annonce) => {
        return annonce.statut === 'PUBLIEE';
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
        const response = await fetch(`/api/annonces/expediteur/${this.user.idUtilisateur}`, {
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

    async cancelAnnonce(id) {
      if (!confirm('Êtes-vous sûr de vouloir annuler cette annonce ?')) {
        return;
      }
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/annonces/${id}/annuler`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (!response.ok) {
          throw new Error('Erreur lors de l\'annulation de l\'annonce');
        }
        // Actualiser la liste des annonces
        this.fetchAnnonces();
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
            <h1>Mes Annonces</h1>
            <router-link to="/client/annonces/new" class="btn-create">
                <i class="fas fa-plus"></i> Créer une annonce
            </router-link>
        </div>
        <div v-if="isLoading" class="loading">
            Chargement des annonces...
        </div>
        <div v-else-if="error" class="error-message">
            {{ error }}
        </div>
        <div v-else-if="annonces.length === 0" class="empty-state">
            <i class="fas fa-bullhorn empty-icon"></i>
            <h3>Vous n'avez pas encore d'annonces</h3>
            <p>Créez votre première annonce pour proposer des services ou des livraisons</p>
            <router-link to="/client/annonces/new" class="btn-primary">
                Créer une annonce
            </router-link>
        </div>

        <div v-else class="annonces-list">
            <div v-for="annonce in annonces" :key="annonce.idAnnonce" class="annonce-card">
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
                <router-link :to="`/client/annonces/${annonce.idAnnonce}/edit`" class="btn-edit">
                    <i class="fas fa-edit"></i> Modifier
                </router-link>
                <button
                    v-if="annonce.statut === 'active'"
                    @click="cancelAnnonce(annonce.idAnnonce)"
                    class="btn-cancel">
                    <i class="fas fa-times"></i> Annuler
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

.btn-create {
  display: inline-flex;
  align-items: center;
  background-color: #4CAF50;
  color: white;
  padding: 0.6rem 1.2rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-create:hover {
  background-color: #45a049;
}

.btn-create i {
  margin-right: 0.5rem;
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

.status-pending {
  background-color: #fff3e0;
  color: #e65100;
}

.status-validated {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.status-in-progress {
  background-color: #e8eaf6;
  color: #3f51b5;
}

.status-completed {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.status-cancelled {
  background-color: #ffebee;
  color: #c62828;
}

.annonce-details {
  padding: 1.5rem;
}

.annonce-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item i {
  margin-right: 0.5rem;
  color: #666;
}

.annonce-actions {
  display: flex;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background-color: #f8f8f8;
  border-top: 1px solid #eee;
}

.btn-edit, .btn-cancel {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-edit {
  background-color: #f0f0f0;
  color: #333;
  text-decoration: none;
  border: none;
}

.btn-edit:hover {
  background-color: #e0e0e0;
}

.btn-cancel {
  background-color: #ffebee;
  color: #d32f2f;
  border: none;
}

.btn-cancel:hover {
  background-color: #ffcdd2;
}

.btn-edit i, .btn-cancel i {
  margin-right: 0.5rem;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  background-color: #f8f8f8;
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

.btn-primary {
  display: inline-block;
  background-color: #4CAF50;
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-primary:hover {
  background-color: #45a049;
}

.loading, .error-message {
  text-align: center;
  padding: 2rem;
}

.error-message {
  color: #e53935;
}
</style>