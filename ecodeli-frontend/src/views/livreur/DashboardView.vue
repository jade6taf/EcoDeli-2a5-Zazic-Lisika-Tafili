<script>
export default {
  name: 'LivreurDashboard',
  data() {
    return {
      user: null,
      isLoading: false
    }
  },
  mounted() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.user = JSON.parse(userStr);
    } else {
      this.$router.push('/login');
    }
  }
}
</script>

<template>
  <div class="dashboard-container">
    <h1>Dashboard Livreur</h1>

    <div v-if="isLoading" class="loading">
      Chargement...
    </div>

    <div v-else-if="user" class="dashboard-content">
      <div class="welcome-message">
        <h2>Bienvenue, {{ user.prenom }} {{ user.nom }}</h2>
        <p>Voici votre espace personnel pour trouver des annonces et suivre vos livraisons.</p>
      </div>

      <div class="dashboard-actions">
        <router-link to="/livreur/annonces" class="dashboard-card">
          <i class="fas fa-list-alt"></i>
          <h3>Annonces disponibles</h3>
          <p>Consultez toutes les annonces en attente de livreur</p>
        </router-link>
        <router-link to="/livreur/mes-livraisons" class="dashboard-card">
          <i class="fas fa-truck"></i>
          <h3>Mes livraisons</h3>
          <p>Suivez vos livraisons en cours et passées</p>
        </router-link>
        <router-link to="/livreur/mes-segments" class="dashboard-card">
          <i class="fas fa-route"></i>
          <h3>Mes segments</h3>
          <p>Gérez vos segments de livraison partielle</p>
        </router-link>
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
  border-left: 4px solid #3F51B5;
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

.dashboard-actions {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-top: 2rem;
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
  color: #3F51B5;
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