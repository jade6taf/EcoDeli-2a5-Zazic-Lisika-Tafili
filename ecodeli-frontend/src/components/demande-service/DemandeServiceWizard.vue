<script>
import CategorySelector from './CategorySelector.vue'
import TransportForm from './forms/TransportForm.vue'
import ServiceDomicileForm from './forms/ServiceDomicileForm.vue'
import TravauxForm from './forms/TravauxForm.vue'
import CoursesForm from './forms/CoursesForm.vue'
import ServicesPersonnelsForm from './forms/ServicesPersonnelsForm.vue'
import EducationForm from './forms/EducationForm.vue'

export default {
  name: 'DemandeServiceWizard',
  components: {
    CategorySelector,
    TransportForm,
    ServiceDomicileForm,
    TravauxForm,
    CoursesForm,
    ServicesPersonnelsForm,
    EducationForm
  },
  props: {
    initialData: {
      type: Object,
      default: () => ({})
    },
    isEditing: {
      type: Boolean,
      default: false
    }
  },
  emits: ['submit', 'cancel'],
  data() {
    return {
      currentStep: 1,
      isSubmitting: false,
      confirmSubmission: false,
      budgetNegociable: false,
      errors: {},
      steps: [
        { title: 'Catégorie' },
        { title: 'Description' },
        { title: 'Détails spécifiques' },
        { title: 'Budget' },
        { title: 'Résumé' }
      ],
      formData: {
        titre: '',
        description: '',
        categorieService: '',
        budgetMin: null,
        budgetMax: null,
        detailsSpecifiques: {}
      }
    }
  },
  computed: {
    canProceedToNextStep() {
      switch (this.currentStep) {
        case 1:
          return this.formData.categorieService !== ''
        case 2:
          return this.formData.titre && this.formData.description
        case 3:
          return this.validateSpecificFields()
        case 4:
          return true
        default:
          return false
      }
    },
    currentCategoryComponent() {
      const componentMap = {
        'TRANSPORT_LIVRAISON': 'TransportForm',
        'SERVICES_DOMICILE': 'ServiceDomicileForm',
        'TRAVAUX_REPARATIONS': 'TravauxForm',
        'COURSES_ACHATS': 'CoursesForm',
        'SERVICES_PERSONNELS': 'ServicesPersonnelsForm',
        'EDUCATION_FORMATION': 'EducationForm'
      }
      return componentMap[this.formData.categorieService] || null
    },
    hasSpecificDetails() {
      return this.formData.detailsSpecifiques &&
             Object.keys(this.formData.detailsSpecifiques).length > 0
    }
  },
  watch: {
    initialData: {
      immediate: true,
      handler(newData) {
        if (newData && Object.keys(newData).length > 0) {
          this.formData = { ...this.formData, ...newData }
        }
      }
    }
  },
  methods: {
    onCategorySelected(categoryCode) {
      this.formData.detailsSpecifiques = {}
      this.errors = {}
    },

    getCategoryLabel(code) {
      const categoryMap = {
        'TRANSPORT_LIVRAISON': 'TRANSPORT & LIVRAISON',
        'SERVICES_DOMICILE': 'SERVICES À DOMICILE',
        'TRAVAUX_REPARATIONS': 'TRAVAUX & RÉPARATIONS',
        'COURSES_ACHATS': 'COURSES & ACHATS',
        'SERVICES_PERSONNELS': 'SERVICES PERSONNELS',
        'EDUCATION_FORMATION': 'ÉDUCATION & FORMATION'
      }
      return categoryMap[code] || ''
    },

    validateSpecificFields() {
      if (!this.currentCategoryComponent) {
        return false
      }

      const details = this.formData.detailsSpecifiques

      switch (this.formData.categorieService) {
        case 'TRANSPORT_LIVRAISON':
          return details.lieuDepart && details.lieuArrivee &&
                 details.dateHeure && details.typeTransport

        case 'SERVICES_DOMICILE':
          return details.typeService && details.typeService.length > 0 &&
                 details.dateSouhaitee && details.frequence

        case 'TRAVAUX_REPARATIONS':
          return details.typeTravaux && details.typeTravaux.length > 0 &&
                 details.descriptionTravaux && details.dateSouhaitee

        case 'COURSES_ACHATS':
          return details.typeAchat && details.listeArticles &&
                 details.adresseLivraison && details.dateLivraison

        case 'SERVICES_PERSONNELS':
          return details.typeService && details.descriptionBesoin &&
                 details.dateSouhaitee

        case 'EDUCATION_FORMATION':
          return details.typeFormation && details.matiereDomaine &&
                 details.niveau && details.modalite

        default:
          return Object.keys(details).length > 0
      }
    },

    onValidateSpecificDetails(isValid, errors = {}) {
      if (!isValid) {
        this.errors.detailsSpecifiques = errors
      } else {
        delete this.errors.detailsSpecifiques
      }
    },

    nextStep() {
      if (this.canProceedToNextStep && this.currentStep < 5) {
        this.currentStep++
      }
    },

    previousStep() {
      if (this.currentStep > 1) {
        this.currentStep--
      }
    },

    async handleSubmit() {
      if (!this.confirmSubmission) return

      this.isSubmitting = true

      try {
        const submitData = {
          ...this.formData,
          budgetNegociable: this.budgetNegociable
        }

        this.$emit('submit', submitData)
      } catch (error) {
        console.error('Erreur lors de la soumission:', error)
      } finally {
        this.isSubmitting = false
      }
    },

    saveToLocalStorage() {
      const dataToSave = {
        ...this.formData,
        currentStep: this.currentStep,
        budgetNegociable: this.budgetNegociable
      }
      localStorage.setItem('demande-service-draft', JSON.stringify(dataToSave))
    },

    loadFromLocalStorage() {
      const saved = localStorage.getItem('demande-service-draft')
      if (saved && !this.isEditing) {
        try {
          const data = JSON.parse(saved)
          this.formData = { ...this.formData, ...data }
          this.currentStep = data.currentStep || 1
          this.budgetNegociable = data.budgetNegociable || false
        } catch (e) {
          console.warn('Erreur lors du chargement des données sauvegardées')
        }
      }
    },

    clearLocalStorage() {
      localStorage.removeItem('demande-service-draft')
    }
  },

  watch: {
    formData: {
      deep: true,
      handler() {
        this.saveToLocalStorage()
      }
    }
  },

  mounted() {
    this.loadFromLocalStorage()
  },

  beforeUnmount() {
    if (this.isSubmitting) {
      this.clearLocalStorage()
    }
  }
}
</script>

<template>
  <div class="demande-service-wizard">
    <h1>{{ isEditing ? 'Modifier la demande de service' : 'Nouvelle demande de service' }}</h1>

    <!-- Indicateur d'étapes -->
    <div class="steps-indicator">
      <div
        v-for="(step, index) in steps"
        :key="index"
        class="step"
        :class="{
          'active': currentStep === index + 1,
          'completed': currentStep > index + 1
        }"
      >
        <div class="step-number">{{ index + 1 }}</div>
        <div class="step-title">{{ step.title }}</div>
      </div>
    </div>

    <!-- Contenu des étapes -->
    <form @submit.prevent="handleSubmit" class="wizard-form">

      <!-- Étape 1: Sélection de catégorie -->
      <div v-if="currentStep === 1" class="step-content">
        <CategorySelector 
          v-model="formData.categorieService"
          @category-selected="onCategorySelected"
        />
      </div>

      <!-- Étape 2: Informations de base -->
      <div v-if="currentStep === 2" class="step-content">
        <h2>Étape 2: Décrivez votre besoin</h2>

        <div class="form-group">
          <label for="titre">Titre de votre demande *</label>
          <input
            type="text"
            id="titre"
            v-model="formData.titre"
            placeholder="Ex: Montage d'un canapé d'angle"
            class="form-input"
            :class="{ 'error': errors.titre }"
            required
          >
          <span v-if="errors.titre" class="error-message">{{ errors.titre }}</span>
        </div>

        <div class="form-group">
          <label for="description">Description détaillée *</label>
          <textarea
            id="description"
            v-model="formData.description"
            placeholder="Décrivez précisément ce que vous attendez du prestataire..."
            class="form-textarea"
            :class="{ 'error': errors.description }"
            rows="5"
            required
          ></textarea>
          <span v-if="errors.description" class="error-message">{{ errors.description }}</span>
        </div>
      </div>

      <!-- Étape 3: Détails spécifiques -->
      <div v-if="currentStep === 3" class="step-content">
        <h2>Détails spécifiques - {{ getCategoryLabel(formData.categorieService) }}</h2>

        <component
          :is="currentCategoryComponent"
          v-model="formData.detailsSpecifiques"
          :errors="errors.detailsSpecifiques || {}"
          @validate="onValidateSpecificDetails"
        />
      </div>

      <!-- Étape 4: Budget -->
      <div v-if="currentStep === 4" class="step-content">
        <h2>Étape 4: Budget (optionnel mais recommandé)</h2>

        <div class="budget-section">
          <p class="budget-hint">Indiquer un budget aide les prestataires à mieux calibrer leurs offres</p>

          <div class="form-row">
            <div class="form-group">
              <label for="budgetMin">Budget minimum (€)</label>
              <input
                type="number"
                id="budgetMin"
                v-model="formData.budgetMin"
                placeholder="50"
                min="0"
                step="5"
                class="form-input"
              >
            </div>
            <div class="form-group">
              <label for="budgetMax">Budget maximum (€)</label>
              <input
                type="number"
                id="budgetMax"
                v-model="formData.budgetMax"
                placeholder="150"
                min="0"
                step="5"
                class="form-input"
              >
            </div>
          </div>

          <div class="checkbox-group">
            <input
              type="checkbox"
              id="budgetNegociable"
              v-model="budgetNegociable"
            >
            <label for="budgetNegociable">Budget négociable</label>
          </div>
        </div>
      </div>

      <!-- Étape 5: Résumé -->
      <div v-if="currentStep === 5" class="step-content">
        <h2>Étape 5: Résumé de votre demande</h2>

        <div class="summary-card">
          <h3>{{ formData.titre }}</h3>

          <div class="summary-section">
            <h4>📋 Service demandé</h4>
            <p><strong>Catégorie:</strong> {{ getCategoryLabel(formData.categorieService) }}</p>
          </div>

          <div class="summary-section">
            <h4>📝 Description</h4>
            <p>{{ formData.description }}</p>
          </div>

          <div v-if="hasSpecificDetails" class="summary-section">
            <h4>🔧 Détails spécifiques</h4>
            <div class="details-summary">
              <component
                :is="currentCategoryComponent"
                v-model="formData.detailsSpecifiques"
                :readonly="true"
                :summary="true"
              />
            </div>
          </div>

          <div v-if="formData.budgetMin || formData.budgetMax" class="summary-section">
            <h4>💰 Budget</h4>
            <p v-if="formData.budgetMin && formData.budgetMax">
              Entre {{ formData.budgetMin }}€ et {{ formData.budgetMax }}€
            </p>
            <p v-else-if="formData.budgetMin">
              À partir de {{ formData.budgetMin }}€
            </p>
            <p v-else-if="formData.budgetMax">
              Maximum {{ formData.budgetMax }}€
            </p>
          </div>
        </div>

        <div class="confirmation-checkbox">
          <input
            type="checkbox"
            id="confirmSubmission"
            v-model="confirmSubmission"
            required
          >
          <label for="confirmSubmission">
            Je confirme que les informations sont correctes et je souhaite {{ isEditing ? 'modifier' : 'publier' }} cette demande
          </label>
        </div>
      </div>

      <!-- Navigation -->
      <div class="form-navigation">
        <button
          type="button"
          @click="previousStep"
          class="btn btn-secondary"
          v-if="currentStep > 1"
        >
          ← Précédent
        </button>

        <button
          type="button"
          @click="nextStep"
          class="btn btn-primary"
          v-if="currentStep < 5"
          :disabled="!canProceedToNextStep"
        >
          Suivant →
        </button>

        <button
          type="submit"
          class="btn btn-success"
          v-if="currentStep === 5"
          :disabled="isSubmitting || !confirmSubmission"
        >
          {{ isSubmitting ? 'En cours...' : (isEditing ? 'Modifier la demande' : 'Publier la demande') }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.demande-service-wizard {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.demande-service-wizard h1 {
  text-align: center;
  color: var(--text-primary);
  margin-bottom: 2rem;
}

.steps-indicator {
  display: flex;
  justify-content: space-between;
  margin: 2rem 0;
  padding: 0 1rem;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  position: relative;
}

.step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 20px;
  left: 60%;
  right: -40%;
  height: 2px;
  background-color: var(--border-color);
  z-index: 1;
}

.step.completed:not(:last-child)::after {
  background-color: var(--success-color);
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--border-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  position: relative;
  z-index: 2;
  transition: all 0.3s ease;
}

.step.active .step-number {
  background-color: var(--primary-color);
  transform: scale(1.1);
}

.step.completed .step-number {
  background-color: var(--success-color);
}

.step.completed .step-number::after {
  content: '✓';
  position: absolute;
  font-size: 0.8rem;
}

.step-title {
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
  text-align: center;
  transition: all 0.3s ease;
}

.step.active .step-title {
  color: var(--primary-color);
  font-weight: 600;
}

.step.completed .step-title {
  color: var(--success-color);
  font-weight: 500;
}

.wizard-form {
  background-color: var(--card-bg);
  padding: 2rem;
  border-radius: 12px;
  box-shadow: var(--shadow);
  margin-top: 2rem;
}

.step-content h2 {
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  text-align: center;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  font-weight: 500;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 0.875rem;
  border: 2px solid var(--border-color);
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background-color: white;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input.error, .form-textarea.error {
  border-color: var(--error-color, #dc3545);
}

.error-message {
  color: var(--error-color, #dc3545);
  font-size: 0.875rem;
  margin-top: 0.25rem;
  display: block;
}

.budget-section {
  background-color: var(--background-color);
  padding: 1.5rem;
  border-radius: 8px;
}

.budget-hint {
  color: var(--text-secondary);
  margin-bottom: 1rem;
  text-align: center;
  font-style: italic;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 1rem 0;
}

.checkbox-group input[type="checkbox"] {
  width: auto;
  accent-color: var(--primary-color);
}

.summary-card {
  background-color: var(--background-color);
  padding: 2rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.summary-card h3 {
  color: var(--primary-color);
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.summary-section {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.summary-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.summary-section h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
}

.summary-section p {
  color: var(--text-secondary);
  margin: 0.25rem 0;
  line-height: 1.5;
}

.details-summary {
  background-color: var(--card-bg);
  padding: 1rem;
  border-radius: 6px;
  border-left: 4px solid var(--primary-color);
}

.confirmation-checkbox {
  background-color: var(--warning-color-light, #fff3cd);
  padding: 1rem;
  border-radius: 8px;
  border-left: 4px solid var(--warning-color, #ffc107);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.btn {
  padding: 0.875rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
  font-weight: 500;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--primary-color-dark, #1d4ed8);
  transform: translateY(-1px);
}

.btn-secondary {
  background-color: var(--border-color);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background-color: var(--text-secondary);
  color: white;
}

.btn-success {
  background-color: var(--success-color);
  color: white;
}

.btn-success:hover:not(:disabled) {
  background-color: var(--success-color-dark, #047857);
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .demande-service-wizard {
    padding: 1rem;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .steps-indicator {
    flex-direction: column;
    gap: 1rem;
  }
  
  .step::after {
    display: none;
  }
  
  .wizard-form {
    padding: 1.5rem;
  }
  
  .form-navigation {
    flex-direction: column;
    gap: 1rem;
  }
  
  .btn {
    width: 100%;
    text-align: center;
  }
}
</style>
