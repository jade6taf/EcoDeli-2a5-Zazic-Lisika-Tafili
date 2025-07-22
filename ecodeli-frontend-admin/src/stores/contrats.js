import { defineStore } from 'pinia'
import axios from 'axios'

const API_BASE_URL = 'https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/admin/contrats'

export const useContratsAdminStore = defineStore('contratsAdmin', {
  state: () => ({
    contrats: [],
    templates: [],
    contratActuel: null,
    templateActuel: null,
    loading: false,
    error: null,
    statistiques: {}
  }),

  getters: {
    contratsParStatut: (state) => (statut) => {
      if (!statut) return state.contrats
      return state.contrats.filter(contrat => contrat.statut === statut)
    },
    
    templatesActifs: (state) => {
      return state.templates.filter(template => template.actif)
    },
    
    nombreContratsParStatut: (state) => {
      const stats = {}
      state.contrats.forEach(contrat => {
        stats[contrat.statut] = (stats[contrat.statut] || 0) + 1
      })
      return stats
    }
  },

  actions: {
    async chargerContrats(statut = null) {
      this.loading = true
      this.error = null
      
      try {
        const params = statut ? { statut } : {}
        const response = await axios.get(API_BASE_URL, { params })
        
        if (response.data.success) {
          this.contrats = response.data.contrats
          return { success: true, contrats: response.data.contrats }
        } else {
          throw new Error(response.data.error || 'Erreur lors du chargement')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors du chargement des contrats'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async chargerContrat(idContrat) {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.get(`${API_BASE_URL}/${idContrat}`)
        
        if (response.data.success) {
          this.contratActuel = response.data.contrat
          return { success: true, contrat: response.data.contrat }
        } else {
          throw new Error(response.data.error || 'Erreur lors du chargement')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors du chargement du contrat'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async creerContrat(idContrat, contenuContrat, idAdmin, commentaire = '') {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.post(`${API_BASE_URL}/${idContrat}/creer`, {
          contenuContrat,
          idAdmin,
          commentaire
        })
        
        if (response.data.success) {
          const index = this.contrats.findIndex(c => c.idContrat === idContrat)
          if (index !== -1) {
            this.contrats[index] = response.data.contrat
          }
          this.contratActuel = response.data.contrat
          return { success: true, message: response.data.message, contrat: response.data.contrat }
        } else {
          throw new Error(response.data.error || 'Erreur lors de la création')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors de la création du contrat'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async mettreAJourStatutContrat(idContrat, statut, commentaire = '') {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.put(`${API_BASE_URL}/${idContrat}/statut`, {
          statut,
          commentaire
        })
        
        if (response.data.success) {
          const index = this.contrats.findIndex(c => c.idContrat === idContrat)
          if (index !== -1) {
            this.contrats[index] = response.data.contrat
          }
          this.contratActuel = response.data.contrat
          return { success: true, message: response.data.message, contrat: response.data.contrat }
        } else {
          throw new Error(response.data.error || 'Erreur lors de la mise à jour')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors de la mise à jour du statut'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async supprimerContrat(idContrat) {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.delete(`${API_BASE_URL}/${idContrat}`)
        
        if (response.data.success) {
          this.contrats = this.contrats.filter(c => c.idContrat !== idContrat)
          if (this.contratActuel?.idContrat === idContrat) {
            this.contratActuel = null
          }
          return { success: true, message: response.data.message }
        } else {
          throw new Error(response.data.error || 'Erreur lors de la suppression')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors de la suppression du contrat'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async chargerTemplates(actifsSeuls = false) {
      this.loading = true
      this.error = null
      
      try {
        const params = { actifsSeuls }
        const response = await axios.get(`${API_BASE_URL}/templates`, { params })
        
        if (response.data.success) {
          this.templates = response.data.templates
          return { success: true, templates: response.data.templates }
        } else {
          throw new Error(response.data.error || 'Erreur lors du chargement')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors du chargement des templates'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async chargerTemplate(idTemplate) {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.get(`${API_BASE_URL}/templates/${idTemplate}`)
        
        if (response.data.success) {
          this.templateActuel = response.data.template
          return { success: true, template: response.data.template }
        } else {
          throw new Error(response.data.error || 'Erreur lors du chargement')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors du chargement du template'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async creerTemplate(nomTemplate, contenuTemplate, description, idAdmin) {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.post(`${API_BASE_URL}/templates`, {
          nomTemplate,
          contenuTemplate,
          description,
          idAdmin
        })
        
        if (response.data.success) {
          this.templates.unshift(response.data.template)
          return { success: true, message: response.data.message, template: response.data.template }
        } else {
          throw new Error(response.data.error || 'Erreur lors de la création')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors de la création du template'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async mettreAJourTemplate(idTemplate, updates) {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.put(`${API_BASE_URL}/templates/${idTemplate}`, updates)
        
        if (response.data.success) {
          const index = this.templates.findIndex(t => t.idTemplate === idTemplate)
          if (index !== -1) {
            this.templates[index] = response.data.template
          }
          this.templateActuel = response.data.template
          return { success: true, message: response.data.message, template: response.data.template }
        } else {
          throw new Error(response.data.error || 'Erreur lors de la mise à jour')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors de la mise à jour du template'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async supprimerTemplate(idTemplate) {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.delete(`${API_BASE_URL}/templates/${idTemplate}`)
        
        if (response.data.success) {
          this.templates = this.templates.filter(t => t.idTemplate !== idTemplate)
          if (this.templateActuel?.idTemplate === idTemplate) {
            this.templateActuel = null
          }
          return { success: true, message: response.data.message }
        } else {
          throw new Error(response.data.error || 'Erreur lors de la suppression')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors de la suppression du template'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    async chargerStatistiques() {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.get(`${API_BASE_URL}/statistiques`)
        
        if (response.data.success) {
          this.statistiques = response.data.statistiques
          return { success: true, statistiques: response.data.statistiques }
        } else {
          throw new Error(response.data.error || 'Erreur lors du chargement')
        }
      } catch (error) {
        const message = error.response?.data?.error || error.message || 'Erreur lors du chargement des statistiques'
        this.error = message
        return { success: false, error: message }
      } finally {
        this.loading = false
      }
    },

    clearError() {
      this.error = null
    },

    resetState() {
      this.contrats = []
      this.templates = []
      this.contratActuel = null
      this.templateActuel = null
      this.error = null
      this.statistiques = {}
    }
  }
})