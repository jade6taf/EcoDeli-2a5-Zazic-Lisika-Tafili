<script>
import apiServices from '@/services/apiServices';

export default {
  name: 'PrestatairesList',
  props: {
    serviceType: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      prestataires: [],
      loading: true,
      error: null,
      selectedPrestataire: null
    }
  },
  watch: {
    serviceType: {
      immediate: true,
      handler: 'loadPrestataires'
    }
  },
  methods: {
    async loadPrestataires() {
      if (!this.serviceType)
        return;
      this.loading = true;
      this.error = null;
      try {
        this.prestataires = await apiServices.getPrestatairesByServiceType(this.serviceType);
      } catch (err) {
        this.error = "Erreur lors du chargement des prestataires: " + err.message;
      } finally {
        this.loading = false;
      }
    },
    selectPrestataire(prestataire) {
      this.selectedPrestataire = prestataire;
      this.$emit('prestataire-selected', prestataire);
    },
    averageRating(evaluations) {
      if (!evaluations || evaluations.length === 0) return 0;
      return evaluations.reduce((acc, evaluation) => acc + evaluation.note, 0) / evaluations.length;
    },
    formatRating(rating) {
      return rating.toFixed(1);
    },
    truncateDescription(description) {
      if (!description) return 'Aucune description disponible';
      return description.length > 100 ? description.substring(0, 100) + '...' : description;
    },
    handleImageError(e) {
      e.target.src = '/default-avatar.png';
    }
  }
}
</script>

<template>
  <div class="prestataires-list">
    <div v-if="loading" class="loading">
      <i class="fas fa-spinner fa-spin"></i>
      Chargement des prestataires...
    </div>
    <div v-else-if="error" class="error">
      <i class="fas fa-exclamation-triangle"></i>
      {{ error }}
    </div>
    <div v-else>
      <h3><i class="fas fa-users"></i> Prestataires disponibles</h3>
      <div v-if="prestataires.length === 0" class="no-results">
        <i class="fas fa-info-circle"></i>
        Aucun prestataire disponible pour ce type de service.
      </div>
      <div v-else class="prestataires-grid">
        <div v-for="prestataire in prestataires"
             :key="prestataire.id"
             class="prestataire-card"
             :class="{ 'selected': selectedPrestataire?.id === prestataire.id }"
             @click="selectPrestataire(prestataire)">

          <!-- En-tête de la carte avec photo et statut -->
          <div class="card-header">
            <!-- <div class="profile-image">
              <img :src="prestataire.imageUrl || '/default-avatar.png'"
                   :alt="prestataire.nomEntreprise"
                   @error="handleImageError">
            </div> -->
            <div class="status-badge" :class="{ 'available': prestataire.disponible }">
              <i class="fas" :class="prestataire.disponible ? 'fa-check-circle' : 'fa-times-circle'"></i>
              {{ prestataire.disponible ? 'Disponible' : 'Non disponible' }}
            </div>
          </div>

          <!-- Informations principales -->
          <div class="card-body">
            <h4>{{ prestataire.nomEntreprise }}</h4>
            <div class="expertise">
              <i class="fas fa-star"></i>
              <span>{{ prestataire.domaineExpertise }}</span>
            </div>

            <!-- Évaluations -->
            <div class="ratings">
              <div class="rating-stars">
                <i v-for="n in 5"
                   :key="n"
                   class="fas fa-star"
                   :class="{ 'filled': n <= averageRating(prestataire.evaluations) }">
                </i>
                <span class="rating-value">{{ formatRating(averageRating(prestataire.evaluations)) }}</span>
              </div>
              <span class="rating-count">({{ prestataire.evaluations.length }} avis)</span>
            </div>

            <!-- Description -->
            <p class="description">
              <i class="fas fa-info-circle"></i>
              {{ truncateDescription(prestataire.description) }}
            </p>

            <!-- Tarif -->
            <div class="tarif">
              <i class="fas fa-euro-sign"></i>
              {{ prestataire.tarifHoraire }}€/heure
            </div>

            <!-- Zone d'intervention -->
            <div class="zone">
              <i class="fas fa-map-marker-alt"></i>
              {{ prestataire.zoneIntervention }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.prestataires-list {
  padding: 1.5rem;
}

.prestataires-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-top: 1.5rem;
}

.prestataire-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
}

.prestataire-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.prestataire-card.selected {
  border: 2px solid var(--primary-color, #4CAF50);
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
}

.card-header {
  position: relative;
  padding: 1rem;
  background: #f8f9fa;
}

.profile-image {
  width: 80px;
  height: 80px;
  margin: 0 auto;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.profile-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  background: #ff4444;
  color: white;
}

.status-badge.available {
  background: #00C851;
}

.card-body {
  padding: 1.25rem;
}

.card-body h4 {
  margin: 0 0 0.5rem;
  font-size: 1.2rem;
  color: #2c3e50;
}

.expertise {
  margin-bottom: 0.75rem;
  color: #666;
}

.ratings {
  margin-bottom: 1rem;
}

.rating-stars {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.rating-stars i {
  color: #ddd;
}

.rating-stars i.filled {
  color: #ffc107;
}

.rating-value {
  margin-left: 0.5rem;
  font-weight: bold;
}

.rating-count {
  color: #666;
  font-size: 0.9rem;
}

.description {
  margin: 1rem 0;
  font-size: 0.95rem;
  color: #666;
  line-height: 1.4;
}

.tarif, .zone {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
  color: #555;
}

.loading, .error, .no-results {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.error {
  color: #dc3545;
}

@media (max-width: 768px) {
  .prestataires-grid {
    grid-template-columns: 1fr;
  }

  .prestataire-card {
    margin-bottom: 1rem;
  }
}

i[class^="fas"] {
  margin-right: 0.5rem;
}

h3 i {
  color: var(--primary-color, #4CAF50);
}
</style>