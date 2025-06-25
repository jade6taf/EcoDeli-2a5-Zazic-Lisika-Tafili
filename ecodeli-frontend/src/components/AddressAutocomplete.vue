<script>
import { addressAutocompleteService, mapsServices } from '@/services/mapsServices'

export default {
  name: 'AddressAutocompleteEnhanced',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: 'Entrez une adresse...'
    },
    disabled: {
      type: Boolean,
      default: false
    },
    required: {
      type: Boolean,
      default: false
    },
    validateOnSelect: {
      type: Boolean,
      default: true
    },
    showValidation: {
      type: Boolean,
      default: true
    },
    showCoordinates: {
      type: Boolean,
      default: false
    },
    showLocationButton: {
      type: Boolean,
      default: true
    },
    debounceDelay: {
      type: Number,
      default: 300
    }
  },
  data() {
    return {
      inputValue: this.modelValue,
      suggestions: [],
      showSuggestions: false,
      highlightedIndex: -1,
      isLoading: false,
      isLocating: false,
      hasError: false,
      errorMessage: '',
      isValid: false,
      validatedAddress: null,
      debounceTimer: null,
      isFocused: false
    }
  },
  watch: {
    modelValue(newValue) {
      this.inputValue = newValue
      if (!newValue) {
        this.resetValidation()
      }
    }
  },
  async mounted() {
    await addressAutocompleteService.initialize()
  },
  methods: {
    onInput() {
      this.$emit('update:modelValue', this.inputValue)
      this.resetValidation()
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer)
      }
      this.debounceTimer = setTimeout(() => {
        this.searchSuggestions()
      }, this.debounceDelay)
    },

    onFocus() {
      this.isFocused = true
      if (this.inputValue && this.suggestions.length > 0) {
        this.showSuggestions = true
      }
    },

    onBlur() {
      this.isFocused = false
      setTimeout(() => {
        if (!this.isFocused) {
          this.showSuggestions = false
        }
      }, 200)
    },

    onKeydown(event) {
      if (!this.showSuggestions || this.suggestions.length === 0) return

      switch (event.key) {
        case 'ArrowDown':
          event.preventDefault()
          this.highlightedIndex = Math.min(this.highlightedIndex + 1, this.suggestions.length - 1)
          break
        case 'ArrowUp':
          event.preventDefault()
          this.highlightedIndex = Math.max(this.highlightedIndex - 1, -1)
          break
        case 'Enter':
          event.preventDefault()
          if (this.highlightedIndex >= 0) {
            this.selectSuggestion(this.suggestions[this.highlightedIndex])
          }
          break
        case 'Escape':
          this.showSuggestions = false
          this.highlightedIndex = -1
          break
      }
    },

    async searchSuggestions() {
      if (!this.inputValue || this.inputValue.length < 3) {
        this.suggestions = []
        this.showSuggestions = false
        return
      }

      this.isLoading = true
      this.resetValidation()

      try {
        if (!addressAutocompleteService.isGoogleMapsLoaded()) {
          console.log('Chargement de Google Maps API en cours...')
        }

        this.suggestions = await addressAutocompleteService.getSuggestions(this.inputValue)
        this.showSuggestions = this.suggestions.length > 0 && this.isFocused
        this.highlightedIndex = -1
        if (this.suggestions.length === 0 && this.inputValue.length >= 3) {
          console.log('Aucune suggestion trouvée pour:', this.inputValue)
        }
      } catch (error) {
        console.error('Erreur lors de la recherche de suggestions:', error)
        if (error.message.includes('Service d\'autocomplétion non disponible')) {
          this.setError('Service d\'autocomplétion en cours de chargement, veuillez patienter...')
          setTimeout(() => {
            if (this.inputValue && this.inputValue.length >= 3) {
              this.searchSuggestions()
            }
          }, 2000)
        } else {
          this.setError('Erreur lors de la recherche d\'adresses. Vérifiez votre connexion.')
        }
      } finally {
        this.isLoading = false
      }
    },

    async selectSuggestion(suggestion) {
      this.inputValue = suggestion.description
      this.$emit('update:modelValue', this.inputValue)
      this.showSuggestions = false
      this.highlightedIndex = -1

      if (this.validateOnSelect) {
        await this.validateSelectedAddress(suggestion)
      }

      this.$emit('suggestion-selected', suggestion)
    },

    async validateSelectedAddress(suggestion) {
      this.isLoading = true
      this.resetValidation()

      try {
        const details = await addressAutocompleteService.getPlaceDetails(suggestion.id)
        this.validatedAddress = {
          address: details.address,
          coordinates: details.coordinates,
          name: details.name || ''
        }
        this.isValid = true
        this.$emit('address-validated', this.validatedAddress)
        console.log('Adresse validée avec succès:', this.validatedAddress.address)
      } catch (error) {
        console.error('Erreur lors de la validation de l\'adresse:', error)

        if (error.message.includes('Google Maps API non disponible')) {
          this.setError('Service de cartes en cours de chargement, veuillez réessayer')
        } else {
          this.setError('Impossible de valider cette adresse. Veuillez en choisir une autre.')
        }
      } finally {
        this.isLoading = false
      }
    },

    async useCurrentLocation() {
      if (!navigator.geolocation) {
        this.setError('La géolocalisation n\'est pas supportée par votre navigateur')
        return
      }

      this.isLocating = true
      this.resetValidation()

      try {
        const position = await new Promise((resolve, reject) => {
          navigator.geolocation.getCurrentPosition(resolve, reject, {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 60000
          })
        })

        const coords = {
          lat: position.coords.latitude,
          lng: position.coords.longitude
        }

        await this.reverseGeocode(coords)
      } catch (error) {
        console.error('Erreur de géolocalisation:', error)
        this.setError('Impossible d\'obtenir votre position')
      } finally {
        this.isLocating = false
      }
    },

    async reverseGeocode(coordinates) {
      try {
        if (window.google && window.google.maps) {
          const geocoder = new window.google.maps.Geocoder()
          const results = await new Promise((resolve, reject) => {
            geocoder.geocode({ location: coordinates }, (results, status) => {
              if (status === 'OK' && results[0]) {
                resolve(results)
              } else {
                reject(new Error('Aucune adresse trouvée'))
              }
            })
          })

          const address = results[0].formatted_address
          this.inputValue = address
          this.$emit('update:modelValue', address)
          this.validatedAddress = {
            address: address,
            coordinates: coordinates
          }
          this.isValid = true
          this.$emit('location-found', this.validatedAddress)
        }
      } catch (error) {
        console.error('Erreur de reverse geocoding:', error)
        this.setError('Impossible de déterminer l\'adresse de votre position')
      }
    },

    setError(message) {
      this.hasError = true
      this.errorMessage = message
      this.isValid = false
      this.validatedAddress = null
    },

    resetValidation() {
      this.hasError = false
      this.errorMessage = ''
      this.isValid = false
      this.validatedAddress = null
    },

    validate() {
      if (this.required && !this.inputValue) {
        this.setError('Cette adresse est obligatoire')
        return false
      }
      return this.isValid
    },

    clear() {
      this.inputValue = ''
      this.$emit('update:modelValue', '')
      this.resetValidation()
      this.suggestions = []
      this.showSuggestions = false
    },

    focus() {
      this.$refs.addressInput.focus()
    }
  }
}
</script>

<template>
  <div class="address-autocomplete-enhanced">
    <div class="input-container" :class="{ 'has-error': hasError, 'is-loading': isLoading }">
      <input
        ref="addressInput"
        type="text"
        v-model="inputValue"
        @input="onInput"
        @focus="onFocus"
        @blur="onBlur"
        @keydown="onKeydown"
        :placeholder="placeholder"
        :disabled="disabled"
        class="address-input"
      />

      <div class="input-icons">
        <i v-if="isLoading" class="fas fa-spinner fa-spin loading-icon"></i>
        <i v-else-if="hasError" class="fas fa-exclamation-triangle error-icon"></i>
        <i v-else-if="isValid" class="fas fa-check-circle success-icon"></i>
        <i v-else class="fas fa-map-marker-alt default-icon"></i>
      </div>
    </div>

    <div v-if="showSuggestions && suggestions.length > 0" class="suggestions-dropdown">
      <div
        v-for="(suggestion, index) in suggestions"
        :key="suggestion.id"
        @click="selectSuggestion(suggestion)"
        @mouseenter="highlightedIndex = index"
        class="suggestion-item"
        :class="{ 'highlighted': index === highlightedIndex }"
      >
        <div class="suggestion-icon">
          <i class="fas fa-map-marker-alt"></i>
        </div>
        <div class="suggestion-content">
          <div class="suggestion-main">{{ suggestion.mainText }}</div>
          <div class="suggestion-secondary">{{ suggestion.secondaryText }}</div>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="error-message">
      <i class="fas fa-exclamation-circle"></i>
      {{ errorMessage }}
    </div>

    <div v-if="validatedAddress && showValidation" class="validated-address">
      <div class="validation-header">
        <i class="fas fa-check-circle"></i>
        <span>Adresse validée</span>
      </div>
      <div class="validation-content">
        <div class="address-text">{{ validatedAddress.address }}</div>
        <div v-if="showCoordinates" class="coordinates">
          <small>{{ validatedAddress.coordinates.lat.toFixed(6) }}, {{ validatedAddress.coordinates.lng.toFixed(6) }}</small>
        </div>
      </div>
    </div>

    <button
      v-if="showLocationButton"
      @click="useCurrentLocation"
      class="location-button"
      :disabled="isLocating"
      type="button"
    >
      <i :class="isLocating ? 'fas fa-spinner fa-spin' : 'fas fa-location-arrow'"></i>
      {{ isLocating ? 'Localisation...' : 'Ma position' }}
    </button>
  </div>
</template>

<style scoped>
.address-autocomplete-enhanced {
  position: relative;
  width: 100%;
}

.input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.address-input {
  width: 100%;
  padding: 12px 45px 12px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: white;
}

.address-input:focus {
  outline: none;
  border-color: #2196f3;
  box-shadow: 0 0 0 3px rgba(33, 150, 243, 0.1);
}

.input-container.has-error .address-input {
  border-color: #f44336;
}

.input-container.has-error .address-input:focus {
  box-shadow: 0 0 0 3px rgba(244, 67, 54, 0.1);
}

.input-container.is-loading .address-input {
  border-color: #ff9800;
}

.input-icons {
  position: absolute;
  right: 15px;
  display: flex;
  align-items: center;
  pointer-events: none;
}

.default-icon {
  color: #9e9e9e;
}

.loading-icon {
  color: #ff9800;
}

.error-icon {
  color: #f44336;
}

.success-icon {
  color: #4caf50;
}

.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.suggestion-item:hover,
.suggestion-item.highlighted {
  background-color: #f5f5f5;
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-icon {
  margin-right: 12px;
  color: #9e9e9e;
  width: 20px;
  text-align: center;
}

.suggestion-content {
  flex: 1;
  min-width: 0;
}

.suggestion-main {
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suggestion-secondary {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #ffebee;
  color: #c62828;
  border-radius: 4px;
  font-size: 14px;
}

.validated-address {
  margin-top: 12px;
  padding: 12px;
  background-color: #e8f5e8;
  border-radius: 8px;
  border-left: 4px solid #4caf50;
}

.validation-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2e7d32;
  font-weight: 600;
  margin-bottom: 8px;
}

.validation-content {
  color: #1b5e20;
}

.address-text {
  font-weight: 500;
  margin-bottom: 4px;
}

.coordinates {
  color: #4caf50;
  font-family: monospace;
}

.location-button {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.location-button:hover:not(:disabled) {
  background: #1976d2;
  transform: translateY(-1px);
}

.location-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .address-input {
    font-size: 16px; /* Évite le zoom sur iOS */
  }
  
  .suggestions-dropdown {
    max-height: 200px;
  }
  
  .suggestion-item {
    padding: 10px 12px;
  }
}
</style>