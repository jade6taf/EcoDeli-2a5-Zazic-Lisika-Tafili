<script>

  export default {
    name: 'Header',
  }

</script>

<template>

    <header class="header">
        <div>
            <router-link to="/">
              <img src="@/assets/logo_EcoDeli.png" alt="EcoDeli Logo" class="logo-img" />
            </router-link>
        </div>

        <div class="header-links">
            <router-link to="/services" class="header-button">Nos Services</router-link>
            <router-link to="/profiles" class="header-button">Profiles</router-link>
            <router-link to="/contact" class="header-button">Nous contacter</router-link>
            <router-link to="/about" class="header-button">A propos</router-link>
        </div>

        <div class="auth-buttons">
            <router-link to="/login" class="auth-button">Connexion</router-link>
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
    background-color: #F1F5F4;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .logo-img {
    height: 100px;
    margin-right: 2rem;
  }

  .header-links {
    display: flex;
    gap: 1rem;
    justify-content: center;
  }

  .spacer {
    flex: 1;
  }

  .header-button {
    padding: 0.5rem 1rem;
    border-radius: 20px;
    background-color: #f1f1f1;
    color: #205B01;
    text-decoration: none;
  }

  .header-button.router-link-active {
    background-color: #9cbf2a;
    color: #F1F5F4;
  }

  .header-button:hover {
    background-color: #e2f5a0;
    color: #205B01;
  }

  .auth-buttons {
    display: flex;
    gap: 1rem;
    margin-left: auto;
}

  .auth-button {
      padding: 0.5rem 1rem;
      border-radius: 20px;
      background-color: #205B01;
      color: #F1F5F4;
      transition: background-color 0.3s;
  }

  .auth-button:hover {
      background-color: #205B01;
  }

</style>
