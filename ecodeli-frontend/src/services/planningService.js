import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/planning'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default {
  async getDisponibilites(start, end) {
    try {
      const startDate = start.toISOString().split('T')[0]
      const endDate = end.toISOString().split('T')[0]

      const response = await apiClient.get('/disponibilites', {
        params: { start: startDate, end: endDate }
      })

      return response.data
    } catch (error) {
      console.error('Erreur lors de la récupération des disponibilités:', error)
      throw error
    }
  },

  async creerDisponibilite(disponibilite) {
    try {
      const response = await apiClient.post('/disponibilites', disponibilite)
      return response.data
    } catch (error) {
      console.error('Erreur lors de la création de la disponibilité:', error)
      throw error
    }
  },

  async modifierDisponibilite(id, disponibilite) {
    try {
      const response = await apiClient.put(`/disponibilites/${id}`, disponibilite)
      return response.data
    } catch (error) {
      console.error('Erreur lors de la modification de la disponibilité:', error)
      throw error
    }
  },

  async supprimerDisponibilite(id) {
    try {
      const response = await apiClient.delete(`/disponibilites/${id}`)
      return response.data
    } catch (error) {
      console.error('Erreur lors de la suppression de la disponibilité:', error)
      throw error
    }
  },

  async getStatistiques() {
    try {
      const response = await apiClient.get('/stats')
      return response.data
    } catch (error) {
      console.error('Erreur lors de la récupération des statistiques:', error)
      throw error
    }
  },

  async getProchainsCreneaux(limite = 5) {
    try {
      const response = await apiClient.get('/prochains-creneaux', {
        params: { limite }
      })
      return response.data
    } catch (error) {
      console.error('Erreur lors de la récupération des prochains créneaux:', error)
      throw error
    }
  },

  async getTypesDisponibilite() {
    try {
      const response = await apiClient.get('/types')
      return response.data
    } catch (error) {
      console.error('Erreur lors de la récupération des types:', error)
      throw error
    }
  },

  async getStatutsDisponibilite() {
    try {
      const response = await apiClient.get('/statuts')
      return response.data
    } catch (error) {
      console.error('Erreur lors de la récupération des statuts:', error)
      throw error
    }
  },

  async creerDisponibilitesBatch(disponibilites) {
    try {
      const response = await apiClient.post('/disponibilites/batch', disponibilites)
      return response.data
    } catch (error) {
      console.error('Erreur lors de la création en lot:', error)
      throw error
    }
  },

  utils: {
    formatForCalendar(disponibilite) {
      return {
        id: disponibilite.id,
        title: disponibilite.titre || this.getTypeLabel(disponibilite.type),
        start: disponibilite.dateDebut,
        end: disponibilite.dateFin,
        backgroundColor: this.getTypeColor(disponibilite.type),
        borderColor: this.getTypeColor(disponibilite.type),
        textColor: '#ffffff',
        extendedProps: {
          type: disponibilite.type,
          description: disponibilite.description,
          recurrent: disponibilite.recurrent,
          statut: disponibilite.statut
        }
      }
    },

    getTypeColor(type) {
      const colors = {
        'DISPONIBLE': '#22c55e',
        'OCCUPE': '#ef4444',
        'PAUSE': '#f59e0b'
      }
      return colors[type] || '#6b7280'
    },

    getTypeLabel(type) {
      const labels = {
        'DISPONIBLE': 'Disponible',
        'OCCUPE': 'Occupé',
        'PAUSE': 'Pause'
      }
      return labels[type] || type
    },

    getStatutLabel(statut) {
      const labels = {
        'ACTIVE': 'Active',
        'INACTIVE': 'Inactive',
        'TEMPORAIRE': 'Temporaire'
      }
      return labels[statut] || statut
    },

    formatDateTimeLocal(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      // Correction du fuseau horaire pour éviter le décalage
      date.setMinutes(date.getMinutes() - date.getTimezoneOffset())
      return date.toISOString().slice(0, 16)
    },

    formatDate(dateString) {
      if (!dateString) return ''
      return new Date(dateString).toLocaleDateString('fr-FR', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
      })
    },

    formatTime(dateString) {
      if (!dateString) return ''
      return new Date(dateString).toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    calculateDuration(dateDebut, dateFin) {
      if (!dateDebut || !dateFin) return 0
      const debut = new Date(dateDebut)
      const fin = new Date(dateFin)
      return (fin - debut) / (1000 * 60 * 60) // Conversion en heures
    },

    validateDisponibilite(disponibilite) {
      const errors = []

      if (!disponibilite.dateDebut) {
        errors.push('La date de début est obligatoire')
      }

      if (!disponibilite.dateFin) {
        errors.push('La date de fin est obligatoire')
      }

      if (disponibilite.dateDebut && disponibilite.dateFin) {
        const debut = new Date(disponibilite.dateDebut)
        const fin = new Date(disponibilite.dateFin)

        if (fin <= debut) {
          errors.push('La date de fin doit être postérieure à la date de début')
        }
      }

      if (!disponibilite.type) {
        errors.push('Le type de disponibilité est obligatoire')
      }

      if (disponibilite.recurrent && (!disponibilite.joursRecurrence || disponibilite.joursRecurrence.length === 0)) {
        errors.push('Veuillez sélectionner au moins un jour pour la récurrence')
      }

      return {
        isValid: errors.length === 0,
        errors
      }
    },

    getDaysOfWeek() {
      return [
        { value: 'LUNDI', label: 'Lun', fullLabel: 'Lundi' },
        { value: 'MARDI', label: 'Mar', fullLabel: 'Mardi' },
        { value: 'MERCREDI', label: 'Mer', fullLabel: 'Mercredi' },
        { value: 'JEUDI', label: 'Jeu', fullLabel: 'Jeudi' },
        { value: 'VENDREDI', label: 'Ven', fullLabel: 'Vendredi' },
        { value: 'SAMEDI', label: 'Sam', fullLabel: 'Samedi' },
        { value: 'DIMANCHE', label: 'Dim', fullLabel: 'Dimanche' }
      ]
    }
  }
}
