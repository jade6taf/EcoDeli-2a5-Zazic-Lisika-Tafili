<script>
import { ref, computed, watch, onMounted } from 'vue'

export default {
  name: 'PasswordInput',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    label: {
      type: String,
      default: 'Mot de passe'
    },
    placeholder: {
      type: String,
      default: 'Entrez votre mot de passe'
    },
    required: {
      type: Boolean,
      default: false
    },
    showStrength: {
      type: Boolean,
      default: true
    },
    showCriteria: {
      type: Boolean,
      default: true
    },
    showGenerator: {
      type: Boolean,
      default: true
    },
    userInfo: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['update:modelValue', 'validation-change'],
  setup(props, { emit }) {
    const showPassword = ref(false)
    const generating = ref(false)
    const passwordStrength = ref(0)
    const validationResult = ref({
      valid: false,
      strength: 0,
      errors: [],
      suggestions: []
    })
    const debounceTimer = ref(null)

    const inputId = ref(`password-input-${Math.random().toString(36).substr(2, 9)}`)

    const strengthPercentage = computed(() => passwordStrength.value)

    const strengthClass = computed(() => {
      if (passwordStrength.value < 25)
        return 'very-weak'
      if (passwordStrength.value < 50)
        return 'weak'
      if (passwordStrength.value < 75)
        return 'medium'
      if (passwordStrength.value < 90)
        return 'strong'
      return 'very-strong'
    })

    const strengthLabel = computed(() => {
      if (passwordStrength.value < 25)
        return 'Très faible'
      if (passwordStrength.value < 50)
        return 'Faible'
      if (passwordStrength.value < 75)
        return 'Moyen'
      if (passwordStrength.value < 90)
        return 'Fort'
      return 'Très fort'
    })

    const criteria = computed(() => {
      const password = props.modelValue || ''
      return {
        length: password.length >= 8,
        lowercase: /[a-z]/.test(password),
        uppercase: /[A-Z]/.test(password),
        numbers: /[0-9]/.test(password),
        special: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password),
        noSequences: !containsSequences(password),
        noPersonalInfo: !containsPersonalInfo(password)
      }
    })

    const isValid = computed(() => validationResult.value.valid)
    const hasErrors = computed(() => validationResult.value.errors.length > 0)

    const handleInput = (event) => {
      const value = event.target.value
      emit('update:modelValue', value)
      if (debounceTimer.value) {
        clearTimeout(debounceTimer.value)
      }
      debounceTimer.value = setTimeout(() => {
        validatePassword(value)
      }, 300)
    }

    const handleBlur = () => {
      if (props.modelValue) {
        validatePassword(props.modelValue)
      }
    }

    const togglePasswordVisibility = () => {
      showPassword.value = !showPassword.value
    }

    const generatePassword = async () => {
      generating.value = true
      try {
        const response = await fetch('/api/auth/generate-password?length=12', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        })

        if (response.ok) {
          const data = await response.json()
          emit('update:modelValue', data.password)
          passwordStrength.value = data.strength
          await validatePassword(data.password)
        }
      } catch (error) {
        console.error('Erreur lors de la génération du mot de passe:', error)
      } finally {
        generating.value = false
      }
    }

    const validatePassword = async (password) => {
      if (!password || password.length === 0) {
        validationResult.value = {
          valid: false,
          strength: 0,
          errors: [],
          suggestions: []
        }
        passwordStrength.value = 0
        emit('validation-change', validationResult.value)
        return
      }

      try {
        const response = await fetch('/api/auth/validate-password', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            password: password,
            nom: props.userInfo.nom || '',
            prenom: props.userInfo.prenom || '',
            email: props.userInfo.email || ''
          })
        })

        if (response.ok) {
          const result = await response.json()
          validationResult.value = result
          passwordStrength.value = result.strength
          emit('validation-change', result)
        }
      } catch (error) {
        console.error('Erreur lors de la validation du mot de passe:', error)
        passwordStrength.value = calculateClientSideStrength(password)
        validationResult.value = {
          valid: passwordStrength.value >= 50,
          strength: passwordStrength.value,
          errors: [],
          suggestions: passwordStrength.value < 50 ? ['Utilisez un mot de passe plus complexe'] : []
        }
        emit('validation-change', validationResult.value)
      }
    }

    const calculateClientSideStrength = (password) => {
      let score = 0

      score += Math.min(password.length * 2, 25)

      if (/[a-z]/.test(password)) score += 10
      if (/[A-Z]/.test(password)) score += 10
      if (/[0-9]/.test(password)) score += 10
      if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) score += 10

      if (password.length >= 12)
        score += 10
      if (password.length >= 16)
        score += 10

      const unique = new Set(password).size
      if (unique >= password.length * 0.7)
        score += 15
      return Math.max(0, Math.min(100, score))
    }

    const containsSequences = (password) => {
      const sequences = ['123456', '654321', 'abcdef', 'qwerty', 'azerty']
      const lowerPassword = password.toLowerCase()
      return sequences.some(seq => lowerPassword.includes(seq))
    }

    const containsPersonalInfo = (password) => {
      if (!props.userInfo)
        return false

      const lowerPassword = password.toLowerCase()
      const { nom, prenom, email } = props.userInfo

      if (nom && lowerPassword.includes(nom.toLowerCase()))
        return true
      if (prenom && lowerPassword.includes(prenom.toLowerCase()))
        return true
      if (email) {
        const emailPrefix = email.split('@')[0].toLowerCase()
        if (lowerPassword.includes(emailPrefix))
            return true
      }
      return false
    }
    watch(() => props.modelValue, (newValue) => {
      if (newValue && newValue.length > 0) {
        validatePassword(newValue)
      } else {
        passwordStrength.value = 0
        validationResult.value = {
          valid: false,
          strength: 0,
          errors: [],
          suggestions: []
        }
        emit('validation-change', validationResult.value)
      }
    })

    return {
      // Refs
      showPassword,
      generating,
      passwordStrength,
      validationResult,
      inputId,
      // Computed
      strengthPercentage,
      strengthClass,
      strengthLabel,
      criteria,
      isValid,
      hasErrors,
      // Methods
      handleInput,
      handleBlur,
      togglePasswordVisibility,
      generatePassword
    }
  }
}
</script>

<template>
  <div class="password-input-container">
    <div class="password-field">
      <label :for="inputId" class="password-label">
        {{ label }}
        <span v-if="required" class="required">*</span>
      </label>

      <div class="password-input-wrapper">
        <input
          :id="inputId"
          :type="showPassword ? 'text' : 'password'"
          :value="modelValue"
          @input="handleInput"
          @blur="handleBlur"
          :placeholder="placeholder"
          :required="required"
          class="password-input"
          :class="{
            'has-error': hasErrors,
            'is-valid': isValid && modelValue.length > 0
          }"
          autocomplete="new-password"
        />

        <div class="password-actions">
          <button
            type="button"
            @click="togglePasswordVisibility"
            class="toggle-password-btn"
            :title="showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'"
          >
            <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
          </button>

          <button
            v-if="showGenerator"
            type="button"
            @click="generatePassword"
            class="generate-password-btn"
            title="Générer un mot de passe sécurisé"
            :disabled="generating"
          >
            <i v-if="generating" class="fas fa-spinner fa-spin"></i>
            <i v-else class="fas fa-dice"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- Indicateur de force du mot de passe -->
    <div v-if="showStrength && modelValue" class="password-strength">
      <div class="strength-bar">
        <div
          class="strength-progress"
          :class="strengthClass"
          :style="{ width: strengthPercentage + '%' }"
        ></div>
      </div>
      <div class="strength-info">
        <span class="strength-label" :class="strengthClass">
          {{ strengthLabel }}
        </span>
        <span class="strength-score">{{ passwordStrength }}/100</span>
      </div>
    </div>

    <!-- Critères de validation -->
    <div v-if="showCriteria && modelValue" class="password-criteria">
      <h4 class="criteria-title">Critères de sécurité :</h4>
      <ul class="criteria-list">
        <li class="criteria-item" :class="{ 'valid': criteria.length }">
          <i :class="criteria.length ? 'fas fa-check' : 'fas fa-times'"></i>
          Au moins 8 caractères
        </li>
        <li class="criteria-item" :class="{ 'valid': criteria.lowercase }">
          <i :class="criteria.lowercase ? 'fas fa-check' : 'fas fa-times'"></i>
          Lettres minuscules (a-z)
        </li>
        <li class="criteria-item" :class="{ 'valid': criteria.uppercase }">
          <i :class="criteria.uppercase ? 'fas fa-check' : 'fas fa-times'"></i>
          Lettres majuscules (A-Z)
        </li>
        <li class="criteria-item" :class="{ 'valid': criteria.numbers }">
          <i :class="criteria.numbers ? 'fas fa-check' : 'fas fa-times'"></i>
          Chiffres (0-9)
        </li>
        <li class="criteria-item" :class="{ 'valid': criteria.special }">
          <i :class="criteria.special ? 'fas fa-check' : 'fas fa-times'"></i>
          Caractères spéciaux (!@#$%^&*)
        </li>
        <li class="criteria-item" :class="{ 'valid': criteria.noSequences }">
          <i :class="criteria.noSequences ? 'fas fa-check' : 'fas fa-times'"></i>
          Pas de séquences communes
        </li>
        <li v-if="userInfo && (userInfo.nom || userInfo.prenom || userInfo.email)" 
           class="criteria-item" :class="{ 'valid': criteria.noPersonalInfo }">
          <i :class="criteria.noPersonalInfo ? 'fas fa-check' : 'fas fa-times'"></i>
          Pas d'informations personnelles
        </li>
      </ul>
    </div>

    <!-- Messages d'erreur -->
    <div v-if="hasErrors" class="password-errors">
      <ul class="error-list">
        <li v-for="error in validationResult.errors" :key="error" class="error-item">
          <i class="fas fa-exclamation-triangle"></i>
          {{ error }}
        </li>
      </ul>
    </div>

    <!-- Suggestions -->
    <div v-if="validationResult.suggestions && validationResult.suggestions.length > 0" class="password-suggestions">
      <h5 class="suggestions-title">Suggestions :</h5>
      <ul class="suggestions-list">
        <li v-for="suggestion in validationResult.suggestions" :key="suggestion" class="suggestion-item">
          <i class="fas fa-lightbulb"></i>
          {{ suggestion }}
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.password-input-container {
  width: 100%;
}

.password-field {
  margin-bottom: 1rem;
}

.password-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: var(--text-color);
}

.required {
  color: var(--error-color);
}

.password-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-input {
  width: 100%;
  padding: 1rem 4rem 1rem 1rem;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
  color: var(--text-color);
}

.password-input:focus {
  border-color: var(--primary-color);
  outline: none;
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

.password-input.has-error {
  border-color: var(--error-color);
}

.password-input.is-valid {
  border-color: var(--success-color);
}

.password-actions {
  position: absolute;
  right: 0.5rem;
  display: flex;
  gap: 0.25rem;
}

.toggle-password-btn,
.generate-password-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-password-btn:hover,
.generate-password-btn:hover {
  background: var(--bg-secondary);
  color: var(--primary-color);
}

.generate-password-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.password-strength {
  margin-bottom: 1rem;
}

.strength-bar {
  width: 100%;
  height: 8px;
  background: var(--border-light);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.strength-progress {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 4px;
}

.strength-progress.very-weak {
  background: #f44336;
}

.strength-progress.weak {
  background: #ff9800;
}

.strength-progress.medium {
  background: #ffeb3b;
}

.strength-progress.strong {
  background: #8bc34a;
}

.strength-progress.very-strong {
  background: #4caf50;
}

.strength-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.strength-label {
  font-weight: 600;
  font-size: 0.9rem;
}

.strength-label.very-weak {
  color: #f44336;
}

.strength-label.weak {
  color: #ff9800;
}

.strength-label.medium {
  color: #ff9800;
}

.strength-label.strong {
  color: #8bc34a;
}

.strength-label.very-strong {
  color: #4caf50;
}

.strength-score {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.password-criteria {
  margin-bottom: 1rem;
}

.criteria-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-color);
  margin: 0 0 0.75rem 0;
}

.criteria-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 0.5rem;
}

.criteria-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: var(--error-color);
  transition: color 0.3s ease;
}

.criteria-item.valid {
  color: var(--success-color);
}

.criteria-item i {
  width: 12px;
  font-size: 0.75rem;
}

.password-errors {
  margin-bottom: 1rem;
}

.error-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.error-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--error-color);
  font-size: 0.85rem;
  margin-bottom: 0.25rem;
}

.password-suggestions {
  margin-bottom: 1rem;
}

.suggestions-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin: 0 0 0.5rem 0;
}

.suggestions-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--primary-color);
  font-size: 0.8rem;
  margin-bottom: 0.25rem;
}

.suggestion-item i {
  width: 12px;
  font-size: 0.75rem;
}

@media (max-width: 768px) {
  .criteria-list {
    grid-template-columns: 1fr;
  }
  
  .password-input {
    padding: 0.875rem 3.5rem 0.875rem 0.875rem;
  }
  
  .password-actions {
    right: 0.375rem;
  }
  
  .toggle-password-btn,
  .generate-password-btn {
    width: 32px;
    height: 32px;
  }
}

[data-theme="dark"] .password-input {
  background: var(--bg-secondary);
  border-color: var(--border-color);
  color: var(--text-color);
}

[data-theme="dark"] .strength-bar {
  background: var(--border-color);
}
</style>
