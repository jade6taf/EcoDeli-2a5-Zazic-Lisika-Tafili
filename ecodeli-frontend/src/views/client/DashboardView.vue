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

    <h1>Dashboard Client</h1>
    <div v-if="isLoading" class="loading">
      Chargement...
    </div>

    <div v-else-if="user" class="dashboard-content">
      <div class="welcome-message">
        <h2>Bienvenue, {{ user.prenom }} {{ user.nom }}</h2>
        <p>Voici votre espace personnel pour gérer vos annonces et suivre vos activités.</p>
      </div>
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

.welcome-message {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: #f5f5f5;
  border-radius: 8px;
  border-left: 4px solid #4CAF50;
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
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  text-decoration: none;
  color: #333;
}

.dashboard-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.15);
}

.dashboard-card i {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: #4CAF50;
}

.loading, .error-message {
  text-align: center;
  padding: 2rem;
}

.error-message {
  color: #e53935;
}
</style>