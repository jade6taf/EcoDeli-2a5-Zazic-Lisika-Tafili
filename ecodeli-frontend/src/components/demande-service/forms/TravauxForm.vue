<script>
export default {
  name: 'TravauxForm',
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
        typeTravaux: [],
        descriptionTravaux: '',
        dateSouhaitee: '',
        typeLogement: '',
        urgence: 'standard',
        budgetEstimatif: null,
        outilsRequis: '',
        materiauxAFournir: '',
        accesBatiment: '',
        contraintesParticulieres: '',
        diagnosticRequis: false,
        devisGratuit: false,
        surfaceATravail: null,
        niveauDifficulte: '',
        delaiMaxAcceptable: ''
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
          if (!Array.isArray(this.localData.typeTravaux)) {
            this.localData.typeTravaux = []
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

      if (!this.localData.typeTravaux || this.localData.typeTravaux.length === 0) {
        errors.typeTravaux = 'Au moins un type de travaux doit être sélectionné'
        isValid = false
      }

      if (!this.localData.descriptionTravaux) {
        errors.descriptionTravaux = 'La description des travaux est obligatoire'
        isValid = false
      } else if (this.localData.descriptionTravaux.length < 10) {
        errors.descriptionTravaux = 'La description doit faire au moins 10 caractères'
        isValid = false
      } else if (this.localData.descriptionTravaux.length > 1000) {
        errors.descriptionTravaux = 'La description ne peut pas dépasser 1000 caractères'
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

      if (this.localData.budgetEstimatif && this.localData.budgetEstimatif <= 0) {
        errors.budgetEstimatif = 'Le budget doit être positif'
        isValid = false
      }

      if (this.localData.surfaceATravail && this.localData.surfaceATravail < 1) {
        errors.surfaceATravail = 'La surface doit être au moins 1m²'
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

    getTypeTravauxLabels() {
      const labels = {
        'bricolage': 'Bricolage général',
        'plomberie': 'Plomberie',
        'electricite': 'Électricité',
        'peinture': 'Peinture',
        'montage': 'Montage/assemblage',
        'renovation': 'Rénovation'
      }
      return this.localData.typeTravaux.map(type => labels[type] || type).join(', ')
    },

    getTypeLogementLabel(type) {
      const labels = {
        'appartement': 'Appartement',
        'maison': 'Maison',
        'bureau': 'Bureau/local'
      }
      return labels[type] || type
    },

    getUrgenceLabel(urgence) {
      const labels = {
        'standard': 'Standard',
        'urgent': 'Urgent'
      }
      return labels[urgence] || urgence
    }
  },

  mounted() {
    this.validateForm()
  }
}
</script>

<template>
  <div class="travaux-form">
    <div v-if="!readonly" class="professional-form">

      <!-- Champs essentiels -->
      <div class="essential-fields">
        <h3>Informations principales</h3>

        <div class="field-group">
          <div class="field-item full-width">
            <label>Type de travaux *</label>
            <div class="checkbox-grid">
              <label class="checkbox-item" :class="{ 'checked': localData.typeTravaux.includes('bricolage') }">
                <input type="checkbox" v-model="localData.typeTravaux" value="bricolage">
                <span>Bricolage général</span>
              </label>
              <label class="checkbox-item" :class="{ 'checked': localData.typeTravaux.includes('plomberie') }">
                <input type="checkbox" v-model="localData.typeTravaux" value="plomberie">
                <span>Plomberie</span>
              </label>
              <label class="checkbox-item" :class="{ 'checked': localData.typeTravaux.includes('electricite') }">
                <input type="checkbox" v-model="localData.typeTravaux" value="electricite">
                <span>Électricité</span>
              </label>
              <label class="checkbox-item" :class="{ 'checked': localData.typeTravaux.includes('peinture') }">
                <input type="checkbox" v-model="localData.typeTravaux" value="peinture">
                <span>Peinture</span>
              </label>
              <label class="checkbox-item" :class="{ 'checked': localData.typeTravaux.includes('montage') }">
                <input type="checkbox" v-model="localData.typeTravaux" value="montage">
                <span>Montage/assemblage</span>
              </label>
              <label class="checkbox-item" :class="{ 'checked': localData.typeTravaux.includes('renovation') }">
                <input type="checkbox" v-model="localData.typeTravaux" value="renovation">
                <span>Rénovation</span>
              </label>
            </div>
            <span v-if="errors.typeTravaux" class="error-message">{{ errors.typeTravaux }}</span>
          </div>

          <div class="field-item full-width">
            <label>Description des travaux *</label>
            <textarea
              v-model="localData.descriptionTravaux"
              placeholder="Décrivez précisément les travaux à réaliser (minimum 10 caractères)..."
              class="pro-textarea"
              :class="{ 'error': errors.descriptionTravaux }"
              rows="4"
              required
            ></textarea>
            <div class="char-counter">
              {{ localData.descriptionTravaux.length }}/1000 caractères
            </div>
            <span v-if="errors.descriptionTravaux" class="error-message">{{ errors.descriptionTravaux }}</span>
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
              <label>Type de logement</label>
              <select v-model="localData.typeLogement" class="pro-select">
                <option value="">Non précisé</option>
                <option value="appartement">Appartement</option>
                <option value="maison">Maison</option>
                <option value="bureau">Bureau/local</option>
              </select>
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
              <label>Niveau d'urgence</label>
              <div class="radio-group-pro">
                <label class="radio-item">
                  <input type="radio" v-model="localData.urgence" value="standard">
                  <span>Standard</span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="localData.urgence" value="urgent">
                  <span>Urgent</span>
                </label>
              </div>
            </div>
            <div class="field-item">
              <label>Budget estimatif (€)</label>
              <input
                type="number"
                v-model="localData.budgetEstimatif"
                placeholder="Ex: 200"
                class="pro-input"
                :class="{ 'error': errors.budgetEstimatif }"
                min="0"
                step="0.01"
              >
              <span v-if="errors.budgetEstimatif" class="error-message">{{ errors.budgetEstimatif }}</span>
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Surface à travailler (m²)</label>
              <input
                type="number"
                v-model="localData.surfaceATravail"
                placeholder="Ex: 15"
                class="pro-input"
                :class="{ 'error': errors.surfaceATravail }"
                min="1"
                step="0.1"
              >
              <span v-if="errors.surfaceATravail" class="error-message">{{ errors.surfaceATravail }}</span>
            </div>
            <div class="field-item">
              <label>Niveau de difficulté</label>
              <select v-model="localData.niveauDifficulte" class="pro-select">
                <option value="">Non évalué</option>
                <option value="facile">Facile</option>
                <option value="moyen">Moyen</option>
                <option value="difficile">Difficile</option>
                <option value="expert">Expert requis</option>
              </select>
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Délai max acceptable</label>
              <input
                type="text"
                v-model="localData.delaiMaxAcceptable"
                placeholder="Ex: 1 semaine, 2 jours"
                class="pro-input"
              >
            </div>
            <div class="field-item">
              <label>Options</label>
              <div class="options-group">
                <label class="option-item">
                  <input type="checkbox" v-model="localData.diagnosticRequis">
                  <span>Diagnostic requis</span>
                </label>
                <label class="option-item">
                  <input type="checkbox" v-model="localData.devisGratuit">
                  <span>Devis gratuit souhaité</span>
                </label>
              </div>
            </div>
          </div>

          <div class="field-item full-width">
            <label>Outils requis</label>
            <textarea
              v-model="localData.outilsRequis"
              placeholder="Listez les outils spécifiques nécessaires..."
              class="pro-textarea"
              rows="2"
            ></textarea>
          </div>

          <div class="field-item full-width">
            <label>Matériaux à fournir</label>
            <textarea
              v-model="localData.materiauxAFournir"
              placeholder="Précisez qui fournit quoi (client/prestataire)..."
              class="pro-textarea"
              rows="2"
            ></textarea>
          </div>

          <div class="field-item full-width">
            <label>Accès au bâtiment</label>
            <textarea
              v-model="localData.accesBatiment"
              placeholder="Code d'accès, étage, ascenseur, stationnement..."
              class="pro-textarea"
              rows="2"
            ></textarea>
          </div>

          <div class="field-item full-width">
            <label>Contraintes particulières</label>
            <textarea
              v-model="localData.contraintesParticulieres"
              placeholder="Horaires, bruit, présence d'animaux, allergies..."
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
        <strong>Type(s) de travaux:</strong> {{ getTypeTravauxLabels() }}
      </div>
      <div class="summary-item">
        <strong>Description:</strong> {{ localData.descriptionTravaux }}
      </div>
      <div class="summary-item">
        <strong>Date souhaitée:</strong> {{ formatDate(localData.dateSouhaitee) }}
      </div>
      <div v-if="localData.typeLogement" class="summary-item">
        <strong>Type de logement:</strong> {{ getTypeLogementLabel(localData.typeLogement) }}
      </div>
      <div class="summary-item">
        <strong>Urgence:</strong> {{ getUrgenceLabel(localData.urgence) }}
      </div>
      <div v-if="localData.budgetEstimatif" class="summary-item">
        <strong>Budget estimatif:</strong> {{ localData.budgetEstimatif }}€
      </div>
      <div v-if="localData.surfaceATravail" class="summary-item">
        <strong>Surface:</strong> {{ localData.surfaceATravail }} m²
      </div>
      <div v-if="localData.niveauDifficulte" class="summary-item">
        <strong>Difficulté:</strong> {{ localData.niveauDifficulte }}
      </div>
      <div v-if="localData.delaiMaxAcceptable" class="summary-item">
        <strong>Délai max:</strong> {{ localData.delaiMaxAcceptable }}
      </div>
      <div v-if="localData.diagnosticRequis || localData.devisGratuit" class="summary-item">
        <strong>Options:</strong>
        <span v-if="localData.diagnosticRequis">Diagnostic requis</span>
        <span v-if="localData.diagnosticRequis && localData.devisGratuit">, </span>
        <span v-if="localData.devisGratuit">Devis gratuit</span>
      </div>
      <div v-if="localData.outilsRequis" class="summary-item">
        <strong>Outils requis:</strong> {{ localData.outilsRequis }}
      </div>
      <div v-if="localData.materiauxAFournir" class="summary-item">
        <strong>Matériaux:</strong> {{ localData.materiauxAFournir }}
      </div>
      <div v-if="localData.accesBatiment" class="summary-item">
        <strong>Accès:</strong> {{ localData.accesBatiment }}
      </div>
      <div v-if="localData.contraintesParticulieres" class="summary-item">
        <strong>Contraintes:</strong> {{ localData.contraintesParticulieres }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.travaux-form {
  width: 100%;
}

/* Styles professionnels pour les formulaires */
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

.char-counter {
  font-size: 0.8rem;
  color: var(--text-secondary);
  text-align: right;
  margin-top: 0.25rem;
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

.options-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.option-item input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: var(--primary-color);
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
