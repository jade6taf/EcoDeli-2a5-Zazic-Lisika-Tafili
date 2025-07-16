<template>
  <div class="category-selection">
    <div class="header">
      <h2>Créer une nouvelle annonce</h2>
      <p class="subtitle">Choisissez la catégorie qui correspond le mieux à vos besoins</p>
    </div>

    <div class="categories-grid">
      <div 
        v-for="category in categories" 
        :key="category.code"
        class="category-card"
        :class="{ 'selected': selectedCategory === category.code }"
        @click="selectCategory(category.code)"
      >
        <div class="category-icon">
          <i :class="getCategoryIcon(category.code)"></i>
        </div>
        
        <div class="category-content">
          <h3>{{ category.libelle }}</h3>
          <p class="category-description">{{ category.description }}</p>
          
          <div class="category-features">
            <div class="features-title">Fonctionnalités incluses :</div>
            <ul class="features-list">
              <li v-for="feature in getCategoryFeatures(category.code)" :key="feature">
                <i class="pi pi-check"></i>
                {{ feature }}
              </li>
            </ul>
          </div>
        </div>
        
        <div class="category-action">
          <button 
            class="select-btn"
            :class="{ 'selected': selectedCategory === category.code }"
          >
            <i class="pi pi-check" v-if="selectedCategory === category.code"></i>
            <span>{{ selectedCategory === category.code ? 'Sélectionné' : 'Choisir' }}</span>
          </button>
        </div>
      </div>
    </div>

    <div class="help-section" v-if="selectedCategory">
      <div class="help-card">
        <div class="help-header">
          <i class="pi pi-info-circle"></i>
          <h4>Guide pour {{ getSelectedCategoryTitle() }}</h4>
        </div>
        <div class="help-content">
          <div class="help-description">
            {{ contextualHelp.description }}
          </div>
          <div class="help-steps">
            <h5>Étapes à suivre :</h5>
            <ol>
              <li v-for="(step, index) in contextualHelp.etapes" :key="index">
                {{ step }}
              </li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <div class="actions">
      <button 
        class="btn btn-secondary" 
        @click="$emit('cancel')"
        :disabled="loading"
      >
        Annuler
      </button>
      <button 
        class="btn btn-primary" 
        @click="continueToForm"
        :disabled="!selectedCategory || loading"
      >
        <i class="pi pi-arrow-right"></i>
        Continuer
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useAnnonceCommercantStore } from '@/stores/annonceCommercant'

const emit = defineEmits(['category-selected', 'cancel'])

const annonceStore = useAnnonceCommercantStore()

const selectedCategory = ref(null)
const loading = ref(false)

const categories = computed(() => annonceStore.getCategoriesList)
const contextualHelp = computed(() => {
  if (!selectedCategory.value) return {}
  return annonceStore.contextualHelp[selectedCategory.value] || {}
})

const selectCategory = (categoryCode) => {
  selectedCategory.value = categoryCode
  loadContextualHelp(categoryCode)
}

const getCategoryIcon = (categoryCode) => {
  const icons = {
    'LIVRAISON_PONCTUELLE': 'pi pi-clock',
    'SERVICE_CHARIOT': 'pi pi-shopping-cart',
    'TRANSPORT_MARCHANDISES': 'pi pi-truck'
  }
  return icons[categoryCode] || 'pi pi-circle'
}

const getCategoryFeatures = (categoryCode) => {
  const features = {
    'LIVRAISON_PONCTUELLE': [
      'Planification précise date/heure',
      'Suivi en temps réel',
      'Vérification de disponibilité',
      'Contact direct destinataire'
    ],
    'SERVICE_CHARIOT': [
      'Gestion des créneaux',
      'Zone de couverture définie',
      'Commande minimum configurable',
      'Disponibilité continue'
    ],
    'TRANSPORT_MARCHANDISES': [
      'Transport régulier ou ponctuel',
      'Optimisation des itinéraires',
      'Conditions spéciales',
      'Planification de capacité'
    ]
  }
  return features[categoryCode] || []
}

const getSelectedCategoryTitle = () => {
  if (!selectedCategory.value) return ''
  const category = categories.value.find(c => c.code === selectedCategory.value)
  return category?.libelle || ''
}

const loadContextualHelp = async (categoryCode) => {
  try {
    loading.value = true
    await annonceStore.fetchContextualHelp(categoryCode)
  } catch (error) {
    console.error('Erreur lors du chargement de l\'aide:', error)
  } finally {
    loading.value = false
  }
}

const continueToForm = () => {
  if (selectedCategory.value) {
    emit('category-selected', selectedCategory.value)
  }
}

onMounted(async () => {
  try {
    loading.value = true
    await annonceStore.fetchCategories()
  } catch (error) {
    console.error('Erreur lors du chargement des catégories:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.category-selection {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  text-align: center;
  margin-bottom: 3rem;
}

.header h2 {
  color: #2c3e50;
  margin-bottom: 0.5rem;
  font-size: 2.5rem;
  font-weight: 600;
}

.subtitle {
  color: #7f8c8d;
  font-size: 1.2rem;
  margin: 0;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.category-card {
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.category-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #4CAF50, #66BB6A);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.category-card:hover {
  border-color: #4CAF50;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(76, 175, 80, 0.15);
}

.category-card:hover::before {
  opacity: 1;
}

.category-card.selected {
  border-color: #4CAF50;
  background: linear-gradient(135deg, #f8fff8, #e8f5e8);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(76, 175, 80, 0.2);
}

.category-card.selected::before {
  opacity: 1;
}

.category-icon {
  text-align: center;
  margin-bottom: 1.5rem;
}

.category-icon i {
  font-size: 3rem;
  color: #4CAF50;
  background: linear-gradient(135deg, #e8f5e8, #f0f9f0);
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #4CAF50;
}

.category-content h3 {
  color: #2c3e50;
  margin-bottom: 1rem;
  font-size: 1.5rem;
  font-weight: 600;
  text-align: center;
}

.category-description {
  color: #7f8c8d;
  margin-bottom: 1.5rem;
  text-align: center;
  line-height: 1.6;
}

.category-features {
  margin-bottom: 2rem;
}

.features-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
  font-size: 0.95rem;
}

.features-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.features-list li {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
  color: #555;
  font-size: 0.9rem;
}

.features-list li i {
  color: #4CAF50;
  margin-right: 0.5rem;
  font-size: 0.8rem;
}

.category-action {
  text-align: center;
}

.select-btn {
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  color: white;
  border: none;
  padding: 0.75rem 2rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  justify-content: center;
}

.select-btn:hover {
  background: linear-gradient(135deg, #45a049, #5cb85c);
  transform: translateY(-1px);
}

.select-btn.selected {
  background: linear-gradient(135deg, #2e7d32, #388e3c);
}

.help-section {
  margin-bottom: 3rem;
}

.help-card {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 2rem;
  border-left: 4px solid #4CAF50;
}

.help-header {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
}

.help-header i {
  color: #4CAF50;
  margin-right: 1rem;
  font-size: 1.2rem;
}

.help-header h4 {
  color: #2c3e50;
  margin: 0;
  font-size: 1.3rem;
}

.help-description {
  color: #555;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.help-steps h5 {
  color: #2c3e50;
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.help-steps ol {
  color: #555;
  padding-left: 1.5rem;
}

.help-steps li {
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 2rem;
  border-top: 1px solid #e9ecef;
}

.btn {
  padding: 0.75rem 2rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  border: none;
  font-size: 1rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.btn-secondary:hover:not(:disabled) {
  background: #e9ecef;
  color: #495057;
}

.btn-primary {
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #45a049, #5cb85c);
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .category-selection {
    padding: 1rem;
  }
  
  .categories-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .category-card {
    padding: 1.5rem;
  }
  
  .header h2 {
    font-size: 2rem;
  }
  
  .actions {
    flex-direction: column;
    gap: 1rem;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>