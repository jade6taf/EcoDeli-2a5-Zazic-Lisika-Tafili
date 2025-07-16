<template>
  <div class="transport-marchandises-preview">
    <div class="preview-header">
      <div class="preview-category">
        <i class="pi pi-truck"></i>
        <span>Transport de marchandises</span>
      </div>
      <div class="preview-status">
        <span class="status-badge">Aperçu</span>
      </div>
    </div>

    <div class="preview-content">
      <h2 class="preview-title">{{ annonceData.titre }}</h2>
      <p class="preview-description">{{ annonceData.description }}</p>

      <div class="preview-sections">
        <!-- Section Fréquence -->
        <div class="preview-section">
          <div class="section-header">
            <i class="pi pi-calendar"></i>
            <h3>Fréquence de transport</h3>
          </div>
          <div class="section-content">
            <div class="frequency-display">
              <div class="frequency-badge" :class="getFrequencyClass(annonceData.frequenceTransport)">
                <i :class="getFrequencyIcon(annonceData.frequenceTransport)"></i>
                <span>{{ getFrequencyLabel(annonceData.frequenceTransport) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Section Catégories de marchandises -->
        <div class="preview-section">
          <div class="section-header">
            <i class="pi pi-tags"></i>
            <h3>Catégories de marchandises</h3>
          </div>
          <div class="section-content">
            <div class="categories-grid">
              <div 
                v-for="categoryId in selectedCategories" 
                :key="categoryId"
                class="category-item"
              >
                <i :class="getCategoryIcon(categoryId)"></i>
                <span>{{ getCategoryName(categoryId) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Section Conditions de transport -->
        <div class="preview-section">
          <div class="section-header">
            <i class="pi pi-shield"></i>
            <h3>Conditions de transport</h3>
          </div>
          <div class="section-content">
            <div class="conditions-display">
              <div class="condition-group">
                <div class="condition-label">
                  <i class="pi pi-thermometer"></i>
                  <span>Température</span>
                </div>
                <div class="condition-value">
                  {{ getTemperatureLabel(transportConditions.temperature) }}
                </div>
              </div>
              
              <div class="condition-group">
                <div class="condition-label">
                  <i class="pi pi-exclamation-triangle"></i>
                  <span>Fragilité</span>
                </div>
                <div class="condition-value">
                  {{ getFragilityLabel(transportConditions.fragilite) }}
                </div>
              </div>
              
              <div class="condition-group" v-if="transportConditions.special && transportConditions.special.length > 0">
                <div class="condition-label">
                  <i class="pi pi-plus"></i>
                  <span>Conditions spéciales</span>
                </div>
                <div class="special-conditions">
                  <span 
                    v-for="condition in transportConditions.special" 
                    :key="condition"
                    class="special-badge"
                  >
                    {{ getSpecialConditionLabel(condition) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Section Itinéraires -->
        <div class="preview-section" v-if="routes && routes.length > 0">
          <div class="section-header">
            <i class="pi pi-map-marker"></i>
            <h3>Itinéraires détaillés</h3>
          </div>
          <div class="section-content">
            <div class="routes-list">
              <div 
                v-for="(route, index) in routes" 
                :key="index"
                class="route-item"
              >
                <div class="route-path">
                  <div class="route-point start">
                    <i class="pi pi-circle"></i>
                    <span>{{ route.depart }}</span>
                  </div>
                  <div class="route-arrow">
                    <i class="pi pi-arrow-right"></i>
                  </div>
                  <div class="route-point end">
                    <i class="pi pi-map-marker"></i>
                    <span>{{ route.arrivee }}</span>
                  </div>
                </div>
                <div class="route-details">
                  <div class="route-distance">
                    <i class="pi pi-compass"></i>
                    <span>{{ route.distance }} km</span>
                  </div>
                  <div class="route-frequency">
                    <i class="pi pi-refresh"></i>
                    <span>{{ route.frequence }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Section Volume et capacité -->
        <div class="preview-section">
          <div class="section-header">
            <i class="pi pi-box"></i>
            <h3>Volume et capacité</h3>
          </div>
          <div class="section-content">
            <div class="volume-grid">
              <div class="volume-item" v-if="annonceData.volumeEstime">
                <div class="volume-label">
                  <i class="pi pi-cube"></i>
                  <span>Volume estimé</span>
                </div>
                <div class="volume-value">{{ annonceData.volumeEstime }} m³</div>
              </div>
              
              <div class="volume-item" v-if="annonceData.capaciteVehiculeRequise">
                <div class="volume-label">
                  <i class="pi pi-car"></i>
                  <span>Véhicule requis</span>
                </div>
                <div class="volume-value">{{ getVehicleTypeLabel(annonceData.capaciteVehiculeRequise) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Section Options d'optimisation -->
        <div class="preview-section" v-if="annonceData.optimisationRoute || annonceData.planificationCapacite">
          <div class="section-header">
            <i class="pi pi-cog"></i>
            <h3>Options d'optimisation</h3>
          </div>
          <div class="section-content">
            <div class="optimization-options">
              <div class="optimization-item" v-if="annonceData.optimisationRoute">
                <div class="optimization-icon">
                  <i class="pi pi-route"></i>
                </div>
                <div class="optimization-content">
                  <h4>Optimisation des itinéraires</h4>
                  <p>Calcul automatique des trajets les plus efficaces</p>
                  <div class="optimization-benefits">
                    <span class="benefit-tag">Réduction des coûts</span>
                    <span class="benefit-tag">Gain de temps</span>
                    <span class="benefit-tag">Écologique</span>
                  </div>
                </div>
              </div>
              
              <div class="optimization-item" v-if="annonceData.planificationCapacite">
                <div class="optimization-icon">
                  <i class="pi pi-chart-bar"></i>
                </div>
                <div class="optimization-content">
                  <h4>Planification de capacité</h4>
                  <p>Optimisation du remplissage des véhicules</p>
                  <div class="optimization-benefits">
                    <span class="benefit-tag">Maximise l'utilisation</span>
                    <span class="benefit-tag">Économies</span>
                    <span class="benefit-tag">Prévisions</span>
                  </div>
                </div>
              </div>
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

const selectedCategories = computed(() => {
  try {
    return JSON.parse(props.annonceData.categoriesMarchandises || '[]')
  } catch (e) {
    return []
  }
})

const transportConditions = computed(() => {
  try {
    return JSON.parse(props.annonceData.conditionsTransport || '{}')
  } catch (e) {
    return { temperature: 'normale', fragilite: 'normale', special: [] }
  }
})

const routes = computed(() => {
  try {
    return JSON.parse(props.annonceData.itinerairesDetailles || '[]')
  } catch (e) {
    return []
  }
})

const getFrequencyLabel = (frequency) => {
  const labels = {
    'UNIQUE': 'Une seule fois',
    'HEBDOMADAIRE': 'Hebdomadaire',
    'MENSUELLE': 'Mensuelle',
    'REGULIERE': 'Régulière'
  }
  return labels[frequency] || frequency
}

const getFrequencyIcon = (frequency) => {
  const icons = {
    'UNIQUE': 'pi pi-calendar-times',
    'HEBDOMADAIRE': 'pi pi-calendar',
    'MENSUELLE': 'pi pi-calendar-plus',
    'REGULIERE': 'pi pi-refresh'
  }
  return icons[frequency] || 'pi pi-calendar'
}

const getFrequencyClass = (frequency) => {
  const classes = {
    'UNIQUE': 'frequency-unique',
    'HEBDOMADAIRE': 'frequency-weekly',
    'MENSUELLE': 'frequency-monthly',
    'REGULIERE': 'frequency-regular'
  }
  return classes[frequency] || 'frequency-default'
}

const getCategoryIcon = (categoryId) => {
  const icons = {
    'alimentaire': 'pi pi-shopping-bag',
    'electronique': 'pi pi-mobile',
    'textile': 'pi pi-heart',
    'mobilier': 'pi pi-home',
    'pharmaceutique': 'pi pi-plus',
    'industriel': 'pi pi-cog',
    'automobile': 'pi pi-car',
    'autre': 'pi pi-box'
  }
  return icons[categoryId] || 'pi pi-box'
}

const getCategoryName = (categoryId) => {
  const names = {
    'alimentaire': 'Alimentaire',
    'electronique': 'Électronique',
    'textile': 'Textile',
    'mobilier': 'Mobilier',
    'pharmaceutique': 'Pharmaceutique',
    'industriel': 'Industriel',
    'automobile': 'Automobile',
    'autre': 'Autre'
  }
  return names[categoryId] || categoryId
}

const getTemperatureLabel = (temperature) => {
  const labels = {
    'normale': 'Température normale',
    'refrigeree': 'Réfrigérée (2-8°C)',
    'congelee': 'Congelée (-18°C)'
  }
  return labels[temperature] || temperature
}

const getFragilityLabel = (fragilite) => {
  const labels = {
    'normale': 'Normale',
    'fragile': 'Fragile',
    'tres_fragile': 'Très fragile'
  }
  return labels[fragilite] || fragilite
}

const getSpecialConditionLabel = (condition) => {
  const labels = {
    'etanche': 'Étanche',
    'vertical': 'Maintenir vertical',
    'assure': 'Assuré'
  }
  return labels[condition] || condition
}

const getVehicleTypeLabel = (vehicleType) => {
  const labels = {
    'utilitaire': 'Utilitaire (jusqu\'à 3m³)',
    'fourgon': 'Fourgon (3-10m³)',
    'camion': 'Camion (10-30m³)',
    'semi_remorque': 'Semi-remorque (30m³+)'
  }
  return labels[vehicleType] || vehicleType
}
</script>

<style scoped>
.transport-marchandises-preview {
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

.frequency-display {
  display: flex;
  justify-content: center;
}

.frequency-badge {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 2rem;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  color: white;
}

.frequency-unique { background: linear-gradient(135deg, #ff6b6b, #ff8e8e); }
.frequency-weekly { background: linear-gradient(135deg, #4ecdc4, #44b3ac); }
.frequency-monthly { background: linear-gradient(135deg, #45b7d1, #4299d4); }
.frequency-regular { background: linear-gradient(135deg, #96ceb4, #85b8a3); }

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1rem;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: white;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  color: #2c3e50;
  font-weight: 500;
}

.category-item i {
  color: #4CAF50;
  font-size: 1.2rem;
}

.conditions-display {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.condition-group {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.condition-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #495057;
}

.condition-label i {
  color: #4CAF50;
}

.condition-value {
  font-weight: 500;
  color: #2c3e50;
}

.special-conditions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.special-badge {
  background: #e8f5e8;
  color: #2e7d32;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.routes-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.route-item {
  background: white;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.route-path {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.route-point {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
}

.route-point.start i {
  color: #4CAF50;
}

.route-point.end i {
  color: #f44336;
}

.route-arrow {
  color: #6c757d;
  font-size: 1.1rem;
}

.route-details {
  display: flex;
  gap: 2rem;
  color: #6c757d;
  font-size: 0.9rem;
}

.route-distance,
.route-frequency {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.route-distance i,
.route-frequency i {
  color: #4CAF50;
}

.volume-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.volume-item {
  background: white;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.volume-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #495057;
  font-size: 0.9rem;
}

.volume-label i {
  color: #4CAF50;
}

.volume-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: #2c3e50;
}

.optimization-options {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.optimization-item {
  display: flex;
  gap: 1rem;
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.optimization-icon {
  width: 60px;
  height: 60px;
  background: #e8f5e8;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.optimization-icon i {
  color: #4CAF50;
  font-size: 1.5rem;
}

.optimization-content h4 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.optimization-content p {
  margin: 0 0 1rem 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.optimization-benefits {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.benefit-tag {
  background: #e8f5e8;
  color: #2e7d32;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
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
  
  .categories-grid {
    grid-template-columns: 1fr;
  }
  
  .volume-grid {
    grid-template-columns: 1fr;
  }
  
  .condition-group {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }
  
  .route-path {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .route-details {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .optimization-item {
    flex-direction: column;
    text-align: center;
  }
}
</style>