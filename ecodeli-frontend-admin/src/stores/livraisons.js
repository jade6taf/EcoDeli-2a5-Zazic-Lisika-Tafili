import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useLivraisonsStore = defineStore('livraisons', () => {
  const livraisons = ref([])
  const loading = ref(false)
  const error = ref(null)

  const API_BASE = 'http://localhost:8080/api/admin/livraisons'

  const getAuthHeaders = () => {
    const token = localStorage.getItem('admin_token')
    return {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    }
  }

  const fetchLivraisons = async (filters = {}) => {
    loading.value = true
    error.value = null

    try {
      const params = new URLSearchParams()

      if (filters.statut) {
        params.append('statut', filters.statut)
      }
      if (filters.type) {
        params.append('type', filters.type)
      }

      const url = params.toString() ? `${API_BASE}?${params}` : API_BASE
      const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders()
      })

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`)
      }

      const data = await response.json()
      livraisons.value = data

      return {
        success: true,
        data: data
      }
    } catch (err) {
      console.error('Erreur lors de la récupération des livraisons:', err)
      error.value = err.message
      return {
        success: false,
        message: err.message
      }
    } finally {
      loading.value = false
    }
  }

  const getLivraisonById = async (id) => {
    loading.value = true
    error.value = null

    try {
      const response = await fetch(`${API_BASE}/${id}`, {
        method: 'GET',
        headers: getAuthHeaders()
      })

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`)
      }

      const data = await response.json()
      return {
        success: true,
        data: data
      }
    } catch (err) {
      console.error('Erreur lors de la récupération de la livraison:', err)
      error.value = err.message
      return {
        success: false,
        message: err.message
      }
    } finally {
      loading.value = false
    }
  }

  const updateStatutLivraison = async (id, nouveauStatut) => {
    loading.value = true
    error.value = null

    try {
      const response = await fetch(`${API_BASE}/${id}/statut`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify({ statut: nouveauStatut })
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.error || `Erreur HTTP: ${response.status}`)
      }

      const data = await response.json()

      const index = livraisons.value.findIndex(l => l.idLivraison === id)
      if (index !== -1) {
        livraisons.value[index] = data.livraison
      }

      return {
        success: true,
        message: data.message,
        data: data.livraison
      }
    } catch (err) {
      console.error('Erreur lors de la mise à jour du statut:', err)
      error.value = err.message
      return {
        success: false,
        message: err.message
      }
    } finally {
      loading.value = false
    }
  }

  const annulerLivraison = async (id) => {
    loading.value = true
    error.value = null

    try {
      const response = await fetch(`${API_BASE}/${id}/annuler`, {
        method: 'PUT',
        headers: getAuthHeaders()
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.error || `Erreur HTTP: ${response.status}`)
      }

      const data = await response.json()

      const index = livraisons.value.findIndex(l => l.idLivraison === id)
      if (index !== -1) {
        livraisons.value[index] = data.livraison
      }

      return {
        success: true,
        message: data.message,
        data: data.livraison
      }
    } catch (err) {
      console.error('Erreur lors de l\'annulation de la livraison:', err)
      error.value = err.message
      return {
        success: false,
        message: err.message
      }
    } finally {
      loading.value = false
    }
  }

  const getStats = async () => {
    loading.value = true
    error.value = null

    try {
      const response = await fetch(`${API_BASE}/stats`, {
        method: 'GET',
        headers: getAuthHeaders()
      })

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`)
      }

      const data = await response.json()
      return {
        success: true,
        data: data
      }
    } catch (err) {
      console.error('Erreur lors de la récupération des statistiques:', err)
      error.value = err.message
      return {
        success: false,
        message: err.message
      }
    } finally {
      loading.value = false
    }
  }

  const getLivraisonsByStatut = computed(() => (statut) => {
    return livraisons.value.filter(livraison => livraison.statut === statut)
  })

  const getLivraisonsByType = computed(() => (type) => {
    return livraisons.value.filter(livraison => livraison.typeLivraison === type)
  })

  const totalLivraisons = computed(() => livraisons.value.length)

  const getStatutSeverity = (statut) => {
    const severities = {
      'VALIDEE': 'info',
      'EN_COURS': 'warning',
      'ATTENTE_SEGMENT_2': 'secondary',
      'SEGMENT_2_EN_COURS': 'warning',
      'ARRIVED': 'help',
      'TERMINEE': 'success',
      'ANNULEE': 'danger'
    }
    return severities[statut] || 'secondary'
  }

  const getTypeSeverity = (type) => {
    const severities = {
      'DIRECTE': 'info',
      'PARTIELLE': 'warning'
    }
    return severities[type] || 'secondary'
  }

  const formatStatutLabel = (statut) => {
    const labels = {
      'VALIDEE': 'Validée',
      'EN_COURS': 'En cours',
      'ATTENTE_SEGMENT_2': 'Attente segment 2',
      'SEGMENT_2_EN_COURS': 'Segment 2 en cours',
      'ARRIVED': 'Arrivé',
      'TERMINEE': 'Terminée',
      'ANNULEE': 'Annulée'
    }
    return labels[statut] || statut
  }

  const formatTypeLabel = (type) => {
    const labels = {
      'DIRECTE': 'Directe',
      'PARTIELLE': 'Partielle'
    }
    return labels[type] || type
  }

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A'
    return new Date(dateString).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const formatPrice = (price) => {
    if (price == null)
      return 'N/A'
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(price / 100)
  }

  return {
    livraisons,
    loading,
    error,

    fetchLivraisons,
    getLivraisonById,
    updateStatutLivraison,
    annulerLivraison,
    getStats,

    getLivraisonsByStatut,
    getLivraisonsByType,
    totalLivraisons,

    getStatutSeverity,
    getTypeSeverity,
    formatStatutLabel,
    formatTypeLabel,
    formatDate,
    formatPrice
  }
})
