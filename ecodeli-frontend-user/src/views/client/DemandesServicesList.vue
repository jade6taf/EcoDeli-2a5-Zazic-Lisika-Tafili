<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useServicesStore } from '@/stores/services'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const authStore = useAuthStore()
const servicesStore = useServicesStore()
const toast = useToast()

const demandesServices = ref([])
const loading = ref(false)
const filtreStatut = ref('TOUTES')
const searchTerm = ref('')

const statutOptions = [
  { label: 'Toutes', value: 'TOUTES', severity: 'secondary' },
  { label: 'Publiées', value: 'PUBLIEE', severity: 'info' },
  { label: 'En cours', value: 'EN_COURS', severity: 'warning' },
  { label: 'Terminées', value: 'TERMINEE', severity: 'success' },
  { label: 'Annulées', value: 'ANNULEE', severity: 'danger' }
]

const demandesFiltrees = computed(() => {
  let filtered = demandesServices.value

  if (filtreStatut.value !== 'TOUTES') {
    filtered = filtered.filter(demande => demande.statut === filtreStatut.value)
  }

  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase()
    filtered = filtered.filter(demande => 
      demande.titre.toLowerCase().includes(term) ||
      demande.description.toLowerCase().includes(term)
    )
  }

  return filtered.sort((a, b) => new Date(b.dateCreation) - new Date(a.dateCreation))
})

const statistiques = computed(() => {
  const stats = {
    total: demandesServices.value.length,
    PUBLIEE: 0,
    EN_COURS: 0,
    TERMINEE: 0,
    ANNULEE: 0
  }
  
  demandesServices.value.forEach(demande => {
    if (stats[demande.statut] !== undefined) {
      stats[demande.statut]++
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
    case 'PUBLIEE': return 'info'
    case 'EN_COURS': return 'warning'
    case 'TERMINEE': return 'success'
    case 'ANNULEE': return 'danger'
    default: return 'secondary'
  }
}

const getStatutLabel = (statut) => {
  switch (statut) {
    case 'PUBLIEE': return 'Publiée'
    case 'EN_COURS': return 'En cours'
    case 'TERMINEE': return 'Terminée'
    case 'ANNULEE': return 'Annulée'
    default: return statut
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

const formatDateCourt = (dateString) => {
  if (!dateString) return 'N/A'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR')
  } catch {
    return 'N/A'
  }
}

const peutEtreModifiee = (demande) => {
  return demande.statut === 'PUBLIEE'
}

const peutEtreAnnulee = (demande) => {
  return ['PUBLIEE', 'EN_COURS'].includes(demande.statut)
}

const chargerMesDemandesServices = async () => {
  if (!authStore.user?.id) {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Utilisateur non connecté',
      life: 3000
    })
    return
  }

  loading.value = true
  
  try {
    const result = await servicesStore.chargerMesDemandesServices(authStore.user.id)
    if (result.success) {
      demandesServices.value = result.data
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.error || 'Impossible de charger les demandes',
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur chargement demandes:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Erreur lors du chargement',
      life: 3000
    })
  } finally {
    loading.value = false
  }
}

const creerNouvelleDemande = () => {
  router.push('/client/demande-service')
}

const voirDetails = (demande) => {
  console.log('Voir détails demande:', demande.idDemande)
}

const voirCandidatures = (demande) => {
  router.push(`/client/demandes-services/${demande.idDemande}/candidatures`)
}

const getNombreCandidatures = (demande) => {
  return demande.nombreCandidatures || 0
}

const modifierDemande = (demande) => {
  if (!peutEtreModifiee(demande)) {
    toast.add({
      severity: 'warn',
      summary: 'Action impossible',
      detail: 'Cette demande ne peut plus être modifiée',
      life: 3000
    })
    return
  }
  
  console.log('Modifier demande:', demande.idDemande)
}

const annulerDemande = async (demande) => {
  if (!peutEtreAnnulee(demande)) {
    toast.add({
      severity: 'warn',
      summary: 'Action impossible',
      detail: 'Cette demande ne peut pas être annulée',
      life: 3000
    })
    return
  }

  try {
    const result = await servicesStore.annulerDemandeService(demande.idDemande)
    if (result.success) {
      toast.add({
        severity: 'success',
        summary: 'Demande annulée',
        detail: 'La demande a été annulée avec succès',
        life: 3000
      })
      await chargerMesDemandesServices()
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.error || 'Impossible d\'annuler la demande',
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur annulation demande:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Erreur lors de l\'annulation',
      life: 3000
    })
  }
}

const retourDashboard = () => {
  router.push('/client')
}

onMounted(() => {
  chargerMesDemandesServices()
})
</script>

<template>
  <div class="demandes-services-list">
    <!-- Header -->
    <div class="flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="text-3xl font-bold ecodeli-title m-0">Mes demandes de services</h1>
        <p class="text-600 mt-2 mb-0">Gérez vos demandes de services publiées</p>
      </div>
      <div class="flex gap-2">
        <Button 
          icon="pi pi-arrow-left" 
          label="Retour" 
          outlined 
          @click="retourDashboard"
        />
        <Button 
          icon="pi pi-plus" 
          label="Nouvelle demande" 
          @click="creerNouvelleDemande"
        />
      </div>
    </div>

    <!-- Statistiques -->
    <div class="grid mb-4">
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-blue-500">{{ statistiques.total }}</div>
            <div class="text-600">Total</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-blue-500">{{ statistiques.PUBLIEE }}</div>
            <div class="text-600">Publiées</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-orange-500">{{ statistiques.EN_COURS }}</div>
            <div class="text-600">En cours</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-green-500">{{ statistiques.TERMINEE }}</div>
            <div class="text-600">Terminées</div>
          </template>
        </Card>
      </div>
    </div>

    <!-- Filtres -->
    <Card class="mb-4">
      <template #content>
        <div class="flex flex-column md:flex-row gap-3">
          <div class="flex-1">
            <label for="search" class="block text-sm font-medium mb-1">Rechercher</label>
            <InputText 
              id="search"
              v-model="searchTerm" 
              placeholder="Titre, description..."
              class="w-full"
            />
          </div>
          <div class="flex-none">
            <label class="block text-sm font-medium mb-1">Statut</label>
            <div class="flex gap-2 flex-wrap">
              <Button
                v-for="option in statutOptions"
                :key="option.value"
                :label="option.label"
                :severity="filtreStatut === option.value ? 'primary' : option.severity"
                :outlined="filtreStatut !== option.value"
                size="small"
                @click="filtreStatut = option.value"
              />
            </div>
          </div>
        </div>
      </template>
    </Card>

    <!-- Liste des demandes -->
    <Card>
      <template #content>
        <div v-if="loading" class="text-center p-4">
          <ProgressSpinner />
          <p class="mt-2">Chargement de vos demandes...</p>
        </div>
        
        <div v-else-if="demandesFiltrees.length === 0" class="text-center p-4 text-600">
          <i class="pi pi-inbox text-4xl mb-3"></i>
          <p v-if="demandesServices.length === 0">Aucune demande de service créée</p>
          <p v-else>Aucune demande ne correspond aux filtres</p>
          <Button 
            v-if="demandesServices.length === 0"
            label="Créer ma première demande" 
            icon="pi pi-plus"
            @click="creerNouvelleDemande"
            class="mt-3"
          />
        </div>
        
        <div v-else class="space-y-3">
          <div
            v-for="demande in demandesFiltrees"
            :key="demande.idDemande"
            class="demande-card p-4 border-round surface-100"
          >
            <div class="flex justify-content-between align-items-start mb-3">
              <div class="flex-1">
                <div class="flex align-items-center justify-content-between mb-2">
                  <h3 class="text-lg font-semibold m-0">{{ demande.titre }}</h3>
                  <Tag 
                    :value="getStatutLabel(demande.statut)"
                    :severity="getStatutSeverity(demande.statut)"
                  />
                </div>
                
                <p class="text-sm text-600 mb-3">{{ demande.description }}</p>
                
                <div class="grid">
                  <div class="col-12 md:col-6">
                    <div class="flex align-items-center mb-2 text-sm">
                      <i class="pi pi-tag mr-2 text-blue-500"></i>
                      <span>{{ getCategorieLabel(demande.categorieService) }}</span>
                    </div>
                    <div class="flex align-items-center mb-2 text-sm">
                      <i class="pi pi-calendar mr-2 text-green-500"></i>
                      <span>Souhaitée le {{ formatDate(demande.dateSouhaitee) }}</span>
                    </div>
                  </div>
                  <div class="col-12 md:col-6">
                    <div class="flex align-items-center mb-2 text-sm">
                      <i class="pi pi-map-marker mr-2 text-red-500"></i>
                      <span>{{ demande.adresseDepart }}</span>
                    </div>
                    <div class="flex align-items-center mb-2 text-sm">
                      <i class="pi pi-clock mr-2 text-purple-500"></i>
                      <span>Créée le {{ formatDateCourt(demande.dateCreation) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="flex justify-content-between align-items-center">
              <div class="text-sm text-500">
                Demande #{{ demande.idDemande }}
              </div>
              <div class="flex gap-2">
                <Button
                  icon="pi pi-eye"
                  size="small"
                  text
                  @click="voirDetails(demande)"
                  v-tooltip="'Voir détails'"
                />
                <Button
                  v-if="demande.statut === 'PUBLIEE' || demande.statut === 'PRESTATAIRE_SELECTIONNE'"
                  icon="pi pi-users"
                  size="small"
                  text
                  @click="voirCandidatures(demande)"
                  v-tooltip="'Voir candidatures'"
                />
                <Button
                  v-if="peutEtreModifiee(demande)"
                  icon="pi pi-pencil"
                  size="small"
                  text
                  @click="modifierDemande(demande)"
                  v-tooltip="'Modifier'"
                />
                <Button
                  v-if="peutEtreAnnulee(demande)"
                  icon="pi pi-times"
                  severity="danger"
                  size="small"
                  text
                  @click="annulerDemande(demande)"
                  v-tooltip="'Annuler'"
                />
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>

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

.space-y-3 > * + * {
  margin-top: 0.75rem;
}

.demande-card {
  transition: all 0.3s ease;
}

.demande-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.flex-1 { flex: 1; }
.flex-none { flex: none; }
</style>