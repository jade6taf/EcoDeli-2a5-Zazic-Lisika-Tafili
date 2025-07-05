<script>
import planningService from '@/services/planningService'

export default {
  name: 'DisponibiliteModal',
  props: {
    disponibilite: {
      type: Object,
      default: null
    }
  },
  emits: ['close', 'save', 'delete'],
  data() {
    return {
      form: {
        id: null,
        titre: '',
        dateDebut: '',
        dateFin: '',
        type: 'DISPONIBLE',
        description: '',
        recurrent: false,
        joursRecurrence: [],
        finRecurrence: ''
      },
      daysOfWeek: planningService.utils.getDaysOfWeek(),
      isLoading: false
    }
  },
  computed: {
    isEdit() {
      return this.disponibilite && this.disponibilite.id
    }
  },
  mounted() {
    if (this.disponibilite) {
      this.form = {
        ...this.disponibilite,
        dateDebut: planningService.utils.formatDateTimeLocal(this.disponibilite.dateDebut),
        dateFin: planningService.utils.formatDateTimeLocal(this.disponibilite.dateFin),
        joursRecurrence: this.disponibilite.joursRecurrence || [],
        finRecurrence: this.disponibilite.finRecurrence || ''
      }
    } else {
      const now = new Date()
      const oneHourLater = new Date(now.getTime() + 60 * 60 * 1000)

      this.form.dateDebut = planningService.utils.formatDateTimeLocal(now.toISOString())
      this.form.dateFin = planningService.utils.formatDateTimeLocal(oneHourLater.toISOString())
    }
  },
  methods: {
    async save() {
      if (this.isLoading) return

      const validation = planningService.utils.validateDisponibilite(this.form)
      if (!validation.isValid) {
        alert('Erreur de validation:\n' + validation.errors.join('\n'))
        return
      }

      this.isLoading = true
      try {
        const formData = {
          ...this.form,
          dateDebut: new Date(this.form.dateDebut).toISOString(),
          dateFin: new Date(this.form.dateFin).toISOString()
        }

        if (this.isEdit) {
          await planningService.modifierDisponibilite(this.form.id, formData)
          this.$emit('save', { ...formData, id: this.form.id })
        } else {
          const response = await planningService.creerDisponibilite(formData)
          this.$emit('save', response.data)
        }
      } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error)
        const errorMessage = error.response?.data?.details || error.response?.data?.message || 'Erreur lors de la sauvegarde'
        alert(errorMessage)
      } finally {
        this.isLoading = false
      }
    },

    async deleteDisponibilite() {
      if (!confirm('Êtes-vous sûr de vouloir supprimer ce créneau ?')) {
        return
      }

      this.isLoading = true
      try {
        await planningService.supprimerDisponibilite(this.form.id)
        this.$emit('delete', this.form.id)
      } catch (error) {
        console.error('Erreur lors de la suppression:', error)
        const errorMessage = error.response?.data?.details || error.response?.data?.message || 'Erreur lors de la suppression'
        alert(errorMessage)
      } finally {
        this.isLoading = false
      }
    }
  }
}
</script>

<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h3>{{ isEdit ? 'Modifier' : 'Ajouter' }} un créneau</h3>
        <button @click="$emit('close')" class="close-btn">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <form @submit.prevent="save" class="modal-body">
        <div class="form-group">
          <label>Titre</label>
          <input
            v-model="form.titre"
            type="text"
            class="form-control"
            placeholder="Titre du créneau (optionnel)"
          />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Date de début *</label>
            <input
              v-model="form.dateDebut"
              type="datetime-local"
              class="form-control"
              required
            />
          </div>
          <div class="form-group">
            <label>Date de fin *</label>
            <input
              v-model="form.dateFin"
              type="datetime-local"
              class="form-control"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label>Type *</label>
          <select v-model="form.type" class="form-control" required>
            <option value="">Sélectionner un type</option>
            <option value="DISPONIBLE">Disponible</option>
            <option value="OCCUPE">Occupé</option>
            <option value="PAUSE">Pause</option>
          </select>
        </div>

        <div class="form-group">
          <label>Description</label>
          <textarea
            v-model="form.description"
            class="form-control"
            rows="3"
            placeholder="Description optionnelle"
          ></textarea>
        </div>

        <div class="form-group">
          <label class="checkbox-label">
            <input
              v-model="form.recurrent"
              type="checkbox"
            />
            Créneau récurrent
          </label>
        </div>

        <div v-if="form.recurrent" class="recurrence-options">
          <div class="form-group">
            <label>Jours de récurrence</label>
            <div class="days-selector">
              <label v-for="day in daysOfWeek" :key="day.value" class="day-checkbox">
                <input
                  v-model="form.joursRecurrence"
                  type="checkbox"
                  :value="day.value"
                />
                {{ day.label }}
              </label>
            </div>
          </div>
          <div class="form-group">
            <label>Fin de récurrence</label>
            <input
              v-model="form.finRecurrence"
              type="date"
              class="form-control"
            />
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" @click="$emit('close')" class="btn btn-secondary">
            Annuler
          </button>
          <button v-if="isEdit" type="button" @click="deleteDisponibilite" class="btn btn-danger">
            Supprimer
          </button>
          <button type="submit" class="btn btn-primary">
            {{ isEdit ? 'Modifier' : 'Créer' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: #ffffff;
  border-radius: 16px;
  width: 90%;
  max-width: 650px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25), 
              0 10px 20px -5px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2rem 2rem 1rem 2rem;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px 16px 0 0;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: white;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.modal-header h3::before {
  content: "📅";
  font-size: 1.25rem;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  font-size: 1.25rem;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.modal-body {
  padding: 2rem;
  background: #ffffff;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.75rem;
  color: #374151;
  font-weight: 600;
  font-size: 0.95rem;
  letter-spacing: 0.025em;
}

.form-control {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: #f9fafb;
  color: #111827;
  font-size: 1rem;
  transition: all 0.2s ease;
  font-weight: 500;
}

.form-control:focus {
  outline: none;
  border-color: #667eea;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.form-control:hover {
  border-color: #d1d5db;
  background: #ffffff;
}

select.form-control {
  cursor: pointer;
}

textarea.form-control {
  resize: vertical;
  min-height: 100px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-bottom: 0 !important;
  padding: 1rem;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 12px;
  border: 2px solid #e0f2fe;
  transition: all 0.2s ease;
}

.checkbox-label:hover {
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  border-color: #0ea5e9;
}

.checkbox-label input {
  margin-right: 0.75rem;
  transform: scale(1.2);
  accent-color: #667eea;
}

.recurrence-options {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 1.5rem;
  border-radius: 16px;
  margin-top: 1rem;
  border: 2px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.days-selector {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  justify-content: center;
}

.day-checkbox {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  background: #ffffff;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  transition: all 0.2s ease;
  margin-bottom: 0 !important;
  min-width: 80px;
  justify-content: center;
}

.day-checkbox:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.day-checkbox input {
  margin-right: 0.5rem;
  accent-color: #667eea;
}

.day-checkbox input:checked {
  accent-color: white;
}

.day-checkbox:has(input:checked) {
  background: #667eea;
  color: white;
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 2rem;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 0 0 16px 16px;
}

.btn {
  padding: 1rem 2rem;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.2s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 120px;
  justify-content: center;
  letter-spacing: 0.025em;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 14px 0 rgba(102, 126, 234, 0.39);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: #6b7280;
  color: white;
  box-shadow: 0 4px 14px 0 rgba(107, 114, 128, 0.39);
}

.btn-secondary:hover:not(:disabled) {
  background: #4b5563;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(107, 114, 128, 0.4);
}

.btn-danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  box-shadow: 0 4px 14px 0 rgba(239, 68, 68, 0.39);
}

.btn-danger:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(239, 68, 68, 0.4);
}

/* Mode sombre */
@media (prefers-color-scheme: dark) {
  .modal-content {
    background: #1f2937;
    border-color: #374151;
  }
  
  .modal-body {
    background: #1f2937;
  }
  
  .form-group label {
    color: #f9fafb;
  }
  
  .form-control {
    background: #374151;
    color: #f9fafb;
    border-color: #4b5563;
  }
  
  .form-control:focus {
    background: #374151;
  }
  
  .form-control:hover {
    background: #374151;
    border-color: #6b7280;
  }
  
  .recurrence-options {
    background: #374151;
    border-color: #4b5563;
  }
  
  .day-checkbox {
    background: #4b5563;
    border-color: #6b7280;
    color: #f9fafb;
  }
  
  .modal-footer {
    background: #374151;
    border-color: #4b5563;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .modal-content {
    width: 95%;
    margin: 1rem;
    border-radius: 12px;
  }
  
  .modal-header {
    padding: 1.5rem;
    border-radius: 12px 12px 0 0;
  }
  
  .modal-header h3 {
    font-size: 1.25rem;
  }
  
  .modal-body {
    padding: 1.5rem;
  }
  
  .form-row {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .days-selector {
    justify-content: center;
    gap: 0.5rem;
  }
  
  .day-checkbox {
    min-width: 70px;
    padding: 0.6rem 0.8rem;
    font-size: 0.85rem;
  }
  
  .modal-footer {
    flex-direction: column-reverse;
    padding: 1.5rem;
  }
  
  .btn {
    justify-content: center;
    min-width: auto;
    padding: 0.875rem 1.5rem;
  }
}

@media (max-width: 480px) {
  .modal-content {
    width: 98%;
    margin: 0.5rem;
  }
  
  .form-control {
    padding: 0.875rem;
  }
  
  .btn {
    padding: 0.75rem 1.25rem;
    font-size: 0.9rem;
  }
}
</style>
