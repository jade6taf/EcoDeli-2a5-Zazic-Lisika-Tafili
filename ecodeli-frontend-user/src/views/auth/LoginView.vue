<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const form = reactive({
  email: '',
  motDePasse: ''
})

const errors = ref({})
const loginError = ref('')

const validateForm = () => {
  errors.value = {}

  if (!form.email) {
    errors.value.email = 'Email requis'
  } else if (!/\S+@\S+\.\S+/.test(form.email)) {
    errors.value.email = 'Email invalide'
  }

  if (!form.motDePasse) {
    errors.value.motDePasse = 'Mot de passe requis'
  }

  return Object.keys(errors.value).length === 0
}

const handleLogin = async () => {
  loginError.value = ''

  if (!validateForm()) {
    return
  }

  try {
    const result = await authStore.login(form.email, form.motDePasse)

    if (result.success) {
      toast.add({
        severity: 'success',
        summary: 'Connexion réussie',
        detail: `Bienvenue ${result.user.prenom} !`,
        life: 3000
      })
      const role = result.user.userType.toLowerCase()
      router.push(`/${role}`)
    } else {
      loginError.value = result.message || 'Erreur de connexion'
    }
  } catch (error) {
    console.error('Erreur lors de la connexion:', error)
    loginError.value = 'Une erreur inattendue s\'est produite'
  }
}

const goToHome = () => {
  router.push('/')
}

// Déconnexion automatique si l'utilisateur était connecté
onMounted(() => {
  if (authStore.isAuthenticated) {
    authStore.logout()
  }
})
</script>

<template>
  <div class="login-container">
    <div class="back-to-home">
      <Button
        icon="pi pi-arrow-left"
        label="Retour à l'accueil"
        text
        @click="goToHome"
        class="back-button"
      />
    </div>
    <div class="login-wrapper">
      <Card class="login-card">
        <template #header>
          <div class="login-header">
            <h1 class="ecodeli-title">
              <i class="pi pi-leaf mr-2"></i>
              EcoDeli
            </h1>
            <p class="ecodeli-subtitle">Connectez-vous à votre compte</p>
          </div>
        </template>

        <template #content>
          <form @submit.prevent="handleLogin" class="login-form">
            <div class="field">
              <label for="email" class="field-label">Email</label>
              <InputText
                id="email"
                v-model="form.email"
                type="email"
                placeholder="Votre email"
                :class="{ 'p-invalid': errors.email }"
                class="w-full"
                required
              />
              <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
            </div>

            <div class="field">
              <label for="password" class="field-label">Mot de passe</label>
              <Password
                id="password"
                v-model="form.motDePasse"
                placeholder="Votre mot de passe"
                :class="{ 'p-invalid': errors.motDePasse }"
                class="w-full"
                :feedback="false"
                toggleMask
                required
              />
              <small v-if="errors.motDePasse" class="p-error">{{ errors.motDePasse }}</small>
            </div>

            <div v-if="loginError" class="error-message">
              <i class="pi pi-exclamation-triangle mr-2"></i>
              {{ loginError }}
            </div>

            <Button
              type="submit"
              label="Se connecter"
              :loading="authStore.isLoading"
              class="w-full login-button"
              size="large"
            />
          </form>
        </template>

        <template #footer>
          <div class="login-footer">
            <p class="text-sm text-center">
              Pas encore de compte ?
              <a href="#" class="text-primary">Contactez l'administrateur</a>
            </p>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.login-wrapper {
  width: 100%;
  max-width: 400px;
}

.login-card {
  border: none;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.login-header {
  text-align: center;
  padding: 2rem 2rem 1rem;
}

.login-header h1 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  color: var(--ecodeli-green-dark);
}

.login-header p {
  margin: 0;
  color: var(--surface-600);
}

.login-form {
  padding: 0 2rem 1rem;
}

.field {
  margin-bottom: 1.5rem;
}

.field-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--surface-700);
}

.login-button {
  margin-top: 1rem;
  background: var(--ecodeli-green);
  border-color: var(--ecodeli-green);
}

.login-button:hover {
  background: var(--ecodeli-green-dark);
  border-color: var(--ecodeli-green-dark);
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1rem;
  font-size: 0.875rem;
}

.login-footer {
  padding: 1rem 2rem 2rem;
  border-top: 1px solid var(--surface-200);
}

.w-full {
  width: 100%;
}

.text-primary {
  color: var(--ecodeli-green);
  text-decoration: none;
}

.text-primary:hover {
  color: var(--ecodeli-green-dark);
  text-decoration: underline;
}

.mr-2 {
  margin-right: 0.5rem;
}

.text-sm {
  font-size: 0.875rem;
}

.text-center {
  text-align: center;
}

.back-to-home {
  position: absolute;
  top: 2rem;
  left: 2rem;
  z-index: 10;
}

.back-button {
  color: var(--ecodeli-green);
  transition: all 0.3s ease;
}

.back-button:hover {
  color: var(--ecodeli-green-dark);
  background: rgba(34, 197, 94, 0.1);
}

@media (max-width: 768px) {
  .back-to-home {
    top: 1rem;
    left: 1rem;
  }
  
  .back-button .p-button-label {
    display: none;
  }
}
</style>
