<template>
  <div class="service-chariot-form">
    <!-- Service Hours Section -->
    <div class="form-section" :class="{ 'completed': serviceHoursCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-clock"></i>
        </div>
        <div class="section-title">
          <h3>Heures de service</h3>
          <p>Définissez vos heures d'ouverture pour le service</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="serviceHoursCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="form-row">
          <div class="form-group">
            <label for="heuresServiceDebut" class="form-label">
              Heure d'ouverture *
              <span class="help-text">Début du service</span>
            </label>
            <input
              type="time"
              id="heuresServiceDebut"
              v-model="localData.heuresServiceDebut"
              class="form-input"
              :class="{ 'error': errors.heuresServiceDebut }"
              @change="validateServiceHours"
            />
            <div class="field-error" v-if="errors.heuresServiceDebut">
              {{ errors.heuresServiceDebut }}
            </div>
          </div>

          <div class="form-group">
            <label for="heuresServiceFin" class="form-label">
              Heure de fermeture *
              <span class="help-text">Fin du service</span>
            </label>
            <input
              type="time"
              id="heuresServiceFin"
              v-model="localData.heuresServiceFin"
              class="form-input"
              :class="{ 'error': errors.heuresServiceFin }"
              @change="validateServiceHours"
            />
            <div class="field-error" v-if="errors.heuresServiceFin">
              {{ errors.heuresServiceFin }}
            </div>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">
            Jours de service *
            <span class="help-text">Sélectionnez les jours où le service est disponible</span>
          </label>
          <div class="days-selector">
            <label 
              v-for="(day, index) in daysOfWeek" 
              :key="index" 
              class="day-checkbox"
              :class="{ 'selected': selectedDays.includes(index) }"
            >
              <input
                type="checkbox"
                :value="index"
                v-model="selectedDays"
                @change="updateJoursService"
              />
              <span class="day-label">{{ day.short }}</span>
              <span class="day-full">{{ day.full }}</span>
            </label>
          </div>
          <div class="field-error" v-if="errors.joursService">
            {{ errors.joursService }}
          </div>
        </div>
      </div>
    </div>

    <!-- Coverage Area Section -->
    <div class="form-section" :class="{ 'completed': coverageAreaCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-map"></i>
        </div>
        <div class="section-title">
          <h3>Zone de couverture</h3>
          <p>Définissez votre zone de livraison</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="coverageAreaCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="form-group">
          <label for="zoneCouverture" class="form-label">
            Zone de couverture *
            <span class="help-text">Quartiers, arrondissements ou villes desservis</span>
          </label>
          <textarea
            id="zoneCouverture"
            v-model="localData.zoneCouverture"
            class="form-textarea"
            :class="{ 'error': errors.zoneCouverture }"
            placeholder="Ex: 1er, 2e, 3e et 4e arrondissements de Paris, Boulogne-Billancourt..."
            rows="4"
            @blur="validateCoverageArea"
          ></textarea>
          <div class="field-error" v-if="errors.zoneCouverture">
            {{ errors.zoneCouverture }}
          </div>
        </div>
      </div>
    </div>

    <!-- Service Configuration Section -->
    <div class="form-section" :class="{ 'completed': serviceConfigCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-cog"></i>
        </div>
        <div class="section-title">
          <h3>Configuration du service</h3>
          <p>Paramètres de fonctionnement</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="serviceConfigCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="form-row">
          <div class="form-group">
            <label for="commandeMinimum" class="form-label">
              Commande minimum *
              <span class="help-text">Montant minimum pour une livraison</span>
            </label>
            <div class="input-group">
              <input
                type="number"
                id="commandeMinimum"
                v-model.number="localData.commandeMinimum"
                class="form-input"
                :class="{ 'error': errors.commandeMinimum }"
                placeholder="25"
                min="0"
                max="500"
                step="0.1"
                @blur="validateServiceConfig"
              />
              <span class="input-addon">€</span>
            </div>
            <div class="field-error" v-if="errors.commandeMinimum">
              {{ errors.commandeMinimum }}
            </div>
          </div>

          <div class="form-group">
            <label for="tempsLivraisonMoyen" class="form-label">
              Temps de livraison moyen
              <span class="help-text">Durée moyenne en minutes</span>
            </label>
            <div class="input-group">
              <input
                type="number"
                id="tempsLivraisonMoyen"
                v-model.number="localData.tempsLivraisonMoyen"
                class="form-input"
                :class="{ 'error': errors.tempsLivraisonMoyen }"
                placeholder="30"
                min="5"
                max="120"
                @blur="validateServiceConfig"
              />
              <span class="input-addon">min</span>
            </div>
            <div class="field-error" v-if="errors.tempsLivraisonMoyen">
              {{ errors.tempsLivraisonMoyen }}
            </div>
          </div>
        </div>

        <div class="form-group">
          <label class="checkbox-label">
            <input
              type="checkbox"
              v-model="localData.gestionDisponibiliteContinue"
              @change="handleContinuousAvailabilityToggle"
            />
            <span class="checkbox-custom"></span>
            <span class="checkbox-text">
              Gestion de disponibilité en continu
              <span class="help-text">Mise à jour automatique des créneaux disponibles</span>
            </span>
          </label>
        </div>
      </div>
    </div>

    <!-- Time Slots Section -->
    <div class="form-section" :class="{ 'completed': timeSlotsCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-calendar-plus"></i>
        </div>
        <div class="section-title">
          <h3>Créneaux de livraison</h3>
          <p>Définissez vos créneaux horaires disponibles</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="timeSlotsCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="time-slots-manager">
          <div class="slots-list">
            <div 
              v-for="(slot, index) in timeSlots" 
              :key="index"
              class="time-slot-item"
            >
              <div class="slot-time">
                {{ slot.debut }} - {{ slot.fin }}
              </div>
              <div class="slot-capacity">
                {{ slot.capacite }} commandes max
              </div>
              <button 
                type="button" 
                class="slot-remove"
                @click="removeTimeSlot(index)"
              >
                <i class="pi pi-times"></i>
              </button>
            </div>
            
            <div class="no-slots" v-if="timeSlots.length === 0">
              <i class="pi pi-info-circle"></i>
              <p>Aucun créneau défini</p>
            </div>
          </div>
          
          <div class="add-slot-form">
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Début</label>
                <input
                  type="time"
                  v-model="newSlot.debut"
                  class="form-input"
                />
              </div>
              
              <div class="form-group">
                <label class="form-label">Fin</label>
                <input
                  type="time"
                  v-model="newSlot.fin"
                  class="form-input"
                />
              </div>
              
              <div class="form-group">
                <label class="form-label">Capacité</label>
                <input
                  type="number"
                  v-model.number="newSlot.capacite"
                  class="form-input"
                  min="1"
                  max="50"
                  placeholder="5"
                />
              </div>
              
              <div class="form-group">
                <button 
                  type="button" 
                  class="btn btn-add-slot"
                  @click="addTimeSlot"
                  :disabled="!canAddSlot"
                >
                  <i class="pi pi-plus"></i>
                  Ajouter
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useAnnonceCommercantStore } from '@/stores/annonceCommercant'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  },
  errors: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'validate', 'update-step'])

const annonceStore = useAnnonceCommercantStore()

const localData = ref({
  heuresServiceDebut: '',
  heuresServiceFin: '',
  zoneCouverture: '',
  commandeMinimum: null,
  tempsLivraisonMoyen: 30,
  gestionDisponibiliteContinue: false,
  creneauxDisponibles: '[]',
  joursService: '[]',
  ...props.modelValue
})

const selectedDays = ref([])
const timeSlots = ref([])
const newSlot = ref({
  debut: '',
  fin: '',
  capacite: 5
})

const errors = ref({})

const daysOfWeek = [
  { short: 'L', full: 'Lundi' },
  { short: 'M', full: 'Mardi' },
  { short: 'M', full: 'Mercredi' },
  { short: 'J', full: 'Jeudi' },
  { short: 'V', full: 'Vendredi' },
  { short: 'S', full: 'Samedi' },
  { short: 'D', full: 'Dimanche' }
]

const serviceHoursCompleted = computed(() => {
  return localData.value.heuresServiceDebut && 
         localData.value.heuresServiceFin && 
         selectedDays.value.length > 0
})

const coverageAreaCompleted = computed(() => {
  return localData.value.zoneCouverture && localData.value.zoneCouverture.trim().length > 0
})

const serviceConfigCompleted = computed(() => {
  return localData.value.commandeMinimum && localData.value.commandeMinimum > 0
})

const timeSlotsCompleted = computed(() => {
  return timeSlots.value.length > 0
})

const allCompleted = computed(() => {
  return serviceHoursCompleted.value && 
         coverageAreaCompleted.value && 
         serviceConfigCompleted.value && 
         timeSlotsCompleted.value &&
         Object.keys(errors.value).length === 0
})

const canAddSlot = computed(() => {
  return newSlot.value.debut && 
         newSlot.value.fin && 
         newSlot.value.capacite > 0 &&
         newSlot.value.debut < newSlot.value.fin
})

const validateServiceHours = () => {
  const newErrors = {}
  
  if (!localData.value.heuresServiceDebut) {
    newErrors.heuresServiceDebut = 'L\'heure d\'ouverture est obligatoire'
  }
  
  if (!localData.value.heuresServiceFin) {
    newErrors.heuresServiceFin = 'L\'heure de fermeture est obligatoire'
  }
  
  if (localData.value.heuresServiceDebut && localData.value.heuresServiceFin) {
    if (localData.value.heuresServiceDebut >= localData.value.heuresServiceFin) {
      newErrors.heuresServiceFin = 'L\'heure de fermeture doit être après l\'ouverture'
    }
  }
  
  if (selectedDays.value.length === 0) {
    newErrors.joursService = 'Sélectionnez au moins un jour de service'
  }
  
  updateErrors(newErrors)
}

const validateCoverageArea = () => {
  const newErrors = {}
  
  if (!localData.value.zoneCouverture || localData.value.zoneCouverture.trim().length === 0) {
    newErrors.zoneCouverture = 'La zone de couverture est obligatoire'
  }
  
  updateErrors(newErrors)
}

const validateServiceConfig = () => {
  const newErrors = {}
  
  if (!localData.value.commandeMinimum || localData.value.commandeMinimum <= 0) {
    newErrors.commandeMinimum = 'Le montant minimum doit être supérieur à 0'
  } else if (localData.value.commandeMinimum > 500) {
    newErrors.commandeMinimum = 'Le montant minimum ne peut pas dépasser 500€'
  }
  
  if (localData.value.tempsLivraisonMoyen && 
      (localData.value.tempsLivraisonMoyen < 5 || localData.value.tempsLivraisonMoyen > 120)) {
    newErrors.tempsLivraisonMoyen = 'Le temps de livraison doit être entre 5 et 120 minutes'
  }
  
  updateErrors(newErrors)
}

const updateErrors = (newErrors) => {
  const keysToRemove = [
    'heuresServiceDebut', 'heuresServiceFin', 'joursService',
    'zoneCouverture', 'commandeMinimum', 'tempsLivraisonMoyen'
  ]
  
  keysToRemove.forEach(key => {
    if (errors.value[key]) {
      delete errors.value[key]
    }
  })
  
  errors.value = { ...errors.value, ...newErrors }
  
  emit('validate', errors.value)
}

const updateJoursService = () => {
  localData.value.joursService = JSON.stringify(selectedDays.value)
  validateServiceHours()
}

const handleContinuousAvailabilityToggle = () => {
  if (localData.value.gestionDisponibiliteContinue) {
    generateDefaultTimeSlots()
  }
}

const generateDefaultTimeSlots = () => {
  if (!localData.value.heuresServiceDebut || !localData.value.heuresServiceFin) {
    return
  }
  
  const startHour = parseInt(localData.value.heuresServiceDebut.split(':')[0])
  const endHour = parseInt(localData.value.heuresServiceFin.split(':')[0])
  
  const defaultSlots = []
  for (let hour = startHour; hour < endHour; hour += 2) {
    const debut = `${hour.toString().padStart(2, '0')}:00`
    const fin = `${(hour + 2).toString().padStart(2, '0')}:00`
    
    defaultSlots.push({
      debut,
      fin,
      capacite: 5
    })
  }
  
  timeSlots.value = defaultSlots
  updateCreneauxDisponibles()
}

const addTimeSlot = () => {
  if (!canAddSlot.value) return
  
  const isOverlapping = timeSlots.value.some(slot => {
    return (newSlot.value.debut < slot.fin && newSlot.value.fin > slot.debut)
  })
  
  if (isOverlapping) {
    alert('Ce créneau chevauche avec un créneau existant')
    return
  }
  
  timeSlots.value.push({ ...newSlot.value })
  timeSlots.value.sort((a, b) => a.debut.localeCompare(b.debut))
  
  newSlot.value = {
    debut: '',
    fin: '',
    capacite: 5
  }
  
  updateCreneauxDisponibles()
}

const removeTimeSlot = (index) => {
  timeSlots.value.splice(index, 1)
  updateCreneauxDisponibles()
}

const updateCreneauxDisponibles = () => {
  localData.value.creneauxDisponibles = JSON.stringify(timeSlots.value)
}

const initializeData = () => {
  try {
    const joursData = JSON.parse(localData.value.joursService || '[]')
    selectedDays.value = joursData
  } catch (e) {
    selectedDays.value = []
  }
  
  try {
    const creneauxData = JSON.parse(localData.value.creneauxDisponibles || '[]')
    timeSlots.value = creneauxData
  } catch (e) {
    timeSlots.value = []
  }
}

watch(localData, (newData) => {
  emit('update:modelValue', { ...props.modelValue, ...newData })
}, { deep: true })

watch(allCompleted, (completed) => {
  emit('update-step', 'categorySpecific', completed)
})

onMounted(() => {
  initializeData()
  
  validateServiceHours()
  validateCoverageArea()
  validateServiceConfig()
})
</script>

<style scoped>
.form-section {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  overflow: hidden;
  transition: all 0.3s ease;
}

.form-section.completed {
  border-color: #4CAF50;
  background: linear-gradient(to right, #f8fff8, white);
}

.section-header {
  background: #f8f9fa;
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e9ecef;
}

.form-section.completed .section-header {
  background: linear-gradient(135deg, #e8f5e8, #f0f9f0);
}

.section-icon {
  margin-right: 1rem;
}

.section-icon i {
  font-size: 1.2rem;
  color: #4CAF50;
}

.section-title {
  flex: 1;
}

.section-title h3 {
  margin: 0 0 0.25rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.section-title p {
  margin: 0;
  color: #6c757d;
  font-size: 0.85rem;
}

.section-status i {
  color: #4CAF50;
  font-size: 1.1rem;
}

.section-content {
  padding: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9rem;
}

.help-text {
  display: block;
  font-weight: 400;
  color: #6c757d;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: border-color 0.3s ease;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
}

.form-input.error,
.form-textarea.error {
  border-color: #dc3545;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.input-group .form-input {
  padding-right: 3rem;
}

.input-addon {
  position: absolute;
  right: 1rem;
  color: #6c757d;
  font-size: 0.9rem;
  pointer-events: none;
}

.field-error {
  color: #dc3545;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.days-selector {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.day-checkbox {
  position: relative;
  cursor: pointer;
}

.day-checkbox input {
  display: none;
}

.day-checkbox .day-label {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 2px solid #dee2e6;
  border-radius: 50%;
  font-weight: 600;
  color: #6c757d;
  transition: all 0.3s ease;
}

.day-checkbox .day-full {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  font-size: 0.7rem;
  color: #6c757d;
  margin-top: 0.25rem;
}

.day-checkbox.selected .day-label {
  background: #4CAF50;
  border-color: #4CAF50;
  color: white;
}

.day-checkbox.selected .day-full {
  color: #4CAF50;
}

.checkbox-label {
  display: flex;
  align-items: flex-start;
  cursor: pointer;
  margin-bottom: 1rem;
}

.checkbox-label input[type="checkbox"] {
  display: none;
}

.checkbox-custom {
  width: 20px;
  height: 20px;
  border: 2px solid #ced4da;
  border-radius: 4px;
  margin-right: 0.75rem;
  position: relative;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-top: 2px;
}

.checkbox-label input[type="checkbox"]:checked + .checkbox-custom {
  background: #4CAF50;
  border-color: #4CAF50;
}

.checkbox-label input[type="checkbox"]:checked + .checkbox-custom::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.checkbox-text {
  flex: 1;
  font-size: 0.9rem;
  color: #2c3e50;
}

.time-slots-manager {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
}

.slots-list {
  margin-bottom: 1rem;
}

.time-slot-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 0.75rem;
  margin-bottom: 0.5rem;
}

.slot-time {
  font-weight: 600;
  color: #2c3e50;
}

.slot-capacity {
  color: #6c757d;
  font-size: 0.9rem;
}

.slot-remove {
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  width: 28px;
  height: 28px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s ease;
}

.slot-remove:hover {
  background: #c82333;
}

.no-slots {
  text-align: center;
  color: #6c757d;
  padding: 2rem;
}

.no-slots i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.add-slot-form .form-row {
  grid-template-columns: 1fr 1fr 1fr auto;
  align-items: end;
}

.btn-add-slot {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: background 0.3s ease;
}

.btn-add-slot:hover:not(:disabled) {
  background: #45a049;
}

.btn-add-slot:disabled {
  background: #dee2e6;
  color: #6c757d;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }
  
  .add-slot-form .form-row {
    grid-template-columns: 1fr;
  }
  
  .section-content {
    padding: 1rem;
  }
  
  .section-header {
    padding: 1rem;
  }
  
  .days-selector {
    justify-content: center;
  }
}
</style>