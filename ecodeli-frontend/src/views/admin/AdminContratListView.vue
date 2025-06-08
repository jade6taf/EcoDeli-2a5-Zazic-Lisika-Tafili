<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authStore } from '@/store/auth'
import contratApi from '@/services/contratApi'

export default {
  name: 'AdminContratListView',
  setup() {
    const router = useRouter()

    const contrats = ref([])
    const statistiques = ref({})
    const isLoading = ref(true)
    const error = ref(null)
    const isActionLoading = ref(false)

    const selectedStatus = ref('')
    const searchTerm = ref('')

    const currentPage = ref(1)
    const itemsPerPage = 10

    const showConfirmModal = ref(false)
    const confirmModal = ref({})
    const refusMotif = ref('')
    const selectedContrat = ref(null)

    const user = computed(() => authStore.user)

    const hasActiveFilters = computed(() => {
      return selectedStatus.value || searchTerm.value
    })

    const filteredContrats = computed(() => {
      let filtered = contrats.value

      if (selectedStatus.value) {
        filtered = filtered.filter(contrat => contrat.statut === selectedStatus.value)
      }

      if (searchTerm.value) {
        const term = searchTerm.value.toLowerCase()
        filtered = filtered.filter(contrat =>
          contrat.numeroContrat.toLowerCase().includes(term) ||
          `${contrat.commercant?.nom} ${contrat.commercant?.prenom}`.toLowerCase().includes(term) ||
          contrat.commercant?.email.toLowerCase().includes(term)
        )
      }

      return filtered
    })

    const totalPages = computed(() => {
      return Math.ceil(filteredContrats.value.length / itemsPerPage)
    })

    const paginatedContrats = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage
      const end = start + itemsPerPage
      return filteredContrats.value.slice(start, end)
    })

    const loadContrats = async () => {
      isLoading.value = true
      error.value = null

      try {
        const [contratsData, statsData] = await Promise.all([
          contratApi.getAllContrats(),
          contratApi.getStatistiquesAdmin()
        ])

        contrats.value = contratsData
        statistiques.value = statsData
      } catch (err) {
        error.value = err.message
        console.error('Erreur:', err)
      } finally {
        isLoading.value = false
      }
    }

    const applyFilters = () => {
      currentPage.value = 1
    }

    const resetFilters = () => {
      selectedStatus.value = ''
      searchTerm.value = ''
      currentPage.value = 1
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

    const canEdit = (contrat) => {
      return contrat.statut === 'BROUILLON'
    }

    const canSend = (contrat) => {
      return contrat.statut === 'BROUILLON' && contrat.commercant && contrat.commissionPourcentage
    }

    const canValidate = (contrat) => {
      return contrat.statut === 'ATTENTE_SIGNATURE_ADMIN'
    }

    const canDelete = (contrat) => {
      return contrat.statut === 'BROUILLON'
    }

    const editContrat = (contrat) => {
      router.push(`/admin/contrats/${contrat.idContrat}/edit`)
    }

    const sendForSignature = (contrat) => {
      selectedContrat.value = contrat
      confirmModal.value = {
        title: 'Envoyer pour signature',
        message: `Voulez-vous envoyer le contrat ${contrat.numeroContrat} au commerçant pour signature ?`,
        confirmText: 'Envoyer',
        type: 'send'
      }
      showConfirmModal.value = true
    }

    const validateContrat = (contrat) => {
      selectedContrat.value = contrat
      confirmModal.value = {
        title: 'Valider le contrat',
        message: `Voulez-vous valider et activer le contrat ${contrat.numeroContrat} ?`,
        confirmText: 'Valider',
        type: 'validate'
      }
      showConfirmModal.value = true
    }

    const deleteContrat = (contrat) => {
      selectedContrat.value = contrat
      confirmModal.value = {
        title: 'Supprimer le contrat',
        message: `Êtes-vous sûr de vouloir supprimer le contrat ${contrat.numeroContrat} ? Cette action est irréversible.`,
        confirmText: 'Supprimer',
        type: 'delete'
      }
      showConfirmModal.value = true
    }

    const confirmAction = async () => {
      if (!selectedContrat.value) return

      isActionLoading.value = true

      try {
        switch (confirmModal.value.type) {
          case 'send':
            await contratApi.envoyerPourSignature(selectedContrat.value.idContrat, user.value.idUtilisateur)
            break
          case 'validate':
            await contratApi.validerContrat(selectedContrat.value.idContrat, user.value.idUtilisateur)
            break
          case 'delete':
            await contratApi.deleteContrat(selectedContrat.value.idContrat, user.value.idUtilisateur)
            break
          case 'refuse':
            await contratApi.refuserContratAdmin(
              selectedContrat.value.idContrat,
              user.value.idUtilisateur,
              refusMotif.value
            )
            break
        }

        showConfirmModal.value = false
        await loadContrats()
      } catch (err) {
        error.value = err.message
      } finally {
        isActionLoading.value = false
        refusMotif.value = ''
      }
    }

    onMounted(() => {
      loadContrats()
    })

    return {
      contrats,
      statistiques,
      isLoading,
      error,
      isActionLoading,
      selectedStatus,
      searchTerm,
      currentPage,
      showConfirmModal,
      confirmModal,
      refusMotif,
      hasActiveFilters,
      filteredContrats,
      totalPages,
      paginatedContrats,
      loadContrats,
      applyFilters,
      resetFilters,
      getStatutLabel,
      getStatutBadgeClass,
      formatDate,
      canEdit,
      canSend,
      canValidate,
      canDelete,
      editContrat,
      sendForSignature,
      validateContrat,
      deleteContrat,
      confirmAction
    }
  }
}
</script>

<template>
  <div class="admin-contrat-list-view">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1><i class="fas fa-file-contract"></i> Gestion des contrats</h1>
          <p>Gérez tous les contrats EcoDeli</p>
        </div>

        <div class="header-actions">
          <router-link to="/admin/contrats/create" class="btn-create">
            <i class="fas fa-plus"></i> Nouveau contrat
          </router-link>
        </div>
      </div>
    </div>

    <div class="page-content">
      <div class="content-container">
        <!-- Filtres et statistiques -->
        <div class="filters-section">
          <div class="stats-cards">
            <div class="stat-card total">
              <div class="stat-icon">
                <i class="fas fa-file-contract"></i>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statistiques.totalContrats || 0 }}</div>
                <div class="stat-label">Total contrats</div>
              </div>
            </div>

            <div class="stat-card active">
              <div class="stat-icon">
                <i class="fas fa-check-circle"></i>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statistiques.contratsActifs || 0 }}</div>
                <div class="stat-label">Contrats actifs</div>
              </div>
            </div>

            <div class="stat-card pending">
              <div class="stat-icon">
                <i class="fas fa-clock"></i>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statistiques.contratsEnAttente || 0 }}</div>
                <div class="stat-label">En attente</div>
              </div>
            </div>
          </div>

          <div class="filters">
            <div class="filter-group">
              <label for="statusFilter">Filtrer par statut :</label>
              <select id="statusFilter" v-model="selectedStatus" @change="applyFilters">
                <option value="">Tous les statuts</option>
                <option value="BROUILLON">Brouillon</option>
                <option value="ATTENTE_SIGNATURE_COMMERCANT">En attente commerçant</option>
                <option value="ATTENTE_SIGNATURE_ADMIN">En attente admin</option>
                <option value="ACTIF">Actif</option>
                <option value="REFUSE">Refusé</option>
                <option value="RESILIE">Résilié</option>
              </select>
            </div>

            <div class="filter-group">
              <label for="searchFilter">Rechercher :</label>
              <input 
                id="searchFilter"
                type="text"
                v-model="searchTerm"
                @input="applyFilters"
                placeholder="Numéro contrat, nom commerçant..."
              />
            </div>

            <button @click="resetFilters" class="btn-reset">
              <i class="fas fa-undo"></i> Réinitialiser
            </button>
          </div>
        </div>

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
          <p>Chargement des contrats...</p>
        </div>

        <!-- Tableau des contrats -->
        <div v-if="!isLoading && !error" class="contrats-table-container">
          <div v-if="filteredContrats.length === 0" class="empty-state">
            <div class="empty-icon">
              <i class="fas fa-file-contract"></i>
            </div>
            <h3>Aucun contrat trouvé</h3>
            <p v-if="hasActiveFilters">
              Aucun contrat ne correspond à vos critères de recherche.
            </p>
            <p v-else>
              Aucun contrat n'a encore été créé.
            </p>
            <router-link to="/admin/contrats/create" class="btn-primary">
              <i class="fas fa-plus"></i> Créer le premier contrat
            </router-link>
          </div>

          <div v-else class="table-wrapper">
            <table class="contrats-table">
              <thead>
                <tr>
                  <th>Numéro</th>
                  <th>Commerçant</th>
                  <th>Commission</th>
                  <th>Statut</th>
                  <th>Date création</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="contrat in paginatedContrats"
                  :key="contrat.idContrat"
                  class="contrat-row"
                >
                  <td>
                    <div class="contrat-number">
                      {{ contrat.numeroContrat }}
                    </div>
                  </td>

                  <td>
                    <div class="commercant-info">
                      <div class="commercant-name">
                        {{ contrat.commercant?.nom }} {{ contrat.commercant?.prenom }}
                      </div>
                      <div class="commercant-email">
                        {{ contrat.commercant?.email }}
                      </div>
                    </div>
                  </td>

                  <td>
                    <div class="commission-info">
                      <div class="commission-rate">{{ contrat.commissionPourcentage }}%</div>
                      <div v-if="contrat.fraisFixeMensuel > 0" class="monthly-fee">
                        + {{ contrat.fraisFixeMensuel }}€/mois
                      </div>
                    </div>
                  </td>

                  <td>
                    <span :class="['status-badge', getStatutBadgeClass(contrat.statut)]">
                      {{ getStatutLabel(contrat.statut) }}
                    </span>
                  </td>

                  <td>
                    <div class="date-info">
                      {{ formatDate(contrat.dateCreation) }}
                    </div>
                  </td>

                  <td>
                    <div class="actions">
                      <router-link
                        :to="`/admin/contrats/${contrat.idContrat}`"
                        class="btn-action view"
                        title="Voir les détails"
                      >
                        <i class="fas fa-eye"></i>
                      </router-link>

                      <button
                        v-if="canEdit(contrat)"
                        @click="editContrat(contrat)"
                        class="btn-action edit"
                        title="Modifier"
                      >
                        <i class="fas fa-edit"></i>
                      </button>

                      <button
                        v-if="canSend(contrat)"
                        @click="sendForSignature(contrat)"
                        class="btn-action send"
                        title="Envoyer pour signature"
                      >
                        <i class="fas fa-paper-plane"></i>
                      </button>

                      <button
                        v-if="canValidate(contrat)"
                        @click="validateContrat(contrat)"
                        class="btn-action validate"
                        title="Valider le contrat"
                      >
                        <i class="fas fa-check"></i>
                      </button>

                      <button
                        v-if="canDelete(contrat)"
                        @click="deleteContrat(contrat)"
                        class="btn-action delete"
                        title="Supprimer"
                      >
                        <i class="fas fa-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>

            <!-- Pagination -->
            <div v-if="totalPages > 1" class="pagination">
              <button
                @click="currentPage = 1"
                :disabled="currentPage === 1"
                class="btn-page"
              >
                <i class="fas fa-angle-double-left"></i>
              </button>

              <button
                @click="currentPage--"
                :disabled="currentPage === 1"
                class="btn-page"
              >
                <i class="fas fa-angle-left"></i>
              </button>

              <span class="page-info">
                Page {{ currentPage }} sur {{ totalPages }}
              </span>

              <button
                @click="currentPage++"
                :disabled="currentPage === totalPages"
                class="btn-page"
              >
                <i class="fas fa-angle-right"></i>
              </button>

              <button
                @click="currentPage = totalPages"
                :disabled="currentPage === totalPages"
                class="btn-page"
              >
                <i class="fas fa-angle-double-right"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de confirmation -->
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
              rows="3"
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
.admin-contrat-list-view {
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

.btn-create {
  background: var(--primary-color);
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.btn-create:hover {
  background: var(--secondary-color);
  text-decoration: none;
  color: white;
}

.page-content {
  padding: 2rem 0;
}

.content-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* Statistiques */
.filters-section {
  background: white;
  border-radius: 12px;
  box-shadow: var(--shadow);
  padding: 2rem;
  margin-bottom: 2rem;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  padding: 1.5rem;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-card.active {
  background: linear-gradient(135deg, #28a745, #20c997);
}

.stat-card.pending {
  background: linear-gradient(135deg, #ffc107, #fd7e14);
}

.stat-icon {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.9;
}

/* Filtres */
.filters {
  display: flex;
  gap: 1.5rem;
  align-items: end;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  font-weight: 500;
  color: var(--text-color);
  font-size: 0.9rem;
}

.filter-group select,
.filter-group input {
  padding: 0.6rem;
  border: 1px solid var(--border-light);
  border-radius: 6px;
  font-size: 0.9rem;
  min-width: 180px;
}

.btn-reset {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.6rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
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

/* Tableau */
.contrats-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: var(--shadow);
  overflow: hidden;
}

.table-wrapper {
  overflow-x: auto;
}

.contrats-table {
  width: 100%;
  border-collapse: collapse;
}

.contrats-table th {
  background: var(--bg-secondary);
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: var(--text-color);
  border-bottom: 2px solid var(--border-light);
}

.contrats-table td {
  padding: 1rem;
  border-bottom: 1px solid var(--border-light);
  vertical-align: middle;
}

.contrat-row:hover {
  background: var(--bg-secondary);
}

.contrat-number {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: var(--primary-color);
}

.commercant-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.commercant-name {
  font-weight: 500;
  color: var(--text-color);
}

.commercant-email {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.commission-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.commission-rate {
  font-weight: 600;
  color: var(--primary-color);
  font-size: 1.1rem;
}

.monthly-fee {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.date-info {
  font-size: 0.9rem;
  color: var(--text-secondary);
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

.actions {
  display: flex;
  gap: 0.5rem;
}

.btn-action {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  text-decoration: none;
  font-size: 0.85rem;
}

.btn-action.view { background: #007bff; color: white; }
.btn-action.edit { background: #28a745; color: white; }
.btn-action.send { background: #17a2b8; color: white; }
.btn-action.validate { background: #28a745; color: white; }
.btn-action.delete { background: #dc3545; color: white; }

.btn-action:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

/* État vide */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
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
}

.empty-state p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  line-height: 1.5;
}

.btn-primary {
  background: var(--primary-color);
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  padding: 1.5rem;
  border-top: 1px solid var(--border-light);
}

.btn-page {
  width: 36px;
  height: 36px;
  border: 1px solid var(--border-light);
  background: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.btn-page:hover:not(:disabled) {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  margin: 0 1rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
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

.btn-primary.delete {
  background: #dc3545;
}

.btn-primary.send {
  background: #17a2b8;
}

.btn-primary.validate {
  background: #28a745;
}

/* Responsive */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-group select,
  .filter-group input {
    min-width: auto;
  }

  .contrats-table th,
  .contrats-table td {
    padding: 0.8rem 0.5rem;
  }

  .actions {
    flex-direction: column;
  }
}

/* Dark theme */
[data-theme="dark"] .admin-contrat-list-view {
  background: linear-gradient(135deg, #1e1e1e 0%, #2d3e2d 100%);
}

[data-theme="dark"] .page-header,
[data-theme="dark"] .filters-section,
[data-theme="dark"] .contrats-table-container,
[data-theme="dark"] .modal {
  background: var(--bg-secondary);
}

[data-theme="dark"] .contrats-table th {
  background: var(--bg-color);
}
</style>
