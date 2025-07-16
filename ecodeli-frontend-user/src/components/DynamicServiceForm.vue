<template>
  <div class="dynamic-service-form">
    <div v-for="categorie in categories" :key="categorie.id" class="categorie-section mb-4">
      <div class="flex align-items-center mb-3">
        <span class="text-2xl mr-2">{{ categorie.icon }}</span>
        <h3 class="text-lg font-semibold m-0" :style="{ color: categorie.couleur }">
          {{ categorie.nom }}
        </h3>
      </div>
      
      <div v-for="champ in categorie.champsSpecifiques" :key="champ.id" class="field mb-3">
        <!-- Vérifier si le champ doit être affiché -->
        <div v-if="shouldShowField(champ)" class="field-container">
          <label :for="champ.id" class="field-label mb-2">
            {{ champ.label }}
            <span v-if="champ.obligatoire" class="text-red-500">*</span>
          </label>
          
          <!-- Champ texte -->
          <InputText 
            v-if="champ.type === 'text'"
            :id="champ.id"
            v-model="modelValue[champ.id]"
            :placeholder="champ.placeholder"
            class="w-full"
            @input="updateValue(champ.id, $event.target.value)"
          />
          
          <!-- Champ nombre -->
          <InputNumber 
            v-if="champ.type === 'number'"
            :id="champ.id"
            v-model="modelValue[champ.id]"
            :placeholder="champ.placeholder"
            :min="champ.min"
            :max="champ.max"
            :step="champ.step || 1"
            class="w-full"
            @input="updateValue(champ.id, $event.value)"
          />
          
          <!-- Select simple -->
          <Dropdown 
            v-if="champ.type === 'select'"
            :id="champ.id"
            v-model="modelValue[champ.id]"
            :options="champ.options"
            optionLabel="label"
            optionValue="value"
            :placeholder="'Sélectionnez ' + champ.label.toLowerCase()"
            class="w-full"
            @change="updateValue(champ.id, $event.value)"
          />
          
          <!-- Checkboxes multiples -->
          <div v-if="champ.type === 'checkbox-multiple'" class="checkbox-group">
            <div v-for="option in champ.options" :key="option.value" class="flex align-items-center mb-2">
              <Checkbox
                :id="`${champ.id}_${option.value}`"
                :value="option.value"
                :modelValue="isChecked(champ.id, option.value)"
                @update:modelValue="toggleCheckbox(champ.id, option.value, $event)"
                :binary="true"
              />
              <label :for="`${champ.id}_${option.value}`" class="ml-2 cursor-pointer">{{ option.label }}</label>
            </div>
          </div>
          
          <!-- Radio buttons -->
          <div v-if="champ.type === 'radio'" class="radio-group">
            <div v-for="option in champ.options" :key="option.value" class="flex align-items-center mb-2">
              <RadioButton
                :id="`${champ.id}_${option.value}`"
                :value="option.value"
                v-model="modelValue[champ.id]"
                @change="updateValue(champ.id, option.value)"
              />
              <label :for="`${champ.id}_${option.value}`" class="ml-2 cursor-pointer">{{ option.label }}</label>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, watch } from 'vue'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Dropdown from 'primevue/dropdown'
import Checkbox from 'primevue/checkbox'
import RadioButton from 'primevue/radiobutton'

const props = defineProps({
  categories: {
    type: Array,
    required: true
  },
  modelValue: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue'])

const shouldShowField = (champ) => {
  if (!champ.conditionAffichage) {
    return true
  }
  
  for (const [champCondition, valeursRequises] of Object.entries(champ.conditionAffichage)) {
    const valeurActuelle = props.modelValue[champCondition]
    
    if (!valeurActuelle) {
      return false
    }
    
    if (Array.isArray(valeurActuelle)) {
      const hasIntersection = valeursRequises.some(val => valeurActuelle.includes(val))
      if (!hasIntersection) {
        return false
      }
    } else {
      if (!valeursRequises.includes(valeurActuelle)) {
        return false
      }
    }
  }
  
  return true
}

const updateValue = (key, value) => {
  const newValue = { ...props.modelValue }
  newValue[key] = value
  emit('update:modelValue', newValue)
}

const isChecked = (key, optionValue) => {
  const values = props.modelValue[key] || []
  return values.includes(optionValue)
}

const toggleCheckbox = (key, optionValue, isChecked) => {
  const newValue = { ...props.modelValue }
  
  if (!newValue[key]) {
    newValue[key] = []
  }
  
  if (isChecked) {
    if (!newValue[key].includes(optionValue)) {
      newValue[key].push(optionValue)
    }
  } else {
    newValue[key] = newValue[key].filter(val => val !== optionValue)
  }
  
  emit('update:modelValue', newValue)
}

const updateCheckboxValue = (key, optionValue, isChecked) => {
  toggleCheckbox(key, optionValue, isChecked)
}

watch(() => props.categories, (newCategories) => {
  if (newCategories && newCategories.length > 0) {
    const newValue = { ...props.modelValue }
    let hasChanges = false
    
    newCategories.forEach(categorie => {
      categorie.champsSpecifiques.forEach(champ => {
        if (champ.type === 'checkbox-multiple' && !newValue[champ.id]) {
          newValue[champ.id] = []
          hasChanges = true
        }
      })
    })
    
    if (hasChanges) {
      emit('update:modelValue', newValue)
    }
  }
}, { immediate: true })
</script>

<style scoped>
.dynamic-service-form {
  max-width: 100%;
}

.categorie-section {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1.5rem;
  background: #fafafa;
}

.field-container {
  background: white;
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.field-label {
  display: block;
  font-weight: 500;
  color: #374151;
}

.checkbox-group,
.radio-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.checkbox-group label,
.radio-group label {
  cursor: pointer;
  font-weight: normal;
}

/* Responsive */
@media (max-width: 768px) {
  .categorie-section {
    padding: 1rem;
  }
  
  .field-container {
    padding: 0.75rem;
  }
}
</style>