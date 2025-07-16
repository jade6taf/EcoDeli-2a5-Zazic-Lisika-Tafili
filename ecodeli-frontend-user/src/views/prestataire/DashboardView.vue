<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePrestataireStore } from '@/stores/prestataire'

const router = useRouter()
const authStore = useAuthStore()
const prestataireStore = usePrestataireStore()

const loading = ref(false)
const statutValidation = ref(null)
const statistiques = ref(null)
const demandesDisponibles = ref([])

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

const userInitials = computed(() => {
  if (!authStore.user) return '?'
  const prenom = authStore.user.prenom || ''
  const nom = authStore.user.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
})

const statutSeverity = computed(() => {
  if (!statutValidation.value) return 'secondary'
  
  switch (statutValidation.value.statutGlobal) {
    case 'VALIDE': return 'success'
    case 'REJETE': return 'danger'
    case 'SUSPENDU': return 'secondary'
    default: return 'warning'
  }
})

const peutCandidater = computed(() => {
  return statutValidation.value?.peutCandidater || false
})

const loadData = async () => {
  loading.value = true
  
  try {
    const statutResult = await prestataireStore.getStatutValidation()
    if (statutResult.success) {
      statutValidation.value = statutResult.data
    }
    
    const statsResult = await prestataireStore.getStatistiques()
    if (statsResult.success) {
      statistiques.value = statsResult.data
    }
    
    if (peutCandidater.value) {
      const demandesResult = await prestataireStore.getDemandesDisponibles()
      if (demandesResult.success) {
        demandesDisponibles.value = demandesResult.data.slice(0, 3)
      }
    }
  } catch (error) {
    console.error('Erreur lors du chargement des données:', error)
  } finally {
    loading.value = false
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const navigateTodemandes = () => {
  router.push('/prestataire/demandes')
}

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

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="ecodeli-layout">
    <header class="ecodeli-header">
      <Menubar :model="menuItems" class="border-0">
        <template #start>
          <div class="flex align-items-center">
            <i class="pi pi-wrench text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
            <span class="text-xl font-semibold ecodeli-title">EcoDeli - Prestataire</span>
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
        <!-- Statut de validation -->
        <div class="col-12">
          <Card class="mb-4">
            <template #content>
              <div class="flex justify-content-between align-items-start">
                <div>
                  <h1 class="text-3xl font-bold ecodeli-title m-0">Dashboard Prestataire</h1>
                  <p class="text-600 mt-2">{{ authStore.user?.prenom }} {{ authStore.user?.nom }}</p>
                </div>
                <div v-if="statutValidation" class="text-right">
                  <Tag
                    :value="statutValidation.statutGlobal || 'En attente'"
                    :severity="statutSeverity"
                    class="text-lg"
                  />
                  <div v-if="statutValidation.domaineExpertise" class="text-sm text-600 mt-1">
                    Domaine : {{ getCategorieLabel(statutValidation.domaineExpertise) }}
                  </div>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Message validation -->
        <div v-if="!peutCandidater" class="col-12">
          <Message severity="warn" :closable="false" class="mb-4">
            <div class="flex align-items-center">
              <i class="pi pi-exclamation-triangle mr-2"></i>
              <div>
                <strong>Validation en attente</strong>
                <p class="m-0 mt-1">Votre profil est en cours de validation par notre équipe. Vous pourrez accéder aux demandes une fois validé.</p>
              </div>
            </div>
          </Message>
        </div>

        <!-- Demandes disponibles aperçu -->
        <div v-if="peutCandidater" class="col-12 md:col-8">
          <Card>
            <template #header>
              <div class="flex justify-content-between align-items-center p-3">
                <h3 class="m-0">Dernières demandes disponibles</h3>
                <Button
                  label="Voir toutes"
                  icon="pi pi-arrow-right"
                  class="p-button-text"
                  @click="navigateTodemandes"
                />
              </div>
            </template>
            <template #content>
              <div v-if="demandesDisponibles.length === 0" class="text-center p-4 text-600">
                <i class="pi pi-inbox text-4xl mb-3"></i>
                <p>Aucune demande disponible pour le moment</p>
              </div>
              <div v-else class="space-y-3">
                <div
                  v-for="demande in demandesDisponibles"
                  :key="demande.idDemande"
                  class="p-3 border-round surface-100 hover:surface-200 transition-colors cursor-pointer"
                  @click="navigateTodemandes"
                >
                  <div class="flex justify-content-between align-items-start">
                    <div class="flex-1">
                      <h4 class="m-0 font-semibold">{{ demande.titre }}</h4>
                      <p class="text-sm text-600 m-0 mt-1">{{ demande.description.substring(0, 100) }}...</p>
                      <div class="flex align-items-center mt-2 text-sm text-500">
                        <i class="pi pi-calendar mr-1"></i>
                        {{ formatDate(demande.dateSouhaitee) }}
                        <i class="pi pi-map-marker ml-3 mr-1"></i>
                        {{ demande.adresseDepart || 'Adresse non spécifiée' }}
                      </div>
                    </div>
                    <Tag
                      :value="getCategorieLabel(demande.categorieService)"
                      severity="info"
                      class="ml-2"
                    />
                  </div>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Actions rapides -->
        <div v-if="peutCandidater" class="col-12">
          <Card>
            <template #content>
              <h3 class="mt-0">Actions rapides</h3>
              <div class="flex gap-3 flex-wrap">
                <Button
                  label="Voir les demandes"
                  icon="pi pi-search"
                  @click="navigateTodemandes"
                />
                <Button
                  label="Mes candidatures"
                  icon="pi pi-send"
                  class="p-button-outlined"
                  @click="router.push('/prestataire/candidatures')"
                />
                <Button
                  label="Mes missions"
                  icon="pi pi-briefcase"
                  severity="success"
                  @click="router.push('/prestataire/missions')"
                />
                <Button
                  label="Mes évaluations"
                  icon="pi pi-star"
                  severity="info"
                  @click="router.push('/prestataire/evaluations')"
                />
                <Button
                  label="Mon Portefeuille"
                  icon="pi pi-wallet"
                  severity="warning"
                  @click="router.push('/prestataire/portefeuille')"
                />
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
.bg-primary { background: var(--ecodeli-green) !important; }
</style>
