<script>
import { ref, onMounted, computed } from 'vue'
import { authStore } from '@/store/auth'
import contratApi from '@/services/contratApi'

export default {
  name: 'ContratsListView',
  setup() {
    const contrats = ref([])
    const isLoading = ref(true)
    const error = ref(null)
    const isActionLoading = ref(false)

    const showSignModalState = ref(false)
    const showRefuseModalState = ref(false)
    const selectedContrat = ref(null)
    const acceptedTerms = ref(false)
    const refusMotif = ref('')

    const user = computed(() => authStore.user)

    const loadContrats = async () => {
      isLoading.value = true
      error.value = null

      try {
        contrats.value = await contratApi.getContratsCommercant(user.value.idUtilisateur)
      } catch (err) {
        error.value = err.message
        console.error('Erreur:', err)
      } finally {
        isLoading.value = false
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

    const showSignatureInfo = (contrat) => {
      return contrat.dateSignatureCommercant || contrat.dateSignatureAdmin
    }

    const showSignModal = (contrat) => {
      selectedContrat.value = contrat
      acceptedTerms.value = false
      showSignModalState.value = true
    }

    const showRefuseModal = (contrat) => {
      selectedContrat.value = contrat
      refusMotif.value = ''
      showRefuseModalState.value = true
    }

    const signContract = async () => {
      if (!acceptedTerms.value) return

      isActionLoading.value = true

      try {
        await contratApi.accepterContrat(user.value.idUtilisateur, selectedContrat.value.idContrat)
        showSignModalState.value = false
        await loadContrats()
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
          selectedContrat.value.idContrat,
          refusMotif.value
        )
        showRefuseModalState.value = false
        await loadContrats()
      } catch (err) {
        error.value = err.message
      } finally {
        isActionLoading.value = false
      }
    }

    onMounted(() => {
      if (user.value?.idUtilisateur) {
        loadContrats()
      }
    })

    return {
      contrats,
      isLoading,
      error,
      isActionLoading,
      showSignModalState,
      showRefuseModalState,
      selectedContrat,
      acceptedTerms,
      refusMotif,
      loadContrats,
      getStatutLabel,
      getStatutBadgeClass,
      formatDate,
      formatDateTime,
      canSign,
      canRefuse,
      showSignatureInfo,
      showSignModal,
      showRefuseModal,
      signContract,
      refuseContract
    }
  }
}
</script>

<template>
  <div class="contrats-list-view">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1><i class="fas fa-file-contract"></i> Mes contrats</h1>
          <p>Gérez vos contrats avec EcoDeli</p>
        </div>
      </div>
    </div>

    <div class="page-content">
      <div class="content-container">
        <!-- Messages d'état -->
        <div v-if="error" class="error-message">
          <i class="fas fa-exclamation-triangle"></i>
          {{ error }}
          <button @click="loadContrats()" class="retry-btn">
            <i class="fas fa-redo"></i> Réessayer
          </button>
        </div>

        <div v-if="isLoading" class="loading-state">
          <div class="spinner"></div>
          <p>Chargement de vos contrats...</p>
        </div>

        <!-- Liste des contrats -->
        <div v-if="!isLoading && !error" class="contrats-grid">
          <!-- État vide -->
          <div v-if="contrats.length === 0" class="empty-state">
            <div class="empty-icon">
              <i class="fas fa-file-contract"></i>
            </div>
            <h3>Aucun contrat pour le moment</h3>
            <p>Vous n'avez pas encore de contrat avec EcoDeli. Un contrat sera créé par notre équipe après validation de votre inscription.</p>
          </div>

          <!-- Cartes de contrats -->
          <div v-for="contrat in contrats" :key="contrat.idContrat" class="contrat-card">
            <div class="card-header">
              <div class="card-title">
                <h3>Contrat {{ contrat.numeroContrat }}</h3>
                <span :class="['status-badge', getStatutBadgeClass(contrat.statut)]">
                  {{ getStatutLabel(contrat.statut) }}
                </span>
              </div>
            </div>

            <div class="card-content">
              <div class="contrat-details">
                <div class="detail-row">
                  <div class="detail-item">
                    <i class="fas fa-percentage"></i>
                    <span>Commission : {{ contrat.commissionPourcentage }}%</span>
                  </div>
                  <div class="detail-item" v-if="contrat.fraisFixeMensuel > 0">
                    <i class="fas fa-euro-sign"></i>
                    <span>Frais mensuels : {{ contrat.fraisFixeMensuel }}€</span>
                  </div>
                </div>

                <div class="detail-row" v-if="contrat.dateDebut">
                  <div class="detail-item">
                    <i class="fas fa-calendar-alt"></i>
                    <span>Du {{ formatDate(contrat.dateDebut) }}</span>
                  </div>
                  <div class="detail-item" v-if="contrat.dateFin">
                    <i class="fas fa-calendar-times"></i>
                    <span>Au {{ formatDate(contrat.dateFin) }}</span>
                  </div>
                </div>

                <div class="detail-row">
                  <div class="detail-item">
                    <i class="fas fa-clock"></i>
                    <span>Créé le {{ formatDate(contrat.dateCreation) }}</span>
                  </div>
                </div>
              </div>

              <div v-if="contrat.conditionsService" class="conditions">
                <h4><i class="fas fa-list"></i> Conditions spécifiques</h4>
                <p>{{ contrat.conditionsService }}</p>
              </div>

              <!-- Informations de signature -->
              <div v-if="showSignatureInfo(contrat)" class="signature-info">
                <div v-if="contrat.dateSignatureCommercant" class="signature-item">
                  <i class="fas fa-check-circle text-success"></i>
                  <span>Signé par vous le {{ formatDateTime(contrat.dateSignatureCommercant) }}</span>
                </div>
                <div v-if="contrat.dateSignatureAdmin" class="signature-item">
                  <i class="fas fa-check-circle text-success"></i>
                  <span>Validé par EcoDeli le {{ formatDateTime(contrat.dateSignatureAdmin) }}</span>
                </div>
              </div>
            </div>

            <div class="card-footer">
              <div class="actions">
                <router-link
                  :to="`/commercant/contrats/${contrat.idContrat}`"
                  class="btn-view"
                >
                  <i class="fas fa-eye"></i> Voir les détails
                </router-link>

                <button
                  v-if="canSign(contrat)"
                  @click="showSignModal(contrat)"
                  class="btn-sign"
                >
                  <i class="fas fa-pen"></i> Signer le contrat
                </button>

                <button
                  v-if="canRefuse(contrat)"
                  @click="showRefuseModal(contrat)"
                  class="btn-refuse"
                >
                  <i class="fas fa-times"></i> Refuser
                </button>
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
          <p>Vous êtes sur le point de signer le contrat <strong>{{ selectedContrat?.numeroContrat }}</strong>.</p>

          <div class="contrat-summary">
            <h4>Résumé du contrat :</h4>
            <ul>
              <li>Commission : {{ selectedContrat?.commissionPourcentage }}%</li>
              <li v-if="selectedContrat?.fraisFixeMensuel > 0">
                Frais mensuels : {{ selectedContrat?.fraisFixeMensuel }}€
              </li>
              <li v-if="selectedContrat?.dateDebut">
                Période : du {{ formatDate(selectedContrat?.dateDebut) }}
                <span v-if="selectedContrat?.dateFin"> au {{ formatDate(selectedContrat?.dateFin) }}</span>
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
          <p>Vous êtes sur le point de refuser le contrat <strong>{{ selectedContrat?.numeroContrat }}</strong>.</p>

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
.contrats-list-view {
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
}

.header-left h1 {
  font-size: 2rem;
  color: var(--text-color);
  margin: 0 0 0.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.header-left p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 1.1rem;
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

/* Grille de contrats */
.contrats-grid {
  display: grid;
  gap: 1.5rem;
}

.contrat-card {
  background: white;
  border-radius: 12px;
  box-shadow: var(--shadow);
  overflow: hidden;
  transition: all 0.3s ease;
}

.contrat-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.card-header {
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-light);
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.card-title h3 {
  font-size: 1.2rem;
  color: var(--text-color);
  margin: 0;
  font-weight: 600;
}

.status-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  flex-shrink: 0;
}

.badge-success { background: #d4edda; color: #155724; }
.badge-info { background: #d1ecf1; color: #0c5460; }
.badge-warning { background: #fff3cd; color: #856404; }
.badge-secondary { background: #e2e3e5; color: #383d41; }
.badge-danger { background: #f8d7da; color: #721c24; }

.card-content {
  padding: 1.5rem;
}

.contrat-details {
  display: grid;
  gap: 0.8rem;
  margin-bottom: 1.5rem;
}

.detail-row {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.detail-item i {
  color: var(--primary-color);
  width: 16px;
  text-align: center;
}

.conditions {
  margin: 1.5rem 0;
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 8px;
}

.conditions h4 {
  margin: 0 0 0.5rem 0;
  color: var(--text-color);
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.conditions p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.5;
}

.signature-info {
  margin-top: 1.5rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #28a745;
}

.signature-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.signature-item:last-child {
  margin-bottom: 0;
}

.text-success {
  color: #28a745 !important;
}

.card-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--border-light);
}

.actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.btn-view {
  background: var(--primary-color);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
}

.btn-view:hover {
  background: var(--secondary-color);
  text-decoration: none;
  color: white;
}

.btn-sign {
  background: #28a745;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 6px;
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
  padding: 0.5rem 1rem;
  border-radius: 6px;
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

/* État vide */
.empty-state {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 15px;
  box-shadow: var(--shadow);
}

.empty-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  color: white;
  font-size: 2rem;
}

.empty-state h3 {
  color: var(--text-color);
  margin-bottom: 1rem;
  font-size: 1.3rem;
}

.empty-state p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  font-size: 1.1rem;
  line-height: 1.5;
}

/* Modales */
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

.contrat-summary li {
  margin-bottom: 0.3rem;
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

.acceptance-checkbox input[type="checkbox"] {
  width: 16px;
  height: 16px;
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
  font-size: 0.9rem;
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
  font-size: 0.9rem;
}

.btn-success {
  background: #28a745;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
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
  font-size: 0.9rem;
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
  .card-title {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }

  .detail-row {
    flex-direction: column;
    gap: 0.5rem;
  }

  .actions {
    flex-direction: column;
  }
}

/* Dark theme */
[data-theme="dark"] .contrats-list-view {
  background: linear-gradient(135deg, #1e1e1e 0%, #2d3e2d 100%);
}

[data-theme="dark"] .page-header {
  background: var(--bg-secondary);
}

[data-theme="dark"] .contrat-card,
[data-theme="dark"] .empty-state,
[data-theme="dark"] .modal {
  background: var(--bg-secondary);
}

[data-theme="dark"] .conditions,
[data-theme="dark"] .contrat-summary {
  background: var(--bg-color);
}
</style>
