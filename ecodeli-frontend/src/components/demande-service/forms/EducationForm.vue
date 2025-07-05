<script>
export default {
  name: 'EducationForm',
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
        typeFormation: '',
        matiereDomaine: '',
        niveau: '',
        modalite: '',
        objectifs: '',
        frequenceSouhaitee: '',
        lieuFormation: '',
        niveauActuel: '',
        preparationExamen: null,
        nomExamen: '',
        dateExamen: '',
        methodePedagogique: '',
        supportsCours: '',
        devoirs: false,
        dureeParSession: null,
        nombreSessions: null,
        ageApprenant: '',
        handicapAdaptation: '',
        materielRequis: '',
        disponibilites: ''
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
    },
    'localData.modalite'(newValue) {
      if (newValue === 'en_ligne') {
        this.localData.lieuFormation = ''
      }
    }
  },
  methods: {
    validateForm() {
      const errors = {}
      let isValid = true

      if (!this.localData.typeFormation) {
        errors.typeFormation = 'Le type de formation est obligatoire'
        isValid = false
      }

      if (!this.localData.matiereDomaine) {
        errors.matiereDomaine = 'La matière/domaine est obligatoire'
        isValid = false
      } else if (this.localData.matiereDomaine.length < 2) {
        errors.matiereDomaine = 'La matière doit faire au moins 2 caractères'
        isValid = false
      } else if (this.localData.matiereDomaine.length > 100) {
        errors.matiereDomaine = 'La matière ne peut pas dépasser 100 caractères'
        isValid = false
      }

      if (!this.localData.niveau) {
        errors.niveau = 'Le niveau est obligatoire'
        isValid = false
      }

      if (!this.localData.modalite) {
        errors.modalite = 'La modalité est obligatoire'
        isValid = false
      }

      if (this.localData.dureeParSession && (this.localData.dureeParSession < 1 || this.localData.dureeParSession > 8)) {
        errors.dureeParSession = 'La durée par session doit être entre 1 et 8 heures'
        isValid = false
      }

      if (this.localData.nombreSessions && (this.localData.nombreSessions < 1 || this.localData.nombreSessions > 50)) {
        errors.nombreSessions = 'Le nombre de sessions doit être entre 1 et 50'
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

    getTypeFormationLabel(type) {
      const labels = {
        'cours_particuliers': 'Cours particuliers',
        'soutien_scolaire': 'Soutien scolaire',
        'formation_pro': 'Formation professionnelle',
        'coaching': 'Coaching personnel',
        'langue': 'Apprentissage de langues'
      }
      return labels[type] || type
    },

    getNiveauLabel(niveau) {
      const labels = {
        'debutant': 'Débutant',
        'intermediaire': 'Intermédiaire',
        'avance': 'Avancé',
        'expert': 'Expert'
      }
      return labels[niveau] || niveau
    },

    getModaliteLabel(modalite) {
      const labels = {
        'presentiel': 'Présentiel',
        'en_ligne': 'En ligne'
      }
      return labels[modalite] || modalite
    }
  },

  mounted() {
    this.validateForm()
  }
}
</script>

<template>
  <div class="education-form">
    <div v-if="!readonly" class="professional-form">

      <!-- Champs essentiels -->
      <div class="essential-fields">
        <h3>Informations principales</h3>

        <div class="field-group">
          <div class="field-row">
            <div class="field-item">
              <label>Type de formation *</label>
              <select
                v-model="localData.typeFormation"
                class="pro-select"
                :class="{ 'error': errors.typeFormation }"
                required
              >
                <option value="">Sélectionnez</option>
                <option value="cours_particuliers">Cours particuliers</option>
                <option value="soutien_scolaire">Soutien scolaire</option>
                <option value="formation_pro">Formation professionnelle</option>
                <option value="coaching">Coaching personnel</option>
                <option value="langue">Apprentissage de langues</option>
              </select>
              <span v-if="errors.typeFormation" class="error-message">{{ errors.typeFormation }}</span>
            </div>
            <div class="field-item">
              <label>Modalité *</label>
              <div class="radio-group-pro">
                <label class="radio-item">
                  <input type="radio" v-model="localData.modalite" value="presentiel" required>
                  <span>Présentiel</span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="localData.modalite" value="en_ligne" required>
                  <span>En ligne</span>
                </label>
              </div>
              <span v-if="errors.modalite" class="error-message">{{ errors.modalite }}</span>
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Matière/Domaine *</label>
              <input
                type="text"
                v-model="localData.matiereDomaine"
                placeholder="Ex: Mathématiques, Anglais, Informatique"
                class="pro-input"
                :class="{ 'error': errors.matiereDomaine }"
                required
              >
              <span v-if="errors.matiereDomaine" class="error-message">{{ errors.matiereDomaine }}</span>
            </div>
            <div class="field-item">
              <label>Niveau souhaité *</label>
              <select
                v-model="localData.niveau"
                class="pro-select"
                :class="{ 'error': errors.niveau }"
                required
              >
                <option value="">Sélectionnez</option>
                <option value="debutant">Débutant</option>
                <option value="intermediaire">Intermédiaire</option>
                <option value="avance">Avancé</option>
                <option value="expert">Expert</option>
              </select>
              <span v-if="errors.niveau" class="error-message">{{ errors.niveau }}</span>
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
              <label>Niveau actuel</label>
              <input
                type="text"
                v-model="localData.niveauActuel"
                placeholder="Ex: Lycée, Bac+3, Débutant complet"
                class="pro-input"
              >
            </div>
            <div class="field-item">
              <label>Âge de l'apprenant</label>
              <input
                type="text"
                v-model="localData.ageApprenant"
                placeholder="Ex: 16 ans, Adulte, 6-10 ans"
                class="pro-input"
              >
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Durée par session (heures)</label>
              <input
                type="number"
                v-model="localData.dureeParSession"
                placeholder="Ex: 2"
                class="pro-input"
                :class="{ 'error': errors.dureeParSession }"
                min="1"
                max="8"
              >
              <span v-if="errors.dureeParSession" class="error-message">{{ errors.dureeParSession }}</span>
            </div>
            <div class="field-item">
              <label>Nombre de sessions</label>
              <input
                type="number"
                v-model="localData.nombreSessions"
                placeholder="Ex: 10"
                class="pro-input"
                :class="{ 'error': errors.nombreSessions }"
                min="1"
                max="50"
              >
              <span v-if="errors.nombreSessions" class="error-message">{{ errors.nombreSessions }}</span>
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Fréquence souhaitée</label>
              <input
                type="text"
                v-model="localData.frequenceSouhaitee"
                placeholder="Ex: 2x/semaine, 1x/mois"
                class="pro-input"
              >
            </div>
            <div class="field-item">
              <label>Lieu de formation</label>
              <input
                type="text"
                v-model="localData.lieuFormation"
                placeholder="Ex: À domicile, Centre ville"
                class="pro-input"
                :disabled="localData.modalite === 'en_ligne'"
              >
              <small v-if="localData.modalite === 'en_ligne'" class="text-muted">
                Non applicable pour les cours en ligne
              </small>
            </div>
          </div>

          <!-- Section préparation d'examen -->
          <div class="specific-fields">
            <h4>Préparation d'examen</h4>
            <div class="field-row">
              <div class="field-item">
                <label>Préparation examen</label>
                <div class="radio-group-pro">
                  <label class="radio-item">
                    <input type="radio" v-model="localData.preparationExamen" :value="true">
                    <span>Oui</span>
                  </label>
                  <label class="radio-item">
                    <input type="radio" v-model="localData.preparationExamen" :value="false">
                    <span>Non</span>
                  </label>
                </div>
              </div>
              <div class="field-item" v-if="localData.preparationExamen">
                <label>Nom de l'examen</label>
                <input
                  type="text"
                  v-model="localData.nomExamen"
                  placeholder="Ex: Bac S, TOEIC, Permis"
                  class="pro-input"
                >
              </div>
            </div>
            <div v-if="localData.preparationExamen" class="field-item full-width">
              <label>Date de l'examen</label>
              <input
                type="date"
                v-model="localData.dateExamen"
                class="pro-input"
              >
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Méthode pédagogique</label>
              <input
                type="text"
                v-model="localData.methodePedagogique"
                placeholder="Ex: Ludique, Intensive, Théorie+pratique"
                class="pro-input"
              >
            </div>
            <div class="field-item">
              <label>Supports de cours</label>
              <input
                type="text"
                v-model="localData.supportsCours"
                placeholder="Ex: Manuel, Vidéos, Exercices"
                class="pro-input"
              >
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Matériel requis</label>
              <input
                type="text"
                v-model="localData.materielRequis"
                placeholder="Ex: Ordinateur, Calculatrice, Webcam"
                class="pro-input"
              >
            </div>
            <div class="field-item">
              <label>Options</label>
              <div class="options-group">
                <label class="option-item">
                  <input type="checkbox" v-model="localData.devoirs">
                  <span>Devoirs entre les sessions</span>
                </label>
              </div>
            </div>
          </div>

          <div class="field-item full-width">
            <label>Objectifs</label>
            <textarea
              v-model="localData.objectifs"
              placeholder="Décrivez vos objectifs d'apprentissage, ce que vous souhaitez accomplir..."
              class="pro-textarea"
              rows="3"
            ></textarea>
          </div>

          <div class="field-item full-width">
            <label>Disponibilités</label>
            <textarea
              v-model="localData.disponibilites"
              placeholder="Précisez vos créneaux disponibles (jours, heures)..."
              class="pro-textarea"
              rows="2"
            ></textarea>
          </div>

          <div class="field-item full-width">
            <label>Handicap/Adaptation</label>
            <textarea
              v-model="localData.handicapAdaptation"
              placeholder="Précisez si des adaptations spécifiques sont nécessaires..."
              class="pro-textarea"
              rows="2"
            ></textarea>
          </div>
        </div>
      </div>
    </div>

    <!-- Mode résumé pour l'étape 5 -->
    <div v-else-if="summary" class="summary-mode">
      <div class="summary-item">
        <strong>Type de formation:</strong> {{ getTypeFormationLabel(localData.typeFormation) }}
      </div>
      <div class="summary-item">
        <strong>Matière/Domaine:</strong> {{ localData.matiereDomaine }}
      </div>
      <div class="summary-item">
        <strong>Niveau:</strong> {{ getNiveauLabel(localData.niveau) }}
      </div>
      <div class="summary-item">
        <strong>Modalité:</strong> {{ getModaliteLabel(localData.modalite) }}
      </div>
      <div v-if="localData.niveauActuel" class="summary-item">
        <strong>Niveau actuel:</strong> {{ localData.niveauActuel }}
      </div>
      <div v-if="localData.ageApprenant" class="summary-item">
        <strong>Âge apprenant:</strong> {{ localData.ageApprenant }}
      </div>
      <div v-if="localData.dureeParSession" class="summary-item">
        <strong>Durée par session:</strong> {{ localData.dureeParSession }}h
      </div>
      <div v-if="localData.nombreSessions" class="summary-item">
        <strong>Nombre de sessions:</strong> {{ localData.nombreSessions }}
      </div>
      <div v-if="localData.frequenceSouhaitee" class="summary-item">
        <strong>Fréquence:</strong> {{ localData.frequenceSouhaitee }}
      </div>
      <div v-if="localData.lieuFormation && localData.modalite === 'presentiel'" class="summary-item">
        <strong>Lieu:</strong> {{ localData.lieuFormation }}
      </div>
      <div v-if="localData.preparationExamen" class="summary-item">
        <strong>Préparation examen:</strong> 
        {{ localData.nomExamen || 'Oui' }}
        <span v-if="localData.dateExamen"> ({{ formatDate(localData.dateExamen) }})</span>
      </div>
      <div v-if="localData.methodePedagogique" class="summary-item">
        <strong>Méthode:</strong> {{ localData.methodePedagogique }}
      </div>
      <div v-if="localData.supportsCours" class="summary-item">
        <strong>Supports:</strong> {{ localData.supportsCours }}
      </div>
      <div v-if="localData.materielRequis" class="summary-item">
        <strong>Matériel requis:</strong> {{ localData.materielRequis }}
      </div>
      <div v-if="localData.devoirs" class="summary-item">
        <strong>Devoirs:</strong> Souhaités entre les sessions
      </div>
      <div v-if="localData.objectifs" class="summary-item">
        <strong>Objectifs:</strong> {{ localData.objectifs }}
      </div>
      <div v-if="localData.disponibilites" class="summary-item">
        <strong>Disponibilités:</strong> {{ localData.disponibilites }}
      </div>
      <div v-if="localData.handicapAdaptation" class="summary-item">
        <strong>Adaptations:</strong> {{ localData.handicapAdaptation }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.education-form {
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

.pro-input:disabled {
  background-color: var(--border-color);
  color: var(--text-secondary);
  cursor: not-allowed;
}

.pro-textarea {
  resize: vertical;
  min-height: 100px;
  line-height: 1.5;
}

.text-muted {
  color: var(--text-secondary);
  font-size: 0.8rem;
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
