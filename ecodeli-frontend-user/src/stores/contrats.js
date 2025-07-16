import { defineStore } from 'pinia'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

export const useContratsStore = defineStore('contrats', {
  state: () => ({
    contrat: null,
    loading: false,
    error: null
  }),

  getters: {
    hasContrat: (state) => state.contrat !== null,
    
    statutContrat: (state) => {
      if (!state.contrat) return 'AUCUN_CONTRAT'
      return state.contrat.statut
    },
    
    statutLibelle: (state) => {
      if (!state.contrat) return 'Aucun contrat'
      return state.contrat.statutLibelle
    },
    
    peutDemanderContrat: (state) => {
      return !state.contrat
    },
    
    peutSignerContrat: (state) => {
      return state.contrat && state.contrat.statut === 'CONTRAT_CREE'
    },
    
    contratSigne: (state) => {
      return state.contrat && state.contrat.statut === 'SIGNE_VALIDE'
    }
  },

  actions: {
    async demanderContrat(idCommercant) {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.post(`${API_BASE_URL}/commercants/contrats/demander`, {
          idCommercant
        })
        
        if (response.data.success) {
          this.contrat = response.data.contrat
          return { success: true, message: response.data.message }
        } else {
          throw new Error(response.data.error || 'Erreur lors de la demande')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors de la demande de contrat'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async getContratCommercant(idCommercant) {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.get(`${API_BASE_URL}/commercants/contrats/${idCommercant}`)
        
        if (response.data.success) {
          this.contrat = response.data.contrat
          return { success: true, contrat: response.data.contrat }
        } else {
          throw new Error(response.data.error || 'Erreur lors de la récupération')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors de la récupération du contrat'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async signerContrat(idContrat, idCommercant) {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.post(`${API_BASE_URL}/commercants/contrats/${idContrat}/signer`, {
          idCommercant,
          acceptation: true
        })
        
        if (response.data.success) {
          this.contrat = response.data.contrat
          return { success: true, message: response.data.message }
        } else {
          throw new Error(response.data.error || 'Erreur lors de la signature')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors de la signature du contrat'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async getStatutContrat(idCommercant) {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.get(`${API_BASE_URL}/commercants/contrats/${idCommercant}/statut`)
        
        if (response.data.success) {
          if (response.data.contrat) {
            this.contrat = response.data.contrat
          }
          return {
            success: true,
            statut: response.data.statut,
            libelle: response.data.libelle,
            peutDemanderContrat: response.data.peutDemanderContrat
          }
        } else {
          throw new Error(response.data.error || 'Erreur lors de la vérification du statut')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors de la vérification du statut'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    clearError() {
      this.error = null
    },

    clearContrat() {
      this.contrat = null
      this.error = null
    }
  }
})