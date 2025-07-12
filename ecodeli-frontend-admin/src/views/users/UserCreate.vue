<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUsersStore } from '@/stores/users'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const usersStore = useUsersStore()
const toast = useToast()

const form = reactive({
  prenom: '',
  nom: '',
  email: '',
  userType: '',
  motDePasse: '',
  confirmPassword: ''
})

const errors = ref({})
const submitError = ref('')
const submitSuccess = ref('')

const userTypeOptions = [
  { label: 'Client', value: 'CLIENT' },
  { label: 'Livreur', value: 'LIVREUR' },
  { label: 'Prestataire', value: 'PRESTATAIRE' },
  { label: 'Commerçant', value: 'COMMERCANT' },
  { label: 'Administrateur', value: 'ADMIN' }
]

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

  if (!form.userType) {
    errors.value.userType = 'Type d\'utilisateur requis'
  }

  if (!form.motDePasse) {
    errors.value.motDePasse = 'Mot de passe requis'
  } else if (form.motDePasse.length < 6) {
    errors.value.motDePasse = 'Le mot de passe doit contenir au moins 6 caractères'
  }

  if (!form.confirmPassword) {
    errors.value.confirmPassword = 'Confirmation du mot de passe requise'
  } else if (form.motDePasse !== form.confirmPassword) {
    errors.value.confirmPassword = 'Les mots de passe ne correspondent pas'
  }

  return Object.keys(errors.value).length === 0
}

const handleSubmit = async () => {
  submitError.value = ''
  submitSuccess.value = ''

  if (!validateForm()) {
    return
  }

  try {
    const userData = {
      prenom: form.prenom,
      nom: form.nom,
      email: form.email,
      userType: form.userType,
      motDePasse: form.motDePasse
    }

    const result = await usersStore.createUser(userData)

    if (result.success) {
      submitSuccess.value = 'Utilisateur créé avec succès ! Redirection en cours...'

      toast.add({
        severity: 'success',
        summary: 'Utilisateur créé',
        detail: `${form.prenom} ${form.nom} a été créé avec succès`,
        life: 4000
      })

      setTimeout(() => {
        router.push('/admin/users')
      }, 2000)
    } else {
      submitError.value = result.message || 'Erreur lors de la création'
    }

  } catch (error) {
    console.error('Erreur lors de la création:', error)
    submitError.value = 'Une erreur inattendue s\'est produite'
  }
}

const goBack = () => {
  router.push('/admin/users')
}
</script>

<template>
  <div class="user-create">
    <div class="page-header mb-4">
      <div class="flex align-items-center">
        <Button
          icon="pi pi-arrow-left"
          class="p-button-text mr-3"
          @click="goBack"
        />
        <div>
          <h1 class="text-3xl font-bold text-800 m-0">
            <i class="pi pi-user-plus mr-3 text-green-500"></i>
            Créer un Nouvel Utilisateur
          </h1>
          <p class="text-600 mt-2">Ajouter un nouvel utilisateur à la plateforme EcoDeli</p>
        </div>
      </div>
    </div>

    <Card>
      <template #content>
        <form @submit.prevent="handleSubmit" class="user-form">
          <div class="grid">
            <div class="col-12 md:col-6">
              <div class="field">
                <label for="prenom" class="field-label">Prénom *</label>
                <InputText
                  id="prenom"
                  v-model="form.prenom"
                  placeholder="Prénom de l'utilisateur"
                  :class="{ 'p-invalid': errors.prenom }"
                  class="w-full"
                />
                <small v-if="errors.prenom" class="p-error">{{ errors.prenom }}</small>
              </div>
            </div>

            <div class="col-12 md:col-6">
              <div class="field">
                <label for="nom" class="field-label">Nom *</label>
                <InputText
                  id="nom"
                  v-model="form.nom"
                  placeholder="Nom de l'utilisateur"
                  :class="{ 'p-invalid': errors.nom }"
                  class="w-full"
                />
                <small v-if="errors.nom" class="p-error">{{ errors.nom }}</small>
              </div>
            </div>

            <div class="col-12 md:col-6">
              <div class="field">
                <label for="email" class="field-label">Email *</label>
                <InputText
                  id="email"
                  v-model="form.email"
                  type="email"
                  placeholder="email@exemple.com"
                  :class="{ 'p-invalid': errors.email }"
                  class="w-full"
                />
                <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
              </div>
            </div>

            <div class="col-12 md:col-6">
              <div class="field">
                <label for="userType" class="field-label">Type d'utilisateur *</label>
                <Dropdown
                  id="userType"
                  v-model="form.userType"
                  :options="userTypeOptions"
                  optionLabel="label"
                  optionValue="value"
                  placeholder="Sélectionner un type"
                  :class="{ 'p-invalid': errors.userType }"
                  class="w-full"
                />
                <small v-if="errors.userType" class="p-error">{{ errors.userType }}</small>
              </div>
            </div>

            <div class="col-12 md:col-6">
              <div class="field">
                <label for="motDePasse" class="field-label">Mot de passe *</label>
                <Password
                  id="motDePasse"
                  v-model="form.motDePasse"
                  placeholder="Mot de passe"
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
            </div>

            <div class="col-12 md:col-6">
              <div class="field">
                <label for="confirmPassword" class="field-label">Confirmer le mot de passe *</label>
                <Password
                  id="confirmPassword"
                  v-model="form.confirmPassword"
                  placeholder="Confirmer le mot de passe"
                  :class="{ 'p-invalid': errors.confirmPassword }"
                  class="w-full"
                  :feedback="false"
                  toggleMask
                />
                <small v-if="errors.confirmPassword" class="p-error">{{ errors.confirmPassword }}</small>
              </div>
            </div>
          </div>

          <InlineMessage
            v-if="submitError"
            severity="error"
            class="w-full mb-3"
          >
            <i class="pi pi-exclamation-triangle mr-2"></i>
            {{ submitError }}
          </InlineMessage>

          <InlineMessage
            v-if="submitSuccess"
            severity="success"
            class="w-full mb-3"
          >
            <i class="pi pi-check mr-2"></i>
            {{ submitSuccess }}
          </InlineMessage>

          <div class="form-actions">
            <Button
              label="Annuler"
              icon="pi pi-times"
              class="p-button-outlined mr-3"
              @click="goBack"
              type="button"
            />
            <Button
              type="submit"
              :label="usersStore.loading ? 'Création en cours...' : 'Créer l\'utilisateur'"
              icon="pi pi-plus"
              :loading="usersStore.loading"
              class="btn-primary"
            />
          </div>
        </form>
      </template>
    </Card>
  </div>
</template>

<style scoped>
.user-create {
  padding: 0;
}

.page-header {
  background: linear-gradient(135deg, #ecfdf5, #d1fae5);
  padding: 2rem;
  border-radius: 12px;
  margin-bottom: 2rem;
}

.user-form {
  padding: 2rem;
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

.form-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid var(--surface-200);
}

.grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1.5rem;
}

.col-12 { grid-column: span 12; }
.col-6 { grid-column: span 6; }

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
}

.flex {
  display: flex;
}

.align-items-center {
  align-items: center;
}

.w-full {
  width: 100%;
}

.mb-4 {
  margin-bottom: 1.5rem;
}

.mb-3 {
  margin-bottom: 0.75rem;
}

.mt-2 {
  margin-top: 0.5rem;
}

.mr-2 {
  margin-right: 0.5rem;
}

.mr-3 {
  margin-right: 0.75rem;
}

.m-0 {
  margin: 0;
}

.text-3xl {
  font-size: 1.875rem;
}

.text-600 {
  color: var(--surface-600);
}

.text-800 {
  color: var(--surface-800);
}

.font-bold {
  font-weight: 700;
}
</style>
