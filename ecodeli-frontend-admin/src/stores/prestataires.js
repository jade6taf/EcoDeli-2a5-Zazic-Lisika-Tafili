import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const usePrestataireStore = defineStore('prestataires', () => {
  const prestataires = ref([])
  const validations = ref([])
  const tarifs = ref([])
  const loading = ref(false)
  const error = ref(null)

  const totalPrestataires = computed(() => prestataires.value.length)

  const prestatairesByStatut = computed(() => {
    const statuts = {
      EN_ATTENTE: 0,
      VALIDE: 0,
      REJETE: 0
    }

    validations.value.forEach(validation => {
      if (statuts.hasOwnProperty(validation.statut)) {
        statuts[validation.statut]++
      }
    })

    return statuts
  })

  const stats = computed(() => ({
    totalPrestataires: totalPrestataires.value,
    enAttente: prestatairesByStatut.value.EN_ATTENTE,
    valides: prestatairesByStatut.value.VALIDE,
    rejetes: prestatairesByStatut.value.REJETE
  }))

  const fetchPrestataires = async () => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get('https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/admin/prestataires')
      prestataires.value = response.data
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement des prestataires'
      console.error('Erreur fetchPrestataires:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const fetchPrestataireById = async (id) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/admin/prestataires/${id}`)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement du prestataire'
      console.error('Erreur fetchPrestataireById:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const fetchValidationsPrestataire = async (prestataireId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/admin/prestataires/${prestataireId}/validations`)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement des validations'
      console.error('Erreur fetchValidationsPrestataire:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const validerCategoriePrestataire = async (prestataireId, categorie, statut) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.post(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/admin/prestataires/${prestataireId}/validation`, {
        categorie: categorie,
        statut: statut,
        commentaire: statut === 'VALIDE' ? 'Catégorie validée par l\'administrateur' : 'Catégorie rejetée par l\'administrateur'
      })

      await fetchValidationsPrestataire(prestataireId)

      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de la validation de la catégorie'
      console.error('Erreur validerCategoriePrestataire:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const definirTarifPrestataire = async (prestataireId, categorie, tarifHoraire) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.post(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/admin/prestataires/${prestataireId}/tarif`, {
        categorie: categorie,
        tarifHoraire: tarifHoraire
      })

      const prestataireIndex = prestataires.value.findIndex(p => p.idUtilisateur === prestataireId)
      if (prestataireIndex !== -1) {
        prestataires.value[prestataireIndex].tarifHoraire = tarifHoraire
      }

      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de la définition du tarif'
      console.error('Erreur definirTarifPrestataire:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const fetchJustificatifsPrestataire = async (prestataireId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/admin/prestataires/${prestataireId}/justificatifs`)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement des justificatifs'
      console.error('Erreur fetchJustificatifsPrestataire:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const validerJustificatif = async (justificatifId, statut, commentaire = '') => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.put(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/admin/justificatifs/${justificatifId}/validation`, {
        statut: statut,
        commentaire: commentaire
      })

      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de la validation du justificatif'
      console.error('Erreur validerJustificatif:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const fetchStatsPrestataires = async () => {
    try {
      const response = await axios.get('https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/admin/prestataires/stats')
      return { success: true, data: response.data }
    } catch (err) {
      console.error('Erreur fetchStatsPrestataires:', err)
      return { success: true, data: stats.value }
    }
  }

  const rechercherPrestataires = async (filtres) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.post('https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/admin/prestataires/recherche', filtres)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de la recherche de prestataires'
      console.error('Erreur rechercherPrestataires:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const exporterPrestataires = async (format = 'csv') => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/admin/prestataires/export?format=${format}`, {
        responseType: 'blob'
      })

      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `prestataires_${new Date().toISOString().split('T')[0]}.${format}`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de l\'export des prestataires'
      console.error('Erreur exporterPrestataires:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    prestataires,
    validations,
    tarifs,
    loading,
    error,

    totalPrestataires,
    prestatairesByStatut,
    stats,

    fetchPrestataires,
    fetchPrestataireById,
    fetchValidationsPrestataire,
    validerCategoriePrestataire,
    definirTarifPrestataire,
    fetchJustificatifsPrestataire,
    validerJustificatif,
    fetchStatsPrestataires,
    rechercherPrestataires,
    exporterPrestataires
  }
})