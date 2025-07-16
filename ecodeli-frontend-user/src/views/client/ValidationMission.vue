<template>
  <div class="validation-mission">
    <!-- Header avec navigation -->
    <Card class="mb-4">
      <template #content>
        <div class="flex justify-content-between align-items-center">
          <div>
            <h1 class="text-3xl font-bold ecodeli-title m-0">Validation de Mission</h1>
            <p v-if="mission" class="text-600 mt-2 mb-0">
              Mission : <strong>{{ mission.demandeService?.titre }}</strong>
            </p>
          </div>
          <div class="flex gap-2">
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
              @click="chargerMission"
              :loading="loading"
            />
          </div>
        </div>
      </template>
    </Card>

    <!-- Message si mission pas terminée -->
    <Card v-if="mission && mission.statut !== 'TERMINEE'" class="mb-4" style="background: #fef3c7;">
      <template #content>
        <div class="flex align-items-center">
          <i class="pi pi-exclamation-triangle text-yellow-600 text-2xl mr-3"></i>
          <div>
            <h4 class="text-lg font-medium mb-1 text-yellow-800">Mission en cours</h4>
            <p class="text-yellow-700 mb-0">Cette mission n'est pas encore terminée par le prestataire.</p>
          </div>
        </div>
      </template>
    </Card>

    <!-- Mission déjà évaluée -->
    <Card v-if="evaluationExistante" class="mb-4" style="background: #f0f9ff;">
      <template #content>
        <div class="flex align-items-center">
          <i class="pi pi-check-circle text-green-500 text-2xl mr-3"></i>
          <div>
            <h4 class="text-lg font-medium mb-1">Mission déjà évaluée</h4>
            <p class="text-600 mb-2">Vous avez donné une note de {{ evaluationExistante.noteGlobale }}/5 ({{ evaluationExistante.noteGlobaleLabel }})</p>
            <Button 
              label="Voir mon évaluation" 
              icon="pi pi-eye"
              size="small"
              text
              @click="afficherEvaluationExistante = true"
            />
          </div>
        </div>
      </template>
    </Card>

    <!-- Contenu principal -->
    <div v-if="loading" class="text-center p-4">
      <ProgressSpinner />
      <p class="mt-2">Chargement des détails de la mission...</p>
    </div>

    <div v-else-if="!mission" class="text-center p-4 text-600">
      <i class="pi pi-exclamation-circle text-4xl mb-3"></i>
      <p>Mission non trouvée ou inaccessible</p>
    </div>

    <div v-else-if="mission.statut === 'TERMINEE' && !evaluationExistante">
      <!-- Récapitulatif Mission -->
      <Card class="mb-4">
        <template #title>
          <div class="flex align-items-center">
            <i class="pi pi-check-circle text-green-500 mr-2"></i>
            Mission Terminée
          </div>
        </template>
        <template #content>
          <div class="grid">
            <div class="col-12 md:col-8">
              <h3 class="mt-0 mb-3">{{ mission.demandeService?.titre }}</h3>
              <p class="text-600 mb-3">{{ mission.demandeService?.description }}</p>
              
              <div class="grid mb-3">
                <div class="col-12 md:col-6">
                  <div class="flex align-items-center mb-2 text-sm">
                    <i class="pi pi-calendar mr-2 text-green-500"></i>
                    <span>{{ formatDate(mission.demandeService?.dateSouhaitee) }}</span>
                  </div>
                  <div class="flex align-items-center mb-2 text-sm">
                    <i class="pi pi-map-marker mr-2 text-red-500"></i>
                    <span>{{ mission.demandeService?.adresseDepart }}</span>
                  </div>
                </div>
                <div class="col-12 md:col-6">
                  <div class="flex align-items-center mb-2 text-sm">
                    <i class="pi pi-euro mr-2 text-orange-500"></i>
                    <span>Prix convenu : {{ mission.prixAccepte }}€</span>
                  </div>
                  <div class="flex align-items-center mb-2 text-sm">
                    <i class="pi pi-clock mr-2 text-blue-500"></i>
                    <span>Terminée le {{ formatDate(mission.dateTerminaison) }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="col-12 md:col-4">
              <!-- Prestataire -->
              <div class="text-center">
                <Avatar 
                  :label="getInitials(mission.prestataire)"
                  class="bg-primary text-white mb-2"
                  shape="circle"
                  size="large"
                />
                <h4 class="m-0">{{ mission.prestataire?.prenom }} {{ mission.prestataire?.nom }}</h4>
                <div class="flex align-items-center justify-content-center mt-1">
                  <EvaluationStars 
                    :model-value="mission.prestataire?.noteMoyenne || 0"
                    readonly
                    size="small"
                    :show-label="false"
                  />
                  <span class="text-sm text-600 ml-2">
                    ({{ mission.prestataire?.totalEvaluations || 0 }} avis)
                  </span>
                </div>
              </div>
            </div>
          </div>
        </template>
      </Card>

      <!-- Message final du prestataire -->
      <Card v-if="mission.noteFinale" class="mb-4">
        <template #title>
          <div class="flex align-items-center">
            <i class="pi pi-comment mr-2"></i>
            Message du prestataire
          </div>
        </template>
        <template #content>
          <div class="p-3 surface-100 border-round">
            <p class="m-0 text-900">{{ mission.noteFinale }}</p>
          </div>
        </template>
      </Card>

      <!-- Formulaire d'évaluation -->
      <Card class="mb-4">
        <template #title>
          <div class="flex align-items-center">
            <i class="pi pi-star mr-2"></i>
            Évaluez cette prestation
            <Tag value="Obligatoire" severity="warning" class="ml-2" />
          </div>
        </template>
        <template #content>
          <p class="text-600 mb-4">
            Votre évaluation aide à maintenir la qualité de service sur EcoDeli et guide les futurs clients.
          </p>

          <!-- Note globale -->
          <div class="mb-4">
            <label class="font-semibold mb-2 block">Note globale *</label>
            <EvaluationStars 
              v-model="evaluation.noteGlobale"
              size="large"
              @change="onNoteGlobaleChange"
            />
            <small v-if="errors.noteGlobale" class="p-error">{{ errors.noteGlobale }}</small>
          </div>

          <!-- Notes détaillées -->
          <div class="mb-4">
            <h4 class="text-lg font-medium mb-3">Notes détaillées *</h4>
            <div class="grid">
              <div class="col-12 md:col-6 mb-3">
                <label class="font-medium mb-2 block">Qualité du travail</label>
                <EvaluationStars 
                  v-model="evaluation.noteQualite"
                  size="medium"
                  :show-label="false"
                />
                <small class="text-600">Le résultat correspond-il à vos attentes ?</small>
              </div>
              
              <div class="col-12 md:col-6 mb-3">
                <label class="font-medium mb-2 block">Respect des délais</label>
                <EvaluationStars 
                  v-model="evaluation.noteDelais"
                  size="medium"
                  :show-label="false"
                />
                <small class="text-600">La mission a-t-elle été réalisée dans les temps ?</small>
              </div>
              
              <div class="col-12 md:col-6 mb-3">
                <label class="font-medium mb-2 block">Communication</label>
                <EvaluationStars 
                  v-model="evaluation.noteCommunication"
                  size="medium"
                  :show-label="false"
                />
                <small class="text-600">Le prestataire était-il disponible et clair ?</small>
              </div>
              
              <div class="col-12 md:col-6 mb-3">
                <label class="font-medium mb-2 block">Professionnalisme</label>
                <EvaluationStars 
                  v-model="evaluation.noteProfessionnalisme"
                  size="medium"
                  :show-label="false"
                />
                <small class="text-600">Comportement, présentation, savoir-être</small>
              </div>
            </div>
          </div>

          <!-- Commentaire -->
          <div class="mb-4">
            <label for="commentaire" class="font-medium mb-2 block">Commentaire (optionnel)</label>
            <Textarea 
              id="commentaire"
              v-model="evaluation.commentaire"
              rows="4"
              placeholder="Décrivez votre expérience, ce qui vous a plu, les points d'amélioration..."
              class="w-full"
              :maxlength="500"
            />
            <div class="text-right text-sm text-600 mt-1">
              {{ evaluation.commentaire?.length || 0 }}/500 caractères
            </div>
          </div>

          <!-- Résumé temps réel -->
          <div v-if="evaluation.noteGlobale > 0" class="mb-4 p-3 surface-50 border-round">
            <h5 class="text-sm font-medium text-600 mb-2">Aperçu de votre évaluation :</h5>
            <div class="flex align-items-center gap-2">
              <EvaluationStars 
                :model-value="evaluation.noteGlobale"
                readonly
                size="medium"
              />
              <span class="font-medium">{{ getNoteLabel(evaluation.noteGlobale) }}</span>
            </div>
          </div>
        </template>
      </Card>

      <!-- Actions -->
      <Card>
        <template #content>
          <div class="flex justify-content-between align-items-center">
            <div class="text-sm text-600">
              <i class="pi pi-info-circle mr-1"></i>
              L'évaluation est obligatoire pour valider la mission
            </div>
            
            <div class="flex gap-2">
              <Button 
                label="Signaler un problème" 
                icon="pi pi-exclamation-triangle"
                severity="danger"
                outlined
                @click="signalerProbleme"
              />
              <Button
                label="Valider et payer la mission"
                icon="pi pi-credit-card"
                :disabled="!isEvaluationComplete"
                @click="validerMission"
                :loading="loadingValidation"
              />
            </div>
          </div>
        </template>
      </Card>
    </div>

    <!-- Modal évaluation existante -->
    <Dialog 
      v-model:visible="afficherEvaluationExistante" 
      header="Votre évaluation" 
      modal
      :style="{ width: '40rem' }"
    >
      <div v-if="evaluationExistante" class="evaluation-details">
        <div class="mb-3">
          <h4 class="mb-2">Note globale</h4>
          <EvaluationStars 
            :model-value="evaluationExistante.noteGlobale"
            readonly
            size="large"
          />
        </div>
        
        <div class="grid mb-3">
          <div class="col-6">
            <strong>Qualité :</strong> 
            <EvaluationStars 
              :model-value="evaluationExistante.noteQualite"
              readonly
              size="small"
              :show-label="false"
            />
          </div>
          <div class="col-6">
            <strong>Délais :</strong>
            <EvaluationStars 
              :model-value="evaluationExistante.noteDelais"
              readonly
              size="small"
              :show-label="false"
            />
          </div>
          <div class="col-6">
            <strong>Communication :</strong>
            <EvaluationStars 
              :model-value="evaluationExistante.noteCommunication"
              readonly
              size="small"
              :show-label="false"
            />
          </div>
          <div class="col-6">
            <strong>Professionnalisme :</strong>
            <EvaluationStars 
              :model-value="evaluationExistante.noteProfessionnalisme"
              readonly
              size="small"
              :show-label="false"
            />
          </div>
        </div>
        
        <div v-if="evaluationExistante.commentaire" class="mb-3">
          <h5>Votre commentaire :</h5>
          <p class="p-3 surface-100 border-round">{{ evaluationExistante.commentaire }}</p>
        </div>
        
        <div class="text-sm text-600">
          Évaluation donnée le {{ formatDate(evaluationExistante.dateEvaluation) }}
        </div>
      </div>
    </Dialog>

    <Toast />
    <ConfirmDialog />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import EvaluationStars from '@/components/EvaluationStars.vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

const mission = ref(null)
const evaluationExistante = ref(null)
const loading = ref(false)
const loadingValidation = ref(false)
const afficherEvaluationExistante = ref(false)
const errors = ref({})

const evaluation = ref({
  noteGlobale: 0,
  noteQualite: 0,
  noteDelais: 0,
  noteCommunication: 0,
  noteProfessionnalisme: 0,
  commentaire: ''
})

const missionId = computed(() => {
  return parseInt(route.params.id)
})

const isEvaluationComplete = computed(() => {
  return evaluation.value.noteGlobale > 0 &&
         evaluation.value.noteQualite > 0 &&
         evaluation.value.noteDelais > 0 &&
         evaluation.value.noteCommunication > 0 &&
         evaluation.value.noteProfessionnalisme > 0
})

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

const getNoteLabel = (note) => {
  const labels = {
    0: '',
    1: 'Insuffisant',
    2: 'Passable',
    3: 'Bien',
    4: 'Très bien',
    5: 'Excellent'
  }
  return labels[note] || ''
}

const chargerMission = async () => {
  loading.value = true
  
  try {
    const missionResponse = await axios.get(`http://localhost:8080/api/missions/${missionId.value}`)
    if (missionResponse.data) {
      mission.value = missionResponse.data
    }
    
    try {
      const evaluationResponse = await axios.get(`http://localhost:8080/api/missions/${missionId.value}/evaluation`)
      if (evaluationResponse.data) {
        evaluationExistante.value = evaluationResponse.data
      }
    } catch (e) {
      evaluationExistante.value = null
    }
    
  } catch (error) {
    console.error('Erreur chargement mission:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger les détails de la mission',
      life: 3000
    })
  } finally {
    loading.value = false
  }
}

const onNoteGlobaleChange = (newValue) => {
  if (evaluation.value.noteQualite === 0) evaluation.value.noteQualite = newValue
  if (evaluation.value.noteDelais === 0) evaluation.value.noteDelais = newValue
  if (evaluation.value.noteCommunication === 0) evaluation.value.noteCommunication = newValue
  if (evaluation.value.noteProfessionnalisme === 0) evaluation.value.noteProfessionnalisme = newValue
}

const validerEvaluation = () => {
  errors.value = {}
  
  if (evaluation.value.noteGlobale === 0) {
    errors.value.noteGlobale = 'La note globale est obligatoire'
  }
  
  return Object.keys(errors.value).length === 0
}

const obtenirPrixReel = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/api/missions/${missionId.value}/prix-calcule`)
    
    if (response.data && response.data.prixTotal) {
      console.log('Prix réel récupéré:', response.data.prixTotal, '€')
      console.log('Détails tarif:', response.data)
      return response.data.prixTotal
    }
    
    console.warn('Pas de prix calculé, utilisation fallback')
    return mission.value.prixAccepte || 25
    
  } catch (error) {
    console.warn('Erreur récupération prix réel:', error)
    return mission.value.prixAccepte || 25
  }
}

const validerMission = async () => {
  if (!validerEvaluation()) {
    toast.add({
      severity: 'warn',
      summary: 'Évaluation incomplète',
      detail: 'Veuillez donner une note globale',
      life: 3000
    })
    return
  }
  
  const montantMission = await obtenirPrixReel()
  
  confirm.require({
    message: `Confirmer l'évaluation ${evaluation.value.noteGlobale}/5 (${getNoteLabel(evaluation.value.noteGlobale)}) et payer ${montantMission}€ ?`,
    header: 'Valider et payer la mission',
    icon: 'pi pi-credit-card',
    acceptClass: 'p-button-success',
    acceptLabel: 'Confirmer le paiement',
    rejectLabel: 'Annuler',
    accept: async () => {
      await executerValidation()
    }
  })
}

const executerValidation = async () => {
  loadingValidation.value = true
  
  try {
    const evaluationData = {
      noteGlobale: evaluation.value.noteGlobale,
      noteQualite: evaluation.value.noteQualite,
      noteDelais: evaluation.value.noteDelais,
      noteCommunication: evaluation.value.noteCommunication,
      noteProfessionnalisme: evaluation.value.noteProfessionnalisme,
      commentaire: evaluation.value.commentaire
    }
    
    const response = await axios.post(`http://localhost:8080/api/missions/${missionId.value}/evaluation`, evaluationData)
    
    if (response.data.success) {
      let toastMessage = 'Votre évaluation a été enregistrée avec succès'
      
      if (response.data.paiementEffectue) {
        toastMessage += ` et le paiement de ${response.data.montantPaye}€ a été effectué.`
      }
      
      toast.add({
        severity: 'success',
        summary: 'Mission validée et payée !',
        detail: toastMessage,
        life: 5000
      })
      
      await chargerMission()
      
      setTimeout(() => {
        const fromMesServices = sessionStorage.getItem('fromMesServices')
        if (fromMesServices) {
          sessionStorage.removeItem('fromMesServices')
          router.push('/client/services')
        } else {
          router.push('/client')
        }
      }, 2000)
    }
    
  } catch (error) {
    console.error('Erreur validation mission:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: error.response?.data?.error || 'Erreur lors de la validation',
      life: 3000
    })
  } finally {
    loadingValidation.value = false
  }
}

const signalerProbleme = () => {
  toast.add({
    severity: 'info',
    summary: 'Fonctionnalité à venir',
    detail: 'Le signalement de problème sera disponible prochainement',
    life: 3000
  })
}

const retourDashboard = () => {
  router.push('/client')
}

onMounted(() => {
  chargerMission()
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
.col-4 { grid-column: span 4; }
.col-8 { grid-column: span 8; }

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
  .md\:col-4 { grid-column: span 4; }
  .md\:col-8 { grid-column: span 8; }
}

.bg-primary {
  background: var(--ecodeli-green) !important;
}

.evaluation-details h4,
.evaluation-details h5 {
  margin-top: 0;
}
</style>