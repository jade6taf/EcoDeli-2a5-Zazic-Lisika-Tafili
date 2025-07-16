<template>
  <div class="candidatures-recues">
    <!-- Header avec navigation -->
    <Card class="mb-4">
      <template #content>
        <div class="flex justify-content-between align-items-center">
          <div>
            <h1 class="text-3xl font-bold ecodeli-title m-0">Candidatures reçues</h1>
            <p v-if="demande" class="text-600 mt-2 mb-0">
              Pour la demande : <strong>{{ demande.titre }}</strong>
            </p>
          </div>
          <div class="flex gap-2">
            <Button 
              icon="pi pi-arrow-left" 
              label="Retour" 
              outlined 
              @click="retourListeServices"
            />
            <Button 
              v-if="demande && demande.statut === 'PUBLIEE'"
              icon="pi pi-refresh" 
              label="Actualiser" 
              outlined
              @click="chargerCandidatures"
              :loading="loading"
            />
          </div>
        </div>
        
        <!-- Info demande -->
        <div v-if="demande" class="mt-3 p-3 surface-100 border-round">
          <div class="grid">
            <div class="col-12 md:col-6">
              <div class="flex align-items-center mb-2 text-sm">
                <i class="pi pi-tag mr-2 text-blue-500"></i>
                <span>{{ getCategorieLabel(demande.categorieService) }}</span>
              </div>
              <div class="flex align-items-center mb-2 text-sm">
                <i class="pi pi-calendar mr-2 text-green-500"></i>
                <span>{{ formatDate(demande.dateSouhaitee) }}</span>
              </div>
            </div>
            <div class="col-12 md:col-6">
              <div class="flex align-items-center mb-2 text-sm">
                <i class="pi pi-map-marker mr-2 text-red-500"></i>
                <span>{{ demande.adresseDepart }}</span>
              </div>
              <div class="flex align-items-center mb-2 text-sm">
                <Tag 
                  :value="getStatutLabel(demande.statut)"
                  :severity="getStatutSeverity(demande.statut)"
                />
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>

    <!-- Statistiques -->
    <div class="grid mb-4">
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-blue-500">{{ statistiques.total || 0 }}</div>
            <div class="text-600">Total candidatures</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-orange-500">{{ statistiques.enAttente || 0 }}</div>
            <div class="text-600">En attente</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-green-500">{{ statistiques.acceptees || 0 }}</div>
            <div class="text-600">Acceptées</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-red-500">{{ statistiques.refusees || 0 }}</div>
            <div class="text-600">Refusées</div>
          </template>
        </Card>
      </div>
    </div>

    <!-- Message si demande déjà traitée -->
    <Card v-if="demande && demande.statut === 'PRESTATAIRE_SELECTIONNE'" class="mb-4" style="background: #f0f9ff;">
      <template #content>
        <div class="flex align-items-center">
          <i class="pi pi-check-circle text-green-500 text-2xl mr-3"></i>
          <div>
            <h4 class="text-lg font-medium mb-1">Prestataire sélectionné</h4>
            <p class="text-600 mb-0">Vous avez déjà sélectionné un prestataire pour cette demande.</p>
          </div>
        </div>
      </template>
    </Card>

    <!-- Liste des candidatures -->
    <Card>
      <template #content>
        <div v-if="loading && candidatures.length === 0" class="text-center p-4">
          <ProgressSpinner />
          <p class="mt-2">Chargement des candidatures...</p>
        </div>
        
        <div v-else-if="candidatures.length === 0" class="text-center p-4 text-600">
          <i class="pi pi-users text-4xl mb-3"></i>
          <p v-if="demande && demande.statut === 'PUBLIEE'">Aucune candidature reçue pour le moment</p>
          <p v-else>Aucune candidature disponible</p>
        </div>
        
        <div v-else class="space-y-3">
          <div
            v-for="candidature in candidatures"
            :key="candidature.idCandidature"
            class="candidature-card p-4 border-round surface-100"
          >
            <div class="flex justify-content-between align-items-start mb-3">
              <div class="flex align-items-center gap-3">
                <Avatar 
                  :label="getInitials(candidature.prestataire)"
                  class="bg-primary text-white"
                  shape="circle"
                  size="large"
                />
                <div>
                  <h4 class="font-semibold text-900 m-0">
                    {{ candidature.prestataire.prenom }} {{ candidature.prestataire.nom }}
                  </h4>
                  <div class="flex align-items-center gap-2 mt-1">
                    <Rating 
                      :modelValue="candidature.prestataire.statistiques.noteMoyenne || 4.2" 
                      readonly 
                      :stars="5"
                      class="text-sm"
                    />
                    <span class="text-sm text-600">
                      ({{ candidature.prestataire.statistiques.nombreEvaluations || 0 }} avis)
                    </span>
                  </div>
                  <div class="flex align-items-center gap-2 mt-1">
                    <Tag 
                      :value="getStatutCandidatureLabel(candidature.statut)"
                      :severity="getStatutCandidatureSeverity(candidature.statut)"
                      size="small"
                    />
                    <span class="text-sm text-600">
                      {{ formatDateCandidature(candidature.dateCandidature) }}
                    </span>
                  </div>
                </div>
              </div>
              
              <div class="text-right">
                <div class="text-2xl font-bold text-primary">{{ candidature.prixPropose }}€</div>
                <div class="text-sm text-600">Prix proposé</div>
                <div v-if="candidature.delaiPropose" class="text-sm text-600 mt-1">
                  Délai : {{ candidature.delaiPropose }} jours
                </div>
              </div>
            </div>

            <!-- Message du prestataire -->
            <div v-if="candidature.messagePrestataire" class="mb-3">
              <h5 class="text-sm font-medium text-600 mb-1">Message du prestataire :</h5>
              <p class="text-900 m-0 p-3 surface-200 border-round text-sm">
                {{ candidature.messagePrestataire }}
              </p>
            </div>

            <!-- Compétences et tarifs -->
            <div class="mb-3">
              <h5 class="text-sm font-medium text-600 mb-2">Catégories validées :</h5>
              <div class="flex gap-2 flex-wrap">
                <Tag 
                  v-for="categorie in candidature.prestataire.categoriesValidees" 
                  :key="categorie.categorieService"
                  :value="`${getCategorieLabel(categorie.categorieService)} - ${categorie.tarifHoraire}€/h`"
                  severity="success"
                  size="small"
                />
              </div>
            </div>

            <!-- Statistiques prestataire -->
            <div class="mb-3">
              <div class="grid">
                <div class="col-6 md:col-3">
                  <div class="text-center p-2 surface-200 border-round">
                    <div class="font-bold text-primary">{{ candidature.prestataire.statistiques.totalCandidatures || 0 }}</div>
                    <div class="text-xs text-600">Candidatures</div>
                  </div>
                </div>
                <div class="col-6 md:col-3">
                  <div class="text-center p-2 surface-200 border-round">
                    <div class="font-bold text-green-500">{{ candidature.prestataire.statistiques.candidaturesAcceptees || 0 }}</div>
                    <div class="text-xs text-600">Acceptées</div>
                  </div>
                </div>
                <div class="col-6 md:col-3">
                  <div class="text-center p-2 surface-200 border-round">
                    <div class="font-bold text-orange-500">{{ Math.round(candidature.prestataire.statistiques.tauxAcceptation || 0) }}%</div>
                    <div class="text-xs text-600">Taux succès</div>
                  </div>
                </div>
                <div class="col-6 md:col-3">
                  <div class="text-center p-2 surface-200 border-round">
                    <div class="font-bold text-blue-500">{{ candidature.prestataire.statistiques.missionsRealisees || 0 }}</div>
                    <div class="text-xs text-600">Missions</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex justify-content-between align-items-center">
              <div class="text-sm text-500">
                Candidature #{{ candidature.idCandidature }}
              </div>
              <div class="flex gap-2">
                <Button 
                  icon="pi pi-eye" 
                  label="Voir profil"
                  size="small"
                  text
                  @click="voirProfilPrestataire(candidature.prestataire)"
                  v-tooltip="'Voir le profil détaillé'"
                />
                <Button 
                  v-if="candidature.statut === 'EN_ATTENTE' && demande && demande.statut === 'PUBLIEE'"
                  icon="pi pi-check" 
                  label="Accepter"
                  severity="success"
                  size="small"
                  @click="accepterCandidature(candidature)"
                  :loading="loadingAction === candidature.idCandidature"
                />
                <Button 
                  v-if="candidature.statut === 'EN_ATTENTE' && demande && demande.statut === 'PUBLIEE'"
                  icon="pi pi-times" 
                  label="Refuser"
                  severity="danger"
                  size="small"
                  outlined
                  @click="refuserCandidature(candidature)"
                  :loading="loadingAction === candidature.idCandidature"
                />
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>

    <!-- Modal profil prestataire (sera créé dans Jour 3) -->
    <Dialog 
      v-model:visible="showProfilModal" 
      modal 
      header="Profil Prestataire"
      :style="{ width: '80vw', maxWidth: '800px' }"
    >
      <div v-if="prestataireSelectionne" class="profil-prestataire">
        <div class="text-center mb-4">
          <Avatar 
            :label="getInitials(prestataireSelectionne)"
            class="bg-primary text-white"
            shape="circle"
            size="xlarge"
          />
          <h3 class="mt-3 mb-1">{{ prestataireSelectionne.prenom }} {{ prestataireSelectionne.nom }}</h3>
          <Rating 
            :modelValue="prestataireSelectionne.statistiques.noteMoyenne || 4.2" 
            readonly 
            class="mb-2"
          />
          <p class="text-600">{{ prestataireSelectionne.statistiques.nombreEvaluations || 0 }} évaluations</p>
        </div>
        
        <Divider />
        
        <div class="grid">
          <div class="col-12 md:col-6">
            <h4>Informations de contact</h4>
            <p><strong>Email :</strong> {{ prestataireSelectionne.email }}</p>
            <p v-if="prestataireSelectionne.telephone"><strong>Téléphone :</strong> {{ prestataireSelectionne.telephone }}</p>
            <p v-if="prestataireSelectionne.adresse"><strong>Adresse :</strong> {{ prestataireSelectionne.adresse }}</p>
          </div>
          
          <div class="col-12 md:col-6">
            <h4>Statistiques</h4>
            <div class="flex justify-content-between mb-2">
              <span>Total candidatures :</span>
              <span class="font-bold">{{ prestataireSelectionne.statistiques.totalCandidatures || 0 }}</span>
            </div>
            <div class="flex justify-content-between mb-2">
              <span>Candidatures acceptées :</span>
              <span class="font-bold text-green-500">{{ prestataireSelectionne.statistiques.candidaturesAcceptees || 0 }}</span>
            </div>
            <div class="flex justify-content-between mb-2">
              <span>Taux de succès :</span>
              <span class="font-bold text-orange-500">{{ Math.round(prestataireSelectionne.statistiques.tauxAcceptation || 0) }}%</span>
            </div>
          </div>
        </div>
        
        <Divider />
        
        <h4>Catégories validées et tarifs</h4>
        <div class="grid">
          <div 
            v-for="categorie in prestataireSelectionne.categoriesValidees" 
            :key="categorie.categorieService"
            class="col-12 md:col-6 mb-3"
          >
            <Card>
              <template #content>
                <div class="text-center">
                  <h5 class="mb-2">{{ getCategorieLabel(categorie.categorieService) }}</h5>
                  <div class="text-2xl font-bold text-primary">{{ categorie.tarifHoraire }}€/h</div>
                  <Tag value="Validé" severity="success" size="small" />
                </div>
              </template>
            </Card>
          </div>
        </div>
      </div>
    </Dialog>

    <Toast />
    <ConfirmDialog />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCandidaturesStore } from '@/stores/candidatures'
import { useServicesStore } from '@/stores/services'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'

const route = useRoute()
const router = useRouter()
const candidaturesStore = useCandidaturesStore()
const servicesStore = useServicesStore()
const toast = useToast()
const confirm = useConfirm()

const candidatures = ref([])
const demande = ref(null)
const statistiques = ref({})
const loading = ref(false)
const loadingAction = ref(null)
const showProfilModal = ref(false)
const prestataireSelectionne = ref(null)

const demandeId = computed(() => {
  return parseInt(route.params.id)
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

const getStatutLabel = (statut) => {
  const labels = {
    'PUBLIEE': 'Publiée',
    'PRESTATAIRE_SELECTIONNE': 'Prestataire sélectionné',
    'EN_COURS': 'En cours',
    'TERMINEE': 'Terminée',
    'ANNULEE': 'Annulée'
  }
  return labels[statut] || statut
}

const getStatutSeverity = (statut) => {
  const severities = {
    'PUBLIEE': 'info',
    'PRESTATAIRE_SELECTIONNE': 'warning',
    'EN_COURS': 'warning',
    'TERMINEE': 'success',
    'ANNULEE': 'danger'
  }
  return severities[statut] || 'secondary'
}

const getStatutCandidatureLabel = (statut) => {
  const labels = {
    'EN_ATTENTE': 'En attente',
    'ACCEPTEE': 'Acceptée',
    'REFUSEE': 'Refusée'
  }
  return labels[statut] || statut
}

const getStatutCandidatureSeverity = (statut) => {
  const severities = {
    'EN_ATTENTE': 'warning',
    'ACCEPTEE': 'success',
    'REFUSEE': 'danger'
  }
  return severities[statut] || 'secondary'
}

const getInitials = (prestataire) => {
  if (!prestataire) return '?'
  const prenom = prestataire.prenom || ''
  const nom = prestataire.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
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

const formatDateCandidature = (dateString) => {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR') + ' à ' + date.toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return dateString
  }
}

const chargerCandidatures = async () => {
  loading.value = true
  
  try {
    const result = await candidaturesStore.getCandidaturesByDemandeService(demandeId.value)
    
    if (result.success) {
      candidatures.value = result.candidatures || []
      statistiques.value = result.statistiques || {}
      console.log('Candidatures chargées:', candidatures.value.length)
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.error || 'Impossible de charger les candidatures',
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur chargement candidatures:', error)
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

const chargerDemande = async () => {
  try {
    const result = await servicesStore.getDemandeServiceById?.(demandeId.value)
    
    if (result?.success) {
      demande.value = result.data
    }
  } catch (error) {
    console.error('Erreur chargement demande:', error)
  }
}

const accepterCandidature = (candidature) => {
  confirm.require({
    message: `Voulez-vous accepter la candidature de ${candidature.prestataire.prenom} ${candidature.prestataire.nom} pour ${candidature.prixPropose}€ ?`,
    header: 'Accepter la candidature',
    icon: 'pi pi-question-circle',
    acceptClass: 'p-button-success',
    acceptLabel: 'Oui, accepter',
    rejectLabel: 'Annuler',
    accept: async () => {
      await executerAcceptation(candidature)
    }
  })
}

const executerAcceptation = async (candidature) => {
  loadingAction.value = candidature.idCandidature
  
  try {
    const result = await candidaturesStore.accepterCandidatureService(candidature.idCandidature, '')
    
    if (result.success) {
      toast.add({
        severity: 'success',
        summary: 'Candidature acceptée !',
        detail: `${result.prestataireNom} va réaliser votre service`,
        life: 4000
      })
      
      await chargerCandidatures()
      await chargerDemande()
      
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.error || 'Erreur lors de l\'acceptation',
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur acceptation:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Erreur lors de l\'acceptation',
      life: 3000
    })
  } finally {
    loadingAction.value = null
  }
}

const refuserCandidature = (candidature) => {
  confirm.require({
    message: `Voulez-vous refuser la candidature de ${candidature.prestataire.prenom} ${candidature.prestataire.nom} ?`,
    header: 'Refuser la candidature',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    acceptLabel: 'Oui, refuser',
    rejectLabel: 'Annuler',
    accept: async () => {
      await executerRefus(candidature)
    }
  })
}

const executerRefus = async (candidature) => {
  loadingAction.value = candidature.idCandidature
  
  try {
    const result = await candidaturesStore.refuserCandidatureService(candidature.idCandidature, '')
    
    if (result.success) {
      toast.add({
        severity: 'info',
        summary: 'Candidature refusée',
        detail: `La candidature de ${candidature.prestataire.prenom} ${candidature.prestataire.nom} a été refusée`,
        life: 3000
      })
      
      await chargerCandidatures()
      
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.error || 'Erreur lors du refus',
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur refus:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Erreur lors du refus',
      life: 3000
    })
  } finally {
    loadingAction.value = null
  }
}

const voirProfilPrestataire = (prestataire) => {
  prestataireSelectionne.value = prestataire
  showProfilModal.value = true
}

const retourListeServices = () => {
  router.push('/client/demandes-services')
}

onMounted(async () => {
  await chargerDemande()
  await chargerCandidatures()
})
</script>

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

.candidature-card {
  transition: all 0.3s ease;
}

.candidature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.bg-primary {
  background: var(--ecodeli-green) !important;
}

.text-primary {
  color: var(--ecodeli-green) !important;
}
</style>