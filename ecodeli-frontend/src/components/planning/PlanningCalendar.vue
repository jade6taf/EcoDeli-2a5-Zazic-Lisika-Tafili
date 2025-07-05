<script>
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import frLocale from '@fullcalendar/core/locales/fr'
import planningService from '@/services/planningService'
import DisponibiliteModal from './DisponibiliteModal.vue'

export default {
  name: 'PlanningCalendar',
  components: {
    FullCalendar,
    DisponibiliteModal
  },
  emits: ['success', 'error', 'event-created', 'event-updated', 'event-deleted'],
  data() {
    return {
      currentView: 'dayGridMonth',
      showModal: false,
      selectedDisponibilite: null,
      isLoading: false,
      calendarOptions: {
        plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
        headerToolbar: {
          left: 'prev,next today',
          center: 'title',
          right: ''
        },
        initialView: 'dayGridMonth',
        locale: frLocale,
        height: 'auto',
        selectable: true,
        editable: true,
        dayMaxEvents: true,
        weekends: true,
        businessHours: {
          daysOfWeek: [1, 2, 3, 4, 5],
          startTime: '08:00',
          endTime: '18:00'
        },
        events: this.loadEvents,
        select: this.handleDateSelect,
        eventClick: this.handleEventClick,
        eventDrop: this.handleEventDrop,
        eventResize: this.handleEventResize,
        eventDisplay: 'block',
        displayEventTime: true,
        slotMinTime: '06:00:00',
        slotMaxTime: '22:00:00',
        slotDuration: '00:30:00',
        expandRows: true,
        dayHeaderFormat: { weekday: 'short', day: 'numeric' },
        eventTimeFormat: {
          hour: '2-digit',
          minute: '2-digit',
          meridiem: false
        },
        nowIndicator: true,
        scrollTime: '08:00:00',
        allDaySlot: false
      }
    }
  },
  methods: {
    async loadEvents(info) {
      if (this.isLoading) return []

      try {
        this.isLoading = true
        const events = await planningService.getDisponibilites(info.start, info.end)
        console.log('Événements chargés depuis l\'API:', events)

        return events || []
      } catch (error) {
        console.error('Erreur lors du chargement des événements:', error)
        this.$emit('error', 'Erreur lors du chargement du planning')
        return []
      } finally {
        this.isLoading = false
      }
    },

    handleDateSelect(selectInfo) {
      this.selectedDisponibilite = {
        dateDebut: selectInfo.start.toISOString(),
        dateFin: selectInfo.end.toISOString(),
        type: 'DISPONIBLE'
      }
      this.showModal = true

      selectInfo.view.calendar.unselect()
    },

    handleEventClick(clickInfo) {
      const event = clickInfo.event
      this.selectedDisponibilite = {
        id: event.id,
        titre: event.title,
        dateDebut: event.start.toISOString(),
        dateFin: event.end.toISOString(),
        type: event.extendedProps.type,
        description: event.extendedProps.description,
        recurrent: event.extendedProps.recurrent,
        statut: event.extendedProps.statut
      }
      this.showModal = true
    },

    async handleEventDrop(dropInfo) {
      try {
        const eventData = {
          id: dropInfo.event.id,
          dateDebut: dropInfo.event.start.toISOString(),
          dateFin: dropInfo.event.end.toISOString(),
          type: dropInfo.event.extendedProps.type,
          titre: dropInfo.event.title,
          description: dropInfo.event.extendedProps.description
        }

        await planningService.modifierDisponibilite(dropInfo.event.id, eventData)
        this.$emit('success', 'Créneau déplacé avec succès')
        this.$emit('event-updated', eventData)
      } catch (error) {
        console.error('Erreur lors du déplacement:', error)
        dropInfo.revert()
        this.$emit('error', 'Erreur lors du déplacement du créneau')
      }
    },

    async handleEventResize(resizeInfo) {
      try {
        const eventData = {
          id: resizeInfo.event.id,
          dateDebut: resizeInfo.event.start.toISOString(),
          dateFin: resizeInfo.event.end.toISOString(),
          type: resizeInfo.event.extendedProps.type,
          titre: resizeInfo.event.title,
          description: resizeInfo.event.extendedProps.description
        }

        await planningService.modifierDisponibilite(resizeInfo.event.id, eventData)
        this.$emit('success', 'Créneau redimensionné avec succès')
        this.$emit('event-updated', eventData)
      } catch (error) {
        console.error('Erreur lors du redimensionnement:', error)
        resizeInfo.revert()
        this.$emit('error', 'Erreur lors du redimensionnement du créneau')
      }
    },

    changeView(viewName) {
      this.currentView = viewName
      const calendarApi = this.$refs.fullCalendar.getApi()
      calendarApi.changeView(viewName)
    },

    closeModal() {
      this.showModal = false
      this.selectedDisponibilite = null
    },

    async saveDisponibilite(disponibilite) {
      try {
        this.closeModal()

        const calendarApi = this.$refs.fullCalendar.getApi()
        calendarApi.refetchEvents()

        if (disponibilite.id) {
          this.$emit('success', 'Créneau modifié avec succès')
          this.$emit('event-updated', disponibilite)
        } else {
          this.$emit('success', 'Créneau créé avec succès')
          this.$emit('event-created', disponibilite)
        }
      } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error)
        this.$emit('error', 'Erreur lors de la sauvegarde du créneau')
      }
    },

    async deleteDisponibilite(disponibiliteId) {
      try {
        this.closeModal()

        const calendarApi = this.$refs.fullCalendar.getApi()
        calendarApi.refetchEvents()

        this.$emit('success', 'Créneau supprimé avec succès')
        this.$emit('event-deleted', disponibiliteId)
      } catch (error) {
        console.error('Erreur lors de la suppression:', error)
        this.$emit('error', 'Erreur lors de la suppression du créneau')
      }
    },

    refreshEvents() {
      const calendarApi = this.$refs.fullCalendar.getApi()
      calendarApi.refetchEvents()
    },

    openAddModal(defaultData = {}) {
      this.selectedDisponibilite = {
        type: 'DISPONIBLE',
        ...defaultData
      }
      this.showModal = true
    },

    goToDate(date) {
      const calendarApi = this.$refs.fullCalendar.getApi()
      calendarApi.gotoDate(date)
    },

    today() {
      const calendarApi = this.$refs.fullCalendar.getApi()
      calendarApi.today()
    },

    prev() {
      const calendarApi = this.$refs.fullCalendar.getApi()
      calendarApi.prev()
    },

    next() {
      const calendarApi = this.$refs.fullCalendar.getApi()
      calendarApi.next()
    }
  }
}
</script>

<template>
  <div class="planning-calendar">
    <div class="calendar-header">
      <div class="calendar-actions">
        <button @click="showModal = true" class="btn btn-primary">
          <i class="fas fa-plus"></i> Ajouter un créneau
        </button>
        <div class="view-buttons">
          <button
            @click="changeView('dayGridMonth')"
            :class="{ active: currentView === 'dayGridMonth' }"
            class="view-btn"
          >
            Mois
          </button>
          <button
            @click="changeView('timeGridWeek')"
            :class="{ active: currentView === 'timeGridWeek' }"
            class="view-btn"
          >
            Semaine
          </button>
          <button
            @click="changeView('timeGridDay')"
            :class="{ active: currentView === 'timeGridDay' }"
            class="view-btn"
          >
            Jour
          </button>
        </div>
      </div>
    </div>

    <div class="calendar-container">
      <FullCalendar
        ref="fullCalendar"
        :options="calendarOptions"
      />
    </div>

    <!-- Modal pour créer/éditer un créneau -->
    <DisponibiliteModal
      v-if="showModal"
      :disponibilite="selectedDisponibilite"
      @close="closeModal"
      @save="saveDisponibilite"
      @delete="deleteDisponibilite"
    />
  </div>
</template>

<style scoped>
.planning-calendar {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.calendar-header {
  margin-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.calendar-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-primary {
  background: var(--primary-color);
  color: white;
}

.btn-primary:hover {
  background: var(--primary-color-dark);
  transform: translateY(-1px);
}

.view-buttons {
  display: flex;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  overflow: hidden;
  background: var(--card-bg);
}

.view-btn {
  padding: 0.5rem 1rem;
  border: none;
  background: transparent;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.2s;
  border-right: 1px solid var(--border-color);
}

.view-btn:last-child {
  border-right: none;
}

.view-btn:hover {
  background: var(--primary-color-light);
}

.view-btn.active {
  background: var(--primary-color);
  color: white;
}

.calendar-container {
  flex: 1;
  background: var(--card-bg);
  border-radius: 8px;
  padding: 1rem;
  box-shadow: var(--shadow);
  overflow: hidden;
}

/* Styles FullCalendar personnalisés */
:deep(.fc) {
  font-family: inherit;
}

:deep(.fc-header-toolbar) {
  margin-bottom: 1rem;
}

:deep(.fc-toolbar-title) {
  color: var(--text-primary);
  font-size: 1.5rem;
  font-weight: 600;
}

:deep(.fc-button-primary) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  font-weight: 500;
}

:deep(.fc-button-primary:hover) {
  background-color: var(--primary-color-dark);
  border-color: var(--primary-color-dark);
}

:deep(.fc-button-primary:disabled) {
  background-color: var(--text-secondary);
  border-color: var(--text-secondary);
}

:deep(.fc-today-button) {
  background-color: var(--info-color);
  border-color: var(--info-color);
}

:deep(.fc-daygrid-day-number),
:deep(.fc-col-header-cell-cushion) {
  color: var(--text-primary);
  text-decoration: none;
}

:deep(.fc-day-today) {
  background-color: var(--primary-color-light);
}

:deep(.fc-event) {
  border-radius: 4px;
  font-size: 0.85rem;
  padding: 2px 4px;
  cursor: pointer;
  border: none;
}

:deep(.fc-event:hover) {
  opacity: 0.8;
}

:deep(.fc-daygrid-event) {
  margin: 1px 0;
  border-radius: 3px;
}

:deep(.fc-timegrid-event) {
  border-radius: 3px;
}

:deep(.fc-event-title) {
  font-weight: 500;
}

:deep(.fc-event-time) {
  font-weight: 400;
  opacity: 0.9;
}

:deep(.fc-timegrid-slot-label) {
  color: var(--text-secondary);
}

:deep(.fc-timegrid-axis-cushion) {
  color: var(--text-secondary);
}

:deep(.fc-scrollgrid) {
  border-color: var(--border-color);
}

:deep(.fc-scrollgrid-sync-table) {
  border-color: var(--border-color);
}

:deep(.fc-daygrid-day-frame),
:deep(.fc-timegrid-col-frame) {
  background: var(--background-color);
}

:deep(.fc-highlight) {
  background: var(--primary-color-light);
  opacity: 0.3;
}

:deep(.fc-non-business) {
  background: var(--background-color);
  opacity: 0.7;
}

/* Responsive */
@media (max-width: 768px) {
  .calendar-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .calendar-actions {
    justify-content: space-between;
  }
  
  .view-buttons {
    flex: 1;
    justify-content: center;
  }
  
  .view-btn {
    flex: 1;
    justify-content: center;
  }
  
  .calendar-container {
    padding: 0.5rem;
  }
  
  :deep(.fc-toolbar) {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  :deep(.fc-toolbar-chunk) {
    display: flex;
    justify-content: center;
  }
  
  :deep(.fc-button-group) {
    display: flex;
  }
  
  :deep(.fc-event-title) {
    font-size: 0.75rem;
  }
}

@media (max-width: 480px) {
  :deep(.fc-daygrid-event-dot) {
    display: none;
  }
  
  :deep(.fc-event-title) {
    font-size: 0.7rem;
  }
  
  .btn {
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
  }
}
</style>
