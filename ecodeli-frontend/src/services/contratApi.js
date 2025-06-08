import { authStore } from '@/store/auth'

const API_BASE_URL = 'http://localhost:8080/api'

class ContratApiService {
  async getContratsCommercant(idCommercant) {
    const response = await fetch(`${API_BASE_URL}/commercants/${idCommercant}/contrats`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error('Erreur lors de la récupération des contrats')
    }

    return await response.json()
  }

  async getContratCommercant(idCommercant, idContrat) {
    const response = await fetch(`${API_BASE_URL}/commercants/${idCommercant}/contrats/${idContrat}`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      if (response.status === 404) {
        throw new Error('Contrat non trouvé')
      }
      throw new Error('Erreur lors de la récupération du contrat')
    }

    return await response.json()
  }

  async accepterContrat(idCommercant, idContrat) {
    const response = await fetch(`${API_BASE_URL}/commercants/${idCommercant}/contrats/${idContrat}/accept`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || 'Erreur lors de la signature du contrat')
    }

    return await response.json()
  }

  async refuserContrat(idCommercant, idContrat, motif) {
    const response = await fetch(`${API_BASE_URL}/commercants/${idCommercant}/contrats/${idContrat}/refuse`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ motif })
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || 'Erreur lors du refus du contrat')
    }

    return await response.json()
  }

  async getHistoriqueContrat(idCommercant, idContrat) {
    const response = await fetch(`${API_BASE_URL}/commercants/${idCommercant}/contrats/${idContrat}/historique`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error('Erreur lors de la récupération de l\'historique')
    }

    return await response.json()
  }

  async getStatistiquesCommercant(idCommercant) {
    const response = await fetch(`${API_BASE_URL}/commercants/${idCommercant}/contrats/statistiques`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error('Erreur lors de la récupération des statistiques')
    }

    return await response.json()
  }

  async getAllContrats() {
    const response = await fetch(`${API_BASE_URL}/admin/contrats`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error('Erreur lors de la récupération des contrats')
    }

    return await response.json()
  }

  async getContratsByStatut(statut) {
    const response = await fetch(`${API_BASE_URL}/admin/contrats/statut/${statut}`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error('Erreur lors de la récupération des contrats')
    }

    return await response.json()
  }

  async createContrat(contrat, idAdmin) {
    const response = await fetch(`${API_BASE_URL}/admin/contrats?idAdmin=${idAdmin}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(contrat)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || 'Erreur lors de la création du contrat')
    }

    return await response.json()
  }

  async updateContrat(idContrat, contrat, idAdmin) {
    const response = await fetch(`${API_BASE_URL}/admin/contrats/${idContrat}?idAdmin=${idAdmin}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(contrat)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || 'Erreur lors de la modification du contrat')
    }

    return await response.json()
  }

  async envoyerPourSignature(idContrat, idAdmin) {
    const response = await fetch(`${API_BASE_URL}/admin/contrats/${idContrat}/send?idAdmin=${idAdmin}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || 'Erreur lors de l\'envoi du contrat')
    }

    return await response.json()
  }

  async validerContrat(idContrat, idAdmin) {
    const response = await fetch(`${API_BASE_URL}/admin/contrats/${idContrat}/validate?idAdmin=${idAdmin}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || 'Erreur lors de la validation du contrat')
    }

    return await response.json()
  }

  async refuserContratAdmin(idContrat, idAdmin, motif) {
    const response = await fetch(`${API_BASE_URL}/admin/contrats/${idContrat}/refuse?idAdmin=${idAdmin}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ motif })
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || 'Erreur lors du refus du contrat')
    }

    return await response.json()
  }

  async deleteContrat(idContrat, idAdmin) {
    const response = await fetch(`${API_BASE_URL}/admin/contrats/${idContrat}?idAdmin=${idAdmin}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || 'Erreur lors de la suppression du contrat')
    }
  }

  async getStatistiquesAdmin() {
    const response = await fetch(`${API_BASE_URL}/admin/contrats/statistiques`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error('Erreur lors de la récupération des statistiques')
    }

    return await response.json()
  }

  getStatutLabel(statut) {
    const labels = {
      'BROUILLON': 'Brouillon',
      'ATTENTE_SIGNATURE_COMMERCANT': 'En attente de votre signature',
      'ATTENTE_SIGNATURE_ADMIN': 'En attente de validation',
      'ACTIF': 'Actif',
      'REFUSE': 'Refusé',
      'RESILIE': 'Résilié'
    }
    return labels[statut] || statut
  }

  getStatutBadgeClass(statut) {
    const classes = {
      'BROUILLON': 'badge-secondary',
      'ATTENTE_SIGNATURE_COMMERCANT': 'badge-warning',
      'ATTENTE_SIGNATURE_ADMIN': 'badge-info',
      'ACTIF': 'badge-success',
      'REFUSE': 'badge-danger',
      'RESILIE': 'badge-danger'
    }
    return classes[statut] || 'badge-default'
  }
}

export default new ContratApiService()
