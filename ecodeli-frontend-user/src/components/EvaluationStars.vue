<template>
  <div class="evaluation-stars">
    <div class="stars-container">
      <div 
        v-for="star in 5" 
        :key="star"
        class="star-wrapper"
        @click="selectStar(star)"
        @mouseenter="hoverStar(star)"
        @mouseleave="resetHover"
      >
        <i 
          :class="getStarClass(star)"
          class="star-icon"
        ></i>
      </div>
    </div>
    
    <div v-if="showLabel" class="stars-label">
      <span class="note-text">{{ getNoteLabel(displayValue) }}</span>
      <span v-if="displayValue > 0" class="note-value">({{ displayValue }}/5)</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: 0
  },
  readonly: {
    type: Boolean,
    default: false
  },
  showLabel: {
    type: Boolean,
    default: true
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const hoverValue = ref(0)

const displayValue = computed(() => {
  return hoverValue.value || props.modelValue
})

const starSize = computed(() => {
  const sizes = {
    small: 'text-sm',
    medium: 'text-lg',
    large: 'text-2xl'
  }
  return sizes[props.size]
})

const selectStar = (star) => {
  if (props.readonly) return
  
  const newValue = star === props.modelValue ? 0 : star
  emit('update:modelValue', newValue)
  emit('change', newValue)
}

const hoverStar = (star) => {
  if (props.readonly) return
  hoverValue.value = star
}

const resetHover = () => {
  if (props.readonly) return
  hoverValue.value = 0
}

const getStarClass = (star) => {
  const baseClass = `pi ${starSize.value} star-icon`
  
  if (star <= displayValue.value) {
    return `${baseClass} pi-star-fill star-filled`
  } else {
    return `${baseClass} pi-star star-empty`
  }
}

const getNoteLabel = (value) => {
  const labels = {
    0: '',
    1: 'Insuffisant',
    2: 'Passable', 
    3: 'Bien',
    4: 'Très bien',
    5: 'Excellent'
  }
  return labels[value] || ''
}

watch(() => props.modelValue, (newValue) => {
  hoverValue.value = 0
})
</script>

<style scoped>
.evaluation-stars {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.5rem;
}

.stars-container {
  display: flex;
  gap: 0.25rem;
  align-items: center;
}

.star-wrapper {
  cursor: pointer;
  transition: transform 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.125rem;
  border-radius: 50%;
}

.star-wrapper:hover {
  transform: scale(1.1);
}

.star-wrapper.readonly {
  cursor: default;
}

.star-wrapper.readonly:hover {
  transform: none;
}

.star-icon {
  transition: all 0.3s ease;
  color: #d1d5db; /* Gris par défaut */
}

.star-filled {
  color: #fbbf24 !important; /* Jaune doré */
  text-shadow: 0 0 4px rgba(251, 191, 36, 0.3);
}

.star-empty {
  color: #d1d5db; /* Gris */
}

.star-wrapper:hover .star-icon {
  color: #fbbf24;
  filter: brightness(1.1);
}

.stars-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-height: 1.5rem;
}

.note-text {
  font-weight: 500;
  color: #374151;
  font-size: 0.875rem;
}

.note-value {
  font-size: 0.75rem;
  color: #6b7280;
}

/* Variations de taille */
.text-sm {
  font-size: 0.875rem;
}

.text-lg {
  font-size: 1.125rem;
}

.text-2xl {
  font-size: 1.5rem;
}

/* Animation pour les étoiles */
.star-icon {
  animation: none;
}

.star-filled {
  animation: starFill 0.3s ease forwards;
}

@keyframes starFill {
  0% {
    transform: scale(1);
    color: #d1d5db;
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
    color: #fbbf24;
  }
}

/* États spéciaux */
.note-text.excellent {
  color: #059669;
  font-weight: 600;
}

.note-text.tres-bien {
  color: #0891b2;
  font-weight: 600;
}

.note-text.bien {
  color: #7c3aed;
  font-weight: 600;
}

.note-text.passable {
  color: #ea580c;
  font-weight: 600;
}

.note-text.insuffisant {
  color: #dc2626;
  font-weight: 600;
}

/* Responsive */
@media (max-width: 640px) {
  .stars-container {
    gap: 0.125rem;
  }
  
  .star-wrapper {
    padding: 0.0625rem;
  }
  
  .stars-label {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}

/* Accessibilité */
.star-wrapper:focus {
  outline: 2px solid #3b82f6;
  outline-offset: 2px;
  border-radius: 4px;
}

.star-wrapper:focus:not(:focus-visible) {
  outline: none;
}

/* Support pour les lecteurs d'écran */
.star-wrapper[aria-label] {
  position: relative;
}

/* Animation hover groupe */
.stars-container:hover .star-icon {
  transition: all 0.15s ease;
}
</style>