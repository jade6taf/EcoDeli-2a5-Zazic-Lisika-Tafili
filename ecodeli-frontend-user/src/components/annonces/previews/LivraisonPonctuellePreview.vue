<template>
  <div class="livraison-ponctuelle-preview">
    <div class="preview-header">
      <div class="preview-category">
        <i class="pi pi-clock"></i>
        <span>Livraison ponctuelle</span>
      </div>
      <div class="preview-status">
        <span class="status-badge">Aperçu</span>
      </div>
    </div>

    <div class="preview-content">
      <h2 class="preview-title">{{ annonceData.titre }}</h2>
      <p class="preview-description">{{ annonceData.description }}</p>

      <div class="preview-sections">
        <!-- Section Date et Heure -->
        <div class="preview-section">
          <div class="section-header">
            <i class="pi pi-calendar"></i>
            <h3>Date et heure de livraison</h3>
          </div>
          <div class="section-content">
            <div class="info-row">
              <span class="label">Date :</span>
              <span class="value">{{ formatDate(annonceData.dateLivraisonPrecise) }}</span>
            </div>
            <div class="info-row">
              <span class="label">Heure :</span>
              <span class="value">{{ annonceData.heureLivraisonPrecise }}</span>
            </div>
            <div class="info-row" v-if="annonceData.disponibiliteTempsReel">
              <span class="label">Disponibilité :</span>
              <span class="value badge-success">Temps réel</span>
            </div>
          </div>
        </div>

        <!-- Section Adresses -->
        <div class="preview-section">
          <div class="section-header">
            <i class="pi pi-map-marker"></i>
            <h3>Adresses</h3>
          </div>
          <div class="section-content">
            <div class="address-flow">
              <div class="address-item">
                <div class="address-label">
                  <i class="pi pi-circle text-green-500"></i>
                  <span>Collecte</span>
                </div>
                <div class="address-value">{{ annonceData.adresseCollecte }}</div>
              </div>
              <div class="flow-arrow">
                <i class="pi pi-arrow-down"></i>
              </div>
              <div class="address-item">
                <div class="address-label">
                  <i class="pi pi-map-marker text-red-500"></i>
                  <span>Destination</span>
                </div>
                <div class="address-value">{{ annonceData.adresseDestination }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Section Colis -->
        <div class="preview-section" v-if="annonceData.poidsColis || annonceData.dimensionsColis">
          <div class="section-header">
            <i class="pi pi-box"></i>
            <h3>Détails du colis</h3>
          </div>
          <div class="section-content">
            <div class="colis-details">
              <div class="detail-item" v-if="annonceData.poidsColis">
                <i class="pi pi-scale"></i>
                <span>{{ annonceData.poidsColis }} kg</span>
              </div>
              <div class="detail-item" v-if="annonceData.dimensionsColis">
                <i class="pi pi-ruler"></i>
                <span>{{ annonceData.dimensionsColis }} cm</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Section Instructions -->
        <div class="preview-section" v-if="annonceData.instructionsLivraison">
          <div class="section-header">
            <i class="pi pi-info-circle"></i>
            <h3>Instructions spéciales</h3>
          </div>
          <div class="section-content">
            <p class="instructions">{{ annonceData.instructionsLivraison }}</p>
          </div>
        </div>

        <!-- Section Contact -->
        <div class="preview-section">
          <div class="section-header">
            <i class="pi pi-phone"></i>
            <h3>Contact destinataire</h3>
          </div>
          <div class="section-content">
            <div class="contact-info">
              <i class="pi pi-user"></i>
              <span>{{ annonceData.contactDestinataire }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  annonceData: {
    type: Object,
    required: true
  },
  category: {
    type: String,
    required: true
  }
})

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('fr-FR', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<style scoped>
.livraison-ponctuelle-preview {
  font-family: Arial, sans-serif;
  max-width: 800px;
  margin: 0 auto;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e9ecef;
}

.preview-category {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.1rem;
  font-weight: 600;
  color: #4CAF50;
}

.preview-category i {
  font-size: 1.3rem;
}

.status-badge {
  background: #e8f5e8;
  color: #2e7d32;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
}

.preview-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.preview-description {
  font-size: 1.1rem;
  color: #6c757d;
  margin-bottom: 2rem;
  line-height: 1.6;
}

.preview-sections {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.preview-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 1.5rem;
  border-left: 4px solid #4CAF50;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.section-header i {
  color: #4CAF50;
  font-size: 1.2rem;
}

.section-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.section-content {
  margin-left: 2rem;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 0.75rem;
}

.info-row:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: 600;
  color: #495057;
  min-width: 100px;
}

.value {
  color: #2c3e50;
  font-weight: 500;
}

.badge-success {
  background: #d4edda;
  color: #155724;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
}

.address-flow {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.address-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.address-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #495057;
}

.address-value {
  background: white;
  padding: 0.75rem;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  color: #2c3e50;
}

.flow-arrow {
  text-align: center;
  color: #6c757d;
  font-size: 1.2rem;
}

.colis-details {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: white;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.detail-item i {
  color: #4CAF50;
}

.instructions {
  background: white;
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  color: #2c3e50;
  line-height: 1.6;
  margin: 0;
}

.contact-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: white;
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  color: #2c3e50;
  font-weight: 500;
}

.contact-info i {
  color: #4CAF50;
  font-size: 1.1rem;
}

@media (max-width: 768px) {
  .preview-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .section-content {
    margin-left: 0;
  }
  
  .colis-details {
    flex-direction: column;
    gap: 1rem;
  }
  
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}
</style>