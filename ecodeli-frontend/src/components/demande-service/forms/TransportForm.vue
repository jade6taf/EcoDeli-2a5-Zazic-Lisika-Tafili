<script>
import AddressAutocomplete from '@/components/AddressAutocomplete.vue'

export default {
  name: 'TransportForm',
  components: {
    AddressAutocomplete
  },
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
        lieuDepart: '',
        lieuArrivee: '',
        dateHeure: '',
        typeTransport: '',
        descriptionContenu: '',
        instructions: '',
        vehiculeRequis: '',
        nombrePersonnes: null,
        poidsApproximatif: null,
        volumeApproximatif: null
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

      if (!this.localData.lieuDepart) {
        errors.lieuDepart = 'Le lieu de départ est obligatoire'
        isValid = false
      }

      if (!this.localData.lieuArrivee) {
        errors.lieuArrivee = 'Le lieu d\'arrivée est obligatoire'
        isValid = false
      }

      if (!this.localData.dateHeure) {
        errors.dateHeure = 'La date et heure sont obligatoires'
        isValid = false
      } else {
        const selectedDate = new Date(this.localData.dateHeure)
        const now = new Date()
        if (selectedDate < now) {
          errors.dateHeure = 'La date ne peut pas être dans le passé'
          isValid = false
        }
      }

      if (!this.localData.typeTransport) {
        errors.typeTransport = 'Le type de transport est obligatoire'
        isValid = false
      }

      if (this.localData.poidsApproximatif && this.localData.poidsApproximatif <= 0) {
        errors.poidsApproximatif = 'Le poids doit être positif'
        isValid = false
      }

      if (this.localData.volumeApproximatif && this.localData.volumeApproximatif <= 0) {
        errors.volumeApproximatif = 'Le volume doit être positif'
        isValid = false
      }

      if (this.localData.nombrePersonnes && (this.localData.nombrePersonnes < 1 || this.localData.nombrePersonnes > 8)) {
        errors.nombrePersonnes = 'Le nombre de personnes doit être entre 1 et 8'
        isValid = false
      }

      this.$emit('validate', isValid, errors)
    },

    handleAddressValidated(field, addressData) {
      console.log('Adresse validée:', field, addressData)
    },

    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    getTypeTransportLabel(type) {
      const labels = {
        'personnes': 'Transport de personnes',
        'colis': 'Livraison de colis',
        'demenagement': 'Déménagement'
      }
      return labels[type] || type
    },

    getVehiculeLabel(vehicule) {
      const labels = {
        'voiture': 'Voiture',
        'camionnette': 'Camionnette',
        'camion': 'Camion',
        'moto': 'Moto'
      }
      return labels[vehicule] || vehicule
    }
  },

  mounted() {
    this.validateForm()
  }
}
</script>

<template>
  <div class="transport-form">
    <div v-if="!readonly" class="professional-form">

      <!-- Champs essentiels -->
      <div class="essential-fields">
        <h3>Informations principales</h3>

        <div class="field-group">
          <div class="field-row">
            <div class="field-item">
              <label>Lieu de départ *</label>
              <AddressAutocomplete
                v-model="localData.lieuDepart"
                placeholder="Adresse de départ"
                :required="true"
                :class="{ 'error': errors.lieuDepart }"
                @address-validated="handleAddressValidated('lieuDepart', $event)"
              />
              <span v-if="errors.lieuDepart" class="error-message">{{ errors.lieuDepart }}</span>
            </div>
            <div class="field-item">
              <label>Lieu d'arrivée *</label>
              <AddressAutocomplete
                v-model="localData.lieuArrivee"
                placeholder="Adresse de destination"
                :required="true"
                :class="{ 'error': errors.lieuArrivee }"
                @address-validated="handleAddressValidated('lieuArrivee', $event)"
              />
              <span v-if="errors.lieuArrivee" class="error-message">{{ errors.lieuArrivee }}</span>
            </div>
          </div>

          <div class="field-row">
            <div class="field-item">
              <label>Date et heure *</label>
              <input
                type="datetime-local"
                v-model="localData.dateHeure"
                class="pro-input"
                :class="{ 'error': errors.dateHeure }"
                required
              >
              <span v-if="errors.dateHeure" class="error-message">{{ errors.dateHeure }}</span>
            </div>
            <div class="field-item">
              <label>Type de transport *</label>
              <select
                v-model="localData.typeTransport"
                class="pro-select"
                :class="{ 'error': errors.typeTransport }"
                required
              >
                <option value="">Sélectionnez</option>
                <option value="personnes">Transport de personnes</option>
                <option value="colis">Livraison de colis</option>
                <option value="demenagement">Déménagement</option>
              </select>
              <span v-if="errors.typeTransport" class="error-message">{{ errors.typeTransport }}</span>
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
              <label>Véhicule requis</label>
              <select v-model="localData.vehiculeRequis" class="pro-select">
                <option value="">Non spécifié</option>
                <option value="voiture">Voiture</option>
                <option value="camionnette">Camionnette</option>
                <option value="camion">Camion</option>
                <option value="moto">Moto</option>
              </select>
            </div>
            <div class="field-item" v-if="localData.typeTransport === 'personnes'">
              <label>Nombre de personnes</label>
              <input
                type="number"
                v-model="localData.nombrePersonnes"
                placeholder="Ex: 3"
                class="pro-input"
                min="1"
                max="8"
              >
            </div>
          </div>

          <div class="field-row" v-if="localData.typeTransport === 'colis' || localData.typeTransport === 'demenagement'">
            <div class="field-item">
              <label>Poids approximatif (kg)</label>
              <input
                type="number"
                v-model="localData.poidsApproximatif"
                placeholder="Ex: 25"
                class="pro-input"
                min="0"
                step="0.1"
              >
            </div>
            <div class="field-item">
              <label>Volume approximatif (m³)</label>
              <input
                type="number"
                v-model="localData.volumeApproximatif"
                placeholder="Ex: 2.5"
                class="pro-input"
                min="0"
                step="0.1"
              >
            </div>
          </div>

          <div class="field-item full-width">
            <label>Description du contenu</label>
            <textarea
              v-model="localData.descriptionContenu"
              placeholder="Décrivez ce qui doit être transporté (nature, volume, poids approximatif...)"
              class="pro-textarea"
              rows="3"
            ></textarea>
          </div>

          <div class="field-item full-width">
            <label>Instructions particulières</label>
            <textarea
              v-model="localData.instructions"
              placeholder="Contraintes d'accès, horaires spécifiques, précautions particulières..."
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
        <strong>Trajet:</strong> {{ localData.lieuDepart }} → {{ localData.lieuArrivee }}
      </div>
      <div class="summary-item">
        <strong>Date et heure:</strong> {{ formatDate(localData.dateHeure) }}
      </div>
      <div class="summary-item">
        <strong>Type:</strong> {{ getTypeTransportLabel(localData.typeTransport) }}
      </div>
      <div v-if="localData.vehiculeRequis" class="summary-item">
        <strong>Véhicule:</strong> {{ getVehiculeLabel(localData.vehiculeRequis) }}
      </div>
      <div v-if="localData.nombrePersonnes" class="summary-item">
        <strong>Nombre de personnes:</strong> {{ localData.nombrePersonnes }}
      </div>
      <div v-if="localData.poidsApproximatif" class="summary-item">
        <strong>Poids:</strong> {{ localData.poidsApproximatif }} kg
      </div>
      <div v-if="localData.volumeApproximatif" class="summary-item">
        <strong>Volume:</strong> {{ localData.volumeApproximatif }} m³
      </div>
      <div v-if="localData.descriptionContenu" class="summary-item">
        <strong>Description:</strong> {{ localData.descriptionContenu }}
      </div>
      <div v-if="localData.instructions" class="summary-item">
        <strong>Instructions:</strong> {{ localData.instructions }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.transport-form {
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
  
  .essential-fields, .additional-fields {
    padding: 1.5rem;
  }
}
</style>
