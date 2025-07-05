<script>
export default {
  name: 'ClientAnnoncesList',
  data() {
    return {
      annonces: [],
      user: null,
      isLoading: true,
      error: null,
      successMessage: null
    }
  },
  computed: {
    statutLabels() {
      return {
        'PUBLIEE': { text: 'Publiée', class: 'status-published' },
        'DRAFT': { text: 'En attente de paiement', class: 'status-draft' },
        'PAYEE': { text: 'Payée', class: 'status-paid' },
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
    },
    needsPayment() {
      return (annonce) => {
        return annonce.statut === 'PUBLIEE' && !annonce.payee;
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
        this.fetchAnnonces();
      } catch (err) {
        alert(err.message || 'Une erreur est survenue');
      }
    },

    formatDate(dateString) {
      if (!dateString) return 'Non défini';
      try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
          return 'Date invalide';
        }
        return new Intl.DateTimeFormat('fr-FR', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        }).format(date);
      } catch (error) {
        console.warn('Erreur lors du formatage de la date:', dateString, error);
        return 'Format invalide';
      }
    },
    getColisDetails(annonce) {
      if (!annonce.colis)
        return 'Non spécifié';

      const dimensions = `${annonce.colis.longueur || 0}×${annonce.colis.largeur || 0}×${annonce.colis.hauteur || 0} cm`;
      const poids = `${annonce.colis.poids || 0} kg`;

      return `${dimensions}, ${poids}${annonce.colis.fragile ? ', Fragile' : ''}`;
    },

    payAnnonce(annonce) {
      this.$router.push(`/client/annonces/${annonce.idAnnonce}/payment`);
    }
  },
  mounted() {
    this.fetchAnnonces();

    if (this.$route.query.paymentSuccess === 'true') {
      this.successMessage = 'Paiement effectué avec succès ! Votre annonce est maintenant visible par les livreurs.';

      setTimeout(() => {
        this.successMessage = null;
        this.$router.replace({ query: {} });
      }, 5000);
    }
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
        <div v-if="successMessage" class="success-message">
            <i class="fas fa-check-circle"></i>
            {{ successMessage }}
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
                    <h3>
                        <router-link :to="`/client/annonces/${annonce.idAnnonce}`" class="annonce-title-link">
                            {{ annonce.titre }}
                        </router-link>
                    </h3>
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
                        <div class="info-item" v-if="annonce.colis">
                            <i class="fas fa-box"></i>
                            <span>{{ getColisDetails(annonce) }}</span>
                        </div>
                    </div>
                </div>

                <div class="annonce-actions">
                    <!-- Bouton de paiement prioritaire pour les annonces non payées -->
                    <button
                        v-if="needsPayment(annonce)"
                        @click="payAnnonce(annonce)"
                        class="btn-pay">
                        <i class="fas fa-credit-card"></i> Payer maintenant
                    </button>

                    <router-link :to="`/client/annonces/${annonce.idAnnonce}`" class="btn-view">
                        <i class="fas fa-eye"></i> Voir détails
                    </router-link>
                    <router-link
                        v-if="canModifyAnnonce(annonce)"
                        :to="`/client/annonces/${annonce.idAnnonce}/edit`"
                        class="btn-edit">
                        <i class="fas fa-edit"></i> Modifier
                    </router-link>
                    <button
                        v-if="canModifyAnnonce(annonce)"
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

.annonces-header h1 {
  color: var(--text-primary);
}

.btn-create {
  display: inline-flex;
  align-items: center;
  background-color: var(--primary-color);
  color: white;
  padding: 0.6rem 1.2rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-create:hover {
  background-color: var(--primary-hover);
}

.btn-create i {
  margin-right: 0.5rem;
}

.annonces-list {
  display: grid;
  gap: 1.5rem;
}

.annonce-card {
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: var(--shadow);
  overflow: hidden;
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

.annonce-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background-color: var(--card-header-bg);
  border-bottom: 1px solid var(--border-color);
}

.annonce-title-link {
  color: var(--text-primary);
  text-decoration: none;
  transition: color 0.3s;
}

.annonce-title-link:hover {
  color: var(--primary-color);
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

.status-draft {
  background-color: #fff8e1;
  color: #f57c00;
}

.status-paid {
  background-color: #e8f5e9;
  color: #4caf50;
}

.annonce-details {
  padding: 1.5rem;
}

.annonce-details p {
  color: var(--text-primary);
  margin-bottom: 1rem;
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
  color: var(--text-secondary);
  width: 18px;
  text-align: center;
}

.info-item span {
  color: var(--text-primary);
}

.annonce-actions {
  display: flex;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background-color: var(--card-header-bg);
  border-top: 1px solid var(--border-color);
}

.btn-view, .btn-edit, .btn-cancel, .btn-pay {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.btn-view {
  background-color: #e3f2fd;
  color: #1976d2;
  text-decoration: none;
  border: none;
}

.btn-view:hover {
  background-color: #bbdefb;
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

.btn-pay {
  background-color: #4caf50;
  color: white;
  font-weight: 600;
}

.btn-pay:hover {
  background-color: #45a049;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(76, 175, 80, 0.3);
}

.btn-view i, .btn-edit i, .btn-cancel i, .btn-pay i {
  margin-right: 0.5rem;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  background-color: var(--card-bg);
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.empty-icon {
  font-size: 3rem;
  color: var(--text-disabled);
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin-bottom: 1rem;
  color: var(--text-secondary);
}

.empty-state p {
  margin-bottom: 1.5rem;
  color: var(--text-secondary);
}

.btn-primary {
  display: inline-block;
  background-color: var(--primary-color);
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-primary:hover {
  background-color: var(--primary-hover);
}

.loading, .error-message {
  text-align: center;
  padding: 2rem;
  color: var(--text-primary);
}

.error-message {
  color: var(--error-color);
}

.success-message {
  background-color: #e8f5e9;
  color: #2e7d32;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  border: 1px solid #4caf50;
  animation: slideIn 0.3s ease-out;
}

.success-message i {
  margin-right: 0.75rem;
  font-size: 1.2rem;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .annonce-actions {
    flex-direction: column;
    gap: 0.5rem;
  }

  .btn-view, .btn-edit, .btn-cancel {
    width: 100%;
    justify-content: center;
  }
}
</style>
