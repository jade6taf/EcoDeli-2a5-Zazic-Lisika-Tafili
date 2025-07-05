<script>
import PlanningCalendar from '@/components/planning/PlanningCalendar.vue'
import planningService from '@/services/planningService'

export default {
  name: 'PlanningView',
  components: {
    PlanningCalendar
  },
  data() {
    return {
      stats: null,
      prochainsCreneaux: [],
      notification: null,
      isRefreshing: false,
      isLoading: true
    }
  },
  async mounted() {
    await this.loadData()
    if (this.$route.query.action === 'add') {
      this.$nextTick(() => {
        this.ajouterDisponibilite()
      })
    }
  },
  methods: {
    async loadData() {
      this.isLoading = true
      try {
        const [stats, creneaux] = await Promise.all([
          planningService.getStatistiques(),
          planningService.getProchainsCreneaux(5)
        ])
        console.log('Stats reçues:', stats)
        console.log('Créneaux reçus:', creneaux)
        this.stats = stats
        this.prochainsCreneaux = creneaux
      } catch (error) {
        console.error('Erreur lors du chargement des données:', error)
        this.showError('Erreur lors du chargement des données du planning')
      } finally {
        this.isLoading = false
      }
    },

    async refreshStats() {
      try {
        const [stats, creneaux] = await Promise.all([
          planningService.getStatistiques(),
          planningService.getProchainsCreneaux(5)
        ])

        this.stats = stats
        this.prochainsCreneaux = creneaux
      } catch (error) {
        console.error('Erreur lors du rechargement des statistiques:', error)
      }
    },

    async refreshPlanning() {
      if (this.isRefreshing) return

      this.isRefreshing = true
      try {
        if (this.$refs.planningCalendar) {
          this.$refs.planningCalendar.refreshEvents()
        }
        await this.refreshStats()
        this.showSuccess('Planning actualisé')
      } catch (error) {
        console.error('Erreur lors de l\'actualisation:', error)
        this.showError('Erreur lors de l\'actualisation du planning')
      } finally {
        this.isRefreshing = false
      }
    },

    ajouterDisponibilite() {
      if (this.$refs.planningCalendar) {
        this.$refs.planningCalendar.openAddModal()
      }
    },

    marquerIndisponible() {
      if (this.$refs.planningCalendar) {
        this.$refs.planningCalendar.openAddModal({
          type: 'OCCUPE',
          titre: 'Indisponible'
        })
      }
    },

    showSuccess(message) {
      this.showNotification(message, 'success')
    },

    showError(message) {
      this.showNotification(message, 'error')
    },

    showNotification(message, type) {
      this.notification = { message, type }
      setTimeout(() => {
        this.notification = null
      }, 4000)
    },

    getNotificationIcon(type) {
      const icons = {
        'success': 'fas fa-check-circle',
        'error': 'fas fa-exclamation-circle',
        'warning': 'fas fa-exclamation-triangle',
        'info': 'fas fa-info-circle'
      }
      return icons[type] || 'fas fa-info-circle'
    },

    formatDate(dateString) {
      if (!dateString) return 'Date non définie'
      try {
        const date = new Date(dateString)
        if (isNaN(date.getTime())) return 'Date invalide'
        return date.toLocaleDateString('fr-FR', {
          day: 'numeric',
          month: 'short',
          year: 'numeric'
        })
      } catch (error) {
        console.error('Erreur de formatage de date:', error, dateString)
        return 'Date invalide'
      }
    },

    formatTime(dateString) {
      if (!dateString) return 'Heure non définie'
      try {
        const date = new Date(dateString)
        if (isNaN(date.getTime())) return 'Heure invalide'
        return date.toLocaleTimeString('fr-FR', {
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (error) {
        console.error('Erreur de formatage d\'heure:', error, dateString)
        return 'Heure invalide'
      }
    },

    getTypeLabel(type) {
      return planningService.utils.getTypeLabel(type)
    }
  }
}
</script>

<template>
  <div class="planning-view">
    <div class="planning-header">
      <div class="header-content">
        <h1>
          <i class="fas fa-calendar-alt"></i>
          Mon Planning
        </h1>
        <p>Gérez vos disponibilités et créneaux de travail</p>
      </div>

      <div class="header-stats" v-if="stats">
        <div class="stat-card">
          <i class="fas fa-clock"></i>
          <div>
            <span class="stat-number">{{ stats.heuresDisponibles || 0 }}h</span>
            <span class="stat-label">Cette semaine</span>
          </div>
        </div>
        <div class="stat-card">
          <i class="fas fa-percentage"></i>
          <div>
            <span class="stat-number">{{ Math.round(stats.tauxOccupation || 0) }}%</span>
            <span class="stat-label">Taux d'occupation</span>
          </div>
        </div>
      </div>
    </div>

    <div class="planning-content">
      <div class="calendar-sidebar">
        <div class="sidebar-section">
          <h4>Actions rapides</h4>
          <button @click="ajouterDisponibilite" class="sidebar-btn primary">
            <i class="fas fa-plus"></i>
            Ajouter un créneau
          </button>
          <button @click="marquerIndisponible" class="sidebar-btn secondary">
            <i class="fas fa-times"></i>
            Marquer indisponible
          </button>
          <button @click="refreshPlanning" class="sidebar-btn info" :disabled="isRefreshing">
            <i class="fas fa-sync-alt" :class="{ 'fa-spin': isRefreshing }"></i>
            Actualiser
          </button>
        </div>

        <div class="sidebar-section">
          <h4>Légende</h4>
          <div class="legend-item">
            <span class="legend-color disponible"></span>
            <span>Disponible</span>
          </div>
          <div class="legend-item">
            <span class="legend-color occupe"></span>
            <span>Occupé</span>
          </div>
          <div class="legend-item">
            <span class="legend-color pause"></span>
            <span>Pause</span>
          </div>
        </div>

        <div class="sidebar-section" v-if="prochainsCreneaux.length > 0">
          <h4>Prochains créneaux</h4>
          <div class="upcoming-slots">
            <div v-for="creneau in prochainsCreneaux" :key="creneau.id" class="upcoming-slot">
              <div class="slot-date">{{ formatDate(creneau.dateDebut) }}</div>
              <div class="slot-time">
                {{ formatTime(creneau.dateDebut) }} - {{ formatTime(creneau.dateFin) }}
              </div>
              <div class="slot-type" :class="creneau.type.toLowerCase()">
                {{ getTypeLabel(creneau.type) }}
              </div>
            </div>
          </div>
        </div>

        <div class="sidebar-section" v-else>
          <h4>Prochains créneaux</h4>
          <div class="no-upcoming">
            <i class="fas fa-calendar-plus"></i>
            <p>Aucun créneau planifié</p>
            <button @click="ajouterDisponibilite" class="btn-link">
              Ajouter un créneau
            </button>
          </div>
        </div>
      </div>

      <div class="calendar-main">
        <PlanningCalendar
          ref="planningCalendar"
          @success="showSuccess"
          @error="showError"
          @event-created="refreshStats"
          @event-updated="refreshStats"
          @event-deleted="refreshStats"
        />
      </div>
    </div>

    <!-- Notifications -->
    <transition name="notification">
      <div v-if="notification" class="notification" :class="notification.type">
        <i :class="getNotificationIcon(notification.type)"></i>
        {{ notification.message }}
      </div>
    </transition>
  </div>
</template>

<style scoped>
.planning-view {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  min-height: calc(100vh - 120px);
}

.planning-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--border-color);
}

.header-content h1 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 2rem;
}

.header-content h1 i {
  color: var(--primary-color);
}

.header-content p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 1.1rem;
}

.header-stats {
  display: flex;
  gap: 1rem;
}

.stat-card {
  background: var(--card-bg);
  padding: 1.25rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
  display: flex;
  align-items: center;
  gap: 1rem;
  min-width: 140px;
}

.stat-card i {
  font-size: 1.75rem;
  color: var(--primary-color);
}

.stat-number {
  display: block;
  font-size: 1.75rem;
  font-weight: bold;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

.planning-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 2rem;
  height: calc(100vh - 200px);
  min-height: 600px;
}

.calendar-sidebar {
  background: var(--card-bg);
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: var(--shadow);
  height: fit-content;
  max-height: 100%;
  overflow-y: auto;
}

.sidebar-section {
  margin-bottom: 2rem;
}

.sidebar-section:last-child {
  margin-bottom: 0;
}

.sidebar-section h4 {
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  font-size: 1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.sidebar-btn {
  width: 100%;
  padding: 0.875rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  transition: all 0.2s;
  text-align: left;
}

.sidebar-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.sidebar-btn.primary {
  background: var(--primary-color);
  color: white;
}

.sidebar-btn.primary:hover:not(:disabled) {
  background: var(--primary-color-dark);
  transform: translateY(-1px);
}

.sidebar-btn.secondary {
  background: var(--error-color);
  color: white;
}

.sidebar-btn.secondary:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.sidebar-btn.info {
  background: var(--info-color);
  color: white;
}

.sidebar-btn.info:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
}

.legend-color {
  width: 18px;
  height: 18px;
  border-radius: 4px;
  flex-shrink: 0;
}

.legend-color.disponible {
  background: #22c55e;
}

.legend-color.occupe {
  background: #ef4444;
}

.legend-color.pause {
  background: #f59e0b;
}

.upcoming-slots {
  max-height: 320px;
  overflow-y: auto;
}

.upcoming-slot {
  padding: 1rem;
  background: var(--background-color);
  border-radius: 6px;
  margin-bottom: 0.75rem;
  border-left: 4px solid var(--primary-color);
}

.slot-date {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.95rem;
  margin-bottom: 0.25rem;
}

.slot-time {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.slot-type {
  font-size: 0.85rem;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  display: inline-block;
  font-weight: 500;
}

.slot-type.disponible {
  background: #22c55e20;
  color: #22c55e;
}

.slot-type.occupe {
  background: #ef444420;
  color: #ef4444;
}

.slot-type.pause {
  background: #f59e0b20;
  color: #f59e0b;
}

.no-upcoming {
  text-align: center;
  padding: 1.5rem 0;
  color: var(--text-secondary);
}

.no-upcoming i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  opacity: 0.5;
}

.no-upcoming p {
  margin: 0.5rem 0;
  font-size: 0.9rem;
}

.btn-link {
  background: none;
  border: none;
  color: var(--primary-color);
  cursor: pointer;
  text-decoration: underline;
  font-size: 0.9rem;
}

.btn-link:hover {
  opacity: 0.8;
}

.calendar-main {
  background: var(--card-bg);
  border-radius: 8px;
  box-shadow: var(--shadow);
  overflow: hidden;
  height: 100%;
}

.notification {
  position: fixed;
  top: 2rem;
  right: 2rem;
  padding: 1rem 1.5rem;
  border-radius: 6px;
  color: white;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
  box-shadow: var(--shadow-hover);
}

.notification.success {
  background: var(--success-color);
}

.notification.error {
  background: var(--error-color);
}

.notification.warning {
  background: var(--warning-color);
}

.notification.info {
  background: var(--info-color);
}

.notification-enter-active,
.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.notification-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* Responsive */
@media (max-width: 1024px) {
  .planning-content {
    grid-template-columns: 1fr;
    height: auto;
  }
  
  .calendar-sidebar {
    order: 2;
    height: auto;
    max-height: none;
  }
  
  .calendar-main {
    height: 600px;
  }
}

@media (max-width: 768px) {
  .planning-view {
    padding: 1rem;
  }
  
  .planning-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .header-stats {
    justify-content: space-around;
  }
  
  .stat-card {
    min-width: auto;
    flex: 1;
    padding: 1rem;
  }
  
  .stat-card i {
    font-size: 1.5rem;
  }
  
  .stat-number {
    font-size: 1.5rem;
  }
  
  .planning-content {
    gap: 1rem;
  }
  
  .notification {
    top: 1rem;
    right: 1rem;
    left: 1rem;
  }
}

@media (max-width: 480px) {
  .header-content h1 {
    font-size: 1.5rem;
  }
  
  .header-stats {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .sidebar-btn {
    padding: 0.75rem;
    font-size: 0.9rem;
  }
}
</style>
