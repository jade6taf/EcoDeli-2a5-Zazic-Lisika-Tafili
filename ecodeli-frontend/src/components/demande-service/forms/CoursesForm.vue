<script>
export default {
  name: 'CoursesForm',
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
        typeAchat: '',
        listeArticles: '',
        adresseLivraison: '',
        dateLivraison: '',
        budgetEstimatif: null,
        magasinsPreferences: '',
        instructions: '',
        creneauLivraison: '',
        produitsBio: false,
        substitutsAcceptes: false,
        livraison24h: false,
        modeReception: '',
        codeAcces: '',
        etage: '',
        interphone: '',
        moyenPaiement: '',
        ticketDeCaisse: false,
        contraintesAllergies: ''
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

      if (!this.localData.typeAchat) {
        errors.typeAchat = 'Le type d\'achat est obligatoire'
        isValid = false
      }

      if (!this.localData.listeArticles) {
        errors.listeArticles = 'La liste des articles est obligatoire'
        isValid = false
      } else if (this.localData.listeArticles.length < 5) {
        errors.listeArticles = 'La liste doit faire au moins 5 caractères'
        isValid = false
      } else if (this.localData.listeArticles.length > 2000) {
        errors.listeArticles = 'La liste ne peut pas dépasser 2000 caractères'
        isValid = false
      }

      if (!this.localData.adresseLivraison) {
        errors.adresseLivraison = 'L\'adresse de livraison est obligatoire'
        isValid = false
      }

      if (!this.localData.dateLivraison) {
        errors.dateLivraison = 'La date de livraison est obligatoire'
        isValid = false
      } else {
        const selectedDate = new Date(this.localData.dateLivraison)
        const now = new Date()
        if (selectedDate < now) {
          errors.dateLivraison = 'La date ne peut pas être dans le passé'
          isValid = false
        }
      }

      if (this.localData.budgetEstimatif && this.localData.budgetEstimatif <= 0) {
        errors.budgetEstimatif = 'Le budget doit être positif'
        isValid = false
      }

      this.$emit('validate', isValid, errors)
    },

    formatDateTime(dateTimeString) {
      if (!dateTimeString) return ''
      const date = new Date(dateTimeString)
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    getTypeAchatLabel(type) {
      const labels = {
        'alimentaire': 'Alimentaire',
        'pharmacie': 'Pharmacie',
        'vetements': 'Vêtements',
        'electromenager': 'Électroménager',
        'divers': 'Divers'
      }
      return labels[type] || type
    },

    getModeReceptionLabel(mode) {
      const labels = {
        'domicile': 'À domicile',
        'point_relais': 'Point relais',
        'consigne': 'Consigne automatique'
      }
      return labels[mode] || mode
    },

    getMoyenPaiementLabel(moyen) {
      const labels = {
        'carte': 'Carte bancaire',
        'especes': 'Espèces',
        'cheque': 'Chèque'
      }
      return labels[moyen] || moyen
    },

    hasSpecialOptions() {
      return this.localData.produitsBio ||
             this.localData.substitutsAcceptes ||
             this.localData.livraison24h ||
             this.localData.ticketDeCaisse
    },

    getSpecialOptions() {
      const options = []
      if (this.localData.produitsBio) options.push('Bio privilégié')
      if (this.localData.substitutsAcceptes) options.push('Substituts OK')
      if (this.localData.livraison24h) options.push('Livraison 24h')
      if (this.localData.ticketDeCaisse) options.push('Ticket de caisse')
      return options.join(', ')
    }
  },

  mounted() {
    this.validateForm()
  }
}
</script>

<template>
  <div class="courses-form">
    <div v-if="!readonly" class="professional-form">

      <!-- Champs essentiels -->
      <div class="essential-fields">
        <h3>Informations principales</h3>

        <div class="field-group">
          <div class="field-row">
            <div class="field-item">
              <label>Type d'achat *</label>
              <select 
                v-model="localData.typeAchat" 
                class="pro-select" 
                :class="{ 'error': errors.typeAchat }"
                required
              >
                <option value="">Sélectionnez</option>
                <option value="alimentaire">Alimentaire</option>
                <option value="pharmacie">Pharmacie</option>
                <option value="vetements">Vêtements</option>
                <option value="electromenager">Électroménager</option>
                <option value="divers">Divers</option>
              </select>
              <span v-if="errors.typeAchat" class="error-message">{{ errors.typeAchat }}</span>
            </div>
            <div class="field-item">
              <label>Date de livraison *</label>
              <input
                type="datetime-local"
                v-model="localData.dateLivraison"
                class="pro-input"
                :class="{ 'error': errors.dateLivraison }"
                required
              >
              <span v-if="errors.dateLivraison" class="error-message">{{ errors.dateLivraison }}</span>
            </div>
          </div>

          <div class="field-item full-width">
            <label>Adresse de livraison *</label>
            <input
              type="text"
              v-model="localData.adresseLivraison"
              placeholder="Adresse complète de livraison"
              class="pro-input"
              :class="{ 'error': errors.adresseLivraison }"
              required
            >
            <span v-if="errors.adresseLivraison" class="error-message">{{ errors.adresseLivraison }}</span>
          </div>

          <div class="field-item full-width">
            <label>Liste des articles *</label>
            <textarea
              v-model="localData.listeArticles"
              placeholder="Listez les articles à acheter (un par ligne)&#10;Ex:&#10;- Pain complet, 1 baguette&#10;- Lait demi-écrémé, 2 bouteilles&#10;- Pommes, 1kg"
              class="pro-textarea"
              :class="{ 'error': errors.listeArticles }"
              rows="5"
              required
            ></textarea>
            <div class="char-counter">
              {{ localData.listeArticles.length }}/2000 caractères
            </div>
            <span v-if="errors.listeArticles" class="error-message">{{ errors.listeArticles }}</span>
          </div>
        </div>
      </div>

      <!-- Champs complémentaires -->
      <div class="additional-fields">
        <h3>Détails complémentaires</h3>

        <div class="field-group">
          <div class="field-row">
            <div class="field-item">
              <label>Budget estimatif (€)</label>
              <input
                type="number"
                v-model="localData.budgetEstimatif"
                placeholder="Ex: 50"
                class="pro-input"
                :class="{ 'error': errors.budgetEstimatif }"
                min="0"
                step="0.01"
              >
              <span v-if="errors.budgetEstimatif" class="error-message">{{ errors.budgetEstimatif }}</span>
            </div>
            <div class="field-item">
              <label>Créneau de livraison</label>
              <input
                type="text"
                v-model="localData.creneauLivraison"
                placeholder="Ex: 14h-16h, matin"
                class="pro-input"
              >
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Magasins préférés</label>
              <input
                type="text"
                v-model="localData.magasinsPreferences"
                placeholder="Ex: Carrefour, Leclerc, Monoprix"
                class="pro-input"
              >
            </div>
            <div class="field-item">
              <label>Mode de réception</label>
              <select v-model="localData.modeReception" class="pro-select">
                <option value="">Sélectionnez</option>
                <option value="domicile">À domicile</option>
                <option value="point_relais">Point relais</option>
                <option value="consigne">Consigne automatique</option>
              </select>
            </div>
          </div>

          <!-- Informations d'accès si livraison à domicile -->
          <div v-if="localData.modeReception === 'domicile'" class="field-row">
            <div class="field-item">
              <label>Code d'accès</label>
              <input
                type="text"
                v-model="localData.codeAcces"
                placeholder="Ex: A1234, porte bleue"
                class="pro-input"
              >
            </div>
            <div class="field-item">
              <label>Étage / Interphone</label>
              <div class="split-row">
                <input
                  type="text"
                  v-model="localData.etage"
                  placeholder="3ème"
                  class="pro-input"
                  style="flex: 1;"
                >
                <input
                  type="text"
                  v-model="localData.interphone"
                  placeholder="Nom"
                  class="pro-input"
                  style="flex: 1;"
                >
              </div>
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Moyen de paiement</label>
              <select v-model="localData.moyenPaiement" class="pro-select">
                <option value="">Non précisé</option>
                <option value="carte">Carte bancaire</option>
                <option value="especes">Espèces</option>
                <option value="cheque">Chèque</option>
              </select>
            </div>
            <div class="field-item">
              <label>Options spéciales</label>
              <div class="options-group">
                <label class="option-item">
                  <input type="checkbox" v-model="localData.produitsBio">
                  <span>Privilégier le bio</span>
                </label>
                <label class="option-item">
                  <input type="checkbox" v-model="localData.substitutsAcceptes">
                  <span>Substituts acceptés</span>
                </label>
                <label class="option-item">
                  <input type="checkbox" v-model="localData.livraison24h">
                  <span>Livraison sous 24h</span>
                </label>
                <label class="option-item">
                  <input type="checkbox" v-model="localData.ticketDeCaisse">
                  <span>Ticket de caisse souhaité</span>
                </label>
              </div>
            </div>
          </div>

          <div class="field-item full-width">
            <label>Contraintes allergies</label>
            <textarea
              v-model="localData.contraintesAllergies"
              placeholder="Précisez les allergies ou intolérances à éviter..."
              class="pro-textarea"
              rows="2"
            ></textarea>
          </div>

          <div class="field-item full-width">
            <label>Instructions particulières</label>
            <textarea
              v-model="localData.instructions"
              placeholder="Préférences de marques, instructions de livraison, demandes spéciales..."
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
        <strong>Type d'achat:</strong> {{ getTypeAchatLabel(localData.typeAchat) }}
      </div>
      <div class="summary-item">
        <strong>Date de livraison:</strong> {{ formatDateTime(localData.dateLivraison) }}
      </div>
      <div class="summary-item">
        <strong>Adresse de livraison:</strong> {{ localData.adresseLivraison }}
      </div>
      <div class="summary-item">
        <strong>Articles à acheter:</strong>
        <div class="articles-preview">{{ localData.listeArticles }}</div>
      </div>
      <div v-if="localData.budgetEstimatif" class="summary-item">
        <strong>Budget estimatif:</strong> {{ localData.budgetEstimatif }}€
      </div>
      <div v-if="localData.creneauLivraison" class="summary-item">
        <strong>Créneau:</strong> {{ localData.creneauLivraison }}
      </div>
      <div v-if="localData.magasinsPreferences" class="summary-item">
        <strong>Magasins préférés:</strong> {{ localData.magasinsPreferences }}
      </div>
      <div v-if="localData.modeReception" class="summary-item">
        <strong>Mode de réception:</strong> {{ getModeReceptionLabel(localData.modeReception) }}
      </div>
      <div v-if="localData.codeAcces" class="summary-item">
        <strong>Code d'accès:</strong> {{ localData.codeAcces }}
      </div>
      <div v-if="localData.etage || localData.interphone" class="summary-item">
        <strong>Accès:</strong>
        <span v-if="localData.etage">{{ localData.etage }}</span>
        <span v-if="localData.etage && localData.interphone"> - </span>
        <span v-if="localData.interphone">{{ localData.interphone }}</span>
      </div>
      <div v-if="localData.moyenPaiement" class="summary-item">
        <strong>Paiement:</strong> {{ getMoyenPaiementLabel(localData.moyenPaiement) }}
      </div>
      <div v-if="hasSpecialOptions()" class="summary-item">
        <strong>Options:</strong> {{ getSpecialOptions() }}
      </div>
      <div v-if="localData.contraintesAllergies" class="summary-item">
        <strong>Allergies:</strong> {{ localData.contraintesAllergies }}
      </div>
      <div v-if="localData.instructions" class="summary-item">
        <strong>Instructions:</strong> {{ localData.instructions }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.courses-form {
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

.split-row {
  display: flex;
  gap: 0.5rem;
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

.articles-preview {
  margin-top: 0.5rem;
  padding: 0.75rem;
  background-color: var(--background-color);
  border-radius: 4px;
  font-size: 0.9rem;
  white-space: pre-line;
  max-height: 150px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .field-row {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .split-row {
    flex-direction: column;
  }

  .essential-fields, .additional-fields {
    padding: 1.5rem;
  }
}
</style>
