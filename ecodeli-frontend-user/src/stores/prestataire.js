import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const usePrestataireStore = defineStore('prestataire', () => {
  const statutValidation = ref(null)
  const statistiques = ref(null)
  const demandesDisponibles = ref([])
  const mesCandidatures = ref([])
  const loading = ref(false)
  const error = ref(null)

  const totalCandidatures = computed(() => statistiques.value?.totalCandidatures || 0)
  const tauxAcceptation = computed(() => statistiques.value?.tauxAcceptation || 0)
  const peutCandidater = computed(() => statutValidation.value?.peutCandidater || false)

  const getStatutValidation = async () => {
    loading.value = true
    error.value = null

    try {
      const backendUrl = 'http://localhost:8080/api/prestataire/statut-validation'
      const response = await axios.get(backendUrl)
      
      statutValidation.value = response.data
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors du chargement du statut'
      console.error('Erreur getStatutValidation:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const getStatistiques = async () => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get('/api/prestataire/statistiques')
      statistiques.value = response.data
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors du chargement des statistiques'
      console.error('Erreur getStatistiques:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const getDemandesDisponibles = async () => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get('/api/prestataire/demandes-disponibles')
      demandesDisponibles.value = response.data
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors du chargement des demandes'
      console.error('Erreur getDemandesDisponibles:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const creerCandidature = async (candidatureData) => {
    console.log('=== STORE: Création candidature ===')
    console.log('Données envoyées:', candidatureData)
    
    loading.value = true
    error.value = null

    try {
      const backendUrl = 'http://localhost:8080/api/candidatures/creer'
      console.log('URL appelée:', backendUrl)
      
      const response = await axios.post(backendUrl, candidatureData)
      console.log('Réponse reçue:', response.data)

      await Promise.all([
        getMesCandidatures(),
        getDemandesDisponibles()
      ])

      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors de l\'envoi de la candidature'
      console.error('Erreur creerCandidature:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const uploadJustificatif = async (file, description) => {
    loading.value = true
    error.value = null

    try {
      const formData = new FormData()
      formData.append('file', file)
      if (description) formData.append('description', description)

      const backendUrl = 'http://localhost:8080/api/prestataire/upload-justificatif'
      const response = await axios.post(backendUrl, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })

      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors de l\'upload du justificatif'
      console.error('Erreur uploadJustificatif:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const getJustificatifs = async () => {
    loading.value = true
    error.value = null

    try {
      const backendUrl = 'http://localhost:8080/api/prestataire/justificatifs'
      const response = await axios.get(backendUrl)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors du chargement des justificatifs'
      console.error('Erreur getJustificatifs:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const supprimerJustificatif = async (justificatifId) => {
    loading.value = true
    error.value = null

    try {
      const backendUrl = `http://localhost:8080/api/prestataire/justificatifs/${justificatifId}`
      const response = await axios.delete(backendUrl)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors de la suppression du justificatif'
      console.error('Erreur supprimerJustificatif:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const getDemandesPaginated = async (page = 0, size = 10, filters = {}) => {
    loading.value = true
    error.value = null

    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        ...filters
      })

      const backendUrl = `http://localhost:8080/api/prestataire/demandes-disponibles/paginated?${params}`
      const response = await axios.get(backendUrl)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors du chargement des demandes'
      console.error('Erreur getDemandesPaginated:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const getMesCandidatures = async () => {
    console.log('=== STORE: Récupération candidatures prestataire ===')
    loading.value = true
    error.value = null

    try {
      const { useAuthStore } = await import('./auth')
      const authStore = useAuthStore()
      const prestataireId = authStore.user?.id || authStore.user?.idUtilisateur || 2;
      const backendUrl = `http://localhost:8080/api/candidatures/prestataire/${prestataireId}`
      
      console.log('URL appelée:', backendUrl)
      console.log('PrestataireId utilisé:', prestataireId)
      
      const response = await axios.get(backendUrl)
      console.log('Candidatures reçues:', response.data)
      
      mesCandidatures.value = response.data
      return { success: true, data: response.data }
    } catch (err) {
      console.error('Erreur getMesCandidatures:', err)
      error.value = err.response?.data?.error || 'Erreur lors du chargement des candidatures'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const getProfil = async () => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get('/api/prestataire/profil')
      
      if (response.data.validation) {
        statutValidation.value = response.data.validation
      }
      if (response.data.statistiques) {
        statistiques.value = response.data.statistiques
      }

      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors du chargement du profil'
      console.error('Erreur getProfil:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const peutCandidaterPourDemande = async (demandeId) => {
    try {
      const response = await axios.get(`/api/prestataire/peut-candidater/${demandeId}`)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors de la vérification'
      console.error('Erreur peutCandidaterPourDemande:', err)
      return { success: false, message: error.value }
    }
  }

  const initializeData = async () => {
    loading.value = true

    try {
      await Promise.all([
        getStatutValidation(),
        getStatistiques()
      ])

      if (peutCandidater.value) {
        await Promise.all([
          getDemandesDisponibles(),
          getMesCandidatures()
        ])
      }

      return { success: true }
    } catch (err) {
      console.error('Erreur lors de l\'initialisation:', err)
      return { success: false }
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    statutValidation.value = null
    statistiques.value = null
    demandesDisponibles.value = []
    mesCandidatures.value = []
    loading.value = false
    error.value = null
  }

  const filtrerDemandes = (criteres) => {
    if (!demandesDisponibles.value.length) return []

    return demandesDisponibles.value.filter(demande => {
      if (criteres.search) {
        const search = criteres.search.toLowerCase()
        const matchTexte = demande.titre.toLowerCase().includes(search) ||
                          demande.description.toLowerCase().includes(search)
        if (!matchTexte) return false
      }

      if (criteres.dateMin) {
        const dateMin = new Date(criteres.dateMin)
        const dateDemande = new Date(demande.dateSouhaitee)
        if (dateDemande < dateMin) return false
      }

      if (criteres.dateMax) {
        const dateMax = new Date(criteres.dateMax)
        const dateDemande = new Date(demande.dateSouhaitee)
        if (dateDemande > dateMax) return false
      }

      return true
    })
  }

  const getCandidaturesParStatut = (statut) => {
    return mesCandidatures.value.filter(candidature => candidature.statut === statut)
  }

  const getMesMissions = async () => {
    console.log('=== STORE: Récupération missions prestataire ===')
    loading.value = true
    error.value = null

    try {
      const { useAuthStore } = await import('./auth')
      const authStore = useAuthStore()
      const prestataireId = authStore.user?.id || authStore.user?.idUtilisateur || 2;
      const backendUrl = `http://localhost:8080/api/missions/prestataire/${prestataireId}`
      
      console.log('URL appelée:', backendUrl)
      
      const response = await axios.get(backendUrl)
      console.log('Missions reçues:', response.data)
      
      return { success: true, data: response.data }
    } catch (err) {
      console.error('Erreur getMesMissions:', err)
      error.value = err.response?.data?.error || 'Erreur lors du chargement des missions'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const demarrerMission = async (missionId) => {
    console.log('=== STORE: Démarrage mission ===', missionId)
    loading.value = true
    error.value = null

    try {
      const backendUrl = `http://localhost:8080/api/missions/${missionId}/start`
      
      const response = await axios.post(backendUrl)
      console.log('Mission démarrée:', response.data)
      
      return { success: true, data: response.data }
    } catch (err) {
      console.error('Erreur demarrerMission:', err)
      error.value = err.response?.data?.error || 'Erreur lors du démarrage de la mission'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const terminerMission = async (missionId, completionData) => {
    console.log('=== STORE: Terminaison mission ===', missionId, completionData)
    loading.value = true
    error.value = null

    try {
      const backendUrl = `http://localhost:8080/api/missions/${missionId}/complete`
      
      const response = await axios.post(backendUrl, {
        noteFinale: completionData.noteFinale || '',
        photoFinale: completionData.photoFinale || null
      })
      console.log('Mission terminée:', response.data)
      
      return { success: true, data: response.data }
    } catch (err) {
      console.error('Erreur terminerMission:', err)
      error.value = err.response?.data?.error || 'Erreur lors de la terminaison de la mission'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    statutValidation,
    statistiques,
    demandesDisponibles,
    mesCandidatures,
    loading,
    error,

    totalCandidatures,
    tauxAcceptation,
    peutCandidater,

    getStatutValidation,
    getStatistiques,
    getDemandesDisponibles,
    creerCandidature,
    getMesCandidatures,
    getProfil,
    peutCandidaterPourDemande,
    initializeData,
    reset,
    filtrerDemandes,
    getCandidaturesParStatut,
    uploadJustificatif,
    getJustificatifs,
    supprimerJustificatif,
    getDemandesPaginated,
    getMesMissions,
    demarrerMission,
    terminerMission
  }
})