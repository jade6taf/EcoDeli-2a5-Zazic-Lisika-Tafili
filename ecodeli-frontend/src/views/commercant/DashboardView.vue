<script>
import { useI18n } from 'vue-i18n'
import { authStore } from '@/store/auth'
import { ref, onMounted, computed } from 'vue'

export default {
  name: 'CommercantDashboard',
  setup() {
    const { t } = useI18n()
    const statistiques = ref({})
    const annoncesRecentes = ref([])
    const isLoading = ref(true)
    const error = ref(null)

    const user = computed(() => authStore.user)
    const nomCommerce = computed(() => user.value?.nomCommerce || 'Votre commerce')

    const fetchStatistiques = async () => {
      try {
        const response = await fetch(`/api/commercants/${user.value.idUtilisateur}/statistiques`, {
          headers: {
            'Authorization': `Bearer ${authStore.token}`,
            'Content-Type': 'application/json'
          }
        })

        if (response.ok) {
          statistiques.value = await response.json()
        } else {
          throw new Error('Erreur lors du chargement des statistiques')
        }
      } catch (err) {
        error.value = err.message
        console.error('Erreur:', err)
      }
    }

    const fetchAnnoncesRecentes = async () => {
      try {
        const response = await fetch(`/api/commercants/${user.value.idUtilisateur}/annonces`, {
          headers: {
            'Authorization': `Bearer ${authStore.token}`,
            'Content-Type': 'application/json'
          }
        })

        if (response.ok) {
          const annonces = await response.json()
          annoncesRecentes.value = annonces
            .sort((a, b) => new Date(b.dateCreation) - new Date(a.dateCreation))
            .slice(0, 5)
        } else {
          throw new Error('Erreur lors du chargement des annonces')
        }
      } catch (err) {
        error.value = err.message
        console.error('Erreur:', err)
      }
    }

    const loadDashboardData = async () => {
      isLoading.value = true
      error.value = null

      try {
        await Promise.all([
          fetchStatistiques(),
          fetchAnnoncesRecentes()
        ])
      } catch (err) {
        error.value = 'Erreur lors du chargement des données'
      } finally {
        isLoading.value = false
      }
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

    onMounted(() => {
      if (user.value?.idUtilisateur) {
        loadDashboardData()
      }
    })

    return {
      t,
      user,
      nomCommerce,
      statistiques,
      annoncesRecentes,
      isLoading,
      error,
      loadDashboardData,
      getStatutBadgeClass,
      getStatutLabel
    }
  }
}
</script>

<template>
  <div class="commercant-dashboard">
    <div class="dashboard-header">
      <div class="header-content">
        <div class="welcome-section">
          <div class="welcome-icon">
            <i class="fas fa-store"></i>
          </div>
          <div class="welcome-text">
            <h1>{{ t('commercant.dashboard.welcome') }}</h1>
            <h2>{{ nomCommerce }} !</h2>
            <p>{{ t('commercant.dashboard.subtitle') }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="dashboard-content">
      <div class="content-container">
        <!-- Affichage des erreurs -->
        <div v-if="error" class="error-message">
          <i class="fas fa-exclamation-triangle"></i>
          {{ error }}
          <button @click="loadDashboardData()" class="retry-btn">
            <i class="fas fa-redo"></i> Réessayer
          </button>
        </div>

        <!-- Statistiques principales -->
        <div v-if="!isLoading && !error" class="stats-grid">
          <div class="stat-card primary">
            <div class="stat-icon">
              <i class="fas fa-bullhorn"></i>
            </div>
            <div class="stat-info">
              <h3>{{ statistiques.totalAnnonces || 0 }}</h3>
              <p>Total des annonces</p>
            </div>
          </div>

          <div class="stat-card success">
            <div class="stat-icon">
              <i class="fas fa-eye"></i>
            </div>
            <div class="stat-info">
              <h3>{{ statistiques.annoncesActives || 0 }}</h3>
              <p>Annonces actives</p>
            </div>
          </div>

          <div class="stat-card warning">
            <div class="stat-icon">
              <i class="fas fa-truck"></i>
            </div>
            <div class="stat-info">
              <h3>{{ statistiques.annoncesEnCours || 0 }}</h3>
              <p>En cours de livraison</p>
            </div>
          </div>

          <div class="stat-card info">
            <div class="stat-icon">
              <i class="fas fa-check-circle"></i>
            </div>
            <div class="stat-info">
              <h3>{{ statistiques.annoncesTerminees || 0 }}</h3>
              <p>Annonces terminées</p>
            </div>
          </div>
        </div>

        <!-- Actions rapides -->
        <div v-if="!isLoading && !error" class="quick-actions">
          <h3>Actions rapides</h3>
          <div class="actions-grid">
            <router-link to="/commercant/annonces/new" class="action-card">
              <div class="action-icon">
                <i class="fas fa-plus"></i>
              </div>
              <div class="action-content">
                <h4>Créer une annonce</h4>
                <p>Publier une nouvelle demande de livraison</p>
              </div>
            </router-link>

            <router-link to="/commercant/annonces" class="action-card">
              <div class="action-icon">
                <i class="fas fa-list"></i>
              </div>
              <div class="action-content">
                <h4>Mes annonces</h4>
                <p>Gérer toutes vos annonces</p>
              </div>
            </router-link>

            <router-link to="/commercant/profile" class="action-card">
              <div class="action-icon">
                <i class="fas fa-store"></i>
              </div>
              <div class="action-content">
                <h4>Mon commerce</h4>
                <p>Modifier les informations de votre commerce</p>
              </div>
            </router-link>

            <router-link to="/commercant/contrats" class="action-card">
              <div class="action-icon">
                <i class="fas fa-file-contract"></i>
              </div>
              <div class="action-content">
                <h4>Mes contrats</h4>
                <p>Consulter et signer vos contrats EcoDeli</p>
              </div>
            </router-link>
          </div>
        </div>

        <!-- Annonces récentes -->
        <div v-if="!isLoading && !error && annoncesRecentes.length > 0" class="recent-announcements">
          <h3>Annonces récentes</h3>
          <div class="announcements-list">
            <div
              v-for="annonce in annoncesRecentes"
              :key="annonce.idAnnonce"
              class="announcement-card"
            >
              <div class="announcement-header">
                <h4>{{ annonce.titre }}</h4>
                <span :class="['status-badge', getStatutBadgeClass(annonce.statut)]">
                  {{ getStatutLabel(annonce.statut) }}
                </span>
              </div>
              <div class="announcement-details">
                <div class="detail-item">
                  <i class="fas fa-map-marker-alt"></i>
                  <span>{{ annonce.adresseDepart }} → {{ annonce.adresseFin }}</span>
                </div>
                <div class="detail-item">
                  <i class="fas fa-euro-sign"></i>
                  <span>{{ annonce.prixUnitaire }}€</span>
                </div>
                <div class="detail-item">
                  <i class="fas fa-calendar"></i>
                  <span>{{ new Date(annonce.dateCreation).toLocaleDateString('fr-FR') }}</span>
                </div>
              </div>
              <div class="announcement-actions">
                <router-link
                  :to="`/commercant/annonces/${annonce.idAnnonce}`"
                  class="btn-view"
                >
                  <i class="fas fa-eye"></i> Voir
                </router-link>
              </div>
            </div>
          </div>
        </div>

        <!-- Message si aucune annonce -->
        <div v-if="!isLoading && !error && annoncesRecentes.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="fas fa-bullhorn"></i>
          </div>
          <h3>Aucune annonce pour le moment</h3>
          <p>Commencez par créer votre première demande de livraison</p>
          <router-link to="/commercant/annonces/new" class="btn-primary">
            <i class="fas fa-plus"></i> Créer ma première annonce
          </router-link>
        </div>

        <!-- Loader -->
        <div v-if="isLoading" class="loading-state">
          <div class="spinner"></div>
          <p>Chargement de votre dashboard...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.commercant-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e8f5e9 100%);
}

.dashboard-header {
  background: white;
  box-shadow: var(--shadow);
  border-bottom: 3px solid var(--primary-color);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 3rem 2rem;
}

.welcome-section {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow);
}

.welcome-icon i {
  font-size: 2.5rem;
  color: white;
}

.welcome-text h1 {
  font-size: 2.5rem;
  color: var(--text-color);
  margin: 0 0 0.5rem 0;
  font-weight: 700;
}

.welcome-text h2 {
  font-size: 1.8rem;
  color: var(--primary-color);
  margin: 0 0 1rem 0;
  font-weight: 600;
}

.welcome-text p {
  font-size: 1.1rem;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.6;
}

.dashboard-content {
  padding: 3rem 0;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: grid;
  gap: 2rem;
}

/* Messages d'erreur et de chargement */
.error-message {
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 12px;
  padding: 1.5rem;
  color: #c33;
  display: flex;
  align-items: center;
  gap: 1rem;
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

/* Grille de statistiques */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 15px;
  padding: 1.5rem;
  box-shadow: var(--shadow);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;
  border-left: 4px solid var(--primary-color);
}

.stat-card.primary { border-left-color: var(--primary-color); }
.stat-card.success { border-left-color: #28a745; }
.stat-card.warning { border-left-color: #ffc107; }
.stat-card.info { border-left-color: #17a2b8; }

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
}

.stat-card.primary .stat-icon { background: var(--primary-color); }
.stat-card.success .stat-icon { background: #28a745; }
.stat-card.warning .stat-icon { background: #ffc107; }
.stat-card.info .stat-icon { background: #17a2b8; }

.stat-info h3 {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-color);
}

.stat-info p {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin: 0;
}

/* Actions rapides */
.quick-actions h3,
.recent-announcements h3 {
  color: var(--text-color);
  margin-bottom: 1.5rem;
  font-size: 1.3rem;
  font-weight: 600;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.action-card {
  background: white;
  border-radius: 15px;
  padding: 1.5rem;
  box-shadow: var(--shadow);
  display: flex;
  align-items: center;
  gap: 1rem;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.action-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
  border-color: var(--primary-color);
  text-decoration: none;
  color: inherit;
}

.action-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.action-content h4 {
  font-size: 1.1rem;
  color: var(--text-color);
  margin: 0 0 0.5rem 0;
  font-weight: 600;
}

.action-content p {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.4;
}

/* Annonces récentes */
.announcements-list {
  display: grid;
  gap: 1rem;
}

.announcement-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: var(--shadow);
  transition: all 0.3s ease;
}

.announcement-card:hover {
  box-shadow: var(--shadow-hover);
}

.announcement-header {
  display: flex;
  justify-content: between;
  align-items: flex-start;
  margin-bottom: 1rem;
  gap: 1rem;
}

.announcement-header h4 {
  font-size: 1.1rem;
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

.announcement-details {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
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

.announcement-actions {
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
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
  text-decoration: none;
  color: white;
}

/* Responsive Design */
@media (max-width: 768px) {
  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 1.5rem;
  }

  .welcome-text h1 {
    font-size: 2rem;
  }

  .welcome-text h2 {
    font-size: 1.5rem;
  }

  .header-content,
  .content-container {
    padding: 2rem 1rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: 1fr;
  }

  .action-card {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }

  .announcement-header {
    flex-direction: column;
    gap: 0.5rem;
  }

  .announcement-details {
    flex-direction: column;
    gap: 0.5rem;
  }
}

/* Dark Theme Support */
[data-theme="dark"] .commercant-dashboard {
  background: linear-gradient(135deg, #1e1e1e 0%, #2d3e2d 100%);
}

[data-theme="dark"] .dashboard-header {
  background: var(--bg-secondary);
}

[data-theme="dark"] .stat-card,
[data-theme="dark"] .action-card,
[data-theme="dark"] .announcement-card,
[data-theme="dark"] .empty-state {
  background: var(--bg-secondary);
}
</style>
