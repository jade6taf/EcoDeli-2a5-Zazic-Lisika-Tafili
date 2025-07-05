<script>
import paymentService from '@/services/paymentService.js'

export default {
  name: 'WalletDashboard',
  props: {
    livreurId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      wallet: {
        soldeDisponible: 0,
        soldeEnAttente: 0,
        soldeTotal: 0,
        ibanConfigured: false,
        iban: '',
        nomTitulaire: ''
      },
      stats: null,
      transactions: [],
      withdrawals: [],
      loading: false,
      loadingTransactions: false,
      loadingWithdrawals: false,
      hasMoreTransactions: false,
      currentPage: 0,
      showWithdrawModal: false,
      showBankModal: false,
      withdrawAmount: 1,
      bankForm: {
        iban: '',
        nomTitulaire: ''
      },
      message: '',
      messageType: 'info',
      canWithdraw: false
    }
  },
  computed: {
    isValidIban() {
      return paymentService.validateIban(this.bankForm.iban)
    },
    isValidBankForm() {
      return this.bankForm.iban.trim().length >= 8 &&
             this.bankForm.nomTitulaire.trim().length >= 2
    },
    canRequestWithdrawal() {
      return this.wallet.ibanConfigured &&
             this.withdrawAmount >= 1 &&
             this.withdrawAmount <= this.wallet.soldeDisponible
    }
  },
  async mounted() {
    await this.loadWalletData()
  },
  beforeMount() {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      this.showMessage('Session invalide. Veuillez vous reconnecter.', 'error')
      this.$router.push('/login')
      return
    }

    const user = JSON.parse(userStr)
    if (user.type !== 'LIVREUR' || user.idUtilisateur !== this.livreurId) {
      this.showMessage('Accès non autorisé. Page réservée aux livreurs.', 'error')
      this.$router.push('/dashboard')
      return
    }
  },
  methods: {
    async loadWalletData() {
    this.loading = true
    try {
      const walletData = await paymentService.getWalletBalance(this.livreurId)
      this.wallet = walletData

      this.loadOptionalData()

    } catch (error) {
      if (error.message.includes('Token manquant')) {
        this.$router.push('/login')
      } else {
        this.showMessage('Erreur lors du chargement du wallet: ' + error.message, 'error')
      }
    } finally {
      this.loading = false
    }
  },

  async loadOptionalData() {
    try {
      this.stats = await paymentService.getWalletStats(this.livreurId)
    } catch (error) {
      console.warn('Impossible de charger les statistiques:', error.message)
    }
    try {
      await this.loadTransactions()
    } catch (error) {
      console.warn('Impossible de charger les transactions:', error.message)
    }

    try {
      await this.loadWithdrawals()
    } catch (error) {
      console.warn('Impossible de charger les retraits:', error.message)
    }

    try {
      if (this.wallet.soldeDisponible >= 10) {
        const canWithdrawData = await paymentService.canWithdraw(this.livreurId, 10)
        this.canWithdraw = canWithdrawData.canWithdraw
      } else {
        this.canWithdraw = true
      }
    } catch (error) {
      console.warn('Impossible de vérifier les retraits, activation par défaut')
      this.canWithdraw = true
    }
  },

    async loadTransactions() {
      this.loadingTransactions = true
      try {
        const data = await paymentService.getTransactionHistory(this.livreurId, this.currentPage, 10)
        if (this.currentPage === 0) {
          this.transactions = data.transactions
        } else {
          this.transactions.push(...data.transactions)
        }
        this.hasMoreTransactions = data.hasNext
      } catch (error) {
        this.showMessage('Erreur lors du chargement des transactions: ' + error.message, 'error')
      } finally {
        this.loadingTransactions = false
      }
    },

    async loadWithdrawals() {
      this.loadingWithdrawals = true
      try {
        const data = await paymentService.getWithdrawalHistory(this.livreurId, 0, 10)
        this.withdrawals = data.retraits
      } catch (error) {
        this.showMessage('Erreur lors du chargement des retraits: ' + error.message, 'error')
      } finally {
        this.loadingWithdrawals = false
      }
    },

    async loadMoreTransactions() {
      this.currentPage++
      await this.loadTransactions()
    },

    async refreshWallet() {
      await this.loadWalletData()
      this.showMessage('Wallet actualisé', 'success')
    },

    async requestWithdrawal() {
      try {
        await paymentService.requestWithdrawal(
          this.livreurId,
          this.withdrawAmount,
          this.wallet.iban,
          this.wallet.nomTitulaire
        )
        this.showWithdrawModal = false
        this.withdrawAmount = 1
        this.showMessage('Demande de retrait créée avec succès', 'success')

        await this.loadWalletData()

      } catch (error) {
        this.showMessage('Erreur lors de la demande de retrait: ' + error.message, 'error')
      }
    },

    async updateBankInfo() {
      try {
        const response = await paymentService.updateBankInfo(
          this.livreurId,
          this.bankForm.iban,
          this.bankForm.nomTitulaire
        )

        if (response.wallet) {
          this.wallet.iban = response.wallet.iban || this.bankForm.iban
          this.wallet.nomTitulaire = response.wallet.nomTitulaire || this.bankForm.nomTitulaire
          this.wallet.ibanConfigured = true
        } else {
          // Fallback : mise à jour manuelle
          this.wallet.iban = this.bankForm.iban
          this.wallet.nomTitulaire = this.bankForm.nomTitulaire
          this.wallet.ibanConfigured = true
        }
        this.canWithdraw = true
        this.showBankModal = false
        this.showMessage('Informations bancaires mises à jour avec succès ! Vous pouvez maintenant effectuer des retraits.', 'success')

      } catch (error) {
        this.showMessage('Erreur lors de la mise à jour: ' + error.message, 'error')
      }
    },

    async cancelWithdrawal(retraitId) {
      if (!confirm('Êtes-vous sûr de vouloir annuler ce retrait ?')) return

      try {
        await paymentService.cancelWithdrawal(retraitId)
        this.showMessage('Retrait annulé', 'success')
        await this.loadWithdrawals()
      } catch (error) {
        this.showMessage('Erreur lors de l\'annulation: ' + error.message, 'error')
      }
    },

    formatAmount(amount) {
      return paymentService.formatAmount(amount)
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    getStatusLabel(status) {
      const labels = {
        'PENDING': 'En attente',
        'PROCESSING': 'En cours',
        'COMPLETED': 'Terminé',
        'FAILED': 'Échoué',
        'CANCELLED': 'Annulé'
      }
      return labels[status] || status
    },

    showMessage(text, type = 'info') {
      this.message = text
      this.messageType = type
      setTimeout(() => {
        this.message = ''
      }, 5000)
    }
  },

  watch: {
    showBankModal(newValue) {
      if (newValue) {
        this.bankForm.iban = this.wallet.iban || ''
        this.bankForm.nomTitulaire = this.wallet.nomTitulaire || ''
      }
    }
  }
}
</script>

<template>
  <div class="wallet-dashboard">
    <!-- En-tête du wallet -->
    <div class="wallet-header">
      <h2>💳 Wallet EcoDeli</h2>
      <p class="wallet-subtitle">💡 Système de retrait interne sécurisé</p>
      <div class="wallet-summary">
        <div class="balance-card">
          <div class="balance-item">
            <span class="balance-label">Solde disponible</span>
            <span class="balance-amount available">{{ formatAmount(wallet.soldeDisponible) }}</span>
          </div>
          <div class="balance-item">
            <span class="balance-label">En attente</span>
            <span class="balance-amount pending">{{ formatAmount(wallet.soldeEnAttente) }}</span>
          </div>
          <div class="balance-item total">
            <span class="balance-label">Total</span>
            <span class="balance-amount">{{ formatAmount(wallet.soldeTotal) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Actions rapides -->
    <div class="quick-actions">
      <button
        @click="showWithdrawModal = true"
        class="btn-withdraw"
        :disabled="!wallet.ibanConfigured"
      >
        📤 Demander un retrait
      </button>

      <button @click="showBankModal = true" class="btn-bank-info">
        🏦 {{ wallet.ibanConfigured ? 'Modifier IBAN' : 'Configurer IBAN' }}
      </button>

      <button @click="refreshWallet" class="btn-refresh" :disabled="loading">
        🔄 Actualiser
      </button>
    </div>

    <!-- Informations bancaires -->
    <div v-if="wallet.ibanConfigured" class="bank-info">
      <h3>Informations bancaires</h3>
      <p>
        <strong>IBAN:</strong> {{ wallet.iban }}<br>
        <strong>Titulaire:</strong> {{ wallet.nomTitulaire }}
      </p>
    </div>

    <div v-else class="bank-warning">
      ⚠️ Vous devez configurer votre IBAN pour pouvoir effectuer des retraits.
    </div>

    <!-- Statistiques -->
    <div v-if="stats" class="wallet-stats">
      <h3>📊 Statistiques</h3>
      <div class="stats-grid">
        <div class="stat-item">
          <span class="stat-value">{{ stats.nombreLivraisons }}</span>
          <span class="stat-label">Livraisons</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ formatAmount(stats.totalGains) }}</span>
          <span class="stat-label">Total gagné</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ formatAmount(stats.totalRetraits) }}</span>
          <span class="stat-label">Total retiré</span>
        </div>
      </div>
    </div>

    <!-- Historique des transactions -->
    <div class="transaction-history">
      <h3>📋 Historique des transactions</h3>

      <div v-if="loadingTransactions" class="loading">
        🔄 Chargement des transactions...
      </div>

      <div v-else-if="transactions.length === 0" class="no-transactions">
        Aucune transaction pour le moment.
      </div>

      <div v-else class="transactions-list">
        <div
          v-for="transaction in transactions"
          :key="transaction.idTransaction"
          class="transaction-item"
          :class="{ 'credit': transaction.montant > 0, 'debit': transaction.montant < 0 }"
        >
          <div class="transaction-info">
            <span class="transaction-description">{{ transaction.description }}</span>
            <span class="transaction-date">{{ formatDate(transaction.dateTransaction) }}</span>
          </div>
          <span class="transaction-amount">
            {{ transaction.montant > 0 ? '+' : '' }}{{ formatAmount(transaction.montant) }}
          </span>
        </div>
      </div>

      <button
        v-if="hasMoreTransactions"
        @click="loadMoreTransactions"
        class="btn-load-more"
        :disabled="loadingTransactions"
      >
        Voir plus
      </button>
    </div>

    <!-- Historique des retraits -->
    <div class="withdrawal-history">
      <h3>💳 Historique des retraits</h3>

      <div v-if="loadingWithdrawals" class="loading">
        🔄 Chargement des retraits...
      </div>

      <div v-else-if="withdrawals.length === 0" class="no-withdrawals">
        Aucun retrait pour le moment.
      </div>

      <div v-else class="withdrawals-list">
        <div
          v-for="withdrawal in withdrawals"
          :key="withdrawal.idRetrait"
          class="withdrawal-item"
          :class="withdrawal.statut.toLowerCase()"
        >
          <div class="withdrawal-info">
            <span class="withdrawal-amount">{{ formatAmount(withdrawal.montant) }}</span>
            <span class="withdrawal-date">{{ formatDate(withdrawal.dateDemande) }}</span>
          </div>
          <div class="withdrawal-status">
            <span class="status-badge" :class="withdrawal.statut.toLowerCase()">
              {{ getStatusLabel(withdrawal.statut) }}
            </span>
            <button
              v-if="withdrawal.statut === 'PENDING'"
              @click="cancelWithdrawal(withdrawal.idRetrait)"
              class="btn-cancel-small"
            >
              Annuler
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de retrait -->
    <div v-if="showWithdrawModal" class="modal-overlay" @click="showWithdrawModal = false">
      <div class="modal-content" @click.stop>
        <h3>💸 Demander un retrait</h3>

        <div class="withdraw-form">
          <div class="form-group">
            <label>Montant à retirer (minimum 1€)</label>
            <input
              v-model.number="withdrawAmount"
              type="number"
              min="1"
              :max="wallet.soldeDisponible"
              step="0.01"
              class="form-input"
            >
            <small>Solde disponible: {{ formatAmount(wallet.soldeDisponible) }}</small>
          </div>

          <div v-if="!wallet.ibanConfigured" class="warning">
            ⚠️ Vous devez d'abord configurer votre IBAN.
          </div>

          <div class="modal-actions">
            <button
              @click="requestWithdrawal"
              :disabled="!canRequestWithdrawal"
              class="btn-confirm"
            >
              Confirmer le retrait
            </button>
            <button @click="showWithdrawModal = false" class="btn-cancel">
              Annuler
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal IBAN -->
    <div v-if="showBankModal" class="modal-overlay" @click="showBankModal = false">
      <div class="modal-content" @click.stop>
        <h3>🏦 Informations bancaires</h3>

        <div class="bank-form">
          <div class="form-group">
            <label>IBAN</label>
            <input
              v-model="bankForm.iban"
              type="text"
              placeholder="Saisissez votre IBAN (ex: FR1234567890123456)"
              class="form-input"
            >
            <small class="info">
              💡 Pour les retraits EcoDeli, format libre accepté
            </small>
          </div>

          <div class="form-group">
            <label>Nom du titulaire</label>
            <input
              v-model="bankForm.nomTitulaire"
              type="text"
              placeholder="Prénom NOM"
              class="form-input"
            >
          </div>

          <div class="modal-actions">
            <button
              @click="updateBankInfo"
              :disabled="!isValidBankForm"
              class="btn-confirm"
            >
              Enregistrer
            </button>
            <button @click="showBankModal = false" class="btn-cancel">
              Annuler
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Messages -->
    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>
  </div>
</template>


<style scoped>
.wallet-dashboard {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.wallet-header h2 {
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.wallet-subtitle {
  color: #6c757d;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
  font-style: italic;
}

.balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2rem;
  border-radius: 12px;
  margin-bottom: 2rem;
}

.balance-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.balance-item.total {
  border-top: 1px solid rgba(255, 255, 255, 0.3);
  padding-top: 1rem;
  font-weight: bold;
  font-size: 1.2em;
}

.balance-amount.available {
  color: #4CAF50;
}

.balance-amount.pending {
  color: #FF9800;
}

.quick-actions {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.btn-withdraw {
  background: #28a745;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s;
}

.btn-withdraw:hover:not(:disabled) {
  background: #218838;
}

.btn-withdraw:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.btn-bank-info, .btn-refresh {
  background: #007bff;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-bank-info:hover, .btn-refresh:hover {
  background: #0056b3;
}

.bank-info, .bank-warning {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 2rem;
}

.bank-warning {
  background: #fff3cd;
  border-left: 4px solid #ffc107;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-item {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-value {
  display: block;
  font-size: 1.5rem;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  color: #6c757d;
  font-size: 0.875rem;
}

.transaction-item, .withdrawal-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #dee2e6;
}

.transaction-item.credit {
  border-left: 4px solid #28a745;
}

.transaction-item.debit {
  border-left: 4px solid #dc3545;
}

.transaction-amount {
  font-weight: bold;
  font-size: 1.1rem;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.processing {
  background: #d1ecf1;
  color: #0c5460;
}

.status-badge.completed {
  background: #d4edda;
  color: #155724;
}

.status-badge.failed, .status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 1rem;
}

.form-input.invalid {
  border-color: #dc3545;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.btn-confirm {
  background: #28a745;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
}

.btn-cancel, .btn-cancel-small {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
}

.btn-cancel-small {
  padding: 0.25rem 0.75rem;
  font-size: 0.75rem;
}

.message {
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
}

.message.success {
  background: #d4edda;
  color: #155724;
  border-left: 4px solid #28a745;
}

.message.error {
  background: #f8d7da;
  color: #721c24;
  border-left: 4px solid #dc3545;
}

.loading, .no-transactions, .no-withdrawals {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
}

.info {
  color: #007bff;
  font-size: 0.875rem;
}

/* Responsive */
@media (max-width: 768px) {
  .wallet-dashboard {
    padding: 1rem;
  }
  
  .quick-actions {
    flex-direction: column;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
