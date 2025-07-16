import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useServicesStore = defineStore('services', () => {
  const demandesServices = ref([])
  const loading = ref(false)
  const error = ref(null)
  const categories = ref([])

  const chargerCategories = async () => {
    try {
      const categoriesFiles = [
        'transport-livraison',
        'services-domicile', 
        'travaux-reparations',
        'courses-achats',
        'services-personnels',
        'education-formation'
      ]
      
      const categoriesPromises = categoriesFiles.map(async (file) => {
        const response = await fetch(`/src/data/service-categories/${file}.json`)
        return await response.json()
      })
      
      categories.value = await Promise.all(categoriesPromises)
      return { success: true, data: categories.value }
    } catch (err) {
      error.value = err.message
      return { success: false, error: err.message }
    }
  }

  const creerDemandeService = async (demande) => {
    console.log('=== FRONTEND: Début création demande ===')
    console.log('Données complètes envoyées:', JSON.stringify(demande, null, 2))
    
    loading.value = true
    error.value = null
    
    try {
      console.log('Envoi requête POST vers backend...')
      const response = await axios.post('http://localhost:8080/api/demandes-service', demande)
      
      console.log('Réponse reçue:', response.status, response.data)
      
      if (response.status === 200 || response.status === 201) {
        const nouvelleDemande = response.data
        demandesServices.value.push(nouvelleDemande)
        console.log('Demande créée avec succès!')
        return { success: true, data: nouvelleDemande }
      }
    } catch (err) {
      console.error('=== ERREUR FRONTEND ===')
      console.error('Status:', err.response?.status)
      console.error('Status Text:', err.response?.statusText)
      console.error('Response data:', err.response?.data)
      console.error('Error message:', err.message)
      console.error('Full error:', err)
      
      error.value = err.response?.data?.error || err.response?.data?.message || err.message
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const chargerMesDemandesServices = async (clientId) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await axios.get(`http://localhost:8080/api/demandes-service/client/${clientId}`)
      
      if (response.status === 200) {
        demandesServices.value = response.data
        return { success: true, data: response.data }
      }
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const mettreAJourDemandeService = async (id, donnees) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await axios.put(`http://localhost:8080/api/demandes-service/${id}`, donnees)
      
      if (response.status === 200) {
        const index = demandesServices.value.findIndex(d => d.idDemande === id)
        if (index !== -1) {
          demandesServices.value[index] = response.data
        }
        return { success: true, data: response.data }
      }
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const annulerDemandeService = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await axios.delete(`http://localhost:8080/api/demandes-service/${id}`)
      
      if (response.status === 200) {
        const index = demandesServices.value.findIndex(d => d.idDemande === id)
        if (index !== -1) {
          demandesServices.value[index].statut = 'ANNULEE'
        }
        return { success: true, message: 'Demande annulée avec succès' }
      }
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const getDemandeServiceById = async (demandeId) => {
    console.log('=== STORE: Récupération demande service', demandeId)
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`http://localhost:8080/api/demandes-service/${demandeId}`)
      
      console.log('Demande récupérée:', response.data)
      
      if (response.status === 200) {
        return { success: true, data: response.data }
      }
    } catch (err) {
      console.error('Erreur getDemandeServiceById:', err)
      error.value = err.response?.data?.message || err.message
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const getCategorieById = (id) => {
    return categories.value.find(cat => cat.id === id)
  }

  const getCategoriesByIds = (ids) => {
    return categories.value.filter(cat => ids.includes(cat.id))
  }

  const resetStore = () => {
    demandesServices.value = []
    error.value = null
    loading.value = false
  }

  return {
    demandesServices,
    loading,
    error,
    categories,
    
    chargerCategories,
    creerDemandeService,
    chargerMesDemandesServices,
    mettreAJourDemandeService,
    annulerDemandeService,
    getDemandeServiceById,
    
    getCategorieById,
    getCategoriesByIds,
    resetStore
  }
})