<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePrestataireStore } from '@/stores/prestataire'

const router = useRouter()
const authStore = useAuthStore()
const prestataireStore = usePrestataireStore()

const loading = ref(false)
const candidatures = ref([])
const filtreStatut = ref('TOUTES')

const menuItems = [
  {
    label: 'Dashboard',
    icon: 'pi pi-home',
    command: () => router.push('/prestataire')
  },
  {
    label: 'Demandes disponibles',
    icon: 'pi pi-inbox',
    command: () => router.push('/prestataire/demandes')
  },
  {
    label: 'Mes candidatures',
    icon: 'pi pi-send',
    command: () => router.push('/prestataire/candidatures')
  },
  {
    label: 'Mes missions',
    icon: 'pi pi-briefcase',
    command: () => router.push('/prestataire/missions')
  },
  {
    label: 'Mes évaluations',
    icon: 'pi pi-star',
    command: () => router.push('/prestataire/evaluations')
  },
  {
    label: 'Mon Portefeuille',
    icon: 'pi pi-wallet',
    command: () => router.push('/prestataire/portefeuille')
  },
  {
    label: 'Mon Profil',
    icon: 'pi pi-user',
    command: () => router.push('/prestataire/profil')
  }
]

const statutOptions = [
  { label: 'Toutes', value: 'TOUTES', icon: 'pi pi-list', severity: 'secondary' },
  { label: 'En attente', value: 'EN_ATTENTE', icon: 'pi pi-clock', severity: 'warning' },
  { label: 'Acceptées', value: 'ACCEPTEE', icon: 'pi pi-check', severity: 'success' },
  { label: 'Refusées', value: 'REFUSEE', icon: 'pi pi-times', severity: 'danger' }
]

const userInitials = computed(() => {
  if (!authStore.user) return '?'
  const prenom = authStore.user.prenom || ''
  const nom = authStore.user.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
})

const candidaturesFiltrees = computed(() => {
  if (filtreStatut.value === 'TOUTES') {
    return candidatures.value
  }
  return candidatures.value.filter(candidature => candidature.statut === filtreStatut.value)
})

const statsParStatut = computed(() => {
  const stats = {
    total: candidatures.value.length,
    EN_ATTENTE: 0,
    ACCEPTEE: 0,
    REFUSEE: 0
  }
  
  candidatures.value.forEach(candidature => {
    if (stats[candidature.statut] !== undefined) {
      stats[candidature.statut]++
    }
  })
  
  return stats
})

const getCategorieLabel = (categorie) => {
  const labels = {
    'TRANSPORT_LIVRAISON': 'Transport & Livraison',
    'SERVICES_DOMICILE': 'Services à Domicile',
    'TRAVAUX_REPARATIONS': 'Travaux & Réparations',
    'COURSES_ACHATS': 'Courses & Achats',
    'SERVICES_PERSONNELS': 'Services Personnels',
    'EDUCATION_FORMATION': 'Éducation & Formation'
  }
  return labels[categorie] || categorie
}

const getStatutSeverity = (statut) => {
  switch (statut) {
    case 'ACCEPTEE': return 'success'
    case 'REFUSEE': return 'danger'
    case 'EN_ATTENTE': return 'warning'
    default: return 'secondary'
  }
}

const getStatutLabel = (statut) => {
  switch (statut) {
    case 'ACCEPTEE': return 'Acceptée'
    case 'REFUSEE': return 'Refusée'
    case 'EN_ATTENTE': return 'En attente'
    default: return 'Inconnu'
  }
}

const getStatutIcon = (statut) => {
  switch (statut) {
    case 'ACCEPTEE': return 'pi pi-check'
    case 'REFUSEE': return 'pi pi-times'
    case 'EN_ATTENTE': return 'pi pi-clock'
    default: return 'pi pi-question'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'Date non spécifiée'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    })
  } catch {
    return 'Date invalide'
  }
}

const formatDateTime = (dateString) => {
  if (!dateString) return 'Date non spécifiée'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return 'Date invalide'
  }
}

const calculerGain = (candidature) => {
  if (!candidature.prixPropose) return 'Non défini'
  return `${candidature.prixPropose}€`
}

const loadCandidatures = async () => {
  loading.value = true
  
  try {
    const result = await prestataireStore.getMesCandidatures()
    if (result.success) {
      candidatures.value = result.data
    }
  } catch (error) {
    console.error('Erreur lors du chargement des candidatures:', error)
  } finally {
    loading.value = false
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadCandidatures()
})
</script>

<template>
  <div class="ecodeli-layout">
    <header class="ecodeli-header">
      <Menubar :model="menuItems" class="border-0">
        <template #start>
          <div class="flex align-items-center">
            <i class="pi pi-wrench text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
            <span class="text-xl font-semibold ecodeli-title">EcoDeli - Mes Candidatures</span>
          </div>
        </template>

        <template #end>
          <div class="flex align-items-center gap-2">
            <span class="text-sm text-600">{{ authStore.userName }}</span>
            <Avatar :label="userInitials" class="bg-primary text-white" shape="circle" />
            <Button icon="pi pi-sign-out" text rounded @click="handleLogout" />
          </div>
        </template>
      </Menubar>
    </header>

    <main class="ecodeli-content">
      <div class="grid">
        
        <!-- Statistiques -->
        <div class="col-12">
          <div class="grid">
            <div class="col-12 md:col-3">
              <Card class="text-center">
                <template #content>
                  <div class="text-2xl font-bold text-blue-500">{{ statsParStatut.total }}</div>
                  <div class="text-600">Total candidatures</div>
                </template>
              </Card>
            </div>
            <div class="col-12 md:col-3">
              <Card class="text-center">
                <template #content>
                  <div class="text-2xl font-bold text-orange-500">{{ statsParStatut.EN_ATTENTE }}</div>
                  <div class="text-600">En attente</div>
                </template>
              </Card>
            </div>
            <div class="col-12 md:col-3">
              <Card class="text-center">
                <template #content>
                  <div class="text-2xl font-bold text-green-500">{{ statsParStatut.ACCEPTEE }}</div>
                  <div class="text-600">Acceptées</div>
                </template>
              </Card>
            </div>
            <div class="col-12 md:col-3">
              <Card class="text-center">
                <template #content>
                  <div class="text-2xl font-bold text-red-500">{{ statsParStatut.REFUSEE }}</div>
                  <div class="text-600">Refusées</div>
                </template>
              </Card>
            </div>
          </div>
        </div>

        <!-- Filtres par statut -->
        <div class="col-12">
          <Card class="mb-4">
            <template #title>
              <div class="flex align-items-center">
                <i class="pi pi-filter mr-2"></i>
                Filtrer par statut
              </div>
            </template>
            <template #content>
              <div class="flex gap-2 flex-wrap">
                <Button
                  v-for="option in statutOptions"
                  :key="option.value"
                  :label="option.label"
                  :icon="option.icon"
                  :severity="filtreStatut === option.value ? 'primary' : option.severity"
                  :outlined="filtreStatut !== option.value"
                  @click="filtreStatut = option.value"
                />
              </div>
            </template>
          </Card>
        </div>

        <!-- Liste des candidatures -->
        <div class="col-12">
          <Card>
            <template #title>
              <div class="flex justify-content-between align-items-center">
                <div class="flex align-items-center">
                  <i class="pi pi-send mr-2"></i>
                  Mes Candidatures ({{ candidaturesFiltrees.length }})
                </div>
              </div>
            </template>
            <template #content>
              <div v-if="loading" class="text-center p-4">
                <ProgressSpinner />
                <p class="mt-2">Chargement des candidatures...</p>
              </div>
              
              <div v-else-if="candidaturesFiltrees.length === 0" class="text-center p-4 text-600">
                <i class="pi pi-inbox text-4xl mb-3"></i>
                <p v-if="filtreStatut === 'TOUTES'">Aucune candidature envoyée</p>
                <p v-else>Aucune candidature {{ getStatutLabel(filtreStatut).toLowerCase() }}</p>
                <small>
                  <span v-if="filtreStatut === 'TOUTES'">Consultez les demandes disponibles pour commencer</span>
                  <span v-else>Modifiez le filtre pour voir d'autres candidatures</span>
                </small>
              </div>
              
              <div v-else class="space-y-3">
                <div
                  v-for="candidature in candidaturesFiltrees"
                  :key="candidature.idCandidature"
                  class="p-4 border-round surface-100"
                >
                  <div class="flex justify-content-between align-items-start mb-3">
                    <div class="flex-1">
                      <div class="flex align-items-center justify-content-between mb-2">
                        <h4 class="m-0 font-semibold text-primary">
                          {{ candidature.demandeService?.titre || 'Titre non disponible' }}
                        </h4>
                        <Tag 
                          :value="getStatutLabel(candidature.statut)"
                          :severity="getStatutSeverity(candidature.statut)"
                          :icon="getStatutIcon(candidature.statut)"
                        />
                      </div>
                      
                      <div class="grid mb-3">
                        <div class="col-12 md:col-6">
                          <div class="flex align-items-center mb-2 text-sm">
                            <i class="pi pi-user mr-2 text-blue-500"></i>
                            <span>
                              {{ candidature.demandeService?.client?.prenom }} 
                              {{ candidature.demandeService?.client?.nom }}
                            </span>
                          </div>
                          <div class="flex align-items-center mb-2 text-sm">
                            <i class="pi pi-calendar mr-2 text-green-500"></i>
                            <span>Candidaté le {{ formatDateTime(candidature.dateCreation) }}</span>
                          </div>
                        </div>
                        <div class="col-12 md:col-6">
                          <div class="flex align-items-center mb-2 text-sm">
                            <i class="pi pi-euro mr-2 text-orange-500"></i>
                            <span>Tarif proposé : {{ calculerGain(candidature) }}</span>
                          </div>
                          <div v-if="candidature.delaiPropose" class="flex align-items-center mb-2 text-sm">
                            <i class="pi pi-clock mr-2 text-purple-500"></i>
                            <span>Délai proposé : {{ candidature.delaiPropose }} jours</span>
                          </div>
                        </div>
                      </div>

                      <!-- Message de candidature -->
                      <div v-if="candidature.messagePrestataire" class="mb-3">
                        <strong class="text-sm">Votre message :</strong>
                        <div class="p-3 mt-1 surface-50 border-round text-sm">
                          <pre class="m-0 line-height-3 text-wrap">{{ candidature.messagePrestataire }}</pre>
                        </div>
                      </div>

                      <!-- Actions selon le statut -->
                      <div class="flex justify-content-between align-items-center">
                        <div class="text-sm text-500">
                          <div v-if="candidature.statut === 'ACCEPTEE'" class="text-green-600">
                            <i class="pi pi-check-circle mr-1"></i>
                            Félicitations ! Votre candidature a été acceptée
                          </div>
                          <div v-else-if="candidature.statut === 'REFUSEE'" class="text-red-600">
                            <i class="pi pi-times-circle mr-1"></i>
                            Candidature non retenue cette fois
                          </div>
                          <div v-else class="text-orange-600">
                            <i class="pi pi-clock mr-1"></i>
                            En attente de réponse du client
                          </div>
                        </div>
                        
                        <div class="flex gap-2">
                          <Button 
                            icon="pi pi-eye" 
                            severity="secondary"
                            size="small"
                            text
                            v-tooltip="'Voir détails'"
                          />
                          <Button 
                            v-if="candidature.statut === 'ACCEPTEE'"
                            icon="pi pi-comment" 
                            severity="primary"
                            size="small"
                            text
                            v-tooltip="'Contacter le client'"
                          />
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

    <Toast />
  </div>
</template>

<style scoped>
.grid { display: grid; grid-template-columns: repeat(12, 1fr); gap: 1rem; }
.col-12 { grid-column: span 12; }
.col-6 { grid-column: span 6; }
.col-3 { grid-column: span 3; }
.bg-primary { background: var(--ecodeli-green) !important; }
.space-y-3 > * + * { margin-top: 0.75rem; }

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
  .md\:col-3 { grid-column: span 3; }
}

.flex-1 { flex: 1; }
.text-wrap { white-space: pre-wrap; }
</style>