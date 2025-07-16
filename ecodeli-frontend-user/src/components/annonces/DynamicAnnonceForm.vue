<template>
  <div class="dynamic-annonce-form">
    <!-- Form Header -->
    <div class="form-header">
      <div class="breadcrumb">
        <span class="breadcrumb-item">Nouvelle annonce</span>
        <i class="pi pi-chevron-right"></i>
        <span class="breadcrumb-item active">{{ getCategoryTitle() }}</span>
      </div>
      
      <div class="form-title">
        <div class="category-icon">
          <i :class="getCategoryIcon()"></i>
        </div>
        <div class="title-content">
          <h2>{{ getCategoryTitle() }}</h2>
          <p class="subtitle">{{ getCategoryDescription() }}</p>
        </div>
      </div>
    </div>

    <!-- Progress Indicator -->
    <div class="progress-indicator">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: `${progressPercentage}%` }"></div>
      </div>
      <div class="progress-text">{{ completedSteps }}/{{ totalSteps }} étapes complétées</div>
    </div>

    <!-- Form Content -->
    <form @submit.prevent="submitForm" class="annonce-form">
      <!-- Basic Information Section -->
      <div class="form-section" :class="{ 'completed': basicInfoCompleted }">
        <div class="section-header">
          <div class="section-icon">
            <i class="pi pi-info-circle"></i>
          </div>
          <div class="section-title">
            <h3>Informations de base</h3>
            <p>Définissez les informations générales de votre annonce</p>
          </div>
          <div class="section-status">
            <i class="pi pi-check" v-if="basicInfoCompleted"></i>
          </div>
        </div>
        
        <div class="section-content">
          <div class="form-group">
            <label for="titre" class="form-label">
              Titre de l'annonce *
              <span class="help-text">Un titre clair et descriptif</span>
            </label>
            <input
              type="text"
              id="titre"
              v-model="formData.titre"
              class="form-input"
              :class="{ 'error': errors.titre }"
              placeholder="Ex: Livraison urgente centre-ville"
              maxlength="200"
              @blur="validateField('titre')"
            />
            <div class="field-error" v-if="errors.titre">{{ errors.titre }}</div>
            <div class="char-count">{{ formData.titre.length }}/200</div>
          </div>

          <div class="form-group">
            <label for="description" class="form-label">
              Description
              <span class="help-text">Décrivez en détail votre demande</span>
            </label>
            <textarea
              id="description"
              v-model="formData.description"
              class="form-textarea"
              :class="{ 'error': errors.description }"
              placeholder="Décrivez votre besoin en détail..."
              rows="4"
              @blur="validateField('description')"
            ></textarea>
            <div class="field-error" v-if="errors.description">{{ errors.description }}</div>
          </div>
        </div>
      </div>

      <!-- Category-Specific Sections -->
      <component 
        :is="categoryFormComponent"
        v-model="formData"
        :errors="errors"
        :loading="loading"
        @validate="validateCategoryFields"
        @update-step="updateStepCompletion"
      />

      <!-- Price Estimation Section -->
      <div class="form-section" :class="{ 'completed': priceEstimated }">
        <div class="section-header">
          <div class="section-icon">
            <i class="pi pi-euro"></i>
          </div>
          <div class="section-title">
            <h3>Estimation du prix</h3>
            <p>Prix estimé basé sur vos critères</p>
          </div>
          <div class="section-status">
            <i class="pi pi-check" v-if="priceEstimated"></i>
          </div>
        </div>
        
        <div class="section-content">
          <div class="price-estimation" v-if="estimatedPrice">
            <div class="price-display">
              <div class="price-amount">{{ estimatedPrice.estimatedPrice }}€</div>
              <div class="price-label">Prix estimé</div>
            </div>
            
            <div class="price-breakdown" v-if="estimatedPrice.priceBreakdown">
              <h4>Détail du calcul :</h4>
              <div class="breakdown-item" v-for="(value, key) in estimatedPrice.priceBreakdown" :key="key">
                <span class="breakdown-label">{{ getBreakdownLabel(key) }}</span>
                <span class="breakdown-value">{{ value }}€</span>
              </div>
            </div>
            
            <button 
              type="button" 
              class="btn btn-outline"
              @click="recalculatePrice"
              :disabled="loading"
            >
              <i class="pi pi-refresh"></i>
              Recalculer
            </button>
          </div>
          
          <div class="price-loading" v-else-if="loading">
            <i class="pi pi-spinner pi-spin"></i>
            Calcul en cours...
          </div>
          
          <div class="price-placeholder" v-else>
            <i class="pi pi-info-circle"></i>
            <p>Complétez les informations pour obtenir une estimation</p>
          </div>
        </div>
      </div>

      <!-- Form Actions -->
      <div class="form-actions">
        <div class="actions-left">
          <button 
            type="button" 
            class="btn btn-secondary"
            @click="$emit('back')"
            :disabled="loading"
          >
            <i class="pi pi-arrow-left"></i>
            Retour
          </button>
          
          <button 
            type="button" 
            class="btn btn-outline"
            @click="saveDraft"
            :disabled="loading"
          >
            <i class="pi pi-save"></i>
            Enregistrer brouillon
          </button>
        </div>
        
        <div class="actions-right">
          <button 
            type="button" 
            class="btn btn-outline"
            @click="previewAnnonce"
            :disabled="!isFormValid || loading"
          >
            <i class="pi pi-eye"></i>
            Aperçu
          </button>
          
          <button 
            type="submit" 
            class="btn btn-primary"
            :disabled="!isFormValid || loading"
          >
            <i class="pi pi-check"></i>
            {{ loading ? 'Création...' : 'Créer l\'annonce' }}
          </button>
        </div>
      </div>
    </form>

    <!-- Preview Modal -->
    <div class="modal" v-if="showPreview" @click="closePreview">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Aperçu de votre annonce</h3>
          <button class="modal-close" @click="closePreview">
            <i class="pi pi-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <component 
            :is="previewComponent"
            :annonce-data="formData"
            :category="selectedCategory"
          />
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closePreview">
            Fermer
          </button>
          <button class="btn btn-primary" @click="submitFromPreview">
            <i class="pi pi-check"></i>
            Confirmer et créer
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useAnnonceCommercantStore } from '@/stores/annonceCommercant'
import { useAuthStore } from '@/stores/auth'

import LivraisonPonctuelleForm from './forms/LivraisonPonctuelleForm.vue'
import ServiceChariotForm from './forms/ServiceChariotForm.vue'
import TransportMarchandisesForm from './forms/TransportMarchandisesForm.vue'

import LivraisonPonctuellePreview from './previews/LivraisonPonctuellePreview.vue'
import ServiceChariotPreview from './previews/ServiceChariotPreview.vue'
import TransportMarchandisesPreview from './previews/TransportMarchandisesPreview.vue'

const props = defineProps({
  selectedCategory: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['back', 'created', 'draft-saved'])

const annonceStore = useAnnonceCommercantStore()
const authStore = useAuthStore()

const formData = ref({
  titre: '',
  description: '',
  commercantId: authStore.user?.id,
  categorie: props.selectedCategory
})

const errors = ref({})
const loading = ref(false)
const showPreview = ref(false)
const estimatedPrice = ref(null)
const stepCompletion = ref({
  basicInfo: false,
  categorySpecific: false,
  priceEstimation: false
})

const categoryFormComponent = computed(() => {
  const components = {
    'LIVRAISON_PONCTUELLE': LivraisonPonctuelleForm,
    'SERVICE_CHARIOT': ServiceChariotForm,
    'TRANSPORT_MARCHANDISES': TransportMarchandisesForm
  }
  return components[props.selectedCategory]
})

const previewComponent = computed(() => {
  const components = {
    'LIVRAISON_PONCTUELLE': LivraisonPonctuellePreview,
    'SERVICE_CHARIOT': ServiceChariotPreview,
    'TRANSPORT_MARCHANDISES': TransportMarchandisesPreview
  }
  return components[props.selectedCategory]
})

const basicInfoCompleted = computed(() => {
  return formData.value.titre.length > 0 && formData.value.description.length > 0
})

const priceEstimated = computed(() => {
  return estimatedPrice.value !== null
})

const completedSteps = computed(() => {
  return Object.values(stepCompletion.value).filter(Boolean).length
})

const totalSteps = computed(() => {
  return Object.keys(stepCompletion.value).length
})

const progressPercentage = computed(() => {
  return (completedSteps.value / totalSteps.value) * 100
})

const isFormValid = computed(() => {
  return Object.keys(errors.value).length === 0 && 
         basicInfoCompleted.value && 
         stepCompletion.value.categorySpecific
})

const getCategoryIcon = () => {
  const icons = {
    'LIVRAISON_PONCTUELLE': 'pi pi-clock',
    'SERVICE_CHARIOT': 'pi pi-shopping-cart',
    'TRANSPORT_MARCHANDISES': 'pi pi-truck'
  }
  return icons[props.selectedCategory] || 'pi pi-circle'
}

const getCategoryTitle = () => {
  const titles = {
    'LIVRAISON_PONCTUELLE': 'Livraison ponctuelle',
    'SERVICE_CHARIOT': 'Service "Lâcher de chariot"',
    'TRANSPORT_MARCHANDISES': 'Transport de marchandises'
  }
  return titles[props.selectedCategory] || ''
}

const getCategoryDescription = () => {
  const descriptions = {
    'LIVRAISON_PONCTUELLE': 'Pour des besoins de livraison uniques ou occasionnels',
    'SERVICE_CHARIOT': 'Services de livraison à domicile pour commerçants',
    'TRANSPORT_MARCHANDISES': 'Transport régulier ou en volume'
  }
  return descriptions[props.selectedCategory] || ''
}

const validateField = (fieldName) => {
  switch (fieldName) {
    case 'titre':
      if (!formData.value.titre || formData.value.titre.trim().length === 0) {
        errors.value.titre = 'Le titre est obligatoire'
      } else if (formData.value.titre.length > 200) {
        errors.value.titre = 'Le titre ne peut pas dépasser 200 caractères'
      } else {
        delete errors.value.titre
      }
      break
    case 'description':
      if (formData.value.description && formData.value.description.length > 1000) {
        errors.value.description = 'La description ne peut pas dépasser 1000 caractères'
      } else {
        delete errors.value.description
      }
      break
  }
  
  updateStepCompletion('basicInfo', basicInfoCompleted.value)
}

const validateCategoryFields = (categoryErrors) => {
  errors.value = { ...errors.value, ...categoryErrors }
  
  Object.keys(errors.value).forEach(key => {
    if (!categoryErrors[key] && !['titre', 'description'].includes(key)) {
      delete errors.value[key]
    }
  })
}

const updateStepCompletion = (step, completed) => {
  stepCompletion.value[step] = completed
  
  if (step === 'categorySpecific' && completed && !estimatedPrice.value) {
    calculatePrice()
  }
}

const calculatePrice = async () => {
  try {
    loading.value = true
    const result = await annonceStore.estimatePrice(formData.value)
    
    if (result.success) {
      estimatedPrice.value = result.data
      stepCompletion.value.priceEstimation = true
    }
  } catch (error) {
    console.error('Erreur lors du calcul du prix:', error)
  } finally {
    loading.value = false
  }
}

const recalculatePrice = () => {
  estimatedPrice.value = null
  stepCompletion.value.priceEstimation = false
  calculatePrice()
}

const getBreakdownLabel = (key) => {
  const labels = {
    'basePrice': 'Prix de base',
    'categoryMultiplier': 'Multiplicateur catégorie',
    'additionalServices': 'Services additionnels',
    'distancePrice': 'Prix distance',
    'weightPrice': 'Prix poids',
    'urgencyPrice': 'Prix urgence'
  }
  return labels[key] || key
}

const previewAnnonce = () => {
  showPreview.value = true
}

const closePreview = () => {
  showPreview.value = false
}

const submitFromPreview = () => {
  closePreview()
  submitForm()
}

const submitForm = async () => {
  try {
    loading.value = true
    
    validateField('titre')
    validateField('description')
    
    if (!isFormValid.value) {
      return
    }
    
    const result = await annonceStore.createAnnonce(formData.value)
    
    if (result.success) {
      emit('created', result.data)
    } else {
      if (result.error.includes('validation')) {
        console.error('Erreur de validation:', result.error)
      }
    }
  } catch (error) {
    console.error('Erreur lors de la création:', error)
  } finally {
    loading.value = false
  }
}

const saveDraft = async () => {
  try {
    loading.value = true
    
    const draftData = {
      ...formData.value,
      statut: 'BROUILLON'
    }
    
    const result = await annonceStore.createAnnonce(draftData)
    
    if (result.success) {
      emit('draft-saved', result.data)
    }
  } catch (error) {
    console.error('Erreur lors de la sauvegarde:', error)
  } finally {
    loading.value = false
  }
}

watch(() => formData.value.titre, () => {
  if (formData.value.titre) {
    validateField('titre')
  }
})

watch(() => formData.value.description, () => {
  if (formData.value.description) {
    validateField('description')
  }
})

watch(() => stepCompletion.value, () => {
  if (stepCompletion.value.basicInfo && 
      stepCompletion.value.categorySpecific && 
      !stepCompletion.value.priceEstimation) {
    calculatePrice()
  }
}, { deep: true })

onMounted(() => {
  formData.value.categorie = props.selectedCategory
  formData.value.commercantId = authStore.user?.id
})
</script>

<style scoped>
.dynamic-annonce-form {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.form-header {
  margin-bottom: 2rem;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  color: #6c757d;
}

.breadcrumb-item.active {
  color: #4CAF50;
  font-weight: 600;
}

.form-title {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.category-icon {
  background: linear-gradient(135deg, #e8f5e8, #f0f9f0);
  border: 2px solid #4CAF50;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-icon i {
  font-size: 1.5rem;
  color: #4CAF50;
}

.title-content h2 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.8rem;
}

.subtitle {
  margin: 0;
  color: #7f8c8d;
  font-size: 1rem;
}

.progress-indicator {
  margin-bottom: 2rem;
}

.progress-bar {
  background: #e9ecef;
  height: 4px;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-fill {
  background: linear-gradient(90deg, #4CAF50, #66BB6A);
  height: 100%;
  transition: width 0.3s ease;
}

.progress-text {
  text-align: center;
  font-size: 0.9rem;
  color: #6c757d;
}

.form-section {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  margin-bottom: 2rem;
  overflow: hidden;
  transition: all 0.3s ease;
}

.form-section.completed {
  border-color: #4CAF50;
  background: linear-gradient(to right, #f8fff8, white);
}

.section-header {
  background: #f8f9fa;
  padding: 1.5rem;
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
  font-size: 1.2rem;
}

.section-title p {
  margin: 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.section-status i {
  color: #4CAF50;
  font-size: 1.1rem;
}

.section-content {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.help-text {
  display: block;
  font-weight: 400;
  color: #6c757d;
  font-size: 0.85rem;
  margin-top: 0.25rem;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
}

.form-input.error,
.form-textarea.error {
  border-color: #dc3545;
}

.field-error {
  color: #dc3545;
  font-size: 0.85rem;
  margin-top: 0.25rem;
}

.char-count {
  text-align: right;
  font-size: 0.8rem;
  color: #6c757d;
  margin-top: 0.25rem;
}

.price-estimation {
  text-align: center;
}

.price-display {
  margin-bottom: 1.5rem;
}

.price-amount {
  font-size: 2.5rem;
  font-weight: 700;
  color: #4CAF50;
  margin-bottom: 0.5rem;
}

.price-label {
  color: #6c757d;
  font-size: 1rem;
}

.price-breakdown {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1.5rem;
  text-align: left;
}

.price-breakdown h4 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1rem;
}

.breakdown-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.breakdown-label {
  color: #6c757d;
}

.breakdown-value {
  font-weight: 600;
  color: #2c3e50;
}

.price-loading,
.price-placeholder {
  text-align: center;
  color: #6c757d;
  padding: 2rem;
}

.price-loading i {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.price-placeholder i {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 2rem;
  border-top: 1px solid #e9ecef;
}

.actions-left,
.actions-right {
  display: flex;
  gap: 1rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  border: none;
  font-size: 0.9rem;
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

.btn-outline {
  background: transparent;
  color: #4CAF50;
  border: 1px solid #4CAF50;
}

.btn-outline:hover:not(:disabled) {
  background: #4CAF50;
  color: white;
}

.btn-primary {
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #45a049, #5cb85c);
  transform: translateY(-1px);
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 800px;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: #6c757d;
}

.modal-body {
  flex: 1;
  padding: 1.5rem;
  overflow-y: auto;
}

.modal-footer {
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

@media (max-width: 768px) {
  .dynamic-annonce-form {
    padding: 1rem;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 1rem;
  }
  
  .actions-left,
  .actions-right {
    width: 100%;
    justify-content: center;
  }
  
  .btn {
    flex: 1;
    justify-content: center;
  }
  
  .modal-content {
    width: 95%;
    max-height: 90vh;
  }
}
</style>