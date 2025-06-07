<script>
import DeliveryMap from '@/components/DeliveryMap.vue';

export default {
  name: 'CommercantAnnonceDetailView',
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
    },
    canCancel() {
      return this.annonce && ['PUBLIEE', 'VALIDEE'].includes(this.annonce.statut);
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

        const response = await fetch(`/api/commercants/${this.user.idUtilisateur}/annonces/${annonceId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          if (response.status === 404) {
            throw new Error('Annonce non trouvée');
          }
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
      if (!confirm('Êtes-vous sûr de vouloir annuler cette annonce ? Cette action est irréversible.')) {
        return;
      }

      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/commercants/${this.user.idUtilisateur}/annonces/${this.annonce.idAnnonce}/annuler`, {
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

    editAnnonce() {
      this.$router.push(`/commercant/annonces/${this.annonce.idAnnonce}/edit`);
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
      <h1><i class="fas fa-eye"></i> Détail de l'annonce</h1>
      <router-link to="/commercant/annonces" class="btn-back">
        <i class="fas fa-arrow-left"></i> Retour aux annonces
      </router-link>
    </div>

    <div v-if="isLoading" class="loading">
      <div class="loading-spinner">
        <i class="fas fa-spinner fa-spin"></i>
        <p>Chargement de l'annonce...</p>
      </div>
    </div>

    <div v-else-if="error" class="error-message">
      <i class="fas fa-exclamation-circle"></i>
      <p>{{ error }}</p>
      <button @click="fetchAnnonce" class="retry-btn">
        <i class="fas fa-redo"></i> Réessayer
      </button>
    </div>

    <div v-else-if="annonce" class="annonce-detail-content">
      <!-- Header with title and status -->
      <div class="annonce-status-header">
        <div class="title-section">
          <h2>{{ annonce.titre }}</h2>
          <p class="annonce-id">Annonce #{{ annonce.idAnnonce }}</p>
        </div>
        <div class="status-section">
          <span class="status-badge" :class="statutLabel?.class">
            {{ statutLabel?.text || annonce.statut }}
          </span>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="action-buttons" v-if="canEdit || canCancel">
        <button v-if="canEdit" @click="editAnnonce" class="btn-primary">
          <i class="fas fa-edit"></i> Modifier
        </button>
        <button v-if="canCancel" @click="cancelAnnonce" class="btn-danger">
          <i class="fas fa-times"></i> Annuler l'annonce
        </button>
      </div>

      <!-- Description -->
      <div class="annonce-section">
        <h3><i class="fas fa-align-left"></i> Description</h3>
        <div class="description-content">
          <p>{{ annonce.description }}</p>
        </div>
      </div>

      <!-- Details -->
      <div class="annonce-section">
        <h3><i class="fas fa-info-circle"></i> Détails de la livraison</h3>
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
              <i class="fas fa-euro-sign"></i> Prix proposé
            </div>
            <div class="detail-value price">
              {{ annonce.prixUnitaire }} €
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-label">
              <i class="fas fa-calendar-alt"></i> Prise en charge
            </div>
            <div class="detail-value">
              {{ formatDate(annonce.dateDebut) }}
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-label">
              <i class="fas fa-clock"></i> Livraison avant
            </div>
            <div class="detail-value">
              {{ formatDate(annonce.dateFin) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Package Information -->
      <div class="annonce-section" v-if="annonce.colis">
        <h3><i class="fas fa-box"></i> Informations du colis</h3>
        <div class="colis-info">
          <div class="colis-grid">
            <div class="colis-item">
              <span class="colis-label">Poids</span>
              <span class="colis-value">{{ annonce.colis.poids }} kg</span>
            </div>
            <div class="colis-item">
              <span class="colis-label">Dimensions</span>
              <span class="colis-value">
                {{ annonce.colis.longueur }} × {{ annonce.colis.largeur }} × {{ annonce.colis.hauteur }} cm
              </span>
            </div>
            <div class="colis-item" v-if="annonce.colis.fragile">
              <span class="colis-label">Fragile</span>
              <span class="colis-value fragile">
                <i class="fas fa-exclamation-triangle"></i> Oui
              </span>
            </div>
          </div>
          <div v-if="annonce.colis.description" class="colis-description">
            <span class="colis-label">Description du contenu</span>
            <p>{{ annonce.colis.description }}</p>
          </div>
        </div>
      </div>

      <!-- Addresses -->
      <div class="annonce-section">
        <h3><i class="fas fa-route"></i> Itinéraire</h3>
        <div class="addresses-container">
          <div class="address-box departure">
            <div class="address-header">
              <i class="fas fa-store"></i>
              <h4>Départ (votre boutique)</h4>
            </div>
            <p>{{ annonce.adresseDepart }}</p>
          </div>
          <div class="arrow">
            <i class="fas fa-long-arrow-alt-right"></i>
          </div>
          <div class="address-box arrival">
            <div class="address-header">
              <i class="fas fa-home"></i>
              <h4>Livraison</h4>
            </div>
            <p>{{ annonce.adresseFin }}</p>
          </div>
        </div>
      </div>

      <!-- Contact Information -->
      <div class="annonce-section">
        <h3><i class="fas fa-users"></i> Informations de contact</h3>
        <div class="contacts-grid">
          <div class="contact-card">
            <h4><i class="fas fa-store"></i> Expéditeur (vous)</h4>
            <div class="contact-info">
              <p><strong>{{ annonce.expediteur.prenom }} {{ annonce.expediteur.nom }}</strong></p>
              <p><i class="fas fa-phone"></i> {{ annonce.expediteur.telephone }}</p>
              <p v-if="annonce.expediteur.email"><i class="fas fa-envelope"></i> {{ annonce.expediteur.email }}</p>
            </div>
          </div>
          <div class="contact-card">
            <h4><i class="fas fa-user"></i> Destinataire</h4>
            <div class="contact-info">
              <p><strong>{{ annonce.destinataire.prenom }} {{ annonce.destinataire.nom }}</strong></p>
              <p><i class="fas fa-phone"></i> {{ annonce.destinataire.telephone }}</p>
              <p v-if="annonce.destinataire.email"><i class="fas fa-envelope"></i> {{ annonce.destinataire.email }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Map Section (if map component is available) -->
      <div class="annonce-section">
        <h3><i class="fas fa-map"></i> Carte de l'itinéraire</h3>
        <div class="map-container">
          <DeliveryMap 
            v-if="annonce.adresseDepart && annonce.adresseFin"
            :start-address="annonce.adresseDepart"
            :end-address="annonce.adresseFin"
          />
          <div v-else class="map-placeholder">
            <i class="fas fa-map"></i>
            <p>Carte non disponible</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.annonce-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.annonce-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e5e7eb;
}

.annonce-detail-header h1 {
  color: #2563eb;
  margin: 0;
  font-size: 1.875rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: #f3f4f6;
  color: #374151;
  text-decoration: none;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.btn-back:hover {
  background: #e5e7eb;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.loading-spinner {
  text-align: center;
  color: #6b7280;
}

.loading-spinner i {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #fef2f2;
  color: #dc2626;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.retry-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
}

.retry-btn:hover {
  background: #b91c1c;
}

.annonce-status-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.title-section h2 {
  color: #1f2937;
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.annonce-id {
  color: #6b7280;
  margin: 0;
  font-size: 0.875rem;
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-published { background: #dcfce7; color: #166534; }
.status-pending { background: #fef3c7; color: #92400e; }
.status-validated { background: #dbeafe; color: #1e40af; }
.status-in-progress { background: #e0e7ff; color: #3730a3; }
.status-completed { background: #f3e8ff; color: #7c3aed; }
.status-cancelled { background: #fecaca; color: #dc2626; }

.action-buttons {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.btn-primary, .btn-danger {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.btn-danger {
  background: #dc2626;
  color: white;
}

.btn-danger:hover {
  background: #b91c1c;
  transform: translateY(-1px);
}

.annonce-section {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.annonce-section h3 {
  color: #1f2937;
  margin-bottom: 1rem;
  font-size: 1.25rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.description-content {
  background: white;
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.detail-item {
  background: white;
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.detail-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.detail-value {
  color: #1f2937;
  font-weight: 500;
}

.detail-value.price {
  font-size: 1.125rem;
  font-weight: 700;
  color: #059669;
}

.colis-info {
  background: white;
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.colis-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.colis-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.colis-label {
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
}

.colis-value {
  color: #1f2937;
  font-weight: 500;
}

.colis-value.fragile {
  color: #dc2626;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.colis-description {
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

.colis-description p {
  margin: 0.5rem 0 0 0;
  color: #1f2937;
}

.addresses-container {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: white;
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.address-box {
  flex: 1;
  padding: 1rem;
  border-radius: 6px;
  border: 2px solid #e5e7eb;
}

.address-box.departure {
  border-color: #059669;
  background: #f0fdf4;
}

.address-box.arrival {
  border-color: #dc2626;
  background: #fef2f2;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.address-header h4 {
  color: #1f2937;
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.address-box p {
  margin: 0;
  color: #374151;
  font-weight: 500;
}

.arrow {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #6b7280;
  font-size: 1.5rem;
}

.contacts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.contact-card {
  background: white;
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.contact-card h4 {
  color: #1f2937;
  margin-bottom: 1rem;
  font-size: 1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.contact-info p {
  margin: 0.5rem 0;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.contact-info i {
  width: 16px;
  color: #6b7280;
}

.map-container {
  background: white;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  min-height: 300px;
}

.map-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  color: #6b7280;
}

.map-placeholder i {
  font-size: 3rem;
  margin-bottom: 1rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .annonce-detail-container {
    padding: 1rem;
    margin: 1rem;
  }

  .annonce-detail-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .annonce-detail-header h1 {
    font-size: 1.5rem;
  }

  .annonce-status-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .action-buttons {
    flex-direction: column;
  }

  .addresses-container {
    flex-direction: column;
  }

  .arrow {
    transform: rotate(90deg);
  }

  .detail-grid,
  .contacts-grid {
    grid-template-columns: 1fr;
  }

  .colis-grid {
    grid-template-columns: 1fr;
  }
}

/* Dark theme support */
@media (prefers-color-scheme: dark) {
  .annonce-detail-container {
    background: #1f2937;
    color: #f9fafb;
  }

  .annonce-detail-header {
    border-bottom-color: #374151;
  }

  .annonce-detail-header h1 {
    color: #60a5fa;
  }

  .btn-back {
    background: #374151;
    color: #d1d5db;
  }

  .btn-back:hover {
    background: #4b5563;
  }

  .annonce-status-header,
  .annonce-section {
    background: #374151;
    border-color: #4b5563;
  }

  .annonce-section h3 {
    color: #d1d5db;
  }

  .description-content,
  .detail-item,
  .colis-info,
  .addresses-container,
  .contact-card,
  .map-container {
    background: #4b5563;
    border-color: #6b7280;
  }

  .address-box.departure {
    background: #064e3b;
    border-color: #10b981;
  }

  .address-box.arrival {
    background: #7f1d1d;
    border-color: #ef4444;
  }

  .detail-label,
  .colis-label,
  .contact-card h4,
  .address-header h4 {
    color: #d1d5db;
  }

  .detail-value,
  .colis-value,
  .address-box p,
  .contact-info p,
  .annonce-id {
    color: #f9fafb;
  }

  .map-placeholder {
    color: #9ca3af;
  }
}
</style>
