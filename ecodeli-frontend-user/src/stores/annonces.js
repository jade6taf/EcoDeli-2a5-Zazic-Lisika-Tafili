import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useAnnoncesStore = defineStore('annonces', () => {
  const annonces = ref([])
  const loading = ref(false)
  const error = ref(null)

  const totalAnnonces = computed(() => annonces.value.length)

  const fetchAnnonces = async () => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get('https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces')
      annonces.value = response.data
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement des annonces'
      console.error('Erreur fetchAnnonces:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const createAnnonce = async (annonceData) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.post('https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces', annonceData)

      annonces.value.unshift(response.data)

      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de la création de l\'annonce'
      console.error('Erreur createAnnonce:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const calculateDistanceAndPrice = async (adresseDepart, adresseFin) => {
    try {
      const response = await axios.post('https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces/calculate-distance', {
        adresseDepart,
        adresseFin
      })

      return {
        success: true,
        distance: response.data.distance,
        prix: response.data.prix,
        tarifParKm: response.data.tarifParKm
      }
    } catch (err) {
      console.error('Erreur calculateDistanceAndPrice:', err)
      return {
        success: false,
        message: 'Erreur lors du calcul de la distance'
      }
    }
  }

  const fetchUserAnnonces = async (userId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces/user/${userId}`)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement des annonces utilisateur'
      console.error('Erreur fetchUserAnnonces:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const cancelAnnonce = async (annonceId) => {
    loading.value = true
    error.value = null

    try {
      await axios.put(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces/${annonceId}/cancel`)

      const index = annonces.value.findIndex(annonce => annonce.idAnnonce === annonceId)
      if (index !== -1) {
        annonces.value[index].statut = 'ANNULEE'
      }

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de l\'annulation de l\'annonce'
      console.error('Erreur cancelAnnonce:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const fetchAvailableAnnonces = async () => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get('https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces/available')
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement des annonces disponibles'
      console.error('Erreur fetchAvailableAnnonces:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const takeAnnonce = async (annonceId, livreurId) => {
    loading.value = true
    error.value = null

    try {
      await axios.put(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces/${annonceId}/take`, {
        livreurId: livreurId
      })

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de la prise en charge'
      console.error('Erreur takeAnnonce:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const startDelivery = async (annonceId) => {
    loading.value = true
    error.value = null

    try {
      await axios.put(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces/${annonceId}/start-delivery`)

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du démarrage de la livraison'
      console.error('Erreur startDelivery:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const completeDelivery = async (annonceId) => {
    loading.value = true
    error.value = null

    try {
      await axios.put(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces/${annonceId}/complete`)

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de la finalisation de la livraison'
      console.error('Erreur completeDelivery:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const fetchLivreurAnnonces = async (livreurId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces/livreur/${livreurId}`)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement des annonces du livreur'
      console.error('Erreur fetchLivreurAnnonces:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const generateDeliveryCode = async (annonceId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.post(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces/${annonceId}/generate-code`)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors de la génération du code'
      console.error('Erreur generateDeliveryCode:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const validateDeliveryCode = async (annonceId, code) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.post(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces/${annonceId}/validate-code`, { code })
      return { success: response.data.success, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors de la validation du code'
      console.error('Erreur validateDeliveryCode:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const validerLivreursPartielle = async (annonceId) => {
    loading.value = true
    error.value = null

    try {
      await axios.post(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/annonces/partielle/${annonceId}/valider-livreurs`)
      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors de la validation des livreurs'
      console.error('Erreur validerLivreursPartielle:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    annonces,
    loading,
    error,

    totalAnnonces,

    fetchAnnonces,
    createAnnonce,
    calculateDistanceAndPrice,
    fetchUserAnnonces,
    cancelAnnonce,

    fetchAvailableAnnonces,
    takeAnnonce,
    startDelivery,
    completeDelivery,
    fetchLivreurAnnonces,
    generateDeliveryCode,
    validateDeliveryCode,

    validerLivreursPartielle
  }
})
