<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUsersStore } from '@/stores/users'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const usersStore = useUsersStore()
const toast = useToast()

const stats = ref({
  totalUsers: 0,
  clients: 0,
  livreurs: 0,
  prestataires: 0,
  commercants: 0
})

const goToUsers = () => {
  router.push('/admin/users')
}

const goToAdminRegister = () => {
  router.push('/admin/register')
}

const goToStats = () => {
  router.push('/admin/stats')
}

const loadStats = async () => {
  try {
    await usersStore.fetchUsers()
    stats.value = usersStore.stats

    toast.add({
      severity: 'success',
      summary: 'Dashboard mis à jour',
      detail: `${stats.value.totalUsers} utilisateurs chargés`,
      life: 3000
    })
  } catch (error) {
    console.error('Erreur lors du chargement des stats:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger les statistiques',
      life: 3000
    })
  }
}

onMounted(() => {
  loadStats()
})
</script>

<template>
  <div class="dashboard">
    <div class="dashboard-header mb-4">
      <h1 class="text-3xl font-bold text-800">
        <i class="pi pi-home mr-3 text-green-500"></i>
        Dashboard EcoDeli
      </h1>
      <p class="text-600 mt-2">Vue d'ensemble de la plateforme de crowdshipping écologique</p>
    </div>

    <div class="dashboard-cards">
      <Card class="stat-card">
        <template #content>
          <div class="flex align-items-center justify-content-between">
            <div>
              <div class="stat-value">{{ stats.totalUsers }}</div>
              <div class="stat-label">Utilisateurs Total</div>
            </div>
            <div class="stat-icon bg-blue-100 text-blue-600">
              <i class="pi pi-users text-2xl"></i>
            </div>
          </div>
        </template>
      </Card>

      <Card class="stat-card">
        <template #content>
          <div class="flex align-items-center justify-content-between">
            <div>
              <div class="stat-value">{{ stats.clients }}</div>
              <div class="stat-label">Clients</div>
            </div>
            <div class="stat-icon bg-green-100 text-green-600">
              <i class="pi pi-user text-2xl"></i>
            </div>
          </div>
        </template>
      </Card>

      <Card class="stat-card">
        <template #content>
          <div class="flex align-items-center justify-content-between">
            <div>
              <div class="stat-value">{{ stats.livreurs }}</div>
              <div class="stat-label">Livreurs</div>
            </div>
            <div class="stat-icon bg-orange-100 text-orange-600">
              <i class="pi pi-truck text-2xl"></i>
            </div>
          </div>
        </template>
      </Card>

      <Card class="stat-card">
        <template #content>
          <div class="flex align-items-center justify-content-between">
            <div>
              <div class="stat-value">{{ stats.prestataires }}</div>
              <div class="stat-label">Prestataires</div>
            </div>
            <div class="stat-icon bg-purple-100 text-purple-600">
              <i class="pi pi-wrench text-2xl"></i>
            </div>
          </div>
        </template>
      </Card>
    </div>

    <div class="grid mt-6">
      <div class="col-12 lg:col-6">
        <Card>
          <template #header>
            <div class="p-4 border-bottom-1 surface-border">
              <h3 class="m-0">
                <i class="pi pi-bolt mr-2 text-yellow-500"></i>
                Actions Rapides
              </h3>
            </div>
          </template>
          <template #content>
            <div class="flex flex-column gap-3">
              <Button
                label="Gérer les Utilisateurs"
                icon="pi pi-users"
                class="p-button-outlined"
                @click="goToUsers"
              />
              <Button
                label="Créer un Compte Admin"
                icon="pi pi-user-plus"
                class="p-button-outlined"
                @click="goToAdminRegister"
              />
              <Button
                label="Voir les Statistiques"
                icon="pi pi-chart-bar"
                class="p-button-outlined"
                @click="goToStats"
              />
            </div>
          </template>
        </Card>
      </div>

      <div class="col-12 lg:col-6">
        <Card>
          <template #header>
            <div class="p-4 border-bottom-1 surface-border">
              <h3 class="m-0">
                <i class="pi pi-info-circle mr-2 text-blue-500"></i>
                Informations Système
              </h3>
            </div>
          </template>
          <template #content>
            <div class="flex flex-column gap-3">
              <div class="flex justify-content-between">
                <span class="font-semibold">Version:</span>
                <span>1.0.0</span>
              </div>
              <div class="flex justify-content-between">
                <span class="font-semibold">Environnement:</span>
                <Tag value="Développement" severity="info"></Tag>
              </div>
              <div class="flex justify-content-between">
                <span class="font-semibold">Backend:</span>
                <Tag value="Connecté" severity="success"></Tag>
              </div>
              <div class="flex justify-content-between">
                <span class="font-semibold">Base de données:</span>
                <Tag value="MariaDB" severity="info"></Tag>
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 0;
}

.dashboard-header {
  background: linear-gradient(135deg, var(--ecodeli-green-light), #dcfce7);
  padding: 2rem;
  border-radius: 12px;
  margin-bottom: 2rem;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1.5rem;
}

.col-12 {
  grid-column: span 12;
}

.col-6 {
  grid-column: span 6;
}

@media (min-width: 1024px) {
  .lg\:col-6 {
    grid-column: span 6;
  }
}

.mb-4 {
  margin-bottom: 1.5rem;
}

.mt-2 {
  margin-top: 0.5rem;
}

.mt-6 {
  margin-top: 2rem;
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

.p-4 {
  padding: 1rem;
}

.text-3xl {
  font-size: 1.875rem;
}

.text-2xl {
  font-size: 1.25rem;
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

.font-semibold {
  font-weight: 600;
}

.flex {
  display: flex;
}

.flex-column {
  flex-direction: column;
}

.align-items-center {
  align-items: center;
}

.justify-content-between {
  justify-content: space-between;
}

.gap-3 {
  gap: 0.75rem;
}

.border-bottom-1 {
  border-bottom: 1px solid var(--surface-border);
}

.surface-border {
  border-color: var(--surface-border);
}
</style>
