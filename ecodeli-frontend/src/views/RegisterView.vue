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
        const baseValidation = this.user.nom && this.user.prenom && this.user.email && this.user.motDePasse;

        if (this.user.type === 'PRESTATAIRE') {
          return baseValidation && this.user.nomEntreprise && this.user.siret && /^[0-9]{14}$/.test(this.user.siret);
        }

        return baseValidation;
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

        <!-- Section spécifique pour les prestataires -->
        <div v-if="user.type === 'PRESTATAIRE'" class="form-section">
          <h4>Informations de l'entreprise</h4>
          <div class="form-row">
            <div class="form-group">
              <label for="nomEntreprise">Nom de l'entreprise *</label>
              <input
                type="text"
                id="nomEntreprise"
                v-model="user.nomEntreprise"
                required
                placeholder="Nom de votre entreprise"
              >
            </div>
            <div class="form-group">
              <label for="siret">Numéro SIRET *</label>
              <input
                type="text"
                id="siret"
                v-model="user.siret"
                required
                pattern="[0-9]{14}"
                placeholder="14 chiffres"
                title="Le numéro SIRET doit contenir exactement 14 chiffres"
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
  background-color: var(--bg-color);
  border-radius: 8px;
  box-shadow: var(--shadow);
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--primary-color);
}

h3 {
  margin-bottom: 1.5rem;
  font-size: 1.3rem;
  color: var(--text-secondary);
}

h4 {
  margin-bottom: 1rem;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 0.5rem;
}

.error-message {
  background-color: var(--error-bg);
  color: var(--error-color);
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
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: var(--card-bg);
}

.profile-option:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
}

.profile-option.selected {
  border-color: var(--primary-color);
  background-color: var(--primary-bg);
  box-shadow: var(--shadow-selected);
}

.profile-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  background-color: var(--primary-color);
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
  color: var(--text-primary);
}

.profile-details p {
  margin: 0;
  font-size: 0.9rem;
  color: var(--text-secondary);
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
  color: var(--text-primary);
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
  background-color: var(--input-bg);
  color: var(--text-primary);
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: var(--primary-color);
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
  background-color: var(--primary-color);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--primary-hover);
}

.btn-secondary {
  background-color: var(--button-secondary-bg);
  color: var(--text-secondary);
}

.btn-secondary:hover {
  background-color: var(--button-secondary-hover);
}

.btn-primary:disabled {
  background-color: var(--primary-light);
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 1rem;
  color: var(--text-secondary);
}

.login-link a {
  color: var(--primary-color);
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
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
