import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

class PaymentService {
  constructor() {
    this.apiClient = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json'
      }
    })

    this.apiClient.interceptors.request.use(
      config => {
        const token = localStorage.getItem('token')

        if (!token) {
          throw new Error('Token manquant. Veuillez vous reconnecter.')
        }

        config.headers.Authorization = `Bearer ${token}`
        return config
      },
      error => Promise.reject(error)
    )

    this.apiClient.interceptors.response.use(
      response => response,
      error => {
        if (error.response) {
          if (error.response.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            window.location.href = '/login'
            return Promise.reject(new Error('Session expirée. Veuillez vous reconnecter.'))
          }
          if (error.response.status === 403) {
            return Promise.reject(new Error('Accès non autorisé pour cette ressource.'))
          }
        }
        return Promise.reject(error)
      }
    )
  }

  async createPaymentIntent(annonceId, clientId, montant) {
    try {
      const response = await this.apiClient.post('/payments/create-intent', {
        annonceId,
        clientId,
        montant
      })
      return response.data
    } catch (error) {
      console.error('Erreur lors de la création du Payment Intent:', error)
      throw this.handleError(error)
    }
  }

  async confirmPayment(paymentIntentId) {
    try {
      const response = await this.apiClient.post('/payments/confirm', {
        paymentIntentId
      })
      return response.data
    } catch (error) {
      console.error('Erreur lors de la confirmation du paiement:', error)
      throw this.handleError(error)
    }
  }

  async refundPayment(paymentIntentId, reason = 'Annulation de commande') {
    try {
      const response = await this.apiClient.post('/payments/refund', {
        paymentIntentId,
        reason
      })
      return response.data
    } catch (error) {
      console.error('Erreur lors du remboursement:', error)
      throw this.handleError(error)
    }
  }

  async getPaymentInfo(annonceId) {
    try {
      const response = await this.apiClient.get(`/payments/annonce/${annonceId}`)
      return response.data
    } catch (error) {
      console.error('Erreur lors de la récupération des infos de paiement:', error)
      throw this.handleError(error)
    }
  }

  async getWalletBalance(livreurId) {
    try {
      const response = await this.apiClient.get(`/wallet/balance/${livreurId}`)
      return response.data
    } catch (error) {
      console.error('Erreur lors de la récupération du solde:', error)
      throw this.handleError(error)
    }
  }

  async updateBankInfo(livreurId, iban, nomTitulaire) {
    try {
      const response = await this.apiClient.put(`/wallet/bank-info/${livreurId}`, {
        iban,
        nomTitulaire
      })
      return response.data
    } catch (error) {
      console.error('Erreur lors de la mise à jour des infos bancaires:', error)
      throw this.handleError(error)
    }
  }

  async getTransactionHistory(livreurId, page = 0, size = 20) {
    try {
      const response = await this.apiClient.get(`/wallet/transactions/${livreurId}`, {
        params: { page, size }
      })
      return response.data
    } catch (error) {
      console.error('Erreur lors de la récupération de l\'historique:', error)
      throw this.handleError(error)
    }
  }

  async getWalletStats(livreurId) {
    try {
      const response = await this.apiClient.get(`/wallet/stats/${livreurId}`)
      return response.data
    } catch (error) {
      console.error('Erreur lors de la récupération des statistiques:', error)
      throw this.handleError(error)
    }
  }

  async canWithdraw(livreurId, montant) {
    try {
      const response = await this.apiClient.get(`/wallet/can-withdraw/${livreurId}`, {
        params: { montant }
      })
      return response.data
    } catch (error) {
      console.error('Erreur lors de la vérification de retrait:', error)
      throw this.handleError(error)
    }
  }

  async requestWithdrawal(livreurId, montant, iban, nomTitulaire) {
    try {
      const response = await this.apiClient.post('/withdrawals/request', {
        livreurId,
        montant,
        iban,
        nomTitulaire
      })
      return response.data
    } catch (error) {
      console.error('Erreur lors de la demande de retrait:', error)
      throw this.handleError(error)
    }
  }

  async getWithdrawalHistory(livreurId, page = 0, size = 20) {
    try {
      const response = await this.apiClient.get(`/withdrawals/history/${livreurId}`, {
        params: { page, size }
      })
      return response.data
    } catch (error) {
      console.error('Erreur lors de la récupération de l\'historique des retraits:', error)
      throw this.handleError(error)
    }
  }

  async cancelWithdrawal(retraitId, reason = 'Annulé par l\'utilisateur') {
    try {
      const response = await this.apiClient.post(`/withdrawals/${retraitId}/cancel`, {
        reason
      })
      return response.data
    } catch (error) {
      console.error('Erreur lors de l\'annulation du retrait:', error)
      throw this.handleError(error)
    }
  }

  handleError(error) {
    if (error.response) {
      const { status, data } = error.response

      if (status === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
        return new Error('Session expirée. Veuillez vous reconnecter.')
      }

      if (status === 403) {
        return new Error('Accès interdit.')
      }

      if (data && data.error) {
        return new Error(data.error)
      }

      return new Error(`Erreur ${status}: ${error.response.statusText}`)
    }

    if (error.request) {
      return new Error('Erreur de connexion. Vérifiez votre connexion internet.')
    }

    return new Error('Erreur technique. Veuillez réessayer.')
  }

  formatAmount(amount) {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 2
    }).format(amount)
  }

  validateIban(iban) {
    if (!iban || typeof iban !== 'string') {
      return false
    }

    const cleanIban = iban.replace(/\s+/g, '').trim()

    return cleanIban.length >= 8
  }

  formatIban(iban) {
    const cleanIban = iban.replace(/\s+/g, '').toUpperCase()
    return cleanIban.replace(/(.{4})/g, '$1 ').trim()
  }
}

export default new PaymentService()
