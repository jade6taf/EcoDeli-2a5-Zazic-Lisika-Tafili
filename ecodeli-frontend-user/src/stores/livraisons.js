import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useLivraisonsStore = defineStore('livraisons', () => {
  const livraisons = ref([])
  const loading = ref(false)
  const error = ref(null)

  const fetchLivraisonsByLivreur = async (livreurId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`http://localhost:8080/api/livraisons/livreur/${livreurId}`)
      livraisons.value = response.data
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors du chargement des livraisons'
      console.error('Erreur fetchLivraisonsByLivreur:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const startLivraison = async (livraisonId, livreurId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.put(`http://localhost:8080/api/livraisons/${livraisonId}/start`, {
        livreurId
      })
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors du démarrage de la livraison'
      console.error('Erreur startLivraison:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const completeLivraison = async (livraisonId, livreurId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.put(`http://localhost:8080/api/livraisons/${livraisonId}/complete`, {
        livreurId
      })
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors de la finalisation de la livraison'
      console.error('Erreur completeLivraison:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const validateOTP = async (livraisonId, otp) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.post(`http://localhost:8080/api/livraisons/${livraisonId}/validate-otp`, {
        otp
      })
      return { success: response.data.success, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors de la validation OTP'
      console.error('Erreur validateOTP:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const getLivraisonDetails = async (livraisonId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`http://localhost:8080/api/livraisons/${livraisonId}`)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors du chargement des détails'
      console.error('Erreur getLivraisonDetails:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    livraisons,
    loading,
    error,

    fetchLivraisonsByLivreur,
    startLivraison,
    completeLivraison,
    validateOTP,
    getLivraisonDetails
  }
})
