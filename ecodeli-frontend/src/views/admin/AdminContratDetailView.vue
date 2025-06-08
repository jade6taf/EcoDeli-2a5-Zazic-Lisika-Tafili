<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authStore } from '@/store/auth'
import contratApi from '@/services/contratApi'

export default {
  name: 'AdminContratDetailView',
  setup() {
    const route = useRoute()
    const router = useRouter()

    const contrat = ref(null)
    const historique = ref([])
    const isLoading = ref(true)
    const isLoadingHistory = ref(false)
    const error = ref(null)
    const isActionLoading = ref(false)

    const showConfirmModal = ref(false)
    const confirmModal = ref({})
    const refusMotif = ref('')

    const user = computed(() => authStore.user)
    const idContrat = computed(() => parseInt(route.params.id))

    const loadContrat = async () => {
      isLoading.value = true
      error.value = null

      try {
        const response = await fetch(`/api/admin/contrats/${idContrat.value}`, {
          headers: {
            'Authorization': `Bearer ${authStore.token}`,
            'Content-Type': 'application/json'
          }
        })

        if (response.ok) {
          contrat.value = await response.json()
          await loadHistorique()
        } else {
          throw new Error('Contrat non trouvé')
        }
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
        const response = await fetch(`/api/admin/contrats/${idContrat.value}/historique`, {
          headers: {
            'Authorization': `Bearer ${authStore.token}`,
            'Content-Type': 'application/json'
          }
        })

        if (response.ok) {
          historique.value = await response.json()
        }
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

    const canEdit = (contrat) => {
      return contrat.statut === 'BROUILLON'
    }

    const canSend = (contrat) => {
      return contrat.statut === 'BROUILLON'
    }

    const canValidate = (contrat) => {
      return contrat.statut === 'ATTENTE_SIGNATURE_ADMIN'
    }

    const canRefuse = (contrat) => {
      return contrat.statut === 'ATTENTE_SIGNATURE_ADMIN'
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

    const sendForSignature = () => {
      confirmModal.value = {
        title: 'Envoyer pour signature',
        message: `Voulez-vous envoyer le contrat ${contrat.value.numeroContrat} au commerçant pour signature ?`,
        confirmText: 'Envoyer',
        type: 'send'
      }
      showConfirmModal.value = true
    }

    const validateContrat = () => {
      confirmModal.value = {
        title: 'Valider le contrat',
        message: `Voulez-vous valider et activer le contrat ${contrat.value.numeroContrat} ?`,
        confirmText: 'Valider',
        type: 'validate'
      }
      showConfirmModal.value = true
    }

    const refuseContrat = () => {
      refusMotif.value = ''
      confirmModal.value = {
        title: 'Refuser le contrat',
        message: `Voulez-vous refuser le contrat ${contrat.value.numeroContrat} ?`,
        confirmText: 'Refuser',
        type: 'refuse'
      }
      showConfirmModal.value = true
    }

    const confirmAction = async () => {
      if (!contrat.value) return

      isActionLoading.value = true

      try {
        switch (confirmModal.value.type) {
          case 'send':
            await contratApi.envoyerPourSignature(contrat.value.idContrat, user.value.idUtilisateur)
            break
          case 'validate':
            await contratApi.validerContrat(contrat.value.idContrat, user.value.idUtilisateur)
            break
          case 'refuse':
            if (!refusMotif.value.trim()) {
              error.value = 'Veuillez saisir un motif de refus'
              return
            }
            await contratApi.refuserContratAdmin(
              contrat.value.idContrat,
              user.value.idUtilisateur,
              refusMotif.value
            )
            break
        }

        showConfirmModal.value = false
        await loadContrat()
      } catch (err) {
        error.value = err.message
      } finally {
        isActionLoading.value = false
        refusMotif.value = ''
      }
    }

    onMounted(() => {
      if (idContrat.value) {
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
      showConfirmModal,
      confirmModal,
      refusMotif,
      loadContrat,
      loadHistorique,
      getStatutLabel,
      getStatutBadgeClass,
      formatDate,
      formatDateTime,
      canEdit,
      canSend,
      canValidate,
      canRefuse,
      getHistoriqueIcon,
      sendForSignature,
      validateContrat,
      refuseContrat,
      confirmAction
    }
  }
}
</script>

<template>
  <div class="admin-contrat-detail-view">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <router-link to="/admin/contrats" class="back-link">
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
              <span class="created-date">
                Créé le {{ formatDateTime(contrat.dateCreation) }}
              </span>
            </p>
          </div>
        </div>

        <div class="header-actions" v-if="contrat">
          <router-link
            v-if="canEdit(contrat)"
            :to="`/admin/contrats/${contrat.idContrat}/edit`"
            class="btn-edit"
          >
            <i class="fas fa-edit"></i> Modifier
          </router-link>

          <button
            v-if="canSend(contrat)"
            @click="sendForSignature()"
            class="btn-send"
          >
            <i class="fas fa-paper-plane"></i> Envoyer pour signature
          </button>

          <button
            v-if="canValidate(contrat)"
            @click="validateContrat()"
            class="btn-validate"
          >
            <i class="fas fa-check"></i> Valider le contrat
          </button>

          <button
            v-if="canRefuse(contrat)"
            @click="refuseContrat()"
            class="btn-refuse"
          >
            <i class="fas fa-times"></i> Refuser
          </button>
        </div>
      </div>
    </div>

    <div class="page-content">
      <div class="content-container">
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

        <div v-if="!isLoading && !error && contrat" class="contrat-detail">
          <div class="detail-grid">

            <div class="detail-card">
              <div class="card-header">
                <h3><i class="fas fa-store"></i> Commerçant</h3>
              </div>
              <div class="card-content">
                <div class="commercant-profile">
                  <div class="commercant-avatar">
                    <i class="fas fa-user-tie"></i>
                  </div>
                  <div class="commercant-info">
                    <div class="commercant-name">
                      {{ contrat.commercant?.nom }} {{ contrat.commercant?.prenom }}
                    </div>
                    <div class="commercant-details">
                      <div class="detail-item">
                        <i class="fas fa-envelope"></i>
                        <span>{{ contrat.commercant?.email }}</span>
                      </div>
                      <div v-if="contrat.commercant?.telephone" class="detail-item">
                        <i class="fas fa-phone"></i>
                        <span>{{ contrat.commercant?.telephone }}</span>
                      </div>
                      <div class="detail-item">
                        <i class="fas fa-calendar-plus"></i>
                        <span>Inscrit le {{ formatDate(contrat.commercant?.dateCreation) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="detail-card">
              <div class="card-header">
                <h3><i class="fas fa-info-circle"></i> Informations du contrat</h3>
              </div>
              <div class="card-content">
                <div class="info-grid">
                  <div class="info-item">
                    <span class="label">Numéro :</span>
                    <span class="value contract-number">{{ contrat.numeroContrat }}</span>
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

            <div class="detail-card financial-card">
              <div class="card-header">
                <h3><i class="fas fa-euro-sign"></i> Conditions financières</h3>
              </div>
              <div class="card-content">
                <div class="financial-grid">
                  <div class="financial-item main">
                    <div class="financial-icon">
                      <i class="fas fa-percentage"></i>
                    </div>
                    <div class="financial-content">
                      <div class="financial-label">Commission</div>
                      <div class="financial-value">{{ contrat.commissionPourcentage }}%</div>
                    </div>
                  </div>

                  <div class="financial-item">
                    <div class="financial-icon">
                      <i class="fas fa-calendar-check"></i>
                    </div>
                    <div class="financial-content">
                      <div class="financial-label">Frais mensuels</div>
                      <div class="financial-value">
                        {{ contrat.fraisFixeMensuel > 0 ? `${contrat.fraisFixeMensuel}€` : 'Aucun' }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="detail-card" v-if="contrat.dateDebut || contrat.dateFin">
              <div class="card-header">
                <h3><i class="fas fa-calendar-alt"></i> Durée du contrat</h3>
              </div>
              <div class="card-content">
                <div class="duration-info">
                  <div v-if="contrat.dateDebut" class="duration-item">
                    <i class="fas fa-play-circle"></i>
                    <div>
                      <div class="duration-label">Date de début</div>
                      <div class="duration-value">{{ formatDate(contrat.dateDebut) }}</div>
                    </div>
                  </div>

                  <div v-if="contrat.dateFin" class="duration-item">
                    <i class="fas fa-stop-circle"></i>
                    <div>
                      <div class="duration-label">Date de fin</div>
                      <div class="duration-value">{{ formatDate(contrat.dateFin) }}</div>
                    </div>
                  </div>

                  <div v-if="!contrat.dateFin" class="duration-item">
                    <i class="fas fa-infinity"></i>
                    <div>
                      <div class="duration-label">Durée</div>
                      <div class="duration-value">Indéterminée</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="detail-card signatures-card">
            <div class="card-header">
              <h3><i class="fas fa-signature"></i> État des signatures</h3>
            </div>
            <div class="card-content">
              <div class="signatures-grid">
                <div class="signature-step">
                  <div class="signature-number">1</div>
                  <div class="signature-content">
                    <div class="signature-title">Signature du commerçant</div>
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
                </div>

                <div class="signature-arrow">
                  <i class="fas fa-arrow-right"></i>
                </div>

                <div class="signature-step">
                  <div class="signature-number">2</div>
                  <div class="signature-content">
                    <div class="signature-title">Validation EcoDeli</div>
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
              <div v-else class="historique-timeline">
                <div
                  v-for="entry in historique"
                  :key="entry.idHistory"
                  class="timeline-item"
                >
                  <div class="timeline-marker">
                    <i :class="getHistoriqueIcon(entry.statutNouveau)"></i>
                  </div>
                  <div class="timeline-content">
                    <div class="timeline-header">
                      <div class="timeline-action">{{ entry.commentaire }}</div>
                      <div class="timeline-date">{{ formatDateTime(entry.dateAction) }}</div>
                    </div>
                    <div class="timeline-user">
                      <i class="fas fa-user"></i>
                      {{ entry.utilisateurAction?.nom }} {{ entry.utilisateurAction?.prenom }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showConfirmModal" class="modal-overlay" @click="showConfirmModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>{{ confirmModal.title }}</h3>
        </div>
        <div class="modal-body">
          <p>{{ confirmModal.message }}</p>
          <div v-if="confirmModal.type === 'refuse'" class="form-group">
            <label for="refusMotif">Motif du refus :</label>
            <textarea
              id="refusMotif"
              v-model="refusMotif"
              placeholder="Expliquez le motif du refus..."
              rows="4"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showConfirmModal = false" class="btn-secondary">Annuler</button>
          <button
            @click="confirmAction"
            :class="['btn-primary', confirmModal.type]"
            :disabled="isActionLoading"
          >
            <i v-if="isActionLoading" class="fas fa-spinner fa-spin"></i>
            <span v-else>{{ confirmModal.confirmText }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-contrat-detail-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e8f5e9 100%);
}

.page-header {
  background: white;
  box-shadow: var(--shadow);
  border-bottom: 3px solid var(--primary-color);
}

.header-content {
  max-width: 1400px;
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

.header-left p {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: 0;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.created-date {
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.btn-edit {
  background: #28a745;
  color: white;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  text-decoration: none;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.btn-send {
  background: #17a2b8;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-validate {
  background: #28a745;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-refuse {
  background: #dc3545;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-content {
  padding: 2rem 0;
}

.content-container {
  max-width: 1400px;
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
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.detail-card {
  background: white;
  border-radius: 15px;
  box-shadow: var(--shadow);
  overflow: hidden;
  transition: all 0.3s ease;
}

.detail-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.signatures-card,
.conditions-card {
  grid-column: 1 / -1;
}

.card-header {
  padding: 1.5rem;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: var(--text-color);
  font-size: 1.2rem;
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

.card-content {
  padding: 1.5rem;
}

/* Profil commerçant */
.commercant-profile {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.commercant-avatar {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.commercant-name {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 0.5rem;
}

.commercant-details {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
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
}

/* Informations du contrat */
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

.label {
  font-weight: 500;
  color: var(--text-secondary);
}

.value {
  color: var(--text-color);
  font-weight: 500;
}

.contract-number {
  font-family: 'Courier New', monospace;
  color: var(--primary-color);
  font-weight: 600;
}

.status-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-success { background: #d4edda; color: #155724; }
.badge-info { background: #d1ecf1; color: #0c5460; }
.badge-warning { background: #fff3cd; color: #856404; }
.badge-secondary { background: #e2e3e5; color: #383d41; }
.badge-danger { background: #f8d7da; color: #721c24; }

/* Conditions financières */
.financial-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e8f5e9 100%);
}

.financial-grid {
  display: grid;
  gap: 1.5rem;
}

.financial-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.financial-item.main {
  border-left: 4px solid var(--primary-color);
}

.financial-icon {
  width: 50px;
  height: 50px;
  background: var(--primary-color);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.financial-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-bottom: 0.2rem;
}

.financial-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-color);
}

/* Durée */
.duration-info {
  display: grid;
  gap: 1rem;
}

.duration-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 8px;
}

.duration-item i {
  color: var(--primary-color);
  font-size: 1.2rem;
  width: 20px;
}

.duration-label {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin-bottom: 0.2rem;
}

.duration-value {
  font-weight: 500;
  color: var(--text-color);
}

/* Signatures */
.signatures-grid {
  display: flex;
  align-items: center;
  gap: 2rem;
  padding: 1rem 0;
}

.signature-step {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.signature-number {
  width: 40px;
  height: 40px;
  background: var(--primary-color);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.signature-title {
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 0.5rem;
}

.signature-status .signed {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #28a745;
  font-size: 0.9rem;
}

.signature-status .not-signed {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.signature-arrow {
  color: var(--text-secondary);
  font-size: 1.5rem;
  flex-shrink: 0;
}

/* Conditions */
.conditions-text {
  background: var(--bg-secondary);
  padding: 1.5rem;
  border-radius: 8px;
  line-height: 1.6;
  color: var(--text-color);
  white-space: pre-wrap;
  border-left: 4px solid var(--primary-color);
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

.historique-timeline {
  position: relative;
}

.historique-timeline::before {
  content: '';
  position: absolute;
  left: 20px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--border-light);
}

.timeline-item {
  position: relative;
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.timeline-item:last-child {
  margin-bottom: 0;
}

.timeline-marker {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.timeline-content {
  flex: 1;
  background: var(--bg-secondary);
  padding: 1rem;
  border-radius: 8px;
  margin-top: 0.3rem;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.5rem;
}

.timeline-action {
  font-weight: 500;
  color: var(--text-color);
}

.timeline-date {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.timeline-user {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

/* Modal */
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
}

.modal-body {
  padding: 1.5rem;
}

.modal-body p {
  margin-bottom: 1rem;
  color: var(--text-color);
  line-height: 1.5;
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

.btn-primary {
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-primary.send {
  background: #17a2b8;
}

.btn-primary.validate {
  background: #28a745;
}

.btn-primary.refuse {
  background: #dc3545;
}

/* Responsive */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .header-actions {
    justify-content: center;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .commercant-profile {
    flex-direction: column;
    text-align: center;
  }

  .signatures-grid {
    flex-direction: column;
    gap: 1rem;
  }

  .signature-arrow {
    transform: rotate(90deg);
  }

  .timeline-header {
    flex-direction: column;
    gap: 0.3rem;
  }
}

/* Dark theme */
[data-theme="dark"] .admin-contrat-detail-view {
  background: linear-gradient(135deg, #1e1e1e 0%, #2d3e2d 100%);
}

[data-theme="dark"] .page-header,
[data-theme="dark"] .detail-card,
[data-theme="dark"] .modal {
  background: var(--bg-secondary);
}

[data-theme="dark"] .card-header {
  background: var(--bg-color);
}

[data-theme="dark"] .financial-card {
  background: var(--bg-color);
}

[data-theme="dark"] .financial-item,
[data-theme="dark"] .timeline-content {
  background: var(--bg-color);
}

[data-theme="dark"] .conditions-text {
  background: var(--bg-color);
}
</style>
