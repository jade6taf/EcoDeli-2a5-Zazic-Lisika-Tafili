<script>
export default {
  name: 'RegisterView',
  data() {
    return {
      user: {
        nom: '',
        prenom: '',
        email: '',
        motDePasse: '',
        type: ''
      },
      typeOptions: [
        'CLIENT',
        'LIVREUR',
        'COMMERCANT',
        'PRESTATAIRE'
      ],
      typeDisplay: {
        'CLIENT': 'Client',
        'LIVREUR': 'Livreur',
        'COMMERCANT': 'Commerçant',
        'PRESTATAIRE': 'Prestataire'
      },
      error: null,
      loading: false
    }
  },
  methods: {
    async handleRegister() {
      this.loading = true;
      this.error = null;
      try {
        console.log('Données envoyées au serveur:', this.user);
        const response = await fetch('/api/auth/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.user)
        });
        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData?.message || 'Erreur lors de l\'inscription');
        }
        const data = await response.json();
        console.log('Réponse du serveur:', data);
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data.user));
        const userTypeToPath = {
          'CLIENT': '/',
          'LIVREUR': '/',
          'COMMERCANT': '/',
          'PRESTATAIRE': '/'
        };
        const redirectPath = userTypeToPath[this.user.type] || '/';
        this.$router.push(redirectPath);
      } catch (err) {
        console.error('Erreur d\'inscription:', err);
        this.error = err.message || 'Une erreur est survenue lors de l\'inscription';
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<template>
  <div class="register-container">
    <h2>Inscription</h2>
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
    <form @submit.prevent="handleRegister" class="register-form">
      <div class="form-group">
        <label for="nom">Nom</label>
        <input
          type="text"
          id="nom"
          v-model="user.nom"
          required
          placeholder="Votre nom"
        >
      </div>
      <div class="form-group">
        <label for="prenom">Prénom</label>
        <input
          type="text"
          id="prenom"
          v-model="user.prenom"
          required
          placeholder="Votre prénom"
        >
      </div>
      <div class="form-group">
        <label for="email">Email</label>
        <input
          type="email"
          id="email"
          v-model="user.email"
          required
          placeholder="Votre email"
        >
      </div>
      <div class="form-group">
        <label for="motDePasse">Mot de passe</label>
        <input
          type="password"
          id="motDePasse"
          v-model="user.motDePasse"
          required
          placeholder="Votre mot de passe"
        >
      </div>
      <div class="form-group">
        <label for="type">Type d'utilisateur</label>
        <select id="type" v-model="user.type" required>
          <option value="" disabled>Sélectionnez votre profil</option>
          <option v-for="type in typeOptions" :key="type" :value="type">
            {{ typeDisplay[type] || type }}
          </option>
        </select>
      </div>
      <button type="submit" class="submit-btn" :disabled="loading">
        {{ loading ? 'Inscription en cours...' : 'S\'inscrire' }}
      </button>
      <p class="login-link">
        Déjà un compte ?
        <router-link to="/login">Se connecter</router-link>
      </p>
    </form>
  </div>
</template>

<style scoped>
.register-container {
  max-width: 500px;
  margin: 2rem auto;
  padding: 2rem;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #2c3e50;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

label {
  font-weight: 500;
  color: #2c3e50;
}

input, select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

input:focus, select:focus {
  outline: none;
  border-color: #42b983;
  box-shadow: 0 0 0 2px rgba(66, 185, 131, 0.2);
}

.submit-btn {
  padding: 0.75rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.submit-btn:hover {
  background-color: #3aa876;
}

.submit-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 1rem;
}

.login-link a {
  color: #42b983;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}

.error-message {
  background-color: #ffebee;
  color: #c62828;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}
</style>