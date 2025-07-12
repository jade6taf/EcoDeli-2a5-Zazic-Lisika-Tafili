<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const form = reactive({
  prenom: '',
  nom: '',
  email: '',
  motDePasse: ''
})

const errors = ref({})
const registerError = ref('')
const registerSuccess = ref('')

const validateForm = () => {
  errors.value = {}

  if (!form.prenom.trim()) {
    errors.value.prenom = 'Prénom requis'
  }

  if (!form.nom.trim()) {
    errors.value.nom = 'Nom requis'
  }

  if (!form.email.trim()) {
    errors.value.email = 'Email requis'
  } else if (!/\S+@\S+\.\S+/.test(form.email)) {
    errors.value.email = 'Email invalide'
  }

  if (!form.motDePasse) {
    errors.value.motDePasse = 'Mot de passe requis'
  } else if (form.motDePasse.length < 6) {
    errors.value.motDePasse = 'Le mot de passe doit contenir au moins 6 caractères'
  }

  return Object.keys(errors.value).length === 0
}

const handleRegister = async () => {
  registerError.value = ''
  registerSuccess.value = ''

  if (!validateForm()) {
    return
  }

  try {
    const result = await authStore.register(form)

    if (result.success) {
      registerSuccess.value = 'Compte administrateur créé avec succès ! Redirection en cours...'

      toast.add({
        severity: 'success',
        summary: 'Inscription réussie',
        detail: `Bienvenue ${result.user.prenom} ! Votre compte administrateur a été créé.`,
        life: 4000
      })

      setTimeout(() => {
        router.push('/admin/dashboard')
      }, 2000)
    } else {
      registerError.value = result.message || 'Erreur lors de l\'inscription'
    }

  } catch (error) {
    console.error('Erreur lors de l\'inscription:', error)
    registerError.value = 'Une erreur inattendue s\'est produite'
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
            Créer un Admin EcoDeli
          </h1>
          <div class="admin-badge">Administration</div>
          <p class="text-600 mt-3">Créer un nouveau compte administrateur</p>
        </div>
      </template>

      <template #content>
        <form @submit.prevent="handleRegister" class="auth-form">

          <div class="field mb-3">
            <label for="prenom" class="field-label">Prénom *</label>
            <InputText
              id="prenom"
              v-model="form.prenom"
              placeholder="Votre prénom"
              :class="{ 'p-invalid': errors.prenom }"
              class="w-full"
            />
            <small v-if="errors.prenom" class="p-error">{{ errors.prenom }}</small>
          </div>

          <div class="field mb-3">
            <label for="nom" class="field-label">Nom *</label>
            <InputText
              id="nom"
              v-model="form.nom"
              placeholder="Votre nom"
              :class="{ 'p-invalid': errors.nom }"
              class="w-full"
            />
            <small v-if="errors.nom" class="p-error">{{ errors.nom }}</small>
          </div>

          <div class="field mb-3">
            <label for="email" class="field-label">
              <i class="pi pi-envelope mr-2"></i>
              Email administrateur *
            </label>
            <InputText
              id="email"
              v-model="form.email"
              type="email"
              placeholder="admin@ecodeli.com"
              :class="{ 'p-invalid': errors.email }"
              class="w-full"
            />
            <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
          </div>

          <div class="field mb-3">
            <label for="password" class="field-label">
              <i class="pi pi-lock mr-2"></i>
              Mot de passe *
            </label>
            <Password
              id="password"
              v-model="form.motDePasse"
              placeholder="Votre mot de passe"
              :class="{ 'p-invalid': errors.motDePasse }"
              class="w-full"
              :feedback="true"
              toggleMask
              :promptLabel="'Entrez un mot de passe'"
              :weakLabel="'Faible'"
              :mediumLabel="'Moyen'"
              :strongLabel="'Fort'"
            />
            <small v-if="errors.motDePasse" class="p-error">{{ errors.motDePasse }}</small>
          </div>

          <InlineMessage
            v-if="registerError"
            severity="error"
            class="w-full mb-3"
          >
            <i class="pi pi-exclamation-triangle mr-2"></i>
            {{ registerError }}
          </InlineMessage>

          <InlineMessage
            v-if="registerSuccess"
            severity="success"
            class="w-full mb-3"
          >
            <i class="pi pi-check mr-2"></i>
            {{ registerSuccess }}
          </InlineMessage>

          <Button
            type="submit"
            :label="authStore.isLoading ? 'Création en cours...' : 'Créer le compte admin'"
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
            Déjà un compte administrateur ?
            <router-link to="/admin/login" class="text-primary">Se connecter</router-link>
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
