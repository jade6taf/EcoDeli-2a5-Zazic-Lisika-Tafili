<template>
  <div class="prestataire-layout">
    <nav class="prestataire-nav">
      <router-link 
        to="/prestataire" 
        class="nav-item"
        :class="{ active: $route.name === 'prestataire-dashboard' }"
      >
        <i class="fas fa-tachometer-alt"></i>
        Tableau de bord
      </router-link>
      <router-link 
        to="/prestataire/informations" 
        class="nav-item"
        :class="{ active: $route.name === 'prestataire-informations' }"
      >
        <i class="fas fa-id-card"></i>
        Informations personnelles
      </router-link>
      <router-link 
        to="/prestataire/profil" 
        class="nav-item"
        :class="{ active: $route.name === 'prestataire-profil' }"
      >
        <i class="fas fa-user-edit"></i>
        Profil public
      </router-link>
    </nav>
    <main class="prestataire-content">
      <router-view></router-view>
    </main>
  </div>
</template>

<script>
export default {
  name: 'PrestataireLayout'
}
</script>

<style scoped>
.prestataire-layout {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.prestataire-nav {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  background-color: white;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  text-decoration: none;
  color: #666;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.nav-item i {
  font-size: 1.1rem;
}

.nav-item:hover {
  background-color: #f5f5f5;
  color: #4CAF50;
}

.nav-item.active {
  background-color: #4CAF50;
  color: white;
}

.prestataire-content {
  background-color: #f8f9fa;
  min-height: calc(100vh - 200px);
}

@media (max-width: 768px) {
  .prestataire-nav {
    flex-direction: column;
    gap: 0.5rem;
  }

  .nav-item {
    padding: 0.5rem;
  }
}
</style>
