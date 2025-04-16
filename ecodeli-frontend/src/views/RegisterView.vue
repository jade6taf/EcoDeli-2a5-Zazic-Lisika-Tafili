<script>
export default {
  data() {
    return {
      user: {
        nom: '',
        prenom: '',
        email: '',
        motDePasse: '',
        type: 'CLIENT'
      }
    }
  },
  methods: {
    async handleRegister() {
      try {
        const response = await fetch('http://localhost:8080/api/auth/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.user)
        });

        if (response.ok) {
          alert('Inscription réussie!');
          this.$router.push('/login');
        } else {
          const error = await response.text();
          alert(error);
        }
      } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur lors de l\'inscription');
      }
    }
  }
}
</script>

<template>
  <div class="register-container">
    <h2>Inscription</h2>
    <form @submit.prevent="handleRegister" class="register-form">
      <div class="form-group">
        <label for="nom">Nom</label>
        <input
          type="text"
          id="nom"
          v-model="user.nom"
          required
        >
      </div>
      <div class="form-group">
        <label for="prenom">Prénom</label>
        <input
          type="text"
          id="prenom"
          v-model="user.prenom"
          required
        >
      </div>
      <div class="form-group">
        <label for="email">Email</label>
        <input
          type="email"
          id="email"
          v-model="user.email"
          required
        >
      </div>
      <div class="form-group">
        <label for="motDePasse">Mot de passe</label>
        <input
          type="password"
          id="motDePasse"
          v-model="user.motDePasse"
          required
        >
      </div>
      <div class="form-group">
        <label for="type">Type d'utilisateur</label>
        <input
          type="type"
          id="type"
          v-model="user.type"
          required
        >
      </div>
      <button type="submit" class="submit-btn">S'inscrire</button>
      <p class="login-link">
        Déjà un compte ?
        <router-link to="/login">Se connecter</router-link>
      </p>
    </form>
  </div>
</template>

<style scoped>
.register-container {
  max-width: 400px;
  margin: 40px auto;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0,0,0,0.1);
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.submit-btn {
  padding: 10px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn:hover {
  background-color: #45a049;
}

.login-link {
  text-align: center;
  margin-top: 10px;
}
</style>