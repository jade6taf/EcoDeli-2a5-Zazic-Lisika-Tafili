<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUsersStore } from '@/stores/users'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const route = useRoute()
const usersStore = useUsersStore()
const toast = useToast()

const userId = route.params.id

const currentUser = ref(null)
const loading = ref(true)
const originalData = ref(null)

const form = reactive({
  prenom: '',
  nom: '',
  email: ''
})

const errors = ref({})
const submitError = ref('')
const submitSuccess = ref('')

const hasChanges = computed(() => {
  if (!originalData.value)
    return false

  return (
    form.prenom !== originalData.value.prenom ||
    form.nom !== originalData.value.nom ||
    form.email !== originalData.value.email
  )
})

const getUserType = (user) => {
  if (user.userType) return user.userType
  return 'UNKNOWN'
}

const getTypeTagSeverity = (userType) => {
  const severities = {
    'CLIENT': 'info',
    'LIVREUR': 'success',
    'PRESTATAIRE': 'warning',
    'COMMERCANT': 'secondary',
    'ADMIN': 'danger'
  }
  return severities[userType] || 'secondary'
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('fr-FR')
}

const loadUser = async () => {
  loading.value = true
  try {
    const result = await usersStore.fetchUserById(userId)
    if (result.success) {
      currentUser.value = result.data

      form.prenom = result.data.prenom || ''
      form.nom = result.data.nom || ''
      form.email = result.data.email || ''

      originalData.value = {
        prenom: result.data.prenom || '',
        nom: result.data.nom || '',
        email: result.data.email || ''
      }
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.message,
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur lors du chargement:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger les données utilisateur',
      life: 3000
    })
  } finally {
    loading.value = false
  }
}

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

  return Object.keys(errors.value).length === 0
}

const handleSubmit = async () => {
  submitError.value = ''
  submitSuccess.value = ''

  if (!validateForm()) {
    return
  }

  if (!hasChanges.value) {
    toast.add({
      severity: 'info',
      summary: 'Aucune modification',
      detail: 'Aucune modification détectée',
      life: 3000
    })
    return
  }

  try {
    const updateData = {
      prenom: form.prenom,
      nom: form.nom,
      email: form.email
    }

    const result = await usersStore.updateUser(userId, updateData)
    if (result.success) {
      submitSuccess.value = 'Utilisateur modifié avec succès !'
      toast.add({
        severity: 'success',
        summary: 'Modification réussie',
        detail: `${form.prenom} ${form.nom} a été modifié avec succès`,
        life: 4000
      })
      originalData.value = {
        prenom: form.prenom,
        nom: form.nom,
        email: form.email
      }
      await loadUser()
    } else {
      submitError.value = result.message || 'Erreur lors de la modification'
    }
  } catch (error) {
    console.error('Erreur lors de la modification:', error)
    submitError.value = 'Une erreur inattendue s\'est produite'
  }
}

const goBack = () => {
  router.push('/admin/users')
}

onMounted(() => {
  loadUser()
})
</script>

<template>
  <div class="user-edit">
    <div class="page-header mb-4">
      <div class="flex align-items-center">
        <Button
          icon="pi pi-arrow-left"
          class="p-button-text mr-3"
          @click="goBack"
        />
        <div>
          <h1 class="text-3xl font-bold text-800 m-0">
            <i class="pi pi-pencil mr-3 text-orange-500"></i>
            Modifier l'Utilisateur
          </h1>
          <p class="text-600 mt-2" v-if="currentUser">
            Modification de {{ currentUser.prenom }} {{ currentUser.nom }}
          </p>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center p-4">
      <i class="pi pi-spin pi-spinner text-4xl text-600 mb-3"></i>
      <p>Chargement des données utilisateur...</p>
    </div>

    <!-- Formulaire -->
    <Card v-else-if="currentUser">
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
                <label class="field-label">Type d'utilisateur</label>
                <div class="user-type-display">
                  <Tag
                    :value="getUserType(currentUser)"
                    :severity="getTypeTagSeverity(getUserType(currentUser))"
                  />
                  <small class="ml-2 text-500">Le type d'utilisateur ne peut pas être modifié</small>
                </div>
              </div>
            </div>

            <div class="col-12 md:col-6">
              <div class="field">
                <label class="field-label">ID Utilisateur</label>
                <InputText
                  :value="currentUser.idUtilisateur"
                  readonly
                  class="w-full"
                  disabled
                />
              </div>
            </div>

            <div class="col-12 md:col-6">
              <div class="field">
                <label class="field-label">Date de création</label>
                <InputText
                  :value="formatDate(currentUser.dateCreation)"
                  readonly
                  class="w-full"
                  disabled
                />
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
              :label="usersStore.loading ? 'Modification en cours...' : 'Sauvegarder'"
              icon="pi pi-save"
              :loading="usersStore.loading"
              class="btn-primary"
              :disabled="!hasChanges"
            />
          </div>
        </form>
      </template>
    </Card>

    <Card v-else>
      <template #content>
        <div class="text-center p-4">
          <i class="pi pi-exclamation-triangle text-4xl text-red-500 mb-3"></i>
          <h3>Erreur de chargement</h3>
          <p class="text-600">Impossible de charger les données de l'utilisateur.</p>
          <Button
            label="Retour à la liste"
            icon="pi pi-arrow-left"
            class="mt-3"
            @click="goBack"
          />
        </div>
      </template>
    </Card>
  </div>
</template>

<style scoped>
.user-edit {
  padding: 0;
}

.page-header {
  background: linear-gradient(135deg, #fff7ed, #fed7aa);
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

.user-type-display {
  display: flex;
  align-items: center;
  padding: 0.75rem 0;
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

.mt-3 {
  margin-top: 0.75rem;
}

.mr-2 {
  margin-right: 0.5rem;
}

.mr-3 {
  margin-right: 0.75rem;
}

.ml-2 {
  margin-left: 0.5rem;
}

.m-0 {
  margin: 0;
}

.p-4 {
  padding: 1rem;
}

.text-3xl {
  font-size: 1.875rem;
}

.text-4xl {
  font-size: 2.25rem;
}

.text-center {
  text-align: center;
}

.text-500 {
  color: var(--surface-500);
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
