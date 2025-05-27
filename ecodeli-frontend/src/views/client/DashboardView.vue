<script>

export default {
  name: 'ClientDashboard',
  data() {
    return {
      user: null,
      isLoading: false
    }
  },
  mounted() {
    this.isLoading = true;
    const userStr = localStorage.getItem('user');
    const token = localStorage.getItem('token');

    if (userStr) {
      this.user = JSON.parse(userStr);
    } else {
      this.$router.push('/login');
    }
    this.isLoading = false;
  },
  methods: {
    navigateTo(path) {
      this.$router.push(path);
    }
  }
}
</script>

<template>
  <div class="dashboard-container">

    <h1>Dashboard Client</h1>
    <div v-if="isLoading" class="loading">
      Chargement...
    </div>

    <div v-else-if="user" class="dashboard-content">
      <div class="welcome-message">
        <h2>Bienvenue, {{ user.prenom }} {{ user.nom }}</h2>
        <p>Voici votre espace personnel pour gérer vos annonces et suivre vos activités.</p>
      </div>

      <h3 class="section-title">Annonces de colis</h3>
      <div class="dashboard-actions">
        <router-link to="/client/annonces" class="dashboard-card">
          <i class="fas fa-bullhorn"></i>
          <h3>Mes Annonces</h3>
          <p>Consultez et gérez vos annonces existantes</p>
        </router-link>
        <router-link to="/client/annonces/new" class="dashboard-card">
          <i class="fas fa-plus-circle"></i>
          <h3>Créer une annonce</h3>
          <p>Publiez une nouvelle annonce</p>
        </router-link>
      </div>

      <h3 class="section-title">Services prestataires</h3>
      <div class="dashboard-actions">
        <div @click="navigateTo('/client/services-types')" class="dashboard-card">
          <i class="fas fa-concierge-bell"></i>
          <h3>Découvrir les services</h3>
          <p>Explorez les services proposés par nos prestataires</p>
        </div>
        <div @click="navigateTo('/client/mes-services')" class="dashboard-card">
          <i class="fas fa-tasks"></i>
          <h3>Mes demandes de service</h3>
          <p>Suivez l'état de vos demandes de services</p>
        </div>
      </div>
    </div>

    <div v-else class="error-message">
      Vous devez être connecté pour accéder à cette page.
    </div>

  </div>
</template>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.dashboard-container h1 {
  color: var(--text-primary);
}

.welcome-message {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: var(--card-bg);
  border-radius: 8px;
  border-left: 4px solid var(--primary-color);
  transition: background-color 0.3s ease;
}

.welcome-message h2 {
  color: var(--text-primary);
  margin: 0 0 1rem 0;
}

.welcome-message p {
  color: var(--text-secondary);
  margin: 0;
}

.section-title {
  margin: 2rem 0 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid var(--border-color);
  color: var(--text-primary);
  font-size: 1.5rem;
}

.dashboard-actions {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.dashboard-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 2rem;
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: var(--shadow);
  transition: transform 0.3s, box-shadow 0.3s, background-color 0.3s ease;
  text-decoration: none;
  color: var(--text-primary);
  cursor: pointer;
}

.dashboard-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-hover);
}

.dashboard-card i {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: var(--primary-color);
}

.dashboard-card h3 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
}

.dashboard-card p {
  color: var(--text-secondary);
  margin: 0;
}

.loading, .error-message {
  text-align: center;
  padding: 2rem;
  color: var(--text-primary);
}

.error-message {
  color: var(--error-color);
}
</style>