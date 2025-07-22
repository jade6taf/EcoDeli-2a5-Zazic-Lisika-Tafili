import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useCandidaturesStore = defineStore('candidatures', () => {
  const candidatures = ref([])
  const loading = ref(false)
  const error = ref(null)

  const postulerPourAnnonce = async (annonceId, livreurId, message = '') => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.post('https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures-livraison', {
        annonceId,
        livreurId,
        message
      })

      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de la candidature'
      console.error('Erreur postulerPourAnnonce:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const getCandidaturesByAnnonce = async (annonceId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures-livraison/annonce/${annonceId}`)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement des candidatures'
      console.error('Erreur getCandidaturesByAnnonce:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const getCandidaturesByLivreur = async (livreurId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures-livraison/livreur/${livreurId}`)
      candidatures.value = response.data
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement des candidatures'
      console.error('Erreur getCandidaturesByLivreur:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const accepterCandidature = async (candidatureId, commentaire = '') => {
    loading.value = true
    error.value = null

    try {
      await axios.put(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures-livraison/${candidatureId}/accepter`, {
        commentaire
      })

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de l\'acceptation'
      console.error('Erreur accepterCandidature:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const refuserCandidature = async (candidatureId, commentaire = '') => {
    loading.value = true
    error.value = null

    try {
      await axios.put(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures-livraison/${candidatureId}/refuser`, {
        commentaire
      })

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du refus'
      console.error('Erreur refuserCandidature:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const candidaterPartielle = async (annonceId, livreurId, segment, entrepotChoisi = null, message = '') => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.post('https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures-livraison/partielle', {
        annonceId,
        livreurId,
        segment,
        entrepotChoisi,
        messageLivreur: message
      })

      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors de la candidature partielle'
      console.error('Erreur candidaterPartielle:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const getEntrepotsDisponibles = async () => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get('https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures-livraison/entrepots')
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement des entrepôts'
      console.error('Erreur getEntrepotsDisponibles:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const getCandidaturesParSegment = async (annonceId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures-livraison/annonce/${annonceId}/segments`)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement des candidatures par segment'
      console.error('Erreur getCandidaturesParSegment:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const accepterCandidaturePartielle = async (candidatureId, commentaire = '') => {
    loading.value = true
    error.value = null

    try {
      await axios.put(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures-livraison/${candidatureId}/accepter-partielle`, {
        commentaire
      })

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors de l\'acceptation de la candidature partielle'
      console.error('Erreur accepterCandidaturePartielle:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }


  const getCandidaturesByDemandeService = async (demandeId) => {
    console.log('=== STORE: Récupération candidatures pour demande service', demandeId)
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures/demande/${demandeId}`)
      console.log('Candidatures reçues:', response.data)
      
      if (response.data.success) {
        return {
          success: true,
          candidatures: response.data.candidatures,
          statistiques: response.data.statistiques
        }
      } else {
        throw new Error(response.data.error || 'Erreur inconnue')
      }
    } catch (err) {
      console.error('Erreur getCandidaturesByDemandeService:', err)
      error.value = err.response?.data?.error || err.message || 'Erreur lors du chargement des candidatures'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const accepterCandidatureService = async (candidatureId, commentaire = '') => {
    console.log('=== STORE: Acceptation candidature service', candidatureId)
    loading.value = true
    error.value = null

    try {
      const response = await axios.put(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures/${candidatureId}/accepter`, {
        commentaire
      })
      
      console.log('Réponse acceptation:', response.data)
      
      if (response.data.success) {
        return {
          success: true,
          message: response.data.message,
          prestataireNom: response.data.prestataireNom,
          nouveauStatutDemande: response.data.nouveauStatutDemande
        }
      } else {
        throw new Error(response.data.error || 'Erreur inconnue')
      }
    } catch (err) {
      console.error('Erreur accepterCandidatureService:', err)
      error.value = err.response?.data?.error || err.message || 'Erreur lors de l\'acceptation'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const refuserCandidatureService = async (candidatureId, commentaire = '') => {
    console.log('=== STORE: Refus candidature service', candidatureId)
    loading.value = true
    error.value = null

    try {
      const response = await axios.put(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures/${candidatureId}/refuser`, {
        commentaire
      })
      
      console.log('Réponse refus:', response.data)
      
      if (response.data.success) {
        return {
          success: true,
          message: response.data.message
        }
      } else {
        throw new Error(response.data.error || 'Erreur inconnue')
      }
    } catch (err) {
      console.error('Erreur refuserCandidatureService:', err)
      error.value = err.response?.data?.error || err.message || 'Erreur lors du refus'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const getStatistiquesCandidaturesService = async (demandeId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/candidatures/demande/${demandeId}/statistiques`)
      
      if (response.data.success) {
        return { success: true, statistiques: response.data.statistiques }
      } else {
        throw new Error(response.data.error || 'Erreur inconnue')
      }
    } catch (err) {
      console.error('Erreur getStatistiquesCandidaturesService:', err)
      error.value = err.response?.data?.error || err.message || 'Erreur lors du chargement des statistiques'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    candidatures,
    loading,
    error,

    postulerPourAnnonce,
    getCandidaturesByAnnonce,
    getCandidaturesByLivreur,
    accepterCandidature,
    refuserCandidature,

    candidaterPartielle,
    getEntrepotsDisponibles,
    getCandidaturesParSegment,
    accepterCandidaturePartielle,

    getCandidaturesByDemandeService,
    accepterCandidatureService,
    refuserCandidatureService,
    getStatistiquesCandidaturesService
  }
})
