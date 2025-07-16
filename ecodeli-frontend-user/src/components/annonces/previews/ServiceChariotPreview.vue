<template>
  <div class="service-chariot-preview">
    <div class="preview-header">
      <div class="preview-category">
        <i class="pi pi-shopping-cart"></i>
        <span>Service "Lâcher de chariot"</span>
      </div>
      <div class="preview-status">
        <span class="status-badge">Aperçu</span>
      </div>
    </div>

    <div class="preview-content">
      <h2 class="preview-title">{{ annonceData.titre }}</h2>
      <p class="preview-description">{{ annonceData.description }}</p>

      <div class="preview-sections">
        <!-- Section Heures de service -->
        <div class="preview-section">
          <div class="section-header">
            <i class="pi pi-clock"></i>
            <h3>Heures de service</h3>
          </div>
          <div class="section-content">
            <div class="service-hours">
              <div class="hours-display">
                <span class="hour-badge">{{ annonceData.heuresServiceDebut }}</span>
                <i class="pi pi-arrow-right"></i>
                <span class="hour-badge">{{ annonceData.heuresServiceFin }}</span>
              </div>
              <div class="service-days">
                <span class="days-label">Jours de service :</span>
                <div class="days-grid">
                  <span 
                    v-for="(day, index) in daysOfWeek" 
                    :key="index"
                    class="day-badge"
                    :class="{ 'active': isServiceDay(index) }"
                  >
                    {{ day.short }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Section Zone de couverture -->
        <div class="preview-section">
          <div class="section-header">
            <i class="pi pi-map"></i>
            <h3>Zone de couverture</h3>
          </div>
          <div class="section-content">
            <div class="coverage-area">
              <p class="coverage-text">{{ annonceData.zoneCouverture }}</p>
            </div>
          </div>
        </div>

        <!-- Section Configuration -->
        <div class="preview-section">
          <div class="section-header">
            <i class="pi pi-cog"></i>
            <h3>Configuration du service</h3>
          </div>
          <div class="section-content">
            <div class="config-grid">
              <div class="config-item">
                <div class="config-label">
                  <i class="pi pi-euro"></i>
                  <span>Commande minimum</span>
                </div>
                <div class="config-value">{{ annonceData.commandeMinimum }}€</div>
              </div>
              <div class="config-item" v-if="annonceData.tempsLivraisonMoyen">
                <div class="config-label">
                  <i class="pi pi-clock"></i>
                  <span>Temps de livraison</span>
                </div>
                <div class="config-value">{{ annonceData.tempsLivraisonMoyen }} min</div>
              </div>
              <div class="config-item" v-if="annonceData.gestionDisponibiliteContinue">
                <div class="config-label">
                  <i class="pi pi-refresh"></i>
                  <span>Disponibilité</span>
                </div>
                <div class="config-value badge-success">Continue</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Section Créneaux -->
        <div class="preview-section" v-if="timeSlots && timeSlots.length > 0">
          <div class="section-header">
            <i class="pi pi-calendar-plus"></i>
            <h3>Créneaux de livraison</h3>
          </div>
          <div class="section-content">
            <div class="time-slots-display">
              <div 
                v-for="(slot, index) in timeSlots" 
                :key="index"
                class="time-slot"
              >
                <div class="slot-time">
                  <i class="pi pi-clock"></i>
                  <span>{{ slot.debut }} - {{ slot.fin }}</span>
                </div>
                <div class="slot-capacity">
                  <i class="pi pi-users"></i>
                  <span>{{ slot.capacite }} commandes max</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Section Avantages -->
        <div class="preview-section">
          <div class="section-header">
            <i class="pi pi-check-circle"></i>
            <h3>Avantages du service</h3>
          </div>
          <div class="section-content">
            <div class="advantages-grid">
              <div class="advantage-item">
                <i class="pi pi-home"></i>
                <span>Livraison à domicile</span>
              </div>
              <div class="advantage-item">
                <i class="pi pi-shield"></i>
                <span>Service fiable</span>
              </div>
              <div class="advantage-item">
                <i class="pi pi-heart"></i>
                <span>Service personnalisé</span>
              </div>
              <div class="advantage-item">
                <i class="pi pi-leaf"></i>
                <span>Éco-responsable</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  annonceData: {
    type: Object,
    required: true
  },
  category: {
    type: String,
    required: true
  }
})

const daysOfWeek = [
  { short: 'L', full: 'Lundi' },
  { short: 'M', full: 'Mardi' },
  { short: 'M', full: 'Mercredi' },
  { short: 'J', full: 'Jeudi' },
  { short: 'V', full: 'Vendredi' },
  { short: 'S', full: 'Samedi' },
  { short: 'D', full: 'Dimanche' }
]

const serviceDays = computed(() => {
  try {
    return JSON.parse(props.annonceData.joursService || '[]')
  } catch (e) {
    return []
  }
})

const timeSlots = computed(() => {
  try {
    return JSON.parse(props.annonceData.creneauxDisponibles || '[]')
  } catch (e) {
    return []
  }
})

const isServiceDay = (dayIndex) => {
  return serviceDays.value.includes(dayIndex)
}
</script>

<style scoped>
.service-chariot-preview {
  font-family: Arial, sans-serif;
  max-width: 800px;
  margin: 0 auto;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e9ecef;
}

.preview-category {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.1rem;
  font-weight: 600;
  color: #4CAF50;
}

.preview-category i {
  font-size: 1.3rem;
}

.status-badge {
  background: #e8f5e8;
  color: #2e7d32;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
}

.preview-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.preview-description {
  font-size: 1.1rem;
  color: #6c757d;
  margin-bottom: 2rem;
  line-height: 1.6;
}

.preview-sections {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.preview-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 1.5rem;
  border-left: 4px solid #4CAF50;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.section-header i {
  color: #4CAF50;
  font-size: 1.2rem;
}

.section-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.section-content {
  margin-left: 2rem;
}

.service-hours {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.hours-display {
  display: flex;
  align-items: center;
  gap: 1rem;
  justify-content: center;
}

.hour-badge {
  background: white;
  color: #2c3e50;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  border: 2px solid #4CAF50;
}

.hours-display i {
  color: #4CAF50;
  font-size: 1.2rem;
}

.service-days {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
}

.days-label {
  font-weight: 600;
  color: #495057;
}

.days-grid {
  display: flex;
  gap: 0.5rem;
}

.day-badge {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  color: #6c757d;
  font-weight: 600;
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
}

.day-badge.active {
  background: #4CAF50;
  color: white;
  border-color: #4CAF50;
}

.coverage-area {
  background: white;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.coverage-text {
  margin: 0;
  color: #2c3e50;
  line-height: 1.6;
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.config-item {
  background: white;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.config-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #495057;
  font-size: 0.9rem;
}

.config-label i {
  color: #4CAF50;
}

.config-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: #2c3e50;
}

.badge-success {
  background: #d4edda;
  color: #155724;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.time-slots-display {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.time-slot {
  background: white;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.slot-time {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.slot-time i {
  color: #4CAF50;
}

.slot-capacity {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6c757d;
  font-size: 0.9rem;
}

.slot-capacity i {
  color: #4CAF50;
}

.advantages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
}

.advantage-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: white;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  color: #2c3e50;
  font-weight: 500;
}

.advantage-item i {
  color: #4CAF50;
  font-size: 1.2rem;
}

@media (max-width: 768px) {
  .preview-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .section-content {
    margin-left: 0;
  }
  
  .config-grid {
    grid-template-columns: 1fr;
  }
  
  .time-slots-display {
    grid-template-columns: 1fr;
  }
  
  .advantages-grid {
    grid-template-columns: 1fr;
  }
  
  .hours-display {
    flex-direction: column;
    gap: 1rem;
  }
  
  .service-days {
    align-items: stretch;
  }
  
  .days-grid {
    justify-content: center;
  }
}
</style>