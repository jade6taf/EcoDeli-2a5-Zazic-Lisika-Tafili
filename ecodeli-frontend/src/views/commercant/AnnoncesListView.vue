<script>
import { ref, onMounted, computed } from 'vue'
import { authStore } from '@/store/auth'
import { useRouter } from 'vue-router'

export default {
  name: 'AnnoncesListView',
  setup() {
    const router = useRouter()
    const annonces = ref([])
    const filteredAnnonces = ref([])
    const isLoading = ref(true)
    const error = ref(null)
    const isActionLoading = ref(false)

    const searchQuery = ref('')
    const selectedStatut = ref('')
    const sortBy = ref('dateCreation')
    const sortOrder = ref('desc')

    const showCancelModal = ref(false)
    const showDeleteModal = ref(false)
    const selectedAnnonce = ref(null)
    const activeDropdown = ref(null)

    const user = computed(() => authStore.user)

    const loadAnnonces = async () => {
      isLoading.value = true
      error.value = null

      try {
        const response = await fetch(`http://localhost:8080/api/commercants/${user.value.idUtilisateur}/annonces`, {
          headers: {
            'Authorization': `Bearer ${authStore.token}`,
            'Content-Type': 'application/json'
          }
        })

        if (response.ok) {
          annonces.value = await response.json()
          applyFilters()
        } else {
          throw new Error('Erreur lors du chargement des annonces')
        }
      } catch (err) {
        error.value = err.message
        console.error('Erreur:', err)
      } finally {
        isLoading.value = false
      }
    }

    const applyFilters = () => {
      let filtered = [...annonces.value]

      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(annonce =>
          annonce.titre.toLowerCase().includes(query) ||
          annonce.adresseDepart.toLowerCase().includes(query) ||
          annonce.adresseFin.toLowerCase().includes(query) ||
          (annonce.description && annonce.description.toLowerCase().includes(query))
        )
      }

      if (selectedStatut.value) {
        filtered = filtered.filter(annonce => annonce.statut === selectedStatut.value)
      }

      filtered.sort((a, b) => {
        let aValue = a[sortBy.value]
        let bValue = b[sortBy.value]

        if (sortBy.value === 'dateCreation' || sortBy.value === 'dateDebut') {
          aValue = new Date(aValue)
          bValue = new Date(bValue)
        }

        if (aValue < bValue)
          return sortOrder.value === 'asc' ? -1 : 1
        if (aValue > bValue)
          return sortOrder.value === 'asc' ? 1 : -1
        return 0
      })

      filteredAnnonces.value = filtered
    }

    const toggleSortOrder = () => {
      sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
      applyFilters()
    }

    const clearFilters = () => {
      searchQuery.value = ''
      selectedStatut.value = ''
      applyFilters()
    }

    const getStatutBadgeClass = (statut) => {
      const classes = {
        'PUBLIEE': 'badge-success',
        'VALIDEE': 'badge-info',
        'EN_COURS': 'badge-warning',
        'TERMINEE': 'badge-secondary',
        'ANNULEE': 'badge-danger',
        'SEGMENT_1_PRIS': 'badge-info',
        'SEGMENT_2_PRIS': 'badge-info',
        'SEGMENTS_COMPLETS': 'badge-warning'
      }
      return classes[statut] || 'badge-default'
    }

    const getStatutLabel = (statut) => {
      const labels = {
        'PUBLIEE': 'Publiée',
        'VALIDEE': 'Validée',
        'EN_COURS': 'En cours',
        'TERMINEE': 'Terminée',
        'ANNULEE': 'Annulée',
        'SEGMENT_1_PRIS': 'Segment 1 pris',
        'SEGMENT_2_PRIS': 'Segment 2 pris',
        'SEGMENTS_COMPLETS': 'Segments complets'
      }
      return labels[statut] || statut
    }

    const canEdit = (statut) => {
      return statut === 'PUBLIEE'
    }

    const canCancel = (statut) => {
      return ['PUBLIEE', 'VALIDEE', 'EN_COURS', 'SEGMENT_1_PRIS', 'SEGMENT_2_PRIS', 'SEGMENTS_COMPLETS'].includes(statut)
    }

    const canDelete = (statut) => {
      return statut === 'PUBLIEE'
    }

    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleDateString('fr-FR')
    }

    const formatDateTime = (dateString) => {
      return new Date(dateString).toLocaleString('fr-FR')
    }

    const toggleDropdown = (annonceId) => {
      activeDropdown.value = activeDropdown.value === annonceId ? null : annonceId
    }

    const closeDropdown = () => {
      activeDropdown.value = null
    }

    const editAnnonce = (annonce) => {
      router.push(`/commercant/annonces/${annonce.idAnnonce}/edit`)
    }

    const confirmCancel = (annonce) => {
      selectedAnnonce.value = annonce
      showCancelModal.value = true
      activeDropdown.value = null
    }

    const confirmDelete = (annonce) => {
      selectedAnnonce.value = annonce
      showDeleteModal.value = true
      activeDropdown.value = null
    }

    const cancelAnnonce = async () => {
      isActionLoading.value = true

      try {
        const response = await fetch(`http://localhost:8080/api/commercants/${user.value.idUtilisateur}/annonces/${selectedAnnonce.value.idAnnonce}/annuler`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${authStore.token}`,
            'Content-Type': 'application/json'
          }
        })

        if (response.ok) {
          await loadAnnonces()
          showCancelModal.value = false
        } else {
          const errorData = await response.json()
          throw new Error(errorData.message || 'Erreur lors de l\'annulation')
        }
      } catch (err) {
        error.value = err.message
      } finally {
        isActionLoading.value = false
      }
    }

    const deleteAnnonce = async () => {
      isActionLoading.value = true

      try {
        const response = await fetch(`http://localhost:8080/api/commercants/${user.value.idUtilisateur}/annonces/${selectedAnnonce.value.idAnnonce}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${authStore.token}`,
            'Content-Type': 'application/json'
          }
        })

        if (response.ok) {
          await loadAnnonces()
          showDeleteModal.value = false
        } else {
          const errorData = await response.json()
          throw new Error(errorData.message || 'Erreur lors de la suppression')
        }
      } catch (err) {
        error.value = err.message
      } finally {
        isActionLoading.value = false
      }
    }

    onMounted(() => {
      if (user.value?.idUtilisateur) {
        loadAnnonces()
      }
    })

    return {
      annonces,
      filteredAnnonces,
      isLoading,
      error,
      isActionLoading,
      searchQuery,
      selectedStatut,
      sortBy,
      sortOrder,
      showCancelModal,
      showDeleteModal,
      selectedAnnonce,
      activeDropdown,
      loadAnnonces,
      applyFilters,
      toggleSortOrder,
      clearFilters,
      getStatutBadgeClass,
      getStatutLabel,
      canEdit,
      canCancel,
      canDelete,
      formatDate,
      formatDateTime,
      toggleDropdown,
      closeDropdown,
      editAnnonce,
      confirmCancel,
      confirmDelete,
      cancelAnnonce,
      deleteAnnonce
    }
  },
  directives: {
    'click-away': {
      mounted(el, binding) {
        el._clickAwayHandler = (event) => {
          if (!el.contains(event.target)) {
            binding.value()
          }
        }
        document.addEventListener('click', el._clickAwayHandler)
      },
      unmounted(el) {
        document.removeEventListener('click', el._clickAwayHandler)
      }
    }
  }
}
</script>

<template>
  <div class="annonces-list-view">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1><i class="fas fa-list"></i> Mes annonces</h1>
          <p>Gérez toutes vos demandes de livraison</p>
        </div>
        <div class="header-actions">
          <router-link to="/commercant/annonces/new" class="btn-primary">
            <i class="fas fa-plus"></i> Nouvelle annonce
          </router-link>
        </div>
      </div>
    </div>

    <div class="page-content">
      <div class="content-container">
        <!-- Filtres et recherche -->
        <div class="filters-section">
          <div class="filters-row">
            <div class="search-group">
              <div class="search-input">
                <i class="fas fa-search"></i>
                <input
                  type="text"
                  v-model="searchQuery"
                  placeholder="Rechercher par titre, adresse..."
                  @input="applyFilters"
                />
              </div>
            </div>

            <div class="filter-group">
              <select v-model="selectedStatut" @change="applyFilters">
                <option value="">Tous les statuts</option>
                <option value="PUBLIEE">Publiées</option>
                <option value="VALIDEE">Validées</option>
                <option value="EN_COURS">En cours</option>
                <option value="TERMINEE">Terminées</option>
                <option value="ANNULEE">Annulées</option>
              </select>
            </div>

            <div class="filter-group">
              <select v-model="sortBy" @change="applyFilters">
                <option value="dateCreation">Date de création</option>
                <option value="titre">Titre</option>
                <option value="prixUnitaire">Prix</option>
                <option value="statut">Statut</option>
              </select>
            </div>

            <button @click="toggleSortOrder" class="sort-btn" :title="sortOrder === 'desc' ? 'Tri décroissant' : 'Tri croissant'">
              <i :class="sortOrder === 'desc' ? 'fas fa-sort-amount-down' : 'fas fa-sort-amount-up'"></i>
            </button>
          </div>

          <div class="filters-summary" v-if="filteredAnnonces.length !== annonces.length || searchQuery">
            {{ filteredAnnonces.length }} annonce(s) trouvée(s)
            <button v-if="searchQuery || selectedStatut" @click="clearFilters" class="clear-filters">
              <i class="fas fa-times"></i> Effacer les filtres
            </button>
          </div>
        </div>

        <!-- Messages d'état -->
        <div v-if="error" class="error-message">
          <i class="fas fa-exclamation-triangle"></i>
          {{ error }}
          <button @click="loadAnnonces()" class="retry-btn">
            <i class="fas fa-redo"></i> Réessayer
          </button>
        </div>

        <div v-if="isLoading" class="loading-state">
          <div class="spinner"></div>
          <p>Chargement de vos annonces...</p>
        </div>

        <!-- Liste des annonces -->
        <div v-if="!isLoading && !error" class="annonces-grid">
          <!-- État vide -->
          <div v-if="filteredAnnonces.length === 0 && !searchQuery && !selectedStatut" class="empty-state">
            <div class="empty-icon">
              <i class="fas fa-bullhorn"></i>
            </div>
            <h3>Aucune annonce pour le moment</h3>
            <p>Commencez par créer votre première demande de livraison</p>
            <router-link to="/commercant/annonces/new" class="btn-primary">
              <i class="fas fa-plus"></i> Créer ma première annonce
            </router-link>
          </div>

          <!-- Résultats de recherche vides -->
          <div v-else-if="filteredAnnonces.length === 0" class="empty-search">
            <div class="empty-icon">
              <i class="fas fa-search"></i>
            </div>
            <h3>Aucun résultat trouvé</h3>
            <p>Essayez de modifier vos critères de recherche</p>
          </div>

          <!-- Cartes d'annonces -->
          <div v-for="annonce in filteredAnnonces" :key="annonce.idAnnonce" class="annonce-card">
            <div class="card-header">
              <div class="card-title">
                <h3>{{ annonce.titre }}</h3>
                <span :class="['status-badge', getStatutBadgeClass(annonce.statut)]">
                  {{ getStatutLabel(annonce.statut) }}
                </span>
              </div>
              <div class="card-actions">
                <div class="dropdown" v-click-away="() => closeDropdown(annonce.idAnnonce)">
                  <button @click="toggleDropdown(annonce.idAnnonce)" class="dropdown-toggle">
                    <i class="fas fa-ellipsis-v"></i>
                  </button>
                  <div v-if="activeDropdown === annonce.idAnnonce" class="dropdown-menu">
                    <router-link :to="`/commercant/annonces/${annonce.idAnnonce}`" class="dropdown-item">
                      <i class="fas fa-eye"></i> Voir les détails
                    </router-link>
                    <button
                      v-if="canEdit(annonce.statut)"
                      @click="editAnnonce(annonce)"
                      class="dropdown-item"
                    >
                      <i class="fas fa-edit"></i> Modifier
                    </button>
                    <button
                      v-if="canCancel(annonce.statut)"
                      @click="confirmCancel(annonce)"
                      class="dropdown-item danger"
                    >
                      <i class="fas fa-times"></i> Annuler
                    </button>
                    <button
                      v-if="canDelete(annonce.statut)"
                      @click="confirmDelete(annonce)"
                      class="dropdown-item danger"
                    >
                      <i class="fas fa-trash"></i> Supprimer
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div class="card-content">
              <div class="route-info">
                <div class="route-point start">
                  <i class="fas fa-circle"></i>
                  <span>{{ annonce.adresseDepart }}</span>
                </div>
                <div class="route-line">
                  <i class="fas fa-arrow-down"></i>
                </div>
                <div class="route-point end">
                  <i class="fas fa-map-marker-alt"></i>
                  <span>{{ annonce.adresseFin }}</span>
                </div>
              </div>

              <div class="annonce-details">
                <div class="detail-row">
                  <div class="detail-item">
                    <i class="fas fa-euro-sign"></i>
                    <span>{{ annonce.prixUnitaire }}€</span>
                  </div>
                  <div class="detail-item">
                    <i class="fas fa-calendar"></i>
                    <span>{{ formatDate(annonce.dateCreation) }}</span>
                  </div>
                </div>

                <div class="detail-row" v-if="annonce.dateDebut">
                  <div class="detail-item">
                    <i class="fas fa-clock"></i>
                    <span>{{ formatDateTime(annonce.dateDebut) }}</span>
                  </div>
                  <div class="detail-item" v-if="annonce.livraisonPartielleAutorisee">
                    <i class="fas fa-cut"></i>
                    <span>Livraison partielle</span>
                  </div>
                </div>
              </div>

              <div v-if="annonce.description" class="description">
                {{ annonce.description }}
              </div>
            </div>

            <div class="card-footer">
              <router-link :to="`/commercant/annonces/${annonce.idAnnonce}`" class="btn-view">
                <i class="fas fa-eye"></i> Voir les détails
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modales de confirmation -->
    <div v-if="showCancelModal" class="modal-overlay" @click="showCancelModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Confirmer l'annulation</h3>
        </div>
        <div class="modal-body">
          <p>Êtes-vous sûr de vouloir annuler l'annonce "{{ selectedAnnonce?.titre }}" ?</p>
          <p class="warning">Cette action est irréversible.</p>
        </div>
        <div class="modal-footer">
          <button @click="showCancelModal = false" class="btn-secondary">Annuler</button>
          <button @click="cancelAnnonce" class="btn-danger" :disabled="isActionLoading">
            <i v-if="isActionLoading" class="fas fa-spinner fa-spin"></i>
            <span v-else>Confirmer l'annulation</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Confirmer la suppression</h3>
        </div>
        <div class="modal-body">
          <p>Êtes-vous sûr de vouloir supprimer définitivement l'annonce "{{ selectedAnnonce?.titre }}" ?</p>
          <p class="warning">Cette action est irréversible et supprimera toutes les données associées.</p>
        </div>
        <div class="modal-footer">
          <button @click="showDeleteModal = false" class="btn-secondary">Annuler</button>
          <button @click="deleteAnnonce" class="btn-danger" :disabled="isActionLoading">
            <i v-if="isActionLoading" class="fas fa-spinner fa-spin"></i>
            <span v-else>Supprimer définitivement</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.annonces-list-view {
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
  justify-content: between;
  align-items: center;
  gap: 2rem;
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

.btn-primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
  text-decoration: none;
  color: white;
}

.page-content {
  padding: 2rem 0;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* Filtres */
.filters-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow);
}

.filters-row {
  display: flex;
  gap: 1rem;
  align-items: center;
  margin-bottom: 1rem;
}

.search-group {
  flex: 1;
}

.search-input {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input i {
  position: absolute;
  left: 1rem;
  color: var(--text-secondary);
}

.search-input input {
  width: 100%;
  padding: 0.8rem 1rem 0.8rem 2.5rem;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  font-size: 1rem;
}

.filter-group select,
.sort-btn {
  padding: 0.8rem 1rem;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  background: white;
  font-size: 1rem;
  cursor: pointer;
}

.sort-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.sort-btn:hover {
  background: var(--bg-secondary);
  color: var(--primary-color);
}

.filters-summary {
  font-size: 0.9rem;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.clear-filters {
  background: none;
  border: none;
  color: var(--primary-color);
  cursor: pointer;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.3rem;
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

/* Grille d'annonces */
.annonces-grid {
  display: grid;
  gap: 1.5rem;
}

.annonce-card {
  background: white;
  border-radius: 12px;
  box-shadow: var(--shadow);
  overflow: hidden;
  transition: all 0.3s ease;
}

.annonce-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.card-header {
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: between;
  align-items: flex-start;
  gap: 1rem;
}

.card-title {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 1rem;
}

.card-title h3 {
  font-size: 1.2rem;
  color: var(--text-color);
  margin: 0;
  font-weight: 600;
  flex: 1;
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
.badge-default { background: #f8f9fa; color: #6c757d; }

.card-actions {
  position: relative;
}

.dropdown-toggle {
  background: none;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  color: var(--text-secondary);
  border-radius: 50%;
  transition: all 0.3s ease;
}

.dropdown-toggle:hover {
  background: var(--bg-secondary);
  color: var(--primary-color);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  box-shadow: var(--shadow);
  min-width: 180px;
  z-index: 10;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.8rem 1rem;
  color: var(--text-color);
  text-decoration: none;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  font-size: 0.9rem;
}

.dropdown-item:hover {
  background: var(--bg-secondary);
}

.dropdown-item.danger {
  color: #dc3545;
}

.dropdown-item.danger:hover {
  background: #f8d7da;
}

.card-content {
  padding: 1.5rem;
}

.route-info {
  margin-bottom: 1.5rem;
}

.route-point {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  margin: 0.5rem 0;
  font-size: 0.95rem;
}

.route-point.start i {
  color: #28a745;
}

.route-point.end i {
  color: #dc3545;
}

.route-line {
  display: flex;
  justify-content: center;
  color: var(--text-secondary);
  margin: 0.3rem 0;
}

.annonce-details {
  display: grid;
  gap: 0.8rem;
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

.description {
  margin-top: 1rem;
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 8px;
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.card-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--border-light);
  display: flex;
  justify-content: flex-end;
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
}

.btn-view:hover {
  background: var(--secondary-color);
  text-decoration: none;
  color: white;
}

/* États vides */
.empty-state {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 15px;
  box-shadow: var(--shadow);
}

.empty-search {
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

.empty-state h3,
.empty-search h3 {
  color: var(--text-color);
  margin-bottom: 1rem;
  font-size: 1.3rem;
}

.empty-state p,
.empty-search p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  font-size: 1.1rem;
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
  .header-content {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }

  .filters-row {
    flex-direction: column;
    gap: 1rem;
  }

  .search-group {
    order: -1;
  }

  .detail-row {
    flex-direction: column;
    gap: 0.5rem;
  }

  .card-title {
    flex-direction: column;
    gap: 0.5rem;
  }
}

/* Dark theme */
[data-theme="dark"] .annonces-list-view {
  background: linear-gradient(135deg, #1e1e1e 0%, #2d3e2d 100%);
}

[data-theme="dark"] .page-header {
  background: var(--bg-secondary);
}

[data-theme="dark"] .filters-section,
[data-theme="dark"] .annonce-card,
[data-theme="dark"] .empty-state,
[data-theme="dark"] .empty-search,
[data-theme="dark"] .modal {
  background: var(--bg-secondary);
}

[data-theme="dark"] .search-input input,
[data-theme="dark"] .filter-group select {
  background: var(--bg-color);
  border-color: var(--border-color);
  color: var(--text-color);
}

[data-theme="dark"] .dropdown-menu {
  background: var(--bg-color);
  border-color: var(--border-color);
}
</style>
