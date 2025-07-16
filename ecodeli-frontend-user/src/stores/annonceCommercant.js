import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/annonces-commercant'

export const useAnnonceCommercantStore = defineStore('annonceCommercant', () => {
  const annonces = ref([])
  const currentAnnonce = ref(null)
  const categories = ref({})
  const loading = ref(false)
  const error = ref(null)
  const statistics = ref({})
  const contextualHelp = ref({})
  const searchFilters = ref({
    searchTerm: '',
    categorie: null,
    statut: null,
    commercantId: null
  })

  const getAnnoncesByCategorie = computed(() => {
    return (categorie) => annonces.value.filter(a => a.categorie === categorie)
  })

  const getActiveAnnonces = computed(() => {
    return annonces.value.filter(a => a.statut === 'ACTIVE')
  })

  const getPendingApprovalAnnonces = computed(() => {
    return annonces.value.filter(a => a.statutApprobation === 'EN_ATTENTE')
  })

  const getAnnonceById = computed(() => {
    return (id) => annonces.value.find(a => a.idAnnonceCommercant === id)
  })

  const getCategoriesList = computed(() => {
    return Object.values(categories.value)
  })

  const isLivraisonPonctuelle = computed(() => {
    return (annonce) => annonce?.categorie === 'LIVRAISON_PONCTUELLE'
  })

  const isServiceChariot = computed(() => {
    return (annonce) => annonce?.categorie === 'SERVICE_CHARIOT'
  })

  const isTransportMarchandises = computed(() => {
    return (annonce) => annonce?.categorie === 'TRANSPORT_MARCHANDISES'
  })

  const setLoading = (value) => {
    loading.value = value
  }

  const setError = (message) => {
    error.value = message
  }

  const clearError = () => {
    error.value = null
  }


  const fetchAllAnnonces = async () => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.get(API_BASE_URL)
      annonces.value = response.data
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du chargement des annonces'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const fetchAnnonceById = async (id) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.get(`${API_BASE_URL}/${id}`)
      currentAnnonce.value = response.data
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du chargement de l\'annonce'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const createAnnonce = async (annonceData) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.post(API_BASE_URL, annonceData)
      
      if (response.data.success) {
        annonces.value.unshift(response.data.annonce)
        return { success: true, data: response.data.annonce }
      } else {
        setError(response.data.error)
        return { success: false, error: response.data.error }
      }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors de la création de l\'annonce'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const updateAnnonce = async (id, updates) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.put(`${API_BASE_URL}/${id}`, updates)
      
      if (response.data.success) {
        const index = annonces.value.findIndex(a => a.idAnnonceCommercant === id)
        if (index !== -1) {
          annonces.value[index] = response.data.annonce
        }
        if (currentAnnonce.value?.idAnnonceCommercant === id) {
          currentAnnonce.value = response.data.annonce
        }
        return { success: true, data: response.data.annonce }
      } else {
        setError(response.data.error)
        return { success: false, error: response.data.error }
      }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors de la mise à jour de l\'annonce'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const deleteAnnonce = async (id) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.delete(`${API_BASE_URL}/${id}`)
      
      if (response.data.success) {
        annonces.value = annonces.value.filter(a => a.idAnnonceCommercant !== id)
        if (currentAnnonce.value?.idAnnonceCommercant === id) {
          currentAnnonce.value = null
        }
        return { success: true }
      } else {
        setError(response.data.error)
        return { success: false, error: response.data.error }
      }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors de la suppression de l\'annonce'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }


  const fetchCategories = async () => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.get(`${API_BASE_URL}/categories`)
      categories.value = response.data
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du chargement des catégories'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const fetchAnnoncesByCategorie = async (categorie) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.get(`${API_BASE_URL}/categories/${categorie}`)
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du chargement des annonces'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const fetchActiveByCategorieAnnouncements = async (categorie) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.get(`${API_BASE_URL}/categories/${categorie}/active`)
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du chargement des annonces actives'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const fetchContextualHelp = async (categorie) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/categories/${categorie}/help`)
      contextualHelp.value[categorie] = response.data
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du chargement de l\'aide'
      setError(message)
      return { success: false, error: message }
    }
  }


  const fetchAnnoncesByCommercant = async (commercantId) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.get(`${API_BASE_URL}/commercant/${commercantId}`)
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du chargement des annonces'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const fetchAnnoncesByCommercantAndCategorie = async (commercantId, categorie) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.get(`${API_BASE_URL}/commercant/${commercantId}/categories/${categorie}`)
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du chargement des annonces'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }


  const publishAnnonce = async (id) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.post(`${API_BASE_URL}/${id}/publish`)
      
      if (response.data.success) {
        const index = annonces.value.findIndex(a => a.idAnnonceCommercant === id)
        if (index !== -1) {
          annonces.value[index] = response.data.annonce
        }
        return { success: true, data: response.data.annonce }
      } else {
        setError(response.data.error)
        return { success: false, error: response.data.error }
      }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors de la publication'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const approveAnnonce = async (id, adminId, commentaire) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.post(`${API_BASE_URL}/${id}/approve`, {
        adminId,
        commentaire
      })
      
      if (response.data.success) {
        const index = annonces.value.findIndex(a => a.idAnnonceCommercant === id)
        if (index !== -1) {
          annonces.value[index] = response.data.annonce
        }
        return { success: true, data: response.data.annonce }
      } else {
        setError(response.data.error)
        return { success: false, error: response.data.error }
      }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors de l\'approbation'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const rejectAnnonce = async (id, adminId, commentaire) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.post(`${API_BASE_URL}/${id}/reject`, {
        adminId,
        commentaire
      })
      
      if (response.data.success) {
        const index = annonces.value.findIndex(a => a.idAnnonceCommercant === id)
        if (index !== -1) {
          annonces.value[index] = response.data.annonce
        }
        return { success: true, data: response.data.annonce }
      } else {
        setError(response.data.error)
        return { success: false, error: response.data.error }
      }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du rejet'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }


  const searchAnnonces = async (filters = {}) => {
    try {
      setLoading(true)
      clearError()
      
      const params = new URLSearchParams()
      if (filters.searchTerm) params.append('searchTerm', filters.searchTerm)
      if (filters.categorie) params.append('categorie', filters.categorie)
      if (filters.statut) params.append('statut', filters.statut)
      if (filters.commercantId) params.append('commercantId', filters.commercantId)
      
      const response = await axios.get(`${API_BASE_URL}/search?${params}`)
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors de la recherche'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const fetchActiveAnnouncements = async () => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.get(`${API_BASE_URL}/active`)
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du chargement des annonces actives'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }


  const fetchPendingApproval = async () => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.get(`${API_BASE_URL}/pending-approval`)
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du chargement des annonces en attente'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const fetchStatistics = async () => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.get(`${API_BASE_URL}/statistics`)
      statistics.value = response.data
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du chargement des statistiques'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const fetchExpiringAnnouncements = async (days = 7) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.get(`${API_BASE_URL}/expiring?days=${days}`)
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors du chargement des annonces expirant'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }


  const checkLivraisonPonctuelleAvailability = async (requestData) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.post(`${API_BASE_URL}/livraison-ponctuelle/check-availability`, requestData)
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors de la vérification de disponibilité'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const updateServiceChariotAvailability = async (annonceId, creneauxDisponibles) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.post(`${API_BASE_URL}/service-chariot/update-availability`, {
        annonceId,
        creneauxDisponibles
      })
      
      if (response.data.success) {
        const index = annonces.value.findIndex(a => a.idAnnonceCommercant === annonceId)
        if (index !== -1) {
          annonces.value[index] = response.data.annonce
        }
        return { success: true, data: response.data.annonce }
      } else {
        setError(response.data.error)
        return { success: false, error: response.data.error }
      }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors de la mise à jour de disponibilité'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const optimizeTransportRoutes = async (annonceId) => {
    try {
      setLoading(true)
      clearError()
      
      const response = await axios.post(`${API_BASE_URL}/transport-marchandises/optimize-routes`, {
        annonceId
      })
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors de l\'optimisation des routes'
      setError(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }


  const validateAnnonceData = async (annonceData) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/validate`, annonceData)
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.errors || 'Erreur de validation'
      return { success: false, error: message }
    }
  }

  const estimatePrice = async (annonceData) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/estimate-price`, annonceData)
      
      return { success: true, data: response.data }
    } catch (err) {
      const message = err.response?.data?.error || 'Erreur lors de l\'estimation du prix'
      return { success: false, error: message }
    }
  }


  const setCurrentAnnonce = (annonce) => {
    currentAnnonce.value = annonce
  }

  const clearCurrentAnnonce = () => {
    currentAnnonce.value = null
  }

  const setSearchFilters = (filters) => {
    searchFilters.value = { ...searchFilters.value, ...filters }
  }

  const clearSearchFilters = () => {
    searchFilters.value = {
      searchTerm: '',
      categorie: null,
      statut: null,
      commercantId: null
    }
  }

  const resetStore = () => {
    annonces.value = []
    currentAnnonce.value = null
    categories.value = {}
    loading.value = false
    error.value = null
    statistics.value = {}
    contextualHelp.value = {}
    clearSearchFilters()
  }

  return {
    annonces,
    currentAnnonce,
    categories,
    loading,
    error,
    statistics,
    contextualHelp,
    searchFilters,
    
    getAnnoncesByCategorie,
    getActiveAnnonces,
    getPendingApprovalAnnonces,
    getAnnonceById,
    getCategoriesList,
    isLivraisonPonctuelle,
    isServiceChariot,
    isTransportMarchandises,
    
    fetchAllAnnonces,
    fetchAnnonceById,
    createAnnonce,
    updateAnnonce,
    deleteAnnonce,
    fetchCategories,
    fetchAnnoncesByCategorie,
    fetchActiveByCategorieAnnouncements,
    fetchContextualHelp,
    fetchAnnoncesByCommercant,
    fetchAnnoncesByCommercantAndCategorie,
    publishAnnonce,
    approveAnnonce,
    rejectAnnonce,
    searchAnnonces,
    fetchActiveAnnouncements,
    fetchPendingApproval,
    fetchStatistics,
    fetchExpiringAnnouncements,
    checkLivraisonPonctuelleAvailability,
    updateServiceChariotAvailability,
    optimizeTransportRoutes,
    validateAnnonceData,
    estimatePrice,
    setCurrentAnnonce,
    clearCurrentAnnonce,
    setSearchFilters,
    clearSearchFilters,
    resetStore,
    setLoading,
    setError,
    clearError
  }
})