<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authStore } from '@/store/auth'
import contratApi from '@/services/contratApi'

export default {
  name: 'ContratDetailView',
  setup() {
    const route = useRoute()
    const router = useRouter()

    const contrat = ref(null)
    const historique = ref([])
    const isLoading = ref(true)
    const isLoadingHistory = ref(false)
    const error = ref(null)
    const isActionLoading = ref(false)

    const showSignModalState = ref(false)
    const showRefuseModalState = ref(false)
    const acceptedTerms = ref(false)
    const refusMotif = ref('')

    const user = computed(() => authStore.user)
    const idContrat = computed(() => parseInt(route.params.id))

    const loadContrat = async () => {
      isLoading.value = true
      error.value = null

      try {
        contrat.value = await contratApi.getContratCommercant(user.value.idUtilisateur, idContrat.value)
        await loadHistorique()
      } catch (err) {
        error.value = err.message
        console.error('Erreur:', err)
      } finally {
        isLoading.value = false
      }
    }

    const loadHistorique = async () => {
      isLoadingHistory.value = true

      try {
        historique.value = await contratApi.getHistoriqueContrat(user.value.idUtilisateur, idContrat.value)
      } catch (err) {
        console.error('Erreur lors du chargement de l\'historique:', err)
      } finally {
        isLoadingHistory.value = false
      }
    }

    const getStatutLabel = (statut) => {
      return contratApi.getStatutLabel(statut)
    }

    const getStatutBadgeClass = (statut) => {
      return contratApi.getStatutBadgeClass(statut)
    }

    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleDateString('fr-FR')
    }

    const formatDateTime = (dateString) => {
      return new Date(dateString).toLocaleString('fr-FR')
    }

    const canSign = (contrat) => {
      return contrat.statut === 'ATTENTE_SIGNATURE_COMMERCANT'
    }

    const canRefuse = (contrat) => {
      return contrat.statut === 'ATTENTE_SIGNATURE_COMMERCANT'
    }

    const getHistoriqueIcon = (statut) => {
      const icons = {
        'BROUILLON': 'fas fa-file-alt',
        'ATTENTE_SIGNATURE_COMMERCANT': 'fas fa-paper-plane',
        'ATTENTE_SIGNATURE_ADMIN': 'fas fa-user-check',
        'ACTIF': 'fas fa-check-circle',
        'REFUSE': 'fas fa-times-circle',
        'RESILIE': 'fas fa-ban'
      }
      return icons[statut] || 'fas fa-info-circle'
    }

    const showSignModal = () => {
      acceptedTerms.value = false
      showSignModalState.value = true
    }

    const showRefuseModal = () => {
      refusMotif.value = ''
      showRefuseModalState.value = true
    }

    const signContract = async () => {
      if (!acceptedTerms.value) return

      isActionLoading.value = true

      try {
        await contratApi.accepterContrat(user.value.idUtilisateur, idContrat.value)
        showSignModalState.value = false
        await loadContrat()
      } catch (err) {
        error.value = err.message
      } finally {
        isActionLoading.value = false
      }
    }

    const refuseContract = async () => {
      if (!refusMotif.value.trim()) return

      isActionLoading.value = true

      try {
        await contratApi.refuserContrat(
          user.value.idUtilisateur,
          idContrat.value,
          refusMotif.value
        )
        showRefuseModalState.value = false
        await loadContrat()
      } catch (err) {
        error.value = err.message
      } finally {
        isActionLoading.value = false
      }
    }

    onMounted(() => {
      if (user.value?.idUtilisateur && idContrat.value) {
        loadContrat()
      }
    })

    return {
      contrat,
      historique,
      isLoading,
      isLoadingHistory,
      error,
      isActionLoading,
      showSignModalState,
      showRefuseModalState,
      acceptedTerms,
      refusMotif,
      loadContrat,
      loadHistorique,
      getStatutLabel,
      getStatutBadgeClass,
      formatDate,
      formatDateTime,
      canSign,
      canRefuse,
      getHistoriqueIcon,
      showSignModal,
      showRefuseModal,
      signContract,
      refuseContract
    }
  }
}
</script>

<template>
  <div class="contrat-detail-view">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <router-link to="/commercant/contrats" class="back-link">
            <i class="fas fa-arrow-left"></i>
          </router-link>
          <div>
            <h1 v-if="contrat">
              <i class="fas fa-file-contract"></i>
              Contrat {{ contrat.numeroContrat }}
            </h1>
            <p v-if="contrat">
              <span :class="['status-badge', getStatutBadgeClass(contrat.statut)]">
                {{ getStatutLabel(contrat.statut) }}
              </span>
            </p>
          </div>
        </div>

        <div class="header-actions" v-if="contrat">
          <button
            v-if="canSign(contrat)"
            @click="showSignModal()"
            class="btn-sign"
          >
            <i class="fas fa-pen"></i> Signer le contrat
          </button>

          <button
            v-if="canRefuse(contrat)"
            @click="showRefuseModal()"
            class="btn-refuse"
          >
            <i class="fas fa-times"></i> Refuser
          </button>
        </div>
      </div>
    </div>

    <div class="page-content">
      <div class="content-container">
        <!-- Messages d'état -->
        <div v-if="error" class="error-message">
          <i class="fas fa-exclamation-triangle"></i>
          {{ error }}
          <button @click="loadContrat()" class="retry-btn">
            <i class="fas fa-redo"></i> Réessayer
          </button>
        </div>

        <div v-if="isLoading" class="loading-state">
          <div class="spinner"></div>
          <p>Chargement du contrat...</p>
        </div>

        <!-- Détail du contrat -->
        <div v-if="!isLoading && !error && contrat" class="contrat-detail">
          <div class="detail-grid">
            <!-- Informations principales -->
            <div class="detail-card">
              <div class="card-header">
                <h3><i class="fas fa-info-circle"></i> Informations générales</h3>
              </div>
              <div class="card-content">
                <div class="info-grid">
                  <div class="info-item">
                    <span class="label">Numéro de contrat :</span>
                    <span class="value">{{ contrat.numeroContrat }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">Statut :</span>
                    <span :class="['status-badge', getStatutBadgeClass(contrat.statut)]">
                      {{ getStatutLabel(contrat.statut) }}
                    </span>
                  </div>
                  <div class="info-item">
                    <span class="label">Date de création :</span>
                    <span class="value">{{ formatDateTime(contrat.dateCreation) }}</span>
                  </div>
                  <div class="info-item" v-if="contrat.dateModification">
                    <span class="label">Dernière modification :</span>
                    <span class="value">{{ formatDateTime(contrat.dateModification) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Conditions financières -->
            <div class="detail-card">
              <div class="card-header">
                <h3><i class="fas fa-euro-sign"></i> Conditions financières</h3>
              </div>
              <div class="card-content">
                <div class="info-grid">
                  <div class="info-item highlight">
                    <span class="label">Commission :</span>
                    <span class="value commission">{{ contrat.commissionPourcentage }}%</span>
                  </div>
                  <div class="info-item" v-if="contrat.fraisFixeMensuel > 0">
                    <span class="label">Frais fixes mensuels :</span>
                    <span class="value">{{ contrat.fraisFixeMensuel }}€</span>
                  </div>
                  <div class="info-item" v-else>
                    <span class="label">Frais fixes mensuels :</span>
                    <span class="value text-success">Aucun</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Durée du contrat -->
            <div class="detail-card" v-if="contrat.dateDebut || contrat.dateFin">
              <div class="card-header">
                <h3><i class="fas fa-calendar-alt"></i> Durée du contrat</h3>
              </div>
              <div class="card-content">
                <div class="info-grid">
                  <div class="info-item" v-if="contrat.dateDebut">
                    <span class="label">Date de début :</span>
                    <span class="value">{{ formatDate(contrat.dateDebut) }}</span>
                  </div>
                  <div class="info-item" v-if="contrat.dateFin">
                    <span class="label">Date de fin :</span>
                    <span class="value">{{ formatDate(contrat.dateFin) }}</span>
                  </div>
                  <div class="info-item" v-if="!contrat.dateFin">
                    <span class="label">Durée :</span>
                    <span class="value text-info">Durée indéterminée</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Signatures -->
            <div class="detail-card">
              <div class="card-header">
                <h3><i class="fas fa-signature"></i> Signatures</h3>
              </div>
              <div class="card-content">
                <div class="signatures-grid">
                  <div class="signature-item">
                    <div class="signature-header">
                      <i class="fas fa-store"></i>
                      <span>Votre signature</span>
                    </div>
                    <div class="signature-status">
                      <div v-if="contrat.dateSignatureCommercant" class="signed">
                        <i class="fas fa-check-circle"></i>
                        <span>Signé le {{ formatDateTime(contrat.dateSignatureCommercant) }}</span>
                      </div>
                      <div v-else class="not-signed">
                        <i class="fas fa-clock"></i>
                        <span>En attente de signature</span>
                      </div>
                    </div>
                  </div>

                  <div class="signature-item">
                    <div class="signature-header">
                      <i class="fas fa-building"></i>
                      <span>Signature EcoDeli</span>
                    </div>
                    <div class="signature-status">
                      <div v-if="contrat.dateSignatureAdmin" class="signed">
                        <i class="fas fa-check-circle"></i>
                        <span>Validé le {{ formatDateTime(contrat.dateSignatureAdmin) }}</span>
                      </div>
                      <div v-else class="not-signed">
                        <i class="fas fa-clock"></i>
                        <span>En attente de validation</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Conditions spécifiques -->
          <div v-if="contrat.conditionsService" class="detail-card conditions-card">
            <div class="card-header">
              <h3><i class="fas fa-list-ul"></i> Conditions spécifiques</h3>
            </div>
            <div class="card-content">
              <div class="conditions-text">
                {{ contrat.conditionsService }}
              </div>
            </div>
          </div>

          <!-- Historique -->
          <div class="detail-card">
            <div class="card-header">
              <h3><i class="fas fa-history"></i> Historique des actions</h3>
              <button @click="loadHistorique()" class="btn-refresh" :disabled="isLoadingHistory">
                <i :class="['fas', isLoadingHistory ? 'fa-spinner fa-spin' : 'fa-sync-alt']"></i>
              </button>
            </div>
            <div class="card-content">
              <div v-if="isLoadingHistory" class="loading-small">
                <div class="spinner-small"></div>
                <span>Chargement de l'historique...</span>
              </div>
              <div v-else-if="historique.length === 0" class="empty-history">
                <i class="fas fa-history"></i>
                <span>Aucun historique disponible</span>
              </div>
              <div v-else class="historique-list">
                <div
                  v-for="entry in historique"
                  :key="entry.idHistory"
                  class="historique-item"
                >
                  <div class="historique-icon">
                    <i :class="getHistoriqueIcon(entry.statutNouveau)"></i>
                  </div>
                  <div class="historique-content">
                    <div class="historique-action">
                      {{ entry.commentaire }}
                    </div>
                    <div class="historique-meta">
                      <span class="historique-user">
                        {{ entry.utilisateurAction?.nom }} {{ entry.utilisateurAction?.prenom }}
                      </span>
                      <span class="historique-date">
                        {{ formatDateTime(entry.dateAction) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de signature -->
    <div v-if="showSignModalState" class="modal-overlay" @click="showSignModalState = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Signature du contrat</h3>
        </div>
        <div class="modal-body">
          <p>Vous êtes sur le point de signer le contrat <strong>{{ contrat?.numeroContrat }}</strong>.</p>

          <div class="contrat-summary">
            <h4>Résumé du contrat :</h4>
            <ul>
              <li>Commission : {{ contrat?.commissionPourcentage }}%</li>
              <li v-if="contrat?.fraisFixeMensuel > 0">
                Frais mensuels : {{ contrat?.fraisFixeMensuel }}€
              </li>
              <li v-if="contrat?.dateDebut">
                Période : du {{ formatDate(contrat?.dateDebut) }}
                <span v-if="contrat?.dateFin"> au {{ formatDate(contrat?.dateFin) }}</span>
              </li>
            </ul>
          </div>

          <div class="acceptance-checkbox">
            <label>
              <input type="checkbox" v-model="acceptedTerms" />
              <span>J'accepte les termes et conditions de ce contrat</span>
            </label>
          </div>

          <p class="warning">Une fois signé, ce contrat sera transmis à l'équipe EcoDeli pour validation finale.</p>
        </div>
        <div class="modal-footer">
          <button @click="showSignModalState = false" class="btn-secondary">Annuler</button>
          <button
            @click="signContract"
            class="btn-success"
            :disabled="!acceptedTerms || isActionLoading"
          >
            <i v-if="isActionLoading" class="fas fa-spinner fa-spin"></i>
            <span v-else>Signer le contrat</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Modal de refus -->
    <div v-if="showRefuseModalState" class="modal-overlay" @click="showRefuseModalState = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Refuser le contrat</h3>
        </div>
        <div class="modal-body">
          <p>Vous êtes sur le point de refuser le contrat <strong>{{ contrat?.numeroContrat }}</strong>.</p>

          <div class="form-group">
            <label for="refusMotif">Motif du refus :</label>
            <textarea
              id="refusMotif"
              v-model="refusMotif"
              placeholder="Expliquez pourquoi vous refusez ce contrat..."
              rows="4"
            ></textarea>
          </div>

          <p class="warning">Cette action est définitive. Vous pourrez contacter notre équipe pour renégocier un nouveau contrat.</p>
        </div>
        <div class="modal-footer">
          <button @click="showRefuseModalState = false" class="btn-secondary">Annuler</button>
          <button
            @click="refuseContract"
            class="btn-danger"
            :disabled="!refusMotif.trim() || isActionLoading"
          >
            <i v-if="isActionLoading" class="fas fa-spinner fa-spin"></i>
            <span v-else>Refuser le contrat</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.contrat-detail-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e8f5e9 100%);
}

.page-header {
  background: white;
  box-shadow: var(--shadow);
  border-bottom: 3px solid var(--primary-color);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.back-link {
  width: 40px;
  height: 40px;
  background: var(--primary-color);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  transition: all 0.3s ease;
}

.back-link:hover {
  background: var(--secondary-color);
  transform: translateX(-2px);
}

.header-left h1 {
  font-size: 2rem;
  color: var(--text-color);
  margin: 0 0 0.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.btn-sign {
  background: #28a745;
  color: white;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
}

.btn-sign:hover {
  background: #218838;
}

.btn-refuse {
  background: #dc3545;
  color: white;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
}

.btn-refuse:hover {
  background: #c82333;
}

.page-content {
  padding: 2rem 0;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* Messages d'état */
.error-message {
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 12px;
  padding: 1.5rem;
  color: #c33;
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.retry-btn {
  background: #c33;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  margin-left: auto;
}

.loading-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-secondary);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Grille de détails */
.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.detail-card {
  background: white;
  border-radius: 12px;
  box-shadow: var(--shadow);
  overflow: hidden;
}

.conditions-card {
  grid-column: 1 / -1;
}

.card-header {
  padding: 1.2rem 1.5rem;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: var(--text-color);
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-refresh {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.3rem;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.btn-refresh:hover {
  background: var(--primary-color);
  color: white;
}

.btn-refresh:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.card-content {
  padding: 1.5rem;
}

.info-grid {
  display: grid;
  gap: 1rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 0;
  border-bottom: 1px solid var(--border-light);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item.highlight {
  background: var(--bg-secondary);
  padding: 1rem;
  border-radius: 8px;
  border: none;
}

.label {
  font-weight: 500;
  color: var(--text-secondary);
}

.value {
  color: var(--text-color);
  font-weight: 500;
}

.value.commission {
  font-size: 1.2rem;
  color: var(--primary-color);
  font-weight: 600;
}

.text-success { color: #28a745; }
.text-info { color: #17a2b8; }

.status-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-success { background: #d4edda; color: #155724; }
.badge-info { background: #d1ecf1; color: #0c5460; }
.badge-warning { background: #fff3cd; color: #856404; }
.badge-secondary { background: #e2e3e5; color: #383d41; }
.badge-danger { background: #f8d7da; color: #721c24; }

/* Signatures */
.signatures-grid {
  display: grid;
  gap: 1.5rem;
}

.signature-item {
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid var(--border-light);
}

.signature-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.8rem;
  font-weight: 500;
  color: var(--text-color);
}

.signature-status .signed {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #28a745;
}

.signature-status .not-signed {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
}

/* Conditions */
.conditions-text {
  background: var(--bg-secondary);
  padding: 1.5rem;
  border-radius: 8px;
  line-height: 1.6;
  color: var(--text-color);
  white-space: pre-wrap;
}

/* Historique */
.loading-small {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  color: var(--text-secondary);
  padding: 1rem;
  justify-content: center;
}

.spinner-small {
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.empty-history {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  color: var(--text-secondary);
  padding: 2rem;
  justify-content: center;
  font-style: italic;
}

.historique-list {
  display: grid;
  gap: 1rem;
}

.historique-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid var(--border-light);
  transition: all 0.3s ease;
}

.historique-item:hover {
  background: var(--bg-secondary);
}

.historique-icon {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.historique-content {
  flex: 1;
}

.historique-action {
  font-weight: 500;
  color: var(--text-color);
  margin-bottom: 0.3rem;
}

.historique-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

/* Modales - styles importés de ContratsListView */
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

.modal {
  background: white;
  border-radius: 12px;
  box-shadow: var(--shadow-hover);
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-light);
}

.modal-header h3 {
  margin: 0;
  color: var(--text-color);
  font-size: 1.2rem;
}

.modal-body {
  padding: 1.5rem;
}

.modal-body p {
  margin-bottom: 1rem;
  color: var(--text-color);
  line-height: 1.5;
}

.contrat-summary {
  background: var(--bg-secondary);
  padding: 1rem;
  border-radius: 8px;
  margin: 1rem 0;
}

.contrat-summary h4 {
  margin: 0 0 0.5rem 0;
  color: var(--text-color);
  font-size: 1rem;
}

.contrat-summary ul {
  margin: 0;
  padding-left: 1.5rem;
  color: var(--text-secondary);
}

.acceptance-checkbox {
  margin: 1.5rem 0;
}

.acceptance-checkbox label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  color: var(--text-color);
}

.form-group {
  margin: 1rem 0;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-color);
  font-weight: 500;
}

.form-group textarea {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid var(--border-light);
  border-radius: 6px;
  font-family: inherit;
  resize: vertical;
}

.warning {
  color: #856404;
  background: #fff3cd;
  padding: 0.8rem;
  border-radius: 6px;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.modal-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--border-light);
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.btn-secondary {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
}

.btn-success {
  background: #28a745;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-success:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-danger {
  background: #dc3545;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .header-left {
    flex-direction: column;
    gap: 0.5rem;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .info-item {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }

  .historique-meta {
    flex-direction: column;
    gap: 0.3rem;
  }
}

/* Dark theme */
[data-theme="dark"] .contrat-detail-view {
  background: linear-gradient(135deg, #1e1e1e 0%, #2d3e2d 100%);
}

[data-theme="dark"] .page-header {
  background: var(--bg-secondary);
}

[data-theme="dark"] .detail-card,
[data-theme="dark"] .modal {
  background: var(--bg-secondary);
}

[data-theme="dark"] .card-header {
  background: var(--bg-color);
}

[data-theme="dark"] .conditions-text,
[data-theme="dark"] .contrat-summary {
  background: var(--bg-color);
}
</style>
