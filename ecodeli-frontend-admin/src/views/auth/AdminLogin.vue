<script setup>
import { reactive, ref } from 'vue'
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

  if (!form.email.trim()) {
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

      router.push('/admin/dashboard')
    } else {
      loginError.value = result.message || 'Erreur de connexion'
    }

  } catch (error) {
    console.error('Erreur lors de la connexion:', error)
    loginError.value = 'Une erreur inattendue s\'est produite'
  }
}
</script>

<template>
  <div class="auth-container">
    <Card class="auth-card">
      <template #header>
        <div class="auth-header">
          <h1>
            <i class="pi pi-leaf mr-2"></i>
            EcoDeli Admin
          </h1>
          <div class="admin-badge">Administration</div>
          <p class="text-600 mt-3">Connectez-vous à l'interface d'administration</p>
        </div>
      </template>

      <template #content>
        <form @submit.prevent="handleLogin" class="auth-form">

          <div class="field mb-3">
            <label for="email" class="field-label">
              <i class="pi pi-envelope mr-2"></i>
              Email administrateur
            </label>
            <InputText
              id="email"
              v-model="form.email"
              type="email"
              placeholder="admin@ecodeli.com"
              :class="{ 'p-invalid': errors.email }"
              class="w-full"
              autocomplete="email"
            />
            <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
          </div>

          <div class="field mb-3">
            <label for="password" class="field-label">
              <i class="pi pi-lock mr-2"></i>
              Mot de passe
            </label>
            <Password
              id="password"
              v-model="form.motDePasse"
              placeholder="Votre mot de passe"
              :class="{ 'p-invalid': errors.motDePasse }"
              class="w-full"
              :feedback="false"
              toggleMask
            />
            <small v-if="errors.motDePasse" class="p-error">{{ errors.motDePasse }}</small>
          </div>

          <InlineMessage
            v-if="loginError"
            severity="error"
            class="w-full mb-3"
          >
            <i class="pi pi-exclamation-triangle mr-2"></i>
            {{ loginError }}
          </InlineMessage>

          <Button
            type="submit"
            :label="authStore.isLoading ? 'Connexion...' : 'Se connecter'"
            :loading="authStore.isLoading"
            class="w-full btn-primary"
            size="large"
          />
        </form>
      </template>

      <template #footer>
        <Divider />
        <div class="auth-footer text-center">
          <p class="text-sm">
            Pas encore de compte administrateur ?
            <router-link to="/admin/register" class="text-primary">Créer un compte</router-link>
          </p>
        </div>
      </template>
    </Card>
  </div>
</template>

<style scoped>
.auth-form {
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

.auth-footer {
  padding: 1rem 2rem 2rem;
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

.mb-3 {
  margin-bottom: 0.75rem;
}

.mt-3 {
  margin-top: 0.75rem;
}

.text-sm {
  font-size: 0.875rem;
}

.text-600 {
  color: var(--surface-600);
}
</style>
