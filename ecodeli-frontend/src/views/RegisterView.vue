<script>
import { authStore } from '@/store/auth'

export default {
  name: 'RegisterView',
  data() {
    return {
      user: {
        nom: '',
        prenom: '',
        email: '',
        motDePasse: '',
        type: '',
        nomEntreprise: '',
        siret: ''
      },
      typeOptions: [
        'CLIENT',
        'LIVREUR',
        'COMMERCANT',
        'PRESTATAIRE',
        'ADMIN'
      ],
      typeDisplay: {
        'CLIENT': 'Client',
        'LIVREUR': 'Livreur',
        'COMMERCANT': 'Commerçant',
        'PRESTATAIRE': 'Prestataire',
        'ADMIN': 'Administrateur'
      },
      error: null,
      loading: false,
      step: 1
    }
  },
  computed: {
    stepValid() {
      if (this.step === 1) {
        return !!this.user.type;
      } else if (this.step === 2) {
        return this.user.nom && this.user.prenom && this.user.email && this.user.motDePasse;
      }
      return true;
    }
  },
  methods: {
    nextStep() {
      if (this.stepValid) {
        this.step++;
      }
    },
    prevStep() {
      if (this.step > 1) {
        this.step--;
      }
    },
    async handleRegister() {
      if (!this.stepValid)
        return;

      this.loading = true;
      this.error = null;

      try {
        const userData = { ...this.user };

        const response = await fetch('/api/auth/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(userData)
        });
        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData?.message || 'Erreur lors de l\'inscription');
        }
        const data = await response.json();
        authStore.setAuth(data.token, data.user);
        const userTypeToPath = {
          'CLIENT': '/client',
          'LIVREUR': '/livreur',
          'COMMERCANT': '/commercant',
          'PRESTATAIRE': '/prestataire',
          'ADMIN': '/admin/dashboard'
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

    <!-- Étape 1: Sélection du type d'utilisateur -->
    <div v-if="step === 1" class="step-container">
      <h3>Choisissez votre profil</h3>

      <div class="profile-options">
        <div
          v-for="type in typeOptions"
          :key="type"
          class="profile-option"
          :class="{ 'selected': user.type === type }"
          @click="user.type = type"
        >
          <div class="profile-icon">
            <i :class="['fas',
              type === 'CLIENT' ? 'fa-user' :
              type === 'ADMIN' ? 'fa-user' :
              type === 'LIVREUR' ? 'fa-bicycle' :
              type === 'COMMERCANT' ? 'fa-store' : 'fa-tools'
            ]"></i>
          </div>
          <div class="profile-details">
            <h4>{{ typeDisplay[type] }}</h4>
            <p v-if="type === 'CLIENT'">Passez vos commandes et suivez vos livraisons</p>
            <p v-else-if="type === 'LIVREUR'">Effectuez des livraisons et gagnez un revenu complémentaire</p>
            <p v-else-if="type === 'COMMERCANT'">Gérez votre commerce et vos produits en ligne</p>
            <p v-else-if="type === 'PRESTATAIRE'">Proposez vos services et compétences aux particuliers</p>
            <p v-else-if="type === 'ADMIN'"> Devenez administrateur</p>
          </div>
        </div>
      </div>

      <div class="form-navigation">
        <button
          type="button"
          class="btn-primary"
          @click="nextStep"
          :disabled="!stepValid"
        >
          Continuer
        </button>
      </div>
    </div>

    <!-- Étape 2: Informations personnelles -->
    <div v-else-if="step === 2" class="step-container">
      <h3>Vos informations</h3>

      <form @submit.prevent="handleRegister" class="register-form">
        <div class="form-section">
          <h4>Informations personnelles</h4>
          <div class="form-row">
            <div class="form-group">
              <label for="nom">Nom *</label>
              <input
                type="text"
                id="nom"
                v-model="user.nom"
                required
                placeholder="Votre nom"
              >
            </div>
            <div class="form-group">
              <label for="prenom">Prénom *</label>
              <input
                type="text"
                id="prenom"
                v-model="user.prenom"
                required
                placeholder="Votre prénom"
              >
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="email">Email *</label>
              <input
                type="email"
                id="email"
                v-model="user.email"
                required
                placeholder="Votre email"
              >
            </div>
            <div class="form-group">
              <label for="motDePasse">Mot de passe *</label>
              <input
                type="password"
                id="motDePasse"
                v-model="user.motDePasse"
                required
                placeholder="8 caractères minimum"
              >
            </div>
          </div>
        </div>

        <div class="form-navigation">
          <button type="button" class="btn-secondary" @click="prevStep">
            Retour
          </button>
          <button
            type="submit"
            class="btn-primary"
          >
            {{ loading ? 'Inscription en cours...' : 'S\'inscrire' }}
          </button>
        </div>

        <p class="login-link">
          Déjà un compte ?
          <router-link to="/login">Se connecter</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  max-width: 600px;
  margin: 2rem auto;
  padding: 2rem;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(0,0,0,0.1);
}

h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #4CAF50;
}

h3 {
  margin-bottom: 1.5rem;
  font-size: 1.3rem;
  color: #555;
}

h4 {
  margin-bottom: 1rem;
  color: #666;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.5rem;
}

.error-message {
  background-color: #ffebee;
  color: #c62828;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1.5rem;
}

.step-container {
  animation: fadeIn 0.3s ease-in-out;
}

.profile-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.profile-option {
  display: flex;
  padding: 1.2rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.profile-option:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
}

.profile-option.selected {
  border-color: #4CAF50;
  background-color: #f1f8e9;
  box-shadow: 0 5px 15px rgba(76, 175, 80, 0.1);
}

.profile-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  background-color: #4CAF50;
  color: white;
  border-radius: 50%;
  margin-right: 1rem;
}

.profile-icon i {
  font-size: 1.5rem;
}

.profile-details h4 {
  margin: 0 0 0.5rem 0;
  border-bottom: none;
  padding-bottom: 0;
}

.profile-details p {
  margin: 0;
  font-size: 0.9rem;
  color: #666;
}

.form-section {
  margin-bottom: 2rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  margin-bottom: 1.2rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #4CAF50;
  outline: none;
}

.form-navigation {
  display: flex;
  justify-content: space-between;
  margin-top: 2rem;
}

.btn-primary, .btn-secondary {
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-primary {
  background-color: #4CAF50;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #43a047;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #555;
}

.btn-secondary:hover {
  background-color: #e0e0e0;
}

.btn-primary:disabled {
  background-color: #a5d6a7;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 1rem;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
