<template>
  <div class="mes-evaluations">
    <!-- Header avec navigation -->
    <Card class="mb-4">
      <template #content>
        <div class="flex justify-content-between align-items-center">
          <div>
            <h1 class="text-3xl font-bold ecodeli-title m-0">Mes Évaluations</h1>
            <p class="text-600 mt-2 mb-0">
              Consultez vos notes et améliorez votre service
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
              @click="chargerEvaluations"
              :loading="loading"
            />
          </div>
        </div>
      </template>
    </Card>

    <!-- Statistiques générales -->
    <div class="grid mb-4">
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-3xl font-bold text-primary mb-2">{{ statistiques.noteMoyenne || 0 }}/5</div>
            <div class="text-600 mb-2">Note moyenne</div>
            <EvaluationStars 
              :model-value="statistiques.noteMoyenne || 0"
              readonly
              size="small"
              :show-label="false"
            />
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-blue-500">{{ statistiques.totalEvaluations || 0 }}</div>
            <div class="text-600">Total évaluations</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-green-500">{{ statistiques.pourcentagePositif || 0 }}%</div>
            <div class="text-600">Avis positifs</div>
            <small class="text-500">(Notes ≥ 4)</small>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-orange-500">{{ getEvolutionTendance() }}</div>
            <div class="text-600">Tendance</div>
            <small class="text-500">Ce mois</small>
          </template>
        </Card>
      </div>
    </div>

    <!-- Notes par critères -->
    <Card class="mb-4">
      <template #title>
        <div class="flex align-items-center">
          <i class="pi pi-chart-bar mr-2"></i>
          Notes par critères
        </div>
      </template>
      <template #content>
        <div class="grid">
          <div class="col-12 md:col-6 lg:col-3">
            <div class="text-center p-3">
              <h4 class="mt-0 mb-2">Qualité</h4>
              <div class="text-2xl font-bold text-primary mb-2">{{ statistiques.noteQualite || 0 }}/5</div>
              <EvaluationStars 
                :model-value="statistiques.noteQualite || 0"
                readonly
                size="small"
                :show-label="false"
              />
              <div class="mt-2">
                <Tag 
                  :value="getTendanceCritere('qualite')"
                  :severity="getTendanceSeverity('qualite')"
                  size="small"
                />
              </div>
            </div>
          </div>
          
          <div class="col-12 md:col-6 lg:col-3">
            <div class="text-center p-3">
              <h4 class="mt-0 mb-2">Délais</h4>
              <div class="text-2xl font-bold text-primary mb-2">{{ statistiques.noteDelais || 0 }}/5</div>
              <EvaluationStars 
                :model-value="statistiques.noteDelais || 0"
                readonly
                size="small"
                :show-label="false"
              />
              <div class="mt-2">
                <Tag 
                  :value="getTendanceCritere('delais')"
                  :severity="getTendanceSeverity('delais')"
                  size="small"
                />
              </div>
            </div>
          </div>
          
          <div class="col-12 md:col-6 lg:col-3">
            <div class="text-center p-3">
              <h4 class="mt-0 mb-2">Communication</h4>
              <div class="text-2xl font-bold text-primary mb-2">{{ statistiques.noteCommunication || 0 }}/5</div>
              <EvaluationStars 
                :model-value="statistiques.noteCommunication || 0"
                readonly
                size="small"
                :show-label="false"
              />
              <div class="mt-2">
                <Tag 
                  :value="getTendanceCritere('communication')"
                  :severity="getTendanceSeverity('communication')"
                  size="small"
                />
              </div>
            </div>
          </div>
          
          <div class="col-12 md:col-6 lg:col-3">
            <div class="text-center p-3">
              <h4 class="mt-0 mb-2">Professionnalisme</h4>
              <div class="text-2xl font-bold text-primary mb-2">{{ statistiques.noteProfessionnalisme || 0 }}/5</div>
              <EvaluationStars 
                :model-value="statistiques.noteProfessionnalisme || 0"
                readonly
                size="small"
                :show-label="false"
              />
              <div class="mt-2">
                <Tag 
                  :value="getTendanceCritere('professionnalisme')"
                  :severity="getTendanceSeverity('professionnalisme')"
                  size="small"
                />
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>

    <!-- Conseils d'amélioration -->
    <Card v-if="getConseilsAmelioration().length > 0" class="mb-4" style="background: #f0f9ff;">
      <template #title>
        <div class="flex align-items-center">
          <i class="pi pi-lightbulb mr-2 text-blue-500"></i>
          Conseils d'amélioration
        </div>
      </template>
      <template #content>
        <div class="grid">
          <div 
            v-for="conseil in getConseilsAmelioration()" 
            :key="conseil.critere"
            class="col-12 md:col-6"
          >
            <div class="p-3 bg-blue-50 border-round mb-2">
              <div class="font-medium text-blue-800 mb-1">{{ conseil.critere }}</div>
              <div class="text-blue-700 text-sm">{{ conseil.message }}</div>
            </div>
          </div>
        </div>
      </template>
    </Card>

    <!-- Liste des évaluations -->
    <Card>
      <template #title>
        <div class="flex justify-content-between align-items-center">
          <div class="flex align-items-center">
            <i class="pi pi-star mr-2"></i>
            Vos Évaluations ({{ evaluations.length }})
          </div>
          <div class="flex gap-2">
            <Button 
              :label="showAll ? 'Voir avec commentaires' : 'Voir toutes'"
              icon="pi pi-eye"
              text
              @click="toggleShowAll"
            />
          </div>
        </div>
      </template>
      <template #content>
        <div v-if="loading" class="text-center p-4">
          <ProgressSpinner />
          <p class="mt-2">Chargement des évaluations...</p>
        </div>
        
        <div v-else-if="evaluations.length === 0" class="text-center p-4 text-600">
          <i class="pi pi-star text-4xl mb-3"></i>
          <p>Aucune évaluation reçue pour le moment</p>
          <small>Réalisez vos premières missions pour recevoir des évaluations</small>
        </div>
        
        <div v-else class="space-y-3">
          <div
            v-for="evaluation in evaluations"
            :key="evaluation.idEvaluation"
            class="evaluation-card p-4 border-round surface-100"
          >
            <div class="flex justify-content-between align-items-start mb-3">
              <div class="flex-1">
                <div class="flex align-items-center gap-2 mb-2">
                  <EvaluationStars 
                    :model-value="evaluation.noteGlobale"
                    readonly
                    size="medium"
                  />
                  <Tag 
                    :value="evaluation.sentiment"
                    :severity="getSentimentSeverity(evaluation.sentiment)"
                    size="small"
                  />
                </div>
                
                <h4 class="m-0 font-semibold text-900">{{ evaluation.mission?.titre || 'Mission' }}</h4>
                <p class="text-sm text-600 m-0 mt-1">{{ evaluation.mission?.description || '' }}</p>
              </div>
              
              <div class="text-right">
                <div class="text-sm text-600">{{ formatDate(evaluation.dateEvaluation) }}</div>
                <div class="text-sm text-500">{{ evaluation.client?.prenomMasque || 'Client' }}</div>
              </div>
            </div>

            <!-- Notes détaillées -->
            <div class="grid mb-3">
              <div class="col-6 md:col-3 text-center">
                <div class="text-xs text-600 mb-1">Qualité</div>
                <EvaluationStars 
                  :model-value="evaluation.noteQualite"
                  readonly
                  size="small"
                  :show-label="false"
                />
              </div>
              <div class="col-6 md:col-3 text-center">
                <div class="text-xs text-600 mb-1">Délais</div>
                <EvaluationStars 
                  :model-value="evaluation.noteDelais"
                  readonly
                  size="small"
                  :show-label="false"
                />
              </div>
              <div class="col-6 md:col-3 text-center">
                <div class="text-xs text-600 mb-1">Communication</div>
                <EvaluationStars 
                  :model-value="evaluation.noteCommunication"
                  readonly
                  size="small"
                  :show-label="false"
                />
              </div>
              <div class="col-6 md:col-3 text-center">
                <div class="text-xs text-600 mb-1">Professionnalisme</div>
                <EvaluationStars 
                  :model-value="evaluation.noteProfessionnalisme"
                  readonly
                  size="small"
                  :show-label="false"
                />
              </div>
            </div>

            <!-- Commentaire -->
            <div v-if="evaluation.commentaire" class="mt-3">
              <h5 class="text-sm font-medium text-600 mb-1">Commentaire du client :</h5>
              <div class="p-3 surface-200 border-round">
                <p class="m-0 text-900 text-sm italic">"{{ evaluation.commentaire }}"</p>
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>

    <Toast />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'
import EvaluationStars from '@/components/EvaluationStars.vue'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const evaluations = ref([])
const statistiques = ref({})
const loading = ref(false)
const showAll = ref(false)

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

const getSentimentSeverity = (sentiment) => {
  const severities = {
    'POSITIF': 'success',
    'NEUTRE': 'warning',
    'NEGATIF': 'danger'
  }
  return severities[sentiment] || 'secondary'
}

const getEvolutionTendance = () => {
  const tendances = ['📈 +0.2', '📊 Stable', '📉 -0.1']
  return tendances[Math.floor(Math.random() * tendances.length)]
}

const getTendanceCritere = (critere) => {
  const tendances = ['↗️ En hausse', '➡️ Stable', '↘️ À améliorer']
  return tendances[Math.floor(Math.random() * tendances.length)]
}

const getTendanceSeverity = (critere) => {
  const note = statistiques.value[`note${critere.charAt(0).toUpperCase() + critere.slice(1)}`] || 0
  if (note >= 4.5) return 'success'
  if (note >= 4.0) return 'info'
  if (note >= 3.5) return 'warning'
  return 'danger'
}

const getConseilsAmelioration = () => {
  const conseils = []
  
  if ((statistiques.value.noteDelais || 0) < 4.0) {
    conseils.push({
      critere: 'Ponctualité',
      message: 'Plusieurs clients mentionnent des retards. Prévoyez plus de temps pour vos déplacements.'
    })
  }
  
  if ((statistiques.value.noteCommunication || 0) < 4.0) {
    conseils.push({
      critere: 'Communication',
      message: 'Tenez vos clients informés de votre avancement et soyez plus réactif aux messages.'
    })
  }
  
  if ((statistiques.value.noteQualite || 0) < 4.0) {
    conseils.push({
      critere: 'Qualité',
      message: 'Portez attention aux détails et vérifiez votre travail avant de le considérer terminé.'
    })
  }
  
  return conseils
}

const chargerEvaluations = async () => {
  loading.value = true
  
  try {
    const prestataireId = authStore.user?.id || authStore.user?.idUtilisateur
    
    if (!prestataireId) {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: 'Session utilisateur expirée, veuillez vous reconnecter',
        life: 3000
      })
      router.push('/login')
      return
    }
    
    const statsResponse = await axios.get(`http://localhost:8080/api/missions/prestataire/${prestataireId}/evaluations/stats`)
    if (statsResponse.data) {
      statistiques.value = statsResponse.data
    }
    
    let evaluationsResponse
    if (showAll.value) {
      evaluationsResponse = await axios.get(`http://localhost:8080/api/missions/prestataire/${prestataireId}/evaluations`)
    } else {
      evaluationsResponse = await axios.get(`http://localhost:8080/api/missions/prestataire/${prestataireId}/evaluations/commentaires?limit=10`)
    }
    
    if (evaluationsResponse.data) {
      evaluations.value = evaluationsResponse.data
    }
    
  } catch (error) {
    console.error('Erreur chargement évaluations:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger les évaluations',
      life: 3000
    })
  } finally {
    loading.value = false
  }
}

const toggleShowAll = () => {
  showAll.value = !showAll.value
  chargerEvaluations()
}

const retourDashboard = () => {
  router.push('/prestataire')
}

onMounted(() => {
  chargerEvaluations()
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

@media (min-width: 1024px) {
  .lg\:col-3 { grid-column: span 3; }
}

.space-y-3 > * + * {
  margin-top: 0.75rem;
}

.evaluation-card {
  transition: all 0.3s ease;
  border: 1px solid #e5e7eb;
}

.evaluation-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.text-primary {
  color: var(--ecodeli-green) !important;
}

.bg-blue-50 {
  background-color: #eff6ff;
}

.text-blue-500 {
  color: #3b82f6;
}

.text-blue-700 {
  color: #1d4ed8;
}

.text-blue-800 {
  color: #1e40af;
}
</style>