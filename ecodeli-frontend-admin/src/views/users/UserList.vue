<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { useUsersStore } from '@/stores/users'

const router = useRouter()
const toast = useToast()
const confirm = useConfirm()
const usersStore = useUsersStore()

const filters = ref({
  search: '',
  userType: null
})

const selectedUser = ref(null)
const userDetailDialog = ref(false)

const userTypeOptions = [
  { label: 'Client', value: 'CLIENT' },
  { label: 'Livreur', value: 'LIVREUR' },
  { label: 'Prestataire', value: 'PRESTATAIRE' },
  { label: 'Commerçant', value: 'COMMERCANT' },
  { label: 'Admin', value: 'ADMIN' }
]

const loading = computed(() => usersStore.loading)
const users = computed(() => usersStore.users)

const filteredUsers = computed(() => {
  let result = users.value

  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    result = result.filter(user =>
      user.nom.toLowerCase().includes(search) ||
      user.prenom.toLowerCase().includes(search) ||
      user.email.toLowerCase().includes(search)
    )
  }

  if (filters.value.userType) {
    result = result.filter(user => {
      const userType = getUserType(user)
      return userType === filters.value.userType
    })
  }

  return result
})

const loadUsers = async () => {
  const result = await usersStore.fetchUsers()
  if (!result.success) {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: result.message,
      life: 3000
    })
  }
}

const resetFilters = () => {
  filters.value.search = ''
  filters.value.userType = null
}

const getUserType = (user) => {
  if (user.userType)
    return user.userType

  const className = user.constructor?.name || ''
  return className.toUpperCase() || 'UNKNOWN'
}

const getTypeTagSeverity = (userType) => {
  const type = getUserType({ userType })
  const severities = {
    'CLIENT': 'info',
    'LIVREUR': 'success',
    'PRESTATAIRE': 'warning',
    'COMMERCANT': 'secondary',
    'ADMIN': 'danger'
  }
  return severities[type] || 'secondary'
}

const goToCreate = () => {
  router.push('/admin/users/create')
}

const viewUser = (user) => {
  selectedUser.value = user
  userDetailDialog.value = true
}

const editUser = (user) => {
  router.push(`/admin/users/${user.idUtilisateur}/edit`)
}

const deleteUser = (user) => {
  confirm.require({
    message: `Voulez-vous vraiment supprimer l'utilisateur ${user.prenom} ${user.nom} ?`,
    header: 'Confirmer la suppression',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    acceptLabel: 'Supprimer',
    rejectLabel: 'Annuler',
    accept: async () => {
      const result = await usersStore.deleteUser(user.idUtilisateur)
      if (result.success) {
        toast.add({
          severity: 'success',
          summary: 'Suppression réussie',
          detail: `${user.prenom} ${user.nom} a été supprimé`,
          life: 3000
        })
      } else {
        toast.add({
          severity: 'error',
          summary: 'Erreur',
          detail: result.message,
          life: 3000
        })
      }
    }
  })
}

const exportUsers = () => {
  toast.add({
    severity: 'info',
    summary: 'Export',
    detail: 'Fonctionnalité d\'export en cours de développement',
    life: 3000
  })
}

onMounted(() => {
  loadUsers()
})
</script>

<template>
  <div class="users-page">
    <div class="page-header mb-4">
      <div class="flex justify-content-between align-items-center">
        <div>
          <h1 class="text-3xl font-bold text-800 m-0">
            <i class="pi pi-users mr-3 text-blue-500"></i>
            Gestion des Utilisateurs
          </h1>
          <p class="text-600 mt-2">Gérer tous les utilisateurs de la plateforme EcoDeli</p>
        </div>
        <Button
          label="Nouvel Utilisateur"
          icon="pi pi-plus"
          class="btn-primary"
          @click="goToCreate"
        />
      </div>
    </div>

    <Card class="mb-4">
      <template #content>
        <div class="grid">
          <div class="col-12 md:col-4">
            <div class="field">
              <label for="search" class="field-label">Rechercher</label>
              <InputText
                id="search"
                v-model="filters.search"
                placeholder="Nom, email..."
                class="w-full"
                @input="onFilter"
              />
            </div>
          </div>
          <div class="col-12 md:col-4">
            <div class="field">
              <label for="userType" class="field-label">Type d'utilisateur</label>
              <Dropdown
                id="userType"
                v-model="filters.userType"
                :options="userTypeOptions"
                optionLabel="label"
                optionValue="value"
                placeholder="Tous les types"
                class="w-full"
                showClear
                @change="onFilter"
              />
            </div>
          </div>
          <div class="col-12 md:col-4">
            <div class="field">
              <label class="field-label">&nbsp;</label>
              <div class="flex gap-2">
                <Button
                  label="Filtrer"
                  icon="pi pi-search"
                  @click="onFilter"
                />
                <Button
                  label="Reset"
                  icon="pi pi-refresh"
                  class="p-button-outlined"
                  @click="resetFilters"
                />
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>

    <Card class="admin-datatable">
      <template #header>
        <div class="flex justify-content-between align-items-center">
          <h3 class="m-0">Liste des Utilisateurs</h3>
          <div class="flex gap-2">
            <Button
              icon="pi pi-refresh"
              class="p-button-outlined p-button-sm"
              @click="loadUsers"
              :loading="loading"
            />
            <Button
              icon="pi pi-download"
              class="p-button-outlined p-button-sm"
              @click="exportUsers"
            />
          </div>
        </div>
      </template>

      <template #content>
        <DataTable
          :value="filteredUsers"
          :paginator="true"
          :rows="10"
          :loading="loading"
          dataKey="idUtilisateur"
          :globalFilterFields="['nom', 'prenom', 'email']"
          responsiveLayout="scroll"
        >
          <Column field="idUtilisateur" header="ID" :sortable="true" style="min-width: 80px">
            <template #body="slotProps">
              <Tag :value="slotProps.data.idUtilisateur" severity="secondary" />
            </template>
          </Column>

          <Column field="prenom" header="Prénom" :sortable="true" style="min-width: 120px"></Column>
          <Column field="nom" header="Nom" :sortable="true" style="min-width: 120px"></Column>
          <Column field="email" header="Email" :sortable="true" style="min-width: 200px"></Column>
          <Column field="userType" header="Type" :sortable="true" style="min-width: 120px">

            <template #body="slotProps">
              <Tag
                :value="slotProps.data.userType"
                :severity="getTypeTagSeverity(slotProps.data.userType)"
              />
            </template>
          </Column>

          <Column header="Actions" style="min-width: 120px">
            <template #body="slotProps">
              <div class="flex gap-2">
                <Button
                  icon="pi pi-eye"
                  class="p-button-sm p-button-outlined"
                  @click="viewUser(slotProps.data)"
                />
                <Button
                  icon="pi pi-pencil"
                  class="p-button-sm p-button-outlined"
                  @click="editUser(slotProps.data)"
                />
                <Button
                  icon="pi pi-trash"
                  class="p-button-sm p-button-outlined p-button-danger"
                  @click="deleteUser(slotProps.data)"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <Dialog
      v-model:visible="userDetailDialog"
      modal
      header="Détails de l'utilisateur"
      :style="{ width: '500px' }"
    >
      <div v-if="selectedUser" class="user-details">
        <div class="detail-row">
          <strong>ID:</strong> {{ selectedUser.idUtilisateur }}
        </div>
        <div class="detail-row">
          <strong>Nom complet:</strong> {{ selectedUser.prenom }} {{ selectedUser.nom }}
        </div>
        <div class="detail-row">
          <strong>Email:</strong> {{ selectedUser.email }}
        </div>
        <div class="detail-row">
          <strong>Type:</strong>
          <Tag
            :value="getUserType(selectedUser)"
            :severity="getTypeTagSeverity(getUserType(selectedUser))"
            class="ml-2"
          />
        </div>
      </div>

      <template #footer>
        <Button
          label="Modifier"
          icon="pi pi-pencil"
          @click="editUser(selectedUser)"
          class="p-button-outlined mr-2"
        />
        <Button
          label="Fermer"
          icon="pi pi-times"
          @click="userDetailDialog = false"
          class="p-button-text"
        />
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.users-page {
  padding: 0;
}

.page-header {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  padding: 2rem;
  border-radius: 12px;
}

.field {
  margin-bottom: 1rem;
}

.field-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--surface-700);
}

.grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1rem;
}

.col-12 { grid-column: span 12; }
.col-4 { grid-column: span 4; }

@media (min-width: 768px) {
  .md\:col-4 { grid-column: span 4; }
}

.flex {
  display: flex;
}

.justify-content-between {
  justify-content: space-between;
}

.align-items-center {
  align-items: center;
}

.gap-2 {
  gap: 0.5rem;
}

.w-full {
  width: 100%;
}

.mb-4 {
  margin-bottom: 1.5rem;
}

.mt-2 {
  margin-top: 0.5rem;
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

/* Styles pour le dialog */
.user-details {
  padding: 1rem 0;
}

.detail-row {
  display: flex;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--surface-200);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row strong {
  min-width: 140px;
  color: var(--surface-700);
}

.ml-2 {
  margin-left: 0.5rem;
}

.mr-2 {
  margin-right: 0.5rem;
}
</style>
