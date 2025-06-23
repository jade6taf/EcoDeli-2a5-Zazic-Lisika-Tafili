<script>
import { useI18n } from 'vue-i18n'

export default {
  name: 'LoginView',
  setup() {
    const { t } = useI18n()
    return { t }
  },
  data() {
    return {
      email: '',
      password: '',
      emailError: '',
      passwordError: '',
      errorMessage: '',
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      this.emailError = '';
      this.passwordError = '';
      this.errorMessage = '';

      if (!this.email) {
        this.emailError = this.t('loginview.email-required');
        return;
      }
      if (!this.password) {
        this.passwordError = this.t('loginview.password-required');
        return;
      }
      this.loading = true;

      try {
        const response = await fetch('/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            email: this.email,
            password: this.password
          })
        });

        if (response.ok) {
          const data = await response.json();
          localStorage.setItem('token', data.token);
          localStorage.setItem('user', JSON.stringify(data.user));
          window.location.href = '/';
        } else {
          this.errorMessage = this.t('loginview.invalid-credentials');
        }
      } catch (error) {
        this.errorMessage = this.t('loginview.connection-error');
      } finally {
        this.loading = false;
      }
    },
    handleForgotPassword() {
      alert(this.t('loginview.forgot-password-coming-soon'));
    }
  }
}
</script>

<template>
  <div class="login-hero">
    <div class="login-container">
      <div class="login-card">

        <div class="login-header-content">
          <h1 class="login-title">{{ t('loginview.welcome-to-ecodeli') }}</h1>
          <p class="login-subtitle">{{ t('loginview.login-to-access') }}</p>
        </div>
        <div v-if="errorMessage" class="error-message">
          <i class="fas fa-exclamation-triangle"></i>
          {{ errorMessage }}
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="email">{{ t('loginview.email-address') }}</label>
            <input
              type="email"
              id="email"
              v-model="email"
              required
              :placeholder="t('loginview.email-placeholder')"
              class="form-input"
              :class="{ 'error': emailError }"
            >
            <div v-if="emailError" class="field-error">{{ emailError }}</div>
          </div>

          <div class="form-group">
            <label for="password">{{ t('loginview.password') }}</label>
            <input
              type="password"
              id="password"
              v-model="password"
              required
              :placeholder="t('loginview.password-placeholder')"
              class="form-input"
              :class="{ 'error': passwordError }"
            >
            <div v-if="passwordError" class="field-error">{{ passwordError }}</div>
          </div>

          <button type="submit" class="login-btn" :disabled="loading">
            <span v-if="loading">
              <i class="fas fa-spinner fa-spin"></i>
              {{ t('loginview.logging-in') }}
            </span>
            <span v-else>
              <i class="fas fa-sign-in-alt"></i>
              {{ t('loginview.login') }}
            </span>
          </button>
        </form>

        <div class="login-footer">
          <a href="#" class="forgot-password" @click.prevent="handleForgotPassword">
            <i class="fas fa-key"></i>
            {{ t('loginview.forgot-password') }}
          </a>
          <p class="register-link">
            {{ t('loginview.no-account') }}
            <router-link to="/register">{{ t('loginview.create-account') }}</router-link>
          </p>
        </div>
      </div>

      <div class="eco-badge">
        <i class="fas fa-leaf"></i>
        <span>{{ t('loginview.eco-platform') }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-hero {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--bg-tertiary) 0%, var(--bg-secondary) 100%);
  display: flex;
  flex-direction: column;
}

.login-header {
  padding: 1.5rem 2rem;
  position: relative;
  z-index: 10;
}

.header-content {
  max-width: 1500px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-link {
  display: block;
  transition: transform 0.3s ease;
}

.logo-link:hover {
  transform: scale(1.05);
}

.logo {
  height: 60px;
  width: auto;
}

.login-container {
  flex: 0.5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  position: relative;
}

.login-card {
  background: var(--bg-color);
  border-radius: 20px;
  box-shadow: var(--shadow);
  padding: 3rem;
  width: 100%;
  max-width: 500px;
  border: 1px solid var(--border-color);
  position: relative;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header-content {
  text-align: center;
  margin-bottom: 2.5rem;
}

.login-title {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  line-height: 1.2;
}

.login-subtitle {
  color: var(--text-secondary);
  font-size: 1.1rem;
  margin: 0;
  line-height: 1.5;
}

.error-message {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
  color: white;
  padding: 1rem;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 500;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.login-form {
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: var(--text-color);
  font-size: 0.95rem;
}

.form-input {
  width: 100%;
  padding: 1rem;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  background: var(--bg-color);
  color: var(--text-color);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
  transform: translateY(-2px);
}

.form-input::placeholder {
  color: var(--text-muted);
}

.form-input.error {
  border-color: #ff6b6b;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.1);
}

.field-error {
  color: #ff6b6b;
  font-size: 0.85rem;
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.field-error:before {
  content: '⚠';
}

.login-btn {
  width: 100%;
  padding: 1rem 2rem;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  margin-top: 1rem;
  position: relative;
  overflow: hidden;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(76, 175, 80, 0.2);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.8;
  cursor: not-allowed;
  transform: none;
}

.login-footer {
  text-align: center;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-light);
}

.forgot-password {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.forgot-password:hover {
  color: var(--secondary-color);
  transform: translateY(-1px);
}

.register-link {
  margin: 1rem 0 0 0;
  color: var(--text-secondary);
}

.register-link a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.register-link a:hover {
  color: var(--secondary-color);
}

.eco-badge {
  position: absolute;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-muted);
  font-size: 0.9rem;
  background: rgba(255, 255, 255, 0.8);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

.eco-badge i {
  color: var(--primary-color);
}

@media (max-width: 768px) {
  .login-card {
    padding: 2rem;
    margin: 1rem;
    border-radius: 16px;
  }

  .login-title {
    font-size: 2rem;
  }

  .login-subtitle {
    font-size: 1rem;
  }

  .header-content {
    padding: 0 1rem;
  }

  .logo {
    height: 50px;
  }

  .eco-badge {
    position: relative;
    bottom: auto;
    left: auto;
    transform: none;
    margin-top: 2rem;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 1rem;
  }

  .login-card {
    padding: 1.5rem;
  }
}

[data-theme="dark"] .login-hero {
  background: linear-gradient(135deg, #1e1e1e 0%, #2d3e2d 100%);
}

[data-theme="dark"] .eco-badge {
  background: rgba(30, 30, 30, 0.8);
}

[data-theme="dark"] .form-input {
  background: var(--bg-color);
  color: var(--text-color);
}
</style>
