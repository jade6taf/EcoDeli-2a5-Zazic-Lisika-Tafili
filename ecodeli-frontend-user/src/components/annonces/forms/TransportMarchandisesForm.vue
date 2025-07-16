<template>
  <div class="transport-marchandises-form">
    <!-- Transport Frequency Section -->
    <div class="form-section" :class="{ 'completed': frequencyCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-calendar"></i>
        </div>
        <div class="section-title">
          <h3>Fréquence de transport</h3>
          <p>À quelle fréquence avez-vous besoin de transport</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="frequencyCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="frequency-selector">
          <label 
            v-for="frequency in frequencies" 
            :key="frequency.value"
            class="frequency-option"
            :class="{ 'selected': localData.frequenceTransport === frequency.value }"
          >
            <input
              type="radio"
              name="frequenceTransport"
              :value="frequency.value"
              v-model="localData.frequenceTransport"
              @change="validateFrequency"
            />
            <div class="frequency-card">
              <div class="frequency-icon">
                <i :class="frequency.icon"></i>
              </div>
              <div class="frequency-content">
                <h4>{{ frequency.label }}</h4>
                <p>{{ frequency.description }}</p>
              </div>
            </div>
          </label>
        </div>
        <div class="field-error" v-if="errors.frequenceTransport">
          {{ errors.frequenceTransport }}
        </div>
      </div>
    </div>

    <!-- Merchandise Categories Section -->
    <div class="form-section" :class="{ 'completed': merchandiseCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-tags"></i>
        </div>
        <div class="section-title">
          <h3>Catégories de marchandises</h3>
          <p>Types de produits à transporter</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="merchandiseCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="merchandise-categories">
          <div 
            v-for="category in merchandiseCategories" 
            :key="category.id"
            class="category-checkbox"
            :class="{ 'selected': selectedCategories.includes(category.id) }"
          >
            <label>
              <input
                type="checkbox"
                :value="category.id"
                v-model="selectedCategories"
                @change="updateCategoriesMarchandises"
              />
              <div class="category-card">
                <div class="category-icon">
                  <i :class="category.icon"></i>
                </div>
                <div class="category-info">
                  <h4>{{ category.name }}</h4>
                  <p>{{ category.description }}</p>
                </div>
              </div>
            </label>
          </div>
        </div>
        <div class="field-error" v-if="errors.categoriesMarchandises">
          {{ errors.categoriesMarchandises }}
        </div>
      </div>
    </div>

    <!-- Transport Conditions Section -->
    <div class="form-section" :class="{ 'completed': conditionsCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-shield"></i>
        </div>
        <div class="section-title">
          <h3>Conditions de transport</h3>
          <p>Exigences spéciales pour le transport</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="conditionsCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="transport-conditions">
          <div class="condition-group">
            <h4>Conditions de température</h4>
            <div class="condition-options">
              <label class="condition-option">
                <input
                  type="radio"
                  name="temperature"
                  value="normale"
                  v-model="transportConditions.temperature"
                  @change="updateConditionsTransport"
                />
                <span>Température normale</span>
              </label>
              <label class="condition-option">
                <input
                  type="radio"
                  name="temperature"
                  value="refrigeree"
                  v-model="transportConditions.temperature"
                  @change="updateConditionsTransport"
                />
                <span>Réfrigérée (2-8°C)</span>
              </label>
              <label class="condition-option">
                <input
                  type="radio"
                  name="temperature"
                  value="congelee"
                  v-model="transportConditions.temperature"
                  @change="updateConditionsTransport"
                />
                <span>Congelée (-18°C)</span>
              </label>
            </div>
          </div>

          <div class="condition-group">
            <h4>Fragilité</h4>
            <div class="condition-options">
              <label class="condition-option">
                <input
                  type="radio"
                  name="fragilite"
                  value="normale"
                  v-model="transportConditions.fragilite"
                  @change="updateConditionsTransport"
                />
                <span>Normale</span>
              </label>
              <label class="condition-option">
                <input
                  type="radio"
                  name="fragilite"
                  value="fragile"
                  v-model="transportConditions.fragilite"
                  @change="updateConditionsTransport"
                />
                <span>Fragile</span>
              </label>
              <label class="condition-option">
                <input
                  type="radio"
                  name="fragilite"
                  value="tres_fragile"
                  v-model="transportConditions.fragilite"
                  @change="updateConditionsTransport"
                />
                <span>Très fragile</span>
              </label>
            </div>
          </div>

          <div class="condition-group">
            <h4>Autres conditions</h4>
            <div class="special-conditions">
              <label class="special-condition">
                <input
                  type="checkbox"
                  value="etanche"
                  v-model="transportConditions.special"
                  @change="updateConditionsTransport"
                />
                <span>Étanche</span>
              </label>
              <label class="special-condition">
                <input
                  type="checkbox"
                  value="vertical"
                  v-model="transportConditions.special"
                  @change="updateConditionsTransport"
                />
                <span>Maintenir vertical</span>
              </label>
              <label class="special-condition">
                <input
                  type="checkbox"
                  value="assure"
                  v-model="transportConditions.special"
                  @change="updateConditionsTransport"
                />
                <span>Assuré</span>
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Routes and Volume Section -->
    <div class="form-section" :class="{ 'completed': routesCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-map-marker"></i>
        </div>
        <div class="section-title">
          <h3>Itinéraires et volume</h3>
          <p>Définissez vos trajets et volumes</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="routesCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="routes-manager">
          <div class="routes-list">
            <div 
              v-for="(route, index) in routes" 
              :key="index"
              class="route-item"
            >
              <div class="route-info">
                <div class="route-path">
                  <i class="pi pi-arrow-right"></i>
                  {{ route.depart }} → {{ route.arrivee }}
                </div>
                <div class="route-details">
                  Distance: {{ route.distance }}km | Fréquence: {{ route.frequence }}
                </div>
              </div>
              <button 
                type="button" 
                class="route-remove"
                @click="removeRoute(index)"
              >
                <i class="pi pi-times"></i>
              </button>
            </div>
            
            <div class="no-routes" v-if="routes.length === 0">
              <i class="pi pi-info-circle"></i>
              <p>Aucun itinéraire défini</p>
            </div>
          </div>
          
          <div class="add-route-form">
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Départ</label>
                <input
                  type="text"
                  v-model="newRoute.depart"
                  class="form-input"
                  placeholder="Ville de départ"
                />
              </div>
              
              <div class="form-group">
                <label class="form-label">Arrivée</label>
                <input
                  type="text"
                  v-model="newRoute.arrivee"
                  class="form-input"
                  placeholder="Ville d'arrivée"
                />
              </div>
              
              <div class="form-group">
                <label class="form-label">Distance (km)</label>
                <input
                  type="number"
                  v-model.number="newRoute.distance"
                  class="form-input"
                  min="1"
                  placeholder="100"
                />
              </div>
              
              <div class="form-group">
                <label class="form-label">Fréquence</label>
                <select v-model="newRoute.frequence" class="form-input">
                  <option value="quotidienne">Quotidienne</option>
                  <option value="hebdomadaire">Hebdomadaire</option>
                  <option value="mensuelle">Mensuelle</option>
                </select>
              </div>
              
              <div class="form-group">
                <button 
                  type="button" 
                  class="btn btn-add-route"
                  @click="addRoute"
                  :disabled="!canAddRoute"
                >
                  <i class="pi pi-plus"></i>
                  Ajouter
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="volume-section">
          <div class="form-row">
            <div class="form-group">
              <label for="volumeEstime" class="form-label">
                Volume estimé (m³)
                <span class="help-text">Volume total estimé par transport</span>
              </label>
              <input
                type="number"
                id="volumeEstime"
                v-model.number="localData.volumeEstime"
                class="form-input"
                :class="{ 'error': errors.volumeEstime }"
                placeholder="10"
                min="0.1"
                max="1000"
                step="0.1"
                @blur="validateVolume"
              />
              <div class="field-error" v-if="errors.volumeEstime">
                {{ errors.volumeEstime }}
              </div>
            </div>

            <div class="form-group">
              <label for="capaciteVehiculeRequise" class="form-label">
                Capacité véhicule requise
                <span class="help-text">Type de véhicule nécessaire</span>
              </label>
              <select
                id="capaciteVehiculeRequise"
                v-model="localData.capaciteVehiculeRequise"
                class="form-input"
                :class="{ 'error': errors.capaciteVehiculeRequise }"
                @change="validateCapaciteVehicule"
              >
                <option value="">Sélectionnez...</option>
                <option value="utilitaire">Utilitaire (jusqu'à 3m³)</option>
                <option value="fourgon">Fourgon (3-10m³)</option>
                <option value="camion">Camion (10-30m³)</option>
                <option value="semi_remorque">Semi-remorque (30m³+)</option>
              </select>
              <div class="field-error" v-if="errors.capaciteVehiculeRequise">
                {{ errors.capaciteVehiculeRequise }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Optimization Options Section -->
    <div class="form-section" :class="{ 'completed': optimizationCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-cog"></i>
        </div>
        <div class="section-title">
          <h3>Options d'optimisation</h3>
          <p>Améliorez l'efficacité de vos transports</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="optimizationCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="optimization-options">
          <div class="optimization-card">
            <label class="optimization-label">
              <input
                type="checkbox"
                v-model="localData.optimisationRoute"
                @change="handleOptimizationToggle"
              />
              <div class="optimization-content">
                <div class="optimization-header">
                  <i class="pi pi-route"></i>
                  <h4>Optimisation des itinéraires</h4>
                </div>
                <p>Calcul automatique des trajets les plus efficaces</p>
                <div class="optimization-benefits">
                  <span class="benefit">📉 Réduction des coûts</span>
                  <span class="benefit">⏱️ Gain de temps</span>
                  <span class="benefit">🌱 Écologique</span>
                </div>
              </div>
            </label>
          </div>

          <div class="optimization-card">
            <label class="optimization-label">
              <input
                type="checkbox"
                v-model="localData.planificationCapacite"
                @change="handleCapacityPlanningToggle"
              />
              <div class="optimization-content">
                <div class="optimization-header">
                  <i class="pi pi-chart-bar"></i>
                  <h4>Planification de capacité</h4>
                </div>
                <p>Optimisation du remplissage des véhicules</p>
                <div class="optimization-benefits">
                  <span class="benefit">📦 Maximise l'utilisation</span>
                  <span class="benefit">💰 Économies</span>
                  <span class="benefit">📊 Prévisions</span>
                </div>
              </div>
            </label>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useAnnonceCommercantStore } from '@/stores/annonceCommercant'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  },
  errors: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'validate', 'update-step'])

const annonceStore = useAnnonceCommercantStore()

const localData = ref({
  frequenceTransport: '',
  categoriesMarchandises: '[]',
  conditionsTransport: '{}',
  itinerairesDetailles: '[]',
  volumeEstime: null,
  capaciteVehiculeRequise: '',
  optimisationRoute: false,
  planificationCapacite: false,
  ...props.modelValue
})

const selectedCategories = ref([])
const transportConditions = ref({
  temperature: 'normale',
  fragilite: 'normale',
  special: []
})
const routes = ref([])
const newRoute = ref({
  depart: '',
  arrivee: '',
  distance: null,
  frequence: 'hebdomadaire'
})

const errors = ref({})

const frequencies = [
  {
    value: 'UNIQUE',
    label: 'Une seule fois',
    description: 'Transport ponctuel',
    icon: 'pi pi-calendar-times'
  },
  {
    value: 'HEBDOMADAIRE',
    label: 'Hebdomadaire',
    description: 'Chaque semaine',
    icon: 'pi pi-calendar'
  },
  {
    value: 'MENSUELLE',
    label: 'Mensuelle',
    description: 'Chaque mois',
    icon: 'pi pi-calendar-plus'
  },
  {
    value: 'REGULIERE',
    label: 'Régulière',
    description: 'Plusieurs fois par semaine',
    icon: 'pi pi-refresh'
  }
]

const merchandiseCategories = [
  { id: 'alimentaire', name: 'Alimentaire', description: 'Produits alimentaires', icon: 'pi pi-shopping-bag' },
  { id: 'electronique', name: 'Électronique', description: 'Équipements électroniques', icon: 'pi pi-mobile' },
  { id: 'textile', name: 'Textile', description: 'Vêtements et textiles', icon: 'pi pi-heart' },
  { id: 'mobilier', name: 'Mobilier', description: 'Meubles et décoration', icon: 'pi pi-home' },
  { id: 'pharmaceutique', name: 'Pharmaceutique', description: 'Produits médicaux', icon: 'pi pi-plus' },
  { id: 'industriel', name: 'Industriel', description: 'Équipements industriels', icon: 'pi pi-cog' },
  { id: 'automobile', name: 'Automobile', description: 'Pièces auto', icon: 'pi pi-car' },
  { id: 'autre', name: 'Autre', description: 'Autres produits', icon: 'pi pi-box' }
]

const frequencyCompleted = computed(() => {
  return localData.value.frequenceTransport && localData.value.frequenceTransport.length > 0
})

const merchandiseCompleted = computed(() => {
  return selectedCategories.value.length > 0
})

const conditionsCompleted = computed(() => {
  return transportConditions.value.temperature && transportConditions.value.fragilite
})

const routesCompleted = computed(() => {
  return routes.value.length > 0 && localData.value.volumeEstime && localData.value.capaciteVehiculeRequise
})

const optimizationCompleted = computed(() => {
  return true
})

const allCompleted = computed(() => {
  return frequencyCompleted.value && 
         merchandiseCompleted.value && 
         conditionsCompleted.value && 
         routesCompleted.value &&
         Object.keys(errors.value).length === 0
})

const canAddRoute = computed(() => {
  return newRoute.value.depart && 
         newRoute.value.arrivee && 
         newRoute.value.distance > 0 &&
         newRoute.value.frequence
})

const validateFrequency = () => {
  const newErrors = {}
  
  if (!localData.value.frequenceTransport) {
    newErrors.frequenceTransport = 'La fréquence de transport est obligatoire'
  }
  
  updateErrors(newErrors)
}

const updateCategoriesMarchandises = () => {
  localData.value.categoriesMarchandises = JSON.stringify(selectedCategories.value)
  
  const newErrors = {}
  if (selectedCategories.value.length === 0) {
    newErrors.categoriesMarchandises = 'Sélectionnez au moins une catégorie'
  }
  
  updateErrors(newErrors)
}

const updateConditionsTransport = () => {
  localData.value.conditionsTransport = JSON.stringify(transportConditions.value)
}

const validateVolume = () => {
  const newErrors = {}
  
  if (!localData.value.volumeEstime || localData.value.volumeEstime <= 0) {
    newErrors.volumeEstime = 'Le volume estimé est obligatoire'
  } else if (localData.value.volumeEstime > 1000) {
    newErrors.volumeEstime = 'Le volume ne peut pas dépasser 1000m³'
  }
  
  updateErrors(newErrors)
}

const validateCapaciteVehicule = () => {
  const newErrors = {}
  
  if (!localData.value.capaciteVehiculeRequise) {
    newErrors.capaciteVehiculeRequise = 'La capacité véhicule est obligatoire'
  }
  
  updateErrors(newErrors)
}

const updateErrors = (newErrors) => {
  const keysToRemove = [
    'frequenceTransport', 'categoriesMarchandises', 'volumeEstime', 'capaciteVehiculeRequise'
  ]
  
  keysToRemove.forEach(key => {
    if (errors.value[key]) {
      delete errors.value[key]
    }
  })
  
  errors.value = { ...errors.value, ...newErrors }
  
  emit('validate', errors.value)
}

const addRoute = () => {
  if (!canAddRoute.value) return
  
  routes.value.push({ ...newRoute.value })
  
  newRoute.value = {
    depart: '',
    arrivee: '',
    distance: null,
    frequence: 'hebdomadaire'
  }
  
  updateItinerairesDetailles()
}

const removeRoute = (index) => {
  routes.value.splice(index, 1)
  updateItinerairesDetailles()
}

const updateItinerairesDetailles = () => {
  localData.value.itinerairesDetailles = JSON.stringify(routes.value)
}

const handleOptimizationToggle = () => {
  if (localData.value.optimisationRoute) {
    console.log('Route optimization enabled')
  }
}

const handleCapacityPlanningToggle = () => {
  if (localData.value.planificationCapacite) {
    console.log('Capacity planning enabled')
  }
}

const initializeData = () => {
  try {
    const categoriesData = JSON.parse(localData.value.categoriesMarchandises || '[]')
    selectedCategories.value = categoriesData
  } catch (e) {
    selectedCategories.value = []
  }
  
  try {
    const conditionsData = JSON.parse(localData.value.conditionsTransport || '{}')
    transportConditions.value = {
      temperature: 'normale',
      fragilite: 'normale',
      special: [],
      ...conditionsData
    }
  } catch (e) {
    transportConditions.value = {
      temperature: 'normale',
      fragilite: 'normale',
      special: []
    }
  }
  
  try {
    const routesData = JSON.parse(localData.value.itinerairesDetailles || '[]')
    routes.value = routesData
  } catch (e) {
    routes.value = []
  }
}

watch(localData, (newData) => {
  emit('update:modelValue', { ...props.modelValue, ...newData })
}, { deep: true })

watch(allCompleted, (completed) => {
  emit('update-step', 'categorySpecific', completed)
})

onMounted(() => {
  initializeData()
  
  validateFrequency()
  updateCategoriesMarchandises()
  validateVolume()
  validateCapaciteVehicule()
})
</script>

<style scoped>
.form-section {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  overflow: hidden;
  transition: all 0.3s ease;
}

.form-section.completed {
  border-color: #4CAF50;
  background: linear-gradient(to right, #f8fff8, white);
}

.section-header {
  background: #f8f9fa;
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e9ecef;
}

.form-section.completed .section-header {
  background: linear-gradient(135deg, #e8f5e8, #f0f9f0);
}

.section-icon {
  margin-right: 1rem;
}

.section-icon i {
  font-size: 1.2rem;
  color: #4CAF50;
}

.section-title {
  flex: 1;
}

.section-title h3 {
  margin: 0 0 0.25rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.section-title p {
  margin: 0;
  color: #6c757d;
  font-size: 0.85rem;
}

.section-status i {
  color: #4CAF50;
  font-size: 1.1rem;
}

.section-content {
  padding: 1.5rem;
}

.frequency-selector {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.frequency-option {
  cursor: pointer;
}

.frequency-option input {
  display: none;
}

.frequency-card {
  display: flex;
  align-items: center;
  padding: 1rem;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.frequency-option.selected .frequency-card {
  border-color: #4CAF50;
  background: linear-gradient(135deg, #f8fff8, #e8f5e8);
}

.frequency-card:hover {
  border-color: #4CAF50;
}

.frequency-icon {
  margin-right: 1rem;
}

.frequency-icon i {
  font-size: 1.5rem;
  color: #4CAF50;
}

.frequency-content h4 {
  margin: 0 0 0.25rem 0;
  color: #2c3e50;
  font-size: 1rem;
}

.frequency-content p {
  margin: 0;
  color: #6c757d;
  font-size: 0.85rem;
}

.merchandise-categories {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.category-checkbox {
  cursor: pointer;
}

.category-checkbox input {
  display: none;
}

.category-card {
  display: flex;
  align-items: center;
  padding: 1rem;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.category-checkbox.selected .category-card {
  border-color: #4CAF50;
  background: linear-gradient(135deg, #f8fff8, #e8f5e8);
}

.category-card:hover {
  border-color: #4CAF50;
}

.category-icon {
  margin-right: 0.75rem;
}

.category-icon i {
  font-size: 1.2rem;
  color: #4CAF50;
}

.category-info h4 {
  margin: 0 0 0.25rem 0;
  color: #2c3e50;
  font-size: 0.9rem;
}

.category-info p {
  margin: 0;
  color: #6c757d;
  font-size: 0.8rem;
}

.transport-conditions {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.condition-group h4 {
  margin: 0 0 0.75rem 0;
  color: #2c3e50;
  font-size: 1rem;
}

.condition-options {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.condition-option {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0.5rem 1rem;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.condition-option:hover {
  border-color: #4CAF50;
}

.condition-option input[type="radio"]:checked + span {
  color: #4CAF50;
  font-weight: 600;
}

.condition-option input[type="radio"] {
  margin-right: 0.5rem;
}

.special-conditions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.special-condition {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0.5rem 1rem;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.special-condition:hover {
  border-color: #4CAF50;
}

.special-condition input[type="checkbox"] {
  margin-right: 0.5rem;
}

.special-condition input[type="checkbox"]:checked + span {
  color: #4CAF50;
  font-weight: 600;
}

.routes-manager {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.routes-list {
  margin-bottom: 1rem;
}

.route-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 0.75rem;
  margin-bottom: 0.5rem;
}

.route-path {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.route-path i {
  margin: 0 0.5rem;
  color: #4CAF50;
}

.route-details {
  color: #6c757d;
  font-size: 0.85rem;
}

.route-remove {
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  width: 28px;
  height: 28px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s ease;
}

.route-remove:hover {
  background: #c82333;
}

.no-routes {
  text-align: center;
  color: #6c757d;
  padding: 2rem;
}

.no-routes i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.add-route-form .form-row {
  display: grid;
  grid-template-columns: 1fr 1fr 100px 120px auto;
  gap: 1rem;
  align-items: end;
}

.volume-section {
  margin-top: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9rem;
}

.help-text {
  display: block;
  font-weight: 400;
  color: #6c757d;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: border-color 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
}

.form-input.error {
  border-color: #dc3545;
}

.field-error {
  color: #dc3545;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.btn-add-route {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: background 0.3s ease;
}

.btn-add-route:hover:not(:disabled) {
  background: #45a049;
}

.btn-add-route:disabled {
  background: #dee2e6;
  color: #6c757d;
  cursor: not-allowed;
}

.optimization-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.optimization-card {
  border: 2px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.optimization-card:hover {
  border-color: #4CAF50;
}

.optimization-label {
  display: block;
  cursor: pointer;
}

.optimization-label input {
  display: none;
}

.optimization-label input:checked + .optimization-content {
  background: linear-gradient(135deg, #f8fff8, #e8f5e8);
}

.optimization-content {
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.optimization-header {
  display: flex;
  align-items: center;
  margin-bottom: 0.75rem;
}

.optimization-header i {
  font-size: 1.5rem;
  color: #4CAF50;
  margin-right: 0.75rem;
}

.optimization-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 1rem;
}

.optimization-content p {
  margin: 0 0 1rem 0;
  color: #6c757d;
  font-size: 0.85rem;
}

.optimization-benefits {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.benefit {
  background: #e8f5e8;
  color: #2e7d32;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }
  
  .add-route-form .form-row {
    grid-template-columns: 1fr;
  }
  
  .frequency-selector {
    grid-template-columns: 1fr;
  }
  
  .merchandise-categories {
    grid-template-columns: 1fr;
  }
  
  .optimization-options {
    grid-template-columns: 1fr;
  }
  
  .condition-options {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .special-conditions {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .section-content {
    padding: 1rem;
  }
  
  .section-header {
    padding: 1rem;
  }
}
</style>