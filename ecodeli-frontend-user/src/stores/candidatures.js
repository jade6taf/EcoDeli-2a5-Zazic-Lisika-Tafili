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
      const response = await axios.post('http://localhost:8080/api/candidatures-livraison', {
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
      const response = await axios.get(`http://localhost:8080/api/candidatures-livraison/annonce/${annonceId}`)
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
      const response = await axios.get(`http://localhost:8080/api/candidatures-livraison/livreur/${livreurId}`)
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
      await axios.put(`http://localhost:8080/api/candidatures-livraison/${candidatureId}/accepter`, {
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
      await axios.put(`http://localhost:8080/api/candidatures-livraison/${candidatureId}/refuser`, {
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
      const response = await axios.post('http://localhost:8080/api/candidatures-livraison/partielle', {
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
      const response = await axios.get('http://localhost:8080/api/candidatures-livraison/entrepots')
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
      const response = await axios.get(`http://localhost:8080/api/candidatures-livraison/annonce/${annonceId}/segments`)
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
      await axios.put(`http://localhost:8080/api/candidatures-livraison/${candidatureId}/accepter-partielle`, {
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
    accepterCandidaturePartielle
  }
})
