<script>
import { apiServices } from '@/services/apiServices'
import { authServices } from '@/services/authServices'

export default {
  name: 'SegmentsDisponiblesView',
  data() {
    return {
      segments: [],
      villesDisponibles: [],
      villeFiltre: '',
      loading: false
    }
  },
  async created() {
    await this.chargerVillesDisponibles()
    await this.chargerSegmentsDisponibles()
  },
  methods: {
    async chargerVillesDisponibles() {
      try {
        const response = await apiServices.get('/entrepots/disponibles')
        this.villesDisponibles = response.data
      } catch (error) {
        this.$toast?.error('Erreur lors du chargement des villes disponibles')
      }
    },

    async chargerSegmentsDisponibles() {
      try {
        const endpoint = this.villeFiltre
          ? `/livraisons/attente-segment-2/${this.villeFiltre}`
          : '/livraisons/attente-segment-2'

        const response = await apiServices.get(endpoint)
        this.segments = response.data
      } catch (error) {
        this.$toast?.error('Erreur lors du chargement des segments disponibles')
      }
    },

    async filtrerParVille() {
      await this.chargerSegmentsDisponibles()
    },

    async prendreEnChargeSegment2(segment) {
      try {
        this.loading = true
        const userId = authServices.getCurrentUserId()
        if (!userId) {
          this.$toast?.error('Utilisateur non connecté')
          return
        }

        await apiServices.put(`/livraisons/${segment.idLivraison}/demarrer-segment-2/${userId}`)

        this.$toast?.success('Segment 2 pris en charge avec succès!')

        this.$router.push('/livreur/livraisons')

      } catch (error) {
        this.$toast?.error(error.response?.data || 'Erreur lors de la prise en charge du segment')
      } finally {
        this.loading = false
      }
    },

    calculerPrixSegment2(segment) {
      if (segment.annonce?.prixUnitaire) {
        // Le segment 2 représente la moitié restante du prix total
        return Math.round(segment.annonce.prixUnitaire / 2)
      }
      return 0
    },

    formatDate(dateString) {
      if (!dateString)
        return 'Non définie'
      const date = new Date(dateString)
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<template>
  <div class="segments-disponibles">
    <h1>Segments de livraison disponibles</h1>
    <div class="filters">
      <div class="filter-group">
        <label for="ville-filter">Filtrer par ville d'entrepôt :</label>
        <select v-model="villeFiltre" @change="filtrerParVille" id="ville-filter">
          <option value="">Toutes les villes</option>
          <option v-for="ville in villesDisponibles" :key="ville" :value="ville">
            {{ ville }}
          </option>
        </select>
      </div>
    </div>

    <div v-if="segments.length === 0" class="no-segments">
      <p>Aucun segment de livraison disponible pour le moment.</p>
    </div>

    <div v-else class="segments-grid">
      <div
        v-for="segment in segments"
        :key="segment.idLivraison"
        class="segment-card"
      >
        <div class="segment-header">
          <h3>Livraison #{{ segment.idLivraison }}</h3>
          <span class="statut-badge">{{ segment.statut }}</span>
        </div>

        <div class="segment-details">
          <div class="detail-row">
            <span class="label">Entrepôt :</span>
            <span class="value">{{ segment.entrepotVille }}</span>
          </div>

          <div class="detail-row">
            <span class="label">Destination finale :</span>
            <span class="value">{{ segment.annonce?.adresseFin || 'Non définie' }}</span>
          </div>

          <div class="detail-row">
            <span class="label">Colis :</span>
            <span class="value">{{ segment.colis?.description || 'Description non disponible' }}</span>
          </div>

          <div class="detail-row">
            <span class="label">Prix segment 2 :</span>
            <span class="value price">{{ calculerPrixSegment2(segment) }}€</span>
          </div>

          <div class="detail-row">
            <span class="label">Déposé le :</span>
            <span class="value">{{ formatDate(segment.dateDepotEntrepot) }}</span>
          </div>

          <div class="detail-row">
            <span class="label">Expéditeur :</span>
            <span class="value">{{ segment.expediteur?.prenom }} {{ segment.expediteur?.nom }}</span>
          </div>

          <div class="detail-row">
            <span class="label">Destinataire :</span>
            <span class="value">{{ segment.destinataire?.prenom }} {{ segment.destinataire?.nom }}</span>
          </div>
        </div>

        <div class="segment-actions">
          <button
            @click="prendreEnChargeSegment2(segment)"
            class="btn btn-primary"
            :disabled="loading"
          >
            <span v-if="loading">Chargement...</span>
            <span v-else>Prendre en charge le segment 2</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.segments-disponibles {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  color: #2c3e50;
  margin-bottom: 30px;
  text-align: center;
}

.filters {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 15px;
}

.filter-group label {
  font-weight: 600;
  color: #495057;
}

.filter-group select {
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  background-color: white;
  min-width: 200px;
}

.no-segments {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
  font-size: 18px;
}

.segments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 25px;
}

.segment-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border: 1px solid #e9ecef;
  transition: transform 0.2s, box-shadow 0.2s;
}

.segment-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.segment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e9ecef;
}

.segment-header h3 {
  margin: 0;
  color: #495057;
  font-size: 1.2em;
}

.statut-badge {
  background: #28a745;
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.85em;
  font-weight: 600;
}

.segment-details {
  margin-bottom: 25px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f8f9fa;
}

.detail-row:last-child {
  border-bottom: none;
}

.label {
  font-weight: 600;
  color: #495057;
  flex: 1;
}

.value {
  color: #6c757d;
  flex: 2;
  text-align: right;
}

.value.price {
  color: #28a745;
  font-weight: 700;
  font-size: 1.1em;
}

.segment-actions {
  text-align: center;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.95em;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0056b3;
  transform: translateY(-1px);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .segments-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-group {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .segment-card {
    padding: 20px;
  }
  
  .detail-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .value {
    text-align: left;
  }
}
</style>
