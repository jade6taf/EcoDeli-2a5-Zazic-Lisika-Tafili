<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAnnoncesStore } from '@/stores/annonces'
import { useCandidaturesStore } from '@/stores/candidatures'
import { useLivraisonsStore } from '@/stores/livraisons'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'

const router = useRouter()
const authStore = useAuthStore()
const annoncesStore = useAnnoncesStore()
const candidaturesStore = useCandidaturesStore()
const livraisonsStore = useLivraisonsStore()
const toast = useToast()
const confirm = useConfirm()

watch(() => livraisonsStore.lastCompletedDelivery, (newVal) => {
  if (newVal) {
    loadMyDeliveries()
  }
})

const availableAnnonces = ref([])
const myDeliveries = ref([])
const loadingAvailable = ref(false)
const loadingDeliveries = ref(false)

const menuItems = [
  {
    label: 'Annonces disponibles',
    icon: 'pi pi-shopping-cart',
    command: () => loadAvailableAnnonces()
  },
  {
    label: 'Livraisons partielles',
    icon: 'pi pi-clone',
    command: () => router.push('/livreur/partielles')
  },
  {
    label: 'Mes livraisons',
    icon: 'pi pi-truck',
    command: () => router.push('/livreur/livraisons')
  },
  {
    label: 'Mon Portefeuille',
    icon: 'pi pi-wallet',
    command: () => router.push('/livreur/portefeuille')
  }
]

const userInitials = computed(() => {
  if (!authStore.user) return '?'
  const prenom = authStore.user.prenom || ''
  const nom = authStore.user.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
})

const livraisonsEnCours = computed(() => {
  return myDeliveries.value.filter(livraison =>
    livraison.statut === 'VALIDEE' ||
    livraison.statut === 'EN_COURS' ||
    livraison.statut === 'SEGMENT_2_EN_COURS'
  ).length
})

const livraisonsTerminees = computed(() => {
  return myDeliveries.value.filter(livraison => livraison.statut === 'TERMINEE').length
})

const revenus = computed(() => {
  return myDeliveries.value
    .filter(livraison => livraison.statut === 'TERMINEE')
    .reduce((total, livraison) => {
      const montant = livraison.typeLivraison === 'PARTIELLE'
        ? (parseFloat(livraison.prix) || 0)
        : (parseFloat(livraison.prix) || 0)
      return total + montant
    }, 0)
    .toFixed(2)
})

const getStatutLabel = (statut) => {
  const labels = {
    'PUBLIEE': 'Disponible',
    'VALIDEE': 'Prise en charge',
    'EN_COURS': 'En cours',
    'TERMINEE': 'Terminée'
  }
  return labels[statut] || statut
}

const getStatutSeverity = (statut) => {
  const severities = {
    'PUBLIEE': 'info',
    'VALIDEE': 'warning',
    'EN_COURS': 'success',
    'TERMINEE': 'success'
  }
  return severities[statut] || 'secondary'
}

const getActionButton = (annonce) => {
  switch (annonce.statut) {
    case 'PUBLIEE':
      return { label: 'Prendre en charge', icon: 'pi pi-hand-paper', severity: 'info' }
    case 'VALIDEE':
      return { label: 'Commencer livraison', icon: 'pi pi-play', severity: 'success' }
    case 'EN_COURS':
      return { label: 'Terminer livraison', icon: 'pi pi-check', severity: 'success' }
    default:
      return null
  }
}

const loadAvailableAnnonces = async () => {
  loadingAvailable.value = true
  try {
    const result = await annoncesStore.fetchAvailableAnnonces()
    if (result.success) {
      availableAnnonces.value = result.data
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
    loadingAvailable.value = false
  }
}

const loadMyDeliveries = async () => {
  if (!authStore.user?.id && !authStore.user?.idUtilisateur) return

  loadingDeliveries.value = true
  try {
    const userId = authStore.user.id || authStore.user.idUtilisateur
    const result = await livraisonsStore.fetchLivraisonsByLivreur(userId)
    if (result.success) {
      myDeliveries.value = result.data
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
    loadingDeliveries.value = false
  }
}

const handleAnnonceAction = async (annonce) => {
  const userId = authStore.user.id || authStore.user.idUtilisateur

  switch (annonce.statut) {
    case 'PUBLIEE':
      await takeAnnonce(annonce, userId)
      break
    case 'VALIDEE':
      await startDelivery(annonce)
      break
    case 'EN_COURS':
      await completeDelivery(annonce)
      break
  }
}

const takeAnnonce = async (annonce, userId) => {
  confirm.require({
    message: `Voulez-vous vous proposer pour la livraison "${annonce.titre}" ?`,
    header: 'Se proposer pour la livraison',
    icon: 'pi pi-question-circle',
    acceptLabel: 'Oui, me proposer',
    rejectLabel: 'Annuler',
    accept: async () => {
      const result = await candidaturesStore.postulerPourAnnonce(
        annonce.idAnnonce,
        userId,
        `Je souhaite effectuer cette livraison de ${annonce.adresseDepart} vers ${annonce.adresseFin}.`
      )

      if (result.success) {
        toast.add({
          severity: 'success',
          summary: 'Candidature envoyée',
          detail: `Votre candidature pour "${annonce.titre}" a été envoyée au client`,
          life: 3000
        })

        await loadAvailableAnnonces()
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

const startDelivery = async (livraison) => {
  const userId = authStore.user.id || authStore.user.idUtilisateur
  const result = await livraisonsStore.startLivraison(livraison.idLivraison, userId)
  if (result.success) {
    toast.add({
      severity: 'success',
      summary: 'Livraison commencée',
      detail: `Livraison de "${livraison.titre}" en cours`,
      life: 3000
    })

    await loadMyDeliveries()
  } else {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: result.message,
      life: 3000
    })
  }
}

const completeDelivery = async (livraison) => {
  confirm.require({
    message: `Confirmer la livraison terminée pour "${livraison.titre}" ?`,
    header: 'Confirmer la livraison',
    icon: 'pi pi-check-circle',
    acceptClass: 'p-button-success',
    acceptLabel: 'Oui, livraison terminée',
    rejectLabel: 'Pas encore',
    accept: async () => {
      const userId = authStore.user.id || authStore.user.idUtilisateur
      const result = await livraisonsStore.completeLivraison(livraison.idLivraison, userId)
      if (result.success) {
        toast.add({
          severity: 'success',
          summary: 'Livraison terminée',
          detail: `${livraison.typeLivraison === 'PARTIELLE' ? 'Segment terminé' : 'Livraison terminée'} avec succès`,
          life: 4000
        })

        await loadMyDeliveries()
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

const handleLogout = () => {
  authStore.logout()
  toast.add({
    severity: 'info',
    summary: 'Déconnexion',
    detail: 'À bientôt !',
    life: 3000
  })
  router.push('/login')
}

onMounted(() => {
  loadAvailableAnnonces()
  loadMyDeliveries()
})
</script>

<template>
  <div class="ecodeli-layout">
    <!-- Header -->
    <header class="ecodeli-header">
      <Menubar :model="menuItems" class="border-0">
        <template #start>
          <div class="flex align-items-center">
            <i class="pi pi-truck text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
            <span class="text-xl font-semibold ecodeli-title">EcoDeli - Livreur</span>
          </div>
        </template>

        <template #end>
          <div class="flex align-items-center gap-2">
            <span class="text-sm text-600">Bonjour, {{ authStore.userName }}</span>
            <Avatar
              :label="userInitials"
              class="bg-primary text-white"
              shape="circle"
            />
            <Button
              icon="pi pi-sign-out"
              text
              rounded
              severity="secondary"
              @click="handleLogout"
              title="Se déconnecter"
            />
          </div>
        </template>
      </Menubar>
    </header>

    <!-- Main Content -->
    <main class="ecodeli-content">
      <div class="grid">
        <!-- Welcome Section -->
        <div class="col-12">
          <Card>
            <template #content>
              <div class="flex justify-content-between align-items-center">
                <div>
                  <h1 class="text-3xl font-bold ecodeli-title m-0">
                    Dashboard Livreur
                  </h1>
                  <p class="text-600 mt-2 mb-0">
                    Gérez vos livraisons et trouvez de nouvelles missions
                  </p>
                </div>
                <div class="text-right">
                  <div class="text-2xl font-bold text-primary">
                    {{ new Date().toLocaleDateString('fr-FR') }}
                  </div>
                  <div class="text-sm text-600">Aujourd'hui</div>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Stats Cards -->
        <div class="col-12 md:col-4">
          <Card class="text-center h-full">
            <template #content>
              <div class="text-orange-500">
                <i class="pi pi-truck text-4xl mb-3"></i>
                <div class="text-2xl font-bold">{{ livraisonsEnCours }}</div>
                <div class="text-600">En cours</div>
              </div>
            </template>
          </Card>
        </div>

        <div class="col-12 md:col-4">
          <Card class="text-center h-full">
            <template #content>
              <div class="text-green-500">
                <i class="pi pi-check-circle text-4xl mb-3"></i>
                <div class="text-2xl font-bold">{{ livraisonsTerminees }}</div>
                <div class="text-600">Terminées</div>
              </div>
            </template>
          </Card>
        </div>

        <div class="col-12 md:col-4">
          <Card class="text-center h-full">
            <template #content>
              <div class="text-blue-500">
                <i class="pi pi-euro text-4xl mb-3"></i>
                <div class="text-2xl font-bold">{{ revenus }}€</div>
                <div class="text-600">Revenus</div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Annonces Disponibles -->
        <div class="col-12 lg:col-6">
          <Card>
            <template #header>
              <div class="px-4 pt-4 flex justify-content-between align-items-center">
                <h3 class="text-xl font-semibold ecodeli-title m-0">Annonces Disponibles</h3>
        <Button
          label="Voir mes livraisons"
          icon="pi pi-truck"
          class="p-button-lg w-full"
          @click="router.push('/livreur/livraisons')"
        />
              </div>
            </template>
            <template #content>
              <div v-if="loadingAvailable" class="text-center p-4">
                <ProgressSpinner style="width: 30px; height: 30px" />
                <p class="mt-2">Chargement...</p>
              </div>

              <div v-else-if="availableAnnonces.length === 0" class="text-center p-4">
                <i class="pi pi-inbox text-4xl text-400 mb-3"></i>
                <p class="text-600">Aucune annonce disponible</p>
              </div>

              <div v-else class="flex flex-column gap-3">
                <div
                  v-for="annonce in availableAnnonces.slice(0, 3)" 
                  :key="annonce.idAnnonce"
                  class="flex align-items-center gap-3 p-3 border-round border-1 surface-border"
                >
                  <div>
                    <Tag value="Disponible" severity="info" />
                  </div>
                  <div class="flex-1">
                    <div class="font-medium">{{ annonce.titre }}</div>
                    <div class="text-sm text-600">
                      {{ annonce.adresseDepart }} → {{ annonce.adresseFin }}
                    </div>
                    <div class="text-sm text-primary font-bold">{{ annonce.prixUnitaire }}€</div>
                  </div>
                  <div>
                    <Button
                      label="Se proposer"
                      icon="pi pi-send"
                      class="p-button-sm"
                      @click="handleAnnonceAction(annonce)"
                    />
                  </div>
                </div>

                <div v-if="availableAnnonces.length > 3" class="text-center">
                  <Button
                    label="Voir toutes les annonces"
                    icon="pi pi-list"
                    class="p-button-outlined p-button-sm"
                  />
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Mes Livraisons -->
        <div class="col-12 lg:col-6">
          <Card>
            <template #header>
              <div class="px-4 pt-4 flex justify-content-between align-items-center">
                <h3 class="text-xl font-semibold ecodeli-title m-0">Mes Livraisons</h3>
                <Button
                  icon="pi pi-refresh"
                  class="p-button-outlined p-button-sm"
                  @click="loadMyDeliveries"
                  :loading="loadingDeliveries"
                />
              </div>
            </template>
            <template #content>
              <div v-if="loadingDeliveries" class="text-center p-4">
                <ProgressSpinner style="width: 30px; height: 30px" />
                <p class="mt-2">Chargement...</p>
              </div>

              <div v-else-if="myDeliveries.length === 0" class="text-center p-4">
                <i class="pi pi-truck text-4xl text-400 mb-3"></i>
                <p class="text-600">Aucune livraison en cours</p>
              </div>

              <div v-else class="flex flex-column gap-3">
                <div
                  v-for="delivery in myDeliveries.slice(0, 3)"
                  :key="delivery.idAnnonce"
                  class="flex align-items-center gap-3 p-3 border-round border-1 surface-border"
                >
                  <div>
                    <Tag
                      :value="getStatutLabel(delivery.statut)"
                      :severity="getStatutSeverity(delivery.statut)"
                    />
                  </div>
                  <div class="flex-1">
                    <div class="font-medium">{{ delivery.titre }}</div>
                    <div class="text-sm text-600">
                      {{ delivery.adresseDepart }} → {{ delivery.adresseFin }}
                    </div>
                    <div class="text-sm text-primary font-bold">{{ delivery.prix }}€{{ delivery.typeLivraison === 'PARTIELLE' ? ' (votre part)' : '' }}</div>
                  </div>
                  <div v-if="getActionButton(delivery)">
                    <Button
                      :label="getActionButton(delivery).label"
                      :icon="getActionButton(delivery).icon"
                      :severity="getActionButton(delivery).severity"
                      class="p-button-sm"
                      @click="handleAnnonceAction(delivery)"
                    />
                  </div>
                </div>
              </div>
            </template>
          </Card>
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
.col-4 { grid-column: span 4; }

@media (min-width: 768px) {
  .md\:col-4 { grid-column: span 4; }
  .md\:col-6 { grid-column: span 6; }
}

@media (min-width: 1024px) {
  .lg\:col-6 { grid-column: span 6; }
}

.h-full {
  height: 100%;
}

.bg-primary {
  background: var(--ecodeli-green) !important;
}

.text-primary {
  color: var(--ecodeli-green) !important;
}
</style>
