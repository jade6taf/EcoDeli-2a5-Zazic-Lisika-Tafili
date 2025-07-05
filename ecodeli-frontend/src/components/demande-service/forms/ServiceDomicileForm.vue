<script>
export default {
  name: 'ServiceDomicileForm',
  props: {
    modelValue: {
      type: Object,
      default: () => ({})
    },
    errors: {
      type: Object,
      default: () => ({})
    },
    readonly: {
      type: Boolean,
      default: false
    },
    summary: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue', 'validate'],
  data() {
    return {
      localData: {
        typeService: [],
        dateSouhaitee: '',
        frequence: '',
        dureeEstimee: '',
        adresseService: '',
        informationsParticulieres: '',
        materielFourni: null,
        contraintesAcces: '',
        preferencesHoraires: '',
        nombreEnfants: null,
        nombreAnimaux: null,
        typeAnimaux: '',
        surfaceJardin: null
      }
    }
  },
  watch: {
    modelValue: {
      immediate: true,
      deep: true,
      handler(newValue) {
        if (newValue) {
          this.localData = { ...this.localData, ...newValue }
          if (!Array.isArray(this.localData.typeService)) {
            this.localData.typeService = []
          }
        }
      }
    },
    localData: {
      deep: true,
      handler(newValue) {
        this.$emit('update:modelValue', newValue)
        this.validateForm()
      }
    }
  },
  methods: {
    validateForm() {
      const errors = {}
      let isValid = true

      if (!this.localData.typeService || this.localData.typeService.length === 0) {
        errors.typeService = 'Au moins un type de service doit être sélectionné'
        isValid = false
      }

      if (!this.localData.dateSouhaitee) {
        errors.dateSouhaitee = 'La date souhaitée est obligatoire'
        isValid = false
      } else {
        const selectedDate = new Date(this.localData.dateSouhaitee)
        const today = new Date()
        today.setHours(0, 0, 0, 0)
        if (selectedDate < today) {
          errors.dateSouhaitee = 'La date ne peut pas être dans le passé'
          isValid = false
        }
      }

      if (!this.localData.frequence) {
        errors.frequence = 'La fréquence est obligatoire'
        isValid = false
      }
      if (this.localData.nombreEnfants && (this.localData.nombreEnfants < 1 || this.localData.nombreEnfants > 10)) {
        errors.nombreEnfants = 'Le nombre d\'enfants doit être entre 1 et 10'
        isValid = false
      }

      if (this.localData.nombreAnimaux && (this.localData.nombreAnimaux < 1 || this.localData.nombreAnimaux > 10)) {
        errors.nombreAnimaux = 'Le nombre d\'animaux doit être entre 1 et 10'
        isValid = false
      }

      if (this.localData.surfaceJardin && this.localData.surfaceJardin <= 0) {
        errors.surfaceJardin = 'La surface doit être positive'
        isValid = false
      }

      this.$emit('validate', isValid, errors)
    },

    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },

    getTypeServiceLabels() {
      const labels = {
        'menage': 'Ménage & entretien',
        'garde_enfants': 'Garde d\'enfants',
        'garde_animaux': 'Garde d\'animaux',
        'jardinage': 'Jardinage',
        'assistance_pa': 'Assistance seniors'
      }
      return this.localData.typeService.map(type => labels[type] || type).join(', ')
    },

    getFrequenceLabel(frequence) {
      const labels = {
        'ponctuel': 'Ponctuel',
        'hebdomadaire': 'Hebdomadaire',
        'mensuel': 'Mensuel'
      }
      return labels[frequence] || frequence
    }
  },

  mounted() {
    this.validateForm()
  }
}
</script>

<template>
  <div class="service-domicile-form">
    <div v-if="!readonly" class="professional-form">

      <!-- Champs essentiels -->
      <div class="essential-fields">
        <h3>Informations principales</h3>

        <div class="field-group">
          <div class="field-item full-width">
            <label>Type de service *</label>
            <div class="checkbox-grid">
              <label class="checkbox-item" :class="{ 'checked': localData.typeService.includes('menage') }">
                <input type="checkbox" v-model="localData.typeService" value="menage">
                <span>Ménage & entretien</span>
              </label>
              <label class="checkbox-item" :class="{ 'checked': localData.typeService.includes('garde_enfants') }">
                <input type="checkbox" v-model="localData.typeService" value="garde_enfants">
                <span>Garde d'enfants</span>
              </label>
              <label class="checkbox-item" :class="{ 'checked': localData.typeService.includes('garde_animaux') }">
                <input type="checkbox" v-model="localData.typeService" value="garde_animaux">
                <span>Garde d'animaux</span>
              </label>
              <label class="checkbox-item" :class="{ 'checked': localData.typeService.includes('jardinage') }">
                <input type="checkbox" v-model="localData.typeService" value="jardinage">
                <span>Jardinage</span>
              </label>
              <label class="checkbox-item" :class="{ 'checked': localData.typeService.includes('assistance_pa') }">
                <input type="checkbox" v-model="localData.typeService" value="assistance_pa">
                <span>Assistance seniors</span>
              </label>
            </div>
            <span v-if="errors.typeService" class="error-message">{{ errors.typeService }}</span>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Date souhaitée *</label>
              <input
                type="date"
                v-model="localData.dateSouhaitee"
                class="pro-input"
                :class="{ 'error': errors.dateSouhaitee }"
                required
              >
              <span v-if="errors.dateSouhaitee" class="error-message">{{ errors.dateSouhaitee }}</span>
            </div>
            <div class="field-item">
              <label>Fréquence *</label>
              <select
                v-model="localData.frequence"
                class="pro-select"
                :class="{ 'error': errors.frequence }"
                required
              >
                <option value="">Sélectionnez</option>
                <option value="ponctuel">Ponctuel</option>
                <option value="hebdomadaire">Hebdomadaire</option>
                <option value="mensuel">Mensuel</option>
              </select>
              <span v-if="errors.frequence" class="error-message">{{ errors.frequence }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Champs complémentaires -->
      <div class="additional-fields">
        <h3>Détails complémentaires</h3>

        <div class="field-group">
          <div class="field-row">
            <div class="field-item">
              <label>Durée estimée</label>
              <select v-model="localData.dureeEstimee" class="pro-select">
                <option value="">Non défini</option>
                <option value="1h">1 heure</option>
                <option value="2h">2 heures</option>
                <option value="3h">3 heures</option>
                <option value="4h+">4 heures ou plus</option>
              </select>
            </div>
            <div class="field-item">
              <label>Adresse du service</label>
              <input
                type="text"
                v-model="localData.adresseService"
                placeholder="Ville ou code postal"
                class="pro-input"
              >
            </div>
          </div>

          <!-- Champs spécifiques selon le type de service -->
          <div v-if="localData.typeService.includes('garde_enfants')" class="field-row">
            <div class="field-item">
              <label>Nombre d'enfants</label>
              <input
                type="number"
                v-model="localData.nombreEnfants"
                placeholder="Ex: 2"
                class="pro-input"
                min="1"
                max="10"
              >
            </div>
            <div class="field-item">
              <label>Préférences horaires</label>
              <input
                type="text"
                v-model="localData.preferencesHoraires"
                placeholder="Ex: 8h-17h, weekend"
                class="pro-input"
              >
            </div>
          </div>

          <div v-if="localData.typeService.includes('garde_animaux')" class="field-row">
            <div class="field-item">
              <label>Nombre d'animaux</label>
              <input
                type="number"
                v-model="localData.nombreAnimaux"
                placeholder="Ex: 1"
                class="pro-input"
                min="1"
                max="10"
              >
            </div>
            <div class="field-item">
              <label>Type d'animaux</label>
              <input
                type="text"
                v-model="localData.typeAnimaux"
                placeholder="Ex: Chien, Chat, Lapin"
                class="pro-input"
              >
            </div>
          </div>

          <div v-if="localData.typeService.includes('jardinage')" class="field-row">
            <div class="field-item">
              <label>Surface du jardin (m²)</label>
              <input
                type="number"
                v-model="localData.surfaceJardin"
                placeholder="Ex: 50"
                class="pro-input"
                min="0"
                step="0.1"
              >
            </div>
            <div class="field-item">
              <label>Matériel fourni</label>
              <div class="radio-group-pro">
                <label class="radio-item">
                  <input type="radio" v-model="localData.materielFourni" :value="true">
                  <span>Oui</span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="localData.materielFourni" :value="false">
                  <span>Non</span>
                </label>
              </div>
            </div>
          </div>

          <div class="field-item full-width">
            <label>Contraintes d'accès</label>
            <textarea
              v-model="localData.contraintesAcces"
              placeholder="Code d'accès, étage, interphone, stationnement..."
              class="pro-textarea"
              rows="2"
            ></textarea>
          </div>

          <div class="field-item full-width">
            <label>Informations particulières</label>
            <textarea
              v-model="localData.informationsParticulieres"
              placeholder="Précisions sur le service, allergies, consignes spéciales..."
              class="pro-textarea"
              rows="3"
            ></textarea>
          </div>
        </div>
      </div>
    </div>

    <!-- Mode résumé pour l'étape 5 -->
    <div v-else-if="summary" class="summary-mode">
      <div class="summary-item">
        <strong>Type(s) de service:</strong> {{ getTypeServiceLabels() }}
      </div>
      <div class="summary-item">
        <strong>Date souhaitée:</strong> {{ formatDate(localData.dateSouhaitee) }}
      </div>
      <div class="summary-item">
        <strong>Fréquence:</strong> {{ getFrequenceLabel(localData.frequence) }}
      </div>
      <div v-if="localData.dureeEstimee" class="summary-item">
        <strong>Durée estimée:</strong> {{ localData.dureeEstimee }}
      </div>
      <div v-if="localData.adresseService" class="summary-item">
        <strong>Adresse:</strong> {{ localData.adresseService }}
      </div>
      <div v-if="localData.nombreEnfants" class="summary-item">
        <strong>Nombre d'enfants:</strong> {{ localData.nombreEnfants }}
      </div>
      <div v-if="localData.nombreAnimaux" class="summary-item">
        <strong>Nombre d'animaux:</strong> {{ localData.nombreAnimaux }} ({{ localData.typeAnimaux }})
      </div>
      <div v-if="localData.surfaceJardin" class="summary-item">
        <strong>Surface jardin:</strong> {{ localData.surfaceJardin }} m²
      </div>
      <div v-if="localData.materielFourni !== null" class="summary-item">
        <strong>Matériel fourni:</strong> {{ localData.materielFourni ? 'Oui' : 'Non' }}
      </div>
      <div v-if="localData.contraintesAcces" class="summary-item">
        <strong>Contraintes d'accès:</strong> {{ localData.contraintesAcces }}
      </div>
      <div v-if="localData.informationsParticulieres" class="summary-item">
        <strong>Informations:</strong> {{ localData.informationsParticulieres }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.service-domicile-form {
  width: 100%;
}

.professional-form {
  background-color: var(--card-bg);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.essential-fields {
  background: linear-gradient(135deg, var(--primary-color-light, #f8f9ff) 0%, var(--card-bg) 100%);
  padding: 2rem;
  border-bottom: 1px solid var(--border-color);
}

.essential-fields h3 {
  color: var(--primary-color);
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.essential-fields h3::before {
  content: "✓";
  background-color: var(--primary-color);
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: bold;
}

.additional-fields {
  background-color: var(--background-color);
  padding: 2rem;
}

.additional-fields h3 {
  color: var(--text-secondary);
  font-size: 1.1rem;
  font-weight: 500;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.additional-fields h3::before {
  content: "+";
  background-color: var(--text-secondary);
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  font-weight: bold;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.field-item {
  display: flex;
  flex-direction: column;
}

.field-item.full-width {
  grid-column: 1 / -1;
}

.field-item label {
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  font-size: 0.95rem;
}

.pro-input, .pro-select, .pro-textarea {
  width: 100%;
  padding: 0.875rem;
  border: 2px solid var(--border-color);
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  transition: all 0.3s ease;
  background-color: white;
}

.pro-input:focus, .pro-select:focus, .pro-textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  transform: translateY(-1px);
}

.pro-input:hover, .pro-select:hover, .pro-textarea:hover {
  border-color: var(--primary-color-light, #93c5fd);
}

.pro-input.error, .pro-select.error, .pro-textarea.error {
  border-color: var(--error-color, #dc3545);
}

.pro-textarea {
  resize: vertical;
  min-height: 100px;
  line-height: 1.5;
}

.error-message {
  color: var(--error-color, #dc3545);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border: 2px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: white;
}

.checkbox-item:hover {
  border-color: var(--primary-color-light, #93c5fd);
  background-color: var(--primary-color-light, #f8f9ff);
}

.checkbox-item input[type="checkbox"] {
  width: 18px;
  height: 18px;
  accent-color: var(--primary-color);
  cursor: pointer;
}

.checkbox-item.checked {
  border-color: var(--primary-color);
  background-color: var(--primary-color-light, #f8f9ff);
}

.checkbox-item.checked span {
  color: var(--primary-color);
  font-weight: 500;
}

.checkbox-item span {
  font-size: 0.95rem;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.radio-group-pro {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: 2px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: white;
  flex: 1;
  justify-content: center;
}

.radio-item:hover {
  border-color: var(--primary-color-light, #93c5fd);
  background-color: var(--primary-color-light, #f8f9ff);
}

.radio-item input[type="radio"] {
  width: 16px;
  height: 16px;
  accent-color: var(--primary-color);
  cursor: pointer;
}

.radio-item:has(input[type="radio"]:checked) {
  border-color: var(--primary-color);
  background-color: var(--primary-color);
  color: white;
}

.radio-item:has(input[type="radio"]:checked) span {
  color: white;
  font-weight: 500;
}

.radio-item span {
  font-size: 0.95rem;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

/* Mode résumé */
.summary-mode {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.summary-item {
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--border-color);
  font-size: 0.95rem;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-item strong {
  color: var(--primary-color);
  margin-right: 0.5rem;
}

@media (max-width: 768px) {
  .field-row {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .checkbox-grid {
    grid-template-columns: 1fr;
  }
  
  .radio-group-pro {
    flex-direction: column;
  }
  
  .essential-fields, .additional-fields {
    padding: 1.5rem;
  }
}
</style>
