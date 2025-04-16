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
      }
    }
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

    <div class="header-links">
      <div v-for="(menuItem, key) in menuItems" :key="key" class="dropdown">
        <router-link
          :to="menuItem.hasDropdown ? '#' : menuItem.path"
          class="header-button"
          :class="{ 'with-arrow': menuItem.hasDropdown }"
        >
          {{ key.charAt(0).toUpperCase() + key.slice(1) }}
        </router-link>
        <div v-if="menuItem.hasDropdown" class="dropdown-content">
          <router-link
            v-for="item in menuItem.items"
            :key="item.path"
            :to="item.path"
            class="dropdown-item"
          >
            {{ item.name }}
          </router-link>
        </div>
      </div>
    </div>

  <div class="auth-buttons">
    <router-link to="/login" class="auth-button login">Connexion</router-link>
    <router-link to="/register" class="auth-button">Inscription</router-link>
  </div>

  </header>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: #f1f5f4;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  user-select: none;
}

.logo-img {
  height: 100px;
  margin-left: 2rem;
}

.header-links {
  display: flex;
  gap: 2rem;
  justify-content: center;
  flex: 1;
  margin-left: -200px;
}

.dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-content {
  display: none;
  position: absolute;
  background-color: #f1f5f4;
  min-width: 160px;
  box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);
  border-radius: 5px;
  z-index: 1;
  padding: 0.5rem 0;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
}

.dropdown:hover .dropdown-content {
  display: block;
}

.dropdown-item {
  color: #205B01;
  padding: 12px 16px;
  text-decoration: none;
  display: block;
  transition: color 0.3s;
}

.dropdown-item:hover {
  color: #9CBF2A;
  background-color: transparent;
}

.header-button {
  padding: 0.5rem 1rem;
  color: #205B01;
  text-decoration: none;
  transition: color 0.3s;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.header-button.with-arrow::after {
  content: '▼';
  font-size: 0.8rem;
  margin-left: 5px;
  transition: transform 0.3s;
}

.dropdown:hover .header-button.with-arrow::after {
  transform: rotate(180deg);
}

.header-button:hover {
  color: #9CBF2A;
  background-color: transparent;
}

.auth-buttons {
  display: flex;
  gap: 1rem;
  margin-right: 4rem;
  text-decoration: none !important;
}

.auth-button {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  background-color: #205B01;
  color: #f1f5f4;
  text-decoration: none;
  transition: background-color 0.3s;
}

.auth-button:hover {
  background-color: #184401;
}

.auth-button.login {
  background-color: #f1f5f4;
  color: #205B01;
  border: 1px solid #205B01;
}

.auth-button.login:hover {
  background-color: #9CBF2A;
  color: #f1f5f4;
  border-color: #f1f5f4;
}

</style>
