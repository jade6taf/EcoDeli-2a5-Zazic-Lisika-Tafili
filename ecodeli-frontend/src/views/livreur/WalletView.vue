<script>
import WalletDashboard from '@/components/wallet/WalletDashboard.vue'

export default {
  name: 'WalletView',
  components: {
    WalletDashboard
  },
  data() {
    return {
      user: null
    }
  },
  mounted() {
    this.loadUser()
  },
  methods: {
    loadUser() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        const user = JSON.parse(userStr)
        if (user.type === 'LIVREUR') {
          this.user = user
        } else {
          console.warn('Utilisateur non autorisé pour le wallet livreur')
          this.$router.push('/login')
        }
      } else {
        console.warn('Aucun utilisateur connecté')
        this.$router.push('/login')
      }
    }
  }
}
</script>

<template>
  <div class="wallet-view">
    <!-- En-tête de la page -->
    <div class="page-header">
      <h1>💰 Mon Portefeuille</h1>
      <p>Gérez vos gains et retraits en toute simplicité</p>
    </div>

    <!-- Composant principal du wallet -->
    <div v-if="user" class="wallet-container">
      <WalletDashboard :livreur-id="user.idUtilisateur" />
    </div>

    <!-- Message d'erreur si utilisateur non connecté -->
    <div v-else class="error-container">
      <div class="error-message">
        <h2>⚠️ Accès refusé</h2>
        <p>Vous devez être connecté en tant que livreur pour accéder à cette page.</p>
        <router-link to="/login" class="btn-login">
          Se connecter
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wallet-view {
  min-height: 100vh;
  background: var(--bg-color);
  padding: 2rem 0;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
  padding: 0 2rem;
}

.page-header h1 {
  color: var(--text-primary);
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.page-header p {
  color: var(--text-secondary);
  font-size: 1.1rem;
  margin: 0;
}

.wallet-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
  padding: 2rem;
}

.error-message {
  text-align: center;
  background: var(--card-bg);
  padding: 3rem;
  border-radius: 12px;
  box-shadow: var(--shadow);
  max-width: 500px;
  width: 100%;
}

.error-message h2 {
  color: var(--error-color);
  margin-bottom: 1rem;
  font-size: 1.8rem;
}

.error-message p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

.btn-login {
  display: inline-block;
  background: #3F51B5;
  color: white;
  padding: 0.75rem 2rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: background 0.3s;
}

.btn-login:hover {
  background: #303F9F;
}

/* Responsive */
@media (max-width: 768px) {
  .wallet-view {
    padding: 1rem 0;
  }
  
  .page-header h1 {
    font-size: 2rem;
  }
  
  .page-header p {
    font-size: 1rem;
  }
  
  .wallet-container {
    padding: 0 0.5rem;
  }
  
  .error-message {
    padding: 2rem;
  }
}
</style>
