<template>
  <header class="header">
    <div>
      <router-link to="/">
        <img src="@/assets/logo.png" alt="EcoDeli Logo" class="logo-img" />
      </router-link>
    </div>

    <div class="header-actions">
      <!-- Bouton de thème -->
      <ThemeToggle />
      
      <div v-if="!isAuthenticated" class="auth-buttons">
        <router-link to="/login" class="auth-button login">Connexion</router-link>
        <router-link to="/register" class="auth-button">Inscription</router-link>
      </div>

      <div v-else class="user-menu">
        <div class="user-profile" @click="toggleUserDropdown">
          <i class="fas fa-user-circle user-icon"></i>
          <span class="user-name" v-if="user && user.nom">{{ user.nom }}</span>
          <i class="fas fa-chevron-down dropdown-arrow"></i>
        </div>

        <div v-if="showUserDropdown" class="user-dropdown">
          <router-link :to="userProfilePath" class="dropdown-item">
            <i class="fas fa-tachometer-alt"></i> Dashboard
          </router-link>

          <template v-if="user && user.type === 'CLIENT'">
            <router-link to="/client/annonces" class="dropdown-item">
              <i class="fas fa-bullhorn"></i> Mes annonces
            </router-link>
            <router-link to="/client/profile" class="dropdown-item">
              <i class="fas fa-user"></i> Profil
            </router-link>
          </template>

          <template v-if="user && user.type === 'PRESTATAIRE'">
            <router-link to="/prestataire/informations" class="dropdown-item">
              <i class="fas fa-id-card"></i> Informations personnelles
            </router-link>
            <router-link to="/prestataire/profil" class="dropdown-item">
              <i class="fas fa-user-edit"></i> Profil public
            </router-link>
          </template>

          <template v-if="user && user.type === 'LIVREUR'">
            <router-link to="/livreur/mes-livraisons" class="dropdown-item">
              <i class="fas fa-truck"></i> Mes livraisons
            </router-link>
            <router-link to="/livreur/profile" class="dropdown-item">
              <i class="fas fa-user"></i> Profil
            </router-link>
          </template>

          <a href="#" @click.prevent="logout" class="dropdown-item logout">
            <i class="fas fa-sign-out-alt"></i> Déconnexion
          </a>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
import { authStore } from '@/store/auth'
import ThemeToggle from '@/components/ThemeToggle.vue'

export default {
  name: 'Header',
  components: {
    ThemeToggle
  },
  data() {
    return {
      menuItems: {
        'Nos services': {
          hasDropdown: false,
          path: '/services'
        },
        'Profiles': {
          hasDropdown: true,
          items: [
            { name: 'Client', path: '/client' },
            { name: 'Livreur', path: '/livreur' },
            { name: 'Commerçant', path: '/commerçant' },
            { name: 'Prestataire', path: '/prestataire' },
            { name: 'Admin', path: '/admin' }
          ]
        },
        'À propos': {
          hasDropdown: false,
          path: '/contact'
        },
        'Nous contacter': {
          hasDropdown: false,
          path: '/about'
        }
      },
      showUserDropdown: false
    }
  },
  computed: {
    isAuthenticated() {
      return authStore.isAuthenticated
    },
    user() {
      return authStore.user
    },
    userProfilePath() {
      if (!this.user || !this.user.type)
        return '/';
      const typeToPath = {
        'CLIENT': '/client',
        'LIVREUR': '/livreur',
        'COMMERCANT': '/commerçant',
        'PRESTATAIRE': '/prestataire',
        'ADMIN': '/admin/dashboard'
      };
      return typeToPath[this.user.type] || '/';
    }
  },
  methods: {
    logout() {
      authStore.clearAuth();
      this.showUserDropdown = false;
      this.$router.push('/');
    },
    toggleUserDropdown() {
      this.showUserDropdown = !this.showUserDropdown;
    }
  },
  mounted() {
    authStore.initialize();
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: var(--bg-color);
  box-shadow: var(--shadow);
  border-bottom: 1px solid var(--border-color);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.logo-img {
  height: 40px;
}

.auth-buttons {
  display: flex;
  gap: 1rem;
}

.auth-button {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  color: var(--text-color);
}

.auth-button.login {
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
}

.auth-button:not(.login) {
  background-color: var(--primary-color);
  color: white;
}

.auth-button:hover {
  background-color: var(--primary-hover);
}

.user-menu {
  position: relative;
}

.user-profile {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 4px;
  transition: background-color 0.3s;
  color: var(--text-color);
}

.user-profile:hover {
  background-color: var(--hover-bg);
}

.user-icon {
  font-size: 1.5rem;
  color: var(--primary-color);
  margin-right: 0.5rem;
}

.user-name {
  margin-right: 0.5rem;
  font-weight: 500;
}

.dropdown-arrow {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background: var(--bg-color);
  border-radius: 4px;
  box-shadow: var(--shadow-hover);
  width: 200px;
  z-index: 100;
  margin-top: 0.5rem;
  border: 1px solid var(--border-color);
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  color: var(--text-color);
  text-decoration: none;
  transition: background-color 0.3s;
}

.dropdown-item:hover {
  background-color: var(--hover-bg);
}

.dropdown-item i {
  margin-right: 0.75rem;
  width: 16px;
  color: var(--primary-color);
}

.dropdown-item.logout {
  border-top: 1px solid var(--border-color);
  color: var(--error-color);
}

.dropdown-item.logout:hover {
  background-color: var(--hover-bg);
}
</style>
