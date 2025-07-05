<script>
import { useI18n } from 'vue-i18n'
import { authStore } from '@/store/auth'
import PasswordInput from '@/components/PasswordInput.vue'

export default {
  name: 'RegisterView',
  components: {
    PasswordInput
  },
  setup() {
    const { t } = useI18n()
    return { t }
  },
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
      selectedProfile: null,
      error: null,
      loading: false,
      step: 1,
      passwordValidation: {
        valid: false,
        strength: 0,
        errors: [],
        suggestions: []
      },
      hcaptchaToken: null,
      hcaptchaSiteKey: import.meta.env.VITE_HCAPTCHA_SITE_KEY,
      captchaVerified: false,
      captchaError: null
    }
  },
  computed: {
    profileData() {
      return {
        'CLIENT': {
          title: this.t('registerview.profiles.client.title'),
          subtitle: this.t('registerview.profiles.client.subtitle'),
          description: this.t('registerview.profiles.client.description'),
          icon: 'fa-shipping-fast',
          highlights: [
            this.t('registerview.profiles.client.highlight-1'),
            this.t('registerview.profiles.client.highlight-2'),
            this.t('registerview.profiles.client.highlight-3')
          ],
          gradient: 'linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%)'
        },
        'LIVREUR': {
          title: this.t('registerview.profiles.livreur.title'),
          subtitle: this.t('registerview.profiles.livreur.subtitle'),
          description: this.t('registerview.profiles.livreur.description'),
          icon: 'fa-bicycle',
          highlights: [
            this.t('registerview.profiles.livreur.highlight-1'),
            this.t('registerview.profiles.livreur.highlight-2'),
            this.t('registerview.profiles.livreur.highlight-3')
          ],
          gradient: 'linear-gradient(135deg, #A5C53D 0%, #9CCC65 100%)'
        },
        'COMMERCANT': {
          title: this.t('registerview.profiles.commercant.title'),
          subtitle: this.t('registerview.profiles.commercant.subtitle'),
          description: this.t('registerview.profiles.commercant.description'),
          icon: 'fa-store',
          highlights: [
            this.t('registerview.profiles.commercant.highlight-1'),
            this.t('registerview.profiles.commercant.highlight-2'),
            this.t('registerview.profiles.commercant.highlight-3')
          ],
          gradient: 'linear-gradient(135deg, #95B728 0%, #8BC34A 100%)'
        },
        'PRESTATAIRE': {
          title: this.t('registerview.profiles.prestataire.title'),
          subtitle: this.t('registerview.profiles.prestataire.subtitle'),
          description: this.t('registerview.profiles.prestataire.description'),
          icon: 'fa-tools',
          highlights: [
            this.t('registerview.profiles.prestataire.highlight-1'),
            this.t('registerview.profiles.prestataire.highlight-2'),
            this.t('registerview.profiles.prestataire.highlight-3')
          ],
          gradient: 'linear-gradient(135deg, #2e7d32 0%, #4CAF50 100%)'
        },
        'ADMIN': {
          title: this.t('registerview.profiles.admin.title'),
          subtitle: this.t('registerview.profiles.admin.subtitle'),
          description: this.t('registerview.profiles.admin.description'),
          icon: 'fa-user-shield',
          highlights: [
            this.t('registerview.profiles.admin.highlight-1'),
            this.t('registerview.profiles.admin.highlight-2'),
            this.t('registerview.profiles.admin.highlight-3')
          ],
          gradient: 'linear-gradient(135deg, #1976d2 0%, #2196F3 100%)'
        }
      };
    },
    stepValid() {
      if (this.step === 1) {
        return !!this.selectedProfile;
      } else if (this.step === 2) {
        const baseValidation = this.user.nom && this.user.prenom && this.user.email && this.user.motDePasse && this.passwordValidation.valid && this.captchaVerified;
        if (this.user.type === 'PRESTATAIRE') {
          return baseValidation && this.user.nomEntreprise && this.user.siret && /^[0-9]{14}$/.test(this.user.siret);
        }

        return baseValidation;
      }
      return true;
    },
    visibleProfiles() {
      // Masquer ADMIN sauf conditions spéciales (pour l'instant toujours visible pour le développement)
      return this.typeOptions;
    }
  },
  methods: {
    selectProfile(type) {
      this.selectedProfile = type;
      this.user.type = type;
    },
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
      if (!this.captchaVerified) {
        this.error = 'Veuillez compléter la vérification de sécurité';
        return;
      }

      this.loading = true;
      this.error = null;

      try {
        const userData = { ...this.user };

        if (userData.type !== 'PRESTATAIRE') {
          delete userData.nomEntreprise;
        }
        if (userData.type !== 'PRESTATAIRE' && userData.type !== 'COMMERCANT') {
          delete userData.siret;
        }

        const registrationData = {
          utilisateur: userData,
          hcaptchaToken: this.hcaptchaToken
        };

        const response = await fetch('http://localhost:8080/api/auth/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(registrationData)
        });

        if (!response.ok) {
          const errorData = await response.text().catch(() => 'Erreur lors de l\'inscription');
          throw new Error(errorData || 'Erreur lors de l\'inscription');
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
        this.error = err.message || 'Erreur lors de l\'inscription';
        this.resetCaptcha();
      } finally {
        this.loading = false;
      }
    },
    handlePasswordValidation(validationResult) {
      this.passwordValidation = validationResult;
    },
    onCaptchaVerify(token) {
      this.hcaptchaToken = token;
      this.captchaVerified = true;
      this.captchaError = null;
    },
    onCaptchaExpired() {
      this.hcaptchaToken = null;
      this.captchaVerified = false;
      this.captchaError = 'Le captcha a expiré, veuillez le refaire';
    },
    onCaptchaError(error) {
      this.captchaError = 'Erreur lors de la vérification captcha';
      this.captchaVerified = false;
      this.hcaptchaToken = null;
    },
    resetCaptcha() {
      this.hcaptchaToken = null;
      this.captchaVerified = false;
      this.captchaError = null;
    }
  }
}
</script>

<template>
  <div class="register-hero">

    <!-- Étape 1: Sélection du profil avec cartes immersives -->
    <div v-if="step === 1" class="profile-selection-container">
      <div class="selection-header">
        <h1 class="main-title">{{ t('registerview.join-ecodeli') }}</h1>
        <p class="main-subtitle">{{ t('registerview.choose-profile') }}</p>
      </div>

      <div v-if="error" class="error-message">
        <i class="fas fa-exclamation-triangle"></i>
        {{ error }}
      </div>

      <div class="profile-cards-grid">
        <div
          v-for="type in visibleProfiles"
          :key="type"
          class="profile-card"
          :class="{
            'selected': selectedProfile === type,
            'admin-card': type === 'ADMIN'
          }"
          @click="selectProfile(type)"
        >
          <div class="card-gradient" :style="{ background: profileData[type].gradient }"></div>
          <div class="card-content">
            <div class="card-icon">
              <i :class="['fas', profileData[type].icon]"></i>
            </div>
            <div class="card-header">
              <h3 class="card-title">{{ profileData[type].title }}</h3>
              <p class="card-subtitle">{{ profileData[type].subtitle }}</p>
            </div>
            <p class="card-description">{{ profileData[type].description }}</p>
            <ul class="card-highlights">
              <li v-for="highlight in profileData[type].highlights" :key="highlight">
                <i class="fas fa-check"></i>
                {{ highlight }}
              </li>
            </ul>
          </div>
          <div class="selection-indicator">
            <i class="fas fa-check-circle"></i>
          </div>
        </div>
      </div>

      <div class="navigation-footer">
        <button
          type="button"
          class="continue-btn"
          :class="{ active: stepValid }"
          @click="nextStep"
          :disabled="!stepValid"
        >
          <span>{{ t('registerview.continue') }}</span>
          <i class="fas fa-arrow-right"></i>
        </button>
      </div>
    </div>

    <!-- Étape 2: Formulaire d'informations -->
    <div v-else-if="step === 2" class="form-container">
      <div class="form-header">
        <div class="selected-profile-indicator">
          <div class="profile-badge" :style="{ background: profileData[selectedProfile].gradient }">
            <i :class="['fas', profileData[selectedProfile].icon]"></i>
          </div>
          <div class="profile-info">
            <h3>{{ profileData[selectedProfile].title }}</h3>
            <p>{{ profileData[selectedProfile].subtitle }}</p>
          </div>
        </div>
        <h2>{{ t('registerview.your-information') }}</h2>
      </div>

      <div v-if="error" class="error-message">
        <i class="fas fa-exclamation-triangle"></i>
        {{ error }}
      </div>

      <form @submit.prevent="handleRegister" class="register-form">
        <div class="form-section">
          <h4>
            <i class="fas fa-user"></i>
            {{ t('registerview.personal-info') }}
          </h4>
          <div class="form-row">
            <div class="form-group">
              <label for="nom">{{ t('registerview.last-name') }}</label>
              <input
                type="text"
                id="nom"
                v-model="user.nom"
                required
                :placeholder="t('registerview.last-name-placeholder')"
              >
            </div>
            <div class="form-group">
              <label for="prenom">{{ t('registerview.first-name') }}</label>
              <input
                type="text"
                id="prenom"
                v-model="user.prenom"
                required
                :placeholder="t('registerview.first-name-placeholder')"
              >
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="email">{{ t('registerview.email') }}</label>
              <input
                type="email"
                id="email"
                v-model="user.email"
                required
                :placeholder="t('registerview.email-placeholder')"
              >
            </div>
            <div class="form-group password-field-group">
              <label for="password">{{ t('registerview.password') }}</label>
              <PasswordInput
                v-model="user.motDePasse"
                label="Mot de passe *"
                :placeholder="t('registerview.password-placeholder')"
                :required="true"
                :show-strength="true"
                :show-criteria="true"
                :show-generator="true"
                :user-info="{
                  nom: user.nom,
                  prenom: user.prenom,
                  email: user.email
                }"
                @validation-change="handlePasswordValidation"
              />
            </div>
          </div>
        </div>

        <!-- Section spécifique pour les commerçants -->
        <div v-if="user.type === 'COMMERCANT'" class="form-section">
          <h4>
            <i class="fas fa-store"></i>
            {{ t('registerview.business-info') }}
          </h4>
          <div class="form-row">
            <div class="form-group">
              <label for="nomCommerce">{{ t('registerview.business-name') }}</label>
              <input
                type="text"
                id="nomCommerce"
                v-model="user.nomCommerce"
                required
                :placeholder="t('registerview.business-name-placeholder')"
              >
            </div>
            <div class="form-group">
              <label for="siretCommercant">{{ t('registerview.siret-number') }}</label>
              <input
                type="text"
                id="siretCommercant"
                v-model="user.siret"
                pattern="[0-9]{14}"
                :placeholder="t('registerview.siret-placeholder')"
                :title="t('registerview.siret-title')"
              >
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="descriptionCommerce">{{ t('registerview.business-description') }}</label>
              <textarea
                id="descriptionCommerce"
                v-model="user.descriptionCommerce"
                rows="3"
                :placeholder="t('registerview.business-description-placeholder')"
              ></textarea>
            </div>
            <div class="form-group">
              <label for="horairesOuverture">{{ t('registerview.opening-hours') }}</label>
              <textarea
                id="horairesOuverture"
                v-model="user.horairesOuverture"
                rows="3"
                :placeholder="t('registerview.opening-hours-placeholder')"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- Section spécifique pour les prestataires -->
        <div v-if="user.type === 'PRESTATAIRE'" class="form-section">
          <h4>
            <i class="fas fa-building"></i>
            {{ t('registerview.company-info') }}
          </h4>
          <div class="form-row">
            <div class="form-group">
              <label for="nomEntreprise">{{ t('registerview.company-name') }}</label>
              <input
                type="text"
                id="nomEntreprise"
                v-model="user.nomEntreprise"
                required
                :placeholder="t('registerview.company-name-placeholder')"
              >
            </div>
            <div class="form-group">
              <label for="siret">{{ t('registerview.siret-number') }}</label>
              <input
                type="text"
                id="siret"
                v-model="user.siret"
                required
                pattern="[0-9]{14}"
                :placeholder="t('registerview.siret-placeholder')"
                :title="t('registerview.siret-title')"
              >
            </div>
          </div>
        </div>

        <div class="form-section">
          <h4>
            <i class="fas fa-shield-alt"></i>
            Vérification de sécurité
          </h4>
          <vue-hcaptcha
            :sitekey="hcaptchaSiteKey"
            @verify="onCaptchaVerify"
            @expired="onCaptchaExpired"
            @error="onCaptchaError"
            theme="light"
          />
          <div v-if="captchaError" class="captcha-error">
            <i class="fas fa-exclamation-triangle"></i>
            {{ captchaError }}
          </div>
        </div>

        <div class="form-navigation">
          <button type="button" class="btn-secondary" @click="prevStep">
            <i class="fas fa-arrow-left"></i>
            {{ t('registerview.back') }}
          </button>
          <button
            type="submit"
            class="btn-primary"
            :disabled="loading || !stepValid"
          >
            <span v-if="loading">
              <i class="fas fa-spinner fa-spin"></i>
              {{ t('registerview.registering') }}
            </span>
            <span v-else>
              <i class="fas fa-user-plus"></i>
              {{ t('registerview.register') }}
            </span>
          </button>
        </div>

        <p class="login-link">
          {{ t('registerview.already-account') }}
          <router-link to="/login">{{ t('registerview.login-link') }}</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<style scoped>
:root {
  --transition-smooth: cubic-bezier(0.175, 0.885, 0.32, 1.275);
  --card-hover-scale: 1.02;
  --grid-gap: 2rem;
}

.register-hero {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e8f5e9 100%);
  padding: 0;
}

.register-header {
  background: white;
  box-shadow: var(--shadow);
  padding: 1rem 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-link {
  display: flex;
  align-items: center;
}

.logo {
  height: 40px;
  width: auto;
}

.progress-indicator {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.progress-step {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-muted);
  transition: color 0.3s ease;
}

.progress-step.active {
  color: var(--primary-color);
  font-weight: 600;
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--border-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  transition: all 0.3s ease;
}

.progress-step.active .step-number {
  background: var(--primary-color);
}

.progress-line {
  width: 60px;
  height: 2px;
  background: var(--border-color);
  transition: background 0.3s ease;
}

.progress-line.active {
  background: var(--primary-color);
}

.profile-selection-container {
  max-width: 1500px;
  margin: 0 auto;
  padding: 4rem 2rem;
}

.selection-header {
  text-align: center;
  margin-bottom: 4rem;
}

.main-title {
  font-size: 3rem;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 1rem;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.main-subtitle {
  font-size: 1.2rem;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

.profile-cards-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1.5rem;
  margin-bottom: 4rem;
  max-width: 1400px;
  margin-left: auto;
  margin-right: auto;
}

.profile-card {
  position: relative;
  min-height: 450px;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s var(--transition-smooth);
  border: 3px solid transparent;
  box-shadow: var(--shadow);
  min-width: 280px;
}

.profile-card:hover {
  transform: translateY(-8px) scale(var(--card-hover-scale));
  box-shadow: var(--shadow-hover);
}

.profile-card.selected {
  border-color: var(--primary-color);
  transform: translateY(-8px) scale(var(--card-hover-scale));
  box-shadow: 0 8px 25px rgba(76, 175, 80, 0.3);
}

.card-gradient {
  height: 120px;
  width: 100%;
  position: relative;
}

.card-content {
  padding: 2.5rem 2rem 2rem;
  position: relative;
}

.card-icon {
  position: absolute;
  top: -60px;
  left: 2rem;
  width: 80px;
  height: 80px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow);
}

.card-icon i {
  font-size: 2rem;
  color: var(--primary-color);
}

.card-header {
  margin-top: 1.5rem;
  margin-bottom: 1.5rem;
}

.card-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 0.5rem;
}

.card-subtitle {
  font-size: 1rem;
  color: var(--primary-color);
  font-weight: 600;
  margin: 0;
}

.card-description {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.card-highlights {
  list-style: none;
  padding: 0;
  margin: 0;
}

.card-highlights li {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.card-highlights i {
  color: var(--primary-color);
  font-size: 0.8rem;
}

.selection-indicator {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 40px;
  height: 40px;
  background: var(--primary-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.3s var(--transition-smooth);
}

.profile-card.selected .selection-indicator {
  opacity: 1;
  transform: scale(1);
}

.selection-indicator i {
  color: white;
  font-size: 1.2rem;
}

.admin-card {
  border: 2px dashed var(--border-color);
  background: linear-gradient(135deg, #f8f9fa 0%, #e3f2fd 100%);
}

.navigation-footer {
  text-align: center;
}

.continue-btn {
  background: var(--border-color);
  color: var(--text-muted);
  border: none;
  padding: 1rem 3rem;
  border-radius: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: not-allowed;
  transition: all 0.3s var(--transition-smooth);
  display: inline-flex;
  align-items: center;
  gap: 1rem;
}

.continue-btn.active {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  cursor: pointer;
  transform: scale(1.05);
  box-shadow: var(--shadow-hover);
}

.continue-btn.active:hover {
  transform: scale(1.08);
}

.form-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.form-header {
  text-align: center;
  margin-bottom: 3rem;
}

.selected-profile-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.profile-badge {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-badge i {
  color: white;
  font-size: 1.5rem;
}

.profile-info h3 {
  margin: 0;
  color: var(--text-color);
  font-size: 1.2rem;
}

.profile-info p {
  margin: 0.25rem 0 0 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.error-message {
  background: linear-gradient(135deg, #ffebee 0%, #fce4ec 100%);
  color: var(--error-color);
  padding: 1rem 1.5rem;
  border-radius: 12px;
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border-left: 4px solid var(--error-color);
}

.register-form {
  background: white;
  padding: 2rem;
  border-radius: 20px;
  box-shadow: var(--shadow);
}

.form-section {
  margin-bottom: 2rem;
}

.form-section h4 {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: var(--text-color);
  font-size: 1.1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid var(--border-light);
}

.form-section h4 i {
  color: var(--primary-color);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: var(--text-color);
}

.form-group input {
  width: 100%;
  padding: 1rem;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
  color: var(--text-color);
}

.form-group input:focus,
.form-group textarea:focus {
  border-color: var(--primary-color);
  outline: none;
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

.form-group textarea {
  width: 100%;
  padding: 1rem;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
  color: var(--text-color);
  font-family: inherit;
  resize: vertical;
  min-height: 100px;
}

.form-navigation {
  display: flex;
  justify-content: space-between;
  margin-top: 2rem;
  gap: 1rem;
}

.btn-primary, .btn-secondary {
  padding: 1rem 2rem;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  flex: 1;
  justify-content: center;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.btn-primary:disabled {
  background: var(--border-color);
  color: var(--text-muted);
  cursor: not-allowed;
  transform: none;
}

.btn-secondary {
  background: white;
  color: var(--text-secondary);
  border: 2px solid var(--border-color);
}

.btn-secondary:hover {
  background: var(--bg-secondary);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.login-link {
  text-align: center;
  margin-top: 2rem;
  color: var(--text-secondary);
}

.login-link a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
}

.login-link a:hover {
  text-decoration: underline;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.profile-selection-container,
.form-container {
  animation: fadeIn 0.6s ease-out;
}

@media (max-width: 1600px) {
  .profile-cards-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 1.5rem;
  }
}

@media (max-width: 1200px) {
  .profile-cards-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem;
  }
}

@media (max-width: 1024px) {
  .profile-cards-grid {
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
    gap: 1.5rem;
  }

  .main-title {
    font-size: 2.5rem;
  }
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 1rem;
  }

  .progress-indicator .step-label {
    display: none;
  }

  .profile-selection-container,
  .form-container {
    padding: 2rem 1rem;
  }

  .profile-cards-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .main-title {
    font-size: 2rem;
  }

  .main-subtitle {
    font-size: 1rem;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .form-navigation {
    flex-direction: column;
  }

  .selected-profile-indicator {
    flex-direction: column;
    gap: 0.5rem;
  }
}

@media (max-width: 480px) {
  .profile-card {
    min-height: 350px;
  }

  .card-content {
    padding: 1.5rem;
  }

  .card-icon {
    width: 60px;
    height: 60px;
    top: -40px;
  }

  .card-icon i {
    font-size: 1.5rem;
  }
}

[data-theme="dark"] .register-hero {
  background: linear-gradient(135deg, #1e1e1e 0%, #2d3e2d 100%);
}

[data-theme="dark"] .register-header {
  background: var(--bg-secondary);
}

[data-theme="dark"] .profile-card {
  background: var(--bg-secondary);
  border-color: var(--border-color);
}

[data-theme="dark"] .card-icon {
  background: var(--bg-color);
}

[data-theme="dark"] .register-form {
  background: var(--bg-secondary);
}

[data-theme="dark"] .form-group input,
[data-theme="dark"] .form-group textarea {
  background: var(--bg-color);
  border-color: var(--border-color);
  color: var(--text-color);
}

[data-theme="dark"] .admin-card {
  background: linear-gradient(135deg, var(--bg-tertiary) 0%, #2d3e42 100%);
}

/* Styles spécifiques pour le composant PasswordInput dans le formulaire */
.password-field-group {
  margin-bottom: 0;
}

.password-field-group :deep(.password-input-container) {
  margin-bottom: 0;
}

.password-field-group :deep(.password-field) {
  margin-bottom: 0;
}

.password-field-group :deep(.password-label) {
  display: none; /* Le label est déjà affiché par le form-group */
}

.password-field-group :deep(.password-strength),
.password-field-group :deep(.password-criteria),
.password-field-group :deep(.password-errors),
.password-field-group :deep(.password-suggestions) {
  margin-top: 1rem;
}

/* Ajustements responsive pour le champ mot de passe */
@media (max-width: 768px) {
  .password-field-group :deep(.criteria-list) {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }
  
  .password-field-group :deep(.password-criteria) {
    margin-top: 0.75rem;
  }
  
  .password-field-group :deep(.criteria-title) {
    font-size: 0.85rem;
  }
}

.captcha-error {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  background: linear-gradient(135deg, #ffebee 0%, #fce4ec 100%);
  color: var(--error-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  border-left: 4px solid var(--error-color);
}

.captcha-error i {
  color: var(--error-color);
  font-size: 1rem;
}

[data-theme="dark"] .captcha-error {
  background: linear-gradient(135deg, #2d1b1b 0%, #3d1a1a 100%);
  color: #ff8a80;
  border-left-color: #ff8a80;
}

@media (max-width: 768px) {
  .captcha-error {
    font-size: 0.85rem;
    padding: 0.6rem 0.8rem;
  }
}
</style>
