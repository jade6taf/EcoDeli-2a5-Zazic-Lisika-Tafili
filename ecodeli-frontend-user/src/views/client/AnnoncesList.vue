<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAnnoncesStore } from '@/stores/annonces'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'

const router = useRouter()
const authStore = useAuthStore()
const annoncesStore = useAnnoncesStore()
const toast = useToast()
const confirm = useConfirm()

const myAnnonces = ref([])
const loading = ref(false)
const selectedFilter = ref('TOUTES')

const userInitials = computed(() => {
  if (!authStore.user) return '?'
  const prenom = authStore.user.prenom || ''
  const nom = authStore.user.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
})

const statusFilters = [
  { label: 'Toutes', value: 'TOUTES', icon: 'pi pi-list' },
  { label: 'Publiées', value: 'PUBLIEE', icon: 'pi pi-eye' },
  { label: 'Validées', value: 'VALIDEE', icon: 'pi pi-check' },
  { label: 'En cours', value: 'EN_COURS', icon: 'pi pi-truck' },
  { label: 'Terminées', value: 'TERMINEE', icon: 'pi pi-check-circle' },
  { label: 'Annulées', value: 'ANNULEE', icon: 'pi pi-times-circle' }
]

const filteredAnnonces = computed(() => {
  if (selectedFilter.value === 'TOUTES') {
    return myAnnonces.value
  }
  return myAnnonces.value.filter(annonce => annonce.statut === selectedFilter.value)
})

const getStatutLabel = (statut) => {
  const labels = {
    'PUBLIEE': 'Publiée',
    'VALIDEE': 'Validée',
    'EN_COURS': 'En cours',
    'TERMINEE': 'Terminée',
    'ANNULEE': 'Annulée'
  }
  return labels[statut] || statut
}

const getStatutSeverity = (statut) => {
  const severities = {
    'PUBLIEE': 'info',
    'VALIDEE': 'warning',
    'EN_COURS': 'success',
    'TERMINEE': 'success',
    'ANNULEE': 'danger'
  }
  return severities[statut] || 'secondary'
}

const getTimelineIcon = (statut) => {
  const icons = {
    'PUBLIEE': 'pi pi-eye',
    'VALIDEE': 'pi pi-check',
    'EN_COURS': 'pi pi-truck',
    'TERMINEE': 'pi pi-check-circle',
    'ANNULEE': 'pi pi-times-circle'
  }
  return icons[statut] || 'pi pi-circle'
}

const getTimelineColor = (statut) => {
  const colors = {
    'PUBLIEE': '#2196f3',
    'VALIDEE': '#ffa726',
    'EN_COURS': '#66bb6a',
    'TERMINEE': '#4caf50',
    'ANNULEE': '#f44336'
  }
  return colors[statut] || '#9e9e9e'
}

const getActionButtons = (annonce) => {
  const buttons = []

  switch (annonce.statut) {
    case 'PUBLIEE':
      if (annonce.livraisonPartielleAutorisee) {
        buttons.push(
          { label: 'Gérer candidatures partielles', icon: 'pi pi-clone', severity: 'info', action: 'candidatures-partielles' }
        )
      } else {
        buttons.push(
          { label: 'Voir candidatures', icon: 'pi pi-users', severity: 'info', action: 'candidatures' }
        )
      }
      buttons.push(
        { label: 'Modifier', icon: 'pi pi-pencil', severity: 'secondary', action: 'edit' },
        { label: 'Annuler', icon: 'pi pi-times', severity: 'danger', action: 'cancel' }
      )
      break
    case 'SEGMENT_1_PRIS':
    case 'SEGMENT_2_PRIS':
    case 'SEGMENTS_COMPLETS':
      buttons.push(
        { label: 'Gérer candidatures partielles', icon: 'pi pi-clone', severity: 'info', action: 'candidatures-partielles' }
      )
      break
    case 'VALIDEE':
    case 'EN_COURS':
      buttons.push(
        { label: 'Suivre livraison', icon: 'pi pi-map-marker', severity: 'info', action: 'track' }
      )
      break
    case 'TERMINEE':
      break
  }

  return buttons
}

const loadMyAnnonces = async () => {
  if (!authStore.user?.id && !authStore.user?.idUtilisateur)
    return

  loading.value = true
  try {
    const userId = authStore.user.id || authStore.user.idUtilisateur
    const result = await annoncesStore.fetchUserAnnonces(userId)
    if (result.success) {
      myAnnonces.value = result.data.sort((a, b) => new Date(b.dateCreation || 0) - new Date(a.dateCreation || 0))
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
  } finally {
    loading.value = false
  }
}

const handleAnnonceAction = async (annonce, action) => {
  switch (action) {
    case 'candidatures':
      router.push(`/client/annonce/${annonce.idAnnonce}/candidatures`)
      break
    case 'candidatures-partielles':
      router.push(`/client/candidatures-partielles/${annonce.idAnnonce}`)
      break
    case 'edit':
      router.push(`/client/annonce/${annonce.idAnnonce}/edit`)
      break
    case 'cancel':
      cancelAnnonce(annonce)
      break
    case 'track':
      toast.add({
        severity: 'info',
        summary: 'Suivi',
        detail: 'Fonctionnalité de suivi en cours de développement',
        life: 3000
      })
      break
    case 'details':
      toast.add({
        severity: 'info',
        summary: 'Détails',
        detail: 'Page de détails en cours de développement',
        life: 3000
      })
      break
  }
}

const cancelAnnonce = async (annonce) => {
  confirm.require({
    message: `Êtes-vous sûr de vouloir annuler l'annonce "${annonce.titre}" ?`,
    header: 'Confirmer l\'annulation',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    acceptLabel: 'Oui, annuler',
    rejectLabel: 'Non',
    accept: async () => {
      const result = await annoncesStore.cancelAnnonce(annonce.idAnnonce)
      if (result.success) {
        toast.add({
          severity: 'success',
          summary: 'Annonce annulée',
          detail: `L'annonce "${annonce.titre}" a été annulée`,
          life: 3000
        })

        const index = myAnnonces.value.findIndex(a => a.idAnnonce === annonce.idAnnonce)
        if (index !== -1) {
          myAnnonces.value[index].statut = 'ANNULEE'
        }
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

const goBack = () => {
  router.push('/client')
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadMyAnnonces()
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
            rounded
            @click="goBack"
            class="mr-3"
          />
          <i class="pi pi-list text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
          <span class="text-xl font-semibold ecodeli-title">Mes Annonces</span>
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
      <div class="max-w-6xl mx-auto">
        <!-- En-tête -->
        <Card class="mb-4">
          <template #content>
            <div class="flex justify-content-between align-items-center">
              <div>
                <h1 class="text-3xl font-bold ecodeli-title m-0">
                  <i class="pi pi-list mr-3" style="color: var(--ecodeli-green)"></i>
                  Mes Annonces
                </h1>
                <p class="text-600 mt-2">Gérez toutes vos demandes de livraison</p>
              </div>
              <Button
                icon="pi pi-refresh"
                class="p-button-outlined"
                @click="loadMyAnnonces"
                :loading="loading"
                label="Actualiser"
              />
            </div>
          </template>
        </Card>

        <!-- Liste des annonces -->
        <div v-if="loading" class="text-center p-6">
          <ProgressSpinner style="width: 50px; height: 50px" />
          <p class="mt-3">Chargement de vos annonces...</p>
        </div>

        <div v-else-if="filteredAnnonces.length === 0" class="text-center p-6">
          <Card>
            <template #content>
              <i class="pi pi-list text-6xl text-400 mb-4"></i>
              <h3 class="text-xl font-semibold">
                {{ selectedFilter === 'TOUTES' ? 'Aucune annonce' : `Aucune annonce ${getStatutLabel(selectedFilter).toLowerCase()}` }}
              </h3>
              <p class="text-600 mb-4">
                {{ selectedFilter === 'TOUTES' ? 'Vous n\'avez pas encore créé d\'annonces' : 'Aucune annonce dans cette catégorie' }}
              </p>
              <div class="flex gap-2 justify-content-center">
                <Button
                  v-if="selectedFilter !== 'TOUTES'"
                  label="Voir toutes"
                  icon="pi pi-list"
                  outlined
                  @click="selectedFilter = 'TOUTES'"
                />
                <Button
                  label="Créer une annonce"
                  icon="pi pi-plus"
                  @click="router.push('/client/create-annonce')"
                />
              </div>
            </template>
          </Card>
        </div>

        <div v-else class="grid gap-4">
          <div
            v-for="annonce in filteredAnnonces"
            :key="annonce.idAnnonce"
            class="col-12"
          >
            <Card>
              <template #content>
                <div class="flex align-items-start gap-4">
                  <!-- Timeline -->
                  <div class="flex flex-column align-items-center">
                    <div
                      class="flex align-items-center justify-content-center border-circle"
                      :style="{
                        width: '50px',
                        height: '50px',
                        backgroundColor: getTimelineColor(annonce.statut),
                        color: 'white'
                      }"
                    >
                      <i :class="getTimelineIcon(annonce.statut)" class="text-xl"></i>
                    </div>
                    <div class="mt-2 text-center">
                      <Tag
                        :value="getStatutLabel(annonce.statut)"
                        :severity="getStatutSeverity(annonce.statut)"
                      />
                    </div>
                  </div>

                  <!-- Contenu -->
                  <div class="flex-1">
                    <div class="flex justify-content-between align-items-start mb-3">
                      <div>
                        <h3 class="text-xl font-semibold ecodeli-title m-0">{{ annonce.titre }}</h3>
                        <p class="text-600 mt-1">{{ annonce.description }}</p>
                      </div>
                      <div class="text-right">
                        <div class="text-2xl font-bold text-primary">{{ annonce.prixUnitaire }}€</div>
                        <div class="text-sm text-600">Prix de livraison</div>
                      </div>
                    </div>

                    <!-- Adresses -->
                    <div class="grid mb-4">
                      <div class="col-12 md:col-6">
                        <div class="flex align-items-center gap-2 mb-2">
                          <i class="pi pi-map-marker text-orange-500"></i>
                          <span class="font-medium text-600">Départ</span>
                        </div>
                        <p class="text-900 m-0">{{ annonce.adresseDepart }}</p>
                      </div>
                      <div class="col-12 md:col-6">
                        <div class="flex align-items-center gap-2 mb-2">
                          <i class="pi pi-flag text-green-500"></i>
                          <span class="font-medium text-600">Arrivée</span>
                        </div>
                        <p class="text-900 m-0">{{ annonce.adresseFin }}</p>
                      </div>
                    </div>

                    <!-- Email destinataire -->
                    <div v-if="annonce.emailDestinataire" class="mb-4">
                      <div class="flex align-items-center gap-2 mb-1">
                        <i class="pi pi-envelope text-blue-500"></i>
                        <span class="font-medium text-600">Destinataire</span>
                      </div>
                      <p class="text-900 m-0">{{ annonce.emailDestinataire }}</p>
                    </div>

                    <!-- Informations colis -->
                    <div v-if="annonce.colis" class="surface-100 border-round p-3 mb-4">
                      <h4 class="font-medium text-600 m-0 mb-2">Informations colis</h4>
                      <div class="grid">
                        <div class="col-6 md:col-3">
                          <span class="text-sm text-600">Poids:</span>
                          <div class="font-medium">{{ annonce.colis.poids }}kg</div>
                        </div>
                        <div v-if="annonce.colis.fragile" class="col-6 md:col-3">
                          <Tag value="Fragile" severity="danger" />
                        </div>
                      </div>
                      <div v-if="annonce.colis.description" class="mt-2">
                        <span class="text-sm text-600">Description:</span>
                        <p class="m-0 mt-1">{{ annonce.colis.description }}</p>
                      </div>
                    </div>

                    <!-- Actions -->
                    <div class="flex gap-2 justify-content-end">
                      <Button
                        v-for="button in getActionButtons(annonce)"
                        :key="button.action"
                        :label="button.label"
                        :icon="button.icon"
                        :severity="button.severity"
                        @click="handleAnnonceAction(annonce, button.action)"
                        size="small"
                      />
                    </div>
                  </div>
                </div>
              </template>
            </Card>
          </div>
        </div>
      </div>
    </main>

    <!-- Toast -->
    <Toast />

    <!-- Confirm Dialog -->
    <ConfirmDialog />
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

.max-w-6xl {
  max-width: 72rem;
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
