<script>
export default {
  name: 'ServicesPersonnelsForm',
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
        typeService: '',
        descriptionBesoin: '',
        dateSouhaitee: '',
        dureeEstimee: '',
        lieuService: '',
        livrables: '',
        delaiReduction: '',
        urgence: false,
        confidentialite: false,
        competencesSpecifiques: false,
        logicielsRequis: '',
        languesRequises: '',
        niveauExpertise: '',
        modalitesContact: '',
        frequenceSupports: '',
        formatLivrable: '',
        nombreInvites: null,
        typeEvenement: '',
        budget: '',
        typeDocuments: '',
        attestationRequise: null
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

      if (!this.localData.typeService) {
        errors.typeService = 'Le type de service est obligatoire'
        isValid = false
      }

      if (!this.localData.descriptionBesoin) {
        errors.descriptionBesoin = 'La description de la mission est obligatoire'
        isValid = false
      } else if (this.localData.descriptionBesoin.length < 10) {
        errors.descriptionBesoin = 'La description doit faire au moins 10 caractères'
        isValid = false
      } else if (this.localData.descriptionBesoin.length > 2000) {
        errors.descriptionBesoin = 'La description ne peut pas dépasser 2000 caractères'
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

      if (this.localData.nombreInvites && this.localData.nombreInvites < 1) {
        errors.nombreInvites = 'Le nombre d\'invités doit être au moins 1'
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

    getTypeServiceLabel(type) {
      const labels = {
        'administratif': 'Assistance administrative',
        'conciergerie': 'Conciergerie',
        'evenementiel': 'Organisation d\'événements',
        'secretariat': 'Secrétariat',
        'redaction': 'Rédaction'
      }
      return labels[type] || type
    },

    getDureeEstimeeLabel(duree) {
      const labels = {
        '1-2h': '1-2 heures',
        'demi-journee': 'Demi-journée',
        'journee': 'Journée complète',
        'plusieurs-jours': 'Plusieurs jours'
      }
      return labels[duree] || duree
    },

    hasOptions() {
      return this.localData.urgence ||
             this.localData.confidentialite ||
             this.localData.competencesSpecifiques
    },

    getOptionsText() {
      const options = []
      if (this.localData.urgence) options.push('Urgent')
      if (this.localData.confidentialite) options.push('Confidentiel')
      if (this.localData.competencesSpecifiques) options.push('Compétences spécifiques')
      return options.join(', ')
    }
  },

  mounted() {
    this.validateForm()
  }
}
</script>

<template>
  <div class="services-personnels-form">
    <div v-if="!readonly" class="professional-form">

      <!-- Champs essentiels -->
      <div class="essential-fields">
        <h3>Informations principales</h3>

        <div class="field-group">
          <div class="field-row">
            <div class="field-item">
              <label>Type de service *</label>
              <select
                v-model="localData.typeService"
                class="pro-select"
                :class="{ 'error': errors.typeService }"
                required
              >
                <option value="">Sélectionnez</option>
                <option value="administratif">Assistance administrative</option>
                <option value="conciergerie">Conciergerie</option>
                <option value="evenementiel">Organisation d'événements</option>
                <option value="secretariat">Secrétariat</option>
                <option value="redaction">Rédaction</option>
              </select>
              <span v-if="errors.typeService" class="error-message">{{ errors.typeService }}</span>
            </div>
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
          </div>

          <div class="field-item full-width">
            <label>Description de la mission *</label>
            <textarea
              v-model="localData.descriptionBesoin"
              placeholder="Décrivez précisément la tâche à accomplir, les livrables attendus (minimum 10 caractères)..."
              class="pro-textarea"
              :class="{ 'error': errors.descriptionBesoin }"
              rows="4"
              required
            ></textarea>
            <div class="char-counter">
              {{ localData.descriptionBesoin.length }}/2000 caractères
            </div>
            <span v-if="errors.descriptionBesoin" class="error-message">{{ errors.descriptionBesoin }}</span>
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
                <option value="1-2h">1-2 heures</option>
                <option value="demi-journee">Demi-journée</option>
                <option value="journee">Journée complète</option>
                <option value="plusieurs-jours">Plusieurs jours</option>
              </select>
            </div>
            <div class="field-item">
              <label>Lieu de service</label>
              <input
                type="text"
                v-model="localData.lieuService"
                placeholder="À domicile, en ligne, bureau..."
                class="pro-input"
              >
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Niveau d'expertise requis</label>
              <select v-model="localData.niveauExpertise" class="pro-select">
                <option value="">Non précisé</option>
                <option value="debutant">Débutant accepté</option>
                <option value="intermediaire">Intermédiaire</option>
                <option value="avance">Avancé</option>
                <option value="expert">Expert requis</option>
              </select>
            </div>
            <div class="field-item">
              <label>Options</label>
              <div class="options-group">
                <label class="option-item">
                  <input type="checkbox" v-model="localData.urgence">
                  <span>Mission urgente</span>
                </label>
                <label class="option-item">
                  <input type="checkbox" v-model="localData.confidentialite">
                  <span>Confidentialité requise</span>
                </label>
                <label class="option-item">
                  <input type="checkbox" v-model="localData.competencesSpecifiques">
                  <span>Compétences spécifiques requises</span>
                </label>
              </div>
            </div>
          </div>

          <!-- Champs spécifiques selon le type de service -->
          <div v-if="localData.typeService === 'evenementiel'" class="specific-fields">
            <h4>Détails événementiel</h4>
            <div class="field-row">
              <div class="field-item">
                <label>Type d'événement</label>
                <input
                  type="text"
                  v-model="localData.typeEvenement"
                  placeholder="Ex: Anniversaire, Mariage, Séminaire"
                  class="pro-input"
                >
              </div>
              <div class="field-item">
                <label>Nombre d'invités</label>
                <input
                  type="number"
                  v-model="localData.nombreInvites"
                  placeholder="Ex: 50"
                  class="pro-input"
                  min="1"
                >
              </div>
            </div>
            <div class="field-item full-width">
              <label>Budget événement</label>
              <input
                type="text"
                v-model="localData.budget"
                placeholder="Ex: 1000-2000€"
                class="pro-input"
              >
            </div>
          </div>

          <div v-if="localData.typeService === 'administratif'" class="specific-fields">
            <h4>Détails administratif</h4>
            <div class="field-row">
              <div class="field-item">
                <label>Type de documents</label>
                <input
                  type="text"
                  v-model="localData.typeDocuments"
                  placeholder="Ex: Contrats, Factures, Déclarations"
                  class="pro-input"
                >
              </div>
              <div class="field-item">
                <label>Attestation</label>
                <div class="radio-group-pro">
                  <label class="radio-item">
                    <input type="radio" v-model="localData.attestationRequise" :value="true">
                    <span>Requise</span>
                  </label>
                  <label class="radio-item">
                    <input type="radio" v-model="localData.attestationRequise" :value="false">
                    <span>Non requise</span>
                  </label>
                </div>
              </div>
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Logiciels requis</label>
              <input
                type="text"
                v-model="localData.logicielsRequis"
                placeholder="Ex: Word, Excel, Photoshop"
                class="pro-input"
              >
            </div>
            <div class="field-item">
              <label>Langues requises</label>
              <input
                type="text"
                v-model="localData.languesRequises"
                placeholder="Ex: Français, Anglais"
                class="pro-input"
              >
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Format des livrables</label>
              <input
                type="text"
                v-model="localData.formatLivrable"
                placeholder="Ex: PDF, Word, Présentation"
                class="pro-input"
              >
            </div>
            <div class="field-item">
              <label>Délai de réduction</label>
              <input
                type="text"
                v-model="localData.delaiReduction"
                placeholder="Ex: 2 jours, 1 semaine"
                class="pro-input"
              >
            </div>
          </div>

          <div class="field-item full-width">
            <label>Livrables attendus</label>
            <textarea
              v-model="localData.livrables"
              placeholder="Décrivez précisément ce que vous attendez en retour..."
              class="pro-textarea"
              rows="3"
            ></textarea>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Modalités de contact</label>
              <input
                type="text"
                v-model="localData.modalitesContact"
                placeholder="Email, téléphone, visio..."
                class="pro-input"
              >
            </div>
            <div class="field-item">
              <label>Fréquence des points</label>
              <input
                type="text"
                v-model="localData.frequenceSupports"
                placeholder="Quotidien, hebdomadaire..."
                class="pro-input"
              >
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Mode résumé pour l'étape 5 -->
    <div v-else-if="summary" class="summary-mode">
      <div class="summary-item">
        <strong>Type de service:</strong> {{ getTypeServiceLabel(localData.typeService) }}
      </div>
      <div class="summary-item">
        <strong>Date souhaitée:</strong> {{ formatDate(localData.dateSouhaitee) }}
      </div>
      <div class="summary-item">
        <strong>Description:</strong> {{ localData.descriptionBesoin }}
      </div>
      <div v-if="localData.dureeEstimee" class="summary-item">
        <strong>Durée estimée:</strong> {{ getDureeEstimeeLabel(localData.dureeEstimee) }}
      </div>
      <div v-if="localData.lieuService" class="summary-item">
        <strong>Lieu:</strong> {{ localData.lieuService }}
      </div>
      <div v-if="localData.niveauExpertise" class="summary-item">
        <strong>Niveau requis:</strong> {{ localData.niveauExpertise }}
      </div>
      <div v-if="hasOptions()" class="summary-item">
        <strong>Options:</strong> {{ getOptionsText() }}
      </div>
      <div v-if="localData.typeEvenement" class="summary-item">
        <strong>Type d'événement:</strong> {{ localData.typeEvenement }}
      </div>
      <div v-if="localData.nombreInvites" class="summary-item">
        <strong>Nombre d'invités:</strong> {{ localData.nombreInvites }}
      </div>
      <div v-if="localData.budget" class="summary-item">
        <strong>Budget:</strong> {{ localData.budget }}
      </div>
      <div v-if="localData.typeDocuments" class="summary-item">
        <strong>Type de documents:</strong> {{ localData.typeDocuments }}
      </div>
      <div v-if="localData.attestationRequise !== null" class="summary-item">
        <strong>Attestation:</strong> {{ localData.attestationRequise ? 'Requise' : 'Non requise' }}
      </div>
      <div v-if="localData.logicielsRequis" class="summary-item">
        <strong>Logiciels:</strong> {{ localData.logicielsRequis }}
      </div>
      <div v-if="localData.languesRequises" class="summary-item">
        <strong>Langues:</strong> {{ localData.languesRequises }}
      </div>
      <div v-if="localData.formatLivrable" class="summary-item">
        <strong>Format:</strong> {{ localData.formatLivrable }}
      </div>
      <div v-if="localData.delaiReduction" class="summary-item">
        <strong>Délai:</strong> {{ localData.delaiReduction }}
      </div>
      <div v-if="localData.livrables" class="summary-item">
        <strong>Livrables:</strong> {{ localData.livrables }}
      </div>
      <div v-if="localData.modalitesContact" class="summary-item">
        <strong>Contact:</strong> {{ localData.modalitesContact }}
      </div>
      <div v-if="localData.frequenceSupports" class="summary-item">
        <strong>Fréquence points:</strong> {{ localData.frequenceSupports }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.services-personnels-form {
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

.specific-fields {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  margin: 1.5rem 0;
  border-left: 4px solid var(--primary-color);
}

.specific-fields h4 {
  color: var(--primary-color);
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1rem;
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
  
  .radio-group-pro {
    flex-direction: column;
  }
  
  .essential-fields, .additional-fields, .specific-fields {
    padding: 1.5rem;
  }
}
</style>
