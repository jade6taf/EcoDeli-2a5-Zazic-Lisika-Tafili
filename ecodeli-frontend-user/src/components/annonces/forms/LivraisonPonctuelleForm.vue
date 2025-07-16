<template>
  <div class="livraison-ponctuelle-form">
    <!-- Date and Time Section -->
    <div class="form-section" :class="{ 'completed': dateTimeCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-calendar"></i>
        </div>
        <div class="section-title">
          <h3>Date et heure de livraison</h3>
          <p>Définissez quand la livraison doit avoir lieu</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="dateTimeCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="form-row">
          <div class="form-group">
            <label for="dateLivraisonPrecise" class="form-label">
              Date de livraison *
              <span class="help-text">Sélectionnez la date souhaitée</span>
            </label>
            <input
              type="date"
              id="dateLivraisonPrecise"
              v-model="localData.dateLivraisonPrecise"
              class="form-input"
              :class="{ 'error': errors.dateLivraisonPrecise }"
              :min="minDate"
              @change="validateDateTime"
            />
            <div class="field-error" v-if="errors.dateLivraisonPrecise">
              {{ errors.dateLivraisonPrecise }}
            </div>
          </div>

          <div class="form-group">
            <label for="heureLivraisonPrecise" class="form-label">
              Heure de livraison *
              <span class="help-text">Heure souhaitée</span>
            </label>
            <input
              type="time"
              id="heureLivraisonPrecise"
              v-model="localData.heureLivraisonPrecise"
              class="form-input"
              :class="{ 'error': errors.heureLivraisonPrecise }"
              @change="validateDateTime"
            />
            <div class="field-error" v-if="errors.heureLivraisonPrecise">
              {{ errors.heureLivraisonPrecise }}
            </div>
          </div>
        </div>

        <div class="form-group">
          <label class="checkbox-label">
            <input
              type="checkbox"
              v-model="localData.disponibiliteTempsReel"
              @change="handleRealTimeToggle"
            />
            <span class="checkbox-custom"></span>
            <span class="checkbox-text">
              Vérifier la disponibilité en temps réel
              <span class="help-text">Vérifie automatiquement si le créneau est disponible</span>
            </span>
          </label>
        </div>

        <div class="availability-check" v-if="localData.disponibiliteTempsReel && availabilityStatus">
          <div class="availability-result" :class="availabilityStatus.available ? 'available' : 'unavailable'">
            <i :class="availabilityStatus.available ? 'pi pi-check-circle' : 'pi pi-times-circle'"></i>
            <span>{{ availabilityStatus.message }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Addresses Section -->
    <div class="form-section" :class="{ 'completed': addressesCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-map-marker"></i>
        </div>
        <div class="section-title">
          <h3>Adresses de collecte et destination</h3>
          <p>Où récupérer et livrer le colis</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="addressesCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="form-group">
          <label for="adresseCollecte" class="form-label">
            Adresse de collecte *
            <span class="help-text">Où récupérer le colis</span>
          </label>
          <textarea
            id="adresseCollecte"
            v-model="localData.adresseCollecte"
            class="form-textarea"
            :class="{ 'error': errors.adresseCollecte }"
            placeholder="123 Rue Example, 75001 Paris"
            rows="3"
            @blur="validateAddresses"
          ></textarea>
          <div class="field-error" v-if="errors.adresseCollecte">
            {{ errors.adresseCollecte }}
          </div>
        </div>

        <div class="form-group">
          <label for="adresseDestination" class="form-label">
            Adresse de destination *
            <span class="help-text">Où livrer le colis</span>
          </label>
          <textarea
            id="adresseDestination"
            v-model="localData.adresseDestination"
            class="form-textarea"
            :class="{ 'error': errors.adresseDestination }"
            placeholder="456 Avenue Destination, 75002 Paris"
            rows="3"
            @blur="validateAddresses"
          ></textarea>
          <div class="field-error" v-if="errors.adresseDestination">
            {{ errors.adresseDestination }}
          </div>
        </div>
      </div>
    </div>

    <!-- Package Details Section -->
    <div class="form-section" :class="{ 'completed': packageDetailsCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-box"></i>
        </div>
        <div class="section-title">
          <h3>Détails du colis</h3>
          <p>Informations sur le colis à livrer</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="packageDetailsCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="form-row">
          <div class="form-group">
            <label for="poidsColis" class="form-label">
              Poids (kg)
              <span class="help-text">Poids approximatif</span>
            </label>
            <input
              type="number"
              id="poidsColis"
              v-model.number="localData.poidsColis"
              class="form-input"
              :class="{ 'error': errors.poidsColis }"
              placeholder="0.5"
              min="0"
              max="1000"
              step="0.1"
              @blur="validatePackageDetails"
            />
            <div class="field-error" v-if="errors.poidsColis">
              {{ errors.poidsColis }}
            </div>
          </div>

          <div class="form-group">
            <label for="dimensionsColis" class="form-label">
              Dimensions
              <span class="help-text">L x l x h (cm)</span>
            </label>
            <input
              type="text"
              id="dimensionsColis"
              v-model="localData.dimensionsColis"
              class="form-input"
              :class="{ 'error': errors.dimensionsColis }"
              placeholder="30 x 20 x 10"
              @blur="validatePackageDetails"
            />
            <div class="field-error" v-if="errors.dimensionsColis">
              {{ errors.dimensionsColis }}
            </div>
          </div>
        </div>

        <div class="form-group">
          <label for="instructionsLivraison" class="form-label">
            Instructions spéciales de livraison
            <span class="help-text">Consignes particulières pour le livreur</span>
          </label>
          <textarea
            id="instructionsLivraison"
            v-model="localData.instructionsLivraison"
            class="form-textarea"
            placeholder="Fragile, à manipuler avec précaution. Sonner à l'interphone..."
            rows="3"
            @blur="validatePackageDetails"
          ></textarea>
        </div>
      </div>
    </div>

    <!-- Contact Information Section -->
    <div class="form-section" :class="{ 'completed': contactCompleted }">
      <div class="section-header">
        <div class="section-icon">
          <i class="pi pi-phone"></i>
        </div>
        <div class="section-title">
          <h3>Contact destinataire</h3>
          <p>Informations pour contacter le destinataire</p>
        </div>
        <div class="section-status">
          <i class="pi pi-check" v-if="contactCompleted"></i>
        </div>
      </div>
      
      <div class="section-content">
        <div class="form-group">
          <label for="contactDestinataire" class="form-label">
            Contact destinataire *
            <span class="help-text">Téléphone ou email du destinataire</span>
          </label>
          <input
            type="text"
            id="contactDestinataire"
            v-model="localData.contactDestinataire"
            class="form-input"
            :class="{ 'error': errors.contactDestinataire }"
            placeholder="06 12 34 56 78 ou email@example.com"
            @blur="validateContact"
          />
          <div class="field-error" v-if="errors.contactDestinataire">
            {{ errors.contactDestinataire }}
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
  dateLivraisonPrecise: '',
  heureLivraisonPrecise: '',
  adresseCollecte: '',
  adresseDestination: '',
  poidsColis: null,
  dimensionsColis: '',
  instructionsLivraison: '',
  contactDestinataire: '',
  disponibiliteTempsReel: false,
  ...props.modelValue
})

const availabilityStatus = ref(null)
const errors = ref({})

const minDate = computed(() => {
  const today = new Date()
  return today.toISOString().split('T')[0]
})

const dateTimeCompleted = computed(() => {
  return localData.value.dateLivraisonPrecise && localData.value.heureLivraisonPrecise
})

const addressesCompleted = computed(() => {
  return localData.value.adresseCollecte && localData.value.adresseDestination
})

const packageDetailsCompleted = computed(() => {
  return localData.value.poidsColis || localData.value.dimensionsColis
})

const contactCompleted = computed(() => {
  return localData.value.contactDestinataire
})

const allCompleted = computed(() => {
  return dateTimeCompleted.value && 
         addressesCompleted.value && 
         contactCompleted.value &&
         Object.keys(errors.value).length === 0
})

const validateDateTime = () => {
  const newErrors = {}
  
  if (!localData.value.dateLivraisonPrecise) {
    newErrors.dateLivraisonPrecise = 'La date de livraison est obligatoire'
  } else {
    const selectedDate = new Date(localData.value.dateLivraisonPrecise)
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    
    if (selectedDate < today) {
      newErrors.dateLivraisonPrecise = 'La date ne peut pas être dans le passé'
    }
  }
  
  if (!localData.value.heureLivraisonPrecise) {
    newErrors.heureLivraisonPrecise = 'L\'heure de livraison est obligatoire'
  } else {
    const [hours, minutes] = localData.value.heureLivraisonPrecise.split(':')
    const selectedDateTime = new Date(localData.value.dateLivraisonPrecise)
    selectedDateTime.setHours(parseInt(hours), parseInt(minutes))
    
    if (selectedDateTime < new Date()) {
      newErrors.heureLivraisonPrecise = 'L\'heure ne peut pas être dans le passé'
    }
  }
  
  updateErrors(newErrors)
  
  if (localData.value.disponibiliteTempsReel && dateTimeCompleted.value) {
    checkAvailability()
  }
}

const validateAddresses = () => {
  const newErrors = {}
  
  if (!localData.value.adresseCollecte || localData.value.adresseCollecte.trim().length === 0) {
    newErrors.adresseCollecte = 'L\'adresse de collecte est obligatoire'
  }
  
  if (!localData.value.adresseDestination || localData.value.adresseDestination.trim().length === 0) {
    newErrors.adresseDestination = 'L\'adresse de destination est obligatoire'
  }
  
  updateErrors(newErrors)
}

const validatePackageDetails = () => {
  const newErrors = {}
  
  if (localData.value.poidsColis && (localData.value.poidsColis < 0 || localData.value.poidsColis > 1000)) {
    newErrors.poidsColis = 'Le poids doit être entre 0 et 1000 kg'
  }
  
  if (localData.value.dimensionsColis && !/^\d+\s*x\s*\d+\s*x\s*\d+$/.test(localData.value.dimensionsColis)) {
    newErrors.dimensionsColis = 'Format: L x l x h (ex: 30 x 20 x 10)'
  }
  
  updateErrors(newErrors)
}

const validateContact = () => {
  const newErrors = {}
  
  if (!localData.value.contactDestinataire || localData.value.contactDestinataire.trim().length === 0) {
    newErrors.contactDestinataire = 'Le contact destinataire est obligatoire'
  } else {
    const contact = localData.value.contactDestinataire.trim()
    const phoneRegex = /^(\+33|0)[1-9](\d{8})$/
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    
    if (!phoneRegex.test(contact.replace(/\s/g, '')) && !emailRegex.test(contact)) {
      newErrors.contactDestinataire = 'Veuillez saisir un téléphone ou email valide'
    }
  }
  
  updateErrors(newErrors)
}

const updateErrors = (newErrors) => {
  const keysToRemove = [
    'dateLivraisonPrecise', 'heureLivraisonPrecise', 
    'adresseCollecte', 'adresseDestination',
    'poidsColis', 'dimensionsColis',
    'contactDestinataire'
  ]
  
  keysToRemove.forEach(key => {
    if (errors.value[key]) {
      delete errors.value[key]
    }
  })
  
  errors.value = { ...errors.value, ...newErrors }
  
  emit('validate', errors.value)
}

const handleRealTimeToggle = () => {
  if (localData.value.disponibiliteTempsReel && dateTimeCompleted.value) {
    checkAvailability()
  } else {
    availabilityStatus.value = null
  }
}

const checkAvailability = async () => {
  try {
    const requestData = {
      dateLivraisonPrecise: localData.value.dateLivraisonPrecise,
      heureLivraisonPrecise: localData.value.heureLivraisonPrecise,
      adresseCollecte: localData.value.adresseCollecte,
      adresseDestination: localData.value.adresseDestination
    }
    
    const result = await annonceStore.checkLivraisonPonctuelleAvailability(requestData)
    
    if (result.success) {
      availabilityStatus.value = result.data
    }
  } catch (error) {
    console.error('Erreur lors de la vérification de disponibilité:', error)
    availabilityStatus.value = {
      available: false,
      message: 'Erreur lors de la vérification'
    }
  }
}

watch(localData, (newData) => {
  emit('update:modelValue', { ...props.modelValue, ...newData })
}, { deep: true })

watch(allCompleted, (completed) => {
  emit('update-step', 'categorySpecific', completed)
})

onMounted(() => {
  validateDateTime()
  validateAddresses()
  validatePackageDetails()
  validateContact()
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

.field-error {
  color: #dc3545;
  font-size: 0.8rem;
  margin-top: 0.25rem;
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

.availability-check {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 6px;
  background: #f8f9fa;
}

.availability-result {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
}

.availability-result.available {
  color: #28a745;
}

.availability-result.unavailable {
  color: #dc3545;
}

.availability-result i {
  font-size: 1.1rem;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }
  
  .section-content {
    padding: 1rem;
  }
  
  .section-header {
    padding: 1rem;
  }
}
</style>