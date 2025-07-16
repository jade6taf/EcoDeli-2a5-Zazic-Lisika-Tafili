<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAnnoncesStore } from '@/stores/annonces'
import { useCandidaturesStore } from '@/stores/candidatures'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'

const router = useRouter()
const authStore = useAuthStore()
const annoncesStore = useAnnoncesStore()
const candidaturesStore = useCandidaturesStore()
const toast = useToast()
const confirm = useConfirm()

const userAnnonces = ref([])
const loadingAnnonces = ref(false)

const candidaturesByAnnonce = ref({})
const loadingCandidatures = ref(false)

const totalServicesDepenses = ref(0)

const menuItems = [
  {
    label: 'Accueil',
    icon: 'pi pi-home',
    command: () => router.push('/client')
  },
  {
    label: 'Mes demandes',
    icon: 'pi pi-list',
    items: [
      {
        label: 'Livraisons',
        icon: 'pi pi-truck',
        command: () => router.push('/client/annonces')
      },
      {
        label: 'Services',
        icon: 'pi pi-wrench',
        command: () => router.push('/client/demandes-services')
      }
    ]
  },
  {
    label: 'Mes Services',
    icon: 'pi pi-cog',
    command: () => router.push('/client/services')
  },
  {
    label: 'Historique',
    icon: 'pi pi-history',
  },
  {
    label: 'Profil',
    icon: 'pi pi-user',
    command: () => router.push('/profile')
  }
]

const userInitials = computed(() => {
  if (!authStore.user) return '?'
  const prenom = authStore.user.prenom || ''
  const nom = authStore.user.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
})

const annoncesActives = computed(() => {
  return userAnnonces.value.filter(annonce =>
    annonce.statut === 'PUBLIEE' ||
    annonce.statut === 'VALIDEE' ||
    annonce.statut === 'SEGMENTS_COMPLETS'
  ).length
})

const annoncesEnCours = computed(() => {
  return userAnnonces.value.filter(annonce =>
    annonce.statut === 'EN_COURS' ||
    annonce.statut === 'EN_COURS_SEGMENT_1' ||
    annonce.statut === 'EN_COURS_SEGMENT_2'
  ).length
})

const annoncesTerminees = computed(() => {
  return userAnnonces.value.filter(annonce => annonce.statut === 'TERMINEE').length
})

const totalDepense = computed(() => {
  const totalLivraisons = userAnnonces.value
    .filter(annonce => annonce.statut === 'TERMINEE')
    .reduce((total, annonce) => total + (parseFloat(annonce.prixUnitaire) || 0), 0)
  
  const totalServices = totalServicesDepenses.value || 0
  
  return (totalLivraisons + totalServices).toFixed(2)
})

const getStatutLabel = (statut) => {
  const labels = {
    'PUBLIEE': 'Publiée',
    'VALIDEE': 'Acceptée',
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

const loadUserAnnonces = async () => {
  if (!authStore.user?.id && !authStore.user?.idUtilisateur) {
    console.error('ID utilisateur non disponible')
    return
  }

  loadingAnnonces.value = true
  try {
    const userId = authStore.user.id || authStore.user.idUtilisateur
    const result = await annoncesStore.fetchUserAnnonces(userId)

    if (result.success) {
      userAnnonces.value = result.data
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.message,
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur lors du chargement des annonces:', error)
  } finally {
    loadingAnnonces.value = false
  }
}

const handleCancelAnnonce = (annonce) => {
  confirm.require({
    message: `Voulez-vous vraiment annuler l'annonce "${annonce.titre}" ?`,
    header: 'Confirmer l\'annulation',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    acceptLabel: 'Annuler l\'annonce',
    rejectLabel: 'Conserver',
    accept: async () => {
      const result = await annoncesStore.cancelAnnonce(annonce.idAnnonce)
      if (result.success) {
        const index = userAnnonces.value.findIndex(a => a.idAnnonce === annonce.idAnnonce)
        if (index !== -1) {
          userAnnonces.value[index].statut = 'ANNULEE'
        }

        toast.add({
          severity: 'success',
          summary: 'Annonce annulée',
          detail: `L'annonce "${annonce.titre}" a été annulée`,
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

const newDeliveryRequest = () => {
  router.push('/client/create-annonce')
}

const newServiceRequest = () => {
  router.push('/client/demande-service')
}

const viewOrders = () => {
  router.push('/client/annonces')
}

const viewServicesRequests = () => {
  router.push('/client/demandes-services')
}

const hasCandidatures = computed(() => {
  return Object.keys(candidaturesByAnnonce.value).length > 0
})

const annoncesAvecCandidatures = computed(() => {
  return userAnnonces.value.filter(annonce =>
    candidaturesByAnnonce.value[annonce.idAnnonce] &&
    candidaturesByAnnonce.value[annonce.idAnnonce].length > 0
  )
})

const getCandidaturesForAnnonce = (annonceId) => {
  return candidaturesByAnnonce.value[annonceId] || []
}

const getLivreurInitials = (livreur) => {
  if (!livreur) return '?'
  const prenom = livreur.prenom || ''
  const nom = livreur.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
}

const getCandidatureStatutLabel = (statut) => {
  const labels = {
    'EN_ATTENTE': 'En attente',
    'ACCEPTEE': 'Acceptée',
    'REFUSEE': 'Refusée'
  }
  return labels[statut] || statut
}

const getCandidatureStatutSeverity = (statut) => {
  const severities = {
    'EN_ATTENTE': 'warning',
    'ACCEPTEE': 'success',
    'REFUSEE': 'danger'
  }
  return severities[statut] || 'secondary'
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadCandidatures = async () => {
  if (userAnnonces.value.length === 0) return

  loadingCandidatures.value = true
  try {
    const annoncesPubliees = userAnnonces.value.filter(annonce => annonce.statut === 'PUBLIEE')

    for (const annonce of annoncesPubliees) {
      const result = await candidaturesStore.getCandidaturesByAnnonce(annonce.idAnnonce)
      if (result.success) {
        candidaturesByAnnonce.value[annonce.idAnnonce] = result.data
      }
    }
  } catch (error) {
    console.error('Erreur lors du chargement des candidatures:', error)
  } finally {
    loadingCandidatures.value = false
  }
}

const loadTotalServicesDepenses = async () => {
  try {
    const clientId = authStore.user?.id || authStore.user?.idUtilisateur
    
    if (!clientId) {
      console.warn('ID utilisateur non trouvé pour le calcul des dépenses services')
      return
    }
    
    const response = await fetch(`http://localhost:8080/api/paiement/client/${clientId}/total-depense`)
    
    if (response.ok) {
      const data = await response.json()
      totalServicesDepenses.value = parseFloat(data.totalServicesPayes) || 0
      
      console.log('Total services dépensés:', totalServicesDepenses.value, '€')
    } else {
      console.warn('Erreur récupération total services:', response.statusText)
    }
  } catch (error) {
    console.error('Erreur lors du chargement du total services:', error)
  }
}

const handleAcceptCandidature = (candidature, annonce) => {
  confirm.require({
    message: `Voulez-vous accepter la candidature de ${candidature.livreur.prenom} ${candidature.livreur.nom} pour "${annonce.titre}" ?`,
    header: 'Accepter la candidature',
    icon: 'pi pi-question-circle',
    acceptClass: 'p-button-success',
    acceptLabel: 'Oui, accepter',
    rejectLabel: 'Annuler',
    accept: async () => {
      const result = await candidaturesStore.accepterCandidature(
        candidature.idCandidatureLivraison,
        `Candidature acceptée pour la livraison "${annonce.titre}"`
      )

      if (result.success) {
        toast.add({
          severity: 'success',
          summary: 'Candidature acceptée',
          detail: `${candidature.livreur.prenom} ${candidature.livreur.nom} va effectuer votre livraison`,
          life: 4000
        })

        const annonceIndex = userAnnonces.value.findIndex(a => a.idAnnonce === annonce.idAnnonce)
        if (annonceIndex !== -1) {
          userAnnonces.value[annonceIndex].statut = 'VALIDEE'
        }

        await loadCandidatures()
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

const handleRefuseCandidature = (candidature) => {
  confirm.require({
    message: `Voulez-vous refuser la candidature de ${candidature.livreur.prenom} ${candidature.livreur.nom} ?`,
    header: 'Refuser la candidature',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    acceptLabel: 'Oui, refuser',
    rejectLabel: 'Annuler',
    accept: async () => {
      const result = await candidaturesStore.refuserCandidature(
        candidature.idCandidatureLivraison,
        'Candidature refusée par le client'
      )

      if (result.success) {
        toast.add({
          severity: 'info',
          summary: 'Candidature refusée',
          detail: `La candidature de ${candidature.livreur.prenom} ${candidature.livreur.nom} a été refusée`,
          life: 3000
        })

        await loadCandidatures()
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

onMounted(async () => {
  await loadUserAnnonces()
  await loadCandidatures()
  await loadTotalServicesDepenses()
})
</script>

<template>
  <div class="ecodeli-layout">
    <!-- Header -->
    <header class="ecodeli-header">
      <Menubar :model="menuItems" class="border-0">
        <template #start>
          <div class="flex align-items-center">
            <i class="pi pi-leaf text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
            <span class="text-xl font-semibold ecodeli-title">EcoDeli</span>
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
              v-tooltip="'Se déconnecter'"
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
                    Tableau de bord Client
                  </h1>
                  <p class="text-600 mt-2 mb-0">
                    Bienvenue {{ authStore.userName }}, gérez vos demandes et livraisons
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

        <!-- Stats Cards - Vraies données -->
        <div class="col-12 md:col-3">
          <Card class="text-center h-full">
            <template #content>
              <div class="text-primary">
                <i class="pi pi-shopping-cart text-4xl mb-3"></i>
                <div class="text-2xl font-bold">{{ annoncesActives }}</div>
                <div class="text-600">Annonces actives</div>
              </div>
            </template>
          </Card>
        </div>

        <div class="col-12 md:col-3">
          <Card class="text-center h-full">
            <template #content>
              <div class="text-orange-500">
                <i class="pi pi-truck text-4xl mb-3"></i>
                <div class="text-2xl font-bold">{{ annoncesEnCours }}</div>
                <div class="text-600">En livraison</div>
              </div>
            </template>
          </Card>
        </div>

        <div class="col-12 md:col-3">
          <Card class="text-center h-full">
            <template #content>
              <div class="text-green-500">
                <i class="pi pi-check-circle text-4xl mb-3"></i>
                <div class="text-2xl font-bold">{{ annoncesTerminees }}</div>
                <div class="text-600">Terminées</div>
              </div>
            </template>
          </Card>
        </div>

        <div class="col-12 md:col-3">
          <Card class="text-center h-full">
            <template #content>
              <div class="text-blue-500">
                <i class="pi pi-euro text-4xl mb-3"></i>
                <div class="text-2xl font-bold">{{ totalDepense }}€</div>
                <div class="text-600">Total dépensé</div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Quick Actions -->
        <div class="col-12 lg:col-6">
          <Card>
            <template #header>
              <div class="px-4 pt-4">
                <h3 class="text-xl font-semibold ecodeli-title m-0">Actions rapides</h3>
              </div>
            </template>
            <template #content>
              <div class="grid gap-3">
                <div class="col-12">
                  <Button
                    label="Nouvelle demande de livraison"
                    icon="pi pi-plus"
                    class="w-full p-button-outlined"
                    @click="newDeliveryRequest"
                  />
                </div>
                <div class="col-12">
                  <Button
                    label="Demander un service"
                    icon="pi pi-wrench"
                    class="w-full p-button-outlined"
                    @click="newServiceRequest"
                  />
                </div>
                <div class="col-12">
                  <Button
                    label="Voir mes commandes"
                    icon="pi pi-list"
                    class="w-full p-button-outlined"
                    @click="viewOrders"
                  />
                </div>
                <div class="col-12">
                  <Button
                    label="Mes demandes de services"
                    icon="pi pi-clipboard-list"
                    class="w-full p-button-outlined"
                    @click="viewServicesRequests"
                  />
                </div>
                <div class="col-12">
                  <Button
                    label="Mes Services & Validations"
                    icon="pi pi-cog"
                    class="w-full p-button-outlined"
                    severity="info"
                    @click="router.push('/client/services')"
                  />
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Mes Annonces -->
        <div class="col-12 lg:col-6">
          <Card>
            <template #header>
              <div class="px-4 pt-4 flex justify-content-between align-items-center">
                <h3 class="text-xl font-semibold ecodeli-title m-0">Mes Annonces</h3>
                <Button
                  icon="pi pi-refresh"
                  class="p-button-outlined p-button-sm"
                  @click="loadUserAnnonces"
                  :loading="loadingAnnonces"
                />
              </div>
            </template>
            <template #content>
              <div v-if="loadingAnnonces" class="text-center p-4">
                <ProgressSpinner style="width: 30px; height: 30px" />
                <p class="mt-2">Chargement des annonces...</p>
              </div>

              <div v-else-if="userAnnonces.length === 0" class="text-center p-4">
                <i class="pi pi-inbox text-4xl text-400 mb-3"></i>
                <p class="text-600">Aucune annonce pour le moment</p>
                <Button
                  label="Créer ma première annonce"
                  icon="pi pi-plus"
                  class="p-button-sm"
                  @click="newDeliveryRequest"
                />
              </div>

              <div v-else class="flex flex-column gap-3">
                <div
                  v-for="annonce in userAnnonces.slice(0, 3)"
                  :key="annonce.idAnnonce"
                  class="flex align-items-center gap-3 p-3 border-round border-1 surface-border"
                >
                  <div>
                    <Tag
                      :value="getStatutLabel(annonce.statut)"
                      :severity="getStatutSeverity(annonce.statut)"
                    />
                  </div>
                  <div class="flex-1">
                    <div class="font-medium">{{ annonce.titre }}</div>
                    <div class="text-sm text-600">
                      {{ annonce.adresseDepart }} → {{ annonce.adresseFin }}
                    </div>
                    <div class="text-sm text-primary font-bold">{{ annonce.prixUnitaire }}€</div>
                  </div>
                  <div class="flex gap-2">
                    <Button
                      v-if="annonce.statut === 'PUBLIEE'"
                      icon="pi pi-times"
                      class="p-button-sm p-button-outlined p-button-danger"
                      @click="handleCancelAnnonce(annonce)"
                      v-tooltip="'Annuler'"
                    />
                  </div>
                </div>

                <div v-if="userAnnonces.length > 3" class="text-center">
                  <Button
                    label="Voir toutes mes annonces"
                    icon="pi pi-list"
                    class="p-button-outlined p-button-sm"
                    @click="viewOrders"
                  />
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Candidatures reçues -->
        <div class="col-12">
          <Card>
            <template #header>
              <div class="px-4 pt-4 flex justify-content-between align-items-center">
                <h3 class="text-xl font-semibold ecodeli-title m-0">Candidatures reçues</h3>
                <Button
                  icon="pi pi-refresh"
                  class="p-button-outlined p-button-sm"
                  @click="loadCandidatures"
                  :loading="loadingCandidatures"
                />
              </div>
            </template>
            <template #content>
              <div v-if="loadingCandidatures" class="text-center p-4">
                <ProgressSpinner style="width: 30px; height: 30px" />
                <p class="mt-2">Chargement des candidatures...</p>
              </div>

              <div v-else-if="!hasCandidatures" class="text-center p-4">
                <i class="pi pi-users text-4xl text-400 mb-3"></i>
                <p class="text-600">Aucune candidature reçue</p>
              </div>

              <div v-else class="grid gap-4">
                <div
                  v-for="annonce in annoncesAvecCandidatures"
                  :key="annonce.idAnnonce"
                  class="col-12"
                >
                  <div class="surface-100 border-round p-4">
                    <div class="flex justify-content-between align-items-start mb-3">
                      <div>
                        <h4 class="font-semibold text-900 m-0">{{ annonce.titre }}</h4>
                        <p class="text-600 mt-1 mb-0">{{ annonce.adresseDepart }} → {{ annonce.adresseFin }}</p>
                      </div>
                      <Tag
                        :value="`${getCandidaturesForAnnonce(annonce.idAnnonce).length} candidature(s)`"
                        severity="info"
                      />
                    </div>

                    <div class="grid gap-3">
                      <div
                        v-for="candidature in getCandidaturesForAnnonce(annonce.idAnnonce)"
                        :key="candidature.idCandidatureLivraison"
                        class="col-12 md:col-6"
                      >
                        <div class="border-1 surface-border border-round p-3">
                          <div class="flex justify-content-between align-items-start mb-2">
                            <div class="flex align-items-center gap-2">
                              <Avatar
                                :label="getLivreurInitials(candidature.livreur)"
                                class="bg-primary text-white"
                                shape="circle"
                                size="small"
                              />
                              <div>
                                <div class="font-medium text-900">
                                  {{ candidature.livreur.prenom }} {{ candidature.livreur.nom }}
                                </div>
                                <div class="text-sm text-600">
                                  {{ formatDate(candidature.dateCandidature) }}
                                </div>
                              </div>
                            </div>
                            <Tag
                              :value="getCandidatureStatutLabel(candidature.statut)"
                              :severity="getCandidatureStatutSeverity(candidature.statut)"
                            />
                          </div>

                          <div v-if="candidature.messageLivreur" class="mb-3">
                            <div class="text-sm text-600 mb-1">Message:</div>
                            <p class="text-900 m-0 text-sm">{{ candidature.messageLivreur }}</p>
                          </div>

                          <div
                            v-if="candidature.statut === 'EN_ATTENTE'"
                            class="flex gap-2"
                          >
                            <Button
                              label="Accepter"
                              icon="pi pi-check"
                              severity="success"
                              size="small"
                              @click="handleAcceptCandidature(candidature, annonce)"
                            />
                            <Button
                              label="Refuser"
                              icon="pi pi-times"
                              severity="danger"
                              size="small"
                              outlined
                              @click="handleRefuseCandidature(candidature)"
                            />
                          </div>

                          <div v-else-if="candidature.statut === 'ACCEPTEE'" class="text-green-600 text-sm font-medium">
                            <i class="pi pi-check-circle mr-2"></i>
                            Candidature acceptée
                          </div>

                          <div v-else-if="candidature.statut === 'REFUSEE'" class="text-red-600 text-sm font-medium">
                            <i class="pi pi-times-circle mr-2"></i>
                            Candidature refusée
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </main>

    <!-- Toast for notifications -->
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
  .md\:col-3 { grid-column: span 3; }
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
