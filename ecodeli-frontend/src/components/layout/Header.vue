<script>
  export default {
    name: 'Header',
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
              { name: 'Prestataire', path: '/prestataire' }
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
        isAuthenticated: false,
        user: null,
        showUserDropdown: false
      }
    },
    computed: {
      userProfilePath() {
        if (!this.user || !this.user.type)
          return '/';
        const typeToPath = {
          'CLIENT': '/client',
          'LIVREUR': '/livreur',
          'COMMERCANT': '/commerçant',
          'PRESTATAIRE': '/prestataire'
        };
        return typeToPath[this.user.type] || '/';
      }
    },
    methods: {
      logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        this.isAuthenticated = false;
        this.user = null;
        this.showUserDropdown = false;
        this.$router.push('/');
      },
      toggleUserDropdown() {
        this.showUserDropdown = !this.showUserDropdown;
      }
    },
    mounted() {
      const token = localStorage.getItem('token');
      const user = localStorage.getItem('user');

      if (token && user) {
        this.isAuthenticated = true;
        this.user = JSON.parse(user);
      }
      window.addEventListener('storage', (event) => {
        if (event.key === 'token' || event.key === 'user') {
          const token = localStorage.getItem('token');
          const user = localStorage.getItem('user');
          this.isAuthenticated = !!(token && user);
          this.user = user ? JSON.parse(user) : null;
        }
      });
    }
  }
</script>

<template>
  <header class="header">
    <div>
      <router-link to="/">
        <img src="@/assets/logo.png" alt="EcoDeli Logo" class="logo-img" />
      </router-link>
    </div>

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
          <i class="fas fa-id-card"></i> Mon profil
        </router-link>
        <a href="#" @click.prevent="logout" class="dropdown-item logout">
          <i class="fas fa-sign-out-alt"></i> Déconnexion
        </a>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
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
}

.auth-button.login {
  color: #4CAF50;
  border: 1px solid #4CAF50;
}

.auth-button:not(.login) {
  background-color: #4CAF50;
  color: white;
}

.auth-button:hover {
  opacity: 0.8;
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
}

.user-profile:hover {
  background-color: #f1f1f1;
}

.user-icon {
  font-size: 1.5rem;
  color: #4CAF50;
  margin-right: 0.5rem;
}

.user-name {
  margin-right: 0.5rem;
  font-weight: 500;
}

.dropdown-arrow {
  font-size: 0.8rem;
  color: #666;
}

.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 4px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  width: 200px;
  z-index: 100;
  margin-top: 0.5rem;
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  color: #333;
  text-decoration: none;
  transition: background-color 0.3s;
}

.dropdown-item:hover {
  background-color: #f5f5f5;
}

.dropdown-item i {
  margin-right: 0.75rem;
  width: 16px;
  color: #4CAF50;
}

.dropdown-item.logout {
  border-top: 1px solid #eee;
}

.dropdown-item.logout i {
  color: #ff6b6b;
}
</style>