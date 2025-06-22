<script>
export default {
  name: 'PrestataireDashboard',
  data() {
    return {
      user: null,
      isLoading: true,
      statistiques: null,
      candidaturesRecentes: [],
      disponible: true,
      nouveauxServices: 0,
      servicesEnCours: 0,
      moyenneEvaluations: '4.5'
    }
  },
  async mounted() {
    await this.loadData();
  },
  methods: {
    async loadData() {
      this.isLoading = true;

      try {
        const userStr = localStorage.getItem('user');
        if (userStr) {
          this.user = JSON.parse(userStr);
        }

        await this.loadStatistiques();
        await this.loadCandidaturesRecentes();
        this.nouveauxServices = 1;
        this.servicesEnCours = 1;
        this.disponible = true;
      } catch (error) {
        console.error('Erreur lors du chargement des données:', error);
      } finally {
        this.isLoading = false;
      }
    },

    async loadStatistiques() {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/candidatures/mes-candidatures/statistiques', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          this.statistiques = await response.json();
        }
      } catch (error) {
        console.error('Erreur lors du chargement des statistiques:', error);
      }
    },

    async loadCandidaturesRecentes() {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/candidatures/mes-candidatures', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const candidatures = await response.json();
          this.candidaturesRecentes = candidatures.slice(0, 5); // 5 plus récentes
        }
      } catch (error) {
        console.error('Erreur lors du chargement des candidatures:', error);
      }
    },

    async updateDisponibilite() {
      try {
        // TODO: Implémenter l'API pour mettre à jour la disponibilité
        console.log('Disponibilité mise à jour:', this.disponible);
      } catch (error) {
        console.error('Erreur lors de la mise à jour de la disponibilité:', error);
        this.disponible = !this.disponible;
      }
    },

    toggleDisponibilite() {
      this.disponible = !this.disponible;
      this.updateDisponibilite();
    },

    getStatutLabel(statut) {
      const labels = {
        'EN_ATTENTE': 'En attente de réponse',
        'ACCEPTEE': 'Candidature acceptée',
        'REFUSEE': 'Candidature refusée'
      };
      return labels[statut] || statut;
    },

    getStatutClass(statut) {
      const classes = {
        'EN_ATTENTE': 'pending',
        'ACCEPTEE': 'success',
        'REFUSEE': 'error'
      };
      return classes[statut] || '';
    },

    getStatutIcon(statut) {
      const icons = {
        'EN_ATTENTE': 'fas fa-clock',
        'ACCEPTEE': 'fas fa-check-circle',
        'REFUSEE': 'fas fa-times-circle'
      };
      return icons[statut] || 'fas fa-question-circle';
    },

    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('fr-FR', {
        day: 'numeric',
        month: 'short'
      });
    }
  }
}
</script>

<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <h1>Dashboard Prestataire</h1>
      <div v-if="user" class="welcome-message">
        <h2>Bienvenue, {{ user.prenom }} {{ user.nom }}</h2>
        <p>Gérez vos candidatures et services depuis votre espace personnel</p>
      </div>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i> Chargement...
    </div>

    <div v-else>
      <div class="stats-section" v-if="statistiques">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-clipboard-list"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ statistiques.total }}</div>
            <div class="stat-label">Candidatures totales</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon pending">
            <i class="fas fa-clock"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ statistiques.en_attente }}</div>
            <div class="stat-label">En attente</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon success">
            <i class="fas fa-check-circle"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ statistiques.acceptees }}</div>
            <div class="stat-label">Acceptées</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon warning">
            <i class="fas fa-star"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ moyenneEvaluations }}</div>
            <div class="stat-label">Note moyenne</div>
          </div>
        </div>
      </div>

      <div class="main-actions">
        <router-link to="/prestataire/demandes-disponibles" class="action-card primary">
          <div class="card-icon">
            <i class="fas fa-search"></i>
          </div>
          <div class="card-content">
            <h3>Demandes disponibles</h3>
            <p>Découvrez les nouvelles opportunités de services</p>
            <div class="card-badge" v-if="nouveauxServices > 0">{{ nouveauxServices }} nouveaux</div>
          </div>
          <div class="card-arrow">
            <i class="fas fa-chevron-right"></i>
          </div>
        </router-link>

        <router-link to="/prestataire/mes-candidatures" class="action-card secondary">
          <div class="card-icon">
            <i class="fas fa-paper-plane"></i>
          </div>
          <div class="card-content">
            <h3>Mes candidatures</h3>
            <p>Suivez l'état de vos candidatures envoyées</p>
            <div class="card-badge" v-if="statistiques && statistiques.en_attente > 0">{{ statistiques.en_attente }} en attente</div>
          </div>
          <div class="card-arrow">
            <i class="fas fa-chevron-right"></i>
          </div>
        </router-link>

        <router-link to="/prestataire/services-en-cours" class="action-card success">
          <div class="card-icon">
            <i class="fas fa-tools"></i>
          </div>
          <div class="card-content">
            <h3>Services en cours</h3>
            <p>Gérez vos services actuellement actifs</p>
            <div class="card-badge" v-if="servicesEnCours > 0">{{ servicesEnCours }} actifs</div>
          </div>
          <div class="card-arrow">
            <i class="fas fa-chevron-right"></i>
          </div>
        </router-link>

        <router-link to="/prestataire/services-termines" class="action-card info">
          <div class="card-icon">
            <i class="fas fa-history"></i>
          </div>
          <div class="card-content">
            <h3>Historique</h3>
            <p>Consultez vos services terminés et évaluations</p>
          </div>
          <div class="card-arrow">
            <i class="fas fa-chevron-right"></i>
          </div>
        </router-link>
      </div>

      <div class="secondary-actions">
        <h3 class="section-title">Gestion du profil</h3>
        <div class="profile-actions">
          <router-link to="/prestataire/informations" class="profile-card">
            <i class="fas fa-user-edit"></i>
            <h4>Informations personnelles</h4>
            <p>Modifiez vos données personnelles</p>
          </router-link>

          <router-link to="/prestataire/profil" class="profile-card">
            <i class="fas fa-eye"></i>
            <h4>Profil public</h4>
            <p>Consultez votre profil visible par les clients</p>
          </router-link>

          <div class="profile-card disponibilite-card" @click="toggleDisponibilite">
            <i class="fas fa-toggle-on" :class="{ 'available': disponible, 'unavailable': !disponible }"></i>
            <h4>Disponibilité</h4>
            <p>{{ disponible ? 'Vous êtes disponible' : 'Vous êtes indisponible' }}</p>
            <div class="toggle-switch">
              <label class="switch">
                <input type="checkbox" v-model="disponible" @change="updateDisponibilite">
                <span class="slider"></span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <div class="recent-activity" v-if="candidaturesRecentes.length > 0">
        <h3 class="section-title">Activité récente</h3>
        <div class="activity-list">
          <div v-for="candidature in candidaturesRecentes" :key="candidature.idCandidature" class="activity-item">
            <div class="activity-icon" :class="getStatutClass(candidature.statut)">
              <i :class="getStatutIcon(candidature.statut)"></i>
            </div>
            <div class="activity-content">
              <h4>{{ candidature.demandeService?.titre || 'Demande sans titre' }}</h4>
              <p>{{ getStatutLabel(candidature.statut) }} • {{ formatDate(candidature.dateCandidature) }}</p>
            </div>
            <div class="activity-amount" v-if="candidature.prixPropose">
              {{ candidature.prixPropose }}€
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.welcome-section {
  margin-bottom: 2rem;
}

.welcome-section h1 {
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.welcome-message {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  border-left: 4px solid var(--primary-color);
  box-shadow: var(--shadow);
}

.welcome-message h2 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
}

.welcome-message p {
  color: var(--text-secondary);
  margin: 0;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: var(--text-primary);
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.stat-card {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  font-size: 1.2rem;
}

.stat-icon.pending {
  background-color: var(--warning-color);
}

.stat-icon.success {
  background-color: var(--success-color);
}

.stat-icon.warning {
  background-color: #ffc107;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-top: 0.25rem;
}

.main-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.action-card {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
  overflow: hidden;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.action-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background-color: var(--primary-color);
}

.action-card.secondary::before {
  background-color: var(--info-color);
}

.action-card.success::before {
  background-color: var(--success-color);
}

.action-card.info::before {
  background-color: #6c757d;
}

.card-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color-light);
  color: var(--primary-color);
  font-size: 1.3rem;
}

.card-content {
  flex: 1;
}

.card-content h3 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
}

.card-content p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 0.9rem;
}

.card-badge {
  background-color: var(--primary-color);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
  margin-top: 0.5rem;
  display: inline-block;
}

.card-arrow {
  color: var(--text-secondary);
  font-size: 1.2rem;
}

.secondary-actions {
  margin-bottom: 3rem;
}

.section-title {
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid var(--border-color);
}

.profile-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.profile-card {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
  text-decoration: none;
  text-align: center;
  transition: transform 0.3s ease;
}

.profile-card:hover {
  transform: translateY(-2px);
}

.profile-card i {
  font-size: 2rem;
  color: var(--primary-color);
  margin-bottom: 1rem;
}

.profile-card h4 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
}

.profile-card p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 0.9rem;
}

.disponibilite-card {
  cursor: pointer;
  position: relative;
}

.disponibilite-card .fas.fa-toggle-on.available {
  color: var(--success-color);
}

.disponibilite-card .fas.fa-toggle-on.unavailable {
  color: var(--text-secondary);
}

.toggle-switch {
  margin-top: 1rem;
}

.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--border-color);
  transition: .4s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: var(--success-color);
}

input:checked + .slider:before {
  transform: translateX(26px);
}

.recent-activity {
  background-color: var(--card-bg);
  padding: 2rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background-color: var(--background-color);
  border-radius: 8px;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.activity-icon.pending {
  background-color: var(--warning-color);
}

.activity-icon.success {
  background-color: var(--success-color);
}

.activity-icon.error {
  background-color: var(--error-color);
}

.activity-content {
  flex: 1;
}

.activity-content h4 {
  color: var(--text-primary);
  margin: 0 0 0.25rem 0;
  font-size: 0.95rem;
}

.activity-content p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 0.85rem;
}

.activity-amount {
  color: var(--text-primary);
  font-weight: 600;
}
</style>
