<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUsersStore } from '@/stores/users'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const authStore = useAuthStore()
const usersStore = useUsersStore()
const toast = useToast()

const form = reactive({
  prenom: '',
  nom: '',
  telephone: '',
  adresse: '',
  ville: '',
  codePostal: '',
  pays: 'France',
  dateNaissance: null,
  genre: ''
})

const loading = ref(false)
const errors = ref({})

const genreOptions = [
  { label: 'Homme', value: 'HOMME' },
  { label: 'Femme', value: 'FEMME' },
  { label: 'Autre', value: 'AUTRE' },
  { label: 'Non spécifié', value: 'NON_SPECIFIE' }
]

const userInitials = computed(() => {
  if (!authStore.user) return '?'
  const prenom = authStore.user.prenom || ''
  const nom = authStore.user.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
})

const formModified = computed(() => {
  if (!authStore.user)
    return false

  let dateChanged = false
  if (form.dateNaissance && authStore.user.dateDeNaissance) {
    const formDate = form.dateNaissance.toISOString().split('T')[0]
    const userDate = authStore.user.dateDeNaissance
    dateChanged = formDate !== userDate
  } else if (form.dateNaissance || authStore.user.dateDeNaissance) {
    dateChanged = true
  }

  return form.prenom !== (authStore.user.prenom || '') ||
         form.nom !== (authStore.user.nom || '') ||
         form.telephone !== (authStore.user.telephone || '') ||
         form.adresse !== (authStore.user.adresse || '') ||
         form.ville !== (authStore.user.ville || '') ||
         form.codePostal !== (authStore.user.codePostal || '') ||
         form.pays !== (authStore.user.pays || 'France') ||
         form.genre !== (authStore.user.genre || '') ||
         dateChanged
})

const validateForm = () => {
  errors.value = {}

  if (!form.prenom.trim()) {
    errors.value.prenom = 'Le prénom est requis'
  }

  if (!form.nom.trim()) {
    errors.value.nom = 'Le nom est requis'
  }

  if (form.telephone && !/^[0-9+\-\s()]+$/.test(form.telephone)) {
    errors.value.telephone = 'Format de téléphone invalide'
  }

  if (form.codePostal && !/^[0-9]{5}$/.test(form.codePostal)) {
    errors.value.codePostal = 'Le code postal doit contenir 5 chiffres'
  }

  return Object.keys(errors.value).length === 0
}

const loadUserData = () => {
  if (!authStore.user)
    return

  form.prenom = authStore.user.prenom || ''
  form.nom = authStore.user.nom || ''
  form.telephone = authStore.user.telephone || ''
  form.adresse = authStore.user.adresse || ''
  form.ville = authStore.user.ville || ''
  form.codePostal = authStore.user.codePostal || ''
  form.pays = authStore.user.pays || 'France'
  form.genre = authStore.user.genre || ''

  if (authStore.user.dateDeNaissance) {
    form.dateNaissance = new Date(authStore.user.dateDeNaissance)
  }
}

const saveProfile = async () => {
  if (!validateForm()) {
    toast.add({
      severity: 'error',
      summary: 'Erreur de validation',
      detail: 'Veuillez corriger les erreurs dans le formulaire',
      life: 3000
    })
    return
  }

  loading.value = true
  try {
    const userId = authStore.user?.idUtilisateur || authStore.user?.id
    if (!userId) {
      throw new Error('ID utilisateur non disponible')
    }

    const profileData = {
      prenom: form.prenom,
      nom: form.nom,
      telephone: form.telephone,
      adresse: form.adresse,
      ville: form.ville,
      codePostal: form.codePostal,
      pays: form.pays,
      genre: form.genre
    }

    if (form.dateNaissance) {
      profileData.dateNaissance = form.dateNaissance.toISOString().split('T')[0]
    }

    const result = await usersStore.updateUserProfile(userId, profileData)

    if (result.success) {
      if (result.data) {
        authStore.updateUserData(result.data)
      }

      toast.add({
        severity: 'success',
        summary: 'Profil mis à jour',
        detail: 'Vos informations ont été sauvegardées avec succès',
        life: 3000
      })
    } else {
      throw new Error(result.message)
    }

  } catch (error) {
    console.error('Erreur saveProfile:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: error.message || 'Erreur lors de la sauvegarde du profil',
      life: 3000
    })
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  const userType = authStore.userRole?.toLowerCase()
  router.push(`/${userType}`)
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(async () => {
  const userId = authStore.user?.idUtilisateur || authStore.user?.id
  if (userId) {
    const result = await usersStore.getUserProfile(userId)
    if (result.success) {
      authStore.updateUserData(result.data)
    }
  }

  loadUserData()
})
</script>

<template>
  <div class="ecodeli-layout">
    <!-- Header -->
    <header class="ecodeli-header">
      <div class="flex align-items-center justify-content-between p-4">
        <div class="flex align-items-center">
          <Button
            icon="pi pi-arrow-left"
            text
            rounded
            @click="goBack"
            class="mr-3"
          />
          <i class="pi pi-user text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
          <span class="text-xl font-semibold ecodeli-title">Mon Profil</span>
        </div>

        <div class="flex align-items-center gap-2">
          <span class="text-sm text-600">{{ authStore.userName }}</span>
          <Avatar
            :label="userInitials"
            class="bg-primary text-white"
            shape="circle"
          />
          <Button
            icon="pi pi-sign-out"
            text
            rounded
            @click="handleLogout"
          />
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="ecodeli-content">
      <div class="max-w-4xl mx-auto">
        <!-- En-tête -->
        <Card class="mb-4">
          <template #content>
            <div class="text-center">
              <h1 class="text-3xl font-bold ecodeli-title m-0">
                <i class="pi pi-user mr-3" style="color: var(--ecodeli-green)"></i>
                Mon Profil
              </h1>
              <p class="text-600 mt-2">Gérez vos informations personnelles</p>
            </div>
          </template>
        </Card>

        <!-- Informations personnelles -->
        <Card>
          <template #header>
            <div class="px-4 pt-4">
              <h3 class="text-xl font-semibold ecodeli-title m-0">Informations personnelles</h3>
            </div>
          </template>
          <template #content>
            <form @submit.prevent="saveProfile" class="p-4">
              <div class="grid gap-4">

                <!-- Email (lecture seule) -->
                <div class="col-12">
                  <label for="email" class="font-medium text-900 mb-2 block">Email</label>
                  <InputText
                    id="email"
                    :value="authStore.user?.email"
                    disabled
                    class="w-full"
                  />
                  <small class="text-600">L'email ne peut pas être modifié</small>
                </div>

                <!-- Prénom et Nom -->
                <div class="col-12 md:col-6">
                  <label for="prenom" class="font-medium text-900 mb-2 block">Prénom *</label>
                  <InputText
                    id="prenom"
                    v-model="form.prenom"
                    placeholder="Votre prénom"
                    class="w-full"
                    :class="{ 'p-invalid': errors.prenom }"
                  />
                  <small v-if="errors.prenom" class="p-error">{{ errors.prenom }}</small>
                </div>

                <div class="col-12 md:col-6">
                  <label for="nom" class="font-medium text-900 mb-2 block">Nom *</label>
                  <InputText
                    id="nom"
                    v-model="form.nom"
                    placeholder="Votre nom"
                    class="w-full"
                    :class="{ 'p-invalid': errors.nom }"
                  />
                  <small v-if="errors.nom" class="p-error">{{ errors.nom }}</small>
                </div>

                <!-- Téléphone -->
                <div class="col-12 md:col-6">
                  <label for="telephone" class="font-medium text-900 mb-2 block">Téléphone</label>
                  <InputText
                    id="telephone"
                    v-model="form.telephone"
                    placeholder="06 12 34 56 78"
                    class="w-full"
                    :class="{ 'p-invalid': errors.telephone }"
                  />
                  <small v-if="errors.telephone" class="p-error">{{ errors.telephone }}</small>
                </div>

                <!-- Genre -->
                <div class="col-12 md:col-6">
                  <label for="genre" class="font-medium text-900 mb-2 block">Genre</label>
                  <Dropdown
                    id="genre"
                    v-model="form.genre"
                    :options="genreOptions"
                    optionLabel="label"
                    optionValue="value"
                    placeholder="Sélectionnez votre genre"
                    class="w-full"
                  />
                </div>

                <!-- Date de naissance -->
                <div class="col-12 md:col-6">
                  <label for="dateNaissance" class="font-medium text-900 mb-2 block">Date de naissance</label>
                  <Calendar
                    id="dateNaissance"
                    v-model="form.dateNaissance"
                    placeholder="jj/mm/aaaa"
                    dateFormat="dd/mm/yy"
                    :maxDate="new Date()"
                    class="w-full"
                    :showIcon="true"
                  />
                </div>

                <!-- Adresse -->
                <div class="col-12">
                  <label for="adresse" class="font-medium text-900 mb-2 block">Adresse</label>
                  <Textarea
                    id="adresse"
                    v-model="form.adresse"
                    placeholder="Votre adresse complète"
                    rows="2"
                    class="w-full"
                  />
                </div>

                <!-- Ville et Code postal -->
                <div class="col-12 md:col-6">
                  <label for="ville" class="font-medium text-900 mb-2 block">Ville</label>
                  <InputText
                    id="ville"
                    v-model="form.ville"
                    placeholder="Votre ville"
                    class="w-full"
                  />
                </div>

                <div class="col-12 md:col-3">
                  <label for="codePostal" class="font-medium text-900 mb-2 block">Code postal</label>
                  <InputText
                    id="codePostal"
                    v-model="form.codePostal"
                    placeholder="75001"
                    class="w-full"
                    :class="{ 'p-invalid': errors.codePostal }"
                  />
                  <small v-if="errors.codePostal" class="p-error">{{ errors.codePostal }}</small>
                </div>

                <div class="col-12 md:col-3">
                  <label for="pays" class="font-medium text-900 mb-2 block">Pays</label>
                  <InputText
                    id="pays"
                    v-model="form.pays"
                    placeholder="France"
                    class="w-full"
                  />
                </div>

              </div>

              <!-- Boutons -->
              <div class="flex justify-content-end gap-3 mt-6">
                <Button
                  label="Annuler"
                  icon="pi pi-times"
                  severity="secondary"
                  outlined
                  @click="loadUserData"
                  type="button"
                  :disabled="!formModified"
                />
                <Button
                  type="submit"
                  :label="loading ? 'Sauvegarde...' : 'Sauvegarder'"
                  icon="pi pi-check"
                  :loading="loading"
                  :disabled="!formModified"
                />
              </div>
            </form>
          </template>
        </Card>
      </div>
    </main>

    <!-- Toast -->
    <Toast />
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1rem;
}

.col-12 { grid-column: span 12; }
.col-6 { grid-column: span 6; }
.col-3 { grid-column: span 3; }

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
  .md\:col-3 { grid-column: span 3; }
}

@media (min-width: 1024px) {
  .lg\:col-4 { grid-column: span 4; }
  .lg\:col-8 { grid-column: span 8; }
}

.max-w-4xl {
  max-width: 56rem;
}

.mx-auto {
  margin-left: auto;
  margin-right: auto;
}

.bg-primary {
  background: var(--ecodeli-green) !important;
}

.text-primary {
  color: var(--ecodeli-green) !important;
}
</style>
