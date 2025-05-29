<script>
import DeliveryMap from '@/components/DeliveryMap.vue';

export default {
  name: 'AnnonceDetailView',
  components: {
    DeliveryMap
  },
  data() {
    return {
      annonce: null,
      isLoading: true,
      error: null,
      user: null
    }
  },
  computed: {
    statutLabel() {
      const labels = {
        'PUBLIEE': { text: 'Publiée', class: 'status-published' },
        'EN_ATTENTE_VALIDATION': { text: 'En attente', class: 'status-pending' },
        'VALIDEE': { text: 'Validée', class: 'status-validated' },
        'EN_COURS': { text: 'En cours', class: 'status-in-progress' },
        'TERMINEE': { text: 'Terminée', class: 'status-completed' },
        'ANNULEE': { text: 'Annulée', class: 'status-cancelled' }
      };
      return this.annonce && this.annonce.statut ? labels[this.annonce.statut] : null;
    },
    canEdit() {
      return this.user && this.annonce && this.user.idUtilisateur === this.annonce.expediteur.idUtilisateur && this.annonce.statut === 'PUBLIEE';
    }
  },
  methods: {
    async fetchAnnonce() {
      this.isLoading = true;
      this.error = null;

      try {
        const annonceId = this.$route.params.id;
        if (!annonceId) {
          throw new Error('ID d\'annonce non spécifié');
        }

        const token = localStorage.getItem('token');
        if (!token) {
          this.$router.push('/login');
          return;
        }

        const response = await fetch(`/api/annonces/${annonceId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error('Erreur lors de la récupération de l\'annonce');
        }

        this.annonce = await response.json();
      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
      } finally {
        this.isLoading = false;
      }
    },

    async cancelAnnonce() {
      if (!confirm('Êtes-vous sûr de vouloir annuler cette annonce ?')) {
        return;
      }

      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/annonces/${this.annonce.idAnnonce}/annuler`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error('Erreur lors de l\'annulation de l\'annonce');
        }

        this.fetchAnnonce();
      } catch (err) {
        alert(err.message || 'Une erreur est survenue');
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
    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.user = JSON.parse(userStr);
      this.fetchAnnonce();
    } else {
      this.$router.push('/login');
    }
  }
}
</script>

<template>
  <div class="annonce-detail-container">
    <div class="annonce-detail-header">
      <h1>Détail de l'annonce</h1>
      <router-link :to="user && user.type === 'CLIENT' ? '/client/annonces' : '/livreur/annonces'" class="btn-back">
        <i class="fas fa-arrow-left"></i> Retour aux annonces
      </router-link>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i>
      <p>Chargement de l'annonce...</p>
    </div>

    <div v-else-if="error" class="error-message">
      <i class="fas fa-exclamation-circle"></i>
      <p>{{ error }}</p>
    </div>

    <div v-else-if="annonce" class="annonce-detail-content">
      <div class="annonce-status-header">
        <h2>{{ annonce.titre }}</h2>
        <span class="status-badge" :class="statutLabel?.class">
          {{ statutLabel?.text || annonce.statut }}
        </span>
      </div>

      <div class="annonce-section">
        <h3>Description</h3>
        <p>{{ annonce.description }}</p>
      </div>

      <div class="annonce-section">
        <h3>Détails de l'annonce</h3>
        <div class="detail-grid">
          <div class="detail-item">
            <div class="detail-label">
              <i class="fas fa-tag"></i> Type
            </div>
            <div class="detail-value">
              {{ annonce.typeAnnonce === 'unique' ? 'Livraison unique' : 'Livraison récurrente' }}
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-label">
              <i class="fas fa-euro-sign"></i> Prix
            </div>
            <div class="detail-value">
              {{ annonce.prixUnitaire }} €
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-label">
              <i class="fas fa-calendar-day"></i> Période
            </div>
            <div class="detail-value">
              Du {{ formatDate(annonce.dateDebut) }} au {{ formatDate(annonce.dateFin) }}
            </div>
          </div>
        </div>
      </div>

      <div class="annonce-section">
        <h3>Adresses</h3>
        <div class="addresses-container">
          <div class="address-box">
            <div class="address-header">
              <i class="fas fa-map-marker-alt"></i>
              <h4>Adresse de départ</h4>
            </div>
            <p>{{ annonce.adresseDepart }}</p>
          </div>
          <div class="arrow">
            <i class="fas fa-long-arrow-alt-right"></i>
          </div>
          <div class="address-box">
            <div class="address-header">
              <i class="fas fa-map-marker"></i>
              <h4>Adresse d'arrivée</h4>
            </div>
            <p>{{ annonce.adresseFin }}</p>
          </div>
        </div>
      </div>

      <!-- Section Carte -->
      <div class="annonce-section">
        <h3>Carte de la Livraison</h3>
        <delivery-map
          :start-latitude="Number(annonce.latitudeEnvoi)"
          :start-longitude="Number(annonce.longitudeEnvoi)"
          :end-latitude="Number(annonce.latitudeLivraison)"
          :end-longitude="Number(annonce.longitudeLivraison)"
        ></delivery-map>
      </div>

      <div class="annonce-section" v-if="annonce.colis">
        <h3>Détails du colis</h3>
        <div class="colis-details">
          <div class="colis-dimensions">
            <div class="dimension-item">
              <i class="fas fa-weight"></i>
              <span>{{ annonce.colis.poids }} kg</span>
            </div>
            <div class="dimension-item">
              <i class="fas fa-arrows-alt"></i>
              <span>{{ annonce.colis.longueur }} × {{ annonce.colis.largeur }} × {{ annonce.colis.hauteur }} cm</span>
            </div>
            <div class="dimension-item" v-if="annonce.colis.fragile">
              <i class="fas fa-glass-whiskey"></i>
              <span>Colis fragile</span>
            </div>
          </div>
          <div class="colis-description" v-if="annonce.colis.description">
            <h4>Description du contenu</h4>
            <p>{{ annonce.colis.description }}</p>
          </div>
        </div>
      </div>

      <div class="annonce-section">
        <h3>Contacts</h3>
        <div class="contacts-container">
          <div class="contact-box">
            <div class="contact-header">
              <i class="fas fa-user"></i>
              <h4>Expéditeur</h4>
            </div>
            <div class="contact-info">
              <p><strong>Nom:</strong> {{ annonce.expediteur.prenom }} {{ annonce.expediteur.nom }}</p>
              <p v-if="annonce.expediteur.telephone"><strong>Téléphone:</strong> {{ annonce.expediteur.telephone }}</p>
              <p v-if="annonce.expediteur.email"><strong>Email:</strong> {{ annonce.expediteur.email }}</p>
            </div>
          </div>
          <div class="contact-box">
            <div class="contact-header">
              <i class="fas fa-user-friends"></i>
              <h4>Destinataire</h4>
            </div>
            <div class="contact-info">
              <p><strong>Nom:</strong> {{ annonce.destinataire.prenom }} {{ annonce.destinataire.nom }}</p>
              <p v-if="annonce.destinataire.telephone"><strong>Téléphone:</strong> {{ annonce.destinataire.telephone }}</p>
              <p v-if="annonce.destinataire.email"><strong>Email:</strong> {{ annonce.destinataire.email }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="annonce-actions" v-if="canEdit">
        <router-link :to="`/client/annonces/${annonce.idAnnonce}/edit`" class="btn-action btn-edit">
          <i class="fas fa-edit"></i> Modifier
        </router-link>
        <button @click="cancelAnnonce" class="btn-action btn-cancel">
          <i class="fas fa-times"></i> Annuler
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.annonce-detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.annonce-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  color: #555;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  background-color: #f0f0f0;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background-color: #e0e0e0;
}

.btn-back i {
  margin-right: 0.5rem;
}

.loading, .error-message {
  text-align: center;
  padding: 3rem;
}

.loading i {
  font-size: 2rem;
  color: #4CAF50;
  margin-bottom: 1rem;
}

.error-message {
  color: #c62828;
  background-color: #ffebee;
  border-radius: 8px;
}

.error-message i {
  font-size: 1.5rem;
  margin-bottom: 1rem;
}

.annonce-detail-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  padding: 2rem;
}

.annonce-status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
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

.annonce-section {
  margin-bottom: 2rem;
}

.annonce-section h3 {
  color: #4CAF50;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #eee;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-label {
  color: #666;
  margin-bottom: 0.5rem;
}

.detail-label i {
  margin-right: 0.5rem;
}

.detail-value {
  font-weight: 500;
}

.addresses-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.address-box {
  flex: 1;
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.address-header {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
  color: #4CAF50;
}

.address-header i {
  margin-right: 0.5rem;
}

.arrow {
  color: #666;
  font-size: 1.5rem;
}

.colis-details {
  background-color: #f9f9f9;
  padding: 1rem;
  border-radius: 4px;
}

.colis-dimensions {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.dimension-item {
  display: flex;
  align-items: center;
}

.dimension-item i {
  margin-right: 0.5rem;
  color: #4CAF50;
}

.colis-description h4 {
  margin-bottom: 0.5rem;
  color: #555;
}

.contacts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.contact-box {
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.contact-header {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
  color: #4CAF50;
}

.contact-header i {
  margin-right: 0.5rem;
}

.contact-info p {
  margin-bottom: 0.5rem;
}

.annonce-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.btn-action {
  padding: 0.8rem 1.5rem;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  border: none;
}

.btn-edit {
  background-color: #f0f0f0;
  color: #333;
}

.btn-edit:hover {
  background-color: #e0e0e0;
}

.btn-cancel {
  background-color: #ffebee;
  color: #c62828;
}

.btn-cancel:hover {
  background-color: #ffcdd2;
}

.btn-action i {
  margin-right: 0.5rem;
}

@media (max-width: 768px) {
  .annonce-detail-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .addresses-container {
    flex-direction: column;
  }

  .arrow {
    transform: rotate(90deg);
    margin: 1rem 0;
  }
}
</style>