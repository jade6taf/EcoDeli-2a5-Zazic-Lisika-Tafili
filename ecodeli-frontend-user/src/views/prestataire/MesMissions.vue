<template>
  <div class="mes-missions">
    <!-- Header avec navigation -->
    <Card class="mb-4">
      <template #content>
        <div class="flex justify-content-between align-items-center">
          <div>
            <h1 class="text-3xl font-bold ecodeli-title m-0">Mes Missions</h1>
            <p class="text-600 mt-2 mb-0">
              Gérez vos missions acceptées et en cours
            </p>
          </div>
          <div class="flex gap-2">
            <Button
              icon="pi pi-star"
              label="Mes évaluations"
              outlined
              severity="info"
              @click="router.push('/prestataire/evaluations')"
            />
            <Button
              icon="pi pi-wallet"
              label="Mon Portefeuille"
              outlined
              severity="warning"
              @click="router.push('/prestataire/portefeuille')"
            />
            <Button
              icon="pi pi-arrow-left"
              label="Retour"
              outlined
              @click="retourDashboard"
            />
            <Button
              icon="pi pi-refresh"
              label="Actualiser"
              outlined
              @click="chargerMissions"
              :loading="loading"
            />
          </div>
        </div>
      </template>
    </Card>

    <!-- Statistiques missions -->
    <div class="grid mb-4">
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-blue-500">{{ statistiques.total || 0 }}</div>
            <div class="text-600">Total missions</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-orange-500">{{ statistiques.enCours || 0 }}</div>
            <div class="text-600">En cours</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-green-500">{{ statistiques.terminees || 0 }}</div>
            <div class="text-600">Terminées</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-purple-500">{{ calculerGainTotal() }}€</div>
            <div class="text-600">Gains totaux</div>
          </template>
        </Card>
      </div>
    </div>

    <!-- Filtres par statut -->
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

    <!-- Liste des missions -->
    <Card>
      <template #title>
        <div class="flex justify-content-between align-items-center">
          <div class="flex align-items-center">
            <i class="pi pi-briefcase mr-2"></i>
            Mes Missions ({{ missionsFiltrees.length }})
          </div>
        </div>
      </template>
      <template #content>
        <div v-if="loading" class="text-center p-4">
          <ProgressSpinner />
          <p class="mt-2">Chargement des missions...</p>
        </div>
        
        <div v-else-if="missionsFiltrees.length === 0" class="text-center p-4 text-600">
          <i class="pi pi-briefcase text-4xl mb-3"></i>
          <p v-if="filtreStatut === 'TOUTES'">Aucune mission acceptée pour le moment</p>
          <p v-else>Aucune mission {{ getStatutLabel(filtreStatut).toLowerCase() }}</p>
          <small>
            <span v-if="filtreStatut === 'TOUTES'">Candidatez sur des demandes pour obtenir vos premières missions</span>
            <span v-else>Modifiez le filtre pour voir d'autres missions</span>
          </small>
        </div>
        
        <div v-else class="space-y-3">
          <div
            v-for="mission in missionsFiltrees"
            :key="mission.id"
            class="mission-card p-4 border-round surface-100"
          >
            <div class="flex justify-content-between align-items-start mb-3">
              <div class="flex-1">
                <div class="flex align-items-center justify-content-between mb-2">
                  <h4 class="m-0 font-semibold text-primary">
                    {{ mission.demandeService?.titre || 'Mission sans titre' }}
                  </h4>
                  <Tag 
                    :value="getStatutLabel(mission.statut)"
                    :severity="getStatutSeverity(mission.statut)"
                    :icon="getStatutIcon(mission.statut)"
                  />
                </div>
                
                <p class="text-sm text-600 mb-3">{{ mission.demandeService?.description }}</p>
                
                <div class="grid mb-3">
                  <div class="col-12 md:col-6">
                    <div class="flex align-items-center mb-2 text-sm">
                      <i class="pi pi-user mr-2 text-blue-500"></i>
                      <span>
                        {{ mission.demandeService?.client?.prenom }} 
                        {{ mission.demandeService?.client?.nom }}
                      </span>
                    </div>
                    <div class="flex align-items-center mb-2 text-sm">
                      <i class="pi pi-calendar mr-2 text-green-500"></i>
                      <span>{{ formatDate(mission.demandeService?.dateSouhaitee) }}</span>
                    </div>
                  </div>
                  <div class="col-12 md:col-6">
                    <div class="flex align-items-center mb-2 text-sm">
                      <i class="pi pi-euro mr-2 text-orange-500"></i>
                      <span>Prix accepté : {{ mission.prixAccepte || 'Non défini' }}€</span>
                    </div>
                    <div class="flex align-items-center mb-2 text-sm">
                      <i class="pi pi-map-marker mr-2 text-red-500"></i>
                      <span>{{ mission.demandeService?.adresseDepart }}</span>
                    </div>
                  </div>
                </div>

                <!-- Timeline mission -->
                <div class="mb-3">
                  <h5 class="text-sm font-medium text-600 mb-2">Progression :</h5>
                  <div class="flex align-items-center gap-2">
                    <div class="flex align-items-center">
                      <i class="pi pi-check-circle text-green-500 mr-1"></i>
                      <span class="text-xs">Acceptée</span>
                    </div>
                    <div class="border-top border-300" style="width: 30px;"></div>
                    <div class="flex align-items-center">
                      <i :class="mission.statut === 'PRESTATAIRE_SELECTIONNE' ? 'pi pi-circle text-300' : 'pi pi-check-circle text-green-500'" class="mr-1"></i>
                      <span class="text-xs" :class="mission.statut === 'PRESTATAIRE_SELECTIONNE' ? 'text-300' : ''">En cours</span>
                    </div>
                    <div class="border-top border-300" style="width: 30px;"></div>
                    <div class="flex align-items-center">
                      <i :class="mission.statut === 'TERMINEE' ? 'pi pi-check-circle text-green-500' : 'pi pi-circle text-300'" class="mr-1"></i>
                      <span class="text-xs" :class="mission.statut === 'TERMINEE' ? '' : 'text-300'">Terminée</span>
                    </div>
                  </div>
                </div>

                <!-- Actions selon le statut -->
                <div class="flex justify-content-between align-items-center">
                  <div class="text-sm text-500">
                    <div v-if="mission.dateDebut">
                      Démarrée le {{ formatDate(mission.dateDebut) }}
                    </div>
                    <div v-else-if="mission.statut === 'PRESTATAIRE_SELECTIONNE'">
                      Mission acceptée le {{ formatDate(mission.dateAcceptation) }}
                    </div>
                    <div v-if="mission.dateTerminaison">
                      Terminée le {{ formatDate(mission.dateTerminaison) }}
                    </div>
                  </div>
                  
                  <div class="flex gap-2">
                    <Button
                      icon="pi pi-eye"
                      severity="secondary"
                      size="small"
                      text
                      @click="voirDetailsMission(mission)"
                    />
                    
                    <!-- Actions selon statut -->
                    <Button 
                      v-if="mission.statut === 'PRESTATAIRE_SELECTIONNE'"
                      icon="pi pi-play" 
                      label="Démarrer"
                      severity="success"
                      size="small"
                      @click="demarrerMission(mission)"
                      :loading="loadingAction === mission.id"
                    />
                    
                    <Button 
                      v-if="mission.statut === 'EN_COURS'"
                      icon="pi pi-check" 
                      label="Terminer"
                      severity="warning"
                      size="small"
                      @click="terminerMission(mission)"
                      :loading="loadingAction === mission.id"
                    />
                    
                    <Button
                      v-if="mission.statut === 'EN_COURS'"
                      icon="pi pi-comment"
                      severity="info"
                      size="small"
                      text
                      @click="contacterClient(mission)"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>

    <!-- Dialog détails mission -->
    <Dialog 
      v-model:visible="showDetailsDialog" 
      header="Détails de la mission" 
      modal
      :style="{ width: '60rem' }"
      :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
    >
      <div v-if="missionSelectionnee" class="mission-details">
        <Card class="mb-4">
          <template #title>{{ missionSelectionnee.demandeService?.titre }}</template>
          <template #content>
            <div class="grid">
              <div class="col-12 md:col-6">
                <h5>Informations client</h5>
                <p><strong>Client :</strong> {{ missionSelectionnee.demandeService?.client?.prenom }} {{ missionSelectionnee.demandeService?.client?.nom }}</p>
                <p><strong>Email :</strong> {{ missionSelectionnee.demandeService?.client?.email }}</p>
                <p><strong>Téléphone :</strong> {{ missionSelectionnee.demandeService?.client?.telephone || 'Non renseigné' }}</p>
              </div>
              <div class="col-12 md:col-6">
                <h5>Détails mission</h5>
                <p><strong>Prix :</strong> {{ missionSelectionnee.prixAccepte }}€</p>
                <p><strong>Date souhaitée :</strong> {{ formatDate(missionSelectionnee.demandeService?.dateSouhaitee) }}</p>
                <p><strong>Créneau :</strong> {{ missionSelectionnee.demandeService?.creneauHoraire || 'Flexible' }}</p>
              </div>
            </div>
            
            <Divider />
            
            <h5>Description détaillée</h5>
            <p>{{ missionSelectionnee.demandeService?.description }}</p>
            
            <h5>Adresses</h5>
            <p><strong>Départ :</strong> {{ missionSelectionnee.demandeService?.adresseDepart }}</p>
            <p v-if="missionSelectionnee.demandeService?.adresseArrivee">
              <strong>Arrivée :</strong> {{ missionSelectionnee.demandeService?.adresseArrivee }}
            </p>
          </template>
        </Card>
      </div>
    </Dialog>

    <!-- Dialog terminer mission -->
    <Dialog 
      v-model:visible="showTerminerDialog" 
      header="Terminer la mission" 
      modal
      :style="{ width: '40rem' }"
    >
      <div v-if="missionATerminer" class="terminer-mission">
        <p class="mb-3">
          Vous êtes sur le point de marquer la mission <strong>"{{ missionATerminer.demandeService?.titre }}"</strong> comme terminée.
        </p>
        
        <div class="field mb-3">
          <label for="noteFinale" class="font-semibold mb-2 block">Message final pour le client (optionnel)</label>
          <Textarea 
            id="noteFinale"
            v-model="noteFinale"
            rows="4"
            placeholder="Décrivez brièvement le travail réalisé, donnez des conseils ou remerciez le client..."
            class="w-full"
          />
        </div>
        
        <div class="field mb-3">
          <label class="font-semibold mb-2 block">Upload photo finale (optionnel)</label>
          <input
            type="file"
            accept="image/*"
            @change="onUploadPhotoFinale"
            class="w-full p-inputtext"
          />
        </div>
      </div>
      
      <template #footer>
        <Button label="Annuler" @click="showTerminerDialog = false" class="p-button-text" />
        <Button 
          label="Marquer comme terminée" 
          icon="pi pi-check"
          @click="confirmerTerminaison"
          :loading="loadingAction === missionATerminer?.id"
        />
      </template>
    </Dialog>

    <Toast />
    <ConfirmDialog />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePrestataireStore } from '@/stores/prestataire'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'

const router = useRouter()
const authStore = useAuthStore()
const prestataireStore = usePrestataireStore()
const toast = useToast()
const confirm = useConfirm()

const missions = ref([])
const statistiques = ref({})
const loading = ref(false)
const loadingAction = ref(null)
const filtreStatut = ref('TOUTES')

const showDetailsDialog = ref(false)
const showTerminerDialog = ref(false)
const missionSelectionnee = ref(null)
const missionATerminer = ref(null)
const noteFinale = ref('')

const statutOptions = [
  { label: 'Toutes', value: 'TOUTES', icon: 'pi pi-list', severity: 'secondary' },
  { label: 'À démarrer', value: 'PRESTATAIRE_SELECTIONNE', icon: 'pi pi-play', severity: 'info' },
  { label: 'En cours', value: 'EN_COURS', icon: 'pi pi-spinner', severity: 'warning' },
  { label: 'Terminées', value: 'TERMINEE', icon: 'pi pi-check', severity: 'success' }
]

const missionsFiltrees = computed(() => {
  if (filtreStatut.value === 'TOUTES') {
    return missions.value
  }
  return missions.value.filter(mission => mission.statut === filtreStatut.value)
})

const getStatutLabel = (statut) => {
  const labels = {
    'PRESTATAIRE_SELECTIONNE': 'À démarrer',
    'EN_COURS': 'En cours',
    'TERMINEE': 'Terminée'
  }
  return labels[statut] || statut
}

const getStatutSeverity = (statut) => {
  const severities = {
    'PRESTATAIRE_SELECTIONNE': 'info',
    'EN_COURS': 'warning',
    'TERMINEE': 'success'
  }
  return severities[statut] || 'secondary'
}

const getStatutIcon = (statut) => {
  const icons = {
    'PRESTATAIRE_SELECTIONNE': 'pi pi-play',
    'EN_COURS': 'pi pi-spinner',
    'TERMINEE': 'pi pi-check'
  }
  return icons[statut] || 'pi pi-question'
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

const calculerGainTotal = () => {
  return missions.value
    .filter(m => m.statut === 'TERMINEE')
    .reduce((total, m) => total + (parseFloat(m.prixAccepte) || 0), 0)
    .toFixed(0)
}

const chargerMissions = async () => {
  loading.value = true
  
  try {
    const result = await prestataireStore.getMesMissions()
    
    if (result.success) {
      missions.value = result.data || []
      calculerStatistiques()
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.error || 'Impossible de charger les missions',
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur chargement missions:', error)
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

const calculerStatistiques = () => {
  statistiques.value = {
    total: missions.value.length,
    enCours: missions.value.filter(m => m.statut === 'EN_COURS').length,
    terminees: missions.value.filter(m => m.statut === 'TERMINEE').length
  }
}

const demarrerMission = (mission) => {
  confirm.require({
    message: `Voulez-vous démarrer la mission "${mission.demandeService?.titre}" ?`,
    header: 'Démarrer la mission',
    icon: 'pi pi-play',
    acceptClass: 'p-button-success',
    acceptLabel: 'Oui, démarrer',
    rejectLabel: 'Annuler',
    accept: async () => {
      await executerDemarrage(mission)
    }
  })
}

const executerDemarrage = async (mission) => {
  loadingAction.value = mission.id
  
  try {
    const result = await prestataireStore.demarrerMission(mission.id)
    
    if (result.success) {
      toast.add({
        severity: 'success',
        summary: 'Mission démarrée !',
        detail: 'La mission est maintenant en cours',
        life: 3000
      })
      
      await chargerMissions()
      
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.error || 'Erreur lors du démarrage',
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur démarrage mission:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Erreur lors du démarrage',
      life: 3000
    })
  } finally {
    loadingAction.value = null
  }
}

const terminerMission = (mission) => {
  missionATerminer.value = mission
  noteFinale.value = ''
  showTerminerDialog.value = true
}

const confirmerTerminaison = async () => {
  if (!missionATerminer.value) return
  
  loadingAction.value = missionATerminer.value.id
  
  try {
    const result = await prestataireStore.terminerMission(missionATerminer.value.id, {
      noteFinale: noteFinale.value
    })
    
    if (result.success) {
      toast.add({
        severity: 'success',
        summary: 'Mission terminée !',
        detail: 'Le client va recevoir une notification pour valider',
        life: 4000
      })
      
      showTerminerDialog.value = false
      await chargerMissions()
      
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.error || 'Erreur lors de la terminaison',
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur terminaison mission:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Erreur lors de la terminaison',
      life: 3000
    })
  } finally {
    loadingAction.value = null
  }
}

const voirDetailsMission = (mission) => {
  missionSelectionnee.value = mission
  showDetailsDialog.value = true
}

const contacterClient = (mission) => {
  toast.add({
    severity: 'info',
    summary: 'Fonctionnalité à venir',
    detail: 'Le système de chat sera disponible prochainement',
    life: 3000
  })
}

const onUploadPhotoFinale = (event) => {
  console.log('Upload photo finale:', event)
}

const retourDashboard = () => {
  router.push('/prestataire')
}

onMounted(() => {
  chargerMissions()
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

.mission-card {
  transition: all 0.3s ease;
}

.mission-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.flex-1 { flex: 1; }
</style>