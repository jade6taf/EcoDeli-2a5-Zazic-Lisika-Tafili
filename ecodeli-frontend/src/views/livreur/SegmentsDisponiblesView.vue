<script>
import { apiServices } from '@/services/apiServices'
import { authServices } from '@/services/authServices'

export default {
  name: 'SegmentsDisponiblesView',
  data() {
    return {
      segments: [],
      loading: false
    }
  },
  async created() {
    await this.chargerSegmentsDisponibles()
  },
  methods: {
    async chargerSegmentsDisponibles() {
      try {
        const response = await apiServices.get('/livraisons/attente-segment-2')
        this.segments = response.data
      } catch (error) {
        this.$toast?.error('Erreur lors du chargement des segments disponibles')
      }
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
        return Math.round(segment.annonce.prixUnitaire / 2)
      }
      return 0
    },

    getEntrepotOptimal(segment) {
      if (segment.annonce?.entrepotIntermediaire) {
        return segment.annonce.entrepotIntermediaire
      }
      return segment.entrepotVille || 'Non défini'
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

    <div class="info-header">
      <div class="info-card">
        <i class="fas fa-info-circle"></i>
        <p>Les entrepôts sont automatiquement assignés selon l'optimisation du trajet. Vous ne pouvez plus choisir librement.</p>
      </div>
    </div>

    <div v-if="segments.length === 0" class="no-segments">
      <i class="fas fa-warehouse"></i>
      <p>Aucun segment de livraison disponible pour le moment.</p>
      <small>Les segments apparaîtront ici une fois que les livreurs du segment 1 auront déposé leurs colis aux entrepôts.</small>
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

        <!-- Entrepôt optimal imposé -->
        <div class="entrepot-optimal">
          <div class="entrepot-icon">
            <i class="fas fa-warehouse"></i>
          </div>
          <div class="entrepot-info">
            <h4>Entrepôt assigné</h4>
            <p class="entrepot-nom">{{ getEntrepotOptimal(segment) }}</p>
            <small class="entrepot-raison">
              <i class="fas fa-route"></i>
              Calculé automatiquement pour optimiser le trajet
            </small>
          </div>
          <span class="badge-optimal">
            <i class="fas fa-star"></i>
            OPTIMAL
          </span>
        </div>

        <div class="segment-details">
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
            <span v-else>
              <i class="fas fa-truck"></i>
              Prendre en charge le segment 2
            </span>
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

.info-header {
  margin-bottom: 30px;
}

.info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.info-card i {
  font-size: 1.5rem;
  color: #fff;
  opacity: 0.9;
}

.info-card p {
  margin: 0;
  font-size: 1rem;
  line-height: 1.5;
}

.no-segments {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
  font-size: 18px;
  background: #f8f9fa;
  border-radius: 12px;
}

.no-segments i {
  font-size: 3rem;
  color: #dee2e6;
  margin-bottom: 20px;
  display: block;
}

.no-segments small {
  display: block;
  margin-top: 10px;
  font-size: 14px;
  color: #adb5bd;
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

/* Styles pour entrepôt optimal */
.entrepot-optimal {
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  padding: 20px;
  margin: -25px -25px 20px -25px;
  display: flex;
  align-items: center;
  gap: 15px;
  position: relative;
}

.entrepot-icon {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.entrepot-icon i {
  font-size: 1.5rem;
  color: white;
}

.entrepot-info {
  flex: 1;
}

.entrepot-info h4 {
  margin: 0 0 5px 0;
  font-size: 0.9rem;
  opacity: 0.9;
  font-weight: 500;
}

.entrepot-nom {
  margin: 0 0 8px 0;
  font-size: 1.4rem;
  font-weight: 700;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.entrepot-raison {
  font-size: 0.85rem;
  opacity: 0.8;
  display: flex;
  align-items: center;
  gap: 5px;
}

.entrepot-raison i {
  font-size: 0.8rem;
}

.badge-optimal {
  background: #FFC107;
  color: #212529;
  padding: 8px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.badge-optimal i {
  font-size: 0.8rem;
}

@media (max-width: 768px) {
  .segments-grid {
    grid-template-columns: 1fr;
  }
  
  .info-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    text-align: left;
  }
  
  .segment-card {
    padding: 20px;
  }
  
  .entrepot-optimal {
    margin: -20px -20px 20px -20px;
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .entrepot-optimal .badge-optimal {
    align-self: flex-end;
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
